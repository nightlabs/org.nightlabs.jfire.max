/*
 * Created on Jun 18, 2005
 */
package org.nightlabs.jfire.accounting.pay;

import java.util.Date;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.pay.PaymentData"
 *		detachable="true"
 *		table="JFireTrade_PaymentDataBankTransferGermany"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class PaymentDataBankTransferGermany extends PaymentData
{
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date moneyInDT = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date moneyOutDT = null;

	/**
	 * @deprecated Only for JDO!
	 */
	protected PaymentDataBankTransferGermany() { }

	/**
	 * @param payment
	 */
	public PaymentDataBankTransferGermany(Payment payment)
	{
		super(payment);
	}

	/**
	 * @return Returns the moneyInDT.
	 */
	public Date getMoneyInDT()
	{
		return moneyInDT;
	}
	/**
	 * @param moneyInDT The moneyInDT to set.
	 */
	public void setMoneyInDT(Date arrivalDT)
	{
		this.moneyInDT = arrivalDT;
	}
	/**
	 * @return Returns the moneyOutDT.
	 */
	public Date getMoneyOutDT()
	{
		return moneyOutDT;
	}
	/**
	 * @param moneyOutDT The moneyOutDT to set.
	 */
	public void setMoneyOutDT(Date moneyOutDT)
	{
		this.moneyOutDT = moneyOutDT;
	}
}
