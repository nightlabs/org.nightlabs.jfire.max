package org.nightlabs.jfire.voucher.accounting;

import java.io.Serializable;

import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.voucher.store.VoucherKey;
import org.nightlabs.util.Utils;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.voucher.accounting.id.VoucherRedemptionID"
 *		detachable="true"
 *		table="JFireVoucher_VoucherRedemption"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, voucherRedemptionID"
 *
 * @jdo.fetch-group name="VoucherRedemption.voucherKey" fields="voucherKey"
 * @jdo.fetch-group name="VoucherRedemption.payment" fields="payment"
 */
public class VoucherRedemption
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_VOUCHER_KEY = "VoucherRedemption.voucherKey";
	public static final String FETCH_GROUP_PAYMENT = "VoucherRedemption.payment";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long voucherRedemptionID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private VoucherKey voucherKey;

//	/**
//	 * @jdo.field persistence-modifier="persistent" null-value="exception"
//	 */
//	private Date redemptionDT;
//
//	/**
//	 * @jdo.field persistence-modifier="persistent" null-value="exception"
//	 */
//	private User redemptionUser;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private Payment payment;

	/**
	 * @deprecated Only for JDO!
	 */
	protected VoucherRedemption() { }

	public VoucherRedemption(String organisationID, long voucherRedemptionID, VoucherKey voucherKey, Payment payment)
	{
		this.organisationID = organisationID;
		this.voucherRedemptionID = voucherRedemptionID;
		this.voucherKey = voucherKey;
//		this.redemptionDT = new Date();
//		this.redemptionUser = user;
		this.payment = payment;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public long getVoucherRedemptionID()
	{
		return voucherRedemptionID;
	}

	public VoucherKey getVoucherKey()
	{
		return voucherKey;
	}

//	public Date getRedemptionDT()
//	{
//		return redemptionDT;
//	}
//	public User getRedemptionUser()
//	{
//		return redemptionUser;
//	}

	public Payment getPayment()
	{
		return payment;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof VoucherRedemption)) return false;
		VoucherRedemption o = (VoucherRedemption) obj;
		return Utils.equals(o.organisationID, this.organisationID) && Utils.equals(o.voucherRedemptionID, this.voucherRedemptionID);
	}
	@Override
	public int hashCode()
	{
		return Utils.hashCode(organisationID) + Utils.hashCode(voucherRedemptionID);
	}
}
