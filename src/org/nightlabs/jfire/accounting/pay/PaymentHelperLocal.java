package org.nightlabs.jfire.accounting.pay;

import javax.annotation.security.RolesAllowed;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.pay.id.PaymentDataID;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;

public interface PaymentHelperLocal {

	/**
	 * @param paymentData The <tt>PaymentData</tt> to be stored.
	 * @return Returns the JDO objectID of the newly persisted <tt>paymentData</tt>
	 * @throws ModuleException
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	PaymentDataID payBegin_storePaymentData(PaymentData paymentData)
			throws ModuleException;

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	PaymentResult payBegin_internal(PaymentDataID paymentDataID,
			String[] fetchGroups, int maxFetchDepth) throws PaymentException;

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	void payDoWork_storePayDoWorkClientResult(PaymentID paymentID,
			PaymentResult payDoWorkClientResult, boolean forceRollback)
			throws ModuleException;

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	PaymentResult payDoWork_internal(PaymentID paymentID, String[] fetchGroups,
			int maxFetchDepth) throws PaymentException;

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	PaymentResult payEnd_internal(PaymentID paymentID, String[] fetchGroups,
			int maxFetchDepth) throws PaymentException;

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	void payEnd_storePayEndClientResult(PaymentID paymentID,
			PaymentResult payEndClientResult, boolean forceRollback)
			throws ModuleException;

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	PaymentResult payBegin_storePayBeginServerResult(PaymentID paymentID,
			PaymentResult payBeginServerResult, boolean get,
			String[] fetchGroups, int maxFetchDepth) throws ModuleException;

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	PaymentResult payDoWork_storePayDoWorkServerResult(PaymentID paymentID,
			PaymentResult payDoWorkServerResult, boolean get,
			String[] fetchGroups, int maxFetchDepth) throws ModuleException;

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	PaymentResult payEnd_storePayEndServerResult(PaymentID paymentID,
			PaymentResult payEndServerResult, boolean get,
			String[] fetchGroups, int maxFetchDepth) throws ModuleException;

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type="RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed("_Guest_")
	void payRollback(PaymentID paymentID) throws ModuleException;

}