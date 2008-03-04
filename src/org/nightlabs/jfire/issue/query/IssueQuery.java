package org.nightlabs.jfire.issue.query;

import java.util.Date;
import java.util.Set;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueComment;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueResolutionID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.security.id.UserID;

public class IssueQuery 
extends AbstractJDOQuery<Issue> {
	
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(IssueQuery.class);
	
	private String issueSubject;
	private String issueComment;
	private String issueSubjectNComment;
	private IssueTypeID issueTypeID;
	private IssueSeverityTypeID issueSeverityTypeID;
	private IssuePriorityID issuePriorityID;
	private IssueResolutionID issueResolutionID;
	private UserID reporterID;
	private UserID assigneeID;
	private Date createTimestamp;
	private Date updateTimestamp;
	private Set<ObjectID> objectIDs;
	
	@Override
	protected Query prepareQuery() {
		Query q = getPersistenceManager().newQuery(Issue.class);
		StringBuffer filter = new StringBuffer();
		StringBuffer stringNames = new StringBuffer();
		
		if (issueSubject != null) {
			issueSubject = ".*" + issueSubject.toLowerCase() + ".*";
			filter.append("( this.subject.names.containsValue(varSubject) && varSubject.toLowerCase().matches(\"" + issueSubject + "\") ) &&");
			q.declareVariables(String.class.getName() + " varSubject");
		}
		
		if (issueComment != null) {
			issueComment = ".*" + issueComment.toLowerCase() + ".*";
			filter.append("( this.comments.contains(varComment) && varComment.text.toLowerCase().matches(\"" + issueComment + "\") ) &&");
			q.declareVariables(IssueComment.class.getName() + " varComment");
		}
		
		if (issueSubjectNComment != null) {
			issueSubjectNComment = ".*" + issueSubjectNComment.toLowerCase() + ".*";
			filter.append("( this.comments.contains(varComment) && varComment.text.toLowerCase().matches(\"" + issueSubjectNComment + "\") ) &&");
			filter.append("( this.subject.names.containsValue(varSubject) && varSubject.toLowerCase().matches(\"" + issueSubject + "\") ) &&");
			q.declareVariables(IssueComment.class.getName() + " varComment");
			q.declareVariables(String.class.getName() + " varSubject");
		}

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
		
		if (issueResolutionID != null) {
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//			filter.append("JDOHelper.getObjectId(this.issueResolution) == :issueResolutionID && ");
			// WORKAROUND:
			filter.append("( this.issueResolution.organisationID == \"" + issueResolutionID.organisationID + "\" && ");
			filter.append("  this.issueResolution.issueResolutionID == \"" + issueResolutionID.issueResolutionID + "\" ) && ");
		}
		
		if (reporterID != null) {
//			filter.append("JDOHelper.getObjectId(this.reporter) == :reporterID && ");
			filter.append("( this.reporter.organisationID == \"" + reporterID.organisationID + "\" && ");
			filter.append("  this.reporter.userID == \"" + reporterID.userID + "\" ) && ");
		}
		
		if (assigneeID != null) {
//			filter.append("JDOHelper.getObjectId(this.assignee) == :assigneeID && ");
			filter.append("( this.assignee.organisationID == \"" + assigneeID.organisationID + "\" && ");
			filter.append("  this.assignee.userID == \"" + assigneeID.userID + "\" ) && ");
		}
	
		if (createTimestamp != null) {
//			filter.append("JDOHelper.getObjectId(this.createTimestamp) == :createTimestamp && ");
			filter.append("( this.createTimestamp >= :createTimestamp ) &&");
		}
		
		if (updateTimestamp != null) {
//			filter.append("JDOHelper.getObjectId(this.updateTimestamp) == :updateTimestamp && ");
			filter.append("( this.updateTimestamp >= :updateTimestamp ) &&");
		}
		
		if (objectIDs != null) {
			for (int i = 0; i < objectIDs.size(); i++) {
				ObjectID objectID = objectIDs.iterator().next();
				String objectIDString = objectID.toString();
				filter.append("( this.referencedObjectIDs.contains(varObjectID"+i+") && varObjectID"+i+".matches(\"" + objectIDString + "\") )");
				stringNames.append(String.class.getName() + " varObjectID" + i);
				
				if (i != objectIDs.size() - 1) {
					stringNames.append(";");
					filter.append("||");
				}
			}
			
			q.declareVariables(stringNames.toString());
			
			filter.append("&& ");
		}
		
		filter.append("(1 == 1)");
		logger.info(filter.toString());
		q.setFilter(filter.toString());
		return q;
	}

	public String getIssueSubject() {
		return issueSubject;
	}
	
	public void setIssueSubject(String issueSubject) {
		this.issueSubject = issueSubject;
	}
	
	public void setIssueSubjectNComment(String issueSubjectNComment) {
		this.issueSubjectNComment = issueSubjectNComment;
	}
	
	public String getIssueSubjectNComment() {
		return issueSubjectNComment;
	}
	
	public String getIssueComment() {
		return issueComment;
	}
	
	public void setIssueComment(String issueComment) {
		this.issueComment = issueComment;
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

	public IssueResolutionID getIssueResolutionID() {
		return issueResolutionID;
	}
	
	public void setIssueResolutionID(IssueResolutionID issueResolutionID) {
		this.issueResolutionID = issueResolutionID;
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
	
	public void setObjectIDs(Set<ObjectID> objectIDs) {
		this.objectIDs = objectIDs;
	}
	
	public Set<ObjectID> getObjectIDs() {
		return objectIDs;
	}

	@Override
	protected Class<Issue> init()
	{
		return Issue.class;
	}
}
