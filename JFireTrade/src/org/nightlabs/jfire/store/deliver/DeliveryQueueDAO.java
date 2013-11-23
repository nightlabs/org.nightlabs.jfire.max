package org.nightlabs.jfire.store.deliver;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.deliver.id.DeliveryQueueID;
import org.nightlabs.progress.ProgressMonitor;

public class DeliveryQueueDAO extends BaseJDOObjectDAO<DeliveryQueueID, DeliveryQueue>{

	private static DeliveryQueueDAO sharedInstance;

	public static synchronized DeliveryQueueDAO sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new DeliveryQueueDAO();

		return sharedInstance;
	}

	private DeliveryQueueDAO() {

	}

	@Override
	protected Collection<DeliveryQueue> retrieveJDOObjects(Set<DeliveryQueueID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		monitor.beginTask("Fetching DeliveryQueues", 3);
		StoreManagerRemote storeManager = getEjbProvider().getRemoteBean(StoreManagerRemote.class);
		monitor.worked(1);
		Collection<DeliveryQueue> deliveryQueues = storeManager.getDeliveryQueuesById(objectIDs, fetchGroups, maxFetchDepth);
		monitor.worked(2);
		monitor.done();

		return deliveryQueues;
	}

	public synchronized DeliveryQueue getDeliveryQueue(DeliveryQueueID deliveryQueueId, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getDeliveryQueues(Arrays.asList(new DeliveryQueueID[] { deliveryQueueId }), fetchGroups, maxFetchDepth, monitor).iterator().next();
	}

	public synchronized Collection<DeliveryQueue> getDeliveryQueues(Collection<DeliveryQueueID> deliveryQueueIds, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if (deliveryQueueIds == null || deliveryQueueIds.isEmpty()) {
			return Collections.emptySet();
		}
		try {
			return getJDOObjects(null, new HashSet<DeliveryQueueID>(deliveryQueueIds), fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized DeliveryQueue storeDeliveryQueue(DeliveryQueue deliveryQueue, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		Collection<DeliveryQueue> result = storeDeliveryQueues(Collections.singleton(deliveryQueue), get, fetchGroups, maxFetchDepth, monitor);
		if (get)
			return result.iterator().next();
		else
			return null;
	}

	public synchronized Collection<DeliveryQueue> storeDeliveryQueues(Collection<DeliveryQueue> deliveryQueues, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		monitor.beginTask("Storing DeliveryQueues", 3);
		StoreManagerRemote storeManager = getEjbProvider().getRemoteBean(StoreManagerRemote.class);
		monitor.worked(1);

		try {
			Collection<DeliveryQueue> result = storeManager.storeDeliveryQueues(deliveryQueues, get, fetchGroups, maxFetchDepth);
			monitor.worked(2);
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException(e);
		}
	}
}
