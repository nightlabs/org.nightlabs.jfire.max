package org.nightlabs.jfire.accounting;


import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.util.NLLocale;

import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

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
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_ManualMoneyTransfer")
@FetchGroups(
	@FetchGroup(
		name=ManualMoneyTransfer.FETCH_GROUP_REASON,
		members=@Persistent(name="reason"))
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
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
	@Persistent(
		dependent="true",
		mappedBy="manualMoneyTransfer",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private ManualMoneyTransferReason reason;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
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
	
	@Override
	protected String internalGetDescription() {
		if (reason != null)
			return reason.getText(NLLocale.getDefault().getLanguage());
		return String.format(
				"Manual moneytransfer"
			);
		
	}
}
