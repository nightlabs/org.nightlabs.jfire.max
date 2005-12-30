/*
 * Created on Jun 15, 2005
 */
package org.nightlabs.jfire.accounting;

import java.util.Map;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.TransferRegistry;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.MoneyTransfer"
 *		detachable="true"
 *		table="JFireTrade_InvoiceMoneyTransfer"
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class InvoiceMoneyTransfer
extends MoneyTransfer
{
	public static final String BOOK_TYPE_BOOK = "book";
	public static final String BOOK_TYPE_PAY = "pay";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Invoice invoice;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String bookType;

	/**
	 * @deprecated Only for JDO! 
	 */
	protected InvoiceMoneyTransfer() { }

	private static Currency getCurrency(Invoice invoice)
	{		
		if (invoice == null)
			throw new IllegalArgumentException("invoice must not be null!");

		return invoice.getCurrency();
	}

	/**
	 * @param transferRegistry
	 * @param container
	 * @param initiator
	 * @param from
	 * @param to
	 * @param currency
	 * @param amount
	 */
	public InvoiceMoneyTransfer(
			String bookType, TransferRegistry transferRegistry,
			User initiator, Anchor from, Anchor to,
			Invoice invoice, long amount)
	{
		super(transferRegistry, null, initiator, from, to, getCurrency(invoice), amount);
		this.invoice = invoice;
		this.setBookType(bookType);
	}

	/**
	 * @param transferRegistry
	 * @param containerMoneyTransfer
	 * @param from
	 * @param to
	 * @param invoice
	 * @param amount
	 */
	public InvoiceMoneyTransfer(
			String bookType, TransferRegistry transferRegistry,
			MoneyTransfer containerMoneyTransfer, Anchor from, Anchor to,
			Invoice invoice, long amount)
	{
		super(transferRegistry, containerMoneyTransfer, from, to, amount);
		this.invoice = invoice;
		this.setBookType(bookType);
	}

	/**
	 * @return Returns the invoice.
	 */
	public Invoice getInvoice()
	{
		return invoice;
	}

	/**
	 * @param bookType The bookType to set.
	 */
	protected void setBookType(String bookType)
	{
		if (!BOOK_TYPE_BOOK.equals(bookType) &&
				!BOOK_TYPE_PAY.equals(bookType))
			throw new IllegalArgumentException("bookType \""+bookType+"\" is invalid! Must be BOOK_TYPE_BOOK or BOOK_TYPE_PAY!");

		this.bookType = bookType;
	}

	/**
	 * @return Returns the bookType.
	 */
	public String getBookType()
	{
		return bookType;
	}

	/**
	 * @see org.nightlabs.jfire.transfer.Transfer#bookTransfer(org.nightlabs.jfire.security.User, java.util.Map)
	 */
	public void bookTransfer(User user, Map involvedAnchors)
	{
		bookTransferAtInvoice(user, involvedAnchors);
		super.bookTransfer(user, involvedAnchors);
	}

	protected void bookTransferAtInvoice(User user, Map involvedAnchors)
	{
		if (BOOK_TYPE_PAY.equals(bookType))
			invoice.bookPayInvoiceMoneyTransfer(this, false);
	}


	/**
	 * @see org.nightlabs.jfire.transfer.Transfer#rollbackTransfer(org.nightlabs.jfire.security.User, java.util.Map)
	 */
	public void rollbackTransfer(User user, Map involvedAnchors)
	{
		rollbackTransferAtInvoice(user, involvedAnchors);
		super.rollbackTransfer(user, involvedAnchors);
	}

	protected void rollbackTransferAtInvoice(User user, Map involvedAnchors)
	{
		if (BOOK_TYPE_PAY.equals(bookType))
			invoice.bookPayInvoiceMoneyTransfer(this, true);
	}
}
