/*
 * Created on Jun 11, 2005
 */
package org.nightlabs.ipanema.store.deliver.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DeliveryConfigurationID extends BaseObjectID
{
	public String organisationID;
	public String deliveryConfigurationID;

	public DeliveryConfigurationID() { }

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public DeliveryConfigurationID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static DeliveryConfigurationID create(String organisationID, String deliveryConfigurationID)
	{
		DeliveryConfigurationID n = new DeliveryConfigurationID();
		n.organisationID = organisationID;
		n.deliveryConfigurationID = deliveryConfigurationID;
		return n;
	}

}
