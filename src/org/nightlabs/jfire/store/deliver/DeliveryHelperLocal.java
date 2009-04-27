package org.nightlabs.jfire.store.deliver;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.store.deliver.id.DeliveryDataID;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;

public interface DeliveryHelperLocal {

	/**
	 * @param deliveryData The <tt>DeliveryData</tt> to be stored.
	 * @return Returns the JDO objectID of the newly persisted <tt>deliveryData</tt>
	 * @throws ModuleException
	 */
	DeliveryDataID deliverBegin_storeDeliveryData(DeliveryData deliveryData);

	DeliveryResult deliverBegin_internal(DeliveryDataID deliveryDataID,
			String[] fetchGroups, int maxFetchDepth) throws DeliveryException;

	void deliverDoWork_storeDeliverDoWorkClientResult(DeliveryID deliveryID,
			DeliveryResult deliverDoWorkClientResult, boolean forceRollback)
			throws ModuleException;

	DeliveryResult deliverDoWork_internal(DeliveryID deliveryID,
			String[] fetchGroups, int maxFetchDepth) throws DeliveryException;

	DeliveryResult deliverEnd_internal(DeliveryID deliveryID,
			String[] fetchGroups, int maxFetchDepth) throws DeliveryException,
			AsyncInvokeEnqueueException;

	void deliverEnd_storeDeliverEndClientResult(DeliveryID deliveryID,
			DeliveryResult deliverEndClientResult, boolean forceRollback)
			throws ModuleException;

	DeliveryResult deliverBegin_storeDeliverBeginServerResult(
			DeliveryID deliveryID, DeliveryResult deliverBeginServerResult,
			boolean get, String[] fetchGroups, int maxFetchDepth)
			throws ModuleException;

	DeliveryResult deliverDoWork_storeDeliverDoWorkServerResult(
			DeliveryID deliveryID, DeliveryResult deliverDoWorkServerResult,
			boolean get, String[] fetchGroups, int maxFetchDepth)
			throws ModuleException;

	DeliveryResult deliverEnd_storeDeliverEndServerResult(
			DeliveryID deliveryID, DeliveryResult deliverEndServerResult,
			boolean get, String[] fetchGroups, int maxFetchDepth)
			throws ModuleException;

	void deliverRollback(DeliveryID deliveryID) throws ModuleException;

}