/*
 * Created on Feb 2, 2005
 */
package org.nightlabs.jfire.geography;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.geography.id.LocationNameID"
 *		detachable = "true"
 *		table = "JFireGeography_LocationName"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.fetch-group name="Location.name" fields="location, names"
 */
public class LocationName extends I18nText
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
	 * @jdo.column length="100"
	 */
	private String locationID;
	/////// end primary key ///////

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Location location;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		dependent="true"
	 *		table="JFireGeography_LocationName_names"
	 *
	 * @jdo.join
	 */
	protected Map names = new HashMap();
	
	/**
	 * This variable contains the name in a certain language after localization.
	 *
	 * @see #localize(String)
	 * @see #detachCopyLocalized(String, javax.jdo.PersistenceManager)
	 *
	 * @jdo.field persistence-modifier="transactional" default-fetch-group="false"
	 */
	protected String name;

	protected LocationName()
	{
	}

	public LocationName(Location location)
	{
		this.location = location;
		this.countryID = location.getCountryID();
		this.organisationID = location.getOrganisationID();
		this.locationID = location.getLocationID();
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap()
	{
		return names;
	}

	public Location getLocation()
	{
		return location;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#setText(java.lang.String)
	 */
	protected void setText(String localizedValue)
	{
		this.name = localizedValue;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getText()
	 */
	public String getText()
	{
		return name;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return Location.getPrimaryKey(countryID, organisationID, locationID);
	}
}
