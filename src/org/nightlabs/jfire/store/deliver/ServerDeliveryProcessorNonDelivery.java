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
import org.nightlabs.jfire.transfer.Anchor;

/**
 * This implementation of
 * {@link org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor}
 * represents non-delivery and therefore doesn't do
 * anything but cause the delivery to be postponed.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor"
 *		detachable="true"
 *		table="JFireTrade_ServerDeliveryProcessorNonDelivery"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class ServerDeliveryProcessorNonDelivery extends ServerDeliveryProcessor
{
	public static ServerDeliveryProcessorNonDelivery getServerDeliveryProcessorNonDelivery(PersistenceManager pm)
	{
		ServerDeliveryProcessorNonDelivery serverDeliveryProcessorNonDelivery;
		try {
			pm.getExtent(ServerDeliveryProcessorNonDelivery.class);
			serverDeliveryProcessorNonDelivery = (ServerDeliveryProcessorNonDelivery) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEV_ORGANISATION_ID, ServerDeliveryProcessorNonDelivery.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverDeliveryProcessorNonDelivery = new ServerDeliveryProcessorNonDelivery(Organisation.DEV_ORGANISATION_ID, ServerDeliveryProcessorNonDelivery.class.getName());
			pm.makePersistent(serverDeliveryProcessorNonDelivery);
		}

		return serverDeliveryProcessorNonDelivery;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ServerDeliveryProcessorNonDelivery()
	{
	}

	/**
	 * @param organisationID
	 * @param serverDeliveryProcessorID
	 */
	public ServerDeliveryProcessorNonDelivery(String organisationID,
			String serverDeliveryProcessorID)
	{
		super(organisationID, serverDeliveryProcessorID);
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#getAnchorOutside(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	@Override
	public Anchor getAnchorOutside(DeliverParams deliverParams)
	{
		return getRepositoryOutside(deliverParams, "anchorOutside.nonDelivery");
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#externalDeliverBegin(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	@Override
	protected DeliveryResult externalDeliverBegin(DeliverParams deliverParams) throws DeliveryException
	{
		return new DeliveryResult(
				DeliveryResult.CODE_POSTPONED,
				(String)null,
				(Throwable)null);
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#externalDeliverDoWork(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	@Override
	protected DeliveryResult externalDeliverDoWork(DeliverParams deliverParams)
			throws DeliveryException
	{
		return new DeliveryResult(
				DeliveryResult.CODE_POSTPONED,
				(String)null,
				(Throwable)null);
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#externalDeliverCommit(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	@Override
	protected DeliveryResult externalDeliverCommit(DeliverParams deliverParams) throws DeliveryException
	{
		return new DeliveryResult(
				DeliveryResult.CODE_POSTPONED,
				(String)null,
				(Throwable)null);
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#externalDeliverRollback(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	@Override
	protected DeliveryResult externalDeliverRollback(DeliverParams deliverParams) throws DeliveryException
	{
		return null;
	}

}
