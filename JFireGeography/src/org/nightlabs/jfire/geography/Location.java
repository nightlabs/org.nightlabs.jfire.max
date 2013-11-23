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

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.geography.id.LocationID;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
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
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.geography.id.LocationID"
 *		detachable="true"
 *		table="JFireGeography_Location"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="countryID, organisationID, locationID"
 *		include-imports="id/LocationID.imports.inc"
 *		include-body="id/LocationID.body.inc"
 *
 * @jdo.fetch-group name="Location.name" fields="name"
 * @jdo.fetch-group name="Location.city" fields="city"
 * @!jdo.fetch-group name="Location.this" fields="city, district, name"
 *
 */
@PersistenceCapable(
	objectIdClass=LocationID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireGeography_Location")
@FetchGroups({
	@FetchGroup(
		name=Location.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=Location.FETCH_GROUP_CITY,
		members=@Persistent(name="city"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class Location implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "Location.name";
	public static final String FETCH_GROUP_CITY = "Location.city";
//	/**
//	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
//	 */
//	@Deprecated
//	public static final String FETCH_GROUP_THIS_LOCATION = "Location.this";

	/**
	 * 2-char-iso-code
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
@PrimaryKey
@Column(length=100)

	private String countryID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)

	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)

	private String locationID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)

	protected transient Geography geography;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private City city;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private District district;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="location"
	 */
	@Persistent(
		mappedBy="location",
		persistenceModifier=PersistenceModifier.PERSISTENT)

	private LocationName name;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected Location()
	{
	}

	public Location(String organisationID, String locationID, City city)
	{
		this(null, organisationID, locationID, city);
	}

	public Location(String organisationID, String locationID, District district)
	{
		this(null, organisationID, locationID, district);
	}

	public Location(Geography geography, String organisationID, String locationID, District district)
	{
		init(geography, organisationID, locationID, null, district);
	}

	public Location(Geography geography, String organisationID, String locationID, City city)
	{
		init(geography, organisationID, locationID, city, null);
	}

	protected void init(Geography geography, String organisationID, String locationID, City city, District district)
	{
		if (organisationID == null)
			throw new NullPointerException("organisationID");

		if (locationID == null)
			throw new NullPointerException("locationID");

		if (city == null && district == null)
			throw new IllegalArgumentException("Both city and district are null! One of them must be defined!");

		if (city == null)
			city = district.getCity();

		this.geography = geography;
		this.countryID = city.getCountryID();
		this.organisationID = organisationID;
		this.locationID = locationID;
		this.primaryKey = getPrimaryKey(countryID, organisationID, locationID);
		this.city = city;
		this.setDistrict(district);
		this.name = new LocationName(this);
	}

	public District getDistrict()
	{
		return district;
	}
	public void setDistrict(District district)
	{
		if (district != null) {
			if (!city.getPrimaryKey().equals(district.getCity().getPrimaryKey()))
				throw new IllegalArgumentException("district.city != city!!!");
		}

		this.district = district;
	}

	public static String getPrimaryKey(String countryID, String organisationID, String locationID)
	{
		return countryID + '/' + organisationID + '/' + locationID;
	}
	public static String getPrimaryKey(LocationID locationID)
	{
		return getPrimaryKey(locationID.countryID, locationID.organisationID, locationID.locationID);
	}

	public String getCountryID()
	{
		return countryID;
	}
	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getLocationID()
	{
		return locationID;
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}

	public City getCity()
	{
		return city;
	}

	public LocationName getName()
	{
		return name;
	}

	/**
	 * This method creates a copy of this Location. It exchanges the City by the
	 * <tt>persistentCity</tt>.
	 *
	 * @param persistentCity An instance of <tt>City</tt> which either is
	 *		currently persistent or has been detached from detastore. The primary key
	 *		must match the currently assigned city.
	 * @return Returns a partial copy of this instance.
	 */
	public Location copyForJDOStorage(City persistentCity)
	{
		if (persistentCity == null)
			throw new NullPointerException("persistentCity");

		if (!persistentCity.getPrimaryKey().equals(city.getPrimaryKey()))
			throw new IllegalArgumentException("persistentCity.primaryKey != city.primaryKey!!!");

		if (JDOHelper.getObjectId(persistentCity) == null)
			throw new IllegalArgumentException("persistentCity is neither persistent nor detached! Could not obtain an object-id!");

		Location n = new Location(organisationID, locationID, persistentCity);
		n.name.copyFrom(this.name);
		return n;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof Location)) return false;
		Location  o = (Location) obj;
		return
				Util.equals(this.countryID, o.countryID) &&
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.locationID, o.locationID);
	}
	@Override
	public int hashCode()
	{
		return
				Util.hashCode(countryID) ^
				Util.hashCode(organisationID) ^
				Util.hashCode(locationID);
	}
}
