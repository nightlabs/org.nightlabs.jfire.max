/*
 * Created on Jan 12, 2005
 */
package org.nightlabs.ipanema.accounting.tariffpriceconfig.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class PriceCellID extends BaseObjectID
{

	public String organisationID;
	public long priceConfigID;
	public long priceID;

	public PriceCellID() { }

	public PriceCellID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static PriceCellID create(String organisationID, long priceConfigID, long priceID)
	{
		PriceCellID n = new PriceCellID();
		n.organisationID = organisationID;
		n.priceConfigID = priceConfigID;
		n.priceID = priceID;
		return n;
	}

}
