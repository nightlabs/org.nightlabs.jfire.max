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
import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.geography.id.DistrictID;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.geography.id.DistrictID"
 *		detachable="true"
 *		table="JFireGeography_District"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="countryID, organisationID, districtID"
 */
public class District implements Serializable // , StoreCallback
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/////// begin primary key ///////
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
	 */
	private String districtID;
	/////// end primary key ///////

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	protected transient Geography geography;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private City city;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private double latitude;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private double longitude;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="String"
	 *		dependent-element="true"
	 *		table = "JFireGeography_District_zips"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Set<String> zips = new HashSet<String>();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String name;

	/**
	 * @deprecated Only for JDO!
	 */
	protected District() { }

	public District(String organisationID, String districtID, City city)
	{
		this(null, organisationID, districtID, city);
	}
	public District(Geography geography, String organisationID, String districtID, City city)
	{
		if (organisationID == null)
			throw new NullPointerException("organisationID");

		if (districtID == null)
			throw new NullPointerException("districtID");

		this.geography = geography;
		this.city = city;
		this.countryID = city.getCountryID();
		this.organisationID = organisationID;
		this.districtID = districtID;
		this.primaryKey = getPrimaryKey(countryID, organisationID, districtID);
	}

	public String getCountryID()
	{
		return countryID;
	}
	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the districtID.
	 */
	public String getDistrictID()
	{
		return districtID;
	}
	public static String getPrimaryKey(String countryID, String organisationID, String districtID)
	{
		return countryID + '/' + organisationID + '/' + districtID;
	}
	public static String getPrimaryKey(DistrictID districtID)
	{
		return getPrimaryKey(districtID.countryID, districtID.organisationID, districtID.districtID);
	}
	/**
	 * @return Returns the primaryKey.
	 */
	public String getPrimaryKey()
	{
		return primaryKey;
	}

	/**
	 * @return Returns the latitude.
	 */
	public double getLatitude()
	{
		return latitude;
	}
	/**
	 * @param latitude The latitude to set.
	 */
	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}
	/**
	 * @return Returns the longitude.
	 */
	public double getLongitude()
	{
		return longitude;
	}
	/**
	 * @param longitude The longitude to set.
	 */
	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}
	/**
	 * @return Returns the city.
	 */
	public City getCity()
	{
		return city;
	}
	/**
	 * @return Returns the zips (instances of <tt>java.lang.String</tt>).
	 */
	public Collection<String> getZips()
	{
		if (geography != null)
			geography.needZips(countryID);

		return zips;
	}
	public void addZip(String zip)
	{
		zips.add(zip);
	}
	public boolean removeZip(String zip)
	{
		return zips.remove(zip);
	}
	public boolean containsZip(String zip)
	{
		return zips.contains(zip);
	}
	/**
	 * @return Returns the name.
	 */
	protected String getName()
	{
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	protected void setName(String name)
	{
		this.name = name;
	}
//	/**
//	 * @see javax.jdo.listener.StoreCallback#jdoPreStore()
//	 */
//	public void jdoPreStore()
//	{
//		if (organisationID == null || districtID < 0) {
//			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//
//			Geography geo = Geography.getGeography(pm);
//			if (organisationID == null)
//				setOrganisationID(geo.getOrganisationID());
//
//			if (districtID < 0)
//				setDistrictID(geo.generateDistrictID());
//		}
//	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof District)) return false;
		District o = (District) obj;
		return
				Util.equals(this.countryID, o.countryID) &&
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.districtID, o.districtID);
	}
	@Override
	public int hashCode()
	{
		return
				Util.hashCode(countryID) ^
				Util.hashCode(organisationID) ^
				Util.hashCode(districtID);
	}
}
