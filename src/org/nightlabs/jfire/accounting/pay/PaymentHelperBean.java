/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.accounting.pay;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.pay.id.PaymentDataID;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @ejb.bean name="jfire/ejb/JFireTrade/PaymentHelper"
 *           jndi-name="jfire/ejb/JFireTrade/PaymentHelper"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class PaymentHelperBean
extends BaseSessionBeanImpl
implements PaymentHelperLocal
{
	private static final long serialVersionUID = 1L;

//	private static final Logger logger = Logger.getLogger(PaymentHelperBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.pay.PaymentHelperLocal#payBegin_storePaymentData(org.nightlabs.jfire.accounting.pay.PaymentData)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public PaymentDataID payBegin_storePaymentData(PaymentData paymentData)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
//			if (!JDOHelper.isNew(paymentData))
//				throw new IllegalStateException("paymentData is not new! this method must be called with a brand new PaymentData object.");
//			if (JDOHelper.isDetached(paymentData))
//				paymentData = (PaymentData) pm.attachCopy(paymentData, false);
//			else

			// PaymentLocal registers itself with Payment
			new PaymentLocal(paymentData.getPayment());

			paymentData.getPayment().initUser(User.getUser(pm, getPrincipal()));
			paymentData = pm.makePersistent(paymentData);

			if (paymentData.getPayment().getPartner() == null) {
				String mandatorPK = Accounting.getAccounting(pm).getMandator().getPrimaryKey();
				Invoice invoice = paymentData.getPayment().getInvoices().iterator().next();

				LegalEntity partner = invoice.getCustomer();
				if (mandatorPK.equals(partner.getPrimaryKey()))
					partner = invoice.getVendor();

				paymentData.getPayment().setPartner(partner);
			}

			return (PaymentDataID) JDOHelper.getObjectId(paymentData);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.pay.PaymentHelperLocal#payBegin_internal(org.nightlabs.jfire.accounting.pay.id.PaymentDataID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public PaymentResult payBegin_internal(
			PaymentDataID paymentDataID,
			String[] fetchGroups, int maxFetchDepth)
	throws PaymentException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());

			pm.getExtent(PaymentData.class);
			PaymentData paymentData = (PaymentData) pm.getObjectById(paymentDataID);

			// delegate to Accounting
			PaymentResult payBeginServerResult = Accounting.getAccounting(pm).payBegin(
					user, paymentData);

//			if (!JDOHelper.isPersistent(payBeginServerResult))
//				payBeginServerResult = pm.makePersistent(payBeginServerResult);
			payBeginServerResult = pm.makePersistent(payBeginServerResult);
			paymentData.getPayment().setPayBeginServerResult(payBeginServerResult);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			PaymentResult payBeginServerResult_detached = pm.detachCopy(payBeginServerResult);
//			payBeginServerResult_detached.setError(payBeginServerResult.getError());

			return payBeginServerResult_detached;
		} finally {
			pm.close();
		}

	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.pay.PaymentHelperLocal#payDoWork_storePayDoWorkClientResult(org.nightlabs.jfire.accounting.pay.id.PaymentID, org.nightlabs.jfire.accounting.pay.PaymentResult, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public void payDoWork_storePayDoWorkClientResult(PaymentID paymentID, PaymentResult payDoWorkClientResult, boolean forceRollback)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(Payment.class);
			Payment payment = (Payment) pm.getObjectById(paymentID);
			payment.setPayDoWorkClientResult(payDoWorkClientResult);
			if (forceRollback)
				payment.setForceRollback();
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.pay.PaymentHelperLocal#payDoWork_internal(org.nightlabs.jfire.accounting.pay.id.PaymentID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public PaymentResult payDoWork_internal(PaymentID paymentID, String[] fetchGroups, int maxFetchDepth)
	throws PaymentException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			PaymentDataID paymentDataID = PaymentDataID.create(paymentID);
			pm.getExtent(PaymentData.class);
			PaymentData paymentData = (PaymentData) pm.getObjectById(paymentDataID);

			User user = User.getUser(pm, getPrincipal());

			PaymentResult payDoWorkServerResult = Accounting.getAccounting(pm).payDoWork(
					user,
					paymentData
					);

//			if (!JDOHelper.isPersistent(payDoWorkServerResult))
//				payDoWorkServerResult = pm.makePersistent(payDoWorkServerResult);
			payDoWorkServerResult = pm.makePersistent(payDoWorkServerResult);
			paymentData.getPayment().setPayDoWorkServerResult(payDoWorkServerResult);

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			PaymentResult payDoWorkServerResult_detached = pm.detachCopy(payDoWorkServerResult);
//			payDoWorkServerResult_detached.setError(payDoWorkServerResult.getError());
			return payDoWorkServerResult_detached;
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.pay.PaymentHelperLocal#payEnd_internal(org.nightlabs.jfire.accounting.pay.id.PaymentID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public PaymentResult payEnd_internal(PaymentID paymentID, String[] fetchGroups, int maxFetchDepth)
	throws PaymentException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			PaymentDataID paymentDataID = PaymentDataID.create(paymentID);
			pm.getExtent(PaymentData.class);
			PaymentData paymentData = (PaymentData) pm.getObjectById(paymentDataID);

			User user = User.getUser(pm, getPrincipal());

			PaymentResult payEndServerResult = Accounting.getAccounting(pm).payEnd(
					user,
					paymentData
					);

			if (paymentData.getPayment().isFailed()) {
				Accounting.getAccounting(pm).payRollback(user, paymentData);
			}

//			if (!JDOHelper.isPersistent(payEndServerResult))
//				payEndServerResult = pm.makePersistent(payEndServerResult);

			payEndServerResult = pm.makePersistent(payEndServerResult);
			paymentData.getPayment().setPayEndServerResult(payEndServerResult);
			paymentData.getPayment().getInvoiceIDs();

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			PaymentResult payEndServerResult_detached = pm.detachCopy(payEndServerResult);
//			payBeginServerResult_detached.setError(payBeginServerResult.getError());

			return payEndServerResult_detached;
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.pay.PaymentHelperLocal#payEnd_storePayEndClientResult(org.nightlabs.jfire.accounting.pay.id.PaymentID, org.nightlabs.jfire.accounting.pay.PaymentResult, boolean)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public void payEnd_storePayEndClientResult(PaymentID paymentID, PaymentResult payEndClientResult, boolean forceRollback)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(Payment.class);
			Payment payment = (Payment) pm.getObjectById(paymentID);
			payment.setPayEndClientResult(payEndClientResult);
			if (forceRollback)
				payment.setForceRollback();
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.pay.PaymentHelperLocal#payBegin_storePayBeginServerResult(org.nightlabs.jfire.accounting.pay.id.PaymentID, org.nightlabs.jfire.accounting.pay.PaymentResult, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public PaymentResult payBegin_storePayBeginServerResult(
			PaymentID paymentID, PaymentResult payBeginServerResult,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(Payment.class);
			Payment payment = (Payment) pm.getObjectById(paymentID);

			payBeginServerResult = pm.makePersistent(payBeginServerResult);

//			PaymentResult old = payment.getPayBeginServerResult();
//			if (old != null) {
//				payment.setPayBeginServerResult(null);
//			}
			payment.setPayBeginServerResult(payBeginServerResult);

//			// trigger the ProductTypeActionHandler s
//			Map<Class, Set<Article>> productTypeClass2articleSet = Article.getProductTypeClass2articleSetMapFromArticleContainers(payment.getInvoices());
//			for (Map.Entry<Class, Set<Article>> me : productTypeClass2articleSet.entrySet()) {
//				ProductTypeActionHandler.getProductTypeActionHandler(pm, me.getKey()).onPayBegin_storePayBeginServerResult(getPrincipal(), payment, me.getValue());
//			}

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(payBeginServerResult);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.pay.PaymentHelperLocal#payDoWork_storePayDoWorkServerResult(org.nightlabs.jfire.accounting.pay.id.PaymentID, org.nightlabs.jfire.accounting.pay.PaymentResult, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public PaymentResult payDoWork_storePayDoWorkServerResult(
			PaymentID paymentID, PaymentResult payDoWorkServerResult,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(Payment.class);
			Payment payment = (Payment) pm.getObjectById(paymentID);

			payDoWorkServerResult = pm.makePersistent(payDoWorkServerResult);

			payment.setPayDoWorkServerResult(payDoWorkServerResult);

//			// trigger the ProductTypeActionHandler s
//			Map<Class, Set<Article>> productTypeClass2articleSet = Article.getProductTypeClass2articleSetMapFromArticleContainers(payment.getInvoices());
//			for (Map.Entry<Class, Set<Article>> me : productTypeClass2articleSet.entrySet()) {
//				ProductTypeActionHandler.getProductTypeActionHandler(pm, me.getKey()).onPayDoWork_storePayDoWorkServerResult(getPrincipal(), payment, me.getValue());
//			}

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(payDoWorkServerResult);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.pay.PaymentHelperLocal#payEnd_storePayEndServerResult(org.nightlabs.jfire.accounting.pay.id.PaymentID, org.nightlabs.jfire.accounting.pay.PaymentResult, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public PaymentResult payEnd_storePayEndServerResult(
			PaymentID paymentID, PaymentResult payEndServerResult,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(Payment.class);
			Payment payment = (Payment) pm.getObjectById(paymentID);

			payEndServerResult = pm.makePersistent(payEndServerResult);

			payment.setPayEndServerResult(payEndServerResult);

//			// trigger the ProductTypeActionHandler s
//			Map<Class, Set<Article>> productTypeClass2articleSet = Article.getProductTypeClass2articleSetMapFromArticleContainers(payment.getInvoices());
//			for (Map.Entry<Class, Set<Article>> me : productTypeClass2articleSet.entrySet()) {
//				ProductTypeActionHandler.getProductTypeActionHandler(pm, me.getKey()).onPayEnd_storePayEndServerResult(getPrincipal(), payment, me.getValue());
//			}

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(payEndServerResult);
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.accounting.pay.PaymentHelperLocal#payRollback(org.nightlabs.jfire.accounting.pay.id.PaymentID)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	public void payRollback(PaymentID paymentID)
	throws ModuleException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getExtent(Payment.class);
			Payment payment = (Payment) pm.getObjectById(paymentID);

			User user = User.getUser(pm, getPrincipal());
			pm.getExtent(PaymentData.class);
			PaymentData paymentData = (PaymentData) pm.getObjectById(PaymentDataID.create(paymentID));
			Accounting.getAccounting(pm).payRollback(user, paymentData);
		} finally {
			pm.close();
		}
	}

}
