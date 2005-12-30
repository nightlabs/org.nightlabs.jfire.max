/*
 * Created on May 31, 2005
 */
package org.nightlabs.jfire.store.deliver.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ModeOfDeliveryID extends BaseObjectID
{
	public String organisationID;
	public String modeOfDeliveryID;

	public ModeOfDeliveryID() { }

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public ModeOfDeliveryID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static ModeOfDeliveryID create(String organisationID, String modeOfDeliveryID)
	{
		ModeOfDeliveryID n = new ModeOfDeliveryID();
		n.organisationID = organisationID;
		n.modeOfDeliveryID = modeOfDeliveryID;
		return n;
	}
}
