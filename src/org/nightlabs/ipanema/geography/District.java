/*
 * Created on Feb 2, 2005
 */
package org.nightlabs.ipanema.geography;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.nightlabs.ipanema.geography.id.DistrictID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.ipanema.geography.id.DistrictID"
 *		detachable = "true"
 *		table = "JFireGeography_District"
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class District implements Serializable // , StoreCallback
{
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
	protected transient GeographySystem geographySystem;

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
	 *		dependent="true"
	 *		table = "JFireGeography_District_zips"
	 *
	 * @jdo.join
	 */
	protected Set zips = new HashSet();

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
	public District(GeographySystem geographySystem, String organisationID, String districtID, City city)
	{
		if (organisationID == null)
			throw new NullPointerException("organisationID");

		if (districtID == null)
			throw new NullPointerException("districtID");

		this.geographySystem = geographySystem;
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
	public Collection getZips()
	{
		if (geographySystem != null)
			geographySystem.needZips(countryID);

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
}
