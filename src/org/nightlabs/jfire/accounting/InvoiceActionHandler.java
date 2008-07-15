package org.nightlabs.jfire.accounting;

import java.io.Serializable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.accounting.pay.PaymentException;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.InvoiceActionHandlerID"
 *		detachable="true"
 *		table="JFireTrade_InvoiceActionHandler"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, invoiceActionHandlerID"
 */
public class InvoiceActionHandler
implements Serializable
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String invoiceActionHandlerID; // TODO: Tobias: Why is this field a string? Shouldn't it rather be a long so that it can be generated using the IDGenerator?

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected InvoiceActionHandler() { }

	public InvoiceActionHandler(String organisationID, String invoiceActionHandlerID)
	{
		this.organisationID = organisationID;
		this.invoiceActionHandlerID = invoiceActionHandlerID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getInvoiceActionHandlerID()
	{
		return invoiceActionHandlerID;
	}

	/**
	 * This method is called by {@link Accounting#onBookInvoice(User, Invoice)} after
	 * all work is done.
	 *
	 * @param user The responsible user.
	 * @param invoice The invoice that is booked.
	 */
	public void onBook(User user, Invoice invoice)
	{
	}

	/**
	 * This method is called by {@link Accounting#payBegin(User, PaymentData)} after the {@link ServerPaymentProcessor} has
	 * been triggered.
	 *
	 * @param user The responsible user.
	 * @param paymentData The payment-data which allows to access the {@link Payment}.
	 * @param invoice The invoice that is paid. Note that multiple <code>InvoiceActionHandler</code>s might be called for multiple <code>Invoice</code>s
	 * 		for <strong>the same</strong> {@link PaymentData}, because one payment can comprise many invoices.
	 * @throws Exception If sth. goes wrong. It will be wrapped inside a PaymentException
	 * @throws PaymentException If you throw a PaymentException directly, it won't be wrapped.
	 */
	public void onPayBegin(User user, PaymentData paymentData, Invoice invoice)
	throws Exception, PaymentException
	{
	}

	/**
	 * This method is called by {@link Accounting#payDoWork(User, PaymentData)} after the {@link ServerPaymentProcessor} has
	 * been triggered.
	 *
	 * @param user The responsible user.
	 * @param paymentData The payment-data which allows to access the {@link Payment}.
	 * @param invoice The invoice that is paid. Note that multiple <code>InvoiceActionHandler</code>s might be called for multiple <code>Invoice</code>s
	 * 		for <strong>the same</strong> {@link PaymentData}, because one payment can comprise many invoices.
	 * @throws Exception If sth. goes wrong. It will be wrapped inside a PaymentException
	 * @throws PaymentException If you throw a PaymentException directly, it won't be wrapped.
	 */
	public void onPayDoWork(User user, PaymentData paymentData, Invoice invoice)
	throws Exception, PaymentException
	{
	}

	/**
	 * This method is called by {@link Accounting#payEnd(User, PaymentData)} after all has been done successfully.
	 * <p>
	 * <b>Note that this method is called, too, if the payment has been postponed.</b> Thus, you should check
	 * {@link Payment#isPostponed()}!
	 * </p>
	 * <p>
	 * You should try to avoid throwing an Exception here, because it is too late for a roll-back in an external payment system!
	 * If you do risky things that might fail, you should better override {@link #onPayDoWork(User, PaymentData, Invoice)} and do them
	 * there! The best solution, is to ensure already in {@link #onPayBegin(User, PaymentData, Invoice)} that a payment will succeed.
	 * </p>
	 * <p>
	 * An exception at this stage (i.e. thrown by this method) will require manual clean-up by an operator!
	 * </p>
	 *
	 * @param user The responsible user.
	 * @param paymentData The payment-data which allows to access the {@link Payment}.
	 * @param invoice The invoice that is paid. Note that multiple <code>InvoiceActionHandler</code>s might be called for multiple <code>Invoice</code>s
	 * 		for <strong>the same</strong> {@link PaymentData}, because one payment can comprise many invoices.
	 */
	public void onPayEnd(User user, PaymentData paymentData, Invoice invoice)
	{
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not yet persistent or currently not attached to a datastore! Cannot obtain PersistenceManager!");

		return pm;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof InvoiceActionHandler))
			return false;

		InvoiceActionHandler o = (InvoiceActionHandler) obj;

		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.invoiceActionHandlerID, o.invoiceActionHandlerID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) ^ Util.hashCode(invoiceActionHandlerID);
	}
}
