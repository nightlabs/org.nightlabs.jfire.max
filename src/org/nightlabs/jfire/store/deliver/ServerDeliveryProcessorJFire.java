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
					ServerDeliveryProcessorID.create(Organisation.DEV_ORGANISATION_ID, ServerDeliveryProcessorJFire.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverDeliveryProcessorJFire = new ServerDeliveryProcessorJFire(Organisation.DEV_ORGANISATION_ID, ServerDeliveryProcessorJFire.class.getName());
			serverDeliveryProcessorJFire = pm.makePersistent(serverDeliveryProcessorJFire);
		}

		return serverDeliveryProcessorJFire;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ServerDeliveryProcessorJFire()
	{
	}

	public ServerDeliveryProcessorJFire(String organisationID, String serverDeliveryProcessorID)
	{
		super(organisationID, serverDeliveryProcessorID);
	}

	@Override
	protected DeliveryResult externalDeliverBegin(DeliverParams deliverParams)
			throws DeliveryException
	{
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverCommit(DeliverParams deliverParams)
			throws DeliveryException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverDoWork(DeliverParams deliverParams)
			throws DeliveryException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DeliveryResult externalDeliverRollback(DeliverParams deliverParams)
			throws DeliveryException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Anchor getAnchorOutside(DeliverParams deliverParams)
	{
		return getRepositoryOutside(deliverParams, null);
//		PersistenceManager pm = getPersistenceManager();
//		String organisationID = deliverParams.store.getOrganisationID();
//		LegalEntity partner = deliverParams.deliveryData.getDelivery().getPartner();
//		Repository outsideRepository = PartnerStorekeeper.createPartnerOutsideRepository(pm, organisationID, partner);
//		return outsideRepository;
	}
}
