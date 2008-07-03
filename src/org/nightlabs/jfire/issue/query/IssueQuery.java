package org.nightlabs.jfire.issue.query;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueLink;
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
	extends AbstractJDOQuery
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
	private Set<IssueLink> issueLinks;
	
	// Property IDs used for the PropertyChangeListeners
	private static final String PROPERTY_PREFIX = "IssueQuery.";
	public static final String PROPERTY_ASSIGNEE_ID = PROPERTY_PREFIX + "assigneeID";
	public static final String PROPERTY_CREATE_TIMESTAMP = PROPERTY_PREFIX + "createTimestamp";
	public static final String PROPERTY_ISSUE_COMMENT = PROPERTY_PREFIX + "issueComment";
	public static final String PROPERTY_ISSUE_PRIORITY_ID = PROPERTY_PREFIX + "issuePriorityID";
	public static final String PROPERTY_ISSUE_RESOLUTION_ID = PROPERTY_PREFIX + "issueResolutionID";
	public static final String PROPERTY_ISSUE_SEVERITY_TYPE_ID = PROPERTY_PREFIX + "issueSeverityTypeID";
	public static final String PROPERTY_ISSUE_SUBJECT = PROPERTY_PREFIX + "issueSubject";
	public static final String PROPERTY_ISSUE_SUBJECT_AND_COMMENT = PROPERTY_PREFIX + "issueSubjectNComment";
	public static final String PROPERTY_ISSUE_TYPE_ID = PROPERTY_PREFIX + "issueTypeID";
	public static final String PROPERTY_ISSUE_LINKS = PROPERTY_PREFIX + "issueLinks";
	public static final String PROPERTY_REPORTER_ID = PROPERTY_PREFIX + "reporterID";
	public static final String PROPERTY_UPDATE_TIMESTAMP = PROPERTY_PREFIX + "updateTimestamp";
	
	@Override
	public List<FieldChangeCarrier> getChangedFields(String propertyName)
	{
		final List<FieldChangeCarrier> changedFields = super.getChangedFields(propertyName);
		final boolean allFields = AbstractSearchQuery.PROPERTY_WHOLE_QUERY.equals(propertyName);
		
		if (allFields || PROPERTY_ASSIGNEE_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ASSIGNEE_ID, assigneeID) );
		}
		if (allFields || PROPERTY_CREATE_TIMESTAMP.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_CREATE_TIMESTAMP, createTimestamp) );
		}
		if (allFields || PROPERTY_ISSUE_COMMENT.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ISSUE_COMMENT, removeRegexpSearch(issueComment)) );
		}
		if (allFields || PROPERTY_ISSUE_LINKS.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ISSUE_LINKS, issueLinks) );
		}
		if (allFields || PROPERTY_ISSUE_PRIORITY_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ISSUE_PRIORITY_ID, issuePriorityID) );
		}
		if (allFields || PROPERTY_ISSUE_RESOLUTION_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ISSUE_RESOLUTION_ID, issueResolutionID) );
		}
		if (allFields || PROPERTY_ISSUE_SEVERITY_TYPE_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ISSUE_SEVERITY_TYPE_ID, issueSeverityTypeID) );
		}
		if (allFields || PROPERTY_ISSUE_SUBJECT.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ISSUE_SUBJECT, removeRegexpSearch(issueSubject)) );
		}
		if (allFields || PROPERTY_ISSUE_SUBJECT_AND_COMMENT.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ISSUE_SUBJECT_AND_COMMENT, removeRegexpSearch(issueSubjectNComment)) );
		}
		if (allFields || PROPERTY_ISSUE_TYPE_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ISSUE_TYPE_ID, issueTypeID) );
		}
		if (allFields || PROPERTY_REPORTER_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_REPORTER_ID, reporterID) );
		}
		if (allFields || PROPERTY_UPDATE_TIMESTAMP.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_UPDATE_TIMESTAMP, updateTimestamp) );
		}
		
		return changedFields;
	}
	
	@Override
	protected void prepareQuery(Query q) {
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
			filter.append("\n && subject.names.containsValue(varSubject) && varSubject.toLowerCase().matches(:issueSubjectNComment) ");
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
		if (issueLinks != null && !issueLinks.isEmpty())
		{
			filter.append("\n && ( ");
			filter.append("\n \t this.issueLinks.contains(varIssueLink) && \n \t (");
			for (IssueLink issueLink : issueLinks)
			{
				ObjectID linkedObjectID = issueLink.getLinkedObjectID();
				filter.append("\n \t \t varIssueLink.linkedObjectID.matches(" + linkedObjectID + ") ||");
			}
			filter.delete(filter.length() - 2, filter.length());
			filter.append("\n \t )");
			filter.append("\n && )");
		}
		
//		if (issueLinks != null && !issueLinks.isEmpty())
//		{
//			filter.append("\n && ( ");
//			filter.append("\n \t this.referencedObjectIDs.contains(varObjectID) && \n \t (");
//			for (ObjectID objectID : issueLinks)
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
	}

	public String getIssueSubject()
	{
		return issueSubject;
	}
	
	/**
	 * Helper that removes the '.*' from the beginning and end of the given string.
	 * @param pattern the regexp pattern that should be cleansed of the '.*'
	 * @return the pattern without '.*'.
	 */
	private String removeRegexpSearch(String pattern)
	{
		if (pattern == null)
			return null;
		
		String result = pattern;
		if (pattern.startsWith(".*"))
		{
			result = result.substring(2);
		}
		if (pattern.endsWith(".*"))
		{
			result = result.substring(0, result.length()-2);
		}
		return pattern;
	}
	
	public void setIssueSubject(String issueSubject)
	{
		final String oldIssueSubject = removeRegexpSearch(this.issueSubject);
		if (issueSubject == null || issueSubject.length() == 0)
		{
			this.issueSubject = null;
		}
		else
		{
			this.issueSubject = ".*" + issueSubject.toLowerCase() + ".*";
		}
		notifyListeners(PROPERTY_ISSUE_SUBJECT, oldIssueSubject, issueSubject);
	}
	
	public void setIssueSubjectNComment(String issueSubjectNComment)
	{
		final String oldIssueSubjectNComment = removeRegexpSearch(this.issueSubjectNComment);
		if (issueSubjectNComment == null || issueSubjectNComment.length() == 0)
		{
			this.issueSubjectNComment = null;			
		}
		else
		{
			this.issueSubjectNComment = ".*" + issueSubjectNComment.toLowerCase() + ".*";
		}
		notifyListeners(PROPERTY_ISSUE_SUBJECT_AND_COMMENT, oldIssueSubjectNComment, issueSubjectNComment);
	}
	
	public String getIssueSubjectNComment() {
		return issueSubjectNComment;
	}
	
	public String getIssueComment() {
		return issueComment;
	}
	
	public void setIssueComment(String issueComment)
	{
		final String oldIssueComment = removeRegexpSearch(this.issueComment);
		if (issueComment == null || issueComment.length() == 0)
		{
			this.issueComment = null;
		}
		else
		{
			this.issueComment = ".*" + issueComment.toLowerCase() + ".*";
		}
		notifyListeners(PROPERTY_ISSUE_COMMENT, oldIssueComment, issueComment);
	}
	
	public IssueTypeID getIssueTypeID() {
		return issueTypeID;
	}

	public void setIssueTypeID(IssueTypeID issueTypeID)
	{
		final IssueTypeID oldIssueTypeID = this.issueTypeID;
		this.issueTypeID = issueTypeID;
		notifyListeners(PROPERTY_ISSUE_TYPE_ID, oldIssueTypeID, issueTypeID);
	}

	public IssueSeverityTypeID getIssueSeverityTypeID() {
		return issueSeverityTypeID;
	}

	public void setIssueSeverityTypeID(IssueSeverityTypeID issueSeverityTypeID)
	{
		final IssueSeverityTypeID oldIssueSeverityTypeID = this.issueSeverityTypeID; 
		this.issueSeverityTypeID = issueSeverityTypeID;
		notifyListeners(PROPERTY_ISSUE_SEVERITY_TYPE_ID, oldIssueSeverityTypeID, issueSeverityTypeID);
	}

	public IssuePriorityID getIssuePriorityID() {
		return issuePriorityID;
	}

	public void setIssuePriorityID(IssuePriorityID issuePriorityID)
	{
		final IssuePriorityID oldIssuePriorityID = this.issuePriorityID;
		this.issuePriorityID = issuePriorityID;
		notifyListeners(PROPERTY_ISSUE_PRIORITY_ID, oldIssuePriorityID, issuePriorityID);
	}

	public IssueResolutionID getIssueResolutionID() {
		return issueResolutionID;
	}
	
	public void setIssueResolutionID(IssueResolutionID issueResolutionID)
	{
		final IssueResolutionID oldIssueResolutionID = this.issueResolutionID;
		this.issueResolutionID = issueResolutionID;
		notifyListeners(PROPERTY_ISSUE_RESOLUTION_ID, oldIssueResolutionID, issueResolutionID);
	}
	
	public UserID getReporterID() {
		return reporterID;
	}

	public void setReporterID(UserID reporterID)
	{
		final UserID oldReporterID = this.reporterID;
		this.reporterID = reporterID;
		notifyListeners(PROPERTY_REPORTER_ID, oldReporterID, reporterID);
	}

	public UserID getAssigneeID() {
		return assigneeID;
	}

	public void setAssigneeID(UserID assigneeID)
	{
		final UserID oldAssigneeID = this.assigneeID;
		this.assigneeID = assigneeID;
		notifyListeners(PROPERTY_ASSIGNEE_ID, oldAssigneeID, assigneeID);
	}

	public Date getCreateTimestamp() {
		return createTimestamp;
	}

	public void setCreateTimestamp(Date createTimestamp)
	{
		final Date oldCreateTimestamp = this.createTimestamp;
		this.createTimestamp = createTimestamp;
		notifyListeners(PROPERTY_CREATE_TIMESTAMP, oldCreateTimestamp, createTimestamp);
	}
	
	public Date getUpdateTimestamp() {
		return updateTimestamp;
	}

	public void setUpdateTimestamp(Date updateTimestamp)
	{
		final Date oldUpdateTimestamp = this.updateTimestamp; 
		this.updateTimestamp = updateTimestamp;
		notifyListeners(PROPERTY_UPDATE_TIMESTAMP, oldUpdateTimestamp, updateTimestamp);
	}
	
	public void setIssueLinks(Set<IssueLink> issueLinks)
	{
		final Set<IssueLink> oldIssueLinks = this.issueLinks;
		this.issueLinks = issueLinks;
		notifyListeners(PROPERTY_ISSUE_LINKS, oldIssueLinks, issueLinks);
	}
	
	public Set<IssueLink> getIssueLinks() {
		return issueLinks;
	}

	@Override
	protected Class<Issue> initCandidateClass()
	{
		return Issue.class;
	}
}
