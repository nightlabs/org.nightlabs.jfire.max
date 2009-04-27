package org.nightlabs.jfire.issue;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.organisation.Organisation;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 * This extended class of {@link IssueLinkTypeIssueToIssue} used for creating parent-child relation between {@link Issue}s.
 * <p>
 * </p>
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.issue.IssueLinkTypeIssueToIssue"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)

public class IssueLinkTypeParentChild
extends IssueLinkTypeIssueToIssue
{
	private static final long serialVersionUID = 1L;

	public static final IssueLinkTypeID ISSUE_LINK_TYPE_ID_PARENT = IssueLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "parent");
	public static final IssueLinkTypeID ISSUE_LINK_TYPE_ID_CHILD = IssueLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "child");

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueLinkTypeParentChild() { }

	/**
	 * 
	 * @param issueLinkTypeID
	 */
	public IssueLinkTypeParentChild(IssueLinkTypeID issueLinkTypeID) {
		super(issueLinkTypeID);
		if (!ISSUE_LINK_TYPE_ID_CHILD.equals(issueLinkTypeID) && !ISSUE_LINK_TYPE_ID_PARENT.equals(issueLinkTypeID))
			throw new IllegalArgumentException("Illegal issueLinkTypeID! Only ISSUE_LINK_TYPE_ID_PARENT and ISSUE_LINK_TYPE_ID_CHILD are allowed! " + issueLinkTypeID);
	}

	@Override
	public IssueLinkType getReverseIssueLinkType(PersistenceManager pm, IssueLinkTypeID newIssueLinkTypeID) {
		IssueLinkType issueLinkTypeForOtherSide = null;
		if (ISSUE_LINK_TYPE_ID_PARENT.equals(newIssueLinkTypeID))
			issueLinkTypeForOtherSide = (IssueLinkType) pm.getObjectById(ISSUE_LINK_TYPE_ID_CHILD);

		if (ISSUE_LINK_TYPE_ID_CHILD.equals(newIssueLinkTypeID))
			issueLinkTypeForOtherSide = (IssueLinkType) pm.getObjectById(ISSUE_LINK_TYPE_ID_PARENT);
		return issueLinkTypeForOtherSide;
	}
}
