/*
 * Created on Jun 11, 2005
 */
package org.nightlabs.ipanema.store.deliver.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DeliveryConfigurationNameID extends BaseObjectID
{
	public String organisationID;
	public String deliveryConfigurationID;

	public DeliveryConfigurationNameID() { }

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public DeliveryConfigurationNameID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static DeliveryConfigurationNameID create(String organisationID, String deliveryConfigurationID)
	{
		DeliveryConfigurationNameID n = new DeliveryConfigurationNameID();
		n.organisationID = organisationID;
		n.deliveryConfigurationID = deliveryConfigurationID;
		return n;
	}

}
