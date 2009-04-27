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
import org.nightlabs.jfire.store.deliver.id.ServerDeliveryProcessorID;
import org.nightlabs.jfire.transfer.Anchor;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class ServerDeliveryProcessorMailingPhysicalDefault extends ServerDeliveryProcessor
{
	private static final long serialVersionUID = 1L;

	public static ServerDeliveryProcessorMailingPhysicalDefault getServerDeliveryProcessorMailingPhysicalDefault(PersistenceManager pm)
	{
		ServerDeliveryProcessorMailingPhysicalDefault serverDeliveryProcessorManual;
		try {
			pm.getExtent(ServerDeliveryProcessorMailingPhysicalDefault.class);
			serverDeliveryProcessorManual = (ServerDeliveryProcessorMailingPhysicalDefault) pm.getObjectById(
					ServerDeliveryProcessorID.create(Organisation.DEV_ORGANISATION_ID, ServerDeliveryProcessorMailingPhysicalDefault.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverDeliveryProcessorManual = new ServerDeliveryProcessorMailingPhysicalDefault(Organisation.DEV_ORGANISATION_ID, ServerDeliveryProcessorMailingPhysicalDefault.class.getName());
			serverDeliveryProcessorManual = pm.makePersistent(serverDeliveryProcessorManual);
		}

		return serverDeliveryProcessorManual;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ServerDeliveryProcessorMailingPhysicalDefault()
	{
	}

	public ServerDeliveryProcessorMailingPhysicalDefault(String organisationID,
			String serverDeliveryProcessorID)
	{
		super(organisationID, serverDeliveryProcessorID);
	}

	@Override
	public Anchor getAnchorOutside(DeliverParams deliverParams)
	{
		return getRepositoryOutside(deliverParams, "anchorOutside.mailing.physical.default");
	}

	@Override
	protected DeliveryResult externalDeliverBegin(DeliverParams deliverParams) throws DeliveryException
	{
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverDoWork(DeliverParams deliverParams)
			throws DeliveryException
	{
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverCommit(DeliverParams deliverParams) throws DeliveryException
	{
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverRollback(DeliverParams deliverParams) throws DeliveryException
	{
		return null;
	}

}
