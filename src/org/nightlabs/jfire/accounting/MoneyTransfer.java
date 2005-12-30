/*
 * Created on 26.10.2004
 */
package org.nightlabs.jfire.accounting;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.TransferRegistry;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.transfer.Transfer"
 *		detachable="true"
 *		table="JFireTrade_MoneyTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class MoneyTransfer extends Transfer
{
	public static final String TRANSFERTYPEID = "MoneyTransfer";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Currency currency;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long amount;

//	private boolean statistical = false; // necessary?!

	protected MoneyTransfer() {
//		this.invoices = new ArrayList();
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
	public MoneyTransfer(TransferRegistry transferRegistry, Transfer container,
			User initiator, Anchor from, Anchor to, Currency currency, long amount)
	{
		super(transferRegistry, TRANSFERTYPEID, container, initiator, from, to);

		if (currency == null)
			throw new NullPointerException("currency must not be null!");
		
		if (amount < 0)
			throw new IllegalArgumentException("amount must be positive! Switch from & to to inverse the direction!");

		this.currency = currency;
		this.amount = amount;

		if (from instanceof Account) {
			Account fromAccount = (Account)from;
			String fromCurrencyID = fromAccount.getCurrency().getCurrencyID();
			if (!currency.getCurrencyID().equals(fromCurrencyID))
				throw new IllegalArgumentException("currency mismatch! From-account \""+from.getPrimaryKey()+"\" has currency \""+fromCurrencyID+"\", but given currency is \""+currency.getCurrencyID()+"\"!");
		}

		if (to instanceof Account) {
			Account toAccount = (Account)to;
			String toCurrencyID = toAccount.getCurrency().getCurrencyID();
			if (!currency.getCurrencyID().equals(toCurrencyID))
				throw new IllegalArgumentException("currency mismatch! To-account \""+to.getPrimaryKey()+"\" has currency \""+toCurrencyID+"\", but given currency is \""+currency.getCurrencyID()+"\"!");
		}
	}

//	protected static Collection getSingleInvoiceCollection(Invoice invoice)
//	{
//		ArrayList res = new ArrayList(1);
//		res.add(invoice);
//		return res;
//	}

	/** 
	 * Used to create a MoneyTransfer accosiated to
	 * the (first) invoice of containerMoneyTransfer. 
	 * 
	 * @param transferRegistry
	 * @param container
	 * @param from
	 * @param to
	 * @param localLegalEntity
	 * @param interLegalEntityMoneyTransfer
	 * @param currency
	 * @param amount
	 */
	public MoneyTransfer(TransferRegistry transferRegistry,
			MoneyTransfer containerMoneyTransfer, Anchor from, Anchor to, 
			long amount)
	{
		this(transferRegistry, containerMoneyTransfer, containerMoneyTransfer.getInitiator(),
				from, to,
				containerMoneyTransfer.getCurrency(),
				amount);
	}

//	/** 
//	 * Used to assosiate a MoneyTransfer 
//	 * to the given invoice. 
//	 * 
//	 * @param transferRegistry
//	 * @param container
//	 * @param from
//	 * @param to
//	 * @param localLegalEntity
//	 * @param interLegalEntityMoneyTransfer
//	 * @param currency
//	 * @param amount
//	 */
//	public MoneyTransfer(TransferRegistry transferRegistry,
//			MoneyTransfer containerMoneyTransfer, Anchor from, Anchor to, 
//			long amount)
//	{
//		this(transferRegistry, containerMoneyTransfer, containerMoneyTransfer.getInitiator(),
//				from, to,
//				containerMoneyTransfer.getCurrency(),
//				amount);
//	}

	/**
	 * Creates a MoneyTransfer associated to the invoice
	 * of the given containerMoneyTransfer in its currency and with
	 * its price. 
	 * 
	 * @param transferRegistry
	 * @param containerMoneyTransfer
	 * @param from
	 * @param to
	 */
	public MoneyTransfer(TransferRegistry transferRegistry,
			MoneyTransfer containerMoneyTransfer, Anchor from, Anchor to) 
	{
		this(transferRegistry, containerMoneyTransfer, containerMoneyTransfer.getInitiator(),
				from, to,
				containerMoneyTransfer.getCurrency(),
				containerMoneyTransfer.getAmount()
				);
	}

	/**
	 * @return Returns the currency.
	 */
	public Currency getCurrency()
	{
		return currency;
	}
	/**
	 * @return Returns the amount.
	 */
	public long getAmount()
	{
		return amount;
	}
	
}
