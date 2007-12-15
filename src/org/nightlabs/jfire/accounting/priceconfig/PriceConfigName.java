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

package org.nightlabs.jfire.accounting.priceconfig;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigNameID"
 *		detachable="true"
 *		table="JFireTrade_PriceConfigName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, priceConfigID"
 *
 * @jdo.fetch-group name="PriceConfig.name" fields="priceConfig, names"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="priceConfig, names"
 */
public class PriceConfigName extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String priceConfigID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceConfig priceConfig;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireTrade_PriceConfigName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names;

	/**
	 * @deprecated Only for JDO!
	 */
	protected PriceConfigName() { }

	public PriceConfigName(PriceConfig priceConfig)
	{
		this.organisationID = priceConfig.getOrganisationID();
		this.priceConfigID = priceConfig.getPriceConfigID();
		this.priceConfig = priceConfig;

		names = new HashMap<String, String>();
	}

	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	@Override
	protected String getFallBackValue(String languageID)
	{
		return priceConfig.getPrimaryKey();
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the priceConfigID.
	 */
	public String getPriceConfigID()
	{
		return priceConfigID;
	}
	/**
	 * @return Returns the priceConfig.
	 */
	public IPriceConfig getPriceConfig()
	{
		return priceConfig;
	}
}
