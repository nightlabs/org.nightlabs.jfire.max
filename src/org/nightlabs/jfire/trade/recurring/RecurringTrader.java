package org.nightlabs.jfire.trade.recurring;

import java.util.Iterator;
import javax.jdo.PersistenceManager;
import org.nightlabs.jfire.security.SecurityReflector;


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


}
