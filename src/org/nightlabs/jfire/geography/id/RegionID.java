/*
 * Created on Dec 14, 2004
 */
package org.nightlabs.jfire.geography.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class RegionID extends BaseObjectID
{
	public String countryID;
	public String organisationID;
	public String regionID;

	public RegionID()
	{
	}

	public RegionID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static RegionID create(String countryID, String organisationID, String regionID)
	{
		RegionID n = new RegionID();
		n.countryID = countryID;
		n.organisationID = organisationID;
		n.regionID = regionID;
		return n;
	}
}
