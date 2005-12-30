/*
 * Created on May 31, 2005
 */
package org.nightlabs.ipanema.accounting.pay.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ModeOfPaymentFlavourID extends BaseObjectID
{
	public String organisationID;
	public String modeOfPaymentFlavourID;

	public ModeOfPaymentFlavourID() { }

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public ModeOfPaymentFlavourID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static ModeOfPaymentFlavourID create(String organisationID, String modeOfPaymentFlavourID)
	{
		ModeOfPaymentFlavourID n = new ModeOfPaymentFlavourID();
		n.organisationID = organisationID;
		n.modeOfPaymentFlavourID = modeOfPaymentFlavourID;
		return n;
	}
}
