/*
 * Created on Jun 6, 2005
 */
package org.nightlabs.jfire.accounting.pay.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class PaymentResultID extends BaseObjectID
{
	public String organisationID;
	public String paymentResultID;

	public PaymentResultID()
	{
	}

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public PaymentResultID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static PaymentResultID create(String organisationID, String paymentResultID)
	{
		PaymentResultID n = new PaymentResultID();
		n.organisationID = organisationID;
		n.paymentResultID = paymentResultID;
		return n;
	}
}
