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
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
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
import org.nightlabs.jfire.trade.history.ProductHistory;
import org.nightlabs.jfire.trade.history.ProductHistoryItem;
import org.nightlabs.jfire.trade.history.ProductHistoryItem.ProductHistoryItemType;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OfferLocalID;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerAcceptOffer;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerAcceptOfferImplicitelyVendor;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerFinalizeOffer;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerFinalizeOfferForCrossTrade;
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

	// /**
	// * key: String orderPK<br/>
	// * value: OrderRequirement orderRequirement
	// *
	// * @jdo.field
	// * persistence-modifier="persistent"
	// * collection-type="map"
	// * key-type="java.lang.String"
	// * value-type="OrderRequirement"
	// *
	// * @jdo.join
	// *
	// * @jdo.map-vendor-extension vendor-name="jpox" key="key-length" value="max
	// 201"
	// */
	// protected Map orderRequirements = new HashMap();
	//
	// /**
	// * key: String offerPK<br/>
	// * value: OfferRequirement offerRequirement
	// *
	// * @jdo.field
	// * persistence-modifier="persistent"
	// * collection-type="map"
	// * key-type="java.lang.String"
	// * value-type="OfferRequirement"
	// *
	// * @jdo.join
	// *
	// * @jdo.map-vendor-extension vendor-name="jpox" key="key-length" value="max
	// 201"
	// */
	// protected Map offerRequirements = new HashMap();

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

	public LegalEntity getVendor(String organisationID, String anchorID)
	{
		// TODO: implement
		return null;
		// String vendorPK = LegalEntity.getPrimaryKey(organisationID, anchorID);
		// return (LegalEntity) vendors.get(vendorPK);
	}

	public LegalEntity getCustomer(String organisationID, String anchorID)
	{
		// TODO: implement
		return null;
		// String customerPK = LegalEntity.getPrimaryKey(organisationID, anchorID);
		// return (LegalEntity) vendors.get(customerPK);
	}

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
	throws ModuleException
	{
		if (reversedArticles.isEmpty())
			throw new IllegalArgumentException("Set reversedArticles must not be empty!");

		if (offerIDPrefix == null) {
			TradeConfigModule tradeConfigModule;
			try {
				tradeConfigModule = Config.getConfig(
						getPersistenceManager(), getOrganisationID(), user).createConfigModule(TradeConfigModule.class);
			} catch (ModuleException x) {
				throw new RuntimeException(x); // should not happen.
			}

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
			try {
				tradeConfigModule = Config.getConfig(
						getPersistenceManager(), getOrganisationID(), user).createConfigModule(TradeConfigModule.class);
			} catch (ModuleException x) {
				throw new RuntimeException(x); // should not happen.
			}

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

	public Offer createOffer(User user, Order order, String offerIDPrefix) throws ModuleException
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
				try {
					tradeConfigModule = Config.getConfig(getPersistenceManager(), organisationID, user).createConfigModule(TradeConfigModule.class);
				} catch (ModuleException x) {
					throw new RuntimeException(x); // should not happen.
				}

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

		OfferConfigModule offerConfigModule;
		try {
			offerConfigModule = Config.getConfig(getPersistenceManager(), organisationID, workstation).createConfigModule(OfferConfigModule.class);
		} catch (ModuleException x) {
			throw new RuntimeException(x); // should not happen.
		}

		offerConfigModule.setOfferExpiry(offer);
	}

	public Set<Article> reverseArticles(User user, Offer reversingOffer, Collection<Article> reversedArticles)
	throws ModuleException
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
	 * method {@link #allocateArticlesEnd(User, Collection)} is either called
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
			boolean allocateSynchronously) throws ModuleException
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
	public void allocateArticles(User user, Collection<? extends Article> articles,
			boolean synchronously) throws ModuleException
			{
		try {
			allocateArticlesBegin(user, articles); // allocateArticleBegin
			// (re)creates the price

			if (synchronously)
				allocateArticlesEnd(user, articles);
			else
				AsyncInvoke.exec(
						new AllocateArticlesEndInvocation(user, articles),
						null,
						new AllocateArticlesEndErrorCallback(),
						new AllocateArticlesEndUndeliverableCallback(), true);

		} catch (RuntimeException x) {
			throw x;
		} catch (ModuleException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}
			}

	public static class AllocateArticlesEndUndeliverableCallback extends
	UndeliverableCallback
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void handle(AsyncInvokeEnvelope envelope) throws Exception
		{
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Article.class);
				Collection<ArticleID> articleIDs = ((AllocateArticlesEndInvocation) envelope
						.getInvocation()).getArticleIDs();
//				for (Iterator iter = articleIDs.iterator(); iter.hasNext();) {
//				ArticleID articleID = (ArticleID) iter.next();
				for (ArticleID articleID : articleIDs) {
					Article article = null;
					try {
						article = (Article) pm.getObjectById(articleID);
					} catch (JDOObjectNotFoundException x) {
						logger.error("AllocateArticlesEndUndeliverableCallback: Article does not exist in datastore: " + articleID);
					}

					if (article != null)
						article.setAllocationAbandoned(true);
				}
			} finally {
				pm.close();
			}
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
				InvocationError invocationError = envelope.getAsyncInvokeProblem(pm).getLastError();

				pm.getExtent(Article.class);
				Collection<ArticleID> articleIDs = ((AllocateArticlesEndInvocation) envelope
						.getInvocation()).getArticleIDs();
//				for (Iterator iter = articleIDs.iterator(); iter.hasNext();) {
//				ArticleID articleID = (ArticleID) iter.next();
				for (ArticleID articleID : articleIDs) {
					Article article = null;
					try {
						article = (Article) pm.getObjectById(articleID);
					} catch (JDOObjectNotFoundException x) {
						logger.error("AllocateArticlesEndErrorCallback: Article does not exist in datastore: " + articleID);
					}

					if (article != null) {
						if (invocationError.getError() != null)
							article.setAllocationException(invocationError.getError());
						else
							article.setAllocationException(invocationError.getErrorRootCauseClassName(), invocationError.getErrorMessage(), invocationError.getErrorStackTrace());
					}
				}
			} finally {
				pm.close();
			}
		}
	}

	public static class AllocateArticlesEndInvocation extends Invocation
	{
		private static final long serialVersionUID = 1L;

		private UserID userID;

		private Set<ArticleID> articleIDs;

		public Set<ArticleID> getArticleIDs()
		{
			return articleIDs;
		}

		public AllocateArticlesEndInvocation(User user, Collection<? extends Article> articles)
		{
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
				Trader.getTrader(pm).allocateArticlesEnd(user, articles);
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
	 * @param enableXA This applies only if <code>synchronously==false</code> and it is passed to
	 *		{@link AsyncInvoke#exec(Invocation, org.nightlabs.jfire.asyncinvoke.SuccessCallback, ErrorCallback, UndeliverableCallback, boolean)}.
	 */
	public void releaseArticles(User user, Collection<Article> articles,
			boolean synchronously, boolean deleteAfterRelease, boolean enableXA) throws ModuleException
			{
		try {
			releaseArticlesBegin(user, articles);

			if (synchronously)
				releaseArticlesEnd(user, articles, deleteAfterRelease);
			else
				AsyncInvoke.exec(
						new ReleaseArticlesEndInvocation(user, articles, deleteAfterRelease),
						null,
						new ReleaseArticlesEndErrorCallback(),
						new ReleaseArticlesEndUndeliverableCallback(),
						enableXA);

		} catch (RuntimeException x) {
			throw x;
		} catch (ModuleException x) {
			throw x;
		} catch (Exception x) {
			throw new ModuleException(x);
		}
			}

	public static class ReleaseArticlesEndUndeliverableCallback extends
	UndeliverableCallback
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void handle(AsyncInvokeEnvelope envelope) throws Exception
		{
			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Article.class);
				Collection<ArticleID> articleIDs = ((ReleaseArticlesEndInvocation) envelope
						.getInvocation()).getArticleIDs();
//				for (Iterator iter = articleIDs.iterator(); iter.hasNext();) {
//				ArticleID articleID = (ArticleID) iter.next();
				for (ArticleID articleID : articleIDs) {
					Article article = (Article) pm.getObjectById(articleID);
					article.setReleaseAbandoned(true);
				}
			} finally {
				pm.close();
			}
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
				InvocationError invocationError = envelope.getAsyncInvokeProblem(pm).getLastError();

				pm.getExtent(Article.class);
				Collection<ArticleID> articleIDs = ((ReleaseArticlesEndInvocation) envelope
						.getInvocation()).getArticleIDs();
//				for (Iterator iter = articleIDs.iterator(); iter.hasNext();) {
//				ArticleID articleID = (ArticleID) iter.next();
				for (ArticleID articleID : articleIDs) {
					Article article = (Article) pm.getObjectById(articleID);

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

		private UserID userID;

		private Collection<ArticleID> articleIDs;

		private boolean deleteAfterRelease;

		public Collection<ArticleID> getArticleIDs()
		{
			return articleIDs;
		}

		public ReleaseArticlesEndInvocation(User user, Collection<Article> articles, boolean deleteAfterRelease)
		{
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
				Collection<Article> articles = NLJDOHelper.getObjectSet(pm, articleIDs,
						Article.class);
				Trader.getTrader(pm).releaseArticlesEnd(user, articles, deleteAfterRelease);
			} finally {
				pm.close();
			}
			return null;
		}
	}

	protected void releaseArticlesBegin(User user, Collection<Article> articles)
	throws ModuleException
	{
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

		if (!tas.allocated || tas.releasePending)
			return;

		if (tas.allocationPending)
			throw new IllegalStateException(
					"Articles \""
					+ getToStringList(articles)
					+ "\" cannot be released, because they are currently in state allocationPending!");

		Map<ProductTypeActionHandler, List<Article>> productTypeActionHandler2Articles = new HashMap<ProductTypeActionHandler, List<Article>>();

//		JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
//		try {
//		Map<Offer, ProcessInstance> offer2ProcessInstance = new HashMap<Offer, ProcessInstance>();

		for (Article article : articles) {
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

	protected void releaseArticlesEnd(User user, Collection<Article> articles, boolean deleteAfterRelease)
	throws ModuleException
	{
		if (logger.isDebugEnabled()) {
			logger.debug("releaseArticlesEnd: entered with " + articles.size() + " articles.");
			if (logger.isTraceEnabled()) {
				for (Article article : articles)
					logger.trace("releaseArticlesEnd: * " + article);
			}
		}

		PersistenceManager pm = getPersistenceManager();
		NLJDOHelper.enableTransactionSerializeReadObjects(pm);
		try {
			pm.refreshAll(articles);
			for (Article article : articles) {
				pm.refresh(article.getProduct().getProductLocal());
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

		TotalArticleStatus tas = getTotalArticleStatus(articles);

		if (!tas.allocated)
			return;

		if (!tas.releasePending)
			throw new IllegalArgumentException("Articles "
					+ getToStringList(articles) + " are NOT in state releasePending!");

		ProductTypeActionHandlerCache productTypeActionHandlerCache = new ProductTypeActionHandlerCache(pm);

		Map<ProductTypeActionHandler, List<Article>> productTypeActionHandler2Articles = new HashMap<ProductTypeActionHandler, List<Article>>();
		for (Article article : articles) {
			Product product = article.getProduct();

			ProductTypeActionHandler productTypeActionHandler = productTypeActionHandlerCache.getProductTypeActionHandler(product);
			// delegate disassembling to the product
			productTypeActionHandler.disassembleProduct(user, productTypeActionHandlerCache, product, true);
//			product.disassemble(user, true);

			// clear product's article and update all states
			ProductLocal productLocal = product.getProductLocal();
			productLocal.setAllocated(false);
			productLocal.setSaleArticle(null);
			article.setAllocated(false);
			article.setReleasePending(false);

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

		if (deleteAfterRelease) {
			deleteArticles(user, articles);
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
	 *
	 * @param user
	 *          The user who is responsible for this allocation.
	 * @param article
	 *          The <code>Article</code> that shall be allocated. It must have
	 *          its <code>Product</code> assigned ({@link Article#getProduct()}
	 *          must not return <code>null</code>).
	 * @throws org.nightlabs.jfire.store.NotAvailableException
	 *           If the product (or a packaged product) cannot be allocated.
	 * @throws ModuleException
	 *           If another error occurs.
	 */
	protected void allocateArticlesBegin(User user, Collection<? extends Article> articles)
	throws ModuleException
	{
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
				pm.refresh(article.getProduct().getProductLocal());
			}
		} finally {
			NLJDOHelper.disableTransactionSerializeReadObjects(pm);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("allocateArticlesBegin: refreshing articles done.");
		}

		// If all articles are currently allocated or the allocation is pending, we
		// silently return.
		// If the status is different between them, we throw an
		// IllegalArgumentException
		TotalArticleStatus tas = getTotalArticleStatus(articles);

		if (tas.allocated || tas.allocationPending)
			return;

		if (tas.releasePending)
			throw new IllegalStateException(
					"Articles \""
					+ getToStringList(articles)
					+ "\" cannot be allocated, because it is currently in state releasePending!");

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
			IPackagePriceConfig packagePriceConfig = product.getProductType()
			.getPackagePriceConfig();

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
	 *
	 * @param user
	 *          The user who is responsible for this allocation.
	 * @param article
	 *          The <code>Article</code> that shall be allocated. It must have
	 *          its <code>Product</code> assigned ({@link Article#getProduct()}
	 *          must not return <code>null</code>).
	 * @throws ModuleException
	 */
	protected void allocateArticlesEnd(User user, Collection<? extends Article> articles)
	throws ModuleException
	{
		if (logger.isDebugEnabled()) {
			logger.debug("allocateArticlesEnd: entered with " + articles.size() + " articles.");
			if (logger.isTraceEnabled()) {
				for (Article article : articles)
					logger.trace("allocateArticlesEnd: * " + article);
			}
		}

		PersistenceManager pm = getPersistenceManager();
		NLJDOHelper.enableTransactionSerializeReadObjects(pm);
		try {
			pm.refreshAll(articles);
			for (Article article : articles) {
				pm.refresh(article.getProduct().getProductLocal());
			}
		} finally {
			NLJDOHelper.disableTransactionSerializeReadObjects(pm);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("allocateArticlesEnd: refreshing articles done.");
		}

		TotalArticleStatus tas = getTotalArticleStatus(articles);
		if (tas.allocated)
			return;

		if (!tas.allocationPending)
			throw new IllegalArgumentException("Articles "
					+ getToStringList(articles) + " are NOT in state allocationPending!");

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
			article.setAllocated(true);
			article.setAllocationPending(false);

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
		// check whether we have to finalize remote offers as well
		PersistenceManager pm = getPersistenceManager();
		OfferRequirement offerRequirement = OfferRequirement.getOfferRequirement(pm, offer, false);
		if (offerRequirement != null) {
			for (Iterator itO = offerRequirement.getPartnerOffers().iterator(); itO.hasNext(); ) {
				Offer partnerOffer = (Offer) itO.next();

				LegalEntity vendor = partnerOffer.getOrder().getVendor();
				if (!(vendor instanceof OrganisationLegalEntity))
					throw new IllegalStateException("Vendor of Offer " + partnerOffer.getPrimaryKey() + " is not an OrganisationLegalEntity, even though this Offer is part of the OfferRequirements for Offer " + offer.getPrimaryKey());

				String partnerOrganisationID = vendor.getOrganisationID();

//				TradeManager tradeManager = TradeManagerUtil.getHome(Lookup.getInitialContextProperties(pm, partnerOrganisationID)).create();
				signalOffer((OfferID) JDOHelper.getObjectId(partnerOffer), JbpmConstantsOffer.Vendor.TRANSITION_NAME_ACCEPT_FOR_CROSS_TRADE);
				// TODO this is not yet the right handling of JBPM - needs to be fixed! Isn't this fine now? Marco.
			} // for (Iterator itO = offerRequirement.getPartnerOffers().iterator(); itO.hasNext(); ) {
		} // if (offerRequirement != null) {

		offer.getOfferLocal().accept(user);
		for (OfferActionHandler offerActionHandler : offer.getOfferLocal().getOfferActionHandlers()) {
			offerActionHandler.onAcceptOffer(user, offer);
		}
	}

	/**
	 * You must NOT call this method directly. It is called by {@link ActionHandlerFinalizeOffer}.
	 */
	public void onFinalizeOffer(User user, Offer offer)
	throws RemoteException, CreateException, NamingException
	{
		PersistenceManager pm = getPersistenceManager();

		// check whether we have to finalize remote offers as well
		OfferRequirement offerRequirement = OfferRequirement.getOfferRequirement(pm, offer, false);
		if (offerRequirement != null) {
			for (Offer partnerOffer : offerRequirement.getPartnerOffers()) {
				LegalEntity vendor = partnerOffer.getOrder().getVendor();
				if (!(vendor instanceof OrganisationLegalEntity))
					throw new IllegalStateException("Vendor of Offer " + partnerOffer.getPrimaryKey() + " is not an OrganisationLegalEntity, even though this Offer is part of the OfferRequirements for Offer " + offer.getPrimaryKey());

				String partnerOrganisationID = vendor.getOrganisationID();

//				TradeManager tradeManager = TradeManagerUtil.getHome(Lookup.getInitialContextProperties(pm, partnerOrganisationID)).create();
////				tradeManager.signalOffer((OfferID) JDOHelper.getObjectId(partnerOffer), JbpmConstantsOffer.Vendor.TRANSITION_NAME_ACCEPT_FOR_CROSS_TRADE);
//				tradeManager.
				signalOffer((OfferID) JDOHelper.getObjectId(partnerOffer), JbpmConstantsOffer.Vendor.TRANSITION_NAME_FINALIZE_FOR_CROSS_TRADE);
				// TODO this is not yet the right handling of JBPM - needs to be fixed! Isn't this correct, now? Marco.
			} // for (Iterator itO = offerRequirement.getPartnerOffers().iterator(); itO.hasNext(); ) {
		} // if (offerRequirement != null) {

		// set expiry timestamp
		setOfferExpiry(offer);

		offer.setFinalized(user);

		// trigger listeners
		for (OfferActionHandler offerActionHandler : offer.getOfferLocal().getOfferActionHandlers()) {
			offerActionHandler.onFinalizeOffer(user, offer);
		}
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
	throws ModuleException
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
		StateDefinition stateDefinition;
		try {
			stateDefinition = StateDefinition.getStateDefinition(processDefinition, jbpmNodeName);
		} catch (JDOObjectNotFoundException x) {
			logger.warn("The ProcessDefinition \"" + processDefinition.getJbpmProcessDefinitionName() + "\" does not contain a jBPM Node named \"" + jbpmNodeName + "\"!");
			return;
		}
		stateDefinition.getName().setText(Locale.ENGLISH.getLanguage(), name);
		stateDefinition.getDescription().setText(Locale.ENGLISH.getLanguage(), description);
		stateDefinition.setPublicState(publicState);
	}

	/**
	 * TODO: Most things done here should be configured in the process definition xml file, not in code.
	 *       Examples:
	 *          * the action handlers on certain nodes
	 *          * The names of the nodes/states/transitions should also be in a xml file
	 */
	public ProcessDefinition storeProcessDefinitionOffer(TradeSide tradeSide, URL jbpmProcessDefinitionURL)
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();

		org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition = ProcessDefinition.readProcessDefinition(jbpmProcessDefinitionURL);

		// we add the events+actionhandlers
		ActionHandlerNodeEnter.register(jbpmProcessDefinition);

		if (TradeSide.vendor == tradeSide || TradeSide.customerLocal == tradeSide) {
			ActionHandlerFinalizeOffer.register(jbpmProcessDefinition);
			ActionHandlerSendOffer.register(jbpmProcessDefinition);
			ActionHandlerAcceptOffer.register(jbpmProcessDefinition);
			ActionHandlerAcceptOfferImplicitelyVendor.register(jbpmProcessDefinition);
		}
		if (TradeSide.vendor == tradeSide) {
			ActionHandlerFinalizeOfferForCrossTrade.register(jbpmProcessDefinition);
		}

		// store it
		ProcessDefinition processDefinition = ProcessDefinition.storeProcessDefinition(pm, null, jbpmProcessDefinition, jbpmProcessDefinitionURL);
		ProcessDefinitionID processDefinitionID = (ProcessDefinitionID) JDOHelper.getObjectId(processDefinition);

		setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Both.NODE_NAME_SENT,
				"sent",
				"The Offer has been sent from the vendor to the customer.",
				true);

		setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Both.NODE_NAME_REVOKED,
				"revoked",
				"The Offer has been revoked by the vendor. The result is the same as if the customer had rejected the offer. A new Offer needs to be created in order to continue the interaction.",
				true);

		setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Both.NODE_NAME_EXPIRED,
				"expired",
				"The Offer has expired - the customer waited too long. A new Offer needs to be created in order to continue the interaction.",
				true);

		switch (tradeSide) {
		case vendor:
		{
			// give known StateDefinitions a name and a description
			setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Vendor.NODE_NAME_CREATED,
					"created",
					"The Offer has been newly created. This is the first state in the Offer related workflow.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Vendor.NODE_NAME_ABORTED,
					"aborted",
					"The Offer has been aborted by the vendor (before finalization). A new Offer needs to be created in order to continue the interaction.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Vendor.NODE_NAME_FINALIZED,
					"finalized",
					"The Offer has been finalized. After that, it cannot be modified anymore. A modification would require revocation and recreation.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Vendor.NODE_NAME_ACCEPTED,
					"accepted",
					"The Offer has been accepted by the customer. That turns the offer into a binding contract.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Vendor.NODE_NAME_REJECTED,
					"rejected",
					"The Offer has been rejected by the customer. A new Offer needs to be created in order to continue the interaction.",
					true);

			// give known Transitions a name
			for (Transition transition : Transition.getTransitions(pm, processDefinitionID, JbpmConstantsOffer.Vendor.TRANSITION_NAME_ACCEPT_IMPLICITELY)) {
//				transition.getName().setText(Locale.ENGLISH.getLanguage(), "accept implicitely"); - no name necessary as it's not displayed to the user
				transition.setUserExecutable(false);
			}

			for (Transition transition : Transition.getTransitions(pm, processDefinitionID, JbpmConstantsOffer.Vendor.TRANSITION_NAME_CUSTOMER_ACCEPTED)) {
//				transition.getName().setText(Locale.ENGLISH.getLanguage(), "customer accepted");
				transition.setUserExecutable(false);
			}

			for (Transition transition : Transition.getTransitions(pm, processDefinitionID, JbpmConstantsOffer.Vendor.TRANSITION_NAME_CUSTOMER_REJECTED)) {
//				transition.getName().setText(Locale.ENGLISH.getLanguage(), "customer rejected");
				transition.setUserExecutable(false);
			}

//			Transition transition;

//			try {
//			transition = (Transition) pm.getObjectById(JbpmConstantsOffer.Vendor.getTransitionID_created_2_accept(processDefinitionID));
//			transition.getName().setText(Locale.ENGLISH.getLanguage(), "accept");
//			transition.setUserExecutable(false);
//			} catch (JDOObjectNotFoundException x) {
//			logger.warn("The ProcessDefinition \"" + processDefinition.getJbpmProcessDefinitionName() + "\" does not contain a jBPM Transition named \"accept\" from Node \"created\"!");
//			}

//			transition = (Transition) pm.getObjectById(JbpmConstantsOffer.Vendor.getTransitionID_finalized_2_customerAccepted(processDefinitionID));
//			transition.getName().setText(Locale.ENGLISH.getLanguage(), "customer accepted");
//			transition.setUserExecutable(false);

//			transition = (Transition) pm.getObjectById(JbpmConstantsOffer.Vendor.getTransitionID_finalized_2_customerRejected(processDefinitionID));
//			transition.getName().setText(Locale.ENGLISH.getLanguage(), "customer rejected");
//			transition.setUserExecutable(false);
		}
		break;
		case customerCrossOrganisation:
		{
			// give known StateDefinitions a name and a description
			setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Customer.NODE_NAME_CUSTOMER_ACCEPTED,
					"customer accepted",
					"The customer has accepted the Offer.",
					true);

			setStateDefinitionProperties(processDefinition, JbpmConstantsOffer.Customer.NODE_NAME_CUSTOMER_REJECTED,
					"customer rejected",
					"The customer has rejected the Offer.",
					true);

			for (Transition transition : Transition.getTransitions(pm, processDefinitionID, JbpmConstantsOffer.Customer.TRANSITION_NAME_EXPIRED)) {
//				transition.getName().setText(Locale.ENGLISH.getLanguage(), "expired"); - no name necessary as it's not displayed to the user
				transition.setUserExecutable(false);
			}

			for (Transition transition : Transition.getTransitions(pm, processDefinitionID, JbpmConstantsOffer.Customer.TRANSITION_NAME_REVOKED)) {
				transition.setUserExecutable(false);
			}

			for (Transition transition : Transition.getTransitions(pm, processDefinitionID, JbpmConstantsOffer.Customer.TRANSITION_NAME_ACCEPTED_IMPLICITELY)) {
				transition.setUserExecutable(false);
			}

//			// give known Transitions a name
//			Transition transition;

//			transition = (Transition) pm.getObjectById(JbpmConstantsOffer.Customer.getTransitionID_sent_2_expired(processDefinitionID));
//			transition.getName().setText(Locale.ENGLISH.getLanguage(), "expired");
//			transition.setUserExecutable(false);

//			transition = (Transition) pm.getObjectById(JbpmConstantsOffer.Customer.getTransitionID_sent_2_revoked(processDefinitionID));
//			transition.getName().setText(Locale.ENGLISH.getLanguage(), "revoked");
//			transition.setUserExecutable(false);
		}
		break;
		case customerLocal:
		{
			// later...
		}
		break;
		default:
			throw new IllegalStateException("Unknown TradeSide: " + tradeSide);
		}

		return processDefinition;
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
	 *
	 * signal a given Jbpm transition to the offer
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
//	TradeManager tradeManager = TradeManagerUtil.getHome(initialContextProperties).create();

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
}
