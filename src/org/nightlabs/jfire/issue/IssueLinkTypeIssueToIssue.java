package org.nightlabs.jfire.issue;

import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.issue.id.IssueLinkTypeID;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.issue.IssueLinkType"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */ 
public abstract class IssueLinkTypeIssueToIssue 
extends IssueLinkType
{
	/**
	 * @deprecated Only for JDO!
	 */
	protected IssueLinkTypeIssueToIssue() { }
	
	public IssueLinkTypeIssueToIssue(IssueLinkTypeID issueLinkTypeID) {
		
	}

	public abstract IssueLinkType getReverseIssueLinkType(PersistenceManager pm, IssueLinkTypeID newIssueLinkTypeID);
	
	@Override
	protected void postCreateIssueLink(PersistenceManager pm, IssueLink newIssueLink) {
		// create a reverse link.
		IssueLinkType issueLinkType = newIssueLink.getIssueLinkType();
		IssueLinkTypeID issueLinkTypeID = (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkType);
		if (issueLinkTypeID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(newIssueLink.getIssueLinkType()) returned null!");

		IssueLinkType issueLinkTypeForAnotherSide = getReverseIssueLinkType(pm, issueLinkTypeID);
		
		Issue issueOnAnotherSide = (Issue) newIssueLink.getLinkedObject();

		// prevent this from causing an ETERNAL recursion.
		// => find out, if the other side already has an issue-link of the required type pointing back
		Set<IssueLink> issueLinksOfIssueOnOtherSide = issueOnAnotherSide.getIssueLinks();
		for (IssueLink issueLinkOfIssueOnOtherSide : issueLinksOfIssueOnOtherSide) {
			if (issueLinkOfIssueOnOtherSide.getIssueLinkType().equals(issueLinkTypeForAnotherSide)  && issueLinkOfIssueOnOtherSide.getLinkedObject().equals(newIssueLink.getIssue())) 
				return;
		}

		issueOnAnotherSide.createIssueLink(issueLinkTypeForAnotherSide, newIssueLink.getIssue());
	}
	
	@Override
	protected void preDeleteIssueLink(PersistenceManager pm, IssueLink issueLinkToBeDeleted) {
		// remove the reverse link
		IssueLinkType issueLinkTypeDuplicate = issueLinkToBeDeleted.getIssueLinkType();
		IssueLinkTypeID issueLinkTypeDuplicateID = (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkTypeDuplicate);
		if (issueLinkTypeDuplicateID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(issueLinkToBeDeleted.getIssueLinkType()) returned null!");

		Issue issueOnOtherSide = (Issue)issueLinkToBeDeleted.getLinkedObject();

		Set<IssueLink> issueLinksOnOtherSide = issueOnOtherSide.getIssueLinks();
		for (IssueLink issueLinkOnOtherSide : issueLinksOnOtherSide) {
			Object otherSideObject = issueLinkOnOtherSide.getLinkedObject();
			Object thisSideObject = issueLinkToBeDeleted.getIssue();

			if (otherSideObject.equals(thisSideObject)) {
				if (issueLinkTypeDuplicate.equals(issueLinkOnOtherSide.getIssueLinkType())) 
					issueOnOtherSide.removeIssueLink(issueLinkOnOtherSide);
			}
		}
	}
}
