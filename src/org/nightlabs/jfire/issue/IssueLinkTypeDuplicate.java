package org.nightlabs.jfire.issue;

import javax.jdo.PersistenceManager;

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
public class IssueLinkTypeDuplicate
extends IssueLinkTypeIssueToIssue
{
	private static final long serialVersionUID = 1L;

	public static final IssueLinkTypeID ISSUE_LINK_TYPE_ID_DUPLICATE = IssueLinkTypeID.create(Organisation.DEV_ORGANISATION_ID, "duplicate");

	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueLinkTypeDuplicate() { }

	public IssueLinkTypeDuplicate(IssueLinkTypeID issueLinkTypeID) {
		super(issueLinkTypeID);
		if (!ISSUE_LINK_TYPE_ID_DUPLICATE.equals(issueLinkTypeID))
			throw new IllegalArgumentException("Illegal issueLinkTypeID! Only ISSUE_LINK_TYPE_ID_DUPLICATE is allowed! " + issueLinkTypeID);
	}

	@Override
	public IssueLinkType getReverseIssueLinkType(PersistenceManager pm, IssueLinkTypeID newIssueLinkTypeID) {
		return null;
	}
	
//	@Override
//	protected void preDeleteIssueLink(PersistenceManager pm, IssueLink issueLinkToBeDeleted) {
//		// remove the reverse link
//		IssueLinkType issueLinkTypeDuplicate = issueLinkToBeDeleted.getIssueLinkType();
//		IssueLinkTypeID issueLinkTypeDuplicateID = (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkTypeDuplicate);
//		if (issueLinkTypeDuplicateID == null)
//			throw new IllegalStateException("JDOHelper.getObjectId(issueLinkToBeDeleted.getIssueLinkType()) returned null!");
//		
//		Issue issueOnOtherSide = (Issue)issueLinkToBeDeleted.getLinkedObject();
//		
//		Set<IssueLink> issueLinksOnOtherSide = issueOnOtherSide.getIssueLinks();
//		for (IssueLink issueLinkOnOtherSide : issueLinksOnOtherSide) {
//			Object otherSideObject = issueLinkOnOtherSide.getLinkedObject();
//			Object thisSideObject = issueLinkToBeDeleted.getIssue();
//			
//			if (otherSideObject.equals(thisSideObject)) {
//				if (issueLinkTypeDuplicate.equals(issueLinkOnOtherSide.getIssueLinkType())) 
//						issueOnOtherSide.removeIssueLink(issueLinkOnOtherSide);
//			}
//		}
//	}
}
