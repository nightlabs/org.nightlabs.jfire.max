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
