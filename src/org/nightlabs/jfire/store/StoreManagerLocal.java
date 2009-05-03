package org.nightlabs.jfire.store;

import javax.ejb.Local;

import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryResult;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;
import org.nightlabs.jfire.timer.id.TaskID;

@Local
public interface StoreManagerLocal {

	/**
	 * Because this method is only available locally, it does not require any authorization and can be called by everyone
	 * (authorization is required for the remotely available <code>deliverBegin</code> methods which lead to this method being called).
	 */
	DeliveryResult _deliverBegin(DeliveryData deliveryData);

	/**
	 * This method does not require access right control, because it can only be performed, if <code>deliverBegin</code> was
	 * called before, which is restricted.
	 */
	DeliveryResult _deliverDoWork(DeliveryID deliveryID, DeliveryResult deliverDoWorkClientResult, boolean forceRollback);

	/**
	 * This method does not require access right control, because it can only be performed, if <code>deliverBegin</code> was
	 * called before, which is restricted.
	 */
	DeliveryResult _deliverEnd(DeliveryID deliveryID, DeliveryResult deliverEndClientResult, boolean forceRollback);

	void calculateProductTypeAvailabilityPercentage(TaskID taskID) throws Exception;
}