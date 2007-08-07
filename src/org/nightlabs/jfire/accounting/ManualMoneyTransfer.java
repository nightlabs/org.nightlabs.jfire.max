package org.nightlabs.jfire.accounting;



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

	// @Chairat: A subclass of a PC class *NEVER* defines a primary key, because it *must* have the same
	// as the superclass! Additionally, we nearly *always* use composite-pks with the organisationID as namespace.

	/**
	 * @deprecated Only for JDO!
	 */
	protected ManualMoneyTransfer(){
	}

// TODO @Chairat: If we wanted to use a container for this kind of MoneyTransfer, we'd have to pass it to the super constructor! It's wrong to manage it twice (here again)!
//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	protected MoneyTransfer containerMoneyTransfer;
	/**
	 * TODO For the beginning, we don't need a container (and it might never become necessary).
	 * Instead, we need currency, amount etc.
	 * TODO You're using the wrong super-constructor here. It is illegal to use the default-constructor!
	 *
	 * Used to create a MoneyTransfer accosiated to
	 * the (first) invoice of containerMoneyTransfer. 
	 *
	 * @param containerMoneyTransfer
	 */
	public ManualMoneyTransfer(MoneyTransfer containerMoneyTransfer)
	{
//		if (productedMoneyTransferID == null)
//			throw new NullPointerException("productedMoneyTransferID");
		
//		this.containerMoneyTransfer = containerMoneyTransfer;
		this.reason = new ManualMoneyTransferReason(this);
	}

	/**
	 * TODO incomplete tagging - must use mapped-by and dependent!
	 * @Chairat: I already completed it.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		mapped-by="manualMoneyTransfer"
	 *		dependent="true"
	 */
	private ManualMoneyTransferReason reason;

	public ManualMoneyTransferReason getReason() {
		return reason;
	}

	// @Chairat: I removed all the unnecessary methods.
}
