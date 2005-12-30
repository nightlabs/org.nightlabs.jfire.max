/*
 * Created on Feb 27, 2005
 */
package org.nightlabs.ipanema.accounting.tariffpriceconfig.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class PriceCoordinateID extends BaseObjectID
{
	public String organisationID;
	public long priceCoordinateID;

	public PriceCoordinateID()
	{
	}

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public PriceCoordinateID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public PriceCoordinateID create(String organisationID, long priceCoordinateID)
	{
		PriceCoordinateID n = new PriceCoordinateID();
		n.organisationID = organisationID;
		n.priceCoordinateID = priceCoordinateID;
		return n;
	}
}
