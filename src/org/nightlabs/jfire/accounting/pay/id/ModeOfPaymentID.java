/*
 * Created on May 31, 2005
 */
package org.nightlabs.jfire.accounting.pay.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ModeOfPaymentID extends BaseObjectID
{
	public String organisationID;
	public String modeOfPaymentID;

	public ModeOfPaymentID() { }

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public ModeOfPaymentID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static ModeOfPaymentID create(String organisationID, String modeOfPaymentID)
	{
		ModeOfPaymentID n = new ModeOfPaymentID();
		n.organisationID = organisationID;
		n.modeOfPaymentID = modeOfPaymentID;
		return n;
	}
}
