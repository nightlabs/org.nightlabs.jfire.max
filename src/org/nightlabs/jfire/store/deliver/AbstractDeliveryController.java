package org.nightlabs.jfire.store.deliver;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerUtil;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;
import org.nightlabs.jfire.transfer.Stage;
import org.nightlabs.jfire.transfer.TransferController;

public abstract class AbstractDeliveryController extends TransferController<DeliveryData, DeliveryID, DeliveryResult> {
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.transfer.TransferController#serverBegin()
	 */
	@Override
	public void serverBegin() {
		assertLastStage(Stage.ClientBegin);
		setLastStage(Stage.ServerBegin);
		
		if (isSkipServerStages())
			return;
		
		List<DeliveryResult> deliverBeginServerResults = null;
		try {
			for (DeliveryData deliveryData : getTransferDatas())
				deliveryData.prepareUpload();
			
			try {
				deliverBeginServerResults = getStoreManager().deliverBegin(getTransferDatas());
			} finally {
				for (DeliveryData deliveryData : getTransferDatas())
					deliveryData.restoreAfterUpload();
			}

			if (deliverBeginServerResults.size() != getTransferDatas().size())
				throw new IllegalStateException("storeManager.deliverBegin(List) returned an invalid count of results! deliverBeginServerResults.size()=" + deliverBeginServerResults.size() + "; getTransferDatas().size()="+getTransferDatas().size());

			for (Iterator itD = getTransferDatas().iterator(), itR = deliverBeginServerResults.iterator(); itD.hasNext(); ) {
				DeliveryData deliveryData = (DeliveryData) itD.next();
				DeliveryResult deliverBeginServerResult = (DeliveryResult) itR.next();
				deliveryData.getDelivery().setDeliverBeginServerResult(deliverBeginServerResult);
			}
		} catch (DeliveryException x) {
			for (DeliveryData deliveryData : getTransferDatas())
				deliveryData.getDelivery().setDeliverBeginServerResult(x.getDeliveryResult());
		} catch (Throwable t) {
			DeliveryResult deliverBeginServerResult = new DeliveryResult(t);
			for (DeliveryData deliveryData : getTransferDatas())
				deliveryData.getDelivery().setDeliverBeginServerResult(deliverBeginServerResult);
		}
		
		setLastStageResults(deliverBeginServerResults);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.transfer.TransferController#serverDoWork()
	 */
	@Override
	public void serverDoWork() {
		assertLastStage(Stage.ClientDoWork);
		setLastStage(Stage.ServerDoWork);
		
		if (isSkipServerStages())
			return;
		
		List<DeliveryResult> serverDeliverDoWorkResults = null;
		try {
			List<DeliveryResult> clientDeliverDoWorkResults = getLastStageResults();
			//			for (Iterator itD = getTransferDatas().iterator(); itD.hasNext(); )
			//				((DeliveryData)itD.next()).prepareUpload();
			//			try {
			serverDeliverDoWorkResults = getStoreManager().deliverDoWork(getTransferIDs(), clientDeliverDoWorkResults, isForceRollback());
			//			} finally {
			//				for (Iterator itD = getTransferDatas().iterator(); itD.hasNext(); )
			//					((DeliveryData)itD.next()).restoreAfterUpload();
			//			}

			if (serverDeliverDoWorkResults.size() != getTransferDatas().size())
				throw new IllegalStateException(
						"storeManager.deliverDoWork(List, List, boolean) returned an invalid count of results! deliverDoWorkServerResults.size()="
								+ serverDeliverDoWorkResults.size() + "; deliveryIDs.size()=" + getTransferIDs().size() + "; getTransferDatas().size()=" + getTransferDatas().size());

			Iterator itR = serverDeliverDoWorkResults.iterator();
			for (DeliveryData deliveryData : getTransferDatas()) {
				DeliveryResult deliverDoWorkServerResult = (DeliveryResult) itR.next();
				deliveryData.getDelivery().setDeliverDoWorkServerResult(deliverDoWorkServerResult);
			}
		} catch (DeliveryException x) {
			for (DeliveryData deliveryData : getTransferDatas())
				deliveryData.getDelivery().setDeliverDoWorkServerResult(x.getDeliveryResult());
		} catch (Throwable t) {
			DeliveryResult deliverDoWorkServerResult = new DeliveryResult(t);

			for (DeliveryData deliveryData : getTransferDatas())
				deliveryData.getDelivery().setDeliverDoWorkServerResult(deliverDoWorkServerResult);
		}
		setLastStageResults(serverDeliverDoWorkResults);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.transfer.TransferController#serverEnd()
	 */
	@Override
	public void serverEnd() {
		assertLastStage(Stage.ClientEnd);
		setLastStage(Stage.ServerEnd);
		
		if (isSkipServerStages())
			return;
		
		List<DeliveryResult> deliverEndServerResults = null;
		try {
//			for (Iterator itD = getTransferDatas().iterator(); itD.hasNext(); )
//				((DeliveryData)itD.next()).prepareUpload();
//			try {
			deliverEndServerResults = getStoreManager().deliverEnd(getTransferIDs(), getLastStageResults(), isForceRollback());
//			} finally {
//				for (Iterator itD = getTransferDatas().iterator(); itD.hasNext(); )
//					((DeliveryData)itD.next()).restoreAfterUpload();
//			}

			if (deliverEndServerResults.size() != getTransferDatas().size())
				throw new IllegalStateException("storeManager.deliverEnd(List, List, boolean) returned an invalid count of results! deliverEndServerResults.size()=" + deliverEndServerResults.size() + "; deliveryIDs.size()=" + getTransferIDs().size() + "; getTransferDatas().size()="+getTransferDatas().size());

			for (Iterator itD = getTransferDatas().iterator(), itR = deliverEndServerResults.iterator(); itD.hasNext(); ) {
				DeliveryData deliveryData = (DeliveryData) itD.next();
				DeliveryResult deliverEndServerResult = (DeliveryResult) itR.next();
				deliveryData.getDelivery().setDeliverEndServerResult(deliverEndServerResult);
			}
		} catch (DeliveryException x) {
			for (DeliveryData deliveryData : getTransferDatas())
				deliveryData.getDelivery().setDeliverEndServerResult(x.getDeliveryResult());
		} catch (Throwable t) {
			DeliveryResult deliverEndServerResult = new DeliveryResult(t);
			for (DeliveryData deliveryData : getTransferDatas())
				deliveryData.getDelivery().setDeliverEndServerResult(deliverEndServerResult);
		}
		
		setLastStageResults(deliverEndServerResults);
	}
}
