/*
 * Created on May 31, 2005
 */
package org.nightlabs.ipanema.accounting.pay.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ModeOfPaymentFlavourNameID extends BaseObjectID
{
	public String organisationID;
	public String modeOfPaymentFlavourID;

	public ModeOfPaymentFlavourNameID() { }

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public ModeOfPaymentFlavourNameID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static ModeOfPaymentFlavourNameID create(String organisationID, String modeOfPaymentFlavourID)
	{
		ModeOfPaymentFlavourNameID n = new ModeOfPaymentFlavourNameID();
		n.organisationID = organisationID;
		n.modeOfPaymentFlavourID = modeOfPaymentFlavourID;
		return n;
	}
}
