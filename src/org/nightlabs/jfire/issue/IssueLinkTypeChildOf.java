package org.nightlabs.jfire.issue;

import org.nightlabs.jfire.idgenerator.IDGenerator;

public class IssueLinkTypeChildOf
extends IssueLinkType
{
	private static final long serialVersionUID = 1L;
	
	public IssueLinkTypeChildOf(String organisationID, String issueLinkTypeID) {
		super(organisationID, issueLinkTypeID);
	}
	
	@Override
	protected void afterCreateIssueLink(IssueLink newIssueLink) {
		Issue issue = (Issue)getPersistenceManager().detachCopy(newIssueLink.getLinkedObject());
		issue.getIssueLinks().add(new IssueLink(issue.getOrganisationID(), IDGenerator.nextID(IssueLink.class), issue, this, issue));
	}
}
