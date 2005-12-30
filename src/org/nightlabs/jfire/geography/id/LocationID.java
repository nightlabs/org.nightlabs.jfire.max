/*
 * Created on Dec 14, 2004
 */
package org.nightlabs.jfire.geography.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class LocationID extends BaseObjectID
{
	public String countryID;
	public String organisationID;
	public String locationID;

	public LocationID()
	{
	}

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public LocationID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static LocationID create(String countryID, String organisationID, String locationID)
	{
		LocationID n = new LocationID();
		n.countryID = countryID;
		n.organisationID = organisationID;
		n.locationID = locationID;
		return n;
	}
}
