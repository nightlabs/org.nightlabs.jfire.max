package org.nightlabs.jfire.voucher.accounting;

import java.io.Serializable;

import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.voucher.store.VoucherKey;
import org.nightlabs.util.Util;

import org.nightlabs.jfire.voucher.accounting.id.VoucherRedemptionID;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

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
@PersistenceCapable(
	objectIdClass=VoucherRedemptionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireVoucher_VoucherRedemption")
@FetchGroups({
	@FetchGroup(
		name=VoucherRedemption.FETCH_GROUP_VOUCHER_KEY,
		members=@Persistent(name="voucherKey")),
	@FetchGroup(
		name=VoucherRedemption.FETCH_GROUP_PAYMENT,
		members=@Persistent(name="payment"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
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
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long voucherRedemptionID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
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
@Persistent(
	nullValue=NullValue.EXCEPTION,
	persistenceModifier=PersistenceModifier.PERSISTENT)
	private Payment payment;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
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
		return Util.equals(o.organisationID, this.organisationID) && Util.equals(o.voucherRedemptionID, this.voucherRedemptionID);
	}
	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) + Util.hashCode(voucherRedemptionID);
	}
}
