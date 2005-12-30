/*
 * Created on Dec 14, 2004
 */
package org.nightlabs.ipanema.geography.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CountryNameID extends BaseObjectID
{
	public String countryID;
	
	public CountryNameID()
	{
	}

	public CountryNameID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static CountryNameID create(String countryID)
	{
		CountryNameID n = new CountryNameID();
		n.countryID = countryID;
		return n;
	}
}
