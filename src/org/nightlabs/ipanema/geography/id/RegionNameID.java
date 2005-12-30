/*
 * Created on Dec 14, 2004
 */
package org.nightlabs.ipanema.geography.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class RegionNameID extends BaseObjectID
{
	public String countryID;
	public String organisationID;
	public String regionID;

	public RegionNameID()
	{
	}

	public RegionNameID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static RegionNameID create(String countryID, String organisationID, String regionID)
	{
		RegionNameID n = new RegionNameID();
		n.countryID = countryID;
		n.organisationID = organisationID;
		n.regionID = regionID;
		return n;
	}
}
