/*
 * Created on Oct 30, 2005
 */
package org.nightlabs.jfire.accounting;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.jfire.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.InvoiceLocalID"
 *		detachable="true"
 *		table="JFireTrade_InvoiceLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, invoiceID"
 *
 * @jdo.fetch-group name="Invoice.invoiceLocal" fields="invoice"
 * @jdo.fetch-group name="InvoiceLocal.invoice" fields="invoice"
 * @jdo.fetch-group name="InvoiceLocal.bookUser" fields="bookUser"
 * @jdo.fetch-group name="InvoiceLocal.this" fields="invoice, bookUser"
 */
public class InvoiceLocal
implements Serializable
{
	public static final String FETCH_GROUP_INVOICE = "InvoiceLocal.invoice";
	public static final String FETCH_GROUP_BOOK_USER = "InvoiceLocal.bookUser";
	public static final String FETCH_GROUP_THIS_INVOICE_LOCAL = "InvoiceLocal.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long invoiceID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Invoice invoice;

	/**
	 * This member represents the amount already paid.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long amountPaid;

	/**
	 * This flag is true as long as the vendor still hopes to get the money. This means,
	 * it is set false automatically when paid becomes true or when the vendor gives up
	 * - e.g. because the customer is bankrupt.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean outstanding = true;

	/**
	 * This member stores the user who booked this Invoice.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User bookUser = null;

	/**
	 * This member stores when this Invoice was booked.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date bookDT  = null;

	/**
	 * @deprecated Only for JDO!
	 */
	protected InvoiceLocal() { }

	public InvoiceLocal(Invoice invoice)
	{
		this.organisationID = invoice.getOrganisationID();
		this.invoiceID = invoice.getInvoiceID();
		this.invoice = invoice;

		invoice.setInvoiceLocal(this);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public long getInvoiceID()
	{
		return invoiceID;
	}
	public Invoice getInvoice()
	{
		return invoice;
	}

	protected void incAmountPaid(long diffAmount)
	{
		this.amountPaid = amountPaid + diffAmount;
	}

	public long getAmountPaid() {
		return amountPaid;
	}
	public long getAmountToPay() {
		return getInvoice().getPrice().getAmount() - getAmountPaid();
	}

	public boolean isOutstanding() {
		return outstanding;
	}
	public void setOutstanding(boolean outstanding)
	{
		if (this.outstanding != outstanding)
			this.outstanding = outstanding;
	}

	protected void setBooked(User bookUser) {
		if (this.bookDT != null)
			return;

		this.bookUser = bookUser;
		this.bookDT = new Date();
	}
	public User getBookUser() {
		return bookUser;
	}
	public Date getBookDT() {
		return bookDT;
	}
	/**
	 * This member is set to true as soon as the money is booked on the various
	 * accounts.
	 */
	public boolean isBooked() {
		return bookDT != null;
	}
}
