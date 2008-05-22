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
 *		persistence-capable-superclass="org.nightlabs.jfire.issue.IssueLinkType"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */ 
public class IssueLinkTypeDuplicate
extends IssueLinkType
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
	protected void postCreateIssueLink(PersistenceManager pm, IssueLink newIssueLink) {
		// create a reverse link.

		IssueLinkType issueLinkTypeDuplicate = newIssueLink.getIssueLinkType();
		IssueLinkTypeID issueLinkTypeDuplicateID = (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkTypeDuplicate);
		if (issueLinkTypeDuplicateID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(newIssueLink.getIssueLinkType()) returned null!");

		Issue issueOnOtherSide = (Issue) newIssueLink.getLinkedObject();

		// prevent this from causing an ETERNAL recursion.
		// => find out, if the other side already has an issue-link of the required type pointing back
		Set<IssueLink> issueLinksOfIssueOnOtherSide = issueOnOtherSide.getIssueLinks();
		for (IssueLink issueLinkOfIssueOnOtherSide : issueLinksOfIssueOnOtherSide) {
			if (issueLinkOfIssueOnOtherSide.getIssueLinkType().equals(issueLinkTypeDuplicate)  && issueLinkOfIssueOnOtherSide.getLinkedObject().equals(newIssueLink.getIssue())) 
				return;
		}

		issueOnOtherSide.createIssueLink(issueLinkTypeDuplicate, newIssueLink.getIssue());
		JDOHelper.getPersistenceManager(issueOnOtherSide).makePersistent(issueOnOtherSide);
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
			if (issueLinkTypeDuplicate.equals(issueLinkOnOtherSide.getIssueLinkType())) {
				if (issueLinkOnOtherSide.getLinkedObject().equals(issueLinkToBeDeleted.getIssue()))
					issueOnOtherSide.removeIssueLink(issueLinkOnOtherSide);
			}
		}
		
		JDOHelper.getPersistenceManager(issueOnOtherSide).makePersistent(issueOnOtherSide);
//		IssueDAO.sharedInstance().storeIssue(issueOnOtherSide, false, new String[]{FetchPlan.DEFAULT}, 0, null);
	}
}
