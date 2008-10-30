package org.nightlabs.jfire.issue;

import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.issue.id.IssueLinkTypeID;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
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
		super(issueLinkTypeID);
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
		IssueLinkType issueLinkTypeToBeDeleted = issueLinkToBeDeleted.getIssueLinkType();
		IssueLinkTypeID issueLinkTypeIDToBeDeleted = (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkTypeToBeDeleted);
		if (issueLinkTypeIDToBeDeleted == null)
			throw new IllegalStateException("JDOHelper.getObjectId(issueLinkToBeDeleted.getIssueLinkType()) returned null!");

		IssueLinkType issueLinkTypeOnAnotherSide = getReverseIssueLinkType(pm, issueLinkTypeIDToBeDeleted);
		
		Issue issueOnOtherSide = (Issue)issueLinkToBeDeleted.getLinkedObject();

		Set<IssueLink> issueLinksOnOtherSide = issueOnOtherSide.getIssueLinks();
		for (IssueLink issueLinkOnOtherSide : issueLinksOnOtherSide) {
			Object anotherSideObject = issueLinkOnOtherSide.getLinkedObject();
			Object thisSideObject = issueLinkToBeDeleted.getIssue();

			if (anotherSideObject.equals(thisSideObject)) {
				if (issueLinkTypeOnAnotherSide.equals(issueLinkOnOtherSide.getIssueLinkType())) 
					issueOnOtherSide.removeIssueLink(issueLinkOnOtherSide);
			}
		}
	}
}
