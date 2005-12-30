/*
 * Created on Jun 6, 2005
 */
package org.nightlabs.jfire.accounting.pay.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class PaymentDataID extends BaseObjectID
{
	public String organisationID;
	public String paymentID;

	public PaymentDataID()
	{
	}

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public PaymentDataID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static PaymentDataID create(PaymentID paymentID)
	{
		return create(paymentID.organisationID, paymentID.paymentID);
	}

	public static PaymentDataID create(String organisationID, String paymentID)
	{
		PaymentDataID n = new PaymentDataID();
		n.organisationID = organisationID;
		n.paymentID = paymentID;
		return n;
	}
}
