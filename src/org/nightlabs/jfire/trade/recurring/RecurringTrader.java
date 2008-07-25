package org.nightlabs.jfire.trade.recurring;

import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.OfferLocal;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.TradeSide;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.config.TradeConfigModule;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.trade.jbpm.id.ProcessDefinitionAssignmentID;


/**
 * RecurringTrader is responsible for purchase and sale of recurring orders and offers
 *
 * @author Fitas Amine <fitas[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.recurring.id.RecurringTraderID"
 *		detachable="true"
 *		table="JFireTrade_RecurringTrader"
 *
 * @jdo.create-objectid-class
 *
 * @jdo.inheritance strategy="new-table"
 */
public class RecurringTrader {

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;


	/**
	 * This method returns the singleton instance of Trader. If there is no
	 * instance of Trader in the datastore, yet, it will be created.
	 *
	 * @param pm
	 * @return
	 */
	public static RecurringTrader getRecurringTrader(PersistenceManager pm)
	{
		Iterator<?> it = pm.getExtent(RecurringTrader.class).iterator();
		if (it.hasNext()) {
			RecurringTrader recurringTrader = (RecurringTrader) it.next();
			// TODO remove this debug stuff
			String securityReflectorOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
			if (!securityReflectorOrganisationID.equals(recurringTrader.getOrganisationID()))
				throw new IllegalStateException("SecurityReflector returned organisationID " + securityReflectorOrganisationID + " but Trader.organisationID=" + recurringTrader.getOrganisationID());
			// TODO end debug
			return recurringTrader;
		}


		RecurringTrader recurringTrader = new RecurringTrader();
		recurringTrader.organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
		recurringTrader = pm.makePersistent(recurringTrader);

		return recurringTrader;
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID() {
		String res = organisationID;
		if (res == null)
			SecurityReflector.getUserDescriptor().getOrganisationID();

		return res;
	}



	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException(
			"This instance of Trader is currently not attached to a datastore! Cannot get a PersistenceManager!");

		return pm;
	}

	public String getOrderIDPrefix(User user, String orderIDPrefix)
	{
		if (orderIDPrefix == null) {
			TradeConfigModule tradeConfigModule;
			try {
				tradeConfigModule = (TradeConfigModule) Config.getConfig(
						getPersistenceManager(), getOrganisationID(), user).createConfigModule(TradeConfigModule.class);
			} catch (ModuleException x) {
				throw new RuntimeException(x); // should not happen.
			}

			orderIDPrefix = tradeConfigModule.getActiveIDPrefixCf(RecurringOrder.class.getName()).getDefaultIDPrefix();
		}
		return orderIDPrefix;
	}


	public String getOfferIDPrefix(User user, String orderIDPrefix)
	{
		if (orderIDPrefix == null) {
			TradeConfigModule tradeConfigModule;
			try {
				tradeConfigModule = (TradeConfigModule) Config.getConfig(
						getPersistenceManager(), getOrganisationID(), user).createConfigModule(TradeConfigModule.class);
			} catch (ModuleException x) {
				throw new RuntimeException(x); // should not happen.
			}

			orderIDPrefix = tradeConfigModule.getActiveIDPrefixCf(RecurringOffer.class.getName()).getDefaultIDPrefix();
		}
		return orderIDPrefix;
	}



	public RecurringOffer createRecurringOffer(User user, RecurringOrder recurringOrder, String offerIDPrefix) throws ModuleException
	{
		TradeSide tradeSide;

		LegalEntity vendor = recurringOrder.getVendor();
		if (vendor == null)
			throw new IllegalStateException("order.getVendor() returned null!");

		LegalEntity customer = recurringOrder.getCustomer();
		if (customer == null)
			throw new IllegalStateException("order.getCustomer() returned null!");

		PersistenceManager pm = getPersistenceManager();
		Trader trader = Trader.getTrader(pm);
		OrganisationLegalEntity mandator = trader.getMandator();

		if (mandator.equals(customer) && (vendor instanceof OrganisationLegalEntity)) {
			tradeSide = TradeSide.customerCrossOrganisation;
			// TODO: Implement foreign stuff
			throw new UnsupportedOperationException("NYI");
		}
		else {
			if (mandator.equals(vendor))
				tradeSide = TradeSide.vendor;
			else if (mandator.equals(customer))
				tradeSide = TradeSide.customerLocal;
			else
				throw new IllegalStateException("mandator is neither customer nor vendor! order=" + recurringOrder + " mandator=" + mandator);


			offerIDPrefix = getOfferIDPrefix(user, offerIDPrefix);

//			if (offerIDPrefix == null) {
//			TradeConfigModule tradeConfigModule;
//			try {
//			tradeConfigModule = (TradeConfigModule) Config.getConfig(
//			getPersistenceManager(), organisationID, user).createConfigModule(TradeConfigModule.class);
//			} catch (ModuleException x) {
//			throw new RuntimeException(x); // should not happen.
//			}

//			offerIDPrefix = tradeConfigModule.getActiveIDPrefixCf(DeliveryNote.class.getName()).getDefaultIDPrefix();
//			}

			RecurringOffer recurringOffer = new RecurringOffer(
					user, recurringOrder,
					offerIDPrefix, IDGenerator.nextID(RecurringOffer.class, offerIDPrefix));

			new OfferLocal(recurringOffer); // OfferLocal registers itself in Offer

			recurringOffer = getPersistenceManager().makePersistent(recurringOffer);
			trader.validateOffer(recurringOffer);

			ProcessDefinitionAssignment processDefinitionAssignment = (ProcessDefinitionAssignment) getPersistenceManager().getObjectById(
					ProcessDefinitionAssignmentID.create(RecurringOffer.class, tradeSide));
			processDefinitionAssignment.createProcessInstance(null, user, recurringOffer);

			return recurringOffer;
		}
	}


	public RecurringOrder createRecurringOrder(LegalEntity vendor,
			LegalEntity customer, String orderIDPrefix, Currency currency)
//	throws ModuleException
	{
		if (customer == null)
			throw new IllegalArgumentException("customer must not be null!");

		if (currency == null)
			throw new IllegalArgumentException("currency must not be null!");

		PersistenceManager pm = getPersistenceManager();
		Trader trader = Trader.getTrader(pm);

		if (!trader.getMandator().equals(vendor) && (vendor instanceof OrganisationLegalEntity)) {
			// TODO: Implement foreign stuff
			throw new UnsupportedOperationException("NYI");
		}
		else {
			// local: the vendor is the local organisation (owning this datastore) OR it is a locally managed non-organisation-LE
			User user = SecurityReflector.getUserDescriptor().getUser(pm);

			orderIDPrefix = getOrderIDPrefix(user, orderIDPrefix);

			RecurringOrder recurringOrder = new RecurringOrder(
					vendor, customer,
					orderIDPrefix, IDGenerator.nextID(Order.class, orderIDPrefix),
					currency, user);

			recurringOrder = getPersistenceManager().makePersistent(recurringOrder);
			return recurringOrder;
		}
	}


	public RecurringOfferConfiguration storeRecurringOfferConfiguration(RecurringOfferConfiguration configuration, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, configuration, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

}
