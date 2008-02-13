package org.nightlabs.jfire.store.deliver;

import java.io.Serializable;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.deliver.id.DeliveryActionHandlerID;
import org.nightlabs.jfire.trade.Article;

/**
 * This {@link DeliveryActionHandler} handles deliveries in {@link DeliveryQueue}. When all articles in such a delivery are delivered,
 * the delivery in the delivery queue is removed since it is now completely processed.
 *
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.deliver.DeliveryActionHandler"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class DeliveryActionHandlerDeliveryQueue extends DeliveryActionHandler implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DeliveryActionHandlerDeliveryQueue.class);
	
	public static DeliveryActionHandler getDeliveryActionHandlerDeliveryQueue(PersistenceManager pm) {
		DeliveryActionHandlerDeliveryQueue actionHandler = null;
		try {
			actionHandler = (DeliveryActionHandlerDeliveryQueue) pm.getObjectById(DeliveryActionHandlerID.create(Organisation.DEV_ORGANISATION_ID, DeliveryActionHandlerDeliveryQueue.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			actionHandler = new DeliveryActionHandlerDeliveryQueue(Organisation.DEV_ORGANISATION_ID, DeliveryActionHandlerDeliveryQueue.class.getName());
			pm.makePersistent(actionHandler);
		}
		
		return actionHandler;
	}
	
	protected DeliveryActionHandlerDeliveryQueue() {}
	
	public DeliveryActionHandlerDeliveryQueue(String organisationID, String deliveryActionHandlerID) {
		super(organisationID, deliveryActionHandlerID);
	}

	@Override
	public void onFollowUpDeliverEnd(Delivery currentDelivery, Delivery precursorDelivery) throws DeliveryException {
		// remove the processed delivery from the delivery queue
		DeliveryQueue deliveryQueue = DeliveryQueue.getDeliveryQueueForDelivery(precursorDelivery, getPersistenceManager());
		
		boolean allArticlesInPrecursorDeliveryDelivered = true;
		for (Article article : precursorDelivery.getArticles()) {
			allArticlesInPrecursorDeliveryDelivered &= article.getArticleLocal().isDelivered();
		}
		
		if (allArticlesInPrecursorDeliveryDelivered) {
			logger.debug("All articles in precursor delivery " + precursorDelivery.getPrimaryKey() + " have been delivered.");
			deliveryQueue.markProcessed(precursorDelivery);
			logger.debug("Removed processed delivery " + precursorDelivery.getPrimaryKey() + " from DeliveryQueue '" + deliveryQueue.getName() + "'");
	
			// and remove the action handler again
			precursorDelivery.getDeliveryLocal().removeDeliveryActionHandler(this);
			logger.debug("Detached DeliveryActionHandler from DeliveryLocal " + precursorDelivery.getPrimaryKey());
		}
	}
}