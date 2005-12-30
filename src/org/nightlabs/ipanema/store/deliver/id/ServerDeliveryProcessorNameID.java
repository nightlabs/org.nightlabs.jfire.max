/*
 * Created on Jun 5, 2005
 */
package org.nightlabs.ipanema.store.deliver.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ServerDeliveryProcessorNameID extends BaseObjectID
{
	public String organisationID;
	public String serverDeliveryProcessorID;

	public ServerDeliveryProcessorNameID()
	{
	}

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public ServerDeliveryProcessorNameID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static ServerDeliveryProcessorNameID create(String organisationID, String serverDeliveryProcessorID)
	{
		ServerDeliveryProcessorNameID n = new ServerDeliveryProcessorNameID();
		n.organisationID = organisationID;
		n.serverDeliveryProcessorID = serverDeliveryProcessorID;
		return n;
	}

}
