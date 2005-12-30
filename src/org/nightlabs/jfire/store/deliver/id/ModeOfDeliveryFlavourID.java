/*
 * Created on May 31, 2005
 */
package org.nightlabs.jfire.store.deliver.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ModeOfDeliveryFlavourID extends BaseObjectID
{
	public String organisationID;
	public String modeOfDeliveryFlavourID;

	public ModeOfDeliveryFlavourID() { }

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public ModeOfDeliveryFlavourID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static ModeOfDeliveryFlavourID create(String organisationID, String modeOfDeliveryFlavourID)
	{
		ModeOfDeliveryFlavourID n = new ModeOfDeliveryFlavourID();
		n.organisationID = organisationID;
		n.modeOfDeliveryFlavourID = modeOfDeliveryFlavourID;
		return n;
	}
}
