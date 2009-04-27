package org.nightlabs.jfire.store;

import java.io.Serializable;

import javax.jdo.listener.StoreCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import org.nightlabs.jfire.store.id.UnitID;
import javax.jdo.annotations.FetchGroups;
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
 *		objectid-class="org.nightlabs.jfire.store.id.UnitID"
 *		detachable="true"
 *		table="JFireTrade_Unit"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, unitID"
 *
 * @jdo.fetch-group name="Unit.name" fields="name"
 * @jdo.fetch-group name="Unit.symbol" fields="symbol"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default" fields="name, symbol"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default" fields="name, symbol"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default" fields="name, symbol"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default" fields="name, symbol"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInReceptionNoteEditor" fetch-groups="default" fields="name, symbol"
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
	private long unitID;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="unit"
	 */
	@Persistent(
		mappedBy="unit",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private UnitName name;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="unit"
	 */
	@Persistent(
		mappedBy="unit",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private UnitSymbol symbol;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int decimalDigitCount;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected Unit() { }

	public Unit(String organisationID, long unitID, int decimalDigitCount)
	{
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
	public long getUnitID()
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
