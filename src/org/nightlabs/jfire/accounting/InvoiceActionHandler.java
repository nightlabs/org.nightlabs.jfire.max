package org.nightlabs.jfire.accounting;

import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentData;
import org.nightlabs.jfire.accounting.pay.PaymentException;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Utils;

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
	private String invoiceActionHandlerID;

	/**
	 * @deprecated Only for JDO!
	 */
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
	 * This method is called by {@link Accounting#bookInvoice(User, Invoice, boolean, boolean)} after
	 * all work is done.
	 *
	 * @param user The responsible user.
	 * @param invoice The invoice that is booked.
	 */
	public void onBook(User user, Invoice invoice)
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
	 * @throws PaymentException Throw this exception if sth. goes wrong with the payment.
	 */
	public void onPayDoWork(User user, PaymentData paymentData, Invoice invoice)
	throws PaymentException
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
	 * If you do risky things that might fail, you should better override {@link #onPayDoWork(User, PaymentData, Invoice)}! An exception
	 * at this stage will require manual clean-up by an operator!
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

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof InvoiceActionHandler))
			return false;

		InvoiceActionHandler o = (InvoiceActionHandler) obj;

		return
				Utils.equals(this.organisationID, o.organisationID) &&
				Utils.equals(this.invoiceActionHandlerID, o.invoiceActionHandlerID);
	}

	@Override
	public int hashCode()
	{
		return Utils.hashCode(organisationID) ^ Utils.hashCode(invoiceActionHandlerID);
	}
}
