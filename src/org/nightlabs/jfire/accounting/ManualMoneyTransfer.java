package org.nightlabs.jfire.accounting;

import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;



/**
 * @author Chairat Kongarayawetchakun <chairatk[AT]nightlabs[DOT]de>
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.MoneyTransfer"
 *		detachable="true"
 *		table="JFireTrade_ManualMoneyTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="ManualMoneyTransfer.reason" fields="reason"
 */	
public class ManualMoneyTransfer extends MoneyTransfer
{
	private static final long serialVersionUID = 1L; // Added this field. Marco.

	/**
	 * @deprecated Only for JDO!
	 */
	protected ManualMoneyTransfer(){
	}

	public ManualMoneyTransfer(MoneyTransfer containerMoneyTransfer)
	{
		this.reason = new ManualMoneyTransferReason(this);
	}

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		mapped-by="manualMoneyTransfer"
	 *		dependent="true"
	 */
	private ManualMoneyTransferReason reason;

	public ManualMoneyTransferReason getReason() {
		return reason;
	}
	
	/**
	 * @param transferRegistry
	 * @param container
	 * @param initiator
	 * @param from
	 * @param to
	 * @param currency
	 * @param amount
	 * @param reason
	 */
	public ManualMoneyTransfer(
			Transfer container, User initiator, Anchor from, Anchor to, Currency currency, long amount,
			ManualMoneyTransferReason reason)
	{		
		super(container, initiator, from, to, currency, amount);
		this.reason = reason;
	}
}
