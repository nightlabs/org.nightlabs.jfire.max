package org.nightlabs.jfire.store;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.id.UnitID;
import org.nightlabs.util.Util;

/**
 * Instances of this class specify a measurement unit like hour, kilogram and
 * the like.
 *
 * @author Marco Schulze - Marco at NightLabs dot de
 */
@PersistenceCapable(
	objectIdClass=UnitID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_Unit")
@FetchGroups({
	@FetchGroup(
		name=Unit.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=Unit.FETCH_GROUP_SYMBOL,
		members=@Persistent(name="symbol")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInOrderEditor",
		members={@Persistent(name="name"), @Persistent(name="symbol")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInOfferEditor",
		members={@Persistent(name="name"), @Persistent(name="symbol")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInInvoiceEditor",
		members={@Persistent(name="name"), @Persistent(name="symbol")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInDeliveryNoteEditor",
		members={@Persistent(name="name"), @Persistent(name="symbol")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInReceptionNoteEditor",
		members={@Persistent(name="name"), @Persistent(name="symbol")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Unit
implements Serializable, StoreCallback
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "Unit.name";
	public static final String FETCH_GROUP_SYMBOL = "Unit.symbol";

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=50)
	private String unitID;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private UnitName name;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private UnitSymbol symbol;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int decimalDigitCount;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected Unit() { }

	public Unit(String organisationID, String unitID, int decimalDigitCount)
	{
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(unitID, "unitID");

		this.organisationID = organisationID;
		this.unitID = unitID;
		this.name = new UnitName(this);
		this.symbol = new UnitSymbol(this);
		this.decimalDigitCount = decimalDigitCount;
	}

	public void jdoPreStore()
	{
		ObjectIDUtil.assertValidIDString(organisationID, "organisationID");
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getUnitID()
	{
		return unitID;
	}

	public UnitName getName()
	{
		return name;
	}

	public UnitSymbol getSymbol()
	{
		return symbol;
	}

	public int getDecimalDigitCount()
	{
		return decimalDigitCount;
	}

	public void setDecimalDigitCount(int decimalDigitCount) {
		throw new IllegalStateException("The decimal digit count has already been set!!");
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof Unit)) return false;
		Unit o = (Unit)obj;
		return Util.equals(o.organisationID, this.organisationID) && Util.equals(o.unitID, this.unitID);
	}

	@Override
	public int hashCode()
	{
		return (31 * Util.hashCode(organisationID)) + Util.hashCode(unitID);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + unitID + ']';
	}

	/**
	 * Returns the given amount in the double value of this currency.
	 * <p>
	 *   amount / 10^(decimalDigitCount)
	 * <p>
	 *
	 * @param amount The amount to convert
	 * @return
	 */
	public double toDouble(long amount) {
		return amount / Math.pow(10, getDecimalDigitCount());
	}
	/**
	 * Convert the given amount to the long value of this currency.
	 * <p>
	 *   amount * 10^(decimalDigitCount)
	 * <p>
	 *
	 * @param amount The amount to convert
	 * @return the approximate value as long - there might be rounding differences.
	 */
	public long toLong(double amount) {
		return (long)(amount * Math.pow(10, getDecimalDigitCount()));
	}
}
