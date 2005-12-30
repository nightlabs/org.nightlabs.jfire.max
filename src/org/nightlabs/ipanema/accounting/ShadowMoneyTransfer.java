/*
 * Created on 29.10.2004
 */
package org.nightlabs.ipanema.accounting;

import org.nightlabs.ipanema.transfer.TransferRegistry;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.accounting.MoneyTransfer"
 *		detachable="true"
 *		table="JFireTrade_ShadowMoneyTransfer"
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class ShadowMoneyTransfer extends MoneyTransfer
{

	protected ShadowMoneyTransfer() { }

	/**
	 * BookMoneyTransfer associated to only one Invoice, used for invoice-bookings.
	 * 
	 * @param transferRegistry
	 * @param container
	 * @param initiator
	 * @param from
	 * @param to
	 * @param invoice
	 * @param currency
	 * @param amount
	 */
	public ShadowMoneyTransfer(TransferRegistry transferRegistry,
			InvoiceMoneyTransfer container, Account from, Account to, long amount)
	{
		super(transferRegistry, container, // container.getInitiator(), 
					from, to, // container.getInvoice(),
					amount);
	}

}
