package org.nightlabs.jfire.store.deliver;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.config.ConfigModule;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

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
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_DeliveryQueueConfigModule")
@FetchGroups({
	@FetchGroup(
		name=DeliveryQueueConfigModule.FETCH_GROUP_VISIBLE_DELIVERY_QUEUES,
		members=@Persistent(name="visibleDeliveryQueues")),
	@FetchGroup(
		fetchGroups={"default"},
		name="DeliveryQueueConfigModule.this",
		members=@Persistent(name="visibleDeliveryQueues"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DeliveryQueueConfigModule extends ConfigModule {

	private static final long serialVersionUID = 1L;
	
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
	@Join
	@Persistent(
		dependentElement="false",
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_DeliveryQueueConfigModule_visibleDeliveryQueues",
		persistenceModifier=PersistenceModifier.PERSISTENT)
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
