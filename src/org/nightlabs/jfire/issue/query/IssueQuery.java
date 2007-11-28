package org.nightlabs.jfire.issue.query;

import java.util.Date;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.security.id.UserID;

public class IssueQuery 
extends JDOQuery<Issue> {
	
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(IssueQuery.class);
	
	private IssueTypeID issueTypeID;
	private IssueSeverityTypeID issueSeverityTypeID;
	private IssuePriorityID issuePriorityID;
	private UserID reporterID;
	private UserID assigneeID;
	private Date createTimestamp;
	private Date updateTimestamp;
	
	@Override
	protected Query prepareQuery() {
		Query q = getPersistenceManager().newQuery(Issue.class);
		StringBuffer filter = new StringBuffer();
		if (issueTypeID != null) {
//			filter.append("JDOHelper.getObjectId(this.issueType) == :issueTypeID && ");
			filter.append("( this.issueType.organisationID == \"" + issueTypeID.organisationID + "\" && ");
			filter.append("  this.issueType.issueTypeID == \"" + issueTypeID.issueTypeID + "\" ) && ");
		}
		if (issueSeverityTypeID != null) {
			filter.append("( this.issueSeverityType.organisationID == \"" + issueSeverityTypeID.organisationID + "\" && ");
			filter.append("  this.issueSeverityType.issueSeverityTypeID == \"" + issueSeverityTypeID.issueSeverityTypeID + "\" ) && ");
		}
		if (issuePriorityID != null) {
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//			filter.append("JDOHelper.getObjectId(this.issuePriority) == :issuePriorityID && ");
			// WORKAROUND:
			filter.append("( this.issuePriority.organisationID == \"" + issuePriorityID.organisationID + "\" && ");
			filter.append("  this.issuePriority.issuePriorityID == \"" + issuePriorityID.issuePriorityID + "\" ) && ");
		}
		
		if (reporterID != null) {
//			filter.append("JDOHelper.getObjectId(this.reporter) == :reporterID && ");
			filter.append("( this.reporter.organisationID == \"" + reporterID.organisationID + "\" && ");
			filter.append("  this.reporter.userID == \"" + reporterID.userID + "\" ) && ");
		}
		
		if (assigneeID != null) {
//			filter.append("JDOHelper.getObjectId(this.assignee) == :assigneeID && ");
			filter.append("( this.assign.organisationID == \"" + assigneeID.organisationID + "\" && ");
			filter.append("  this.assign.userID == \"" + assigneeID.userID + "\" ) && ");
		}
	
		if (createTimestamp != null) {
//			filter.append("JDOHelper.getObjectId(this.createTimestamp) == :createTimestamp && ");
			filter.append("( this.createTimestamp >= :createTimestamp ) &&");
		}
		
		if (updateTimestamp != null) {
//			filter.append("JDOHelper.getObjectId(this.updateTimestamp) == :updateTimestamp && ");
			filter.append("( this.updateTimestamp >= :updateTimestamp ) &&");
		}
		filter.append("(1 == 1)");
		logger.info(filter.toString());
		q.setFilter(filter.toString());
		return q;
	}

	public IssueTypeID getIssueTypeID() {
		return issueTypeID;
	}

	public void setIssueTypeID(IssueTypeID issueTypeID) {
		this.issueTypeID = issueTypeID;
	}

	public IssueSeverityTypeID getIssueSeverityTypeID() {
		return issueSeverityTypeID;
	}

	public void setIssueSeverityTypeID(IssueSeverityTypeID issueSeverityTypeID) {
		this.issueSeverityTypeID = issueSeverityTypeID;
	}

	public IssuePriorityID getIssuePriorityID() {
		return issuePriorityID;
	}

	public void setIssuePriorityID(IssuePriorityID issuePriorityID) {
		this.issuePriorityID = issuePriorityID;
	}

	public UserID getReporterID() {
		return reporterID;
	}

	public void setReporterID(UserID reporterID) {
		this.reporterID = reporterID;
	}

	public UserID getAssigneeID() {
		return assigneeID;
	}

	public void setAssigneeID(UserID assigneeID) {
		this.assigneeID = assigneeID;
	}

	public Date getCreateTimestamp() {
		return createTimestamp;
	}

	public void setCreateTimestamp(Date createTimestamp) {
		this.createTimestamp = createTimestamp;
	}
	
	public Date getUpdateTimestamp() {
		return updateTimestamp;
	}

	public void setUpdateTimestamp(Date updateTimestamp) {
		this.updateTimestamp = updateTimestamp;
	}
}
