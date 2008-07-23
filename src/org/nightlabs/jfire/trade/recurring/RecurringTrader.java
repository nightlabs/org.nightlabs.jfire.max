package org.nightlabs.jfire.trade.recurring;

import java.util.Iterator;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;

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
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(RecurringTrader.class);

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
			RecurringTrader trader = (RecurringTrader) it.next();
			// TODO remove this debug stuff
			String securityReflectorOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
			if (!securityReflectorOrganisationID.equals(trader.getOrganisationID()))
				throw new IllegalStateException("SecurityReflector returned organisationID " + securityReflectorOrganisationID + " but Trader.organisationID=" + trader.getOrganisationID());
			// TODO end debug
			return trader;
		}

		logger.info("getTrader: The Trader instance does not yet exist! Creating it...");

		RecurringTrader recurringTrader = new RecurringTrader();

		recurringTrader.store = Store.getStore(pm);
		recurringTrader.accounting = Accounting.getAccounting(pm);
		recurringTrader.organisationID = recurringTrader.accounting.getOrganisationID();
		recurringTrader.mandator = recurringTrader.accounting.getMandator();

		// create customer groups
		CustomerGroup anonymousCustomerGroup = new CustomerGroup(recurringTrader.organisationID, CustomerGroup.CUSTOMER_GROUP_ID_ANONYMOUS);
		anonymousCustomerGroup.getName().setText(Locale.GERMAN.getLanguage(), "Anonym");
		anonymousCustomerGroup.getName().setText(Locale.FRENCH.getLanguage(), "Anonyme");
		anonymousCustomerGroup.getName().setText(Locale.ENGLISH.getLanguage(), "Anonymous");
		anonymousCustomerGroup = pm.makePersistent(anonymousCustomerGroup);

		CustomerGroup resellerCustomerGroup = new CustomerGroup(recurringTrader.organisationID, CustomerGroup.CUSTOMER_GROUP_ID_RESELLER);
		resellerCustomerGroup.getName().setText(Locale.GERMAN.getLanguage(), "Wiederverkäufer");
		resellerCustomerGroup.getName().setText(Locale.ENGLISH.getLanguage(), "Reseller");
		resellerCustomerGroup = pm.makePersistent(resellerCustomerGroup);

		CustomerGroup defaultCustomerGroup = new CustomerGroup(recurringTrader.organisationID, CustomerGroup.CUSTOMER_GROUP_ID_DEFAULT);
		defaultCustomerGroup.getName().setText(Locale.GERMAN.getLanguage(), "Standard");
		defaultCustomerGroup.getName().setText(Locale.FRENCH.getLanguage(), "Standard");
		defaultCustomerGroup.getName().setText(Locale.ENGLISH.getLanguage(), "Default");
		defaultCustomerGroup = pm.makePersistent(defaultCustomerGroup);

		recurringTrader.defaultCustomerGroupForKnownCustomer = defaultCustomerGroup;
		recurringTrader = pm.makePersistent(recurringTrader);

		// Normally this is done by OrganisationLegalEntity.getOrganisationLegalEntity(...), but since this would cause an endless recursion, we skip it in this situation
		// there and do it here.
		recurringTrader.mandator.setDefaultCustomerGroup(defaultCustomerGroup);

		logger.info("getTrader: ...new Trader instance created and persisted!");
		return recurringTrader;
	}



	public String getOrganisationID() {
		return organisationID;
	}


	public Accounting getAccounting() {
		return accounting;
	}



	public Store getStore() {
		return store;
	}



	public OrganisationLegalEntity getMandator() {
		return mandator;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private CustomerGroup defaultCustomerGroupForKnownCustomer;

	/**
	 * @return Returns the <tt>CustomerGroup</tt> that is automatically assigned
	 *         to all newly created customers as
	 *         {@link LegalEntity#defaultCustomerGroup}. Note, that the anonymous
	 *         customer has a different <tt>CustomerGroup</tt> assigned!
	 */
	public CustomerGroup getDefaultCustomerGroupForKnownCustomer()
	{
		return defaultCustomerGroupForKnownCustomer;
	}


}
