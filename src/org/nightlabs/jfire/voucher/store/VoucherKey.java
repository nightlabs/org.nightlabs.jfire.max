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
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.pay.Payment;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.voucher.accounting.VoucherRedemption;
import org.nightlabs.jfire.voucher.store.id.VoucherKeyID;
import org.nightlabs.math.Base62Coder;
import org.nightlabs.util.Util;

import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

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
 * @jdo.fetch-group name="VoucherKey.voucher" fields="voucher"
 * @jdo.fetch-group name="VoucherKey.nominalValue" fields="nominalValue"
 * @jdo.fetch-group name="VoucherKey.restValue" fields="restValue"
 * @jdo.fetch-group name="VoucherKey.createUser" fields="createUser"
 * @jdo.fetch-group name="VoucherKey.validUser" fields="validUser"
 * @jdo.fetch-group name="VoucherKey.reversedUser" fields="reversedUser"
 * @jdo.fetch-group name="VoucherKey.redemptions" fields="redemptions"
 *
 * @jdo.query name="getVoucherKeysForVoucherAndValidity"
 *		query="SELECT WHERE this.voucher == :voucher && this.validity == :validity"
 *
 * @jdo.query name="getVoucherKeyIDByVoucherKeyString"
 *		query="SELECT UNIQUE JDOHelper.getObjectId(this) WHERE this.voucherKey == :voucherKey"
 *
 * @jdo.query name="getVoucherKeyByVoucherKeyString"
 *		query="SELECT UNIQUE WHERE this.voucherKey == :voucherKey"
 */
@PersistenceCapable(
	objectIdClass=VoucherKeyID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireVoucher_VoucherKey")
@FetchGroups({
	@FetchGroup(
		name=VoucherKey.FETCH_GROUP_VOUCHER,
		members=@Persistent(name="voucher")),
	@FetchGroup(
		name=VoucherKey.FETCH_GROUP_NOMINAL_VALUE,
		members=@Persistent(name="nominalValue")),
	@FetchGroup(
		name=VoucherKey.FETCH_GROUP_REST_VALUE,
		members=@Persistent(name="restValue")),
	@FetchGroup(
		name=VoucherKey.FETCH_GROUP_CREATE_USER,
		members=@Persistent(name="createUser")),
	@FetchGroup(
		name=VoucherKey.FETCH_GROUP_VALID_USER,
		members=@Persistent(name="validUser")),
	@FetchGroup(
		name=VoucherKey.FETCH_GROUP_REVERSED_USER,
		members=@Persistent(name="reversedUser")),
	@FetchGroup(
		name=VoucherKey.FETCH_GROUP_REDEMPTIONS,
		members=@Persistent(name="redemptions"))
})
@Queries({
	@javax.jdo.annotations.Query(
		name="getVoucherKeysForVoucherAndValidity",
		value="SELECT WHERE this.voucher == :voucher && this.validity == :validity"),
	@javax.jdo.annotations.Query(
		name="getVoucherKeyIDByVoucherKeyString",
		value="SELECT UNIQUE JDOHelper.getObjectId(this) WHERE this.voucherKey == :voucherKey"),
	@javax.jdo.annotations.Query(
		name="getVoucherKeyByVoucherKeyString",
		value="SELECT UNIQUE WHERE this.voucherKey == :voucherKey")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class VoucherKey
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_VOUCHER = "VoucherKey.voucher";
	public static final String FETCH_GROUP_REST_VALUE = "VoucherKey.restValue";
	public static final String FETCH_GROUP_NOMINAL_VALUE = "VoucherKey.nominalValue";
	public static final String FETCH_GROUP_CREATE_USER = "VoucherKey.createUser";
	public static final String FETCH_GROUP_VALID_USER = "VoucherKey.validUser";
	public static final String FETCH_GROUP_REVERSED_USER = "VoucherKey.reversedUser";
	public static final String FETCH_GROUP_REDEMPTIONS = "VoucherKey.redemptions";

	public static List<? extends VoucherKey> getVoucherKeys(PersistenceManager pm, Voucher voucher, byte validity)
	{
		Query q = pm.newNamedQuery(VoucherKey.class, "getVoucherKeysForVoucherAndValidity");
		return (List<? extends VoucherKey>) q.execute(voucher, validity);
	}

	public static VoucherKeyID getVoucherKeyID(PersistenceManager pm, String voucherKey)
	{
		Query q = pm.newNamedQuery(VoucherKey.class, "getVoucherKeyIDByVoucherKeyString");
		return (VoucherKeyID) q.execute(voucherKey);
	}

	public static VoucherKey getVoucherKey(PersistenceManager pm, String voucherKey)
	{
		Query q = pm.newNamedQuery(VoucherKey.class, "getVoucherKeyByVoucherKeyString");
		return (VoucherKey) q.execute(voucherKey);
	}

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private int voucherOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long voucherNumber;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date createDT;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private User createUser;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date validDT;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User validUser;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date reversedDT;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User reversedUser;

	/**
	 * @jdo.field persistence-modifier="persistent" unique="true" null-value="exception"
	 * @jdo.column length="50"
	 */
	@Element(unique="true")
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(length=50)
	private String voucherKey;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception" indexed="true"
	 */
	@Element(indexed="true")
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Voucher voucher;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="VoucherRedemption"
	 *		mapped-by="voucherKey"
	 */
	@Persistent(
		mappedBy="voucherKey",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<VoucherRedemption> redemptions;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean redemptionExisting = false;

	public static final byte VALIDITY_NOT_YET_VALID = 11;
	public static final byte VALIDITY_VALID = 22;
	public static final byte VALIDITY_REVERSED = 33;
	public static final byte VALIDITY_REDEEMED_COMPLETELY = 44;

	/**
	 * This method calls {@link #getValidityString(byte)} with
	 * the value of {@link #getValidity()}.
	 *
	 * @return name of the constant for each validity.
	 * @see #getValidityString(byte)
	 */
	public String getValidityString()
	{
		return getValidityString(getValidity());
	}

	/**
	 * This method converts the byte value to the name of the constant (public static final). The resulting
	 * <code>String</code> may be used for example in l10n properties files.
	 *
	 * @param validity One of the validities: {@link #VALIDITY_NOT_YET_VALID}, {@link #VALIDITY_VALID}, {@link #VALIDITY_REVERSED}, {@link #VALIDITY_REDEEMED_COMPLETELY}
	 * @return the name of the constant
	 */
	public static String getValidityString(byte validity)
	{
		switch (validity) {
			case VoucherKey.VALIDITY_NOT_YET_VALID:
				return "VALIDITY_NOT_YET_VALID";
			case VoucherKey.VALIDITY_REDEEMED_COMPLETELY:
				return "VALIDITY_REDEEMED_COMPLETELY";
			case VoucherKey.VALIDITY_REVERSED:
				return "VALIDITY_REVERSED";
			case VoucherKey.VALIDITY_VALID:
				return "VALIDITY_VALID";
			default:
				throw new IllegalArgumentException("Unknown validity: " + validity);
		}
	}

	/**
	 * @see #getValidity()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private byte validity;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price nominalValue;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price restValue;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
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

//		long priceID = PriceConfig.createPriceID(nominalValue.getOrganisationID(), nominalValue.getPriceConfigID());
//		restValue = new Price(nominalValue.getOrganisationID(), nominalValue.getPriceConfigID(), priceID, nominalValue.getCurrency());
		restValue = new Price(IDGenerator.getOrganisationID(), IDGenerator.nextID(Price.class), nominalValue.getCurrency());
		restValue.createPriceFragment(
				nominalValue.getPriceFragment(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID, PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID, true).getPriceFragmentType()).
				setAmount(nominalValue.getAmount());
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
			throw new IllegalStateException("Encoding voucherOrganisationID went wrong! So far generated voucherKey (should have a length of 5 but doesn't): " + sb.toString());

		if (random == null)
			random = new Random(System.currentTimeMillis());

		int randomNumber = random.nextInt(1000);
		if (randomNumber < 0 || randomNumber > 999)
			throw new IllegalStateException("randomNumber out of range! This should never happen! randomNumber="+randomNumber);

		sb.append(
				base62Coder.encode(voucherNumber * 1000L + randomNumber, 7));

		String res = sb.toString();

		if (res.length() != 12)
			throw new IllegalStateException("Encoding voucherNumber or randomNumber went wrong! Generated voucherKey (should have a length of 12 but doesn't): " + res);

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

		if (!redemptionExisting)
			redemptionExisting = true;

		redemptions.add(redemption);
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Set<VoucherRedemption> unmodifiableRedemptions = null;

	public Set<VoucherRedemption> getRedemptions()
	{
		if (unmodifiableRedemptions == null)
			unmodifiableRedemptions = Collections.unmodifiableSet(redemptions);

		return unmodifiableRedemptions;
	}

	/**
	 * As soon as the first call to {@link #addRedemption(VoucherRedemption)} has happened, this method
	 * returns <code>true</code> and the voucher cannot be reversed (i.e. refunded) anymore.
	 *
	 * @return <code>true</code> if {@link #getRedemptions()} is not empty. <code>false</code> if no {@link VoucherRedemption} exists
	 *		for this <code>VoucherKey</code>
	 */
	public boolean isRedemptionExisting()
	{
		return redemptionExisting;
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
		return voucherOrganisationID + Util.hashCode(voucherNumber);
	}
}
