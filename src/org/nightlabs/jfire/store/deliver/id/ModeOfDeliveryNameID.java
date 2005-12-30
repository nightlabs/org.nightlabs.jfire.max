/*
 * Created on May 31, 2005
 */
package org.nightlabs.jfire.store.deliver.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ModeOfDeliveryNameID extends BaseObjectID
{
	public String organisationID;
	public String modeOfDeliveryID;

	public ModeOfDeliveryNameID() { }

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public ModeOfDeliveryNameID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static ModeOfDeliveryNameID create(String organisationID, String modeOfDeliveryID)
	{
		ModeOfDeliveryNameID n = new ModeOfDeliveryNameID();
		n.organisationID = organisationID;
		n.modeOfDeliveryID = modeOfDeliveryID;
		return n;
	}
}
