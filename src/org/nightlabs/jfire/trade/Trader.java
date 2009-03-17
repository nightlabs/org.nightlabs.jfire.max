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
import java.io.Serializable;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope;
import org.nightlabs.jfire.asyncinvoke.ErrorCallback;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.asyncinvoke.InvocationError;
import org.nightlabs.jfire.asyncinvoke.UndeliverableCallback;
import org.nightlabs.jfire.asyncinvoke.UndeliverableCallbackResult;
import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.jbpm.graph.def.Transition;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.NotAvailableException;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductLocal;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeActionHandler;
import org.nightlabs.jfire.store.ProductTypeActionHandlerCache;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.id.ProductID;
import org.nightlabs.jfire.trade.config.OfferConfigModule;
import org.nightlabs.jfire.trade.config.TradeConfigModule;
import org.nightlabs.jfire.trade.endcustomer.EndCustomerReplicationPolicy;
import org.nightlabs.jfire.trade.history.ProductHistory;
import org.nightlabs.jfire.trade.history.ProductHistoryItem;
import org.nightlabs.jfire.trade.history.ProductHistoryItem.ProductHistoryItemType;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OfferLocalID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerAcceptOffer;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerAcceptOfferImplicitelyVendor;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerFinalizeOffer;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerFinalizeOfferForCrossTrade;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerRejectOffer;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerSendOffer;
import org.nightlabs.jfire.trade.jbpm.JbpmConstantsOffer;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.trade.jbpm.id.ProcessDefinitionAssignmentID;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.WorkstationResolveStrategy;
import org.nightlabs.l10n.NumberFormatter;

/**
 * Trader is responsible for purchase and sale. It manages orders, offers and
 * delegates to the Store and to the Accounting.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.TraderID"
 *		detachable="true"
 *		table="JFireTrade_Trader"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy="new-table"
 */
public class Trader
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(Trader.class);

	/**
	 * This method returns the singleton instance of Trader. If there is no
	 * instance of Trader in the datastore, yet, it will be created.
	 *
	 * @param pm
	 * @return
	 */
	public static Trader getTrader(PersistenceManager pm)
	{
		Iterator<?> it = pm.getExtent(Trader.class).iterator();
		if (it.hasNext()) {
			Trader trader = (Trader) it.next();
			// TODO remove this debug stuff
			String securityReflectorOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
			if (!securityReflectorOrganisationID.equals(trader.getOrganisationID()))
				throw new IllegalStateException("SecurityReflector returned organisationID " + securityReflectorOrganisationID + " but Trader.organisationID=" + trader.getOrganisationID());
			// TODO end debug
			return trader;
		}

		logger.info("getTrader: The Trader instance does not yet exist! Creating it...");

		Trader trader = new Trader();

		trader.store = Store.getStore(pm);
		trader.accounting = Accounting.getAccounting(pm);
		trader.organisationID = trader.accounting.getOrganisationID();
		trader.mandator = trader.accounting.getMandator();

		// create customer groups
		CustomerGroup anonymousCustomerGroup = new CustomerGroup(trader.organisationID, CustomerGroup.CUSTOMER_GROUP_ID_ANONYMOUS);
		anonymousCustomerGroup.getName().setText(Locale.GERMAN.getLanguage(), "Anonym");
		anonymousCustomerGroup.getName().setText(Locale.FRENCH.getLanguage(), "Anonyme");
		anonymousCustomerGroup.getName().setText(Locale.ENGLISH.getLanguage(), "Anonymous");
		anonymousCustomerGroup = pm.makePersistent(anonymousCustomerGroup);

		CustomerGroup resellerCustomerGroup = new CustomerGroup(trader.organisationID, CustomerGroup.CUSTOMER_GROUP_ID_RESELLER);
		resellerCustomerGroup.getName().setText(Locale.GERMAN.getLanguage(), "Wiederverkäufer");
//		resellerCustomerGroup.getName().setText(Locale.FRENCH.getLanguage(), "Reseller");
		resellerCustomerGroup.getName().setText(Locale.ENGLISH.getLanguage(), "Reseller");
		resellerCustomerGroup = pm.makePersistent(resellerCustomerGroup);

		CustomerGroup defaultCustomerGroup = new CustomerGroup(trader.organisationID, CustomerGroup.CUSTOMER_GROUP_ID_DEFAULT);
		defaultCustomerGroup.getName().setText(Locale.GERMAN.getLanguage(), "Standard");
		defaultCustomerGroup.getName().setText(Locale.FRENCH.getLanguage(), "Standard");
		defaultCustomerGroup.getName().setText(Locale.ENGLISH.getLanguage(), "Default");
		defaultCustomerGroup = pm.makePersistent(defaultCustomerGroup);

		trader.defaultCustomerGroupForKnownCustomer = defaultCustomerGroup;
		trader.defaultCustomerGroupForReseller = resellerCustomerGroup;
		trader = pm.makePersistent(trader);

		// Normally this is done by OrganisationLegalEntity.getOrganisationLegalEntity(...), but since this would cause an endless recursion, we skip it in this situation
		// there and do it here.
		trader.mandator.setDefaultCustomerGroup(defaultCustomerGroup);

		logger.info("getTrader: ...new Trader instance created and persisted!");
		return trader;
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Accounting accounting;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Store store;

	/**
	 * The mandator is the LegalEntity that is represented by this Trader.
	 */
	private OrganisationLegalEntity mandator;

	public Accounting getAccounting()
	{
		return accounting;
	}

	public Store getStore()
	{
		return store;
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		String res = organisationID;
		// should never happen, but better we try a workaround than to get an exception
		if (res == null) {
			try {
				res = accounting.getOrganisationID();
				organisationID = res;
			} catch (Exception x) {
				res = null;
			}
			if (res == null) {
				res = LocalOrganisation.getLocalOrganisation(getPersistenceManager()).getOrganisationID();
				organisationID = res;
			}
		}
		return res;
	}

	/**
	 * TODO We don't know yet how we handle the CustomerGroups in the product
	 * chain. This member might be removed soon.
	 *
	 * Update: Probably we will add a mapping of customer groups. Hence, this
	 * member will stay here and always contain a local CustomerGroup (of this
	 * organisation). There will be a mapping within the priceconfig and a global
	 * one. The priceconfig- customergroup-mapping will inherit the global one.
	 *
	 * Update2: The mapping exists. Need to change the complete javadoc here and
	 * add javadoc to {@link #defaultCustomerGroupForReseller}.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private CustomerGroup defaultCustomerGroupForKnownCustomer;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private CustomerGroup defaultCustomerGroupForReseller;

	/**
	 * @return Returns the <tt>CustomerGroup</tt> that is automatically assigned
	 *         to all newly created customers as
	 *         {@link LegalEntity#defaultCustomerGroup}. Note, that the anonymous
	 *         customer has a different <tt>CustomerGroup</tt> assigned! The same
	 *         applies to resellers - see {@link #getDefaultCustomerGroupForReseller()}.
	 */
	public CustomerGroup getDefaultCustomerGroupForKnownCustomer()
	{
		return defaultCustomerGroupForKnownCustomer;
	}

	/**
	 * @return Returns the <tt>CustomerGroup</tt> that is automatically assigned
	 *         to all reseller-organisations as
	 *         {@link LegalEntity#defaultCustomerGroup}. This assignment happens during
	 *         cross-organisation-registration (in a callback).
	 */
	public CustomerGroup getDefaultCustomerGroupForReseller() {
		return defaultCustomerGroupForReseller;
	}

	/**
	 * @param defaultCustomerGroupForKnownCustomer
	 *          The defaultCustomerGroupForKnownCustomer to set.
	 *
	 * @see #getDefaultCustomerGroupForKnownCustomer()
	 */
	public void setDefaultCustomerGroupForKnownCustomer(CustomerGroup customerGroupForEndCustomer)
	{
		if (!getOrganisationID().equals(customerGroupForEndCustomer.getOrganisationID()))
			throw new IllegalArgumentException(
			"defaultCustomerGroupForKnownCustomer.organisationID is foreign!");

		this.defaultCustomerGroupForKnownCustomer = customerGroupForEndCustomer;
	}

	public void setDefaultCustomerGroupForReseller(CustomerGroup defaultCustomerGroupForReseller)
	{
		if (!getOrganisationID().equals(defaultCustomerGroupForReseller.getOrganisationID()))
			throw new IllegalArgumentException(
			"defaultCustomerGroupForReseller.organisationID is foreign!");

		this.defaultCustomerGroupForReseller = defaultCustomerGroupForReseller;
	}

	/**
	 * The mandator is the OrganisationLegalEntity which is represented by this
	 * Trader, means this Trader manages the datastore on behalf of this entity.
	 *
	 * @return Returns the mandator.
	 */
	public OrganisationLegalEntity getMandator()
	{
		return mandator;
	}

//	public LegalEntity getVendor(String organisationID, String anchorID)
//	{
//		// TODO: implement
//		return null;
//		// String vendorPK = LegalEntity.getPrimaryKey(organisationID, anchorID);
//		// return (LegalEntity) vendors.get(vendorPK);
//	}
//
//	public LegalEntity getCustomer(String organisationID, String anchorID)
//	{
//		// TODO: implement
//		return null;
//		// String customerPK = LegalEntity.getPrimaryKey(organisationID, anchorID);
//		// return (LegalEntity) vendors.get(customerPK);
//	}

	/**
	 * Creates a new OrganisationLegalEntity, if it does not yet exist. If it is
	 * already existing, this method doesn't do anything but only returns the
	 * previously created instance.
	 *
	 * @param organisationID
	 * @return Returns an OrganisationLegalEntity for the given organisationID
	 */
	public OrganisationLegalEntity getOrganisationLegalEntity(
			String organisationID)
	{
		PersistenceManager pm = getPersistenceManager();
		return OrganisationLegalEntity.getOrganisationLegalEntity(pm,
				organisationID);

		// if (organisationLegalEntity != null)
		// return organisationLegalEntity;
		//
		// Organisation org = Organisation.getOrganisation(pm, organisationID,
		// false);
		// if (org == null)
		// throw new IllegalArgumentException("There is no Organistaion known with
		// organisationID "+organisationID);
		//
		// organisationLegalEntity = new OrganisationLegalEntity(org);
		// organisationLegalEntity.setPerson(org.getPerson());
		// pm.makePersistent(organisationLegalEntity);
		//
		// return organisationLegalEntity;
	}

	/**
	 * Checks if the LegalEntity with the appropriate anchorID is existent and
	 * creates one if not. The given person will be set to the found or created
	 * LegalEntity. If makePersistent is true the newly created LegalEntity will
	 * be made persistent.
	 *
	 * @param person
	 *          The person to set to the LegalEntity
	 * @param makePersistent
	 *          Whether a newly created LegalEntity should be made persistent
	 * @return The legal entity for this person
	 */
	public LegalEntity setPersonToLegalEntity(Person person, boolean makePersistent)
	{
		if (person == null)
			throw new IllegalArgumentException("person must not be null!");

		PersistenceManager pm = getPersistenceManager();
		boolean found = true;

		LegalEntity legalEntity = LegalEntity.getLegalEntity(pm, person);
		if (legalEntity == null) {
			found = false;
			legalEntity = new LegalEntity(IDGenerator.getOrganisationID(), LegalEntity.nextAnchorID());
			legalEntity.setDefaultCustomerGroup(getDefaultCustomerGroupForKnownCustomer());
		}

		legalEntity.setPerson(person);
		if (makePersistent && !found)
			legalEntity = pm.makePersistent(legalEntity); // if it already is persistent, this call has simply no effect - no problem

		return legalEntity;
	}

	public Collection<Segment> createSegments(Order order, Collection<SegmentType> segmentTypes)
	{
		List<Segment> segments = new ArrayList<Segment>();
		for (SegmentType segmentType : segmentTypes) {
			segments.add(createSegment(order, segmentType));
		}
		return segments;
	}

	/**
	 * Creates a new <tt>Segment</tt> within the given <tt>Order</tt> for the
	 * given <tt>SegmentType</tt>. Note, that you can create many
	 * <tt>Segment</tt>s with the same <tt>SegmentType</tt>.
	 *
	 * @param order
	 *          The <tt>Order</tt> in which to create a <tt>Segment</tt>.
	 * @param segmentType
	 *          Can be <tt>null</tt>. If undefined, the default segmentType
	 *          will be used.
	 * @return the newly created <tt>Segment</tt>
	 */
	public Segment createSegment(Order order, SegmentType segmentType)
	{
		if (!getOrganisationID().equals(order.getOrganisationID())) // TODO implement
			// later.
			throw new UnsupportedOperationException(
			"Cannot yet create a Segment in a foreign order.");

		PersistenceManager pm = getPersistenceManager();

		if (segmentType == null) {
			segmentType = SegmentType.getDefaultSegmentType(pm);
		} // if (segmentType == null) {

		Segment segment = new Segment(getOrganisationID(), Segment.createSegmentID(),
				segmentType, order);
		order.addSegment(segment);

		return segment;
	}

	// public Order getOrder(OrderID orderID) throws OrderNotFoundException {
	// Order order = null;
	// try {
	// order = (Order) getPersistenceManager().getObjectById(orderID);
	// } catch (JDOObjectNotFoundException e) {
	// throw new OrderNotFoundException("Could not find order with OrderID:
	// "+orderID);
	// }
	// return order;
	// }

	public Offer createReverseOffer(User user, Set<Article> reversedArticles, String offerIDPrefix)
	{
		if (reversedArticles.isEmpty())
			throw new IllegalArgumentException("Set reversedArticles must not be empty!");

		if (offerIDPrefix == null) {
			TradeConfigModule tradeConfigModule;
			tradeConfigModule = Config.getConfig(
					getPersistenceManager(), getOrganisationID(), user).createConfigModule(TradeConfigModule.class);

			offerIDPrefix = tradeConfigModule.getActiveIDPrefixCf(Offer.class.getName()).getDefaultIDPrefix();
		}

		Order order = null;
		for (Article article : reversedArticles) {
			if (order == null)
				order = article.getOrder();
			else if (!order.equals(article.getOrder()))
				throw new IllegalArgumentException("Not all reversedArticles in the same Order!");
		}

		Offer offer = new Offer(
				user, order,
				offerIDPrefix, IDGenerator.nextID(Offer.class, offerIDPrefix));
		new OfferLocal(offer); // self-registering
		offer = getPersistenceManager().makePersistent(offer);

		ProcessDefinitionAssignment processDefinitionAssignment = (ProcessDefinitionAssignment) getPersistenceManager().getObjectById(
				ProcessDefinitionAssignmentID.create(Offer.class, TradeSide.vendor));
		processDefinitionAssignment.createProcessInstance(null, user, offer);

		reverseArticles(user, offer, reversedArticles);
		return offer;
	}

	public String getOrderIDPrefix(User user, String orderIDPrefix)
	{
		if (orderIDPrefix == null) {
			TradeConfigModule tradeConfigModule;
			tradeConfigModule = Config.getConfig(
					getPersistenceManager(), getOrganisationID(), user).createConfigModule(TradeConfigModule.class);
			orderIDPrefix = tradeConfigModule.getActiveIDPrefixCf(Order.class.getName()).getDefaultIDPrefix();
		}
		return orderIDPrefix;
	}

	public Order createOrder(LegalEntity vendor,
			LegalEntity customer, String orderIDPrefix, Currency currency)
//	throws ModuleException
	{
		if (customer == null)
			throw new IllegalArgumentException("customer must not be null!");

		if (currency == null)
			throw new IllegalArgumentException("currency must not be null!");

		PersistenceManager pm = getPersistenceManager();

		// LocalOrganisation localOrganisation =
		// LocalOrganisation.getLocalOrganisation(pm);

		if (!getMandator().equals(vendor) && (vendor instanceof OrganisationLegalEntity)) {
			// TODO: Implement foreign stuff
			throw new UnsupportedOperationException("NYI");
		}
		else {
			// local: the vendor is the local organisation (owning this datastore) OR it is a locally managed non-organisation-LE
			User user = SecurityReflector.getUserDescriptor().getUser(pm);

			orderIDPrefix = getOrderIDPrefix(user, orderIDPrefix);

			Order order = new Order(
					vendor, customer,
					orderIDPrefix, IDGenerator.nextID(Order.class, orderIDPrefix),
					currency, user);

			order = getPersistenceManager().makePersistent(order);
			return order;
		}
	}

//	public OrderRequirement createOrderRequirement(Order order)
//	{
//	if (!order.getOrganisationID().equals(this.getOrganisationID()))
//	throw new IllegalArgumentException(
//	"Cannot create an instance of OrderRequirement for a foreign Organisation!");

//	// TODO implement this
//	throw new UnsupportedOperationException("NYI");
//	// OrderRequirement orderRequirement =
//	// (OrderRequirement)orderRequirements.get(order.getPrimaryKey());
//	// if (orderRequirement == null) {
//	// orderRequirement = new OrderRequirement(this, order);
//	// orderRequirements.put(order.getPrimaryKey(), orderRequirement);
//	// }
//	// return orderRequirement;
//	}

//	public OfferRequirement createOfferRequirement(Offer offer)
//	{
//	if (!offer.getOrganisationID().equals(this.getOrganisationID()))
//	throw new IllegalArgumentException(
//	"Cannot create an instance of OfferRequirement for a foreign Organisation!");

//	// TODO implement this
//	throw new UnsupportedOperationException("NYI");
//	// OfferRequirement offerRequirement =
//	// (OfferRequirement)offerRequirements.get(offer.getPrimaryKey());
//	// if (offerRequirement == null) {
//	// offerRequirement = new OfferRequirement(this, offer);
//	// offerRequirements.put(offer.getPrimaryKey(), offerRequirement);
//	// }
//	// return offerRequirement;
//	}

//	/**
//	* This method creates a new Offer for the given vendor or returns a
//	* previously created one.
//	*
//	* @param vendor
//	* @return Returns the offer for the given vendor. Never returns <tt>null</tt>.
//	* @throws ModuleException
//	*/
//	public Offer createOfferRequirementOffer(OfferRequirement offerRequirement,
//	OrganisationLegalEntity vendor, String orderIDPrefix) throws ModuleException // TODO shouldn't orderIDPrefix be looked up or generated automatically?
//	{
//	Offer offer = offerRequirement.getOfferByVendor(vendor);
//	if (offer == null) {
//	// We don't have an Offer registered, thus we need to create one.
//	// Therefore, we first need the OrderRequirement instance assigned
//	// for the order equivalent.
//	OrderRequirement orderRequirement = createOrderRequirement(offerRequirement
//	.getOffer().getOrder());

//	// From the OrderRequirement, we obtain the order for the given vendor.
//	// Order order = orderRequirement.createOrder(vendor);
//	Order order = createOrderRequirementOrder(orderRequirement, vendor, orderIDPrefix);

//	// offer = createOffer();
//	offerRequirement.addOffer(offer);
//	}
//	return offer;
//	}

//	/**
//	* This method creates a new Order for the given vendor or returns a
//	* previously created one.
//	*
//	* @param vendor
//	* @return Returns the order for the given vendor. Never returns <tt>null</tt>.
//	* @throws ModuleException
//	*/
//	public Order createOrderRequirementOrder(OrderRequirement orderRequirement,
//	OrganisationLegalEntity vendor, String orderIDPrefix) throws ModuleException // TODO shouldn't orderIDPrefix be looked up or generated automatically?
//	{
//	Order order = orderRequirement.getPartnerOrder(vendor);
//	if (order == null) {
//	order = createOrder(vendor, getMandator(), orderIDPrefix, order.getCurrency());
//	orderRequirement.addOrder(order);
//	}
//	return order;
//	}

	public Offer createOffer(User user, Order order, String offerIDPrefix)
	{
		TradeSide tradeSide;

		LegalEntity vendor = order.getVendor();
		if (vendor == null)
			throw new IllegalStateException("order.getVendor() returned null!");

		LegalEntity customer = order.getCustomer();
		if (customer == null)
			throw new IllegalStateException("order.getCustomer() returned null!");

		if (getMandator().equals(customer) && (vendor instanceof OrganisationLegalEntity)) {
			tradeSide = TradeSide.customerCrossOrganisation;
			// TODO: Implement foreign stuff
			throw new UnsupportedOperationException("NYI");
		}
		else {
			if (getMandator().equals(vendor))
				tradeSide = TradeSide.vendor;
			else if (getMandator().equals(customer))
				tradeSide = TradeSide.customerLocal;
			else
				throw new IllegalStateException("mandator is neither customer nor vendor! order=" + order + " mandator=" + getMandator());

			if (offerIDPrefix == null) {
				TradeConfigModule tradeConfigModule;
				tradeConfigModule = Config.getConfig(getPersistenceManager(), organisationID, user).createConfigModule(TradeConfigModule.class);
				offerIDPrefix = tradeConfigModule.getActiveIDPrefixCf(Offer.class.getName()).getDefaultIDPrefix();
			}

			Offer offer = new Offer(
					user, order,
					offerIDPrefix, IDGenerator.nextID(Offer.class, offerIDPrefix));

			new OfferLocal(offer); // OfferLocal registers itself in Offer

			offer = getPersistenceManager().makePersistent(offer);
			validateOffer(offer);

			ProcessDefinitionAssignment processDefinitionAssignment = (ProcessDefinitionAssignment) getPersistenceManager().getObjectById(
					ProcessDefinitionAssignmentID.create(Offer.class, tradeSide));
			processDefinitionAssignment.createProcessInstance(null, user, offer);

//			Config.getConfig(pm, organisationID, configKey, configType)

			setOfferExpiry(offer);

			return offer;
		}
	}

	public void setOfferExpiry(Offer offer)
	{
		if (offer.isFinalized()) // unmodifiable after finalization.
			return;

		Workstation workstation = Workstation.getWorkstation(getPersistenceManager(), WorkstationResolveStrategy.FALLBACK);
		OfferConfigModule offerConfigModule = Config.getConfig(getPersistenceManager(), organisationID, workstation).createConfigModule(OfferConfigModule.class);
		offerConfigModule.setOfferExpiry(offer);
	}

	public Set<Article> reverseArticles(User user, Offer reversingOffer, Collection<Article> reversedArticles)
	{
		Set<Article> res = new HashSet<Article>(reversedArticles.size());
//		for (Iterator it = reversedArticles.iterator(); it.hasNext();) {
//		Article reversedArticle = (Article) it.next();
		for (Article reversedArticle : reversedArticles) {
			if (!reversedArticle.getOffer().getOfferLocal().isAccepted())
				throw new IllegalStateException("Offer " + reversedArticle.getOffer().getPrimaryKey() + " of Article " + reversedArticle.getPrimaryKey() + " has NOT been accepted! Cannot create reversing Article!");

			Article reversingArticle = reversedArticle.reverseArticle(user, reversingOffer, Article.createArticleID());
			reversingArticle.createArticleLocal(user);
			res.add(reversingArticle);
		}
		return res;
	}


//	public Article reverseArticle(User user, Offer offer, Article reversedArticle)
//	throws ModuleException
//	{
//	if (!reversedArticle.getOffer().getOfferLocal().isConfirmed())
//	throw new IllegalStateException("Offer " + reversedArticle.getOffer().getPrimaryKey() + " of Article " + reversedArticle.getPrimaryKey() + " is NOT confirmed! Cannot create reversing Article!");

//	Article reversingArticle = reversedArticle.reverseArticle(user, offer, createArticleID());
//	reversingArticle.createArticleLocal(user);
//	return reversingArticle;
//	}

	protected void createArticleLocals(User user, Collection<? extends Article> articles)
	{
//		for (Iterator it = articles.iterator(); it.hasNext();) {
//		Article article = (Article) it.next();
		for (Article article : articles) {
			article.createArticleLocal(user);
		}
	}

	/**
	 * This method creates an <tt>Article</tt> for a noncommittal offer (just to
	 * give the customer an idea about the price). Therefore, no <tt>Product</tt>
	 * will be allocated and only the <tt>ProductType</tt> referenced as given
	 * here.
	 *
	 * @param productTypes
	 *          Instances of {@link ProductType}.
	 */
	public Collection<? extends Article>  createArticles(User user, Offer offer, Segment segment,
			Collection<ProductType> productTypes, ArticleCreator articleCreator)
			throws ModuleException
			{
		if (!segment.getOrder().equals(offer.getOrder()))
			throw new IllegalArgumentException("segment.order != offer.order :: " + segment.getOrder().getPrimaryKey() + " != " + offer.getOrder().getPrimaryKey());

		PersistenceManager pm = getPersistenceManager();


		Collection<? extends Article> articles = articleCreator.createProductTypeArticles(this, user, offer,
				segment, productTypes);

		for (Article article : articles) {
			article.createArticleLocal(user);
		}
		// WORKAROUND begin
		articles = pm.makePersistentAll(articles);
		// WORKAROUND end

		offer.addArticles(articles);
		// create the Articles' prices
		for (Article article : articles) {
			IPackagePriceConfig packagePriceConfig = article.getProductType()
			.getPackagePriceConfig();
			article.setPrice(packagePriceConfig.createArticlePrice(article));
		}

		offer.validate();

		setOfferExpiry(offer);

		return articles;
			}

	/**
	 * This method creates an <tt>Article</tt> with a specific <tt>Product</tt>.
	 * If <tt>allocate</tt> is true, the method
	 * {@link #allocateArticleBegin(User, Article)} is called immediately and the
	 * method {@link #allocateArticlesEnd(String, User, Collection)} is either called
	 * asynchronously or immediately, depending on
	 * <code>allocateSynchronously</code>.
	 *
	 * @param user
	 *          The User who is responsible for the action.
	 * @param offer
	 *          The non-finalized Offer into which the new Article shall be
	 *          created.
	 * @param segment
	 *          The Segment into which the new Article shall be created.
	 * @param products
	 *          The {@link Product}s that shall be wrapped by {@link Article}s.
	 * @param articleCreator
	 *          The ArticleCreator that provides the new naked Article (without
	 *          price and not allocated).
	 * @param allocate
	 *          Whether or not to allocate. This causes the methods
	 *          {@link #allocateArticleBegin(User, Article)} and
	 *          {@link #allocateArticleEnd(User, Article)} to be executed.
	 * @param allocateSynchronously
	 *          The method {@link #allocateArticleEnd(User, Article)} is quite
	 *          expensive (it may require to create and handle offers with other
	 *          organisations). That's why you can set this <code>false</code>
	 *          in order to call this method asynchronously. This param is
	 *          ignored, if <code>allocate == false</code>.
	 * @return Instances of {@link Article}.
	 */
	public Collection<? extends Article> createArticles(User user, Offer offer, Segment segment,
			Collection<? extends Product> products, ArticleCreator articleCreator, boolean allocate,
			boolean allocateSynchronously)
	{
		if (!segment.getOrder().equals(offer.getOrder()))
			throw new IllegalArgumentException("segment.order != offer.order :: " + segment.getOrder().getPrimaryKey() + " != " + offer.getOrder().getPrimaryKey());

		Collection<? extends Article> articles = articleCreator.createProductArticles(this, user, offer, segment, products);

		createArticleLocals(user, articles);

		// WORKAROUND begin
		PersistenceManager pm = getPersistenceManager();
		articles = pm.makePersistentAll(articles);
		// WORKAROUND end

		offer.addArticles(articles);

		if (allocate) {
			// allocateArticle (re)creates the price already => no need to create the
			// price.
			allocateArticles(user, articles, allocateSynchronously);
		}
		else {
			// create the Articles' prices
			for (Article article : articles) {
				IPackagePriceConfig packagePriceConfig = article.getProductType()
				.getPackagePriceConfig();
				article.setPrice(packagePriceConfig.createArticlePrice(article));
			}
		}

		offer.validate();

		setOfferExpiry(offer);

		return articles;
			}

	/**
	 * @param user
	 *          Who is responsible for the allocation.
	 * @param articles
	 *          Instances of {@link Article}
	 * @param synchronously
	 *          Whether the second phase of allocation shall be done
	 *          synchronously. Otherwise it will be done via {@link AsyncInvoke}.
	 */
	public void allocateArticles(User user, Collection<? extends Article> articles, boolean synchronously)
	{
		try {
			String allocateExecID = ObjectIDUtil.makeValidIDString("allocate", true);

			allocateArticlesBegin(allocateExecID, user, articles); // allocateArticleBegin
			// (re)creates the price

			if (synchronously)
				allocateArticlesEnd(allocateExecID, user, articles);
			else
				AsyncInvoke.exec(
						new AllocateArticlesEndInvocation(allocateExecID, user, articles),
						null,
						new AllocateArticlesEndErrorCallback(),
						new AllocateArticlesEndUndeliverableCallback(), true);

		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public static class AllocateArticlesEndUndeliverableCallback extends UndeliverableCallback
	{
		private static final long serialVersionUID = 1L;

		@Override
		public UndeliverableCallbackResult handle(AsyncInvokeEnvelope envelope) throws Exception
		{
			PersistenceManager pm = getPersistenceManager();
			try {
				AllocateArticlesEndInvocation allocateArticlesEndInvocation = (AllocateArticlesEndInvocation) envelope.getInvocation();
				String allocateExecID = allocateArticlesEndInvocation.getAllocateExecID();

				Collection<ArticleID> articleIDs = ((AllocateArticlesEndInvocation) envelope.getInvocation()).getArticleIDs();
//				for (Iterator iter = articleIDs.iterator(); iter.hasNext();) {
//				ArticleID articleID = (ArticleID) iter.next();

				Collection<Article> articles;
				NLJDOHelper.enableTransactionSerializeReadObjects(pm);
				try {
					articles = NLJDOHelper.getObjectSet(pm, articleIDs, Article.class);
					pm.refreshAll(articles);
				} finally {
					NLJDOHelper.disableTransactionSerializeReadObjects(pm);
				}

				for (Article article : articles) {
					if (!allocateExecID.equals(article.getArticleLocal().getAllocateReleaseExecID())) {
						logger.warn("AllocateArticlesEndUndeliverableCallback.handle: allocateExecID != article.articleLocal.allocateReleaseExecID :: " + allocateExecID + " != " + article.getArticleLocal().getAllocateReleaseExecID() + " :: " + article);
						continue;
					}

//					Article article = null;
//					try {
//						article = (Article) pm.getObjectById(articleID);
//					} catch (JDOObjectNotFoundException x) {
//						logger.error("AllocateArticlesEndUndeliverableCallback: Article does not exist in datastore: " + articleID);
//					}
//
//					if (article != null)
						article.setAllocationAbandoned(true);
				}
			} finally {
				pm.close();
			}

			return null;
		}
	}

	public static class AllocateArticlesEndErrorCallback extends ErrorCallback
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void handle(AsyncInvokeEnvelope envelope)
		throws Exception
		{
			PersistenceManager pm = getPersistenceManager();
			try {
				AllocateArticlesEndInvocation allocateArticlesEndInvocation = (AllocateArticlesEndInvocation) envelope.getInvocation();
				String allocateExecID = allocateArticlesEndInvocation.getAllocateExecID();
				InvocationError invocationError = envelope.getAsyncInvokeProblem(pm).getLastError();

				Collection<ArticleID> articleIDs = ((AllocateArticlesEndInvocation) envelope.getInvocation()).getArticleIDs();
//				for (Iterator iter = articleIDs.iterator(); iter.hasNext();) {
//				ArticleID articleID = (ArticleID) iter.next();

				Collection<Article> articles;
				NLJDOHelper.enableTransactionSerializeReadObjects(pm);
				try {
					articles = NLJDOHelper.getObjectSet(pm, articleIDs, Article.class);
					pm.refreshAll(articles);
				} finally {
					NLJDOHelper.disableTransactionSerializeReadObjects(pm);
				}

				for (Article article : articles) {
					if (!allocateExecID.equals(article.getArticleLocal().getAllocateReleaseExecID())) {
						logger.warn("AllocateArticlesEndErrorCallback.handle: allocateExecID != article.articleLocal.allocateReleaseExecID :: " + allocateExecID + " != " + article.getArticleLocal().getAllocateReleaseExecID() + " :: " + article);
						continue;
					}

//					Article article = null;
//					try {
//						article = (Article) pm.getObjectById(articleID);
//					} catch (JDOObjectNotFoundException x) {
//						logger.error("AllocateArticlesEndErrorCallback: Article does not exist in datastore: " + articleID);
//					}
//
//					if (article != null) {
						if (invocationError.getError() != null)
							article.setAllocationException(invocationError.getError());
						else
							article.setAllocationException(invocationError.getErrorRootCauseClassName(), invocationError.getErrorMessage(), invocationError.getErrorStackTrace());
//					}
				}
			} finally {
				pm.close();
			}
		}
	}

	public static class AllocateArticlesEndInvocation extends Invocation
	{
		private static final long serialVersionUID = 2L;

		private String allocateExecID;

		private UserID userID;

		private Set<ArticleID> articleIDs;

		public Set<ArticleID> getArticleIDs()
		{
			return articleIDs;
		}

		public String getAllocateExecID() {
			return allocateExecID;
		}

		public AllocateArticlesEndInvocation(String allocateExecID, User user, Collection<? extends Article> articles)
		{
			if (allocateExecID == null)
				throw new IllegalArgumentException("allocateExecID == null");

			this.allocateExecID = allocateExecID;
			this.userID = (UserID) JDOHelper.getObjectId(user);
			this.articleIDs = NLJDOHelper.getObjectIDSet(articles);
		}

		@Override
		public Serializable invoke() throws Exception
		{
//			// WORKAROUND it doesn't work immediately - the transactions collide
//			try {
//			Thread.sleep(3000);
//			} catch (InterruptedException x) {
//			}

			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(User.class);
				User user = (User) pm.getObjectById(userID);
				Collection<Article>articles = NLJDOHelper.getObjectSet(pm, articleIDs,
						Article.class);
				Trader.getTrader(pm).allocateArticlesEnd(allocateExecID, user, articles);
			} finally {
				pm.close();
			}
			return null;
		}
	}

	/**
	 * If a reversing offer is cancelled (or its workflow is otherwise ended non-successfully), the reversing articles
	 * need to be unregistered from the reversed articles in order for the reversed articles to be reversable in a new
	 * offer.
	 *
	 * @param user the responsible user.
	 * @param reversingArticles the reversing articles that need to be unregistered.
	 */
	public void unregisterReversingArticles(User user, Set<Article> reversingArticles)
	{
		for (Article reversingArticle : reversingArticles) {
			if (!reversingArticle.isReversing())
				throw new IllegalArgumentException("The article \"" + reversingArticle.getPrimaryKey() + "\" is not reversing!");

			Article reversedArticle = reversingArticle.getReversedArticle();
			reversedArticle.setReversingArticle(null);
			reversingArticle.setReversingAborted();
		}
	}

	/**
	 * @param user
	 * @param articles
	 *          Instances of {@link Article}.
	 * @param synchronously
	 */
	public void releaseArticles(User user, Collection<Article> articles, boolean synchronously, boolean deleteAfterRelease)
	{
		try {
			String releaseExecID = ObjectIDUtil.makeValidIDString("release", true);

			releaseArticlesBegin(releaseExecID, user, articles);

			if (synchronously)
				releaseArticlesEnd(releaseExecID, user, articles, deleteAfterRelease);
			else
				AsyncInvoke.exec(
						new ReleaseArticlesEndInvocation(releaseExecID, user, articles, deleteAfterRelease),
						null,
						new ReleaseArticlesEndErrorCallback(),
						new ReleaseArticlesEndUndeliverableCallback(),
						true);

		} catch (RuntimeException x) {
			throw x;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public static class ReleaseArticlesEndUndeliverableCallback extends UndeliverableCallback
	{
		private static final long serialVersionUID = 1L;

		@Override
		public UndeliverableCallbackResult handle(AsyncInvokeEnvelope envelope) throws Exception
		{
			PersistenceManager pm = getPersistenceManager();
			try {
				ReleaseArticlesEndInvocation releaseArticlesEndInvocation = (ReleaseArticlesEndInvocation) envelope.getInvocation();
				String releaseExecID = releaseArticlesEndInvocation.getReleaseExecID();

				Collection<ArticleID> articleIDs = ((ReleaseArticlesEndInvocation) envelope.getInvocation()).getArticleIDs();
//				for (Iterator iter = articleIDs.iterator(); iter.hasNext();) {
//				ArticleID articleID = (ArticleID) iter.next();

				Collection<Article> articles;
				NLJDOHelper.enableTransactionSerializeReadObjects(pm);
				try {
					articles = NLJDOHelper.getObjectSet(pm, articleIDs, Article.class);
					pm.refreshAll(articles);
				} finally {
					NLJDOHelper.disableTransactionSerializeReadObjects(pm);
				}

				for (Article article : articles) {
					if (!releaseExecID.equals(article.getArticleLocal().getAllocateReleaseExecID())) {
						logger.warn("ReleaseArticlesEndUndeliverableCallback.handle: releaseExecID != article.articleLocal.allocateReleaseExecID :: " + releaseExecID + " != " + article.getArticleLocal().getAllocateReleaseExecID() + " :: " + article);
						continue;
					}

					article.setReleaseAbandoned(true);
				}
			} finally {
				pm.close();
			}

			return null;
		}
	}

	public static class ReleaseArticlesEndErrorCallback extends ErrorCallback
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void handle(AsyncInvokeEnvelope envelope)
		throws Exception
		{
			PersistenceManager pm = getPersistenceManager();
			try {
				ReleaseArticlesEndInvocation releaseArticlesEndInvocation = (ReleaseArticlesEndInvocation) envelope.getInvocation();
				String releaseExecID = releaseArticlesEndInvocation.getReleaseExecID();
				InvocationError invocationError = envelope.getAsyncInvokeProblem(pm).getLastError();

				Collection<ArticleID> articleIDs = ((ReleaseArticlesEndInvocation) envelope.getInvocation()).getArticleIDs();
//				for (Iterator iter = articleIDs.iterator(); iter.hasNext();) {
//				ArticleID articleID = (ArticleID) iter.next();

				Collection<Article> articles;
				NLJDOHelper.enableTransactionSerializeReadObjects(pm);
				try {
					articles = NLJDOHelper.getObjectSet(pm, articleIDs, Article.class);
					pm.refreshAll(articles);
				} finally {
					NLJDOHelper.disableTransactionSerializeReadObjects(pm);
				}

				for (Article article : articles) {
					if (!releaseExecID.equals(article.getArticleLocal().getAllocateReleaseExecID())) {
						logger.warn("ReleaseArticlesEndErrorCallback.handle: releaseExecID != article.articleLocal.allocateReleaseExecID :: " + releaseExecID + " != " + article.getArticleLocal().getAllocateReleaseExecID() + " :: " + article);
						continue;
					}

					if (invocationError.getError() != null)
						article.setReleaseException(invocationError.getError());
					else
						article.setReleaseException(invocationError.getErrorRootCauseClassName(), invocationError.getErrorMessage(), invocationError.getErrorStackTrace());
				}
			} finally {
				pm.close();
			}
		}
	}

	public static class ReleaseArticlesEndInvocation extends Invocation
	{
		private static final long serialVersionUID = 1L;

		private String releaseExecID;

		private UserID userID;

		private Collection<ArticleID> articleIDs;

		private boolean deleteAfterRelease;

		public Collection<ArticleID> getArticleIDs()
		{
			return articleIDs;
		}

		public String getReleaseExecID() {
			return releaseExecID;
		}

		public ReleaseArticlesEndInvocation(String releaseExecID, User user, Collection<Article> articles, boolean deleteAfterRelease)
		{
			if (releaseExecID == null)
				throw new IllegalArgumentException("releaseExecID == null");

			this.releaseExecID = releaseExecID;
			this.userID = (UserID) JDOHelper.getObjectId(user);
			this.articleIDs = NLJDOHelper.getObjectIDSet(articles);
			this.deleteAfterRelease = deleteAfterRelease;
		}

		@Override
		public Serializable invoke() throws Exception
		{
//			// WORKAROUND it doesn't work immediately - the transactions collide
//			try {
//			Thread.sleep(3000);
//			} catch (InterruptedException x) {
//			}

			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(User.class);
				User user = (User) pm.getObjectById(userID);
				Collection<Article> articles = NLJDOHelper.getObjectSet(pm, articleIDs, Article.class);
				Trader.getTrader(pm).releaseArticlesEnd(releaseExecID, user, articles, deleteAfterRelease);
			} finally {
				pm.close();
			}
			return null;
		}
	}

	protected void releaseArticlesBegin(String releaseExecID, User user, Collection<Article> articles)
	throws ModuleException
	{
		if (releaseExecID == null)
			throw new IllegalArgumentException("releaseExecID == null");

		if (logger.isDebugEnabled()) {
			logger.debug("releaseArticlesBegin: entered with " + articles.size() + " articles.");
			if (logger.isTraceEnabled()) {
				for (Article article : articles)
					logger.trace("releaseArticlesBegin: * " + article);
			}
		}

		PersistenceManager pm = getPersistenceManager();
		NLJDOHelper.enableTransactionSerializeReadObjects(pm);
		try {
			pm.refreshAll(articles);
			for (Article article : articles) {
				pm.refresh(article.getArticleLocal());
				if (article.getProduct() != null)
					pm.refresh(article.getProduct().getProductLocal());
			}
		} finally {
			NLJDOHelper.disableTransactionSerializeReadObjects(pm);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("releaseArticlesBegin: refreshing articles done.");
		}

		TotalArticleStatus tas = getTotalArticleStatus(articles);

//		if (!tas.allocated || tas.releasePending)
//			return;
//
//		if (tas.allocationPending)
//			throw new IllegalStateException("Articles \"" + getToStringList(articles) + "\" cannot be released, because they are currently in state allocationPending!");

		// Policy change: We allow releasing even when the allocateArticlesEnd was not yet called (i.e. it's still in state allocationPending), because
		// there might be a problem to perform the allocation and we have no other chance to solve the situation than releasing.
		if (tas.releasePending) {
			logger.warn("releaseArticlesBegin: articles are already in state 'releasePending'!", new Exception("StackTrace"));
			for (Article article : articles) {
				logger.warn("releaseArticlesBegin: * " + article);
			}
			// we try it again - commented out the return. marco.
//			return; // already releasing... not trying it again (or should we?)
		}

		if (!tas.allocated && !tas.allocationPending)
			return; // it's neither allocated nor allocationPending - there seems to be nothing to do.


		Map<ProductTypeActionHandler, List<Article>> productTypeActionHandler2Articles = new HashMap<ProductTypeActionHandler, List<Article>>();

//		JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
//		try {
//		Map<Offer, ProcessInstance> offer2ProcessInstance = new HashMap<Offer, ProcessInstance>();

		for (Article article : articles) {
			article.getArticleLocal().setAllocateReleaseExecID(releaseExecID);

			if (article.isReversing()) {
				// reversing article

				// If the reversing article is in a non-accepted Offer, it must not be released!
				if (!article.getOffer().getOfferLocal().isAccepted())
					throw new IllegalStateException("The Offer " + article.getOffer().getPrimaryKey() + " of reversing Article " + article.getPrimaryKey() + " has NOT been accepted!");


				// If the reversed article is in a DeliveryNote, both - reversed and reversing - articles must be in a DeliveryNote.
				// The DeliveryNotes must be booked!

				Article reversedArticle = article.getReversedArticle();
				if (reversedArticle.getDeliveryNote() != null) {
					if (article.getDeliveryNote() ==  null)
						throw new IllegalStateException("The reversing Article " + article.getPrimaryKey() + " is NOT in a DeliveryNote, but its corresponding reversed Article is! In this case, the reversing Article MUST be in a DeliveryNote, too!");

					if (!reversedArticle.getDeliveryNote().getDeliveryNoteLocal().isBooked())
						throw new IllegalStateException("The reversed Article " + reversedArticle.getPrimaryKey() + " is in a DeliveryNote, but it is NOT booked! The DeliveryNote must be booked!");

					if (!article.getDeliveryNote().getDeliveryNoteLocal().isBooked())
						throw new IllegalStateException("The reversing Article " + article.getPrimaryKey() + " is in a DeliveryNote, but it is NOT booked! The DeliveryNote must be booked!");
				}

			}
			else {
				// normal article (non-reversing)

				// check, whether the article is finalized
				if (article.getOffer().isFinalized()) {
					// it is finalized, so we only allow to release, if the offer's workflow has ended in a non-successful way (abort, reject, revoke etc.)
//					ProcessInstance processInstance = offer2ProcessInstance.get(article.getOffer());
//					if (processInstance == null) {
//					processInstance = jbpmContext.getProcessInstance(article.getOffer().getOfferLocal().getJbpmProcessInstanceId());
//					offer2ProcessInstance.put(article.getOffer(), processInstance);
//					}

//					if (!(processInstance.getRootToken().getNode() instanceof EndState)) // currently finishing
					State state = article.getOffer().getOfferLocal().getState();
					if (!state.getStateDefinition().isEndState())
						throw new IllegalStateException("Article \"" + article.getPrimaryKey() + "\" cannot be released, because its Offer is finalized and the Offer's workflow has not ended yet!");

					if (JbpmConstantsOffer.Vendor.NODE_NAME_ACCEPTED.equals(state.getStateDefinition().getJbpmNodeName()))
						throw new IllegalStateException("Article \"" + article.getPrimaryKey() + "\" cannot be released, because its Offer is finalized and the Offer's workflow has ended successfully!");
				}
			}

			Product product = article.getProduct();
			article.setReleasePending(true);
			article.setReleaseAbandoned(false);
			article.setReleaseException(null);
			product.getProductLocal().setReleasePending(true);

			ProductTypeActionHandler productTypeActionHandler = ProductTypeActionHandler.getProductTypeActionHandler(
					pm, article.getProductType().getClass()
			);
			List<Article> al = productTypeActionHandler2Articles.get(productTypeActionHandler);
			if (al == null) {
				al = new LinkedList<Article>();
				productTypeActionHandler2Articles.put(productTypeActionHandler, al);
			}
			al.add(article);
		}

//		} finally {
//		jbpmContext.close();
//		}

		if (logger.isTraceEnabled()) {
			logger.trace("releaseArticlesBegin: about to notify " + productTypeActionHandler2Articles.size() + " ProductTypeActionHandlers.");
		}

		for (Map.Entry<ProductTypeActionHandler, List<Article>> me : productTypeActionHandler2Articles.entrySet()) {
			me.getKey().onReleaseArticlesBegin(user, this, me.getValue());
		}

		if (logger.isTraceEnabled()) {
			logger.trace("releaseArticlesBegin: exit.");
		}
	}

	protected void releaseArticlesEnd(String releaseExecID, User user, Collection<Article> articles, boolean deleteAfterRelease)
	throws ModuleException
	{
		if (releaseExecID == null)
			throw new IllegalArgumentException("releaseExecID == null");

		if (logger.isDebugEnabled()) {
			logger.debug("releaseArticlesEnd: entered with " + articles.size() + " articles.");
			if (logger.isTraceEnabled()) {
				for (Article article : articles)
					logger.trace("releaseArticlesEnd: * " + article);
			}
		}

		// We remove all Articles that have a different allocate/release exec ID - thus we need our own local HashSet.
		articles = new HashSet<Article>(articles);

		PersistenceManager pm = getPersistenceManager();
		NLJDOHelper.enableTransactionSerializeReadObjects(pm);
		try {
			pm.refreshAll(articles);
			for (Iterator<Article> it = articles.iterator(); it.hasNext(); ) {
				Article article = it.next();
				pm.refresh(article.getArticleLocal());

				if (!releaseExecID.equals(article.getArticleLocal().getAllocateReleaseExecID())) {
					if (logger.isDebugEnabled())
						logger.debug("releaseArticlesEnd: Article.articleLocal.allocateReleaseExecID != releaseExecID :: " + article.getArticleLocal().getAllocateReleaseExecID() + " != " + releaseExecID + " :: Skipping " + article);

					it.remove();
					continue;
				}

				Product product = article.getProduct();
				if (product != null)
					pm.refresh(product.getProductLocal());
			}
		} finally {
			NLJDOHelper.disableTransactionSerializeReadObjects(pm);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("releaseArticlesEnd: refreshing articles done.");
//			if (logger.isTraceEnabled()) {
			for (Article article : articles)
				logger.trace("releaseArticlesEnd: * " + article);
//			}
		}

		if (articles.isEmpty())
			return;

// It's not necessary to check the status, because we already did all the checks in releaseArticlesBegin and the newly introduced
// allocateReleaseExecID guarantees that the state is preserved between the first and the second stage. Marco.
//		TotalArticleStatus tas = getTotalArticleStatus(articles);
//
//		if (!tas.allocated && !tas.allocationPending)
//			return;
//
//		if (!tas.releasePending)
//			throw new IllegalArgumentException("Articles " + getToStringList(articles) + " are NOT in state releasePending!");

		ProductTypeActionHandlerCache productTypeActionHandlerCache = new ProductTypeActionHandlerCache(pm);

		Map<ProductTypeActionHandler, List<Article>> productTypeActionHandler2Articles = new HashMap<ProductTypeActionHandler, List<Article>>();
		Set<Offer> offers = new HashSet<Offer>();
		for (Article article : articles) {
			offers.add(article.getOffer());
			Product product = article.getProduct();

			ProductTypeActionHandler productTypeActionHandler = productTypeActionHandlerCache.getProductTypeActionHandler(product);
			// delegate disassembling to the product
			productTypeActionHandler.disassembleProduct(user, productTypeActionHandlerCache, product, true);
//			product.disassemble(user, true);

			// clear product's article and update all states
			ProductLocal productLocal = product.getProductLocal();
			productLocal.setAllocated(false);
			productLocal.setAllocationPending(false);
			productLocal.setSaleArticle(null);
			article.setAllocated(false);
			article.setAllocationPending(false);
			article.setAllocationAbandoned(false);
			article.setAllocationException(null);
			article.setReleaseException(null);
			article.setReleasePending(false);
			article.setReleaseAbandoned(false);

//			ProductTypeActionHandler productTypeActionHandler = ProductTypeActionHandler.getProductTypeActionHandler(
//			getPersistenceManager(), article.getProductType().getClass());
			List<Article> al = productTypeActionHandler2Articles.get(productTypeActionHandler);
			if (al == null) {
				al = new LinkedList<Article>();
				productTypeActionHandler2Articles.put(productTypeActionHandler, al);
			}
			al.add(article);
			// article.product is NOT cleared, because it should be possible to easily
			// re-allocate
		}

		if (logger.isTraceEnabled()) {
			logger.trace("releaseArticlesEnd: about to notify " + productTypeActionHandler2Articles.size() + " ProductTypeActionHandlers.");
		}

		for (Map.Entry<ProductTypeActionHandler, List<Article>> me : productTypeActionHandler2Articles.entrySet()) {
			me.getKey().onReleaseArticlesEnd(user, this, me.getValue());
		}

		for (Article article : articles) {
			article.getArticleLocal().setAllocateReleaseExecID(null);
		}

		if (deleteAfterRelease) {
			deleteArticles(user, articles);
		}

		for (Offer offer : offers) {
			validateOffer(offer);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("releaseArticlesEnd: exit.");

			for (Article article : articles)
				logger.trace("releaseArticlesEnd: * " + article);
		}
	}

	protected static String getToStringList(Collection<?> objects)
	{
		StringBuffer sb = new StringBuffer();
		for (Iterator<?> iter = objects.iterator(); iter.hasNext();) {
			Object o = iter.next();
			sb.append(String.valueOf(o));
			if (iter.hasNext())
				sb.append(", ");
		}
		return sb.toString();
	}

	protected static class TotalArticleStatus
	{
		public boolean allocated = false;

		public boolean allocationPending = false;

		public boolean releasePending = false;
	}

	protected static TotalArticleStatus getTotalArticleStatus(Collection<? extends Article> articles)
	{
		if (articles.isEmpty())
			throw new IllegalArgumentException("articles is empty!");

		boolean first = true;
		TotalArticleStatus res = new TotalArticleStatus();
//		for (Iterator iter = articles.iterator(); iter.hasNext();) {
//		Article article = (Article) iter.next();
		for (Article article : articles) {
			if (first) {
				res.allocated = article.isAllocated();
				res.allocationPending = article.isAllocationPending();
				res.releasePending = article.isReleasePending();
				first = false;
			}
			else {
				if (res.allocated != article.isAllocated())
					throw new IllegalArgumentException("Article "
							+ article.getPrimaryKey()
							+ " has a different 'allocated' status than the others!");

				if (res.allocationPending != article.isAllocationPending())
					throw new IllegalArgumentException("Article "
							+ article.getPrimaryKey()
							+ " has a different 'allocationPending' status than the others!");

				if (res.releasePending != article.isReleasePending())
					throw new IllegalArgumentException("Article "
							+ article.getPrimaryKey()
							+ " has a different 'releasePending' status than the others!");
			}
		}
		return res;
	}

	/**
	 * If the given <tt>article</tt> is already allocated or allocationPending,
	 * this method returns silently without doing anything. If the given
	 * <tt>article</tt> cannot be allocated, a
	 * {@link org.nightlabs.jfire.store.NotAvailableException} will be thrown. A
	 * non-allocated <tt>Product</tt> might be "empty" (=not assembled), i.e.
	 * not have any nested products. Hence, it is necessary to assemble it during
	 * the allocation. That's why this method sets only the status
	 * <code>allocationPending</code> (via
	 * {@link Product#setAllocationPending(boolean)} and
	 * {@link Article#setAllocationPending(boolean)}) and the rest is done
	 * (asynchronously) by {@link #allocateArticleEnd(User, Article)}.
	 * <p>
	 * This method creates a top-level-{@link ArticlePrice} (it will be filled
	 * with nested details in {@link #allocateArticleEnd(User, Article)}).
	 * </p>
	 * @param allocateExecID TODO
	 * @param user
	 *          The user who is responsible for this allocation.
	 * @param article
	 *          The <code>Article</code> that shall be allocated. It must have
	 *          its <code>Product</code> assigned ({@link Article#getProduct()}
	 *          must not return <code>null</code>).
	 *
	 * @throws org.nightlabs.jfire.store.NotAvailableException
	 *           If the product (or a packaged product) cannot be allocated.
	 * @throws ModuleException
	 *           If another error occurs.
	 * @throws NotAvailableException If articles could not be allocated
	 */
	protected void allocateArticlesBegin(String allocateExecID, User user, Collection<? extends Article> articles)
	throws NotAvailableException
	{
		if (allocateExecID == null)
			throw new IllegalArgumentException("allocateExecID == null");

		if (logger.isDebugEnabled()) {
			logger.debug("allocateArticlesBegin: entered with " + articles.size() + " articles.");
			if (logger.isTraceEnabled()) {
				for (Article article : articles)
					logger.trace("allocateArticlesBegin: * " + article);
			}
		}

		PersistenceManager pm = getPersistenceManager();
		pm.flush(); // the refreshAll sometimes fails, because it seems the articles have not yet been written to the datastore (in situations in which they are freshly created)
		NLJDOHelper.enableTransactionSerializeReadObjects(pm);
		try {
			pm.refreshAll(articles);
			for (Article article : articles) {
				pm.refresh(article.getArticleLocal());
				pm.refresh(article.getProduct().getProductLocal());
			}
		} finally {
			NLJDOHelper.disableTransactionSerializeReadObjects(pm);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("allocateArticlesBegin: refreshing articles done.");
		}

		// If all articles are currently allocated or the allocation is pending, we silently return.
		// If the status is different between them, we throw an IllegalArgumentException.
		TotalArticleStatus tas = getTotalArticleStatus(articles);

		if (tas.allocated || tas.allocationPending)
			return;

		if (tas.releasePending)
			throw new IllegalStateException("Articles \"" + getToStringList(articles) + "\" cannot be allocated, because it is currently in state releasePending!");

		Map<Class<? extends ProductType>, ProductTypeActionHandler> productTypeClass2ProductTypeActionHandler = new HashMap<Class<? extends ProductType>, ProductTypeActionHandler>();
		for (Article article : articles) {
			Class<? extends ProductType> ptClazz = article.getProductType().getClass();
			if (productTypeClass2ProductTypeActionHandler.containsKey(ptClazz))
				continue;

			productTypeClass2ProductTypeActionHandler.put(
					ptClazz,
					ProductTypeActionHandler.getProductTypeActionHandler(pm, ptClazz)
			);
		}

		Map<ProductTypeActionHandler, List<Article>> productTypeActionHandler2Articles = new HashMap<ProductTypeActionHandler, List<Article>>();
		for (Article article : articles) {
			article.getArticleLocal().setAllocateReleaseExecID(allocateExecID);
			Product product = article.getProduct();
			if (product == null)
				throw new IllegalStateException("Article '" + article.getPrimaryKey() + "' does not have a product!");

			ProductLocal productLocal = product.getProductLocal();

			if (productLocal.getSaleArticle() != null)
				throw new NotAvailableException(
						"The article '"
						+ article.getPrimaryKey()
						+ "' with product '"
						+ product.getPrimaryKey()
						+ "' cannot be allocated, because the product is already allocated in article '"
						+ productLocal.getSaleArticle().getPrimaryKey() + "'!");

			if (!article.getOrganisationID().equals(product.getOrganisationID()))
				throw new IllegalStateException("The article '"
						+ article.getPrimaryKey()
						+ "' has a different organisationID than the product '"
						+ product.getPrimaryKey() + "'!");

			productLocal.setSaleArticle(article);
			productLocal.setAllocationPending(true);
			article.setAllocationPending(true);
			article.setAllocationAbandoned(false);
			article.setAllocationException(null);
			IPackagePriceConfig packagePriceConfig = product.getProductType().getPackagePriceConfig();

//			pm.flush();

//			// TODO remove this JPOX Workaround - getting:
//			// com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException: Duplicate entry 'chezfrancois.jfire.org-9-8' for key 1
			ArticlePrice articlePrice = packagePriceConfig.createArticlePrice(article);
//			for (int tryCounter = 0; tryCounter < 10; ++tryCounter) {
//			try {
//			articlePrice = pm.makePersistent(articlePrice);
//			break;
//			} catch (Exception x) {
//			logger.warn("Persisting articlePrice instance failed! Trying it again. tryCounter=" + tryCounter, x);
//			}
//			}
			article.setPrice(articlePrice);

//			pm.flush();

//			ProductTypeActionHandler productTypeActionHandler = ProductTypeActionHandler.getProductTypeActionHandler(
//			getPersistenceManager(), article.getProductType().getClass());
			ProductTypeActionHandler productTypeActionHandler = productTypeClass2ProductTypeActionHandler.get(article.getProductType().getClass());
			List<Article> al = productTypeActionHandler2Articles.get(productTypeActionHandler);
			if (al == null) {
				al = new LinkedList<Article>();
				productTypeActionHandler2Articles.put(productTypeActionHandler, al);
			}
			al.add(article);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("allocateArticlesBegin: about to notify " + productTypeActionHandler2Articles.size() + " ProductTypeActionHandlers.");
		}

//		getPersistenceManager().flush(); // TODO is this necessary? JPOX Bug
		for (Map.Entry<ProductTypeActionHandler, List<Article>> me : productTypeActionHandler2Articles.entrySet()) {
			(me.getKey()).onAllocateArticlesBegin(user, this, me.getValue());
		}

		if (logger.isTraceEnabled()) {
			logger.trace("allocateArticlesBegin: exit.");
		}
	}

	/**
	 * This method performs the second step of allocation: After
	 * {@link #allocateArticleBegin(User, Article)} has set the
	 * <code>Article</code> and the corresponding <code>ProductLocal</code> to
	 * status <code>allocationPending</code>, this method assembles the
	 * ProductLocal recursively.
	 * @param allocateExecID TODO
	 * @param user
	 *          The user who is responsible for this allocation.
	 * @param article
	 *          The <code>Article</code> that shall be allocated. It must have
	 *          its <code>Product</code> assigned ({@link Article#getProduct()}
	 *          must not return <code>null</code>).
	 *
	 */
	protected void allocateArticlesEnd(String allocateExecID, User user, Collection<? extends Article> articles)
	throws NotAvailableException
	{
		if (allocateExecID == null)
			throw new IllegalArgumentException("allocateExecID == null");

		if (logger.isDebugEnabled()) {
			logger.debug("allocateArticlesEnd: entered with " + articles.size() + " articles.");
			if (logger.isTraceEnabled()) {
				for (Article article : articles)
					logger.trace("allocateArticlesEnd: * " + article);
			}
		}

		// We remove all Articles that have a different allocate/release exec ID - thus we need our own local HashSet.
		articles = new HashSet<Article>(articles);

		PersistenceManager pm = getPersistenceManager();
		NLJDOHelper.enableTransactionSerializeReadObjects(pm);
		try {
			pm.refreshAll(articles);
			for (Iterator<? extends Article> it = articles.iterator(); it.hasNext(); ) {
				Article article = it.next();
				pm.refresh(article.getArticleLocal());

				if (!allocateExecID.equals(article.getArticleLocal().getAllocateReleaseExecID())) {
					if (logger.isDebugEnabled())
						logger.debug("allocateArticlesEnd: Article.articleLocal.allocateReleaseExecID != allocateExecID :: " + article.getArticleLocal().getAllocateReleaseExecID() + " != " + allocateExecID + " :: Skipping " + article);

					it.remove();
					continue;
				}

				Product product = article.getProduct();
				if (product != null)
					pm.refresh(product.getProductLocal());
			}
		} finally {
			NLJDOHelper.disableTransactionSerializeReadObjects(pm);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("allocateArticlesEnd: refreshing articles done.");
		}

		if (articles.isEmpty())
			return;

// It's not necessary to check the status, because we already did all the checks in releaseArticlesBegin and the newly introduced
// allocateReleaseExecID guarantees that the state is preserved between the first and the second stage. Marco.
//		TotalArticleStatus tas = getTotalArticleStatus(articles);
//		if (tas.allocated)
//			return;
//
//		if (!tas.allocationPending)
//			throw new IllegalArgumentException("Articles " + getToStringList(articles) + " are NOT in state allocationPending!");

		ProductTypeActionHandlerCache productTypeActionHandlerCache = new ProductTypeActionHandlerCache(pm);

		Map<ProductTypeActionHandler, List<Article>> productTypeActionHandler2Articles = new HashMap<ProductTypeActionHandler, List<Article>>();
		for (Article article : articles) {
			Product product = article.getProduct();

			ProductTypeActionHandler productTypeActionHandler = productTypeActionHandlerCache.getProductTypeActionHandler(product);
			// delegate assembling to the product (give it a chance to intercept)
			productTypeActionHandler.assembleProduct(user, productTypeActionHandlerCache, product);

			IPackagePriceConfig packagePriceConfig = product.getProductType()
			.getPackagePriceConfig();
			packagePriceConfig.fillArticlePrice(article);

			product.getProductLocal().setAllocated(true);
			product.getProductLocal().setReleasePending(false);
			article.setAllocated(true);
			article.setAllocationException(null);
			article.setAllocationPending(false);
			article.setAllocationAbandoned(false);
			article.setReleasePending(false);
			article.setReleaseAbandoned(false);
			article.setReleaseException(null);

			List<Article> al = productTypeActionHandler2Articles.get(productTypeActionHandler);
			if (al == null) {
				al = new LinkedList<Article>();
				productTypeActionHandler2Articles.put(productTypeActionHandler, al);
			}
			al.add(article);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("allocateArticlesEnd: about to notify " + productTypeActionHandler2Articles.size() + " ProductTypeActionHandlers.");
		}

		for (Map.Entry<ProductTypeActionHandler, List<Article>> me : productTypeActionHandler2Articles.entrySet()) {
			(me.getKey()).onAllocateArticlesEnd(user, this, me.getValue());
		}

		for (Article article : articles) {
			article.getArticleLocal().setAllocateReleaseExecID(null);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("allocateArticlesEnd: exit.");
		}
	}

	public void validateOffer(Offer offer, boolean force)
	{
		if (force)
			offer.setValid(false);
		else {
			if (offer.isValid())
				return;
		}

		offer.validate();
	}

	public void validateOffer(Offer offer)
	{
		validateOffer(offer, false);
	}

//	has been moved into the ActionHandler
//	/**
//	* This method must not be called directly. It's triggered by jBPM via the {@link ActionHandlerFinalizeOffer}
//	*/
//	public void onFinalizeOffer(User user, Offer offer)
//	{
//	if (offer.isFinalized())
//	return;

//	offer.setFinalized(user);
//	for (OfferActionHandler offerActionHandler : offer.getOfferLocal().getOfferActionHandlers()) {
//	offerActionHandler.onFinalizeOffer(user, offer);
//	}
//	}

	/**
	 * This method is a noop, if the offer is already accepted. If the Offer cannot be accepted implicitely
	 * (either because the business partner doesn't allow implicit acceptance or because the jBPM token is at
	 * a position where this is not possible, an exception is thrown).
	 */
	public void acceptOfferImplicitely(Offer offer)
	{
		OfferID offerID = (OfferID) JDOHelper.getObjectId(offer);
		if (State.hasState(getPersistenceManager(), offerID, JbpmConstantsOffer.Vendor.NODE_NAME_ACCEPTED))
			return;

		JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
		try {
			ProcessInstance processInstance = jbpmContext.getProcessInstance(offer.getOfferLocal().getJbpmProcessInstanceId());
			processInstance.signal(JbpmConstantsOffer.Vendor.TRANSITION_NAME_ACCEPT_IMPLICITELY);
		} finally {
			jbpmContext.close();
		}
	}

	/**
	 * You must NOT call this method directly. It is called by {@link ActionHandlerAcceptOffer}.
	 */
	public void onAcceptOffer(User user, Offer offer)
	throws RemoteException, CreateException, NamingException
	{
		boolean error = true;
		try {
			// check whether we have to finalize remote offers as well
			PersistenceManager pm = getPersistenceManager();
			OfferRequirement offerRequirement = OfferRequirement.getOfferRequirement(pm, offer, false);
			if (offerRequirement != null) {
				for (Offer partnerOffer : offerRequirement.getPartnerOffers()) {
					LegalEntity vendor = partnerOffer.getOrder().getVendor();
					if (!(vendor instanceof OrganisationLegalEntity))
						throw new IllegalStateException("Vendor of Offer " + partnerOffer.getPrimaryKey() + " is not an OrganisationLegalEntity, even though this Offer is part of the OfferRequirements for Offer " + offer.getPrimaryKey());

					String partnerOrganisationID = vendor.getOrganisationID();

					TradeManager tradeManager = JFireEjbFactory.getBean(TradeManager.class, Lookup.getInitialContextProperties(pm, partnerOrganisationID));
					tradeManager.signalOffer((OfferID) JDOHelper.getObjectId(partnerOffer), JbpmConstantsOffer.Vendor.TRANSITION_NAME_ACCEPT_FOR_CROSS_TRADE);
					// TODO we have to do sth. with the local workflow!
				} // for (Iterator itO = offerRequirement.getPartnerOffers().iterator(); itO.hasNext(); ) {
			} // if (offerRequirement != null) {

			offer.getOfferLocal().accept(user);
			for (OfferActionHandler offerActionHandler : offer.getOfferLocal().getOfferActionHandlers()) {
				offerActionHandler.onAcceptOffer(user, offer);
			}

			error = false;
		} finally {
			if (error)
				logger.error("onAcceptOffer: failed for: " + offer);
		}
	}

	/**
	 * You must NOT call this method directly. It is called by {@link ActionHandlerFinalizeOffer}.
	 */
	public void onFinalizeOffer(User user, Offer offer)
	throws RemoteException, NamingException
	{
		boolean error = true;
		try {
			PersistenceManager pm = getPersistenceManager();

			if (offer.isFinalized()) // we (might) come here multiple times => do work only first time.
				return;

			// check whether we have to finalize remote offers as well
			OfferRequirement offerRequirement = OfferRequirement.getOfferRequirement(pm, offer, false);
			if (offerRequirement != null) {
				for (Offer partnerOffer : offerRequirement.getPartnerOffers()) {
					LegalEntity vendor = partnerOffer.getOrder().getVendor();
					if (!(vendor instanceof OrganisationLegalEntity))
						throw new IllegalStateException("Vendor of Offer " + partnerOffer.getPrimaryKey() + " is not an OrganisationLegalEntity, even though this Offer is part of the OfferRequirements for Offer " + offer.getPrimaryKey());

					String partnerOrganisationID = vendor.getOrganisationID();

					TradeManager tradeManager = JFireEjbFactory.getBean(TradeManager.class, Lookup.getInitialContextProperties(pm, partnerOrganisationID));
					tradeManager.signalOffer((OfferID) JDOHelper.getObjectId(partnerOffer), JbpmConstantsOffer.Vendor.TRANSITION_NAME_FINALIZE_FOR_CROSS_TRADE);
					// TODO we have to do sth. with the local workflow!
				} // for (Iterator itO = offerRequirement.getPartnerOffers().iterator(); itO.hasNext(); ) {
			} // if (offerRequirement != null) {

			// set expiry timestamp
			setOfferExpiry(offer);

			offer.setFinalized(user);

			replicateEndCustomer(offer);

			// trigger listeners
			for (OfferActionHandler offerActionHandler : offer.getOfferLocal().getOfferActionHandlers()) {
				offerActionHandler.onFinalizeOffer(user, offer);
			}

			error = false;
		} finally {
			if (error)
				logger.error("onFinalizeOffer: failed for: " + offer);
		}
	}

	public void replicateEndCustomer(Offer offer)
	throws NamingException, RemoteException
	{
		PersistenceManager pm = getPersistenceManager();

		LegalEntity endCustomer = offer.getEndCustomer();
		if (endCustomer == null)
			endCustomer = offer.getCustomer();

		Map<OrganisationLegalEntity, Set<EndCustomerReplicationPolicy>> vendor2EndCustomerReplicationPoliciesMap = new HashMap<OrganisationLegalEntity, Set<EndCustomerReplicationPolicy>>();
		Map<OrganisationLegalEntity, Set<Order>> vendor2OrdersMap = new HashMap<OrganisationLegalEntity, Set<Order>>();

		OfferRequirement offerRequirement = OfferRequirement.getOfferRequirement(pm, offer, false);
		if (offerRequirement != null) {
			for (Offer partnerOffer : offerRequirement.getPartnerOffers()) {
				LegalEntity v = partnerOffer.getOrder().getVendor();
				if (!(v instanceof OrganisationLegalEntity))
					throw new IllegalStateException("Vendor of Offer " + partnerOffer.getPrimaryKey() + " is not an OrganisationLegalEntity, even though this Offer is part of the OfferRequirements for Offer " + offer.getPrimaryKey());

				OrganisationLegalEntity vendor = (OrganisationLegalEntity) v;

				for (Article partnerArticle : partnerOffer.getArticles()) {
					ProductType productType = partnerArticle.getProductType();
					EndCustomerReplicationPolicy endCustomerReplicationPolicy = productType.getEndCustomerReplicationPolicy();
					if (endCustomerReplicationPolicy != null) {
						Set<EndCustomerReplicationPolicy> endCustomerReplicationPolicies = vendor2EndCustomerReplicationPoliciesMap.get(vendor);
						if (endCustomerReplicationPolicies == null) {
							endCustomerReplicationPolicies = new HashSet<EndCustomerReplicationPolicy>();
							vendor2EndCustomerReplicationPoliciesMap.put(vendor, endCustomerReplicationPolicies);
						}
						endCustomerReplicationPolicies.add(endCustomerReplicationPolicy);


						Set<Order> orders = vendor2OrdersMap.get(vendor);
						if (orders == null) {
							orders = new HashSet<Order>();
							vendor2OrdersMap.put(vendor, orders);
						}
						orders.add(partnerOffer.getOrder());
					}
				}
			}

			for (Map.Entry<OrganisationLegalEntity, Set<EndCustomerReplicationPolicy>> me1 : vendor2EndCustomerReplicationPoliciesMap.entrySet()) {
				OrganisationLegalEntity vendor = me1.getKey();
				Set<EndCustomerReplicationPolicy> endCustomerReplicationPolicies = me1.getValue();
				Set<Order> orders = vendor2OrdersMap.get(vendor);
				if (orders == null)
					throw new IllegalStateException("vendor2OrdersMap.get(vendor) returned null! " + vendor);

				LegalEntity detachedEndCustomer = EndCustomerReplicationPolicy.detachLegalEntity(pm, endCustomer, endCustomerReplicationPolicies);

				String partnerOrganisationID = vendor.getOrganisationID();
				TradeManager tradeManager = JFireEjbFactory.getBean(TradeManager.class, Lookup.getInitialContextProperties(pm, partnerOrganisationID));

				Set<OrderID> orderIDs = NLJDOHelper.getObjectIDSet(orders);
				tradeManager.storeEndCustomer(detachedEndCustomer, orderIDs);
			}
		} // if (offerRequirement != null) {
	}

	public void rejectOffer(User user, OfferLocal offerLocal)
	{
		offerLocal.reject(user);
	}
	/**
	 * This is a convenience method which calls {@link #rejectOffer(User, OfferLocal)}.
	 */
	public void rejectOffer(User user, Offer offer)
	{
		rejectOffer(user, offer.getOfferLocal());
	}

//	public void confirmOffer(User user, OfferLocal offerLocal)
//	{
//	offerLocal.confirm(user);
//	}
//	/**
//	* This is a convenience method which calls {@link #confirmOffer(User, OfferLocal)}.
//	*/
//	public void confirmOffer(User user, Offer offer)
//	{
//	confirmOffer(user, offer.getOfferLocal());
//	}

	/**
	 * The {@link Article}s must already be released!
	 *
	 * @param user
	 *          The user who is responsible.
	 * @param articles
	 *          Instances of {@link Article}.
	 */
	public void deleteArticles(User user, Collection<? extends Article> articles)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("deleteArticles: entered with " + articles.size() + " articles.");
			if (logger.isTraceEnabled()) {
				for (Article article : articles)
					logger.trace("deleteArticles: * " + article);
			}
		}

		for (Article article : articles) {
			Offer offer = article.getOffer();
			offer.removeArticle(article);
		}
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException(
			"This instance of Trader is currently not attached to a datastore! Cannot get a PersistenceManager!");

		return pm;
	}

	private static void setStateDefinitionProperties(
			ProcessDefinition processDefinition, String jbpmNodeName,
			String name, String description, boolean publicState)
	{
		StateDefinition stateDefinition = StateDefinition.getStateDefinition(processDefinition, jbpmNodeName);
		if (stateDefinition == null) {
			logger.warn("The ProcessDefinition \"" + processDefinition.getJbpmProcessDefinitionName() + "\" does not contain a jBPM Node named \"" + jbpmNodeName + "\"!");
			return;
		}
		stateDefinition.getName().setText(Locale.ENGLISH.getLanguage(), name);
		stateDefinition.getDescription().setText(Locale.ENGLISH.getLanguage(), description);
		stateDefinition.setPublicState(publicState);
	}


	public ProcessDefinition storeProcessDefinitionOffer(TradeSide tradeSide, URL jbpmProcessDefinitionURL)
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();
		org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition = ProcessDefinition.readProcessDefinition(jbpmProcessDefinitionURL);
// all the actions are defined and registered now in the process definition extension file 		
//		// we add the events+actionhandlers
//		ActionHandlerNodeEnter.register(jbpmProcessDefinition);
//		if (TradeSide.vendor == tradeSide || TradeSide.customerLocal == tradeSide) {
//			ActionHandlerFinalizeOffer.register(jbpmProcessDefinition);
//			ActionHandlerSendOffer.register(jbpmProcessDefinition);
//			ActionHandlerAcceptOffer.register(jbpmProcessDefinition);
//			ActionHandlerRejectOffer.register(jbpmProcessDefinition);
//			ActionHandlerAcceptOfferImplicitelyVendor.register(jbpmProcessDefinition);
//		}
//		if (TradeSide.vendor == tradeSide) {
//			ActionHandlerFinalizeOfferForCrossTrade.register(jbpmProcessDefinition);
//		}
		// store it and return it, most properties of config are written in the XML file now.
		return ProcessDefinition.storeProcessDefinition(pm, null, jbpmProcessDefinition, jbpmProcessDefinitionURL);
	}

	public void assignTariff(User user, Set<? extends Article> articles, Tariff tariff)
	{
		Map<Offer, Set<Article>> offers = new HashMap<Offer, Set<Article>>();
		for (Article article : articles) {
			if (article.getOffer().isFinalized())
				throw new IllegalArgumentException("Article's offer is finalized! article=" + article.getPrimaryKey() + " offer=" + article.getOffer().getPrimaryKey());

			if (article.isAllocationPending())
				throw new IllegalArgumentException("Article is currently in state 'allocationPending'! Wait until allocation finished. article=" + article.getPrimaryKey());

			if (article.isAllocationAbandoned())
				throw new IllegalArgumentException("Article is currently in state 'allocationAbandoned'! Check whether there was an error and fix it! article=" + article.getPrimaryKey());

			if (article.isReleasePending())
				throw new IllegalArgumentException("Article is currently in state 'releasePending'! Wait until release finished. article=" + article.getPrimaryKey());

			if (article.isReleaseAbandoned())
				throw new IllegalArgumentException("Article is currently in state 'releaseAbandoned'! Check whether there was an error and fix it! article=" + article.getPrimaryKey());

			if (tariff.equals(article.getTariff()))
				continue; // already the same tariff assigned => nothing to do

			// the involved offers need to be validated later
			Set<Article> offerArticles = offers.get(article.getOffer());
			if (offerArticles == null) {
				offerArticles = new HashSet<Article>();
				offers.put(article.getOffer(), offerArticles);
			}
			offerArticles.add(article);

			// assign tariff
			article.setTariff(tariff);

			// recalculate price
			IPackagePriceConfig packagePriceConfig = article.getProductType().getPackagePriceConfig();
			article.setPrice(packagePriceConfig.createArticlePrice(article));
			packagePriceConfig.fillArticlePrice(article);
		}

		for (Map.Entry<Offer, Set<Article>> me : offers.entrySet()) {
			Offer offer = me.getKey();
			validateOffer(offer, true);
			for (OfferActionHandler offerActionHandler : offer.getOfferLocal().getOfferActionHandlers())
				offerActionHandler.onArticlesTariffChanged(user, offer, me.getValue());
		}
	}

	/**
	 * Returns the {@link ProductHistory} for the given {@link Product}.
	 *
	 * @param product the Product to obtain an {@link ProductHistory} for
	 * @return the {@link ProductHistory} for the given {@link Product}.
	 */
	public ProductHistory getProductHistory(Product product)
	{
		if (product == null)
			return null;

		ProductHistory productHistory = new ProductHistory((ProductID)JDOHelper.getObjectId(product));

		PersistenceManager pm = getPersistenceManager();
		// get all articles for the product
		Set<Article> articles = Article.getArticles(pm, product);
		if (articles != null && !articles.isEmpty())
		{
			Set<Offer> offers = new HashSet<Offer>();
			Set<Order> orders = new HashSet<Order>();
			Set<Invoice> invoices = new HashSet<Invoice>();
			Set<DeliveryNote> deliveryNotes = new HashSet<DeliveryNote>();

			// collect all articleContainers for all articles
			for (Article article : articles)
			{
				if (article.getOffer() != null) {
					offers.add(article.getOffer());
				}
				if (article.getOrder() != null) {
					orders.add(article.getOrder());
				}
				if (article.getInvoice() != null) {
					invoices.add(article.getInvoice());
				}
				if (article.getDeliveryNote() != null) {
					deliveryNotes.add(article.getDeliveryNote());
				}
			}

			// check offers
			for (Offer offer : offers) {
				List<State> states = offer.getStates();
				for (State state : states) {
					String nodeName = state.getStateDefinition().getJbpmNodeName();
					if (logger.isDebugEnabled()) {
						logger.debug("NodeName = "+nodeName+" for state "+state+" of offer "+offer);
					}
					ProductHistoryItem productHistoryItem = new ProductHistoryItem(
							offer.getCreateUser(),
							state.getStateDefinition().getName().getText(),
							state.getStateDefinition().getDescription().getText(),
							offer,
							offer.getCustomer(),
							null,
							null,
							offer.getCreateDT(),
							ProductHistoryItemType.OFFER);
					productHistory.addProductHistoryItem(productHistoryItem);
				}
			}

			// check invoices
			for (Invoice invoice : invoices) {
				List<State> states = invoice.getStates();
				for (State state : states) {
					String nodeName = state.getStateDefinition().getJbpmNodeName();
					if (logger.isDebugEnabled()) {
						logger.debug("NodeName = "+nodeName+" for state "+state+" of invoice "+invoice);
					}
					ProductHistoryItem productHistoryItem = new ProductHistoryItem(
							invoice.getCreateUser(),
							state.getStateDefinition().getName().getText(),
							state.getStateDefinition().getDescription().getText(),
							invoice,
							invoice.getCustomer(),
							null,
							null,
							invoice.getCreateDT(),
							ProductHistoryItemType.INVOICE);
					productHistory.addProductHistoryItem(productHistoryItem);
				}

				Collection<Payment> payments = Payment.getPaymentsForInvoice(pm, invoice);
				for (Payment payment : payments) {
					ProductHistoryItem productHistoryItem = new ProductHistoryItem(
							payment.getUser(),
							NumberFormatter.formatCurrency(payment.getAmount(), payment.getCurrency()),
							payment.getReasonForPayment(),
							invoice,
							payment.getPartner(),
							null,
							payment.getModeOfPaymentFlavour(),
							payment.getBeginDT(),
							ProductHistoryItemType.PAYMENT);
					productHistory.addProductHistoryItem(productHistoryItem);
				}
			}

			// check delivery notes
			for (DeliveryNote deliveryNote : deliveryNotes) {
				List<State> states = deliveryNote.getStates();
				for (State state : states) {
					String nodeName = state.getStateDefinition().getJbpmNodeName();
					if (logger.isDebugEnabled()) {
						logger.debug("NodeName = "+nodeName+" for state "+state+" of deliveryNote "+deliveryNote);
					}
					ProductHistoryItem productHistoryItem = new ProductHistoryItem(
							deliveryNote.getCreateUser(),
							state.getStateDefinition().getName().getText(),
							state.getStateDefinition().getDescription().getText(),
							deliveryNote,
							deliveryNote.getCustomer(),
							null,
							null,
							deliveryNote.getCreateDT(),
							ProductHistoryItemType.DELIVERY_NOTE);
					productHistory.addProductHistoryItem(productHistoryItem);
				}
				Collection<Delivery> deliveries = Delivery.getDeliveriesForDeliveryNote(pm, deliveryNote);
				for (Delivery delivery : deliveries) {
					ProductHistoryItem productHistoryItem = new ProductHistoryItem(
							delivery.getUser(),
							"Delivery",
							"",
							deliveryNote,
							delivery.getPartner(),
							delivery.getModeOfDeliveryFlavour(),
							null,
							delivery.getBeginDT(),
							ProductHistoryItemType.DELIVERY);
					productHistory.addProductHistoryItem(productHistoryItem);
				}
			}

		}
		return productHistory;
	}


	/**
	 * Signal a given Jbpm transition to the offer.
	 */
	public void signalOffer(OfferID offerID, String jbpmTransitionName)
	{
		PersistenceManager pm = getPersistenceManager();

		OfferLocal offerLocal = (OfferLocal) pm.getObjectById(OfferLocalID.create(offerID));
		JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();

		try{
			ProcessInstance processInstance = jbpmContext.getProcessInstanceForUpdate(offerLocal.getJbpmProcessInstanceId());
			processInstance.signal(jbpmTransitionName);
		}
		finally {
			jbpmContext.close();
		}
	}

//	public Collection<? extends Article> onProductAssemble_importNestedProduct(User user, Product packageProduct, String partnerOrganisationID, Collection<NestedProductTypeLocal> partnerNestedProductTypes)
//	{
//	try {
//	PersistenceManager pm = getPersistenceManager();
//	Article localArticle = packageProduct.getProductLocal().getArticle();
//	Order localOrder = localArticle.getOrder();
//	Offer localOffer = localArticle.getOffer();
//	SegmentType segmentType = localArticle.getSegment().getSegmentType();

//	Set segmentTypeIDsWithTheCurrentInstanceOnly = new HashSet();
//	segmentTypeIDsWithTheCurrentInstanceOnly.add(JDOHelper.getObjectId(segmentType));

//	OrganisationLegalEntity partner = OrganisationLegalEntity.getOrganisationLegalEntity(pm, partnerOrganisationID, OrganisationLegalEntity.ANCHOR_TYPE_ID_ORGANISATION, true);

//	Hashtable initialContextProperties = Lookup.getInitialContextProperties(pm, partnerOrganisationID);
//	TradeManager tradeManager = JFireEjbFactory.getBean(TradeManager.class, initialContextProperties);

////	Set segmentTypeIDs = Segment.getSegmentTypeIDs(pm, localOrder);

//	// for the current order, we create/find an instance of OrderRequirement
//	OrderRequirement orderRequirement = OrderRequirement.getOrderRequirement(pm, localOrder);
//	Order partnerOrder = orderRequirement.getPartnerOrder(partner);
//	OrderID partnerOrderID;
//	SegmentID partnerSegmentID;
//	if (partnerOrder == null) {
//	Order order = tradeManager.createCrossTradeOrder(null, // TODO should we somehow configure the orderIDPrefix on this side? I don't think so. Marco.
//	localOrder.getCurrency().getCurrencyID(),
//	null, // TODO we should find out and pass the CustomerGroupID
////	segmentTypeIDs);
//	segmentTypeIDsWithTheCurrentInstanceOnly);
//	partnerOrder = (Order) pm.makePersistent(order);
//	orderRequirement.addPartnerOrder(partnerOrder);
//	partnerOrderID = (OrderID) JDOHelper.getObjectId(partnerOrder);
//	partnerSegmentID = Segment.getSegmentIDs(pm, partnerOrder, segmentType).iterator().next();
//	}
//	else {
//	partnerOrderID = (OrderID) JDOHelper.getObjectId(partnerOrder);
//	Set segmentIDs = Segment.getSegmentIDs(pm, partnerOrder, segmentType);
//	if (segmentIDs.isEmpty()) {
//	Collection segments = tradeManager.createCrossTradeSegments(partnerOrderID, segmentTypeIDsWithTheCurrentInstanceOnly);
//	segments = pm.makePersistentAll(segments);
//	segmentIDs = NLJDOHelper.getObjectIDSet(segments);
//	}
//	partnerSegmentID = (SegmentID) segmentIDs.iterator().next();
////	Set partnerSegmentTypeIDs = Segment.getSegmentTypeIDs(pm, partnerOrder);
////	if (!segmentTypeIDs.equals(partnerSegmentTypeIDs))
////	tradeManager.createCrossTradeSegments(partnerOrderID, segmentTypeIDs);
//	}

//	// for the current offer, we create/find an instance of OfferRequirement
//	OfferRequirement offerRequirement = OfferRequirement.getOfferRequirement(pm, localOffer);
//	Offer partnerOffer = offerRequirement.getPartnerOffer(partner);
//	if (partnerOffer == null) {
//	{
//	Offer offer = tradeManager.createCrossTradeOffer(partnerOrderID, null); // we don't pass the offerIDPrefix - or should we?
//	new OfferLocal(offer);
//	partnerOffer = (Offer) pm.makePersistent(offer);
//	offerRequirement.addPartnerOffer(partnerOffer);
//	}

//	ProcessDefinitionAssignment processDefinitionAssignment = (ProcessDefinitionAssignment) getPersistenceManager().getObjectById(
//	ProcessDefinitionAssignmentID.create(Offer.class, TradeSide.customer));
//	processDefinitionAssignment.createProcessInstance(null, user, partnerOffer);
//	}
//	OfferID partnerOfferID = (OfferID) JDOHelper.getObjectId(partnerOffer);

////	ProductTypeID[] productTypeIDs = new ProductTypeID[partnerNestedProductTypes.size()];
////	int[] quantities = new int[partnerNestedProductTypes.size()];
////	ProductLocator[] productLocators = new ProductLocator[partnerNestedProductTypes.size()];

////	int idx = 0;
////	for (NestedProductTypeLocal partnerNestedProductType : partnerNestedProductTypes) {
////	ProductLocator productLocator = packageProduct.getProductLocator(user, partnerNestedProductType);
////	productTypeIDs[idx] = (ProductTypeID) JDOHelper.getObjectId(partnerNestedProductType.getInnerProductType());
////	quantities[idx] = partnerNestedProductType.getQuantity();
////	productLocators[idx] = productLocator;
////	++idx;
////	}

////	Map<Integer, Collection<? extends Article>> articleMap = tradeManager.createCrossTradeArticles(partnerOfferID, productTypeIDs, quantities, productLocators);

//	Map productType2NestedProductTypes = new HashMap();
//	for (NestedProductTypeLocal partnerNestedProductType : partnerNestedProductTypes) {
//	Collection nestedProductTypes = (Collection) productType2NestedProductTypes.get(partnerNestedProductType.getInnerProductType());
//	if (nestedProductTypes == null) {
//	nestedProductTypes = new ArrayList();
//	productType2NestedProductTypes.put(partnerNestedProductType.getInnerProductType(), nestedProductTypes);
//	}
//	nestedProductTypes.add(partnerNestedProductType);
//	}

//	Collection resultArticles = null;

//	for (Iterator itME = productType2NestedProductTypes.entrySet().iterator(); itME.hasNext();) {
//	Map.Entry me = (Map.Entry) itME.next();
//	ProductType productType = (ProductType) me.getKey();
//	Collection nestedProductTypes = (Collection) me.getValue();
//	ProductTypeActionHandler productTypeActionHandler = ProductTypeActionHandler.getProductTypeActionHandler(pm, productType.getClass());

//	Collection articles = productTypeActionHandler.createCrossTradeArticles(
//	user, packageProduct, localArticle,
//	partnerOrganisationID, initialContextProperties,
//	partnerOffer, partnerOfferID, partnerSegmentID,
//	productType, nestedProductTypes);

//	articles = pm.makePersistentAll(articles);

//	if (resultArticles == null)
//	resultArticles = new ArrayList(articles);
//	else
//	resultArticles.addAll(articles);

//	for (Iterator itA = articles.iterator(); itA.hasNext();) {
//	Article article = (Article) itA.next();
//	article.createArticleLocal(user);

//	ProductType articleProductType = article.getProductType();
//	if (articleProductType == null)
//	throw new IllegalStateException("article.getProductType() == null for imported Article: " + article.getPrimaryKey());

//	ProductTypeLocal articleProductTypeLocal = articleProductType.getProductTypeLocal();
//	if (articleProductTypeLocal == null)
//	throw new IllegalStateException("article.getProductType().getProductTypeLocal() == null for imported Article (" + article.getPrimaryKey() + "). ProductType: " + articleProductType.getPrimaryKey());

//	store.addProduct(user, article.getProduct(), articleProductTypeLocal.getHome());
//	}
//	}

//	pm.flush();

//	if (resultArticles == null) // can this ever happen?!
//	resultArticles = new ArrayList(0);

//	return resultArticles;
//	} catch (Exception e) {
//	throw new RuntimeException(e);
//	}
//	}

	public void assignCustomer(Order order, LegalEntity customer)
	{
		// if it's already the correct customer, there's no need to change anything.
		if (order.getCustomer().equals(customer))
			return;

		// check if the vendor is the local organisation - otherwise the customer MUST NOT be changed
		if (!getMandator().equals(order.getVendor()))
			throw new UnsupportedOperationException("The customer must not be changed, if the vendor is not the local organisation! Cannot perform the requested operation for: " + order);

		// check offers for finalization
		for (Offer offer : order.getOffers()) {
			if (offer.isFinalized())
				throw new IllegalStateException("Order contains finalized Offer: " + JDOHelper.getObjectId(offer));

			JDOHelper.makeDirty(offer, "finalizeDT"); // force the offer to become dirty as the virtually assigned customerID isn't correct anymore => cache notification
		}

		order.setCustomer(customer);
		order.setCustomerGroup(customer.getDefaultCustomerGroup());
	}

	public void onRejectOffer(User user, Offer offer) {
		offer.getOfferLocal().reject(user);
		for (OfferActionHandler offerActionHandler : offer.getOfferLocal().getOfferActionHandlers()) {
			offerActionHandler.onRejectOffer(user, offer);
		}
	}
}
