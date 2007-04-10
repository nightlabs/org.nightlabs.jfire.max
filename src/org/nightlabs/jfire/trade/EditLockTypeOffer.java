package org.nightlabs.jfire.trade;

import org.nightlabs.jfire.editlock.EditLockType;
import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *		persistence-capable-superclass="org.nightlabs.jfire.editlock.EditLockType"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class EditLockTypeOffer
extends EditLockType
{
	private static final long serialVersionUID = 1L;

	public static final EditLockTypeID EDIT_LOCK_TYPE_ID = EditLockTypeID.create(Organisation.DEVIL_ORGANISATION_ID, EditLockTypeOffer.class.getName());

	/**
	 * @deprecated Only for JDO!
	 */
	protected EditLockTypeOffer() { }

	public EditLockTypeOffer(EditLockTypeID editLockTypeID)
	{
		super(editLockTypeID);
	}

	public EditLockTypeOffer(String organisationID, String editLockTypeID)
	{
		super(organisationID, editLockTypeID);
	}
}
