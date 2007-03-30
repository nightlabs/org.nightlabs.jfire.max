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

import org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.deliver.id.ServerDeliveryProcessorID;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * This implementation of
 * {@link org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor}
 * represents manual (hand-to-hand) delivery and therefore doesn't do
 * anything.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class ServerDeliveryProcessorManual extends ServerDeliveryProcessor
{
	public static ServerDeliveryProcessorManual getServerDeliveryProcessorManual(PersistenceManager pm)
	{
		ServerDeliveryProcessorManual serverDeliveryProcessorManual;
		try {
			pm.getExtent(ServerDeliveryProcessorManual.class);
			serverDeliveryProcessorManual = (ServerDeliveryProcessorManual) pm.getObjectById(
					ServerDeliveryProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerDeliveryProcessorManual.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverDeliveryProcessorManual = new ServerDeliveryProcessorManual(Organisation.DEVIL_ORGANISATION_ID, ServerDeliveryProcessorManual.class.getName());
			serverDeliveryProcessorManual = (ServerDeliveryProcessorManual) pm.makePersistent(serverDeliveryProcessorManual);
		}

		return serverDeliveryProcessorManual;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected ServerDeliveryProcessorManual()
	{
	}

	/**
	 * @param organisationID
	 * @param serverDeliveryProcessorID
	 */
	public ServerDeliveryProcessorManual(String organisationID,
			String serverDeliveryProcessorID)
	{
		super(organisationID, serverDeliveryProcessorID);
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#getAnchorOutside(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	public Anchor getAnchorOutside(DeliverParams deliverParams)
	{
		return getRepositoryOutside(deliverParams, "anchorOutside.manual");
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#externalDeliverBegin(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	protected DeliveryResult externalDeliverBegin(DeliverParams deliverParams) throws DeliveryException
	{
		return null;
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#externalDeliverDoWork(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	protected DeliveryResult externalDeliverDoWork(DeliverParams deliverParams)
			throws DeliveryException
	{
		return null;
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#externalDeliverCommit(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	protected DeliveryResult externalDeliverCommit(DeliverParams deliverParams) throws DeliveryException
	{
		return null;
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#externalDeliverRollback(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	protected DeliveryResult externalDeliverRollback(DeliverParams deliverParams) throws DeliveryException
	{
		return null;
	}

}
