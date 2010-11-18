package org.nightlabs.jfire.accounting.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.accounting.AccountingManagerRemote;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;


/**
 * 
 * @author Fitas Amine - fitas at NightLabs dot de
 */
public class PaymentDAO extends BaseJDOObjectDAO<PaymentID, Payment>
{
	private PaymentDAO() {}

	private static PaymentDAO sharedInstance = null;

	public static PaymentDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (PaymentDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new PaymentDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	protected Collection<? extends Payment> retrieveJDOObjects(
			Set<PaymentID> paymentIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor) throws Exception {
		AccountingManagerRemote am = getEjbProvider().getRemoteBean(AccountingManagerRemote.class);
		return CollectionUtil.castCollection(am.getPayments(paymentIDs, fetchGroups, maxFetchDepth));
	}

	public List<Payment> getPayments(Set<PaymentID> paymentIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, paymentIDs, fetchGroups, maxFetchDepth, monitor);
	}
	
	public List<Payment> getPaymentsForPayableObject(ObjectID payableObjectID,String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try{
			AccountingManagerRemote am = getEjbProvider().getRemoteBean(AccountingManagerRemote.class);
			Set<PaymentID> paymentIDs =am.getPaymentIDsForPayableObjectID(payableObjectID);
			return getJDOObjects(null, paymentIDs, fetchGroups, maxFetchDepth, monitor);
		}
		catch (Exception x) {
			monitor.setCanceled(true);
			throw new RuntimeException(x);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}
}
