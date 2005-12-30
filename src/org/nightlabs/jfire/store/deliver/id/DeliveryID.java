/*
 * Created on Jun 10, 2005
 */
package org.nightlabs.jfire.store.deliver.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DeliveryID extends BaseObjectID
{
	public String organisationID;
	public String deliveryID;

	public DeliveryID()
	{
	}

	public DeliveryID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static DeliveryID create(String organisationID, String deliveryID)
	{
		DeliveryID n = new DeliveryID();
		n.organisationID = organisationID;
		n.deliveryID = deliveryID;
		return n;
	}

	public static DeliveryID create(DeliveryDataID deliveryDataID)
	{
		return create(deliveryDataID.organisationID, deliveryDataID.deliveryID);
	}
}
