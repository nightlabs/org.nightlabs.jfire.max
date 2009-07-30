package org.nightlabs.jfire.store.deliver;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.nightlabs.jfire.store.deliver.id.DeliveryID;
import org.nightlabs.jfire.transfer.AbstractTransferController;

public abstract class AbstractDeliveryController extends AbstractTransferController<DeliveryData, DeliveryID, DeliveryResult> implements DeliveryController {


	private static final long serialVersionUID = 1L;

	@Deprecated
	protected AbstractDeliveryController() { }
	
	public AbstractDeliveryController(List<DeliveryData> transferDatas) {
		super(transferDatas, getDeliveryIDs(transferDatas));
	}

	private static List<DeliveryID> getDeliveryIDs(List<DeliveryData> transferDatas) {
		List<DeliveryID> deliveryIDs = new LinkedList<DeliveryID>();
		for (DeliveryData data : transferDatas) {
			if (data == null)
				throw new IllegalArgumentException("List<DeliveryData> contains a null entry!");

			deliveryIDs.add(DeliveryID.create(data.getDelivery().getOrganisationID(), data.getDelivery().getDeliveryID()));
		}

		return deliveryIDs;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void _serverBegin() {
		if (isSkipServerStages())
			return;

		List<DeliveryResult> deliverBeginServerResults = null;
		try {
			for (DeliveryData deliveryData : getTransferDatas())
				deliveryData.prepareUpload();

			try {
				deliverBeginServerResults = getStoreManager().deliverBegin(getTransferDatasForServer());
			} finally {
				for (DeliveryData deliveryData : getTransferDatas())
					deliveryData.restoreAfterUpload();
			}

			if (deliverBeginServerResults.size() != getTransferDatas().size())
				throw new IllegalStateException("storeManager.deliverBegin(List) returned an invalid count of results! deliverBeginServerResults.size()=" + deliverBeginServerResults.size() + "; getTransferDatas().size()="+getTransferDatas().size());

			for (Iterator<?> itD = getTransferDatas().iterator(), itR = deliverBeginServerResults.iterator(); itD.hasNext(); ) {
				DeliveryData deliveryData = (DeliveryData) itD.next();
				DeliveryResult deliverBeginServerResult = (DeliveryResult) itR.next();
				deliveryData.getDelivery().setDeliverBeginServerResult(deliverBeginServerResult);
			}
		} catch (Throwable t) {
			DeliveryResult deliverBeginServerResult = new DeliveryResult(t);
			for (DeliveryData deliveryData : getTransferDatas())
				deliveryData.getDelivery().setDeliverBeginServerResult(deliverBeginServerResult);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void _serverDoWork() {
		if (isSkipServerStages())
			return;

		List<DeliveryResult> serverDeliverDoWorkResults = null;
		try {
			List<DeliveryResult> clientDeliverDoWorkResults = getLastStageResults();
			serverDeliverDoWorkResults = getStoreManager().deliverDoWork(getTransferIDs(), clientDeliverDoWorkResults, isForceRollback());

			if (serverDeliverDoWorkResults.size() != getTransferDatas().size())
				throw new IllegalStateException(
						"storeManager.deliverDoWork(List, List, boolean) returned an invalid count of results! deliverDoWorkServerResults.size()="
								+ serverDeliverDoWorkResults.size() + "; deliveryIDs.size()=" + getTransferIDs().size() + "; getTransferDatas().size()=" + getTransferDatas().size());

			Iterator<DeliveryResult> itR = serverDeliverDoWorkResults.iterator();
			for (DeliveryData deliveryData : getTransferDatas()) {
				DeliveryResult deliverDoWorkServerResult = itR.next();
				deliveryData.getDelivery().setDeliverDoWorkServerResult(deliverDoWorkServerResult);
			}
		// impossible to be thrown now since it does not inherit ModuleException anymore
//		} catch (DeliveryException x) {
//			for (DeliveryData deliveryData : getTransferDatas())
//				deliveryData.getDelivery().setDeliverDoWorkServerResult(x.getDeliveryResult());
		} catch (Throwable t) {
			DeliveryResult deliverDoWorkServerResult = new DeliveryResult(t);

			for (DeliveryData deliveryData : getTransferDatas())
				deliveryData.getDelivery().setDeliverDoWorkServerResult(deliverDoWorkServerResult);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void _serverEnd() {
		if (isSkipServerStages())
			return;

		List<DeliveryResult> deliverEndServerResults = null;
		try {
			deliverEndServerResults = getStoreManager().deliverEnd(getTransferIDs(), getLastStageResults(), isForceRollback());

			if (deliverEndServerResults.size() != getTransferDatas().size())
				throw new IllegalStateException("storeManager.deliverEnd(List, List, boolean) returned an invalid count of results! deliverEndServerResults.size()=" + deliverEndServerResults.size() + "; deliveryIDs.size()=" + getTransferIDs().size() + "; getTransferDatas().size()="+getTransferDatas().size());

			for (Iterator<?> itD = getTransferDatas().iterator(), itR = deliverEndServerResults.iterator(); itD.hasNext(); ) {
				DeliveryData deliveryData = (DeliveryData) itD.next();
				DeliveryResult deliverEndServerResult = (DeliveryResult) itR.next();
				deliveryData.getDelivery().setDeliverEndServerResult(deliverEndServerResult);
			}
			// impossible to be thrown now since it does not inherit ModuleException anymore
//		} catch (DeliveryException x) {
//			for (DeliveryData deliveryData : getTransferDatas())
//				deliveryData.getDelivery().setDeliverEndServerResult(x.getDeliveryResult());
		} catch (Throwable t) {
			DeliveryResult deliverEndServerResult = new DeliveryResult(t);
			for (DeliveryData deliveryData : getTransferDatas())
				deliveryData.getDelivery().setDeliverEndServerResult(deliverEndServerResult);
		}
	}

	@Override
	public boolean isRollbackRequired() {
		if (isForceRollback())
			return true;

		for (DeliveryData deliveryData : getTransferDatas()) {
			Delivery delivery = deliveryData.getDelivery();
			if (delivery.isFailed() || delivery.isForceRollback())
				return true;
		}

		return false;
	}
}
