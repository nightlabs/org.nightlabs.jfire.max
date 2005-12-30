/*
 * Created on Jun 6, 2005
 */
package org.nightlabs.jfire.accounting.pay.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class PaymentID extends BaseObjectID
{
	public String organisationID;
	public String paymentID;

	public PaymentID()
	{
	}

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public PaymentID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static PaymentID create(String organisationID, String paymentID)
	{
		PaymentID n = new PaymentID();
		n.organisationID = organisationID;
		n.paymentID = paymentID;
		return n;
	}

	public static PaymentID create(PaymentDataID paymentDataID)
	{
		return create(paymentDataID.organisationID, paymentDataID.paymentID);
	}
}
