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
package org.nightlabs.jfire.store.deliver;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.deliver.id.CrossTradeDeliveryCoordinatorID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.deliver.id.CrossTradeDeliveryCoordinatorID"
 *		detachable="true"
 *		table="JFireTrade_CrossTradeDeliveryCoordinator"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, crossTradeDeliveryCoordinatorID"
 */
public class CrossTradeDeliveryCoordinator
{
	public static CrossTradeDeliveryCoordinator getDefaultCrossTradeDeliveryCoordinator(PersistenceManager pm)
	{
		CrossTradeDeliveryCoordinatorID id = CrossTradeDeliveryCoordinatorID.create(Organisation.DEVIL_ORGANISATION_ID, CrossTradeDeliveryCoordinator.class.getName());
		try {
			CrossTradeDeliveryCoordinator ctdc = (CrossTradeDeliveryCoordinator) pm.getObjectById(id);
			ctdc.getModeOfDeliveryFlavour();
			return ctdc;
		} catch (JDOObjectNotFoundException x) {
			CrossTradeDeliveryCoordinator ctdc = new CrossTradeDeliveryCoordinator(id.organisationID, id.crossTradeDeliveryCoordinatorID);

			ModeOfDeliveryFlavour modeOfDeliveryFlavour = (ModeOfDeliveryFlavour) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_FLAVOUR_ID_JFIRE);
			ctdc.setModeOfDeliveryFlavour(modeOfDeliveryFlavour);

			ServerDeliveryProcessor serverDeliveryProcessor = ServerDeliveryProcessorJFire.getServerDeliveryProcessorJFire(pm);
			ctdc.setServerDeliveryProcessor(serverDeliveryProcessor);

			ctdc = (CrossTradeDeliveryCoordinator) pm.makePersistent(ctdc);
			return ctdc;
		}
	}

	/**
	 * @jdo.field primary-key="true"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 */
	private String crossTradeDeliveryCoordinatorID;

	private ModeOfDeliveryFlavour modeOfDeliveryFlavour;

	private ServerDeliveryProcessor serverDeliveryProcessor;

	/**
	 * @deprecated Only for JDO!
	 */
	protected CrossTradeDeliveryCoordinator() {}

	public CrossTradeDeliveryCoordinator(String organisationID, String crossTradeDeliveryCoordinatorID)
	{
		this.organisationID = organisationID;
		this.crossTradeDeliveryCoordinatorID = crossTradeDeliveryCoordinatorID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getCrossTradeDeliveryCoordinatorID()
	{
		return crossTradeDeliveryCoordinatorID;
	}

	public ModeOfDeliveryFlavour getModeOfDeliveryFlavour()
	{
		return modeOfDeliveryFlavour;
	}
	public void setModeOfDeliveryFlavour(ModeOfDeliveryFlavour modeOfDeliveryFlavour)
	{
		this.modeOfDeliveryFlavour = modeOfDeliveryFlavour;
	}
	public ServerDeliveryProcessor getServerDeliveryProcessor()
	{
		return serverDeliveryProcessor;
	}
	public void setServerDeliveryProcessor(ServerDeliveryProcessor serverDeliveryProcessor)
	{
		this.serverDeliveryProcessor = serverDeliveryProcessor;
	}
}
