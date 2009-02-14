package org.nightlabs.jfire.accounting.pay;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.transfer.AbstractTransferController;

public abstract class AbstractPaymentController extends AbstractTransferController<PaymentData, PaymentID, PaymentResult> implements PaymentController {

	public AbstractPaymentController(List<PaymentData> transferDatas) {
		super(transferDatas, getPaymentIDs(transferDatas));
	}

	private static List<PaymentID> getPaymentIDs(List<PaymentData> transferDatas) {
		List<PaymentID> paymentIDs = new LinkedList<PaymentID>();
		for (PaymentData data : transferDatas)
			paymentIDs.add(PaymentID.create(data.getPayment().getOrganisationID(), data.getPayment().getPaymentID()));

		return paymentIDs;
	}


	@SuppressWarnings("unchecked")
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

			for (Iterator<?> itD = getTransferDatas().iterator(), itR = payBeginServerResults.iterator(); itD.hasNext(); ) {
				PaymentData paymentData = (PaymentData) itD.next();
				PaymentResult payBeginServerResult = (PaymentResult) itR.next();
				paymentData.getPayment().setPayBeginServerResult(payBeginServerResult);
			}
			// impossible to be thrown now since it does not inherit ModuleException anymore
//		} catch (PaymentException x) {
//			for (PaymentData paymentData : getTransferDatas())
//				paymentData.getPayment().setPayBeginServerResult(x.getPaymentResult());
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
			payDoWorkServerResults = getAccountingManager().payDoWork(getTransferIDs(), payDoWorkClientResults, isForceRollback());

			if (payDoWorkServerResults.size() != getTransferDatas().size())
				throw new IllegalStateException(
						"accountingManager.payDoWork(List, List, boolean) returned an invalid count of results! payDoWorkServerResults.size()="
								+ payDoWorkServerResults.size() + "; paymentIDs.size()=" + getTransferIDs().size() + "; getTransferDatas().size()=" + getTransferDatas().size());

			Iterator<PaymentResult> itR = payDoWorkServerResults.iterator();
			for (Iterator<?> itD = getTransferDatas().iterator(); itD.hasNext();) {
				PaymentData paymentData = (PaymentData) itD.next();
				PaymentResult payDoWorkServerResult = itR.next();
				paymentData.getPayment().setPayDoWorkServerResult(payDoWorkServerResult);
			}
			// impossible to be thrown now since it does not inherit ModuleException anymore
//		} catch (PaymentException x) {
//			for (Iterator<PaymentData> itD = getTransferDatas().iterator(); itD.hasNext();)
//				(itD.next()).getPayment().setPayDoWorkServerResult(x.getPaymentResult());
		} catch (Throwable t) {
			PaymentResult payDoWorkServerResult = new PaymentResult(SecurityReflector.getUserDescriptor().getOrganisationID(), t);
			for (PaymentData paymentData : getTransferDatas())
				paymentData.getPayment().setPayDoWorkServerResult(payDoWorkServerResult);
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
			payEndServerResults = getAccountingManager().payEnd(getTransferIDs(), getLastStageResults(), isForceRollback());

			if (payEndServerResults.size() != getTransferDatas().size())
				throw new IllegalStateException("accountingManager.payEnd(List, List, boolean) returned an invalid count of results! payEndServerResults.size()=" + payEndServerResults.size() + "; paymentIDs.size()=" + getTransferIDs().size() + "; getTransferDatas().size()="+getTransferDatas().size());

			for (Iterator<?> itD = getTransferDatas().iterator(), itR = payEndServerResults.iterator(); itD.hasNext(); ) {
				PaymentData paymentData = (PaymentData) itD.next();
				PaymentResult payEndServerResult = (PaymentResult) itR.next();
				paymentData.getPayment().setPayEndServerResult(payEndServerResult);
			}
			// impossible to be thrown now since it does not inherit ModuleException anymore
//		} catch (PaymentException x) {
//			for (PaymentData paymentData : getTransferDatas())
//				paymentData.getPayment().setPayEndServerResult(x.getPaymentResult());
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
		return JFireEjbFactory.getBean(AccountingManager.class, SecurityReflector.getInitialContextProperties());
	}
}
