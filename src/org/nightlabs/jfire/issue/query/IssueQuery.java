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
	
	@Override
	protected Query prepareQuery() {
		Query q = getPersistenceManager().newQuery(Issue.class);
		StringBuffer filter = new StringBuffer();
		if (issueTypeID != null) {
			filter.append("JDOHelper.getObjectId(this.issueType) == :issueTypeID && ");
		}
		if (issueSeverityTypeID != null) {
			filter.append("JDOHelper.getObjectId(this.issueSeverityType) == :issueSeverityTypeID && ");
		}
		if (issuePriorityID != null) {
			filter.append("JDOHelper.getObjectId(this.issuePriority) == :issuePriorityID && ");
		}
		
		if (reporterID != null) {
			filter.append("JDOHelper.getObjectId(this.reporter) == :reporterID && ");
		}
		
		if (assigneeID != null) {
			filter.append("JDOHelper.getObjectId(this.assignee) == :assigneeID && ");
		}
		filter.append("(1 == 1)");
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
	
	

}
