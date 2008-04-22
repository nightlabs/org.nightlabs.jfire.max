package org.nightlabs.jfire.issue;

import org.nightlabs.jfire.idgenerator.IDGenerator;
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
	protected void postCreateIssueLink(IssueLink newIssueLink) {
		Issue issue = (Issue)getPersistenceManager().detachCopy(newIssueLink.getLinkedObject());
		if (newIssueLink.getIssueLinkType().getIssueLinkTypeID().equals(ISSUE_LINK_TYPE_ID_PARENT.issueLinkTypeID)) 
			issue.getIssueLinks().add(new IssueLink(issue.getOrganisationID(), IDGenerator.nextID(IssueLink.class), newIssueLink.getIssue(), this, issue));
		if (newIssueLink.getIssueLinkType().getIssueLinkTypeID().equals(ISSUE_LINK_TYPE_ID_CHILD.issueLinkTypeID))
			issue.getIssueLinks().add(new IssueLink(issue.getOrganisationID(), IDGenerator.nextID(IssueLink.class), newIssueLink.getIssue(), this, issue));
	}

	@Override
	protected void preDeleteIssueLink(IssueLink issueLinkToBeDeleted) {
		
	}
}
