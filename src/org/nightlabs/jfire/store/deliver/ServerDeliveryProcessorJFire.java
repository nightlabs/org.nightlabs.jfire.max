package org.nightlabs.jfire.store.deliver;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.store.book.PartnerStorekeeper;
import org.nightlabs.jfire.store.deliver.id.ServerDeliveryProcessorID;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;

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
public class ServerDeliveryProcessorJFire
extends ServerDeliveryProcessor
{
	private static final long serialVersionUID = 1L;

	public static ServerDeliveryProcessorJFire getServerDeliveryProcessorJFire(PersistenceManager pm)
	{
		ServerDeliveryProcessorJFire serverDeliveryProcessorJFire;
		try {
			pm.getExtent(ServerDeliveryProcessorJFire.class);
			serverDeliveryProcessorJFire = (ServerDeliveryProcessorJFire) pm.getObjectById(
					ServerDeliveryProcessorID.create(Organisation.DEVIL_ORGANISATION_ID, ServerDeliveryProcessorJFire.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverDeliveryProcessorJFire = new ServerDeliveryProcessorJFire(Organisation.DEVIL_ORGANISATION_ID, ServerDeliveryProcessorJFire.class.getName());
			serverDeliveryProcessorJFire = (ServerDeliveryProcessorJFire) pm.makePersistent(serverDeliveryProcessorJFire);
		}

		return serverDeliveryProcessorJFire;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected ServerDeliveryProcessorJFire()
	{
	}

	public ServerDeliveryProcessorJFire(String organisationID, String serverDeliveryProcessorID)
	{
		super(organisationID, serverDeliveryProcessorID);
	}

	@Implement
	protected DeliveryResult externalDeliverBegin(DeliverParams deliverParams)
			throws DeliveryException
	{
		return null;
	}

	@Implement
	protected DeliveryResult externalDeliverCommit(DeliverParams deliverParams)
			throws DeliveryException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Implement
	protected DeliveryResult externalDeliverDoWork(DeliverParams deliverParams)
			throws DeliveryException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Implement
	protected DeliveryResult externalDeliverRollback(DeliverParams deliverParams)
			throws DeliveryException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Implement
	public Anchor getAnchorOutside(DeliverParams deliverParams)
	{
		PersistenceManager pm = getPersistenceManager();
		String organisationID = deliverParams.store.getOrganisationID();
		LegalEntity partner = deliverParams.deliveryData.getDelivery().getPartner();
		Repository outsideRepository = PartnerStorekeeper.createPartnerOutsideRepository(pm, organisationID, partner);
		return outsideRepository;
	}

}
