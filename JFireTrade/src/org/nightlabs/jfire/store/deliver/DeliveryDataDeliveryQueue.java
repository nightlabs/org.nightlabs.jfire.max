package org.nightlabs.jfire.store.deliver;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jfire.store.deliver.id.DeliveryQueueID;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 * @author Tobias Langner (tobias[dot]langner[at]nightlabs[dot]de)
 *
 * @jdo.persistence-capable
 *    identity-type="application"
 *    persistence-capable-superclass="org.nightlabs.jfire.store.deliver.DeliveryData"
 *    detachable="true"
 *    table="JFireTrade_DeliveryDataDeliveryQueue"
 *
 * @jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_DeliveryDataDeliveryQueue")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DeliveryDataDeliveryQueue extends DeliveryData implements StoreCallback, AttachCallback {
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private DeliveryQueueID targetQueueID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DeliveryQueue targetQueue;

	public DeliveryDataDeliveryQueue(Delivery delivery) {
		super(delivery);
	}

	public void setTargetQueue(DeliveryQueue targetQueue) {
		this.targetQueue = targetQueue;
	}

	public DeliveryQueue getTargetQueue() {
		return targetQueue;
	}

	public void jdoPreStore() {
		ensureCorrectTargetQueue(targetQueueID);
	}

	public void jdoPostAttach(Object detached) {
		ensureCorrectTargetQueue(((DeliveryDataDeliveryQueue) detached).targetQueueID);
	}

	private void ensureCorrectTargetQueue(DeliveryQueueID targetQueueID) {
		if (targetQueueID == null)
			return;
		
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		targetQueue = (DeliveryQueue) pm.getObjectById(targetQueueID);
	}

	public void prepareForUpload() {
		targetQueueID = (DeliveryQueueID) JDOHelper.getObjectId(targetQueue);
	}

	public void jdoPreAttach() {
	}
}