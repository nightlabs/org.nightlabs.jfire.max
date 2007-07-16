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

package org.nightlabs.jfire.geography;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.geography.id.CityNameID"
 *		detachable = "true"
 *		table = "JFireGeography_CityName"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.create-objectid-class field-order="countryID, organisationID, cityID"
 *
 * @jdo.fetch-group name="City.name" fields="city, names"
 */
public class CityName extends I18nText
{
	/**
	 * 2-char-iso-code
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String countryID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String cityID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private City city;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireGeography_CityName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map names = new HashMap();

	protected CityName()
	{
	}

	public CityName(City city)
	{
		this.city = city;
		this.countryID = city.getCountryID();
		this.organisationID = city.getOrganisationID();
		this.cityID = city.getCityID();
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap()
	{
		return names;
	}

	public City getCity()
	{
		return city;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return City.getPrimaryKey(countryID, organisationID, cityID);
	}
}
