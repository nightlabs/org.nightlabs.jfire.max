/*
 * Created on Jan 5, 2005
 */
package org.nightlabs.jfire.accounting;

import java.util.Iterator;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.organisation.LocalOrganisation;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="datastore"
 *		detachable="true"
 *		table="JFireTrade_TariffRegistry"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @deprecated Is this still used?
 */
public class TariffRegistry
{
	/**
	 * This method returns the singleton instance of Accounting. If there is
	 * no instance of Accounting in the datastore, yet, it will be created.
	 *
	 * @param pm
	 * @return
	 */
	public static TariffRegistry getTariffRegistry(PersistenceManager pm)
	{
		Iterator it = pm.getExtent(TariffRegistry.class).iterator();
		if (it.hasNext())
			return (TariffRegistry)it.next();

		TariffRegistry categorySetRegistry = new TariffRegistry();

		// initialize the organisationID
		it = pm.getExtent(LocalOrganisation.class).iterator();
		if (!it.hasNext())
			throw new IllegalStateException("LocalOrganisation undefined in datastore!");
		LocalOrganisation localOrganisation = (LocalOrganisation) it.next();
		categorySetRegistry.organisationID = localOrganisation.getOrganisation().getOrganisationID();

		pm.makePersistent(categorySetRegistry);
		return categorySetRegistry;
	}
	
	public TariffRegistry()
	{
	}
	
	private long nextTariffID = 0;
	
	private String organisationID;

	public synchronized long createTariffID()
	{
		long res = nextTariffID;
		nextTariffID = res + 1;
		return res;
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
}
