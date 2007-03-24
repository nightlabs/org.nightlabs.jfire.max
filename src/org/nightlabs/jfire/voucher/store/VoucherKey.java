package org.nightlabs.jfire.voucher.store;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.voucher.accounting.VoucherRedemption;
import org.nightlabs.math.Base62Coder;
import org.nightlabs.util.Utils;

/**
 * As a {@link Voucher} is a {@link Product} and thus can be refunded and resold to another customer (as
 * long as no money for it is redeemed), it is essential to modify the key. Otherwise the old customer
 * could redeem money even though its voucher was refunded. Additionally, we want to have a history for
 * all keys that have been created.
 * <p>
 * Therefore, an instance of this class is created during delivery (1st stage). If
 * </p>
 *
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.voucher.store.id.VoucherKeyID"
 *		detachable="true"
 *		table="JFireVoucher_VoucherKey"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="voucherOrganisationID, voucherNumber"
 *
 * @jdo.fetch-group name="VoucherKey.nominalValue" fields="nominalValue"
 * @jdo.fetch-group name="VoucherKey.restValue" fields="restValue"
 * @jdo.fetch-group name="VoucherKey.createUser" fields="createUser"
 * @jdo.fetch-group name="VoucherKey.validUser" fields="validUser"
 * @jdo.fetch-group name="VoucherKey.reversedUser" fields="reversedUser"
 * @jdo.fetch-group name="VoucherKey.redemptions" fields="redemptions"
 *
 * @jdo.query name="getVoucherKeysForVoucherAndValidity"
 *		query="SELECT WHERE this.voucher == :voucher && this.validity == :validity"
 */
public class VoucherKey
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUPS_REST_VALUE = "VoucherKey.restValue";
	public static final String FETCH_GROUPS_NOMINAL_VALUE = "VoucherKey.nominalValue";
	public static final String FETCH_GROUPS_CREATE_USER = "VoucherKey.createUser";
	public static final String FETCH_GROUPS_VALID_USER = "VoucherKey.validUser";
	public static final String FETCH_GROUPS_REVERSED_USER = "VoucherKey.reversedUser";
	public static final String fETCH_GROUPS_REDEMPTIONS = "VoucherKey.redemptions";

	public static List<? extends VoucherKey> getVoucherKeys(PersistenceManager pm, Voucher voucher, byte validity)
	{
		Query q = pm.newNamedQuery(VoucherKey.class, "getVoucherKeysForVoucherAndValidity");
		return (List<? extends VoucherKey>) q.execute(voucher, validity);
	}

	/**
	 * @jdo.field primary-key="true"
	 */
	private int voucherOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long voucherNumber;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private Date createDT;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private User createUser;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date validDT;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User validUser;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date reversedDT;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User reversedUser;

	/**
	 * @jdo.field persistence-modifier="persistent" unique="true"
	 * @jdo.column length="12"
	 */
	private String voucherKey;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception" indexed="true"
	 */
	private Voucher voucher;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="VoucherRedemption"
	 *		mapped-by="voucherKey"
	 */
	private Set<VoucherRedemption> redemptions;

	public static final byte VALIDITY_NOT_YET_VALID = 11;
	public static final byte VALIDITY_VALID = 22;
	public static final byte VALIDITY_REVERSED = 33;
	public static final byte VALIDITY_REDEEMED_COMPLETELY = 44;

	/**
	 * @see #getValidity()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private byte validity;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Price nominalValue;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private Price restValue;

	/**
	 * @deprecated Only for JDO!
	 */
	protected VoucherKey() { }

	public VoucherKey(int voucherOrganisationID, long voucherNumber, Article article, User createUser)
	{
		this.voucherOrganisationID = voucherOrganisationID;
		this.voucherNumber = voucherNumber;

		if (voucherOrganisationID < 0)
			throw new IllegalArgumentException("voucherOrganisationID < 0");
		if (voucherNumber < 0)
			throw new IllegalArgumentException("voucherNumber < 0");

		this.nominalValue = article.getPrice();
		this.voucher = (Voucher) article.getProduct();
		this.voucherKey = generateVoucherKey(voucherOrganisationID, voucherNumber);
		this.createDT = new Date();
		this.createUser = createUser;
		this.validity = VALIDITY_NOT_YET_VALID;
		this.redemptions = new HashSet<VoucherRedemption>();

		long priceID = PriceConfig.createPriceID(nominalValue.getOrganisationID(), nominalValue.getPriceConfigID());
		restValue = new Price(nominalValue.getOrganisationID(), nominalValue.getPriceConfigID(), priceID, nominalValue.getCurrency());
	}

	private static Random random;

	/**
	 * All voucher keys start with a small-letter "v" which is the value of this constant.
	 */
	public static final char VOUCHER_KEY_PREFIX = 'v';

	protected static String generateVoucherKey(long voucherOrganisationID, long voucherNumber)
	{
		Base62Coder base62Coder = Base62Coder.sharedInstance();
		StringBuffer sb = new StringBuffer(12);
		sb.append(VOUCHER_KEY_PREFIX);
		sb.append(
				base62Coder.encode(voucherOrganisationID, 4));

		if (sb.length() != 5)
			throw new IllegalStateException("Encoding ticketingOrganisationID went wrong! So far generated ticketKey (should have a length of 5 but doesn't): " + sb.toString());

		if (random == null)
			random = new Random(System.currentTimeMillis());

		int randomNumber = random.nextInt(1000);
		if (randomNumber < 0 || randomNumber > 999)
			throw new IllegalStateException("randomNumber out of range! This should never happen! randomNumber="+randomNumber);

		sb.append(
				base62Coder.encode(voucherNumber * 1000L + randomNumber, 7));

		String res = sb.toString();
		return res;
	}

	public int getVoucherOrganisationID()
	{
		return voucherOrganisationID;
	}
	public long getVoucherNumber()
	{
		return voucherNumber;
	}
	public Voucher getVoucher()
	{
		return voucher;
	}
	/**
	 * <p>
	 * When a {@link VoucherKey} is created, it's initally {@link #VALIDITY_NOT_YET_VALID}. As soon as it is delivered to the customer,
	 * it starts to be valid (value {@link #VALIDITY_VALID}). The validity is modified during the last stage of delivery. This means,
	 * the validity {@link #VALIDITY_NOT_YET_VALID} only exists for the short time between creation (1st stage of delivery) and
	 * the end of the delivery.
	 * </p>
	 * <p>
	 * Then, two things can happen:
	 * <ul>
	 * <li>
	 * The customer doesn't want it anymore and reverses (refunds) it. In this case, the validity would be changed into
	 * {@link #VALIDITY_REVERSED}. This renders the Voucher unusable (the customer cannot redeem it anymore). The validity
	 * is changed during the last stage of delivery of the reversing {@link Article}.
	 * </li>
	 * <li>
	 * Alternatively, the customer (or the person who received the voucher) can redeem it. As partial redemption is
	 * possible, redeeming it doesn't change the validity until it is completely redeemed (i.e. the rest value is 0). As
	 * soon as the restValue reaches 0, the validity is set to {@link #VALIDITY_REDEEMED_COMPLETELY}.
	 * </li>
	 * </ul>
	 * </p> 
	 */
	public byte getValidity()
	{
		return validity;
	}

	/**
	 * @param validity The new validity.
	 * @see {@link #getValidity()}
	 */
	public void setValidity(byte validity, User user)
	{
		if (this.validity == validity)
			return; // no change => silently ignore

		switch (this.validity) {
			case VALIDITY_NOT_YET_VALID:
				if (VALIDITY_VALID != validity) //  && VALIDITY_REVERSED != validity)
					throw new IllegalArgumentException("The current validity VALIDITY_NOT_YET_VALID allows only to set it to VALIDITY_VALID! The new validity is invalid: " + validity);
				break;

			case VALIDITY_VALID:
				if (VALIDITY_REDEEMED_COMPLETELY != validity && VALIDITY_REVERSED != validity)
					throw new IllegalArgumentException("The current validity VALIDITY_VALID allows only to set it to VALIDITY_REDEEMED_COMPLETELY or VALIDITY_REVERSED! The new validity is invalid: " + validity);
				break;

			case VALIDITY_REDEEMED_COMPLETELY:
				throw new IllegalArgumentException("The current validity VALIDITY_REDEEMED_COMPLETELY does not allow any further changes! The new validity is therefore invalid: " + validity);

			case VALIDITY_REVERSED:
				throw new IllegalArgumentException("The current validity VALIDITY_REVERSED does not allow any further changes! The new validity is therefore invalid: " + validity);

			default:
				throw new IllegalStateException("Current validity is unknown: " + this.validity);
		}

		switch (validity) {
			case VALIDITY_VALID:
				validDT = new Date();
				validUser = user;
			break;
			case VALIDITY_REVERSED:
				reversedDT = new Date();
				reversedUser = user;
			break;
		}

		this.validity = validity;
	}

	public String getVoucherKey()
	{
		return voucherKey;
	}
	/**
	 * @return the timestamp of the moment when this <code>VoucherKey</code> was created.
	 */
	public Date getCreateDT()
	{
		return createDT;
	}
	/**
	 * @return the user who created this <code>VoucherKey</code>.
	 */
	public User getCreateUser()
	{
		return createUser;
	}

	public Date getValidDT()
	{
		return validDT;
	}
	public User getValidUser()
	{
		return validUser;
	}
	public Date getReversedDT()
	{
		return reversedDT;
	}
	public User getReversedUser()
	{
		return reversedUser;
	}

	public Price getNominalValue()
	{
		return nominalValue;
	}
	/**
	 * @return the value that's still available. 
	 */
	public Price getRestValue()
	{
		return restValue;
	}

//	public long decrementRestValue(long amount)
//	{
//		return incrementRestValue(-amount);
//	}
//
//	public long incrementRestValue(long amount)
//	{
//		if (restValue == null)
//			throw new IllegalStateException("This Voucher is currently not allocated within an Article - there is no restValue existing.");
//
//		long newAmount = restValue.getAmount() + amount;
//		if (newAmount < 0)
//			throw new IllegalStateException("restValue.amount would become negative!");
//
//		restValue.setAmount(newAmount);
//
//		if (newAmount == 0)
//			setValidity(VALIDITY_REDEEMED_COMPLETELY, null); // the user isn't stored with this validity
//
//		return newAmount;
//	}

	public void addRedemption(VoucherRedemption redemption)
	{
		if (VALIDITY_VALID != validity)
			throw new IllegalStateException("VoucherKey not valid! Cannot add VoucherRedemption!");

		if (redemptions.contains(redemption))
			throw new IllegalStateException("Why the hell do you try to add the same redemption twice? Should never happen!");

		long redemptionAmount = redemption.getPayment().getAmount(); // this amount is always >= 0 (actually, it should be > 0)

		if (redemptionAmount < 0)
			throw new IllegalStateException("How the hell can it happen that redemption.getPayment().getAmount() < 0?! redemption.getPayment().getAmount() = " + redemption.getPayment().getAmount() + " paymentPK=" + redemption.getPayment().getPrimaryKey());

		if (!restValue.getCurrency().getCurrencyID().equals(redemption.getPayment().getCurrencyID().currencyID))
			throw new IllegalStateException("VoucherRedemption.payment.currency > restValue.currency!!! Trying to redeem with currency " + redemption.getPayment().getCurrencyID() + " but this VoucherKey (" + getVoucherKey() + " is created with currency " + restValue.getCurrency().getCurrencyID());

		if (Payment.PAYMENT_DIRECTION_INCOMING.equals(redemption.getPayment().getPaymentDirection())) {
			// nothing to do
		}
		else if (Payment.PAYMENT_DIRECTION_OUTGOING.equals(redemption.getPayment().getPaymentDirection()))
				redemptionAmount *= -1; // inverse if we have to give money to the customer in order to INCREASE the restValue
		else
			throw new IllegalStateException("Unknown paymentDirection! paymentDirection="+redemption.getPayment().getPaymentDirection()+" paymentPK=" + redemption.getPayment().getPrimaryKey());

		long newAmount = restValue.getAmount() - redemptionAmount; // normally, this decreases the restValue, but this depends on the payment direction
		if (newAmount < 0)
			throw new IllegalStateException("restValue.amount would become negative! Trying to redeem " + redemptionAmount + " but this voucherKey (" + getVoucherKey() + " contains a rest value of only " + restValue.getAmount() + " (in subunits of currency " + redemption.getPayment().getCurrencyID() + ")");

		restValue.setAmount(newAmount);

		if (newAmount == 0)
			setValidity(VALIDITY_REDEEMED_COMPLETELY, redemption.getPayment().getUser()); // the user isn't stored, but it doesn't hurt to pass it - imho it's cleaner. Marco.

		redemptions.add(redemption);
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<VoucherRedemption> unmodifiableRedemptions = null;

	public Set<VoucherRedemption> getRedemptions()
	{
		if (unmodifiableRedemptions == null)
			unmodifiableRedemptions = Collections.unmodifiableSet(redemptions);

		return unmodifiableRedemptions;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof VoucherKey)) return false;
		VoucherKey o = (VoucherKey) obj;
		return o.voucherOrganisationID == this.voucherOrganisationID && o.voucherNumber == this.voucherNumber;
	}
	@Override
	public int hashCode()
	{
		return voucherOrganisationID + Utils.hashCode(voucherNumber);
	}
}
