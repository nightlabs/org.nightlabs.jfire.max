/*
 * Created on Jun 5, 2005
 */
package org.nightlabs.jfire.accounting.pay.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ServerPaymentProcessorID extends BaseObjectID
{
	public String organisationID;
	public String serverPaymentProcessorID;

	public ServerPaymentProcessorID()
	{
	}

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public ServerPaymentProcessorID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static ServerPaymentProcessorID create(String organisationID, String serverPaymentProcessorID)
	{
		ServerPaymentProcessorID n = new ServerPaymentProcessorID();
		n.organisationID = organisationID;
		n.serverPaymentProcessorID = serverPaymentProcessorID;
		return n;
	}

}
