package org.nightlabs.jfire.store.deliver;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.deliver.id.ServerDeliveryProcessorID;
import org.nightlabs.jfire.store.resource.Messages;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.RequirementCheckResult;

/**
 * This implementation of {@link org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor}
 * represents delivery to a delivery queue. These deliveries can be then processed later.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
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
public class ServerDeliveryProcessorDeliveryQueue
extends ServerDeliveryProcessor
{
	private static final Logger logger = Logger.getLogger(ServerDeliveryProcessorDeliveryQueue.class);

	public static final String ERROR_CODE_NO_DELIVERY_QUEUE_DEFINED = ServerDeliveryProcessorDeliveryQueue.class.getName() + "." + "NO_DELIVERY_QUEUE_DEFINED"; //$NON-NLS-1$ //$NON-NLS-2$

	private static final long serialVersionUID = 1L;

	/** @jdo.field persistence-modifier="none" */
	private DeliveryQueueConfigModule deliveryQueueConfigModule;

	public static ServerDeliveryProcessorDeliveryQueue getServerDeliveryProcessorDeliveryQueue(PersistenceManager pm) {
		ServerDeliveryProcessorDeliveryQueue serverDeliveryProcessorDeliveryQueue;
		try {
			pm.getExtent(ServerDeliveryProcessorDeliveryQueue.class);
			serverDeliveryProcessorDeliveryQueue = (ServerDeliveryProcessorDeliveryQueue) pm.getObjectById(ServerDeliveryProcessorID.create(
					Organisation.DEV_ORGANISATION_ID, ServerDeliveryProcessorDeliveryQueue.class.getName()));

		} catch (JDOObjectNotFoundException e) {
			serverDeliveryProcessorDeliveryQueue = new ServerDeliveryProcessorDeliveryQueue(Organisation.DEV_ORGANISATION_ID,	ServerDeliveryProcessorDeliveryQueue.class.getName());
			serverDeliveryProcessorDeliveryQueue.getName().setText(Locale.ENGLISH.getLanguage(), "Server Delivery Processor for delivering to a delivery queue"); //$NON-NLS-1$
			serverDeliveryProcessorDeliveryQueue.getName().setText(Locale.GERMAN.getLanguage(), "Server LieferProcessor für das Liefern in eine Lieferwarteschlange"); //$NON-NLS-1$

			serverDeliveryProcessorDeliveryQueue = pm.makePersistent(serverDeliveryProcessorDeliveryQueue);
		}

		return serverDeliveryProcessorDeliveryQueue;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ServerDeliveryProcessorDeliveryQueue() {
	}

	/**
	 * @param organisationID
	 * @param serverDeliveryProcessorID
	 */
	public ServerDeliveryProcessorDeliveryQueue(String organisationID, String serverDeliveryProcessorID) {
		super(organisationID, serverDeliveryProcessorID);
	}

	@Override
	@Implement
	public Anchor getAnchorOutside(DeliverParams deliverParams) {
		// I think this doesn't matter since the delivery will be postponed anyway and thus no transfer done.
		return getRepositoryOutside(deliverParams, "anchorOutside.deliveryQueue"); //$NON-NLS-1$
	}

	@Override
	@Implement
	protected DeliveryResult externalDeliverBegin(DeliverParams deliverParams) throws DeliveryException {
		// Attach DeliveryActionHandlerDeliveryQueue
		PersistenceManager pm = getPersistenceManager();
		deliverParams.deliveryData.getDelivery().getDeliveryLocal().addDeliveryActionHandler(DeliveryActionHandlerDeliveryQueue.getDeliveryActionHandlerDeliveryQueue(pm));
		logger.debug("Attached DeliveryActionHandlerDeliveryQueue to delivery '" + deliverParams.deliveryData.getDelivery().getPrimaryKey() + "'"); //$NON-NLS-1$ //$NON-NLS-2$

		return new DeliveryResult(DeliveryResult.CODE_POSTPONED, null, null);
	}

	@Override
	@Implement
	protected DeliveryResult externalDeliverDoWork(DeliverParams deliverParams) throws DeliveryException {
		DeliveryQueue targetDeliveryQueue = getTargetDeliveryQueue(deliverParams);
		targetDeliveryQueue.addDelivery(deliverParams.deliveryData.getDelivery());

		return null;
	}

	@Override
	@Implement
	protected DeliveryResult externalDeliverCommit(DeliverParams deliverParams) throws DeliveryException {
		// Nothing to do
		return null; // should automatically be correct ;-)
	}

	@Override
	@Implement
	protected DeliveryResult externalDeliverRollback(DeliverParams deliverParams) throws DeliveryException {
		DeliveryQueue targetDeliveryQueue = getTargetDeliveryQueue(deliverParams);
		targetDeliveryQueue.removeDelivery(deliverParams.deliveryData.getDelivery());

		return null; // should automatically be correct ;-)
	}

	private DeliveryQueue getTargetDeliveryQueue(DeliverParams deliverParams) {
		if (deliverParams.deliveryData instanceof DeliveryDataDeliveryQueue) {
			DeliveryDataDeliveryQueue dData = (DeliveryDataDeliveryQueue) deliverParams.deliveryData;

			return dData.getTargetQueue();
		}
		throw new IllegalArgumentException("deliveryData is not of type DeliveryDataDeliveryQueue"); //$NON-NLS-1$
	}

	private DeliveryQueueConfigModule getDeliveryQueueConfigModule() {
		if (deliveryQueueConfigModule == null) {
			String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
			User user = SecurityReflector.getUserDescriptor().getUser(getPersistenceManager());
			deliveryQueueConfigModule = Config.getConfig(getPersistenceManager(), organisationID, user).getConfigModule(DeliveryQueueConfigModule.class);
		}

		return deliveryQueueConfigModule;
	}

	@Override
	protected RequirementCheckResult _checkRequirements(CheckRequirementsEnvironment checkRequirementsEnvironment) {
		DeliveryQueueConfigModule cfMod = getDeliveryQueueConfigModule();
		if (cfMod.getVisibleDeliveryQueues().isEmpty())
		{
			String resultCode = ERROR_CODE_NO_DELIVERY_QUEUE_DEFINED;
			String message = Messages.getString("org.nightlabs.jfire.store.deliver.ServerDeliveryProcessorDeliveryQueue.message.noDeliveryQueueDefined"); //$NON-NLS-1$
			return new RequirementCheckResult(resultCode, message);
		}
		else
			return null;
	}
}
