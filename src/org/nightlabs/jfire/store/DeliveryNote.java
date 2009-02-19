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

package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.deliver.DeliverProductTransfer;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.jbpm.ActionHandlerFinalizeDeliveryNote;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * @author Alexander Bieber <!-- alex at nightlabs dot de -->
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.DeliveryNoteID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryNote"
 *
 * @jdo.version strategy="version-number"
 *
 * @jdo.implements name="org.nightlabs.jfire.trade.ArticleContainer"
 * @jdo.implements name="org.nightlabs.jfire.jbpm.graph.def.Statable"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, deliveryNoteIDPrefix, deliveryNoteID"
 *		add-interfaces="org.nightlabs.jfire.trade.id.ArticleContainerID"
 *		include-body="id/DeliveryNoteID.body.inc"
 *
 * @jdo.query
 *		name="getDeliveryNoteIDsByVendorAndCustomer"
 *		query="SELECT JDOHelper.getObjectId(this)
 *			WHERE vendor.organisationID == paramVendorID_organisationID &&
 *            vendor.anchorID == paramVendorID_anchorID &&
 *			      customer.organisationID == paramCustomerID_organisationID &&
 *            customer.anchorID == paramCustomerID_anchorID
 *			PARAMETERS String paramVendorID_organisationID, String paramVendorID_anchorID,
 *                 String paramCustomerID_organisationID, String paramCustomerID_anchorID
 *			import java.lang.String
 *			ORDER BY deliveryNoteID DESC"
 *
 * @jdo.query
 *		name="getNonFinalizedDeliveryNotesByVendorAndCustomer"
 *		query="SELECT
 *			WHERE vendor.organisationID == paramVendorID_organisationID &&
 *            vendor.anchorID == paramVendorID_anchorID &&
 *			      customer.organisationID == paramCustomerID_organisationID &&
 *            customer.anchorID == paramCustomerID_anchorID &&
 *            finalizeDT == null
 *			PARAMETERS String paramVendorID_organisationID, String paramVendorID_anchorID,
 *                 String paramCustomerID_organisationID, String paramCustomerID_anchorID
 *			import java.lang.String
 *			ORDER BY deliveryNoteID DESC"
 *
 * @jdo.fetch-group name="DeliveryNote.deliveryNoteLocal" fields="deliveryNoteLocal"
 * @jdo.fetch-group name="DeliveryNote.articles" fields="articles"
 * @jdo.fetch-group name="DeliveryNote.createUser" fields="createUser"
 * @jdo.fetch-group name="DeliveryNote.finalizeUser" fields="finalizeUser"
 * @jdo.fetch-group name="DeliveryNote.this" fetch-groups="default" fields="deliveryNoteLocal, articles, createUser, customer, finalizeUser, vendor, state, states"
 *
 * @jdo.fetch-group name="ArticleContainer.vendor" fields="vendor"
 * @jdo.fetch-group name="ArticleContainer.customer" fields="customer"
 * @jdo.fetch-group name="ArticleContainer.endCustomer" fields="endCustomer"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleContainerInEditor" fields="deliveryNoteLocal, articles, createUser, customer, finalizeUser, vendor, state, states"
 *
 * @jdo.fetch-group name="Statable.state" fields="state"
 * @jdo.fetch-group name="Statable.states" fields="states"
 *
 */
public class DeliveryNote
implements Serializable, ArticleContainer, Statable, DetachCallback
{
	private static final long serialVersionUID = 1L;
	public static final String FETCH_GROUP_DELIVERY_NOTE_LOCAL = "DeliveryNote.deliveryNoteLocal";
	public static final String FETCH_GROUP_ARTICLES = "DeliveryNote.articles";
	public static final String FETCH_GROUP_CREATE_USER = "DeliveryNote.createUser";
	public static final String FETCH_GROUP_FINALIZE_USER = "DeliveryNote.finalizeUser";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_DELIVERY_NOTE = "DeliveryNote.this";

	public static final String FETCH_GROUP_VENDOR = "ArticleContainer.vendor";
	public static final String FETCH_GROUP_CUSTOMER = "ArticleContainer.customer";


	// the following fetch-groups are virtual and processed in the detach callback
//	public static final String FETCH_GROUP_VENDOR_ID = "DeliveryNote.vendorID";
//	public static final String FETCH_GROUP_CUSTOMER_ID = "DeliveryNote.customerID";

	/**
	 * This method queries all <code>DeliveryNote</code>s which exist between the given vendor and customer.
	 * They are ordered by orderID descending (means newest first).
	 *
	 * @param pm The PersistenceManager to be used for accessing the datastore.
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive).
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @return Returns instances of {@link DeliveryNote}.
	 */
	@SuppressWarnings("unchecked")
	public static List<DeliveryNoteID> getDeliveryNoteIDs(PersistenceManager pm, AnchorID vendorID, AnchorID customerID, long rangeBeginIdx, long rangeEndIdx)
	{
		Query query = pm.newNamedQuery(DeliveryNote.class, "getDeliveryNoteIDsByVendorAndCustomer");
		Map params = new HashMap();
		params.put("paramVendorID_organisationID", vendorID.organisationID);
		params.put("paramVendorID_anchorID", vendorID.anchorID);
		params.put("paramCustomerID_organisationID", customerID.organisationID);
		params.put("paramCustomerID_anchorID", customerID.anchorID);

		if (rangeBeginIdx >= 0 && rangeEndIdx >= 0)
			query.setRange(rangeBeginIdx, rangeEndIdx);

		return (List<DeliveryNoteID>) query.executeWithMap(params);
	}

	public static List getNonFinalizedDeliveryNotes(PersistenceManager pm, AnchorID vendorID, AnchorID customerID)
	{
		Query query = pm.newNamedQuery(DeliveryNote.class, "getNonFinalizedDeliveryNotesByVendorAndCustomer");
		Map params = new HashMap();
		params.put("paramVendorID_organisationID", vendorID.organisationID);
		params.put("paramVendorID_anchorID", vendorID.anchorID);
		params.put("paramCustomerID_organisationID", customerID.organisationID);
		params.put("paramCustomerID_anchorID", customerID.anchorID);

		return (List) query.executeWithMap(params);
	}

	/**
	 * @deprecated Constructor exists only for JDO!
	 */
	@Deprecated
	protected DeliveryNote() { }

	public DeliveryNote(User creator, LegalEntity vendor,
			LegalEntity customer, String deliveryNoteIDPrefix, long _deliveryNoteID)
	{
		if (creator == null)
			throw new NullPointerException("creator");

		if (vendor == null)
			throw new NullPointerException("vendor");

		if (customer == null)
			throw new NullPointerException("customer");

		ObjectIDUtil.assertValidIDString(deliveryNoteIDPrefix, "deliveryNoteIDPrefix");

		if (_deliveryNoteID < 0)
			throw new IllegalArgumentException("_deliveryNoteID < 0");

//		if (currency == null)
//			throw new NullPointerException("currency");

		PersistenceManager pm = JDOHelper.getPersistenceManager(vendor);
		if (pm == null)
			throw new IllegalStateException("vendor is not persistent! Could not get a PersistenceManager from it!");

//		Accounting accounting = Accounting.getAccounting(pm);
//		AccountingPriceConfig accountingPriceConfig = accounting.getAccountingPriceConfig();

		this.organisationID = vendor.getOrganisationID();
		this.deliveryNoteIDPrefix = deliveryNoteIDPrefix;
		this.deliveryNoteID = _deliveryNoteID;
		this.createDT = new Date(System.currentTimeMillis());
		this.createUser = creator;
		this.vendor = vendor;
		this.customer = customer;
//		this.currency = currency;
		this.primaryKey = getPrimaryKey(this.organisationID, this.deliveryNoteIDPrefix, this.deliveryNoteID);

		articles = new HashSet<Article>();
		states = new ArrayList<State>();
		receptionNotes = new HashSet<ReceptionNote>();
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String deliveryNoteIDPrefix;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long deliveryNoteID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="deliveryNote" dependent="true"
	 */
	private DeliveryNoteLocal deliveryNoteLocal;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LegalEntity vendor;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LegalEntity customer;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LegalEntity endCustomer;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private AnchorID vendorID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean vendorID_detached = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private AnchorID customerID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean customerID_detached = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private AnchorID endCustomerID = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean endCustomerID_detached = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private State state;

	/**
	 * This is the history of <b>public</b> {@link State}s with the newest last and the oldest first.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="State"
	 *		table="JFireTrade_DeliveryNote_states"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private List<State> states;

//	/**
//	 * key: String articlePK (organisationID/articleID)<br/>
//	 * value: Article article
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="org.nightlabs.jfire.trade.Article"
//	 *		mapped-by="deliveryNote"
//	 *
//	 * @jdo.key mapped-by="primaryKey"
//	 *
//	 * @!jdo.join
//	 *
//	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="primaryKey"
//	 */
//	private Map articles = new HashMap();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int articleCount = 0;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.trade.Article"
	 *		mapped-by="deliveryNote"
	 */
	private Set<Article> articles;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="ReceptionNote"
	 *		mapped-by="deliveryNote"
	 */
	private Set<ReceptionNote> receptionNotes;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date createDT;

	/**
	 * The user who created this DeliveryNote.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User createUser = null;

	public String getOrganisationID() {
		return organisationID;
	}
	public String getDeliveryNoteIDPrefix()
	{
		return deliveryNoteIDPrefix;
	}
	public String getArticleContainerIDPrefix()
	{
		return getDeliveryNoteIDPrefix();
	}
	public long getDeliveryNoteID() {
		return deliveryNoteID;
	}
	public long getArticleContainerID()
	{
		return getDeliveryNoteID();
	}
	public String getDeliveryNoteIDAsString() {
		return ObjectIDUtil.longObjectIDFieldToString(deliveryNoteID);
	}
	public String getArticleContainerIDAsString()
	{
		return getDeliveryNoteIDAsString();
	}

	public static String getPrimaryKey(String organisationID, String deliveryNoteIDPrefix, long deliveryNoteID)
	{
		return organisationID + '/' + deliveryNoteIDPrefix + '/' + ObjectIDUtil.longObjectIDFieldToString(deliveryNoteID);
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}

	/**
	 * @return the same as {@link #getStatableLocal()}
	 */
	public DeliveryNoteLocal getDeliveryNoteLocal()
	{
		return deliveryNoteLocal;
	}

	@Override
	public StatableLocal getStatableLocal()
	{
		return deliveryNoteLocal;
	}

	protected void setDeliveryNoteLocal(DeliveryNoteLocal deliveryNoteLocal)
	{
		this.deliveryNoteLocal = deliveryNoteLocal;
	}

	@Override
	public Date getCreateDT() {
		return createDT;
	}

	@Override
	public User getCreateUser()
	{
		return createUser;
	}

	@Override
	public LegalEntity getCustomer() {
		return customer;
	}

	@Override
	public LegalEntity getEndCustomer() {
		return endCustomer;
	}

	@Override
	public LegalEntity getVendor() {
		return vendor;
	}

	public AnchorID getVendorID()
	{
		if (vendorID == null && !vendorID_detached)
			vendorID = (AnchorID) JDOHelper.getObjectId(vendor);

		return vendorID;
	}

	public AnchorID getCustomerID()
	{
		if (customerID == null && !customerID_detached)
			customerID = (AnchorID) JDOHelper.getObjectId(customer);

		return customerID;
	}

	@Override
	public AnchorID getEndCustomerID()
	{
		if (endCustomerID == null && !endCustomerID_detached)
			endCustomerID = (AnchorID) JDOHelper.getObjectId(endCustomer);

		return endCustomerID;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<Article> _articles = null;

	@Override
	public Collection<Article> getArticles()
	{
		if (_articles == null)
			_articles = Collections.unmodifiableSet(articles);

		return _articles;
	}

	@Override
	public int getArticleCount()
	{
		return articleCount;
	}

	/**
	 * A DeliveryNote is only valid after {@link #validate()} has been called.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean valid = false;

	/**
	 * This member stores the user who finilized this DeliveryNote.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User finalizeUser = null;

	/**
	 * This member stores when this <tt>DeliveryNote</tt> was finalized.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date finalizeDT  = null;

	/**
	 * Adds an Article, if this DeliveryNote is not yet finalized,
	 * the offerItems offer has the same vendor and customer as this DeliveryNote,
	 * and the offerItem is not yet part of another invoice.
	 * <p>
	 * NEVER use this method directly (within the server)! Call {@link Store#addArticlesToDeliveryNote(DeliveryNote, Collection)}
	 * instead!
	 * </p>
	 *
	 * @param article
	 */
	public void addArticle(Article article)
	throws DeliveryNoteEditException
	{
		Offer articleOffer = article.getOffer();
		Order articleOrder = articleOffer.getOrder();
		ArticleID articleID = (ArticleID) JDOHelper.getObjectId(article);
		if (isFinalized())
			throw new DeliveryNoteEditException(
					DeliveryNoteEditException.REASON_DELIVERY_NOTE_FINALIZED,
					"DeliveryNote is finalized, can not change any more!",
					articleID
				);

		if (!vendor.getPrimaryKey().equals(articleOrder.getVendor().getPrimaryKey())
					||
				!customer.getPrimaryKey().equals(articleOrder.getCustomer().getPrimaryKey())
				)
		{
			throw new DeliveryNoteEditException(
				DeliveryNoteEditException.REASON_ANCHORS_DONT_MATCH,
				"Vendor and customer are not equal for the Article to add and the delivery note, can not add the article!"
			);
		}

		if (!article.isAllocated())
			throw new IllegalArgumentException("Article is not allocated! " + article);

		DeliveryNoteID deliveryNoteID = article.getDeliveryNoteID();
		if (deliveryNoteID != null) {
			throw new DeliveryNoteEditException(
				DeliveryNoteEditException.REASON_ARTICLE_ALREADY_IN_DELIVERY_NOTE,
				"Article already in a delivery note. Article "+articleID+", DeliveryNote "+deliveryNoteID,
				articleID,
				deliveryNoteID
			);
		}

		if (!article.getOffer().getOfferLocal().isAccepted()) {
			throw new DeliveryNoteEditException(
				DeliveryNoteEditException.REASON_OFFER_NOT_ACCEPTED,
				"At least one involved offer is not confirmed!",
				articleID
			);
		}

		if (article.isReversed())
			throw new DeliveryNoteEditException(
					DeliveryNoteEditException.REASON_REVERSED_ARTICLE,
					"A reversed Article cannot be added to a DeliveryNote!",
					articleID);

		if (article.isReversing() && article.getReversedArticle().getDeliveryNoteID() == null)
			throw new DeliveryNoteEditException(
					DeliveryNoteEditException.REASON_REVERSING_ARTICLE,
					"The reversed Article is not in a DeliveryNote! Cannot add the reversing Article to one in this case!",
					articleID);

//		if (!getCurrency().getCurrencyID().equals(article.getPrice().getCurrency().getCurrencyID()))
//			throw new DeliveryNoteEditException(
//				DeliveryNoteEditException.REASON_MULTIPLE_CURRENCIES,
//				"Cannot add an Article with a different currency ("+article.getPrice().getCurrency().getCurrencyID()+") than this DeliveryNote's one ("+getCurrency().getCurrencyID()+")!"
//			);

		articles.add(article);
		articleCount = articles.size();

		this.valid = false;
		article.setDeliveryNote(this);
	}

	public void removeArticle(Article article)
	throws DeliveryNoteEditException
	{
		if (isFinalized())
			throw new DeliveryNoteEditException(DeliveryNoteEditException.REASON_DELIVERY_NOTE_FINALIZED, "DeliveryNote is finalized, can not change any more!");

		if (articles.contains(article)) {
			articles.remove(article);
			this.valid = false;
			article.setDeliveryNote(null);
			articleCount = articles.size();
		}
	}

	protected void validate()
	{
		this.valid = true;
	}

	/**
	 * @return Returns the valid.
	 */
	public boolean isValid()
	{
		return valid;
	}

	/**
	 * This method MUST NOT be called directly. It is called by {@link ActionHandlerFinalizeDeliveryNote}.
	 */
	public void setFinalized(User finalizer) {
		if (isFinalized())
			return;

		if (!isValid())
			throw new IllegalStateException("This DeliveryNote is not valid! Call validate() before!");
		this.finalizeUser = finalizer;
		this.finalizeDT = new Date(System.currentTimeMillis());
	}
	/**
	 * This member is set to true as soon as all desired
	 * {@link Article}s were added to this delivery note. A finalized
	 * DeliveryNote can not be altered any more.
	 */
	public boolean isFinalized() {
		return finalizeDT != null;
	}
	public User getFinalizeUser() {
		return finalizeUser;
	}
	public Date getFinalizeDT() {
		return finalizeDT;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Order is currently not attached to the datastore. Cannot obtain PersistenceManager!");
		return pm;
	}

	@Override
	public void jdoPreDetach()
	{
	}

	@Override
	public void jdoPostDetach(Object _attached)
	{
		DeliveryNote attached = (DeliveryNote)_attached;
		DeliveryNote detached = this;
		Collection<String> fetchGroups = CollectionUtil.castCollection(attached.getPersistenceManager().getFetchPlan().getGroups());

		if (fetchGroups.contains(FETCH_GROUP_VENDOR_ID)) {
			detached.vendorID = attached.getVendorID();
			detached.vendorID_detached = true;
		}

		if (fetchGroups.contains(FETCH_GROUP_CUSTOMER_ID)) {
			detached.customerID = attached.getCustomerID();
			detached.customerID_detached = true;
		}

		if (fetchGroups.contains(FETCH_GROUP_END_CUSTOMER_ID)) {
			detached.endCustomerID = attached.getEndCustomerID();
			detached.endCustomerID_detached = true;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof DeliveryNote))
			return false;

		DeliveryNote o = (DeliveryNote) obj;

		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.deliveryNoteIDPrefix, o.deliveryNoteIDPrefix) &&
				this.deliveryNoteID == o.deliveryNoteID;
	}

	@Override
	public int hashCode()
	{
		return
				Util.hashCode(this.organisationID) ^
				Util.hashCode(this.deliveryNoteIDPrefix) ^
				Util.hashCode(this.deliveryNoteID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + deliveryNoteIDPrefix + ',' + ObjectIDUtil.longObjectIDFieldToString(deliveryNoteID) + ']';
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

		this.state = currentState;
		this.states.add(currentState);
	}

	public State getState()
	{
		return state;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient List<State> _states = null;

	public List<State> getStates()
	{
		if (_states == null)
			_states = CollectionUtil.castList(Collections.unmodifiableList(states));

		return _states;
	}

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private int nextReceptionNoteID = 0;
//
//	public int createReceptionNoteID()
//	{
//		return nextReceptionNoteID++;
//	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<ReceptionNote> _receptionNotes = null;

	public Set<ReceptionNote> getReceptionNotes()
	{
		if (_receptionNotes == null)
			_receptionNotes = Collections.unmodifiableSet(receptionNotes);

		return _receptionNotes;
	}

	public void bookDeliveryNoteProductTransfer(DeliveryNoteProductTransfer transfer, Set<Anchor> involvedAnchors, boolean rollback)
	{
//		if (!DeliveryNoteProductTransfer.BOOK_TYPE_DELIVER.equals(transfer.getBookType()))
//			return;
	}
	public void bookDeliverProductTransfer(DeliverProductTransfer transfer, Set<Anchor> involvedAnchors, boolean rollback)
	{
		boolean vendorIsFrom = transfer.getAnchorType(vendor) == Transfer.ANCHORTYPE_FROM;
		boolean vendorIsTo = transfer.getAnchorType(vendor) == Transfer.ANCHORTYPE_TO;
		boolean customerIsFrom = transfer.getAnchorType(customer) == Transfer.ANCHORTYPE_FROM;
		boolean customerIsTo = transfer.getAnchorType(customer) == Transfer.ANCHORTYPE_TO;

		if (!vendorIsFrom && !vendorIsTo && !customerIsFrom && !customerIsTo)
			throw new IllegalArgumentException("Transfer \""+transfer.getPrimaryKey()+"\" && DeliveryNote \""+this.getPrimaryKey()+"\": Transfer and delivery note are not related!");

		int articleCount = 0;

//		Delivery delivery = ((DeliverProductTransfer)transfer.getContainer()).getDelivery();
		Delivery delivery = transfer.getDelivery();
		for (Article article : delivery.getArticles()) {
			if (this.equals(article.getDeliveryNote())) {
				++articleCount;
				article.getArticleLocal().setDelivery(rollback ? null : delivery);
			}
		}

		if (rollback)
			deliveryNoteLocal.decDeliveredArticleCount(articleCount);
		else
			deliveryNoteLocal.incDeliveredArticleCount(articleCount);
	}
}
