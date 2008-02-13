package org.nightlabs.jfire.store.deliver;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.config.ConfigModule;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModule"
 *		detachable="true"
 *		table="JFireTrade_DeliveryQueueConfigModule"
 *
 * @jdo.fetch-group name="DeliveryQueueConfigModule.visibleDeliveryQueues" fields="visibleDeliveryQueues"
 * @jdo.fetch-group name="DeliveryQueueConfigModule.this" fetch-groups="default" fields="visibleDeliveryQueues"
 * 
 * @jdo.inheritance strategy="new-table"
 */
public class DeliveryQueueConfigModule extends ConfigModule {

	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_THIS_DELIVERY_QUEUE_CONFIG_MODULE = "DeliveryQueueConfigModule.this";
	public static final String FETCH_GROUP_VISIBLE_DELIVERY_QUEUES = "DeliveryQueueConfigModule.visibleDeliveryQueues";

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
	 *		null-value="exception"
	 * 
	 * @jdo.join
	 */
	private Set<DeliveryQueue> visibleDeliveryQueues;
	
	@Override
	public void init() {
		visibleDeliveryQueues = new HashSet<DeliveryQueue>();
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
