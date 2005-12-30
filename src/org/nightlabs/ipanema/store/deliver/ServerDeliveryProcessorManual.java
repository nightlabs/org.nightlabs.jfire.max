/*
 * Created on Jun 11, 2005
 */
package org.nightlabs.ipanema.store.deliver;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.ipanema.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.ipanema.organisation.Organisation;
import org.nightlabs.ipanema.transfer.Anchor;

/**
 * This implementation of
 * {@link org.nightlabs.ipanema.store.deliver.ServerDeliveryProcessor}
 * represents manual (hand-to-hand) delivery and therefore doesn't do
 * anything.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.store.deliver.ServerDeliveryProcessor"
 *		detachable="true"
 *		table="JFireTrade_ServerDeliveryProcessorManual"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class ServerDeliveryProcessorManual extends ServerDeliveryProcessor
{
	public static ServerDeliveryProcessorManual getServerDeliveryProcessorManual(PersistenceManager pm)
	{
		ServerDeliveryProcessorManual serverDeliveryProcessorManual;
		try {
			pm.getExtent(ServerDeliveryProcessorManual.class);
			serverDeliveryProcessorManual = (ServerDeliveryProcessorManual) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerDeliveryProcessorManual.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverDeliveryProcessorManual = new ServerDeliveryProcessorManual(Organisation.DEVIL_ORGANISATION_ID, ServerDeliveryProcessorManual.class.getName());
			pm.makePersistent(serverDeliveryProcessorManual);
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
	 * @see org.nightlabs.ipanema.store.deliver.ServerDeliveryProcessor#getAnchorOutside(org.nightlabs.ipanema.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	public Anchor getAnchorOutside(DeliverParams deliverParams)
	{
		return getRepositoryOutside(deliverParams, "anchorOutside.manual");
	}

	/**
	 * @see org.nightlabs.ipanema.store.deliver.ServerDeliveryProcessor#externalDeliverBegin(org.nightlabs.ipanema.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	protected DeliveryResult externalDeliverBegin(DeliverParams deliverParams) throws DeliveryException
	{
		return null;
	}

	/**
	 * @see org.nightlabs.ipanema.store.deliver.ServerDeliveryProcessor#externalDeliverDoWork(org.nightlabs.ipanema.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	protected DeliveryResult externalDeliverDoWork(DeliverParams deliverParams)
			throws DeliveryException
	{
		return null;
	}

	/**
	 * @see org.nightlabs.ipanema.store.deliver.ServerDeliveryProcessor#externalDeliverCommit(org.nightlabs.ipanema.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	protected DeliveryResult externalDeliverCommit(DeliverParams deliverParams) throws DeliveryException
	{
		return null;
	}

	/**
	 * @see org.nightlabs.ipanema.store.deliver.ServerDeliveryProcessor#externalDeliverRollback(org.nightlabs.ipanema.store.deliver.ServerDeliveryProcessor.DeliverParams)
	 */
	protected DeliveryResult externalDeliverRollback(DeliverParams deliverParams) throws DeliveryException
	{
		return null;
	}

}
