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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.geography.id.CountryID"
 *		detachable = "true"
 *		table = "JFireGeography_Country"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.fetch-group name="Country.name" fields="name"
 * @jdo.fetch-group name="Country.regions" fields="regions"
 */
public class Country implements Serializable
{
	public static final String FETCH_GROUP_NAME = "Country.name";
	public static final String FETCH_GROUP_REGIONS = "Country.regions";

	/////// begin primary key ///////
	/**
	 * 2-char-iso-code
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String countryID;
	/////// end primary key ///////

	/////// begin normal fields ///////

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	protected transient GeographySystem geographySystem;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private CountryName name;

	/**
	 * key: String regionPK<br/>
	 * value: Region region
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="Region"
	 *		dependent-value="true"
	 *		mapped-by="country"
	 *
	 * @jdo.key mapped-by="primaryKey"
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="primaryKey"
	 */
	protected Map regions = new HashMap();
	/////// end normal fields ///////
	
	public static final String DEFAULT_LANGUAGEID = Locale.ENGLISH.getLanguage();
	
	/////// begin constructors ///////

	/**
	 * @deprecated Only for JDO!
	 */
	protected Country() { }
	
	public Country(String countryID)
	{
		this(null, countryID);
	}
	public Country(GeographySystem geographySystem, String countryID)
	{
		if (countryID == null)
			throw new NullPointerException("countryID");

		this.geographySystem = geographySystem;
		this.countryID = countryID;
		this.name = new CountryName(this);
	}

	/**
	 * @return Returns the countryID.
	 */
	public String getCountryID()
	{
		return countryID;
	}	
	/**
	 * @param countryID The countryID to set.
	 */
	protected void setCountryID(String countryID)
	{
		this.countryID = countryID;
	}

	/**
	 * @return Returns the name.
	 */
	public CountryName getName()
	{
		return name;
	}

	public void addRegion(Region region)
	{
		if (region.getPrimaryKey() == null) {
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			if (pm == null)			
				throw new IllegalStateException("region does not have a primary key and this instance of Country is not persistent! Cannot assign a primary key!");

			pm.makePersistent(region);
		}

		regions.put(region.getPrimaryKey(), region);
	}
	public Collection getRegions()
	{
		if (geographySystem != null)
			geographySystem.needRegions(countryID);

		return Collections.unmodifiableCollection(regions.values());
	}

	/**
	 * This method creates a copy of this Country without copying the
	 * regions.
	 *
	 * @return Returns a partial copy of this instance.
	 */
	public Country copyForJDOStorage()
	{
		Country n = new Country(this.countryID);
		n.name.load(this.name);
		return n;
	}
}
