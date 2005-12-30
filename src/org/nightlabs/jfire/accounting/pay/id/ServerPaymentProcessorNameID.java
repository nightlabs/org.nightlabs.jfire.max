/*
 * Created on Jun 5, 2005
 */
package org.nightlabs.jfire.accounting.pay.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ServerPaymentProcessorNameID extends BaseObjectID
{
	public String organisationID;
	public String serverPaymentProcessorID;

	public ServerPaymentProcessorNameID()
	{
	}

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public ServerPaymentProcessorNameID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static ServerPaymentProcessorNameID create(String organisationID, String serverPaymentProcessorID)
	{
		ServerPaymentProcessorNameID n = new ServerPaymentProcessorNameID();
		n.organisationID = organisationID;
		n.serverPaymentProcessorID = serverPaymentProcessorID;
		return n;
	}

}
