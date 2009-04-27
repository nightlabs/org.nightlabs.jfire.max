/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.accounting;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.accounting.id.TariffNameID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.TariffNameID"
 *		detachable="true"
 *		table="JFireTrade_TariffName"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, tariffID"
 *
 * @jdo.fetch-group name="Tariff.name" fields="tariff, names"
 * @jdo.fetch-group name="PriceCell.this" fetch-groups="default" fields="price, priceConfig, priceCoordinate"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fields="tariff, names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fields="tariff, names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fields="tariff, names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fields="tariff, names"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fields="tariff, names"
 *
 * @jdo.fetch-group name="FetchGroupsEntityUserSet.replicateToReseller" fields="tariff, names"
 */
@PersistenceCapable(
	objectIdClass=TariffNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_TariffName")
@FetchGroups({
	@FetchGroup(
		name="Tariff.name",
		members={@Persistent(name="tariff"), @Persistent(name="names")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="PriceCell.this",
		members={@Persistent(name="price"), @Persistent(name="priceConfig"), @Persistent(name="priceCoordinate")}),
	@FetchGroup(
		name="FetchGroupsTrade.articleInOrderEditor",
		members={@Persistent(name="tariff"), @Persistent(name="names")}),
	@FetchGroup(
		name="FetchGroupsTrade.articleInOfferEditor",
		members={@Persistent(name="tariff"), @Persistent(name="names")}),
	@FetchGroup(
		name="FetchGroupsTrade.articleInInvoiceEditor",
		members={@Persistent(name="tariff"), @Persistent(name="names")}),
	@FetchGroup(
		name="FetchGroupsTrade.articleInDeliveryNoteEditor",
		members={@Persistent(name="tariff"), @Persistent(name="names")}),
	@FetchGroup(
		name="FetchGroupsPriceConfig.edit",
		members={@Persistent(name="tariff"), @Persistent(name="names")}),
	@FetchGroup(
		name="FetchGroupsEntityUserSet.replicateToReseller",
		members={@Persistent(name="tariff"), @Persistent(name="names")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class TariffName extends I18nText
implements StoreCallback
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

//	/**
//	 * @jdo.field primary-key="true"
//	 */
//	private long tariffID = -1;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String tariffID;

	public TariffName()
	{
	}

	public TariffName(Tariff tariff)
	{
		this.tariff = tariff;
		this.organisationID = tariff.getOrganisationID();
		this.tariffID = tariff.getTariffID();
		names = new HashMap<String, String>();
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Tariff tariff;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireTrade_TariffName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_TariffName_names",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> names;

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @param organisationID The organisationID to set.
	 */
	protected void setOrganisationID(String organisationID)
	{
		this.organisationID = organisationID;
	}
	/**
	 * @return Returns the tariff.
	 */
	public Tariff getTariff()
	{
		return tariff;
	}
	/**
	 * @return Returns the tariffID.
	 */
	public String getTariffID()
	{
		return tariffID;
	}
//	/**
//	 * @param tariffID The tariffID to set.
//	 */
//	protected void setTariffID(long tariffID)
//	{
//		this.tariffID = tariffID;
//	}
	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	@Override
	protected String getFallBackValue(String languageID)
	{
		return Tariff.getPrimaryKey(organisationID, tariffID);
	}

	public void jdoPreStore()
	{
//		if (tariffID < 0)
//			throw new IllegalStateException("tariffID < 0!!!");
	}

}
