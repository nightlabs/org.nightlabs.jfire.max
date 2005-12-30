/*
 * Created on Jun 10, 2005
 */
package org.nightlabs.jfire.store.deliver.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DeliveryDataID extends BaseObjectID
{
	public String organisationID;
	public String deliveryID;

	public DeliveryDataID()
	{
	}

	public DeliveryDataID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}
	
	public static DeliveryDataID create(DeliveryID deliveryID)
	{
		return create(deliveryID.organisationID, deliveryID.deliveryID);
	}

	public static DeliveryDataID create(String organisationID, String deliveryID)
	{
		DeliveryDataID n = new DeliveryDataID();
		n.organisationID = organisationID;
		n.deliveryID = deliveryID;
		return n;
	}
}
