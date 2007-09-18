package org.nightlabs.jfire.accounting;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;

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
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_REASON = "ManualMoneyTransfer.reason";
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		mapped-by="manualMoneyTransfer"
	 *		dependent="true"
	 */
	private ManualMoneyTransferReason reason;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ManualMoneyTransfer() { }

	/**
	 * TODO document this constructor!
	 */
	public ManualMoneyTransfer(
			User initiator, Anchor from, Anchor to, Currency currency, long amount)
	{		
		super(
				null, // there is no container
				initiator, from, to, currency, amount);

		this.reason = new ManualMoneyTransferReason(this);
	}

	public ManualMoneyTransferReason getReason() {
		return reason;
	}
}
