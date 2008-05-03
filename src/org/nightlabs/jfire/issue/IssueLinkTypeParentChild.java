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
public class IssueLinkTypeParentChild
extends IssueLinkType
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
	protected void postCreateIssueLink(PersistenceManager pm, IssueLink newIssueLink) {
		// create a reverse link - i.e. if we just created a parent-relationship, we need to add a child-relationship on the other side.

		IssueLinkType issueLinkType = newIssueLink.getIssueLinkType();
		IssueLinkTypeID issueLinkTypeID = (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkType);
		if (issueLinkTypeID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(newIssueLink.getIssueLinkType()) returned null!");

		IssueLinkType issueLinkTypeForOtherSide = null;

		if (ISSUE_LINK_TYPE_ID_PARENT.equals(issueLinkTypeID))
			issueLinkTypeForOtherSide = (IssueLinkType) pm.getObjectById(ISSUE_LINK_TYPE_ID_CHILD);

		if (ISSUE_LINK_TYPE_ID_CHILD.equals(issueLinkTypeID))
			issueLinkTypeForOtherSide = (IssueLinkType) pm.getObjectById(ISSUE_LINK_TYPE_ID_PARENT);

		if (issueLinkTypeForOtherSide != null) {
			Issue issueOnOtherSide = (Issue) newIssueLink.getLinkedObject();

			// prevent this from causing an ETERNAL recursion.
			// => find out, if the other side already has an issue-link of the required type pointing back
			Set<IssueLink> issueLinksOfIssueOnOtherSide = issueOnOtherSide.getIssueLinks();
			for (IssueLink issueLinkOfIssueOnOtherSide : issueLinksOfIssueOnOtherSide) {
				if (issueLinkOfIssueOnOtherSide.getIssueLinkType().equals(issueLinkTypeForOtherSide) )
					return;
			}

			issueOnOtherSide.createIssueLink(issueLinkTypeForOtherSide, newIssueLink.getIssue());
			pm.makePersistent(issueOnOtherSide);
		}
	}

	@Override
	protected void preDeleteIssueLink(IssueLink issueLinkToBeDeleted) {
		// remove the reverse link
		IssueLinkType issueLinkTypeToBeDeleted = issueLinkToBeDeleted.getIssueLinkType();
		IssueLinkTypeID issueLinkTypeIDToBeDeleted= (IssueLinkTypeID) JDOHelper.getObjectId(issueLinkTypeToBeDeleted);
		if (issueLinkTypeIDToBeDeleted == null)
			throw new IllegalStateException("JDOHelper.getObjectId(issueLinkToBeDeleted.getIssueLinkType()) returned null!");

		IssueLinkType issueLinkTypeForOtherSide = null;
		if (ISSUE_LINK_TYPE_ID_PARENT.equals(issueLinkTypeIDToBeDeleted))
			issueLinkTypeForOtherSide = (IssueLinkType) getPersistenceManager().getObjectById(ISSUE_LINK_TYPE_ID_CHILD);

		if (ISSUE_LINK_TYPE_ID_CHILD.equals(issueLinkTypeIDToBeDeleted))
			issueLinkTypeForOtherSide = (IssueLinkType) getPersistenceManager().getObjectById(ISSUE_LINK_TYPE_ID_PARENT);



		Issue issueOnOtherSide = (Issue)issueLinkToBeDeleted.getLinkedObject();

		Set<IssueLink> issueLinksOnOtherSide = issueOnOtherSide.getIssueLinks();
		for (IssueLink issueLinkOnOtherSide : issueLinksOnOtherSide) {
			//TODO Not sure yet!!!
			if (issueLinkOnOtherSide.getLinkedObject().equals(issueLinkToBeDeleted.getIssue())) {
				if (issueLinkTypeForOtherSide.equals(issueLinkOnOtherSide.getIssueLinkType())) 
						issueOnOtherSide.removeIssueLink(issueLinkOnOtherSide);
			}
		}
		
		IssueDAO.sharedInstance().storeIssue(issueOnOtherSide, false, new String[]{FetchPlan.DEFAULT}, 0, null);
	}
}
