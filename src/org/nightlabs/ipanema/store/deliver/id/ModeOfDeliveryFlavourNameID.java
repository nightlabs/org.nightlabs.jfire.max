/*
 * Created on May 31, 2005
 */
package org.nightlabs.ipanema.store.deliver.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ModeOfDeliveryFlavourNameID extends BaseObjectID
{
	public String organisationID;
	public String modeOfDeliveryFlavourID;

	public ModeOfDeliveryFlavourNameID() { }

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public ModeOfDeliveryFlavourNameID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static ModeOfDeliveryFlavourNameID create(String organisationID, String modeOfDeliveryFlavourID)
	{
		ModeOfDeliveryFlavourNameID n = new ModeOfDeliveryFlavourNameID();
		n.organisationID = organisationID;
		n.modeOfDeliveryFlavourID = modeOfDeliveryFlavourID;
		return n;
	}
}
