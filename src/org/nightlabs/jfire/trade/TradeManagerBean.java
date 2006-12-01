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

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.MalformedVersionException;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.config.LegalEntityViewConfigModule;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.id.SegmentTypeID;
import org.nightlabs.jfire.trade.state.ProcessDefinitionAssignment;
import org.nightlabs.jfire.trade.state.ProcessDefinitionUtil;
import org.nightlabs.jfire.transfer.id.AnchorID;


/**
 * @ejb.bean name="jfire/ejb/JFireTrade/TradeManager"	
 *					 jndi-name="jfire/ejb/JFireTrade/TradeManager"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 */
public abstract class TradeManagerBean 
extends BaseSessionBeanImpl
implements SessionBean 
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(TradeManagerBean.class);

	////////////////////// EJB "constuctor" ////////////////////////////

	/**
	 * @ejb.create-method	
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() 
	throws CreateException
	{
		logger.debug(this.getClass().getName() + ".ejbCreate()");
	}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbRemove()");
	}

	// //// begin EJB stuff ////
	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext ctx)
		throws EJBException, RemoteException
	{
		super.setSessionContext(ctx);
	}
	// //// end EJB stuff ////

	/**
	 * Creates a new order. This method is intended to be called by a user (not another
	 * organisation).
	 *
	 * @param customerID An <tt>Order</tt> is defined between a vendor (this <tt>Organisation</tt>) and a customer. This ID defines the customer.
	 * @param currencyID What <tt>Currency</tt> to use for the new <tt>Order</tt>.
	 * @param segmentTypeIDs May be <tt>null</tt>. If it is not <tt>null</tt>, a {@link Segment} will be created for each defined {@link SegmentType}. For each <tt>null</tt> entry within the array, a <tt>Segment</tt> with the {@link SegmentType#DEFAULT_SEGMENT_TYPE_ID} will be created.
	 * @param fetchGroups What fields should be detached.
	 *
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="TradeManager-write"
	 * @ejb.transaction type = "Required"
	 **/
	public Order createOrder(
			AnchorID customerID, String orderIDPrefix, String currencyID,
			SegmentTypeID[] segmentTypeIDs, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Trader trader = Trader.getTrader(pm);

			pm.getExtent(Currency.class);
			Currency currency = (Currency)pm.getObjectById(CurrencyID.create(currencyID));

			pm.getExtent(LegalEntity.class);
			LegalEntity customer = (LegalEntity) pm.getObjectById(customerID);

			Order order = trader.createOrder(trader.getMandator(), customer, orderIDPrefix, currency);

			if (segmentTypeIDs != null) {
				pm.getExtent(SegmentType.class);
				for (int i = 0; i < segmentTypeIDs.length; ++i) {
					SegmentTypeID segmentTypeID = segmentTypeIDs[i];
					SegmentType segmentType = null;
					if (segmentTypeID != null) {
						segmentType = (SegmentType) pm.getObjectById(segmentTypeID);
					}
					trader.createSegment(order, segmentType);
				}
			} // if (segmentTypeIDs != null) {

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (Order)pm.detachCopy(order);
		} finally {
			pm.close();
		}
	}

	/**
	 * Creates a new order. This method is only usable, if the user (principal)
	 * is an organisation, because this organisation will automatically be set
	 * as the customer for the new Order. 
	 *
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="TradeManager-write"
	 * @ejb.transaction type = "Required"
	 **/
	public Order createOrder(String orderIDPrefix, String currencyID, String[] fetchGroups, int maxFetchDepth)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Trader trader = Trader.getTrader(pm);
			pm.getExtent(Currency.class);
			Currency currency = (Currency)pm.getObjectById(CurrencyID.create(currencyID), true);

			if (!getPrincipal().userIsOrganisation())
				throw new IllegalStateException("This method cannot be called by a user who is not an organisation!");

			OrganisationLegalEntity customer = trader.getOrganisationLegalEntity(getPrincipal().getUserID());
			// TODO: create foreign order ...really? Isn't that todo garbage?
			Order order = trader.createOrder(trader.getMandator(), customer, orderIDPrefix, currency);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (Order)pm.detachCopy(order);
		} finally {
			pm.close();
		}
	}

	/**
	 * Creates a new Offer within a given Order. 
	 *
	 * @param orderID The orderID defining the Order in which to create a new Offer.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="TradeManager-write"
	 * @ejb.transaction type = "Required"
	 **/
	public Offer createOffer(OrderID orderID, String offerIDPrefix, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Trader trader = Trader.getTrader(pm);
			pm.getExtent(Order.class);
			Offer offer = trader.createOffer(
					User.getUser(pm, getPrincipal()), (Order) pm.getObjectById(orderID, true), offerIDPrefix);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (Offer) pm.detachCopy(offer);
		} finally {
			pm.close();
		}
	}

	/**
	 * Finalizes the specified {@link Offer}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="TradeManager-write"
	 * @ejb.transaction type = "Required"
	 **/
	public Offer finalizeOffer(OfferID offerID, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Offer.class);

			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
			}

			Offer offer = (Offer) pm.getObjectById(offerID);
			Trader.getTrader(pm).finalizeOffer(User.getUser(pm, getPrincipal()), offer);

			if (get)
				return (Offer) pm.detachCopy(offer);
			else
				return null;
		} finally {
			pm.close();
		}
	}

	/**
	 * Accepts the specified {@link Offer}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="TradeManager-write"
	 * @ejb.transaction type = "Required"
	 **/
	public Offer acceptOffer(OfferID offerID, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Offer.class);

			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
			}

			Offer offer = (Offer) pm.getObjectById(offerID);
			Trader.getTrader(pm).acceptOffer(User.getUser(pm, getPrincipal()), offer);

			if (get)
				return (Offer) pm.detachCopy(offer);
			else
				return null;
		} finally {
			pm.close();
		}
	}

	/**
	 * Rejects the specified {@link Offer}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="TradeManager-write"
	 * @ejb.transaction type = "Required"
	 **/
	public Offer rejectOffer(OfferID offerID, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Offer.class);

			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
			}

			Offer offer = (Offer) pm.getObjectById(offerID);
			Trader.getTrader(pm).rejectOffer(User.getUser(pm, getPrincipal()), offer);

			if (get)
				return (Offer) pm.detachCopy(offer);
			else
				return null;
		} finally {
			pm.close();
		}
	}

	/**
	 * Confirms the specified {@link Offer}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="TradeManager-write"
	 * @ejb.transaction type = "Required"
	 **/
	public Offer confirmOffer(OfferID offerID, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Offer.class);

			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
			}

			Offer offer = (Offer) pm.getObjectById(offerID);
			Trader.getTrader(pm).confirmOffer(User.getUser(pm, getPrincipal()), offer);

			if (get)
				return (Offer) pm.detachCopy(offer);
			else
				return null;
		} finally {
			pm.close();
		}
	}

	/**
	 * @param orderID The orderID defining the Order for which to find all non-finalized offers.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="TradeManager-read"
	 * @ejb.transaction type = "Required"
	 **/
	public List getNonFinalizedOffers(OrderID orderID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Order.class);
			Order order = (Order) pm.getObjectById(orderID);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (List) pm.detachCopyAll(Offer.getNonFinalizedOffers(pm, order));
		} finally {
			pm.close();
		}
	}

	/**
	 * In order to reverse an <code>Article</code>, you need to create a new "negative" or "inversed"
	 * <code>Article</code>. This is done by this method: It creates new <code>Article</code>s within
	 * the specified {@link Offer} reversing all the specified <code>Article</code>s.
	 *
	 * @param offerID The offerID which defines the {@link Offer} in which to create the new <code>Article</code>s.
	 *		Note, that there are no special requirements for this <code>Offer</code> (it can either be created by
	 *		{@link TradeManager#createOffer(org.nightlabs.jfire.trade.id.OrderID, java.lang.String[])} or by
	 *		{@link TradeManager#createReverseOffer(java.util.Collection, boolean, java.lang.String[])} (or other create-methods)).
	 * @param reversedArticleIDs The IDs of the original <code>Article</code>s that shall be reversed.
	 * @param get Whether or not to return a detached <code>Article</code>.
	 * @return Returns the newly created reversing {@link Article}s or <code>null</code>, depending on <code>get</code>.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="TradeManager-write"
	 * @ejb.transaction type = "Required"
	 **/
	public Collection reverseArticles(OfferID offerID, Collection reversedArticleIDs, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Offer.class);
			Offer offer = (Offer) pm.getObjectById(offerID);

			pm.getExtent(Article.class);
			Order order = null;
			List reversedArticles = new ArrayList(reversedArticleIDs.size());
			for (Iterator it = reversedArticleIDs.iterator(); it.hasNext(); ) {
				ArticleID articleID = (ArticleID) it.next();
				Article article = (Article) pm.getObjectById(articleID);
				if (order == null)
					order = article.getOrder();
				else if (!order.getPrimaryKey().equals(article.getOrder().getPrimaryKey()))
					throw new IllegalArgumentException("Not all Articles are in the same Order!");

				reversedArticles.add(article);
			}
			if (order == null)
				throw new IllegalArgumentException("Collection reversedArticleIDs must not be empty!");

			User user = User.getUser(pm, getPrincipal());

//			List reversingArticles = get ? new ArrayList(reversedArticles.size()) : null;
//			for (Iterator it = reversedArticles.iterator(); it.hasNext(); ) {
//				Article reversedArticle = (Article) it.next();
//				Article reversingArticle = Trader.getTrader(pm).reverseArticle(user, offer, reversedArticle);
//				if (reversingArticles != null)
//					reversingArticles.add(reversingArticle);
//			}
			Collection reversingArticles = Trader.getTrader(pm).reverseArticles(user, offer, reversedArticles);

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopyAll(reversingArticles);
		} finally {
			pm.close();
		}
	}

	/**
	 * In order to reverse an <code>Article</code>, you need to create a new "negative" or "inversed"
	 * <code>Article</code>. This is done by this method: It creates new <code>Article</code>s within
	 * a newly created {@link Offer} reversing all the specified <code>Article</code>s.
	 * <p>
	 * This method is a shortcut for
	 * {@link TradeManager#createOffer(org.nightlabs.jfire.trade.id.OrderID, java.lang.String[])} combined
	 * with
	 * {@link TradeManager#reverseArticle(org.nightlabs.jfire.trade.id.OfferID, org.nightlabs.jfire.trade.id.ArticleID, boolean, java.lang.String[])}
	 * or
	 * {@link TradeManager#reverseArticles(org.nightlabs.jfire.trade.id.OfferID, java.util.Collection, boolean, java.lang.String[])}.
	 * </p>
	 *
	 * @param offerID The offerID which defines the {@link Offer} in which to create the new <code>Article</code>s.
	 * @param reversedArticleIDs The IDs of the original <code>Article</code>s that shall be reversed.
	 * @param get Whether or not to return a detached <code>Article</code>.
	 * @return Returns the newly created reversing {@link Article}s or <code>null</code>, depending on <code>get</code>.
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="TradeManager-write"
	 * @ejb.transaction type = "Required"
	 **/
	public Offer createReverseOffer(
			Collection reversedArticleIDs, String offerIDPrefix,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Order order = null;
			Set<Article> reversedArticles = NLJDOHelper.getObjectSet(pm, reversedArticleIDs, Article.class);
//			List reversedArticles = new ArrayList(reversedArticleIDs.size());
//			for (Iterator it = reversedArticleIDs.iterator(); it.hasNext(); ) {
//				ArticleID articleID = (ArticleID) it.next();
//				Article article = (Article) pm.getObjectById(articleID);
//				if (order == null)
//					order = article.getOrder();
//				else if (!order.getPrimaryKey().equals(article.getOrder().getPrimaryKey()))
//					throw new IllegalArgumentException("Not all Articles are in the same Order!");
//
//				reversedArticles.add(article);
//			}
//			if (order == null)
//				throw new IllegalArgumentException("Collection reversedArticleIDs must not be empty!");

			User user = User.getUser(pm, getPrincipal());

			Trader trader = Trader.getTrader(pm);
			Offer offer = trader.createReverseOffer(user, reversedArticles, offerIDPrefix);

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (Offer)pm.detachCopy(offer);
		} finally {
			pm.close();
		}
	}

//	/**
//	 * In order to reverse an <code>Article</code>, you need to create a new "negative" or "inversed"
//	 * <code>Article</code>. This is done by this method: It creates a new <code>Article</code> within
//	 * the specified {@link Offer} reversing the specified <code>Article</code>.
//	 *
//	 * @param offerID The offerID which defines the {@link Offer} in which to create the new <code>Article</code>.
//	 * @param reversedArticleID The ID of the original <code>Article</code> that shall be reversed.
//	 * @param get Whether or not to return a detached <code>Article</code>.
//	 * @return Returns the newly created reversing Article or <code>null</code>, depending on <code>get</code>.
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="TradeManager-write"
//	 * @ejb.transaction type = "Required"
//	 **/
//	public Article reverseArticle(
//			OfferID offerID, ArticleID reversedArticleID, boolean get, String[] fetchGroups, int maxFetchDepth)
//	throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getExtent(Offer.class);
//			pm.getExtent(Article.class);
//			User user = User.getUser(pm, getPrincipal());
//			Offer offer = (Offer) pm.getObjectById(offerID);
//			Article reversedArticle = (Article) pm.getObjectById(reversedArticleID);
//
//			Article article = Trader.getTrader(pm).reverseArticle(user, offer, reversedArticle);
//
//			if (!get)
//				return null;
//
//			if (fetchGroups != null)
//				pm.getFetchPlan().setGroups(fetchGroups);
//
//			return (Article)pm.detachCopy(article);
//		} finally {
//			pm.close();
//		}
//	}


	/**
	 * This method delegates to
	 * {@link LegalEntity#getAnonymousCustomer(PersistenceManager)}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public LegalEntity getAnonymousCustomer(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			LegalEntity le = LegalEntity.getAnonymousCustomer(pm);
			return (LegalEntity) pm.detachCopy(le);
		} finally {
			pm.close();
		}
	}

	/**
	 * This method delegates to
	 * {@link OrganisationLegalEntity#getOrganisationLegalEntity(PersistenceManager, String, boolean)}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public OrganisationLegalEntity getOrganisationLegalEntity(
			String organisationID, boolean throwExceptionIfNotExistent, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			OrganisationLegalEntity ole = OrganisationLegalEntity.getOrganisationLegalEntity(
					pm, organisationID, OrganisationLegalEntity.ANCHOR_TYPE_ID_ORGANISATION, throwExceptionIfNotExistent);

			return (OrganisationLegalEntity) pm.detachCopy(ole);
		} finally {
			pm.close();
		}
	}

	/**
	 * This method queries all <code>Order</code>s which exist between the given vendor and customer.
	 * They are ordered by orderID descending (means newest first).
	 *
	 * @param vendorID The ID specifying the vendor (which must be a {@link LegalEntity}).
	 * @param customerID The ID specifying the customer (which must be a {@link LegalEntity}).
	 * @param rangeBeginIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall begin (inclusive).
	 * @param rangeEndIdx Either -1, if no range shall be specified, or a positive number (incl. 0) defining the index where the range shall end (exclusive).
	 * @return Returns instances of {@link Order}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public List<OrderID> getOrderIDs(AnchorID vendorID, AnchorID customerID, long rangeBeginIdx, long rangeEndIdx)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return new ArrayList<OrderID>(Order.getOrderIDs(pm, vendorID, customerID, rangeBeginIdx, rangeEndIdx));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public List<Order> getOrders(Set<OrderID> orderIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, orderIDs, Order.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Stores the given Person to a LegalEntity. If no LegalEntity with the right
	 * AnchorID is found a new one will be created and made persistent.
	 * 
	 * @param person The person to be set to the LegalEntity
	 * @param get If true the created LegalEntity will be returned else null
	 * @param fetchGroups The fetchGroups the returned LegalEntity should be detached with
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */ 
	public LegalEntity storePersonAsLegalEntity(Person person, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			Trader trader = Trader.getTrader(pm);
			Person aPerson = (Person)pm.makePersistent(person);
			LegalEntity legalEntity = trader.setPersonToLegalEntity(aPerson, true);
			if (get)
				return (LegalEntity)pm.detachCopy(legalEntity);
			else
				return null;
		} finally {
			pm.close();
		}
	}

	/**
	 * Stores the given LegalEntity.
	 * 
	 * @param legalEntity The LegalEntity to be stored
	 * @param get Weather the stored instance or null should be returned.
	 * @param fetchGroups The fetchGroups the returned LegalEntity should be detached with
	 * @return The stored LegalEntity or null
	 * @throws ModuleException
	 */
	public LegalEntity storeLegalEntity(LegalEntity legalEntity, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return (LegalEntity)NLJDOHelper.storeJDO(pm, legalEntity, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */ 
	public Order getOrder(OrderID orderID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (Order) pm.detachCopy(pm.getObjectById(orderID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */ 
	public LegalEntity getLegalEntity(AnchorID anchorID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (LegalEntity) pm.detachCopy(pm.getObjectById(anchorID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */ 
	public Collection getLegalEntities(Object[] leAnchorIDs, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection les = new LinkedList();
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			for (int i = 0; i < leAnchorIDs.length; i++) {
				if (!(leAnchorIDs[i] instanceof AnchorID))
					throw new IllegalArgumentException("leAnchorIDs["+i+" is not of type AnchorID");
				les.add(pm.getObjectById(leAnchorIDs[i]));
			}
			
			long time = System.currentTimeMillis();
			Collection result = pm.detachCopyAll(les);
			time = System.currentTimeMillis() - time;
			logger.debug("Detach of "+result.size()+" LegalEntities took "+((double)time / (double)1000));
			return result;
		}
		finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */ 
	public Offer getOffer(OfferID offerID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(Offer.class);
			return (Offer) pm.detachCopy(pm.getObjectById(offerID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */ 
	@SuppressWarnings("unchecked")
	public List<Offer> getOffers(Set<OfferID> offerIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, offerIDs, Offer.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */ 
	public Article getArticle(ArticleID articleID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(Article.class);
			return (Article) pm.detachCopy(pm.getObjectById(articleID));
		} finally {
			pm.close();
		}
	}

	/**
	 * @param articleIDs Instances of {@link ArticleID}.
	 * @return Returns instances of {@link Article}
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */ 
	public Collection getArticles(Collection articleIDs, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			if (!(articleIDs instanceof Set)) // make sure the entries are unique (prevent duplicate lookups)
				articleIDs = new HashSet(articleIDs);

			pm.getExtent(Article.class);

			Collection res = new ArrayList(articleIDs.size());
			for (Iterator it = articleIDs.iterator(); it.hasNext(); ) {
				ArticleID articleID = (ArticleID) it.next();
				res.add(pm.getObjectById(articleID));
			}

			return (Collection) pm.detachCopyAll(res);
		} finally {
			pm.close();
		}
	}

	/**
	 * Creates a new <tt>Segment</tt> within the given <tt>Order</tt>
	 * for the given <tt>SegmentType</tt>. Note, that you can create
	 * many <tt>Segment</tt>s with the same <tt>SegmentType</tt>.
	 *
	 * @param orderID The ID of the {@link Order} in which to create the new <tt>Segment</tt>.
	 * @param segmentTypeID The ID of the {@link SegmentType} of which a <tt>Segment</tt>
	 * "instance" will be created. This may be <tt>null</tt>. If undefined, the default
	 * segment type will be used.
	 * @param fetchGroups A <tt>String</tt> array defining what fields to detach.
	 *
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 **/
	public Segment createSegment(
			OrderID orderID, SegmentTypeID segmentTypeID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Order.class);
			Order order = (Order) pm.getObjectById(orderID);

			SegmentType segmentType = null;
			if (segmentTypeID != null) {
				pm.getExtent(SegmentType.class);
				segmentType = (SegmentType) pm.getObjectById(segmentTypeID);
			}

			Trader trader = Trader.getTrader(pm);
			Segment segment = trader.createSegment(order, segmentType);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (Segment) pm.detachCopy(segment);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void deleteArticles(Collection articleIDs, boolean validate)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Article.class);
			Collection articles = new ArrayList(articleIDs.size());
			Set offers = validate ? new HashSet() : null;
			for (Iterator it = articleIDs.iterator(); it.hasNext(); ) {
				ArticleID articleID = (ArticleID) it.next();
				Article article = (Article) pm.getObjectById(articleID);
				articles.add(article);

				if (validate)
					offers.add(article.getOffer());
			}

			Trader trader = Trader.getTrader(pm);
			trader.deleteArticles(User.getUser(pm, getPrincipal()), articles);

			if (validate) {
				for (Iterator it = offers.iterator(); it.hasNext(); ) {
					Offer offer = (Offer) it.next();
					trader.validateOffer(offer);
				}
			}

		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection releaseArticles(Collection articleIDs, boolean synchronously, boolean get, String[] fetchGroups, int maxFetchDepth)
		throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Collection articles = NLJDOHelper.getObjectList(pm, articleIDs, Article.class);

			Trader trader = Trader.getTrader(pm);
			trader.releaseArticles(User.getUser(pm, getPrincipal()), articles, synchronously, true);

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopyAll(articles);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialize()
	throws IOException, MalformedVersionException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireTradeEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of JFireTrade-ConfigModules started...");

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = new ModuleMetaData(
					JFireTradeEAR.MODULE_NAME, "1.0.0-0-beta", "1.0.0-0-beta");
			pm.makePersistent(moduleMetaData);
			
			ConfigSetup configSetup = ConfigSetup.getConfigSetup(
					pm, 
					getOrganisationID(), 
					UserConfigSetup.CONFIG_SETUP_TYPE_USER
				);
			configSetup.getConfigModuleClasses().add(LegalEntityViewConfigModule.class.getName());


//			// create the essential OfferStateDefinitions
//			OfferStateDefinitionUtil offerStateDefinitionUtil;
//
//			offerStateDefinitionUtil = new OfferStateDefinitionUtil(OfferStateDefinitionUtil.STATE_DEFINITION_ID_CREATED);
//			offerStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "created");
//			offerStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Offer has been newly created. This is the first state in the Offer related workflow.");
//			pm.makePersistent(offerStateDefinitionUtil);
//
//			offerStateDefinitionUtil = new OfferStateDefinitionUtil(OfferStateDefinitionUtil.STATE_DEFINITION_ID_FINALIZED);
//			offerStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "created");
//			offerStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Offer has been finalized. After that, it cannot be modified anymore. A modification would require cancellation and recreation.");
//			pm.makePersistent(offerStateDefinitionUtil);
//
//			offerStateDefinitionUtil = new OfferStateDefinitionUtil(OfferStateDefinitionUtil.STATE_DEFINITION_ID_ACCEPTED);
//			offerStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "accepted");
//			offerStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Offer has been accepted by the customer. That turns the offer into a binding contract.");
//			pm.makePersistent(offerStateDefinitionUtil);
//
//			offerStateDefinitionUtil = new OfferStateDefinitionUtil(OfferStateDefinitionUtil.STATE_DEFINITION_ID_CANCELLED);
//			offerStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "cancelled");
//			offerStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Offer has been cancelled by the vendor. The result is the same as if the customer had rejected the offer. A cancellation is possible even after the customer has accepted the Offer.");
//			pm.makePersistent(offerStateDefinitionUtil);
//
//			offerStateDefinitionUtil = new OfferStateDefinitionUtil(OfferStateDefinitionUtil.STATE_DEFINITION_ID_REJECTED);
//			offerStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "rejected");
//			offerStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Offer has been rejected by the customer. A new Offer needs to be created in order to continue the interaction.");
//			pm.makePersistent(offerStateDefinitionUtil);
//
//			offerStateDefinitionUtil = new OfferStateDefinitionUtil(OfferStateDefinitionUtil.STATE_DEFINITION_ID_EXPIRED);
//			offerStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "expired");
//			offerStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Offer has expired - the customer waited too long. A new Offer needs to be created in order to continue the interaction.");
//			pm.makePersistent(offerStateDefinitionUtil);
//
//			offerStateDefinitionUtil = new OfferStateDefinitionUtil(OfferStateDefinitionUtil.STATE_DEFINITION_ID_ABORTED);
//			offerStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "aborted");
//			offerStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Offer has been aborted by the vendor. A new Offer needs to be created in order to continue the interaction.");
//			pm.makePersistent(offerStateDefinitionUtil);
//
//			offerStateDefinitionUtil = new OfferStateDefinitionUtil(OfferStateDefinitionUtil.STATE_DEFINITION_ID_REVOKED);
//			offerStateDefinitionUtil.getName().setText(Locale.ENGLISH.getLanguage(), "revoked");
//			offerStateDefinitionUtil.getDescription().setText(Locale.ENGLISH.getLanguage(), "The Offer has been revoked by the vendor. A new Offer needs to be created in order to continue the interaction.");
//			pm.makePersistent(offerStateDefinitionUtil);


			// persist process definitions
			ProcessDefinition pd;
			pd = ProcessDefinitionUtil.storeProcessDefinition(pm, TradeManagerBean.class.getResource("jbpm/offer/customer/")); // processdefinition.xml"));
			pm.makePersistent(new ProcessDefinitionAssignment(Offer.class, TradeSide.customer, pd));

			pd = ProcessDefinitionUtil.storeProcessDefinition(pm, TradeManagerBean.class.getResource("jbpm/offer/vendor/")); // processdefinition.xml"));
			pm.makePersistent(new ProcessDefinitionAssignment(Offer.class, TradeSide.vendor, pd));

		} finally {
			pm.close();
		}
	}

	/**
	 * This method assigns a customer to an {@link Order}. This fails with
	 * an {@link IllegalStateException}, if the <code>Order</code> contains
	 * at least one finalized {@link Offer}.
	 *
	 * @param orderID The ID of the {@link Order} that shall be linked to another customer.
	 * @param customerID The ID of the {@link LegalEntity} which shall be the new customer.
	 * @throws ModuleException 
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Order assignCustomer(OrderID orderID, AnchorID customerID, boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(Order.class);
			pm.getExtent(LegalEntity.class);

			Order order = (Order) pm.getObjectById(orderID);
			LegalEntity newCustomer = (LegalEntity) pm.getObjectById(customerID);

			// check offers for finalization
			for (Offer offer : order.getOffers()) {
				 if (offer.isFinalized())
					 throw new IllegalStateException("Order contains finalized Offer: " + JDOHelper.getObjectId(offer));

				 JDOHelper.makeDirty(offer, "finalizeDT"); // force the offer to become dirty as the virtually assigned customerID isn't correct anymore => cache notification
			}

			order.setCustomer(newCustomer);

			if (!get)
				return null;

			return (Order) pm.detachCopy(order);
		} finally {
			pm.close();
		}
	}
}
