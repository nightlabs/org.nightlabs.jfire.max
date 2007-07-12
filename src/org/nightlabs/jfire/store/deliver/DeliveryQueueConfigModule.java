package org.nightlabs.jfire.store.deliver;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.config.ConfigModule;

/**
 * @author Tobias Langner (tobias[dot]langner[at]nightlabs[dot]de)
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModule"
 *		detachable="true"
 *		table="JFireTrade_DeliveryQueueConfigModule"
 *
 * @jdo-fetch-group name="DeliveryQueueConfigModule.visibleDeliveryQueues" fetch-group="default, DeliveryQueue.name" fields="visibleDeliveryQueues"
 * @jdo-fetch-group name="DeliveryQueueConfigModule.activeDeliveryQueue" fetch-groups="default, DeliveryQueue.name" fields="activeDeliveryQueue"
 * @jdo-fetch-group name="DeliveryQueueConfigModule.this" fetch-groups="default, DeliveryQueue.this" fields="activeDeliveryQueue, visibleDeliveryQueues"
 * 
 * @jdo.inheritance strategy="new-table"
 */
public class DeliveryQueueConfigModule extends ConfigModule {

	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_THIS_DELIVERY_QUEUE_CONFIG_MODULE = "DeliveryQueueConfigModule.this";
	public static final String FETCH_GROUP_VISIBLE_DELIVERY_QUEUES = "DeliveryQueueConfigModule.visibleDeliveryQueues";
	public static final String FETCH_GROUP_ACTIVE_DELIVERY_QUEUE = "DeliveryQueueConfigModule.activeDeliveryQueue";

	/**
	 * This field determines the {@link DeliveryQueue} that is used when the entity
	 * configured by this config module adds a delivery to the print queue.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private DeliveryQueue activeDeliveryQueue;
	
	/**
	 * This set contains all {@link DeliveryQueue}s that are visible for the entity 
	 * configured by this config module.
	 * 
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		collection-type="collection"
	 * 		dependent-element="false"
	 * 		element-type="org.nightlabs.jfire.store.deliver.DeliveryQueue"
	 * 		table="JFireTrade_DeliveryQueueConfigModule_visibleDeliveryQueues"
	 * 
	 * @jdo.join
	 */
	private Set<DeliveryQueue> visibleDeliveryQueues;
	
	@Override
	public void init() {
		visibleDeliveryQueues = new HashSet<DeliveryQueue>();
	}
	
	/**
	 * Returns the currently active {@link DeliveryQueue}.
	 * @return The currently active {@link DeliveryQueue}.
	 */
	public DeliveryQueue getActiveDeliveryQueue() {
		return activeDeliveryQueue;
	}
	
	public void setActiveDeliveryQueue(DeliveryQueue activeDeliveryQueue) {
		this.activeDeliveryQueue = activeDeliveryQueue;
	}
	
	/**
	 * Returns a set of the visible {@link DeliveryQueue}s. 
	 * @return A set of the visible {@link DeliveryQueue}s.
	 */
	public List<DeliveryQueue> getVisibleDeliveryQueues() {
		return new LinkedList<DeliveryQueue>(visibleDeliveryQueues);
	}
	
	public void setVisibleDeliveryQueues(Collection<DeliveryQueue> visibleDeliveryQueues) {
		this.visibleDeliveryQueues = new HashSet<DeliveryQueue>(visibleDeliveryQueues);
	}
}
