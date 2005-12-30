/*
 * Created on Jun 8, 2005
 */
package org.nightlabs.jfire.accounting.pay;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.accounting.pay.id.PaymentDataID;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.Invocation;
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
 * @ejb.util generate = "physical"
 */
public abstract class PaymentHelperBean
extends BaseSessionBeanImpl
implements SessionBean
{
	public static final Logger LOGGER = Logger.getLogger(PaymentHelperBean.class);

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
			throws EJBException, RemoteException
	{
		LOGGER.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}
	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		LOGGER.debug(this.getClass().getName() + ".ejbCreate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		LOGGER.debug(this.getClass().getName() + ".ejbRemove()");
	}

	/**
	 * @param paymentData The <tt>PaymentData</tt> to be stored.
	 * @return Returns the JDO objectID of the newly persisted <tt>paymentData</tt>
	 * @throws ModuleException 
	 *
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public PaymentDataID payBegin_storePaymentData(PaymentData paymentData)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
//			if (!JDOHelper.isNew(paymentData))
//				throw new IllegalStateException("paymentData is not new! this method must be called with a brand new PaymentData object.");
//			if (JDOHelper.isDetached(paymentData))
//				paymentData = (PaymentData) pm.attachCopy(paymentData, false);
//			else

			paymentData.getPayment().initUser(User.getUser(pm, getPrincipal()));
			pm.makePersistent(paymentData);

			if (paymentData.getPayment().getPartner() == null) {
				String mandatorPK = Accounting.getAccounting(pm).getMandator().getPrimaryKey();
				Invoice invoice = (Invoice) paymentData.getPayment().getInvoices().iterator().next();

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


	/**
	 * @ejb.interface-method
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public PaymentResult payBegin_internal(
			PaymentDataID paymentDataID,
			String[] fetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			User user = User.getUser(pm, getPrincipal());

			pm.getExtent(PaymentData.class);
			PaymentData paymentData = (PaymentData) pm.getObjectById(paymentDataID);

//			Collection invoices = new HashSet(invoiceIDs.size());
//
//			// look up Invoice s for InvoiceID s
//			for (Iterator iter = invoiceIDs.iterator(); iter.hasNext();) {
//				InvoiceID invoiceID = (InvoiceID) iter.next();				
//				Invoice invoice = null;
//				try {
//					invoice = (Invoice)pm.getObjectById(invoiceID);
//				} catch (JDOObjectNotFoundException e) {
//					throw new ModuleException("Could not find an Invoice in datastore for invoiceID " + invoiceID, e);
//				}
//				invoices.add(invoice);
//			}
//
//			// get Currency for CurrencyID
//			pm.getExtent(Currency.class);
//			Currency currency = (Currency) pm.getObjectById(currencyID);
//
//			// get ModeOfPaymentFlavour for ModeOfPaymentFlavourID
//			pm.getExtent(ModeOfPaymentFlavour.class);
//			ModeOfPaymentFlavour modeOfPaymentFlavour = (ModeOfPaymentFlavour) pm.getObjectById(modeOfPaymentFlavourID);

			// delegate to Accounting
			PaymentResult payBeginServerResult = Accounting.getAccounting(pm).payBegin(
					user, paymentData);

			if (!JDOHelper.isPersistent(payBeginServerResult))
				pm.makePersistent(payBeginServerResult);
			paymentData.getPayment().setPayBeginServerResult(payBeginServerResult);
			
			pm.getFetchPlan().setGroups(fetchGroups);

			PaymentResult payBeginServerResult_detached = (PaymentResult) pm.detachCopy(payBeginServerResult);
//			payBeginServerResult_detached.setError(payBeginServerResult.getError());

			return payBeginServerResult_detached;
		} finally {
			pm.close();
		}

	}

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void payDoWork_storePayDoWorkClientResult(PaymentID paymentID, PaymentResult payDoWorkClientResult, boolean forceRollback)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
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
	
	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public PaymentResult payDoWork_internal(PaymentID paymentID, String[] fetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			PaymentDataID paymentDataID = PaymentDataID.create(paymentID);
			pm.getExtent(PaymentData.class);
			PaymentData paymentData = (PaymentData) pm.getObjectById(paymentDataID);

			User user = User.getUser(pm, getPrincipal());

			PaymentResult payDoWorkServerResult = Accounting.getAccounting(pm).payDoWork(
					user,
					paymentData
					);

			if (!JDOHelper.isPersistent(payDoWorkServerResult))
				pm.makePersistent(payDoWorkServerResult);
			paymentData.getPayment().setPayDoWorkServerResult(payDoWorkServerResult);

			pm.getFetchPlan().setGroups(fetchGroups);
			PaymentResult payDoWorkServerResult_detached = (PaymentResult) pm.detachCopy(payDoWorkServerResult);
//			payDoWorkServerResult_detached.setError(payDoWorkServerResult.getError());
			return payDoWorkServerResult_detached;
		} finally {
			pm.close();
		}
	}


	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public PaymentResult payEnd_internal(PaymentID paymentID, String[] fetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
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

			if (!JDOHelper.isPersistent(payEndServerResult))
				pm.makePersistent(payEndServerResult);
			paymentData.getPayment().setPayEndServerResult(payEndServerResult);
			// get InvoiceIDs
			Collection invoiceIDs = paymentData.getPayment().getInvoiceIDs();

			pm.getFetchPlan().setGroups(fetchGroups);
			PaymentResult payEndServerResult_detached = (PaymentResult) pm.detachCopy(payEndServerResult);
//			payBeginServerResult_detached.setError(payBeginServerResult.getError());


			// In case, they're not yet booked, we'll book the invoices asynchronously.
			// For performance reasons (we don't want the booking to block the payment), we do this here
			// and not in payBegin_xxx and delay the booking another 10 sec.
			try {
				new AsyncInvoke().exec(new BookInvoiceInvocation(invoiceIDs, 60000)); // TODO should be 10000
			} catch (Exception e) {
				throw new ModuleException(e);
			}

			return payEndServerResult_detached;
		} finally {
			pm.close();
		}
	}


	/**
	 * This invocation books all {@link Invoice}s specified by the given {@link InvoiceID}s
	 * in case they have not yet been booked. If an <code>Invoice</code> is already booked,
	 * it's silently ignored.
	 *
	 * @author Marco Schulze - marco at nightlabs dot de
	 */
	public static class BookInvoiceInvocation extends Invocation
	{
		protected static final Logger LOGGER = Logger.getLogger(BookInvoiceInvocation.class);

		private long createDT = System.currentTimeMillis();
		private Collection invoiceIDs;
		private long delayMSec;

		/**
		 * @param invoiceIDs Instances of {@link InvoiceID}. Must not be <code>null</code>. The
		 *		specified {@link Invoice}s can already be booked. Those which are already booked,
		 *		will be silently ignored.
		 * @param delayMSec Milliseconds (0 &lt; delayMSec &lt; 60000) which to wait before doing sth.
		 *		In case your main thread does sth. that manipulates invoices or accounts, you should delay
		 *		the booking to avoid performance problems (and dead-locks).
		 */
		public BookInvoiceInvocation(Collection invoiceIDs, long delayMSec)
		{
			if (invoiceIDs == null)
				throw new IllegalArgumentException("invoiceIDs must not be null!");

			if (delayMSec < 0)
				throw new IllegalArgumentException("delayMSec < 0!");

			if (delayMSec > 60000)
				throw new IllegalArgumentException("delayMSec > 60000!");

			this.invoiceIDs = invoiceIDs;
			this.delayMSec = delayMSec;

			LOGGER.info("Created BookInvoiceInvocation for " + invoiceIDs.size() + " invoices with "+delayMSec+" msec delay.");
		}

		public Serializable invoke() throws Exception
		{
			long wait = createDT + delayMSec - System.currentTimeMillis();
			if (wait > 0) {
				LOGGER.info("invoke() called: Waiting " + wait + " msec before starting to book.");
				try { Thread.sleep(wait); } catch (InterruptedException x) { }
			}

			PersistenceManager pm = getPersistenceManager();
			try {
				pm.getExtent(Invoice.class);
				User user = null;
				for (Iterator it = invoiceIDs.iterator(); it.hasNext(); ) {
					InvoiceID invoiceID = (InvoiceID) it.next();
					Invoice invoice = (Invoice) pm.getObjectById(invoiceID);
					if (!invoice.getInvoiceLocal().isBooked()) {
						LOGGER.info("Booking invoice: " + invoice.getPrimaryKey());

						if (user == null)
							user = User.getUser(pm, getPrincipal());

						Accounting.getAccounting(pm).bookInvoice(user, invoice, true, false);
					}
					else
						LOGGER.info("Invoice " + invoice.getPrimaryKey() + " is already booked! Ignoring.");
				}

			} finally {
				pm.close();
			}
			return null;
		}
	}


	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void payEnd_storePayEndClientResult(PaymentID paymentID, PaymentResult payEndClientResult, boolean forceRollback)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
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

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public PaymentResult payBegin_storePayBeginServerResult(
			PaymentID paymentID, PaymentResult payBeginServerResult,
			boolean get, String[] fetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Payment.class);
			Payment payment = (Payment) pm.getObjectById(paymentID);

			if (JDOHelper.isDetached(payBeginServerResult))
				payBeginServerResult = (PaymentResult) pm.attachCopy(payBeginServerResult, false);
			else
				pm.makePersistent(payBeginServerResult);

//			PaymentResult old = payment.getPayBeginServerResult();
//			if (old != null) {
//				payment.setPayBeginServerResult(null);
//			}
			payment.setPayBeginServerResult(payBeginServerResult);

			if (!get)
				return null;

			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (PaymentResult) pm.detachCopy(payBeginServerResult);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public PaymentResult payDoWork_storePayDoWorkServerResult(
			PaymentID paymentID, PaymentResult payDoWorkServerResult,
			boolean get, String[] fetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Payment.class);
			Payment payment = (Payment) pm.getObjectById(paymentID);

			if (JDOHelper.isDetached(payDoWorkServerResult))
				payDoWorkServerResult = (PaymentResult) pm.attachCopy(payDoWorkServerResult, false);
			else
				pm.makePersistent(payDoWorkServerResult);

			payment.setPayDoWorkServerResult(payDoWorkServerResult);

			if (!get)
				return null;

			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (PaymentResult) pm.detachCopy(payDoWorkServerResult);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public PaymentResult payEnd_storePayEndServerResult(
			PaymentID paymentID, PaymentResult payEndServerResult,
			boolean get, String[] fetchGroups)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getExtent(Payment.class);
			Payment payment = (Payment) pm.getObjectById(paymentID);

			if (JDOHelper.isDetached(payEndServerResult))
				payEndServerResult = (PaymentResult) pm.attachCopy(payEndServerResult, false);
			else
				pm.makePersistent(payEndServerResult);

			payment.setPayEndServerResult(payEndServerResult);

			if (!get)
				return null;

			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return (PaymentResult) pm.detachCopy(payEndServerResult);
		} finally {
			pm.close();
		}
	}


	/**
	 * @ejb.interface-method view-type="local"
	 * @ejb.transaction type = "RequiresNew"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void payRollback(PaymentID paymentID)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
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
