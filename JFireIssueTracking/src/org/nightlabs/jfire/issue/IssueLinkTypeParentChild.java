package org.nightlabs.jfire.issue;

import javax.jdo.JDOHelper;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * This extended class of {@link IssueLinkTypeIssueToIssue} used for creating parent-child relation between {@link Issue}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class IssueLinkTypeParentChild extends IssueLinkTypeIssueToIssue {
	private static final long serialVersionUID = 1L;

	public static final IssueLinkTypeID ISSUE_LINK_TYPE_ID_PARENT = IssueLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "parent");
	public static final IssueLinkTypeID ISSUE_LINK_TYPE_ID_CHILD = IssueLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "child");

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
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
	protected IssueLinkTypeID getReverseIssueLinkTypeID() {
		IssueLinkTypeID issueLinkTypeID = (IssueLinkTypeID) JDOHelper.getObjectId(this);
		if (issueLinkTypeID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(this) returned null! " + this);

		if (ISSUE_LINK_TYPE_ID_PARENT.equals(issueLinkTypeID))
			return ISSUE_LINK_TYPE_ID_CHILD;

		if (ISSUE_LINK_TYPE_ID_CHILD.equals(issueLinkTypeID))
			return ISSUE_LINK_TYPE_ID_PARENT;

		throw new IllegalStateException("IssueLinkTypeID of this instance is illegal! " + this);
	}

//	@Override
//	public IssueLinkType getReverseIssueLinkType(PersistenceManager pm, IssueLinkTypeID newIssueLinkTypeID) {
//		IssueLinkType issueLinkTypeForOtherSide = null;
//		if (ISSUE_LINK_TYPE_ID_PARENT.equals(newIssueLinkTypeID))
//			issueLinkTypeForOtherSide = (IssueLinkType) pm.getObjectById(ISSUE_LINK_TYPE_ID_CHILD);
//
//		if (ISSUE_LINK_TYPE_ID_CHILD.equals(newIssueLinkTypeID))
//			issueLinkTypeForOtherSide = (IssueLinkType) pm.getObjectById(ISSUE_LINK_TYPE_ID_PARENT);
//		return issueLinkTypeForOtherSide;
//	}
}
