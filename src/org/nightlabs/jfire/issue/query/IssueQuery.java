package org.nightlabs.jfire.issue.query;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueComment;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueResolutionID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.security.id.UserID;

/**
 * An extended class of {@link AbstractJDOQuery} which intended to allow users setting parameters for querying {@link Issue}.
 * <p>
 * 
 * </p>
 * @author Chairat Kongarayawetchakun <!-- chairat at nightlabs dot de -->
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class IssueQuery
	extends AbstractJDOQuery
{
	private static final long serialVersionUID = 20080811L;

	private static final Logger logger = Logger.getLogger(IssueQuery.class);

	private boolean issueSubjectRegex = false;
	/**
	 * This member is used to create the jdoql query
	 */
	@SuppressWarnings("unused")
	private transient String issueSubjectExpr;
	private String issueSubject;
	private boolean issueCommentRegex = false;
	/**
	 * This member is used to create the jdoql query
	 */
	@SuppressWarnings("unused")
	private transient String issueCommentExpr;
	private String issueComment;
	private boolean issueSubjectNCommentRegex = false;
	/**
	 * This member is used to create the jdoql query
	 */
	@SuppressWarnings("unused")
	private transient String issueSubjectNCommentExpr;
	private String issueSubjectNComment;
	private IssueTypeID issueTypeID;
	private IssueSeverityTypeID issueSeverityTypeID;
	private IssuePriorityID issuePriorityID;
	private IssueResolutionID issueResolutionID;
	private UserID reporterID;
	private UserID assigneeID;
	private Date createTimestamp;
	private Date updateTimestamp;
	private IssueLinkTypeID issueLinkTypeID;
//	private Set<IssueLink> issueLinks;
	private Date issueWorkTimeRangeFrom;
	private Date issueWorkTimeRangeTo;
	private Set<ProjectID> projectIDs;

	/**
	 *  A static class contained all parameters that can be set to the query.
	 *  It's intended to use internally!!
	 */
	public static final class FieldName
	{
		public static final String reporterID = "reporterID";
		public static final String assigneeID = "assigneeID";
		public static final String createTimestamp = "createTimestamp";
		public static final String updateTimestamp = "updateTimestamp";
		public static final String issueTypeID = "issueTypeID";
		public static final String issuePriorityID = "issuePriorityID";
		public static final String issueResolutionID = "issueResolutionID";
		public static final String issueSeverityTypeID = "issueSeverityTypeID";
		public static final String issueSubjectRegex = "issueSubjectRegex";
		public static final String issueSubject = "issueSubject";
		public static final String issueSubjectNCommentRegex = "issueSubjectNCommentRegex";
		public static final String issueSubjectNComment = "issueSubjectNComment";
		public static final String issueCommentRegex = "issueCommentRegex";
		public static final String issueComment = "issueComment";
		public static final String issueLinkTypeID = "issueLinkTypeID";
		public static final String issueWorkTimeRangeFrom = "issueWorkTimeRangeFrom";
		public static final String issueWorkTimeRangeTo = "issueWorkTimeRangeTo";
		public static final String projectIDs = "projectIDs";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareQuery(Query q) {
		StringBuilder filter = new StringBuilder("true");

		if (isFieldEnabled(FieldName.issueSubject) && issueSubject != null) {
			issueSubjectExpr = isIssueSubjectRegex() ? issueSubject : ".*" + issueSubject + ".*";
			filter.append("\n && (subject.names.containsValue(varSubject) && varSubject.toLowerCase().matches(:issueSubjectExpr.toLowerCase())) ");
		}

		if (isFieldEnabled(FieldName.issueComment) && issueComment != null) {
			issueCommentExpr = isIssueCommentRegex() ? issueComment : ".*" + issueComment + ".*";
			filter.append("\n && (comments.contains(varComment) && varComment.text.toLowerCase().matches(:issueCommentExpr.toLowerCase())) ");
		}

		if (isFieldEnabled(FieldName.issueSubjectNComment) && issueSubjectNComment != null) {
			issueSubjectNCommentExpr = isIssueSubjectNCommentRegex() ? issueSubjectNComment : ".*" + issueSubjectNComment + ".*";
			filter.append("\n && ((subject.names.containsValue(varSubject) && varSubject.toLowerCase().matches(:issueSubjectNCommentExpr.toLowerCase()))  ");
			filter.append("\n || (comments.contains(varComment) && varComment.text.toLowerCase().matches(:issueSubjectNCommentExpr.toLowerCase()))) ");
		}

		if (isFieldEnabled(FieldName.issueTypeID) && issueTypeID != null) {
			filter.append("\n && issueType.organisationID == :issueTypeID.organisationID ");
			filter.append("\n && issueType.issueTypeID == :issueTypeID.issueTypeID ");
		}

		if (isFieldEnabled(FieldName.issueSeverityTypeID) && issueSeverityTypeID != null) {
			filter.append("\n && this.issueSeverityType.organisationID == :issueSeverityTypeID.organisationID ");
			filter.append("\n && this.issueSeverityType.issueSeverityTypeID == :issueSeverityTypeID.issueSeverityTypeID ");
		}

		if (isFieldEnabled(FieldName.issuePriorityID) && issuePriorityID != null) {
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//			filter.append("JDOHelper.getObjectId(this.issuePriority) == :issuePriorityID && ");
			// WORKAROUND:
			filter.append("\n && this.issuePriority.organisationID == :issuePriorityID.organisationID ");
			filter.append("\n &&  this.issuePriority.issuePriorityID == :issuePriorityID.issuePriorityID ");
		}

		if (isFieldEnabled(FieldName.issueResolutionID) && issueResolutionID != null) {
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//			filter.append("JDOHelper.getObjectId(this.issueResolution) == :issueResolutionID && ");
			// WORKAROUND:
			filter.append("\n && this.issueResolution.organisationID == :issueResolutionID.organisationID ");
			filter.append("\n && this.issueResolution.issueResolutionID == :issueResolutionID.issueResolutionID ");
		}

		if (isFieldEnabled(FieldName.reporterID) && reporterID != null) {
//			filter.append("JDOHelper.getObjectId(this.reporter) == :reporterID && ");
			filter.append("\n && this.reporter.organisationID == :reporterID.organisationID ");
			filter.append("\n && this.reporter.userID == :reporterID.userID ");
		}

		if (isFieldEnabled(FieldName.assigneeID) && assigneeID != null) {
//			filter.append("JDOHelper.getObjectId(this.assignee) == :assigneeID && ");
			filter.append("\n && this.assignee.organisationID == :assigneeID.organisationID ");
			filter.append("\n && this.assignee.userID == :assigneeID.userID ");
		}

		if (isFieldEnabled(FieldName.createTimestamp) && createTimestamp != null) {
//			filter.append("JDOHelper.getObjectId(this.createTimestamp) == :createTimestamp && ");
			filter.append("\n && this.createTimestamp >= :createTimestamp ");
		}

		if (isFieldEnabled(FieldName.updateTimestamp) && updateTimestamp != null) {
//			filter.append("JDOHelper.getObjectId(this.updateTimestamp) == :updateTimestamp && ");
			filter.append("\n && this.updateTimestamp >= :updateTimestamp ");
		}

		if (isFieldEnabled(FieldName.issueLinkTypeID) && issueLinkTypeID != null) {
//			filter.append("\n && (this.issueLinks.contains(varIssueLink) )) ");
			filter.append("\n && (this.issueLinks.contains(varIssueLink) && (varIssueLink.issueLinkType.organisationID == :issueLinkTypeID.organisationID) && (varIssueLink.issueLinkType.issueLinkTypeID == :issueLinkTypeID.issueLinkTypeID)) ");
		}

		if (isFieldEnabled(FieldName.issueWorkTimeRangeFrom) && !isFieldEnabled(FieldName.issueWorkTimeRangeTo) && issueWorkTimeRangeFrom != null) {
			filter.append("\n && (this.issueWorkTimeRanges.contains(varIssueWorkTimeRange) && (varIssueWorkTimeRange.from >= :issueWorkTimeRangeFrom)) ");
		}
		
		if (!isFieldEnabled(FieldName.issueWorkTimeRangeFrom) && isFieldEnabled(FieldName.issueWorkTimeRangeTo) && issueWorkTimeRangeTo != null) {
			filter.append("\n && (this.issueWorkTimeRanges.contains(varIssueWorkTimeRange) && (varIssueWorkTimeRange.to <= :issueWorkTimeRangeTo)) ");
		}
		
		if (isFieldEnabled(FieldName.issueWorkTimeRangeFrom) && isFieldEnabled(FieldName.issueWorkTimeRangeTo) 
				&& issueWorkTimeRangeFrom != null && issueWorkTimeRangeTo != null) {
			filter.append("\n && (this.issueWorkTimeRanges.contains(varIssueWorkTimeRange) && !((varIssueWorkTimeRange.from >= :issueWorkTimeRangeTo) && (varIssueWorkTimeRange.to > :issueWorkTimeRangeTo))) ");
			filter.append("\n || (this.issueWorkTimeRanges.contains(varIssueWorkTimeRange) && !((varIssueWorkTimeRange.to <= :issueWorkTimeRangeFrom) && (varIssueWorkTimeRange.from < :issueWorkTimeRangeFrom))) ");
		}
		
		if (isFieldEnabled(FieldName.projectIDs) && projectIDs != null && projectIDs.size() > 0) {
			int i = 0;
			for ( Iterator<ProjectID> it = projectIDs.iterator(); it.hasNext(); i++) {
				ProjectID pid = it.next();
				if (i == 0) {
					filter.append("\n && this.project.organisationID == \"" + pid.organisationID + "\" && (");
				}
				
				filter.append("this.project.projectID == " + pid.projectID);
				
				if (i != projectIDs.size() - 1) {
					filter.append(" || ");
				}
				else {
					filter.append(")");
				}
			}
			logger.info(filter);
		}
		// FIXME: chairat please rewrite this part as soon as you have refactored the linkage of objects to Issues. (marius)
//		if (issueLinks != null && !issueLinks.isEmpty())
//		{
//			filter.append("\n && ( ");
//			filter.append("\n \t this.issueLinks.contains(varIssueLink) && \n \t (");
//			for (IssueLink issueLink : issueLinks)
//			{
//				ObjectID linkedObjectID = issueLink.getLinkedObjectID();
//				filter.append("\n \t \t varIssueLink.linkedObjectID.matches(" + linkedObjectID + ") ||");
//			}
//			filter.delete(filter.length() - 2, filter.length());
//			filter.append("\n \t )");
//			filter.append("\n && )");
//		}
//
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

	/**
	 * @return a {@link String} of the {@link IssueSubject}.
	 */
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
		this.issueSubject = issueSubject;
		notifyListeners(FieldName.issueSubject, oldIssueSubject, issueSubject);
	}
	
	/**
	 * @return Whether the value set with {@link #setIssueSubject(String)} represents a regular
	 *         expression. If this is <code>true</code>, the value set with {@link #setIssueSubject(String)}
	 *         will be passed directly as matching string, if it is <code>false</code> a regular expression
	 *         will be made out of it by prefixing and suffixing the value with ".*"
	 */
	public boolean isIssueSubjectRegex() {
		return issueSubjectRegex;
	}

	/**
	 * Sets whether the value set with {@link #setIssueSubject(String)} represents a 
	 * regular expression.
	 * 
	 * @param issueSubjectRegex The issueSubjectRegex to search. 
	 */
	public void setIssueSubjectRegex(boolean issueSubjectRegex) {
		final boolean oldIssueSubjectRegex = this.issueSubjectRegex;
		this.issueSubjectRegex = issueSubjectRegex;
		notifyListeners(FieldName.issueSubjectRegex, oldIssueSubjectRegex, issueSubjectRegex);
	}
	

	/**
	 * Set the string to search in the issue subject and comments for. This can either be a regular expression
	 * (set {@link #setIssueSubjectNCommentRegex(boolean)} to <code>true</code> then) or a string
	 * that should be contained in either the subject or one of the comments of the Issues to find.
	 *  
	 * @param issueSubjectNComment The issueComment to set.
	 */
	public void setIssueSubjectNComment(String issueSubjectNComment)
	{
		final String oldIssueSubjectNComment = removeRegexpSearch(this.issueSubjectNComment);
		this.issueSubjectNComment = issueSubjectNComment.toLowerCase();
		notifyListeners(FieldName.issueSubjectNComment, oldIssueSubjectNComment, issueSubjectNComment);
	}

	/**
	 * @return a {@link String} of the {@link IssueSubject} combined with {@link IssueComment}.
	 */
	public String getIssueSubjectNComment() {
		return issueSubjectNComment;
	}
	
	/**
	 * @return Whether the value set with {@link #setIssueSubjectNComment(String)} represents a regular
	 *         expression. If this is <code>true</code>, the value set with {@link #setIssueSubjectNComment(String)}
	 *         will be passed directly as matching string, if it is <code>false</code> a regular expression
	 *         will be made out of it by prefixing and suffixing the value with ".*"
	 */
	public boolean isIssueSubjectNCommentRegex() {
		return issueSubjectNCommentRegex;
	}

	/**
	 * Sets whether the value set with {@link #setIssueSubjectNComment(String)} represents a 
	 * regular expression.
	 * 
	 * @param issueSubjectNCommentRegex The issueSubjectNCommentRegex to search. 
	 */
	public void setIssueSubjectNCommentRegex(boolean issueSubjectNCommentRegex) {
		final boolean oldIssueSubjectNCommentRegex = this.issueSubjectNCommentRegex;
		this.issueSubjectNCommentRegex = issueSubjectNCommentRegex;
		notifyListeners(FieldName.issueSubjectNCommentRegex, oldIssueSubjectNCommentRegex, issueSubjectNCommentRegex);
	}
	

	/**
	 * @return a {@link String} of the {@link IssueComment}.
	 */
	public String getIssueComment() {
		return issueComment;
	}

	/**
	 * Set the string to search in the issue comments for. This can either be a regular expression
	 * (set {@link #setIssueCommentRegex(boolean)} to <code>true</code> then) or a string
	 * that should be contained in one of the comments of the Issues to find.
	 *  
	 * @param issueComment The issueComment to set.
	 */
	public void setIssueComment(String issueComment)
	{
		final String oldIssueComment = removeRegexpSearch(this.issueComment);
		this.issueComment = issueComment;
		notifyListeners(FieldName.issueComment, oldIssueComment, issueComment);
	}

	/**
	 * @return Whether the value set with {@link #setIssueComment(String)} represents a regular
	 *         expression. If this is <code>true</code>, the value set with {@link #setIssueComment(String)}
	 *         will be passed directly as matching string, if it is <code>false</code> a regular expression
	 *         will be made out of it by prefixing and suffixing the value with ".*"
	 */
	public boolean isIssueCommentRegex() {
		return issueCommentRegex;
	}

	/**
	 * Sets whether the value set with {@link #setIssueComment(String)} represents a 
	 * regular expression.
	 * 
	 * @param issueCommentRegex The issueCommentRegex to search. 
	 */
	public void setIssueCommentRegex(boolean issueCommentRegex) {
		final boolean oldIssueCommentRegex = this.issueCommentRegex;
		this.issueSubjectRegex = issueCommentRegex;
		notifyListeners(FieldName.issueSubjectNCommentRegex, oldIssueCommentRegex, issueCommentRegex);
	}
	
	/**
	 * @return an {@link IssueTypeID}.
	 */
	public IssueTypeID getIssueTypeID() {
		return issueTypeID;
	}

	public void setIssueTypeID(IssueTypeID issueTypeID)
	{
		final IssueTypeID oldIssueTypeID = this.issueTypeID;
		this.issueTypeID = issueTypeID;
		notifyListeners(FieldName.issueTypeID, oldIssueTypeID, issueTypeID);
	}

	/**
	 * @return an {@link IssueSeverityTypeID}.
	 */
	public IssueSeverityTypeID getIssueSeverityTypeID() {
		return issueSeverityTypeID;
	}

	public void setIssueSeverityTypeID(IssueSeverityTypeID issueSeverityTypeID)
	{
		final IssueSeverityTypeID oldIssueSeverityTypeID = this.issueSeverityTypeID;
		this.issueSeverityTypeID = issueSeverityTypeID;
		notifyListeners(FieldName.issueSeverityTypeID, oldIssueSeverityTypeID, issueSeverityTypeID);
	}

	/**
	 * @return an {@link IssuePriorityID}.
	 */
	public IssuePriorityID getIssuePriorityID() {
		return issuePriorityID;
	}

	public void setIssuePriorityID(IssuePriorityID issuePriorityID)
	{
		final IssuePriorityID oldIssuePriorityID = this.issuePriorityID;
		this.issuePriorityID = issuePriorityID;
		notifyListeners(FieldName.issuePriorityID, oldIssuePriorityID, issuePriorityID);
	}

	/**
	 * @return an {@link IssueResolutionID}.
	 */
	public IssueResolutionID getIssueResolutionID() {
		return issueResolutionID;
	}

	public void setIssueResolutionID(IssueResolutionID issueResolutionID)
	{
		final IssueResolutionID oldIssueResolutionID = this.issueResolutionID;
		this.issueResolutionID = issueResolutionID;
		notifyListeners(FieldName.issueResolutionID, oldIssueResolutionID, issueResolutionID);
	}

	/**
	 * @return an {@link UserID} of the {@link Issue}'s reporter.
	 */
	public UserID getReporterID() {
		return reporterID;
	}

	public void setReporterID(UserID reporterID)
	{
		final UserID oldReporterID = this.reporterID;
		this.reporterID = reporterID;
		notifyListeners(FieldName.reporterID, oldReporterID, reporterID);
	}

	/**
	 * @return an {@link UserID} of the {@link Issue}'s assignee.
	 */
	public UserID getAssigneeID() {
		return assigneeID;
	}

	public void setAssigneeID(UserID assigneeID)
	{
		final UserID oldAssigneeID = this.assigneeID;
		this.assigneeID = assigneeID;
		notifyListeners(FieldName.assigneeID, oldAssigneeID, assigneeID);
	}

	/**
	 * @return an {@link Date} of the {@link Issue}'s working time range starting time.
	 */
	public Date getIssueWorkTimeRangeFrom() {
		return issueWorkTimeRangeFrom;
	}

	public void setIssueWorkTimeRangeFrom(Date issueWorkTimeRangeFrom)
	{
		final Date oldIssueWorkTimeRangeFrom = this.issueWorkTimeRangeFrom;
		this.issueWorkTimeRangeFrom = issueWorkTimeRangeFrom;
		notifyListeners(FieldName.issueWorkTimeRangeFrom, oldIssueWorkTimeRangeFrom, issueWorkTimeRangeFrom);
	}
	
	/**
	 * @return an {@link Date} of the {@link Issue}'s working time range ending time.
	 */
	public Date getIssueWorkTimeRangeTo() {
		return issueWorkTimeRangeTo;
	}

	public void setIssueWorkTimeRangeTo(Date issueWorkTimeRangeTo)
	{
		final Date oldIssueWorkTimeRangeTo = this.issueWorkTimeRangeTo;
		this.issueWorkTimeRangeTo = issueWorkTimeRangeTo;
		notifyListeners(FieldName.issueWorkTimeRangeTo, oldIssueWorkTimeRangeTo, issueWorkTimeRangeTo);
	}

	/**
	 * @return an {@link IssueLinkTypeID}.
	 */
	public IssueLinkTypeID getIssueLinkTypeID() {
		return issueLinkTypeID;
	}

	public void setIssueLinkTypeID(IssueLinkTypeID issueLinkTypeID)
	{
		final IssueLinkTypeID oldIssueLinkTypeID = this.issueLinkTypeID;
		this.issueLinkTypeID = issueLinkTypeID;
		notifyListeners(FieldName.issueLinkTypeID, oldIssueLinkTypeID, issueLinkTypeID);
	}

	/**
	 * @return a {@link Date} of {@link Issue}'s created time.
	 */
	public Date getCreateTimestamp() {
		return createTimestamp;
	}

	public void setCreateTimestamp(Date createTimestamp)
	{
		final Date oldCreateTimestamp = this.createTimestamp;
		this.createTimestamp = createTimestamp;
		notifyListeners(FieldName.createTimestamp, oldCreateTimestamp, createTimestamp);
	}
	
	/**
	 * @return a {@link Date} of {@link Issue}'s updated time.
	 */
	public Date getUpdateTimestamp() {
		return updateTimestamp;
	}

	public void setUpdateTimestamp(Date updateTimestamp)
	{
		final Date oldUpdateTimestamp = this.updateTimestamp;
		this.updateTimestamp = updateTimestamp;
		notifyListeners(FieldName.updateTimestamp, oldUpdateTimestamp, updateTimestamp);
	}
	
	/**
	 * @return a {@link Set} of {@link ProjectID}.
	 */
	public Set<ProjectID> getProjectIDs() {
		return projectIDs;
	}

	public void setProjectIDs(Set<ProjectID> projectIDs)
	{
		final Set<ProjectID> oldProjectIDs = this.projectIDs;
		this.projectIDs = projectIDs;
		notifyListeners(FieldName.projectIDs, oldProjectIDs, projectIDs);
	}
	
	@Override
	protected Class<Issue> initCandidateClass()
	{
		return Issue.class;
	}
}
