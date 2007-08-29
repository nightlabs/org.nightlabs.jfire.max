package org.nightlabs.jfire.accounting.pay;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.AccountingManagerUtil;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.transfer.AbstractTransferController;

public abstract class AbstractPaymentController extends AbstractTransferController<PaymentData, PaymentID, PaymentResult> implements PaymentController {

	@Override
	protected void _serverBegin() {
		if (isSkipServerStages())
			return;
		
		List<PaymentResult> payBeginServerResults = null;
		try {
			for (PaymentData paymentData : getTransferDatas())
				paymentData.prepareUpload();

			try {
				payBeginServerResults = getAccountingManager().payBegin(getTransferDatasForServer());
			} finally {
				for (PaymentData paymentData : getTransferDatas())
					paymentData.restoreAfterUpload();
			}

			if (payBeginServerResults.size() != getTransferDatas().size())
				throw new IllegalStateException("accountingManager.payBegin(List) returned an invalid count of results! payBeginServerResults.size()=" + payBeginServerResults.size() + "; getTransferDatas().size()="+getTransferDatas().size());
			
			for (Iterator itD = getTransferDatas().iterator(), itR = payBeginServerResults.iterator(); itD.hasNext(); ) {
				PaymentData paymentData = (PaymentData) itD.next();
				PaymentResult payBeginServerResult = (PaymentResult) itR.next();
				paymentData.getPayment().setPayBeginServerResult(payBeginServerResult);
			}
		} catch (PaymentException x) {
			for (PaymentData paymentData : getTransferDatas())
				paymentData.getPayment().setPayBeginServerResult(x.getPaymentResult());
		} catch (Throwable t) {
			PaymentResult payBeginServerResult = new PaymentResult(SecurityReflector.getUserDescriptor().getOrganisationID(), t);
			for (PaymentData paymentData : getTransferDatas())
				paymentData.getPayment().setPayBeginServerResult(payBeginServerResult);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.transfer.TransferController#serverDoWork()
	 */
	@Override
	protected void _serverDoWork() {
		if (isSkipServerStages())
			return;
		
		List<PaymentResult> payDoWorkServerResults = null;
		try {
			List<PaymentResult> payDoWorkClientResults = getLastStageResults();
			//			for (Iterator itD = getTransferDatas().iterator(); itD.hasNext(); )
			//				((PaymentData)itD.next()).prepareUpload();
			//			try {
			payDoWorkServerResults = getAccountingManager().payDoWork(getTransferIDs(), payDoWorkClientResults, isForceRollback());
			//			} finally {
			//				for (Iterator itD = getTransferDatas().iterator(); itD.hasNext(); )
			//					((PaymentData)itD.next()).restoreAfterUpload();
			//			}

			if (payDoWorkServerResults.size() != getTransferDatas().size())
				throw new IllegalStateException(
						"accountingManager.payDoWork(List, List, boolean) returned an invalid count of results! payDoWorkServerResults.size()="
								+ payDoWorkServerResults.size() + "; paymentIDs.size()=" + getTransferIDs().size() + "; getTransferDatas().size()=" + getTransferDatas().size());

			Iterator itR = payDoWorkServerResults.iterator();
			for (Iterator itD = getTransferDatas().iterator(); itD.hasNext();) {
				PaymentData paymentData = (PaymentData) itD.next();
				PaymentResult payDoWorkServerResult = (PaymentResult) itR.next();
				paymentData.getPayment().setPayDoWorkServerResult(payDoWorkServerResult);
			}
		} catch (PaymentException x) {
			for (Iterator itD = getTransferDatas().iterator(); itD.hasNext();)
				((PaymentData) itD.next()).getPayment().setPayDoWorkServerResult(x.getPaymentResult());
		} catch (Throwable t) {
			PaymentResult payDoWorkServerResult = new PaymentResult(SecurityReflector.getUserDescriptor().getOrganisationID(), t);

			for (Iterator itD = getTransferDatas().iterator(); itD.hasNext();)
				((PaymentData) itD.next()).getPayment().setPayDoWorkServerResult(payDoWorkServerResult);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.transfer.TransferController#serverEnd()
	 */
	@Override
	protected void _serverEnd() {
		if (isSkipServerStages())
			return;
		
		List<PaymentResult> payEndServerResults = null;
		try {
//			for (Iterator itD = getTransferDatas().iterator(); itD.hasNext(); )
//				((PaymentData)itD.next()).prepareUpload();
//			try {
				payEndServerResults = getAccountingManager().payEnd(getTransferIDs(), getLastStageResults(), isForceRollback());
//			} finally {
//				for (Iterator itD = getTransferDatas().iterator(); itD.hasNext(); )
//					((PaymentData)itD.next()).restoreAfterUpload();
//			}

			if (payEndServerResults.size() != getTransferDatas().size())
				throw new IllegalStateException("accountingManager.payEnd(List, List, boolean) returned an invalid count of results! payEndServerResults.size()=" + payEndServerResults.size() + "; paymentIDs.size()=" + getTransferIDs().size() + "; getTransferDatas().size()="+getTransferDatas().size());

			for (Iterator itD = getTransferDatas().iterator(), itR = payEndServerResults.iterator(); itD.hasNext(); ) {
				PaymentData paymentData = (PaymentData) itD.next();
				PaymentResult payEndServerResult = (PaymentResult) itR.next();
				paymentData.getPayment().setPayEndServerResult(payEndServerResult);
			}
		} catch (PaymentException x) {
			for (PaymentData paymentData : getTransferDatas())
				paymentData.getPayment().setPayEndServerResult(x.getPaymentResult());
		} catch (Throwable t) {
			PaymentResult payEndServerResult = new PaymentResult(SecurityReflector.getUserDescriptor().getOrganisationID(), t);
			for (PaymentData paymentData : getTransferDatas())
				paymentData.getPayment().setPayEndServerResult(payEndServerResult);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.trade.transfer.TransferController#isRollbackRequired()
	 */
	@Override
	public boolean isRollbackRequired() {
		if (isForceRollback())
			return true;
		
		for (PaymentData paymentData : getTransferDatas()) {
			Payment payment = paymentData.getPayment();
			if (payment.isFailed() || payment.isForceRollback())
				return true;
		}
		
		return false;
	}
	
	@Override
	public AccountingManager getAccountingManager() throws RemoteException, LoginException, CreateException, NamingException {
		return AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
	}
}
