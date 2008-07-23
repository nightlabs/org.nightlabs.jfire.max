package org.nightlabs.jfire.trade.recurring;

import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.config.TradeConfigModule;


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


	public String getOrganisationID() {
		return organisationID;
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
}
