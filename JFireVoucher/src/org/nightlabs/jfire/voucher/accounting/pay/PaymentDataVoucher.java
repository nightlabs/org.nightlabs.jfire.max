package org.nightlabs.jfire.voucher.accounting.pay;

import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentData;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.pay.PaymentData"
 *		detachable="true"
 *		table="JFireVoucher_PaymentDataVoucher"
 *
 * @jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireVoucher_PaymentDataVoucher")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PaymentDataVoucher
extends PaymentData
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PaymentDataVoucher() { }

	public PaymentDataVoucher(Payment payment)
	{
		super(payment);
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String voucherKey;

	public String getVoucherKey()
	{
		return voucherKey;
	}

	public void setVoucherKey(String voucherKey)
	{
		this.voucherKey = voucherKey;
	}
}
