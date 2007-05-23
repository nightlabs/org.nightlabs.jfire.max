package org.nightlabs.jfire.dynamictrade.store;

import java.io.Serializable;

import javax.jdo.listener.StoreCallback;

import org.nightlabs.jdo.ObjectIDUtil;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.dynamictrade.store.id.UnitID"
 *		detachable="true"
 *		table="JFireDynamicTrade_Unit"
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
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long unitID;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="unit"
	 */
	private UnitName name;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="unit"
	 */
	private UnitSymbol symbol;

	/**
	 * @deprecated Only for JDO!
	 */
	protected Unit() { }

	public Unit(String organisationID, long unitID)
	{
		this.organisationID = organisationID;
		this.unitID = unitID;
		this.name = new UnitName(this);
		this.symbol = new UnitSymbol(this);
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
}
