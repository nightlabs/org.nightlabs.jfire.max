/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.trade;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.Struct;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerAbortOffer;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerFinalizeOffer;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.Util;

/**
 * An Offer is basically a collection of {@link Article}s along with
 * status information.
 *
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author marco schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
@PersistenceCapable(
	objectIdClass=OfferID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_Offer")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@FetchGroups({
	@FetchGroup(
			name=Offer.FETCH_GROUP_OFFER_LOCAL,
			members=@Persistent(name=Offer.FieldName.offerLocal)
	),
	@FetchGroup(
			name=Offer.FETCH_GROUP_PRICE,
			members=@Persistent(name=Offer.FieldName.price)
	),
	@FetchGroup(
			name=Offer.FETCH_GROUP_ARTICLES,
			members=@Persistent(name=Offer.FieldName.articles)
	),
	@FetchGroup(
			name=Offer.FETCH_GROUP_CURRENCY,
			members=@Persistent(name=Offer.FieldName.currency)
	),
	@FetchGroup(
			name=Offer.FETCH_GROUP_ORDER,
			members=@Persistent(name=Offer.FieldName.order)
	),
	@FetchGroup(
			name=Offer.FETCH_GROUP_CREATE_USER,
			members=@Persistent(name=Offer.FieldName.createUser)
	),
	@FetchGroup(
			name=Offer.FETCH_GROUP_FINALIZE_USER,
			members=@Persistent(name=Offer.FieldName.finalizeUser)
	),
	@FetchGroup(
			name=Offer.FETCH_GROUP_SEGMENTS,
			members=@Persistent(name=Offer.FieldName.segments)
	),
	@FetchGroup(
			name=Statable.FETCH_GROUP_STATE,
			members=@Persistent(name=Offer.FieldName.state)
	),
	@FetchGroup(
			name=Statable.FETCH_GROUP_STATES,
			members=@Persistent(name=Offer.FieldName.states)
	),
	@FetchGroup(
			name=FetchGroupsTrade.FETCH_GROUP_ARTICLE_CONTAINER_IN_EDITOR,
			//		name="FetchGroupsTrade.articleContainerInEditor",
			members={
					@Persistent(name=Offer.FieldName.offerLocal),
					@Persistent(name=Offer.FieldName.segments),
					@Persistent(name=Offer.FieldName.createUser),
					@Persistent(name=Offer.FieldName.currency),
					@Persistent(name=Offer.FieldName.finalizeUser),
					@Persistent(name=Offer.FieldName.order),
					@Persistent(name=Offer.FieldName.price),
					@Persistent(name=Offer.FieldName.state),
					@Persistent(name=Offer.FieldName.states),
			}
	),
	@FetchGroup(
			name=ArticleContainer.FETCH_GROUP_PROPERTY_SET,
			members=@Persistent(name=Offer.FieldName.propertySet)
	),
	@FetchGroup(
			name=ArticleContainer.FETCH_GROUP_ARTICLES,
			members=@Persistent(name=Offer.FieldName.articles)
	),
//	@FetchGroup(
//			name=ArticleContainer.FETCH_GROUP_CUSTOMER,
//			members=@Persistent(name=Offer.FieldName.customer)
//	),
//	@FetchGroup(
//			name=ArticleContainer.FETCH_GROUP_VENDOR,
//			members=@Persistent(name=Offer.FieldName.vendor)
//	)
})
@Queries(
		@javax.jdo.annotations.Query(
				name="getNonFinalizedNonEndedOffersForOrder",
				value="SELECT WHERE this.order == :order && this.finalizeDT == null && this.offerLocal.processEnded == false ORDER BY offerID DESCENDING")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Offer
implements
		Serializable,
		ArticleContainer,
		SegmentContainer,
		Statable,
		DetachCallback,
		AttachCallback
{
	private static final long serialVersionUID = 20090629;

	private static final Logger logger = Logger.getLogger(Offer.class);

	public static final String FETCH_GROUP_OFFER_LOCAL = "Offer.offerLocal";
	public static final String FETCH_GROUP_PRICE = "Offer.price";
	public static final String FETCH_GROUP_ARTICLES = "Offer.articles";
	public static final String FETCH_GROUP_CURRENCY = "Offer.currency";
	public static final String FETCH_GROUP_ORDER = "Offer.order";
	public static final String FETCH_GROUP_CREATE_USER = "Offer.createUser";
	public static final String FETCH_GROUP_FINALIZE_USER = "Offer.finalizeUser";
	public static final String FETCH_GROUP_SEGMENTS = "Offer.segments";

	public static final class FieldName {
		public static final String offerLocal = "offerLocal";
		public static final String price = "price";
		public static final String articles = "articles";
		public static final String currency = "currency";
		public static final String customer = "customer";
		public static final String order = "order";
		public static final String createUser = "createUser";
		public static final String finalizeUser = "finalizeUser";
		public static final String segments = "segments";
		public static final String state = "state";
		public static final String states = "states";
		public static final String propertySet = "propertySet";
		public static final String vendor = "vendor";
	}

	/**
	 * @return a <tt>Collection</tt> of <tt>Offer</tt>
	 */
	@SuppressWarnings("unchecked")
	public static Collection<Offer> getNonFinalizedNonEndedOffers(PersistenceManager pm, Order order)
	{
		Query query = pm.newNamedQuery(Offer.class, "getNonFinalizedNonEndedOffersForOrder");
		return (Collection<Offer>) query.execute(order);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	@PrimaryKey
	@Column(length=50)
	private String offerIDPrefix;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long offerID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="offer" dependent="true"
	 */
	@Persistent(
		dependent="true",
		mappedBy="offer",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private OfferLocal offerLocal;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Order order;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Currency currency;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="Segment"
	 *		table="JFireTrade_Offer_segments"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		table="JFireTrade_Offer_segments",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<Segment> segments;

	/**
	 * An Offer is stable, if it does not change values when prices are recalculated.
	 * Only stable Offers can be confirmed. An unstable Offer can come into existence
	 * if conflicting PriceConfigs of two or more OfferItems interfere.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean stable;

	/**
	 * An Offer is only valid if it is stable and has been validated. It needs to be
	 * valid, before it can be finalized.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean valid = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date createDT;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User createUser;

	/**
	 * An <tt>Offer</tt> is finalized, before it is sent on an asynchronous way (e.g. letter,
	 * email) to the customer. This avoids communication conflicts as it forces the generation
	 * of a new <tt>Offer</tt> if the customer requests changes after reception.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date finalizeDT = null;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User finalizeUser = null;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date abortDT = null;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User abortUser = null;


	/**
	 * Instances of {@link Article}.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="Article"
	 *		dependent-value="true"
	 *		mapped-by="offer"
	 */
	@Persistent(
			mappedBy="offer",
			persistenceModifier=PersistenceModifier.PERSISTENT
	)
	@Element(dependent="true")
	private Set<Article> articles;

	/**
	 * This member represents the sum of all prices of all offer items.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price price;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean containsPricesDependentOnOffer = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private LegalEntity vendor = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean vendor_detached = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private LegalEntity customer = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean customer_detached = false;

//	/**
//	 * @jdo.field persistence-modifier="none"
//	 */
//	@Persistent(persistenceModifier=PersistenceModifier.NONE)
//	private LegalEntity endCustomer = null;
//
//	/**
//	 * @jdo.field persistence-modifier="none"
//	 */
//	@Persistent(persistenceModifier=PersistenceModifier.NONE)
//	private boolean endCustomer_detached = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private AnchorID vendorID = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean vendorID_detached = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private AnchorID customerID = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean customerID_detached = false;

//	/**
//	 * @jdo.field persistence-modifier="none"
//	 */
//	@Persistent(persistenceModifier=PersistenceModifier.NONE)
//	private AnchorID endCustomerID = null;
//
//	/**
//	 * @jdo.field persistence-modifier="none"
//	 */
//	@Persistent(persistenceModifier=PersistenceModifier.NONE)
//	private boolean endCustomerID_detached = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private State state;

	/**
	 * This is the history of <b>public</b> {@link State}s with the newest last and the oldest first.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="State"
	 *		table="JFireTrade_Offer_states"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_Offer_states",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private List<State> states;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int articleCount = 0;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date expiryTimestampUnfinalized;

	private boolean expiryTimestampUnfinalizedAutoManaged = true;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date expiryTimestampFinalized;

	private boolean expiryTimestampFinalizedAutoManaged = true;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private PropertySet propertySet;

	/**
	 * @deprecated This constructor exists only for JDO!
	 */
	@Deprecated
	protected Offer() { }

	/**
	 * Create a new Offer for the given Order and with the given primary-key-values.
	 *
	 * @param user The user that initiated the creation of the new {@link Offer}.
	 * @param order The {@link Order} the new {@link Offer} should be part of.
	 * @param offerIDPrefix The offerIDPrefix primary-key-value.
	 * @param offerID The offerID primary-key-value.
	 */
	public Offer(User user, Order order, String offerIDPrefix, long offerID)
	{
		if (order == null)
			throw new IllegalArgumentException("order must not be null!");

		ObjectIDUtil.assertValidIDString(offerIDPrefix, "offerIDPrefix");

		if (offerID < 0)
			throw new IllegalArgumentException("offerID < 0");

		PersistenceManager pm = JDOHelper.getPersistenceManager(order);
		if (pm == null)
			throw new IllegalStateException("order is not persistent! Could not get a PersistenceManager from it!");

//		Accounting accounting = Accounting.getAccounting(pm);

		this.order = order;
		this.organisationID = order.getOrganisationID();
		this.offerIDPrefix = offerIDPrefix;
		this.offerID = offerID;
		this.currency = order.getCurrency();
		this.primaryKey = getPrimaryKey(this.organisationID, this.offerIDPrefix, this.offerID);
		this.createDT = new Date();
		this.createUser = user;

//		AccountingPriceConfig accountingPriceConfig = accounting.getAccountingPriceConfig();
		this.price = new Price(IDGenerator.getOrganisationID(), IDGenerator.nextID(Price.class), order.getCurrency());
//				accountingPriceConfig.getOrganisationID(), accountingPriceConfig.getPriceConfigID(),
//				accountingPriceConfig.createPriceID(), order.getCurrency());

		articles = new HashSet<Article>();

		String structScope = Struct.DEFAULT_SCOPE;
		String structLocalScope = StructLocal.DEFAULT_SCOPE;
		this.propertySet = new PropertySet(
				organisationID, IDGenerator.nextID(PropertySet.class),
				Organisation.DEV_ORGANISATION_ID,
				Offer.class.getName(), structScope, structLocalScope);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	public String getOfferIDPrefix()
	{
		return offerIDPrefix;
	}

	public String getArticleContainerIDPrefix()
	{
		return getOfferIDPrefix();
	}

	/**
	 * @return Returns the offerID.
	 */
	public long getOfferID()
	{
		return offerID;
	}

	public long getArticleContainerID()
	{
		return getOfferID();
	}

	public String getOfferIDAsString()
	{
		return ObjectIDUtil.longObjectIDFieldToString(offerID);
	}

	public String getArticleContainerIDAsString()
	{
		return getOfferIDAsString();
	}

	public static String getPrimaryKey(String organisationID, String offerIDPrefix, long offerID)
	{
		return organisationID + '/' + offerIDPrefix + '/' + ObjectIDUtil.longObjectIDFieldToString(offerID);
	}

	public String getPrimaryKey()
	{
		return primaryKey;
	}

	/**
	 * @return the same as {@link #getStatableLocal()}
	 */
	public OfferLocal getOfferLocal()
	{
		return offerLocal;
	}

	public StatableLocal getStatableLocal()
	{
		return offerLocal;
	}

	protected void setOfferLocal(OfferLocal offerLocal)
	{
		this.offerLocal = offerLocal;
	}

	/**
	 * This method will be called to check the validity of the given
	 * articles before they are added to the list of {@link Article}s
	 * of this {@link Offer}.
	 * <p>
	 * This implementation does nothing, implementations might throw
	 * a RuntimeException if one of the articles is invalid.
	 * </p>
	 * @param articles The articles to check.
	 */
	protected void checkArticles(Collection<? extends Article> articles) {

	}

	public void addArticles(Collection<? extends Article> articles)
	{
		checkArticles(articles);

		if (isFinalized())
			throw new IllegalStateException("This offer is already finalized! Cannot add a new Article!");

		if (isAborted())
			throw new IllegalStateException("This offer is already aborted! Cannot add a new Article!");

		// Whenever an Offer is changed, it needs to be marked as not valid to force a new validation!
		this.valid = false;

		for (Article article : articles) {
			if (!this.equals(article.getOffer()))
				throw new IllegalArgumentException("article.offer != this");

			this.articles.add(article);
			this.order.addArticle(article);
		}

		this.articleCount = this.articles.size();
	}

	public void addArticle(Article article)
	{
		checkArticles(Collections.singleton(article));
		if (isFinalized())
			throw new IllegalStateException("This offer is already finalized! Cannot add a new Article!");

		if (isAborted())
			throw new IllegalStateException("This offer is already aborted! Cannot add a new Article!");

		// Whenever an Offer is changed, it needs to be marked as not valid to force a new validation!
		this.valid = false;

		if (!JDOHelper.getObjectId(this).equals(article.getOfferID()))
			throw new IllegalArgumentException("article.offer != this!");

		this.articles.add(article);

		if (JDOHelper.isDetached(this))
			attachable = false;
		else
			this.order.addArticle(article);

		this.articleCount = this.articles.size();
	}

	public void removeArticle(Article article)
	{
		if (isFinalized())
			throw new IllegalStateException("Offer \""+getPrimaryKey()+"\" is already finalized! Cannot delete the Article \""+article.getPrimaryKey()+"\"!");

		if (isAborted())
			throw new IllegalStateException("Offer \""+getPrimaryKey()+"\" is already aborted! Cannot delete the Article!");

		if (!article.isReversing() && (article.isAllocated() || article.isAllocationPending()))
			throw new IllegalStateException("Article \""+article.getPrimaryKey()+"\" is allocated (or allocationPending) and not reversing! Cannot be removed!");

		this.articles.remove(article);
		this.valid = false;

		if (JDOHelper.isDetached(this))
			attachable = false;
		else {
			this.order.removeArticle(article);

			// WORKAROUND JPOX should have deleted the Article automatically, because dependent=true, but it doesn't work :-(
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			if (pm != null) { // only in the server - this method is called in the client, too.
				pm.deletePersistent(article);
			}
			// end workaround
		}
		this.articleCount = articles.size();
	}

	/**
	 * This method recalculates all prices for all {@link Article}s. To be sure there's no
	 * indefinite price definition, it does it twice
	 * (if there's a price config marked as isDependentOnOffer()=true).
	 */
	protected void calculatePrice()
	{
//	 TODO put this logic into the bean?! No, but maybe into Trader! Marco.
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Offer is not persistent! Cannot calculate the price!");

		Trader trader = Trader.getTrader(pm);
		if (trader.getOrganisationID().equals(this.getOrganisationID())) {
			calculateArticlePrices(false);
			long firstNewAmount = price.getAmount();
			if (containsPricesDependentOnOffer) {
				// we recalculate a second time to make sure we have no indefinite price.
				calculateArticlePrices(false);
				long secondNewAmount = price.getAmount();
				this.stable = secondNewAmount == firstNewAmount;
			}
			else
				this.stable = true;
		} // if (trader.getOrganisationID().equals(this.getOrganisationID())) {
		else {
			// The offer is not coming from here locally, but from another organisation (maybe on different server).
			// Thus, we need to delegate the price calculation to the other organisation.

			// TODO delegate to remote org
			throw new UnsupportedOperationException("NYI");
		}
	}

	/**
	 * This method calculates all offerItem's prices and the offer's price. It's a local helper method
	 * called by calculatePrice().
	 *
	 * @param all If all=false, it does not recalculate the price of an Article,
	 *	if Article.isPriceDependentFromOffer() returns false.
	 *	If all=true, all prices of all items are recalculated.
	 */
	private void calculateArticlePrices(boolean all)
	{
		price.clearFragments();
		price.setAmount(0);
		containsPricesDependentOnOffer = false;

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of offer is currently not persistent!");

		for (Article article : articles) {
			if (all || article.isPriceDependentOnOffer()) {
				if (article.isPriceDependentOnOffer())
					containsPricesDependentOnOffer = true;

				ProductType productType = article.getProductType();
				article.setPriceDependentOnOffer(productType.getPackagePriceConfig().isDependentOnOffer());

				ArticlePrice articlePrice = article.getPrice();
				if (articlePrice != null) {
					article.setPrice(null);
					pm.deletePersistent(articlePrice);
					articlePrice = null;
				}

				article.setPrice(productType.getPackagePriceConfig().createArticlePrice(article));
			} // if (all || offerItem.isPriceDependentOnOffer()) {
			price.sumPrice(article.getPrice());
		}
	}

	/**
	 * @return Returns the order.
	 */
	public Order getOrder()
	{
		return order;
	}

	/**
	 * Returns the vendor {@link LegalEntity} of this Offer.
	 * <p>
	 * Note that this method will try to return the vendor
	 * of the container ({@link Order}) if the vendor for this
	 * instance was not detached (or is <code>null</code>).
	 * </p>
	 * @return The vendor {@link LegalEntity} of this Offer.
	 * @throws JDODetachedFieldAccessException if the vendor was not detached.
	 */
	public LegalEntity getVendor()
	{
		if (vendor == null && !vendor_detached)
			vendor = order.getVendor();

		return vendor;
	}

	/**
	 * @return Returns the ID of the vendor, which is either obtained via {@link Order#getVendorID()} or
	 *		manually detached in {@link #jdoPostDetach(Object)}.
	 */
	public AnchorID getVendorID()
	{
		if (vendorID == null && !vendorID_detached)
			vendorID = order.getVendorID();

		return vendorID;
	}

	/**
	 * Returns the customer {@link LegalEntity} of this Offer.
	 * <p>
	 * Note that this method will try to return the customer
	 * of the container ({@link Order}) if the vendor for this
	 * instance was not detached (or is <code>null</code>).
	 * </p>
	 */
	@Override
	public LegalEntity getCustomer()
	{
		if (customer == null && !customer_detached)
			customer = order.getCustomer();

		return customer;
	}

//	@Override
//	public LegalEntity getEndCustomer()
//	{
//		if (endCustomer == null && !endCustomer_detached)
//			endCustomer = order.getEndCustomer();
//
//		return endCustomer;
//	}

	/**
	 * @return Returns the ID of the customer, which is either obtained via {@link Order#getCustomerID()} or
	 *		manually detached in {@link #jdoPostDetach(Object)}.
	 */
	@Override
	public AnchorID getCustomerID()
	{
		if (customerID == null && !customerID_detached)
			customerID = order.getCustomerID();

		return customerID;
	}

//	@Override
//	public AnchorID getEndCustomerID()
//	{
//		if (endCustomerID == null && !endCustomerID_detached)
//			endCustomerID = order.getEndCustomerID();
//
//		return endCustomerID;
//	}

	/**
	 * @return The date and time this {@link Offer} was created.
	 */
	public Date getCreateDT()
	{
		return createDT;
	}

	/**
	 * @return The {@link User} that created this {@link Offer}.
	 */
	public User getCreateUser()
	{
		return createUser;
	}

	/**
	 * An <tt>Offer</tt> is finalized, before it is sent on an asynchronous way (e.g. letter,
	 * email) to the customer. After finalization the {@link Offer} can't be changed any more,
	 * this avoids communication conflicts as it forces the generation
	 * of a new <tt>Offer</tt> if the customer requests changes after reception.
	 *
	 * @return Returns whether it's finalized.
	 */
	public boolean isFinalized()
	{
		return finalizeDT != null;
	}

	/**
	 * This method must not be called directly! It's called via {@link ActionHandlerFinalizeOffer}.
	 * <p>
	 * This method sets the finalized flag of an <tt>Offer</tt> to true. After that,
	 * a change of an <tt>Offer</tt> is not possible anymore.
	 * </p>
	 * <p>
	 * After creation of an <tt>Offer</tt>, the next step in the service-customer relation
	 * is the finalization of the <tt>Offer</tt>. The customer receives a finalized offer
	 * if the communication works offline. If online, the customer can see each change
	 * of his "shopping cart" expressed by the current offer. But offline, it is necessary
	 * to ensure, that the customer finally confirms the correct Offer (and not one that
	 * has already been changed). Hence, the customer receives the Offer (on paper or
	 * by email) not before it has been finalized.
	 * </p>
	 */
	public void setFinalized(User user)
	{
		if (isFinalized())
			return;

		if (!isValid())
			throw new IllegalStateException("This Offer ("+getPrimaryKey()+") cannot be confirmed, because it is not valid! Call 'validate()' first and ensure the Offer is stable!");

		if (isAborted())
			throw new IllegalStateException("This offer is already aborted! Cannot finalize it!");

		finalizeDT = new Date();
		finalizeUser = user;
	}

	/**
	 * @return Returns the currency.
	 */
	public Currency getCurrency()
	{
		return currency;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Set<Article> _articles = null;

	/**
	 * @return an <b>unmodifiable</b> set of the Articles of this {@link Offer}.
	 * Do not attempt to change this set.
	 */
	public Collection<Article> getArticles()
	{
		if (_articles == null)
			_articles = Collections.unmodifiableSet(articles);

		return _articles;
	}

	/**
	 * This method must be called, before the Offer can be confirmed. If the Offer is changed
	 * (e.g. by <tt>addArticle(..)</tt>) the <tt>valid</tt> status is reset and <tt>validate()</tt>
	 * must be called again.
	 * <br/><br/>
	 * This method causes all prices to be recalculated and all products to be allocated (if they have
	 * been released by timeout).
	 * @return Whether this {@link Offer} is valid after the method call.
	 */
	protected boolean validate()
	{
		if (valid)
			return true;

//		valid = false;

		// TODO here, we should make sure, we have requirement-offers, which can
		// be fulfilled.

		// if (all requirement-offers are valid)
		boolean _valid = true;

		calculatePrice();
		if (!isStable())
			_valid = false;

		this.valid = _valid;
		return isValid();
	}

	/**
	 * An Offer is stable, if PriceConfig-s do not interfere and the prices do not
	 * change on calculation, if the Offer is not changed.
	 *
	 * @return Returns the stable.
	 */
	public boolean isStable()
	{
		return stable;
	}

	/**
	 * An Offer is only valid if it is stable and has been validated. It needs to be
	 * valid, before it can be finalized.
	 *
	 * @return Returns the valid.
	 */
	public boolean isValid()
	{
		if (valid && !stable) {
//		 TODO we should log an error here, because this should never happen!
			return false;
		}
		return valid;
	}

	/**
	 * Sets valid
	 */
	protected void setValid(boolean valid)
	{
		this.valid = valid;
	}

	/**
	 * @return The date and time this {@link Offer} was finalized, or <code>null</code> if it was not finalized yet.
	 */
	public Date getFinalizeDT()
	{
		return finalizeDT;
	}

	/**
	 * @return The {@link User} that finilized this {@link Offer}, or <code>null</code> if it was not finalized yet.
	 */
	public User getFinalizeUser()
	{
		return finalizeUser;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean attachable = true;

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Offer is currently not attached to the datastore. Cannot obtain PersistenceManager!");
		return pm;
	}

	public void jdoPreDetach()
	{
	}

	public void jdoPostDetach(Object _attached)
	{
		Offer attached = (Offer)_attached;
		Offer detached = this;
		PersistenceManager pm = attached.getPersistenceManager();
		Collection<?> fetchGroups = pm.getFetchPlan().getGroups();

		// We don't need to check for FetchPlan.ALL, because the fields Offer.order and Order.customer/Order.vendor
		// are detached as well, if FetchPlan.ALL is passed. So we don't need to separately handle this case.
		// The only problem might arise from a limited max-fetch-depth, but this is IMHO so rare that it doesn't justify the
		// additional overhead here and performance loss. We can add it later, if really needed.
		if (fetchGroups.contains(FETCH_GROUP_VENDOR)) {
			detached.vendor = pm.detachCopy(attached.getVendor());
			detached.vendor_detached = true;
		}

		if (fetchGroups.contains(FETCH_GROUP_CUSTOMER)) {
			detached.customer = pm.detachCopy(attached.getCustomer());
			detached.customer_detached = true;
		}

//		if (fetchGroups.contains(FETCH_GROUP_END_CUSTOMER)) {
//			detached.endCustomer = pm.detachCopy(attached.getEndCustomer());
//			detached.endCustomer_detached = true;
//		}

		if (fetchGroups.contains(FETCH_GROUP_VENDOR_ID)) {
			detached.vendorID = attached.getVendorID();
			detached.vendorID_detached = true;
		}

		if (fetchGroups.contains(FETCH_GROUP_CUSTOMER_ID)) {
			detached.customerID = attached.getCustomerID();
			detached.customerID_detached = true;
		}

//		if (fetchGroups.contains(FETCH_GROUP_END_CUSTOMER_ID)) {
//			detached.endCustomerID = attached.getEndCustomerID();
//			detached.endCustomerID_detached = true;
//		}

		detached.attachable = true;
	}

	public void jdoPreAttach()
	{
		if (!attachable)
			throw new IllegalStateException("This offer became non-attachable!");
	}

	public void jdoPostAttach(Object arg0)
	{
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof Offer))
			return false;

		Offer o = (Offer)obj;
		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.offerIDPrefix, o.offerIDPrefix) &&
				this.offerID == o.offerID;
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) ^ Util.hashCode(offerIDPrefix) ^ Util.hashCode(offerID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + offerIDPrefix + ',' + ObjectIDUtil.longObjectIDFieldToString(offerID) + ']';
	}

	/**
	 * This method is <b>not</b> intended to be called directly. It is called by
	 * {@link State#State(String, long, User, Statable, org.nightlabs.jfire.jbpm.graph.def.StateDefinition)}
	 * which is called automatically by {@link ActionHandlerNodeEnter}, if this <code>ActionHandler</code> is registered.
	 */
	public void setState(State currentState)
	{
		if (currentState == null)
			throw new IllegalArgumentException("state must not be null!");

		if (!currentState.getStateDefinition().isPublicState())
			throw new IllegalArgumentException("state.stateDefinition.publicState is false!");

		if (logger.isDebugEnabled())
			logger.debug("setState: offer=" + getPrimaryKey() + " (" + this + ") state=" + currentState.getPrimaryKey() + " (" + currentState.getStateDefinition().getJbpmNodeName() + ")");

		this.state = currentState;
		this.states.add(currentState);
	}

	/**
	 * Returns the {@link State} this order is currently in.
	 * @return The {@link State} this order is currently in.
	 */
	public State getState()
	{
		return state;
	}

	/**
	 * Returns all {@link State}s this {@link Offer} passed (including the current one) and that were registered.
	 * @return All {@link State}s this {@link Offer} already passed (including the current state).
	 */
	public List<State> getStates()
	{
		if (logger.isDebugEnabled()) {
			logger.debug("getStates: offer=" + getPrimaryKey() + " (" + this + ") returning these states:");

			for (State state : states) {
				logger.debug("  * " + state.getPrimaryKey());
			}
		}

		return Collections.unmodifiableList(states);
	}

	/**
	 * Returns the {@link Price} of this {@link Offer}. It represents the sum
	 * of all {@link ArticlePrice}s of the contained articles.
	 *
	 * @return The {@link Price} of this {@link Offer}.
	 */
	public Price getPrice() {
		return price;
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.ArticleContainer#getArticleCount()
	 */
	public int getArticleCount() {
		return articleCount;
	}

//	/**
//	 * This is a temporary method until this problem is solved: http://www.jpox.org/servlet/forum/viewthread?thread=4718
//	 */
//	public void makeAllDirty()
//	{
//		if (!JDOHelper.isDetached(this))
//			throw new IllegalStateException("This method may only be called while this object is detached!");
//
//		try {
//			State s = state;
//			state = null;
//			state = s;
//		} catch (JDODetachedFieldAccessException x) {
//			// ignore
//		}
//
//		try {
//			Price p = price;
//			price = null;
//			price = p;
//		} catch (JDODetachedFieldAccessException x) {
//			// ignore
//		}
//
//		try {
//			ArrayList<State> ss = new ArrayList<State>(this.states);
//			this.states.clear();
//			for (State state : ss) {
//				this.states.add(state);
//				// TODO delegate "makeAllDirty"
//			}
//		} catch (JDODetachedFieldAccessException x) {
//			// ignore
//		}
//
//		try {
//			ArrayList<Article> aa = new ArrayList<Article>(this.articles);
//			this.articles.clear();
//			for (Article article : aa) {
//				this.articles.add(article);
//				article.makeAllDirty();
//			}
//		} catch (JDODetachedFieldAccessException x) {
//			// ignore
//		}
//	}

	@Override
	public Collection<Segment> getSegments() {
		return Collections.unmodifiableCollection(segments);
	}

	public void addSegment(Segment segment)
	{
		if (!this.getOrder().equals(segment.getOrder()))
			throw new IllegalArgumentException("this.order != segment.order :: " + this + " " + segment);

		segments.add(segment);
	}

	/**
	 * Get the timestamp after which this <code>Offer</code> will be released automatically when it is already finalized
	 * but not accepted by the customer. To expire means that all articles are released and the offer's workflow
	 * ends into the state "expired".
	 * <p>
	 * This timestamp should be printed on the offer document to clearly tell the customer the validity time.
	 * </p>
	 *
	 * @return <code>null</code> to indicate that it never expires or the timestamp after which it expires.
	 */
	public Date getExpiryTimestampFinalized() {
		return expiryTimestampFinalized;
	}
	/**
	 * Set the timestamp after which this <code>Offer</code> will be released automatically, if it has already been finalized
	 * but is not accepted by the customer.
	 *
	 * @param expiryTimestampFinalized the timestamp or <code>null</code>.
	 * @see #getExpiryTimestampFinalized()
	 */
	public void setExpiryTimestampFinalized(Date expiryTimestampFinalized) {
		if (isFinalized())
			throw new IllegalStateException("This offer is already finalized! Cannot set setExpiryTimestampFinalized! " + this);
		this.expiryTimestampFinalized = expiryTimestampFinalized;
	}

	/**
	 * Get the timestamp after which this <code>Offer</code> will be released, if it has not yet been finalized.
	 * Expire means that all articles are released. The user can re-allocate them and continue the workflow.
	 *
	 * @return <code>null</code> to indicate that it never expires or the timestamp after which it expires.
	 */
	public Date getExpiryTimestampUnfinalized() {
		return expiryTimestampUnfinalized;
	}

	/**
	 * Set the timestamp after which this <code>Offer</code> will expire, if it has not yet been finalized.
	 * @param expiryTimestampUnfinalized the timestamp or <code>null</code>.
	 * @see #getExpiryTimestampUnfinalized()
	 */
	public void setExpiryTimestampUnfinalized(Date expiryTimestampUnfinalized) {
		this.expiryTimestampUnfinalized = expiryTimestampUnfinalized;
	}

	/**
	 * Is the property {@link #getExpiryTimestampUnfinalized()} managed automatically? If yes,
	 * the timestamp is updated on every change of the <code>Offer</code>.
	 * <p>
	 * To manually set a timestamp, this flag must be set to <code>false</code>.
	 * </p>
	 *
	 * @return whether the appropriate property is managed automatically.
	 */
	public boolean isExpiryTimestampUnfinalizedAutoManaged() {
		return expiryTimestampUnfinalizedAutoManaged;
	}

	public void setExpiryTimestampUnfinalizedAutoManaged(boolean expiryTimestampUnfinalizedAutoManaged) {
		this.expiryTimestampUnfinalizedAutoManaged = expiryTimestampUnfinalizedAutoManaged;
	}

	/**
	 * Is the property {@link #getExpiryTimestampFinalized()} managed automatically? If yes,
	 * the timestamp is updated when the <code>Offer</code> is finalized.
	 * <p>
	 * To manually set a timestamp, this flag must be set to <code>false</code>.
	 * </p>
	 *
	 * @return whether the appropriate property is managed automatically.
	 */
	public boolean isExpiryTimestampFinalizedAutoManaged() {
		return expiryTimestampFinalizedAutoManaged;
	}

	public void setExpiryTimestampFinalizedAutoManaged(boolean expiryTimestampFinalizedAutoManaged) {
		if (isFinalized())
			throw new IllegalStateException("This offer is already finalized! Cannot set expiryTimestampFinalizedAutoManaged! " + this);

		this.expiryTimestampFinalizedAutoManaged = expiryTimestampFinalizedAutoManaged;
	}

	@Override
	public PropertySet getPropertySet() {
		return propertySet;
	}

	protected void modifyCreateDT(Date createDT) {
		if (createDT == null)
			throw new IllegalArgumentException("createDT must not be null!");

		this.createDT = createDT;
	}

	protected void modifyCreateUser(User createUser) {
		if (createUser == null)
			throw new IllegalArgumentException("createUser must not be null!");

		this.createUser = createUser;
	}

	protected void modifyFinalizeDT(Date finalizeDT) {
		if (finalizeDT == null)
			throw new IllegalArgumentException("finalizeDT must not be null!");

		if (this.finalizeDT == null)
			throw new IllegalStateException("This offer is not finalized! You must finalize it before modifying the finalizeDT!");

		this.finalizeDT = finalizeDT;
	}

	protected void modifyFinalizeUser(User finalizeUser) {
		if (finalizeUser == null)
			throw new IllegalArgumentException("finalizeUser must not be null!");

		if (this.finalizeUser == null)
			throw new IllegalStateException("This offer is not finalized! You must finalize it before modifying the finalizeDT!");

		this.finalizeUser = finalizeUser;
	}

	public Date getAbortDT() {
		return abortDT;
	}

	public User getAbortUser() {
		return abortUser;
	}

	public boolean isAborted() {
		return abortDT != null;
	}

	/**
	 * This method must not be called directly! It's called via {@link ActionHandlerAbortOffer}.
	 */
	public void setAborted(User user) {
		if (user == null)
			throw new IllegalArgumentException("user must not be null!");

		if (isAborted())
			return;

		if (isFinalized())
			throw new IllegalStateException("This offer is already finalized! Cannot abort it!");

		this.abortDT = new Date();
		this.abortUser = user;
	}
}
