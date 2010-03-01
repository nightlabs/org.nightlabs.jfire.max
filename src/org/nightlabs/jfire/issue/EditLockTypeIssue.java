package org.nightlabs.jfire.issue;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.editlock.EditLockType;
import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * An extended class of {@link EditLockType} that used for an editor in the RCP project.
 * 
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *		persistence-capable-superclass="org.nightlabs.jfire.editlock.EditLockType"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
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

	/**
	 * {@inheritDoc}
	 */
	public EditLockTypeIssue(String organisationID, String editLockTypeID)
	{
		super(organisationID, editLockTypeID);
	}
}
