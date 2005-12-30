/*
 * Created on Dec 14, 2004
 */
package org.nightlabs.ipanema.geography.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CountryID extends BaseObjectID
{
	public String countryID;
	
	public CountryID()
	{
	}

	public CountryID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static CountryID create(String countryID)
	{
		CountryID n = new CountryID();
		n.countryID = countryID;
		return n;
	}
}
