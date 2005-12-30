/*
 * Created on Feb 2, 2005
 */
package org.nightlabs.ipanema.geography;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.ipanema.geography.id.RegionNameID"
 *		detachable = "true"
 *		table = "JFireGeography_RegionName"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.fetch-group name="Region.name" fields="region, names"
 */
public class RegionName extends I18nText
{
	/////// begin primary key ///////
	/**
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
	private String regionID;
	/////// end primary key ///////

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Region region;

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
	 *		table="JFireGeography_RegionName_names"
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

	protected RegionName()
	{
	}

	public RegionName(Region region)
	{
		this.countryID = region.getCountryID();
		this.organisationID = region.getOrganisationID();
		this.regionID = region.getRegionID();
		this.region = region;
	}

	/**
	 * @return Returns the countryID.
	 */
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
	 * @return Returns the region.
	 */
	public Region getRegion()
	{
		return region;
	}

	/**
	 * @return Returns the regionID.
	 */
	public String getRegionID()
	{
		return regionID;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap()
	{
		return names;
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
		return region == null ? languageID : region.getPrimaryKey();
	}

}
