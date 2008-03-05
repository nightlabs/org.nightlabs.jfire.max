package org.nightlabs.jfire.issue.query;

import java.util.Date;
import java.util.Set;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueResolutionID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.security.id.UserID;

/**
 * 
 * @author Chairat Kongarayawetchakun <!-- chairat at nightlabs dot de -->
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class IssueQuery 
extends AbstractJDOQuery<Issue>
{
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
		Query q = getPersistenceManager().newQuery(getResultType());
		StringBuilder filter = new StringBuilder("true");
//		StringBuffer stringNames = new StringBuffer();
		
		if (issueSubject != null) {
			filter.append("\n && subject.names.containsValue(varSubject) && varSubject.toLowerCase().matches(:issueSubject) ");
//			q.declareVariables(String.class.getName() + " varSubject");
		}
		
		if (issueComment != null) {
			filter.append("\n && comments.contains(varComment) && varComment.text.toLowerCase().matches(:issueComment) ");
//			q.declareVariables(IssueComment.class.getName() + " varComment");
		}
		
		if (issueSubjectNComment != null) {
			filter.append("\n && comments.contains(varComment) && varComment.text.toLowerCase().matches(:issueSubjectNComment) ");
			filter.append("\n && subject.names.containsValue(varSubject) && varSubject.toLowerCase().matches(:issueSubject) ");
//			q.declareVariables(IssueComment.class.getName() + " varComment");
//			q.declareVariables(String.class.getName() + " varSubject");
		}

		if (issueTypeID != null) {
//			filter.append("JDOHelper.getObjectId(this.issueType) == :issueTypeID && ");
			filter.append("\n && issueType.organisationID == :issueTypeID.organisationID ");
			filter.append("\n && issueType.issueTypeID == :issueTypeID.issueTypeID ");
		}
		
		if (issueSeverityTypeID != null) {
			filter.append("\n && this.issueSeverityType.organisationID == :issueSeverityTypeID.organisationID ");
			filter.append("\n && this.issueSeverityType.issueSeverityTypeID == :issueSeverityTypeID.issueSeverityTypeID ");
		}
		
		if (issuePriorityID != null) {
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//			filter.append("JDOHelper.getObjectId(this.issuePriority) == :issuePriorityID && ");
			// WORKAROUND:
			filter.append("\n && this.issuePriority.organisationID == :issuePriorityID.organisationID ");
			filter.append("\n &&  this.issuePriority.issuePriorityID == :issuePriorityID.issuePriorityID ");
		}
		
		if (issueResolutionID != null) {
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//			filter.append("JDOHelper.getObjectId(this.issueResolution) == :issueResolutionID && ");
			// WORKAROUND:
			filter.append("\n && this.issueResolution.organisationID == :issueResolutionID.organisationID ");
			filter.append("\n && this.issueResolution.issueResolutionID == :issueResolutionID.issueResolutionID ");
		}
		
		if (reporterID != null) {
//			filter.append("JDOHelper.getObjectId(this.reporter) == :reporterID && ");
			filter.append("\n && this.reporter.organisationID == :reporterID.organisationID ");
			filter.append("\n && this.reporter.userID == :reporterID.userID ");
		}
		
		if (assigneeID != null) {
//			filter.append("JDOHelper.getObjectId(this.assignee) == :assigneeID && ");
			filter.append("\n && this.assignee.organisationID == :assigneeID.organisationID ");
			filter.append("\n && this.assignee.userID == :assigneeID.userID ");
		}
	
		if (createTimestamp != null) {
//			filter.append("JDOHelper.getObjectId(this.createTimestamp) == :createTimestamp && ");
			filter.append("\n && this.createTimestamp >= :createTimestamp ");
		}
		
		if (updateTimestamp != null) {
//			filter.append("JDOHelper.getObjectId(this.updateTimestamp) == :updateTimestamp && ");
			filter.append("\n && this.updateTimestamp >= :updateTimestamp ");
		}
		
		// FIXME: chairat please rewrite this part as soon as you have refactored the linkage of objects to Issues. (marius)
//		if (objectIDs != null && !objectIDs.isEmpty())
//		{
//			filter.append("\n && ( ");
//			filter.append("\n \t this.referencedObjectIDs.contains(varObjectID) && \n \t (");
//			for (ObjectID objectID : objectIDs)
//			{
//				String objectIDString = objectID.toString();
//				filter.append("\n \t \t varObjectID.matches(" + objectIDString + ") ||");
//			}
//			filter.delete(filter.length() - 2, filter.length());
//			filter.append("\n \t )");
//			filter.append("\n && )");
//		}
		
		logger.info(filter.toString());
		q.setFilter(filter.toString());
		return q;
	}

	public String getIssueSubject()
	{
		return issueSubject;
	}
	
	public void setIssueSubject(String issueSubject)
	{
		if (issueSubject == null || issueSubject.length() == 0)
		{
			this.issueSubject = null;
		}
		else
		{
			this.issueSubject = ".*" + issueSubject.toLowerCase() + ".*";
		}
	}
	
	public void setIssueSubjectNComment(String issueSubjectNComment)
	{
		if (issueSubjectNComment == null || issueSubjectNComment.length() == 0)
		{
			this.issueSubjectNComment = null;			
		}
		else
		{
			this.issueSubjectNComment = ".*" + issueSubjectNComment.toLowerCase() + ".*";
		}
	}
	
	public String getIssueSubjectNComment() {
		return issueSubjectNComment;
	}
	
	public String getIssueComment() {
		return issueComment;
	}
	
	public void setIssueComment(String issueComment)
	{
		if (issueComment == null || issueComment.length() == 0)
		{
			this.issueComment = null;
		}
		else
		{
			this.issueComment = ".*" + issueComment.toLowerCase() + ".*";
		}
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
