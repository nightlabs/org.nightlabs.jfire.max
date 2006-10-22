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

import javax.jdo.listener.StoreCallback;

import org.nightlabs.i18n.I18nText;

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
 * @!
 * TODO What is that fetch-group "PriceCell.this" doing here? Marco ;-) Well, not mine, maybe just delete it. Alex
 */
public class TariffName extends I18nText
implements StoreCallback
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long tariffID = -1;

	public TariffName()
	{
	}

	public TariffName(Tariff tariff)
	{
		this.tariff = tariff;
		this.organisationID = tariff.getOrganisationID();
		this.tariffID = tariff.getTariffID();
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
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
	 *
	 * @jdo.join
	 */
	protected Map names = new HashMap();

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
	public long getTariffID()
	{
		return tariffID;
	}
	/**
	 * @param tariffID The tariffID to set.
	 */
	protected void setTariffID(long tariffID)
	{
		this.tariffID = tariffID;
	}
	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap()
	{
		return names;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return Tariff.getPrimaryKey(organisationID, tariffID);
	}

	/**
	 * @see javax.jdo.listener.StoreCallback#jdoPreStore()
	 */
	public void jdoPreStore()
	{
		if (tariffID < 0)
			throw new IllegalStateException("tariffID < 0!!!");
	}

}
