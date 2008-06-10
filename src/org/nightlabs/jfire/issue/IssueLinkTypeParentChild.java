package org.nightlabs.jfire.issue;

import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.issue.dao.IssueDAO;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.issue.IssueLinkTypeIssueToIssue"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */ 
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
	
//	@Override
//	protected void preDeleteIssueLink(PersistenceManager pm, IssueLink issueLinkToBeDeleted) {
//		// remove the reverse link
//		IssueLinkType issueLinkTypeToBeDeleted = issueLinkToBeDeleted.getIssueLinkType();
//		IssueLinkTypeID issueLinkTypeIDToBeDeleted= (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkTypeToBeDeleted);
//		if (issueLinkTypeIDToBeDeleted == null)
//			throw new IllegalStateException("JDOHelper.getObjectId(issueLinkToBeDeleted.getIssueLinkType()) returned null!");
//
//		IssueLinkType issueLinkTypeForOtherSide = null;
//		if (ISSUE_LINK_TYPE_ID_PARENT.equals(issueLinkTypeIDToBeDeleted))
//			issueLinkTypeForOtherSide = (IssueLinkType) getPersistenceManager().getObjectById(ISSUE_LINK_TYPE_ID_CHILD);
//
//		if (ISSUE_LINK_TYPE_ID_CHILD.equals(issueLinkTypeIDToBeDeleted))
//			issueLinkTypeForOtherSide = (IssueLinkType) getPersistenceManager().getObjectById(ISSUE_LINK_TYPE_ID_PARENT);
//
//
//
//		Issue issueOnOtherSide = (Issue)issueLinkToBeDeleted.getLinkedObject();
//
//		Set<IssueLink> issueLinksOnOtherSide = issueOnOtherSide.getIssueLinks();
//		for (IssueLink issueLinkOnOtherSide : issueLinksOnOtherSide) {
//			Object otherSideObject = issueLinkOnOtherSide.getLinkedObject();
//			Object thisSideObject = issueLinkToBeDeleted.getIssue();
//			
//			if (otherSideObject.equals(thisSideObject)) {
//				if (issueLinkTypeForOtherSide.equals(issueLinkOnOtherSide.getIssueLinkType())) 
//						issueOnOtherSide.removeIssueLink(issueLinkOnOtherSide);
//			}
//		}
//	}
}
