package org.nightlabs.jfire.store.deliver;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.AttachCallback;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;
import org.nightlabs.jfire.store.deliver.id.DeliveryQueueID;


/**
 * @author Tobias Langner (tobias[dot]langner[at]nightlabs[dot]de)
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.deliver.id.DeliveryQueueID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryQueue"
 *
 * @jdo.create-objectid-class
 * 
 * @jdo.fetch-group name="DeliveryQueue.name" fields="name"
 * @jdo.fetch-group name="DeliveryQueue.deliverySet" fields="deliverySet"
 * @jdo.fetch-group name="DeliveryQueue.outstandingDeliverySet" fields="outstandingDeliverySet"
 * @jdo.fetch-group name="DeliveryQueue.this" fetch-groups="default" fields="name, deliverySet, outstandingDeliverySet"
 * 
 * @jdo.query
 * 		name="getDeliveryQueues"
 * 		query="SELECT WHERE !defunct || defunct == :includeDefunct"
 */
public class DeliveryQueue implements Serializable, AttachCallback {
	
	public static final String FETCH_GROUP_DELIVERY_QUEUE = "DeliveryQueue.this";
	public static final String FETCH_GROUP_NAME = "DeliveryQueue.name";
	public static final String FETCH_GROUP_DELIVERY_SET = "DeliveryQueue.deliverySet";
	public static final String FETCH_GROUP_OUTSTANDING_DELIVERY_SET = "DeliveryQueue.outstandingDeliverySet";
	
	private static final long serialVersionUID = 1L;	

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/** @jdo.field primary-key="true" */
	private long deliveryQueueID;
	
	/**
	 * A descriptive name for this {@link DeliveryQueue}.
	 * 
	 * @jdo.field persistence-modifier="persistent" mapped-by="deliveryQueue"
	 */
	private DeliveryQueueName name; 
	
	/**
	 * The list of the deliveries in this queue no matter their status.
	 * 
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		collection-type="collection"
	 * 		dependent-element="false"
	 * 		element-type="org.nightlabs.jfire.store.deliver.Delivery"
	 * 		table="JFireTrade_DeliveryQueue_deliverySet"
	 *		null-value="exception"
	 *  
	 * @jdo.join
	 */
	private Set<Delivery> deliverySet;
	
	/**
	 * The list of the deliveries in this queue, that have not been printed yet.
	 * 
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		collection-type="collection"
	 * 		dependent-element="false"
	 * 		element-type="org.nightlabs.jfire.store.deliver.Delivery"
	 * 		table="JFireTrade_DeliveryQueue_outstandingDeliverySet"
	 *		null-value="exception"
	 * 
	 * @jdo.join
	 */
	private Set<Delivery> outstandingDeliverySet;
	
	/**
	 * This field indicates whether a delivery queue has been deactivated irrevocably. In that state, no new deliveries
	 * may be added to the DeliveryQueue anymore. However, outstanding deliveries may still be processed in order to empty the queue.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean defunct = false;
	
	/** @deprecated Only for JDO! */
	protected DeliveryQueue() {		
	}
	
	public DeliveryQueue(String organisationID) {
		this.organisationID = organisationID;		
		this.deliveryQueueID = IDGenerator.nextID(DeliveryQueue.class);
		this.deliverySet = new HashSet<Delivery>();
		this.outstandingDeliverySet = new HashSet<Delivery>();
		this.name = new DeliveryQueueName(this);
	}
	
	public Set<Delivery> getDeliverySet() {
		return deliverySet;
	}
	
	public DeliveryQueueName getName() {
		return name;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public long getDeliveryQueueID() {
		return deliveryQueueID;
	}
	
	public DeliveryQueueID getObjectID() {
		return DeliveryQueueID.create(deliveryQueueID, organisationID);
	}
	
	/**
	 * Adds a delivery to this queue.
	 * @param delivery The delivery to be added.
	 */
	public void addDelivery(Delivery delivery) {
		if (isDefunct())
			throw new IllegalStateException("DeliveryQueue is defunct. No new deliveries may be added.");
		
		deliverySet.add(delivery);
		outstandingDeliverySet.add(delivery);
	}
	
	/**
	 * Removes a delivery from this queue.
	 * @param delivery The delivery to be removed.
	 */
	public void removeDelivery(Delivery delivery) {
		if (isDefunct())
			throw new IllegalStateException("DeliveryQueue is defunct. No new deliveries may be removed.");
		
		deliverySet.remove(delivery);
		outstandingDeliverySet.remove(delivery);
	}
	
	/**
	 * This marks the given delivery as processed. Internally, this means that the delivery is removed from the list of
	 * outstanding deliveries.
	 * 
	 * @param delivery The delivery that is to be marked as processed.
	 */
	public void markProcessed(Delivery delivery) {
		outstandingDeliverySet.remove(delivery);
	}
	
	public Set<Delivery> getOutstandingDeliveries() {
		return Collections.unmodifiableSet(outstandingDeliverySet);
	}
	
	public boolean hasOutstandingDeliveries() {
		return outstandingDeliverySet.size() > 0;
	}
	
	public void markDeleted() {
		this.defunct = true;
	}
	
	public boolean isDefunct() {
		return defunct;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (deliveryQueueID ^ (deliveryQueueID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DeliveryQueue))
			return false;
		final DeliveryQueue other = (DeliveryQueue) obj;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		if (deliveryQueueID != other.deliveryQueueID)
			return false;
		return true;
	}
	
	public static Collection<DeliveryQueue> getDeliveryQueues(PersistenceManager pm, boolean includeDefuncted) {
		Query q = pm.newNamedQuery(DeliveryQueue.class, "getDeliveryQueues");
		return (Collection<DeliveryQueue>) q.execute(includeDefuncted);
	}

	public void jdoPreAttach() {
		try {
			checkDirtyStates(outstandingDeliverySet);
			checkDirtyStates(deliverySet);
		} catch(JDODetachedFieldAccessException e) {
			// do nothing
			
			// FIXME WORKAROUND
			// Somehow the fetchgroups returned in DeliveryQueueConfigModuleController#getConfigModuleFetchGroups are not used properly
			// when retrievingthe DeliveryQueueConfigModule along with its DeliveryQueues.
		}
	}
	
	public void jdoPostAttach(Object attached) {
	}
	
	private void checkDirtyStates(Collection<Delivery> deliveries) {
		for (Delivery delivery : deliveries) {
			if (JDOHelper.isDirty(delivery))
				throw new IllegalArgumentException("You are not supposed to change the deliveries in a delivery queue.");				
		}
	}
}