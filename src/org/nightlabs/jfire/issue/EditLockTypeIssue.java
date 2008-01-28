package org.nightlabs.jfire.issue;

import org.nightlabs.jfire.editlock.EditLockType;
import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *		persistence-capable-superclass="org.nightlabs.jfire.editlock.EditLockType"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class EditLockTypeIssue 
extends EditLockType
{
	private static final long serialVersionUID = 1L;

	public static final EditLockTypeID EDIT_LOCK_TYPE_ID = EditLockTypeID.create(Organisation.DEV_ORGANISATION_ID, EditLockTypeIssue.class.getName());

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EditLockTypeIssue() { }

	public EditLockTypeIssue(EditLockTypeID editLockTypeID)
	{
		super(editLockTypeID);
	}

	public EditLockTypeIssue(String organisationID, String editLockTypeID)
	{
		super(organisationID, editLockTypeID);
	}
}
