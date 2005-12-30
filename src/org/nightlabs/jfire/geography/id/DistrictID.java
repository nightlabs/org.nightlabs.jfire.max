/*
 * Created on Dec 14, 2004
 */
package org.nightlabs.jfire.geography.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DistrictID extends BaseObjectID
{
	public String countryID;
	public String organisationID;
	public String districtID;

	public DistrictID()
	{
	}

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public DistrictID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static DistrictID create(String countryID, String organisationID, String districtID)
	{
		DistrictID n = new DistrictID();
		n.countryID = countryID;
		n.organisationID = organisationID;
		n.districtID = districtID;
		return n;
	}
}
