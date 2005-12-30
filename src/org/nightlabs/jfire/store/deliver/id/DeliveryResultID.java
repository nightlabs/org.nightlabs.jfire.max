/*
 * Created on Jun 6, 2005
 */
package org.nightlabs.jfire.store.deliver.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DeliveryResultID extends BaseObjectID
{
	public String organisationID;
	public String deliveryResultID;

	public DeliveryResultID()
	{
	}

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public DeliveryResultID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static DeliveryResultID create(String organisationID, String deliveryResultID)
	{
		DeliveryResultID n = new DeliveryResultID();
		n.organisationID = organisationID;
		n.deliveryResultID = deliveryResultID;
		return n;
	}
}
