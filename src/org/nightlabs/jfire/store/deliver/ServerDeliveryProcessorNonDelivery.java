/*
 * Created on Jun 11, 2005
 */
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
 * @jdo.inheritance strategy="new-table"
 */
public class ServerDeliveryProcessorNonDelivery extends ServerDeliveryProcessor
{
	public static ServerDeliveryProcessorNonDelivery getServerDeliveryProcessorNonDelivery(PersistenceManager pm)
	{
		ServerDeliveryProcessorNonDelivery serverDeliveryProcessorNonDelivery;
		try {
			pm.getExtent(ServerDeliveryProcessorNonDelivery.class);
			serverDeliveryProcessorNonDelivery = (ServerDeliveryProcessorNonDelivery) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerDeliveryProcessorNonDelivery.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverDeliveryProcessorNonDelivery = new ServerDeliveryProcessorNonDelivery(Organisation.DEVIL_ORGANISATION_ID, ServerDeliveryProcessorNonDelivery.class.getName());
			pm.makePersistent(serverDeliveryProcessorNonDelivery);
		}

		return serverDeliveryProcessorNonDelivery;
	}

	/**
	 * @deprecated Only for JDO!
	 */
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
	public Anchor getAnchorOutside(DeliverParams deliverParams)
	{
		return getRepositoryOutside(deliverParams, "anchorOutside.nonDelivery");
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#externalDeliverBegin(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	protected DeliveryResult externalDeliverBegin(DeliverParams deliverParams) throws DeliveryException
	{
		return new DeliveryResult(deliverParams.store.getOrganisationID(),
				DeliveryResult.CODE_POSTPONED,
				(String)null,
				(Throwable)null);
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#externalDeliverDoWork(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	protected DeliveryResult externalDeliverDoWork(DeliverParams deliverParams)
			throws DeliveryException
	{
		return new DeliveryResult(deliverParams.store.getOrganisationID(),
				DeliveryResult.CODE_POSTPONED,
				(String)null,
				(Throwable)null);
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#externalDeliverCommit(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	protected DeliveryResult externalDeliverCommit(DeliverParams deliverParams) throws DeliveryException
	{
		return new DeliveryResult(deliverParams.store.getOrganisationID(),
				DeliveryResult.CODE_POSTPONED,
				(String)null,
				(Throwable)null);
	}

	/**
	 * @see org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor#externalDeliverRollback(org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	protected DeliveryResult externalDeliverRollback(DeliverParams deliverParams) throws DeliveryException
	{
		return null;
	}

}
