/*
 * Created on Aug 4, 2005
 */
package org.nightlabs.ipanema.geography;

import java.io.Serializable;

import javax.jdo.JDOHelper;

import org.nightlabs.ipanema.geography.id.LocationID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.ipanema.geography.id.LocationID"
 *		detachable = "true"
 *		table = "JFireGeography_Location"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.fetch-group name="Location.name" fields="name"
 * @jdo.fetch-group name="Location.city" fields="city"
 */
public class Location implements Serializable
{
	public static final String FETCH_GROUP_NAME = "Location.name";
	public static final String FETCH_GROUP_CITY = "Location.city";

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
	private String locationID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	protected transient GeographySystem geographySystem;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private City city;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private District district;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LocationName name;

	/**
	 * @deprecated Only for JDO!
	 */
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

	public Location(GeographySystem geographySystem, String organisationID, String locationID, District district)
	{
		init(geographySystem, organisationID, locationID, null, district);
	}

	public Location(GeographySystem geographySystem, String organisationID, String locationID, City city)
	{
		init(geographySystem, organisationID, locationID, city, null);
	}

	protected void init(GeographySystem geographySystem, String organisationID, String locationID, City city, District district)
	{
		if (organisationID == null)
			throw new NullPointerException("organisationID");

		if (locationID == null)
			throw new NullPointerException("locationID");

		if (city == null && district == null)
			throw new IllegalArgumentException("Both city and district are null! One of them must be defined!");

		if (city == null)
			city = district.getCity();

		this.geographySystem = geographySystem;
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
		n.name.load(this.name);
		return n;
	}
}
