/*
 * Created on 29.10.2004
 */
package org.nightlabs.jfire.accounting.book;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceMoneyTransfer;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.TransferRegistry;

/**
 * An <tt>BookMoneyTransfer</tt> is a <tt>MoneyTransfer</tt> which
 * happens between two LegalEntities.
 * This means, <tt>from</tt> and <tt>to</tt> must be instances of <tt>LegalEntity</tt>.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.InvoiceMoneyTransfer"
 *		detachable="true"
 *		table="JFireTrade_BookMoneyTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class BookMoneyTransfer extends InvoiceMoneyTransfer
{

	/**
	 * @deprecated Only of JDO!
	 */
	protected BookMoneyTransfer() { }

	private static long getAmountAbsoluteValue(Invoice invoice)
	{		
		if (invoice == null)
			throw new IllegalArgumentException("invoice must not be null!");

		return invoice.getPrice().getAmountAbsoluteValue();
	}

	/**
	 * BookMoneyTransfer associated to only one Invoice, used for invoice-bookings.
	 *
	 * @param transferRegistry
	 * @param initiator
	 * @param from
	 * @param to
	 * @param invoice
	 * @param amount
	 */
	public BookMoneyTransfer(TransferRegistry transferRegistry, User initiator, Anchor from, Anchor to, Invoice invoice)
	{
		super(BOOK_TYPE_BOOK, transferRegistry, initiator, from, to, invoice, getAmountAbsoluteValue(invoice));

		if (!(from instanceof LegalEntity))
			throw new IllegalArgumentException("from must be an instance of LegalEntity, but is of type " + from.getClass().getName());

		if (!(to instanceof LegalEntity))
			throw new IllegalArgumentException("to must be an instance of LegalEntity, but is of type " + from.getClass().getName());
	}

}
