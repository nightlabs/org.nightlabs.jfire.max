package org.nightlabs.jfire.store.deliver;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.annotation.Implement;
import org.nightlabs.config.ConfigModuleNotFoundException;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.deliver.id.ServerDeliveryProcessorID;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * This implementation of
 * {@link org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor}
 * represents delivery to a print queue. The printing of such deliveries
 * can be then processed later.
 *
 * @author Tobias Langner (tobias[dot]langner[at]nightlabs[dot]de)
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor"
 *		detachable="true"
 *		table="JFireTrade_ServerDeliveryProcessorDeliveryQueue"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class ServerDeliveryProcessorDeliveryQueue extends ServerDeliveryProcessor {
	
	private static final Logger logger = Logger.getLogger(ServerDeliveryProcessorDeliveryQueue.class);
	
	private static final long serialVersionUID = 1L;
	
	/** @jdo.field persistence-modifier="none" */
	private DeliveryQueueConfigModule deliveryQueueConfigModule;
	
	public static ServerDeliveryProcessorDeliveryQueue getServerDeliveryProcessorDeliveryQueue(PersistenceManager pm) {
		ServerDeliveryProcessorDeliveryQueue serverDeliveryProcessorDeliveryQueue;
		try {
			pm.getExtent(ServerDeliveryProcessorDeliveryQueue.class);
			serverDeliveryProcessorDeliveryQueue = (ServerDeliveryProcessorDeliveryQueue) pm.getObjectById(ServerDeliveryProcessorID.create(
					Organisation.DEVIL_ORGANISATION_ID, ServerDeliveryProcessorDeliveryQueue.class.getName()));
			
		} catch (JDOObjectNotFoundException e) {
			serverDeliveryProcessorDeliveryQueue = new ServerDeliveryProcessorDeliveryQueue(Organisation.DEVIL_ORGANISATION_ID,	ServerDeliveryProcessorDeliveryQueue.class.getName());
			serverDeliveryProcessorDeliveryQueue.getName().setText(Locale.ENGLISH.getLanguage(), "Server Delivery Processor for delivering tickets to the active print queue");

			serverDeliveryProcessorDeliveryQueue = (ServerDeliveryProcessorDeliveryQueue) pm.makePersistent(serverDeliveryProcessorDeliveryQueue);
		}

		return serverDeliveryProcessorDeliveryQueue;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected ServerDeliveryProcessorDeliveryQueue() {
	}

	/**
	 * @param organisationID
	 * @param serverDeliveryProcessorID
	 */
	public ServerDeliveryProcessorDeliveryQueue(String organisationID, String serverDeliveryProcessorID) {
		super(organisationID, serverDeliveryProcessorID);
	}

	@Implement
	public Anchor getAnchorOutside(DeliverParams deliverParams) {
		return getRepositoryOutside(deliverParams, "anchorOutside.deliveryQueue");
	}

	@Implement
	protected DeliveryResult externalDeliverBegin(DeliverParams deliverParams) throws DeliveryException {
		// Nothing to do
		return new DeliveryResult(DeliveryResult.CODE_POSTPONED, null, null);
	}

	@Implement
	protected DeliveryResult externalDeliverDoWork(DeliverParams deliverParams) throws DeliveryException {
		// Nothing to do
		return new DeliveryResult(DeliveryResult.CODE_POSTPONED, null, null);
	}

	@Implement
	protected DeliveryResult externalDeliverCommit(DeliverParams deliverParams) throws DeliveryException {
		DeliveryQueue activeDeliveryQueue = getDeliveryQueueConfigModule().getActiveDeliveryQueue();
		activeDeliveryQueue.addDelivery(deliverParams.deliveryData.getDelivery());
		
		getPersistenceManager().makePersistent(activeDeliveryQueue);
		
		return new DeliveryResult(DeliveryResult.CODE_POSTPONED, null, null);
	}

	@Implement
	protected DeliveryResult externalDeliverRollback(DeliverParams deliverParams) throws DeliveryException {
		DeliveryQueue activeDeliveryQueue = getDeliveryQueueConfigModule().getActiveDeliveryQueue();
		activeDeliveryQueue.removeDelivery(deliverParams.deliveryData.getDelivery());
		
		getPersistenceManager().makePersistent(activeDeliveryQueue);
		
		return new DeliveryResult(DeliveryResult.CODE_ROLLED_BACK_NO_EXTERNAL, null, null);
	}
	
	private DeliveryQueueConfigModule getDeliveryQueueConfigModule() {
		if (deliveryQueueConfigModule == null) {
			String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
			User user = SecurityReflector.getUserDescriptor().getUser(getPersistenceManager());
			deliveryQueueConfigModule = (DeliveryQueueConfigModule) Config.getConfig(getPersistenceManager(), organisationID, user).getConfigModule(DeliveryQueueConfigModule.class);
		}
		
		return deliveryQueueConfigModule;
	}
	
	@Override
	protected String _checkRequirements(CheckRequirementsEnvironment checkRequirementsEnvironment) {
		DeliveryQueueConfigModule cfMod;
		try {
			cfMod = getDeliveryQueueConfigModule();
		} catch (ConfigModuleNotFoundException e) {
			return "No active print queue defined.";
		}
		
		if (cfMod.getActiveDeliveryQueue() == null)
			return "No active print queue defined.";
		else
			return null;
	}
}
