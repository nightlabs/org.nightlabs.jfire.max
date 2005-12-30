/*
 * Created on May 31, 2005
 */
package org.nightlabs.jfire.accounting.pay.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ModeOfPaymentNameID extends BaseObjectID
{
	public String organisationID;
	public String modeOfPaymentID;

	public ModeOfPaymentNameID() { }

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public ModeOfPaymentNameID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static ModeOfPaymentNameID create(String organisationID, String modeOfPaymentID)
	{
		ModeOfPaymentNameID n = new ModeOfPaymentNameID();
		n.organisationID = organisationID;
		n.modeOfPaymentID = modeOfPaymentID;
		return n;
	}
}
