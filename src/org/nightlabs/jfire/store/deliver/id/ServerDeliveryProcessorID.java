/*
 * Created on Jun 5, 2005
 */
package org.nightlabs.jfire.store.deliver.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ServerDeliveryProcessorID extends BaseObjectID
{
	public String organisationID;
	public String serverDeliveryProcessorID;

	public ServerDeliveryProcessorID()
	{
	}

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public ServerDeliveryProcessorID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static ServerDeliveryProcessorID create(String organisationID, String serverDeliveryProcessorID)
	{
		ServerDeliveryProcessorID n = new ServerDeliveryProcessorID();
		n.organisationID = organisationID;
		n.serverDeliveryProcessorID = serverDeliveryProcessorID;
		return n;
	}

}
