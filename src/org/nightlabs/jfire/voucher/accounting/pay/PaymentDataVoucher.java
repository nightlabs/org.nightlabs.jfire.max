package org.nightlabs.jfire.voucher.accounting.pay;

import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.pay.PaymentData;

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
public class PaymentDataVoucher
extends PaymentData
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	protected PaymentDataVoucher() { }

	public PaymentDataVoucher(Payment payment)
	{
		super(payment);
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
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
