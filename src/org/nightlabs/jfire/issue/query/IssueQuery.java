package org.nightlabs.jfire.issue.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.Query;

import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueComment;
import org.nightlabs.jfire.issue.IssueSubject;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueResolutionID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(IssueQuery.class);

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

	private Long deadlineTimePeriod;

	private IssueLinkTypeID issueLinkTypeID;
	private Date issueWorkTimeRangeFrom;
	private Date issueWorkTimeRangeTo;
	private Set<ProjectID> projectIDs;

	private String jbpmNodeName;

	private ProcessDefinitionID processDefinitionID;

	private Set<IssueLinkQueryElement> issueLinkQueryElements;

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

		public static final String deadlineTimePeriod = "deadlineTimePeriod";

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

		public static final String jbpmNodeName = "jbpmNodeName";
		public static final String processDefinitionID = "processDefinitionID";

		/**
		 * @deprecated Remove this field!
		 */
		@Deprecated
		public static final String issueLinks = "issueLinks";

		public static final String issueLinkQueryElements = "issueLinkQueryElements";
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
			if (reporterID.equals(User.USER_ID_CURRENT_USER)) {
				setCurrentUserAsReporter();
			}
//			filter.append("JDOHelper.getObjectId(this.reporter) == :reporterID && ");
			filter.append("\n && this.reporter.organisationID == :reporterID.organisationID ");
			filter.append("\n && this.reporter.userID == :reporterID.userID ");
		}

		if (isFieldEnabled(FieldName.assigneeID) && assigneeID != null) {
			if (assigneeID.equals(User.USER_ID_CURRENT_USER)) {
				setCurrentUserAsAssignee();
			}
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

		if (isFieldEnabled(FieldName.deadlineTimePeriod) && deadlineTimePeriod != null) {
			Date deadlineTimeDate = new Date(System.currentTimeMillis() + deadlineTimePeriod);
			filter.append("\n && this.deadlineTimestamp <=  :deadlineTimeDate");
			addParam("deadlineTimeDate", deadlineTimeDate);
		}

		/*if (
				(isFieldEnabled(FieldName.deadlineBeforeTimestamp) && deadlineBeforeTimestamp != null) ||
				(isFieldEnabled(FieldName.deadlineAfterTimestamp) && deadlineAfterTimestamp != null)
		)
		{
			if (deadlineBeforeTimestamp != null)
				filter.append("\n && this.deadlineTimestamp <= :deadlineBeforeTimestamp ");
			if (deadlineAfterTimestamp != null)
				filter.append("\n && this.deadlineTimestamp >= :deadlineAfterTimestamp ");
		}*/

		if (isFieldEnabled(FieldName.issueLinkTypeID) && issueLinkTypeID != null) {
//			filter.append("\n && (this.issueLinks.contains(varIssueLink) )) ");
			addVariable(String.class, "varIssueLink");
			filter.append("\n && (this.issueLinks.contains(varIssueLink) && JDOHelper.getObjectId(varIssueLink.issueLinkType) == :issueLinkTypeID) ");
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

		if (isFieldEnabled(FieldName.projectIDs) && projectIDs != null && !projectIDs.isEmpty()) {
			int i = 0;
			for (Iterator<ProjectID> it = projectIDs.iterator(); it.hasNext(); i++) {
				ProjectID pid = it.next();
				if (pid == null){
					continue;
				}
				if (i == 0){
					filter.append("\n&& (");
				}

				String projectIDParamName = "projectID" + i;
				filter.append("\nJDOHelper.getObjectId(this.project) == :").append(projectIDParamName);
				addParam(projectIDParamName, pid);
				
				if (it.hasNext()) {
					filter.append("\n|| ");
				}else{
					filter.append("\n)");
				}
			}
		}

		if (isFieldEnabled(FieldName.processDefinitionID) && processDefinitionID != null) {
			filter.append("\n && this.issueType.processDefinition.organisationID == :processDefinitionID.organisationID ");
			filter.append("\n && this.issueType.processDefinition.processDefinitionID == :processDefinitionID.processDefinitionID ");
		}

		if (isFieldEnabled(FieldName.jbpmNodeName) && jbpmNodeName != null) {
			filter.append("\n && (this.state.stateDefinition.jbpmNodeName == :jbpmNodeName)");
		}

		if (isFieldEnabled(FieldName.issueLinkQueryElements) && issueLinkQueryElements != null) {
			Collection<IssueLinkQueryElement> queryElements = new ArrayList<IssueLinkQueryElement>(issueLinkQueryElements.size());
			for (IssueLinkQueryElement issueLinkQueryElement : issueLinkQueryElements) {
				if (issueLinkQueryElement.getLinkedObjectID() == null && issueLinkQueryElement.getIssueLinkTypeID() == null)
					continue; // no criteria => silently ignore

				queryElements.add(issueLinkQueryElement);
			}

			if (!queryElements.isEmpty()) {
				int i = 0;
				filter.append("\n && (");
				for (Iterator<IssueLinkQueryElement> it = queryElements.iterator(); it.hasNext(); ++i) {
					IssueLinkQueryElement issueLinkQueryElement = it.next();

					if (issueLinkQueryElement.getLinkedObjectID() == null && issueLinkQueryElement.getIssueLinkTypeID() == null)
						continue; // no criteria => silently ignore

					String issueLinkVarName = "varIssueLink" + i;
					addVariable(String.class, issueLinkVarName);
					filter.append("\n (this.issueLinks.contains(").append(issueLinkVarName).append(")");

					if (issueLinkQueryElement.getIssueLinkTypeID() != null) {
						String issueLinkTypeIDParamName = "issueLinkTypeID" + i;
						filter.append(" && JDOHelper.getObjectId(").append(issueLinkVarName).append(".issueLinkType) == :").append(issueLinkTypeIDParamName);
						addParam(issueLinkTypeIDParamName, issueLinkQueryElement.getIssueLinkTypeID());
					}

					if (issueLinkQueryElement.getLinkedObjectID() != null) {
						String linkedObjectIDStringParamName = "linkedObjectIDString" + i;
						filter.append(" && ").append(issueLinkVarName).append(".linkedObjectID == :").append(linkedObjectIDStringParamName);
						addParam(linkedObjectIDStringParamName, issueLinkQueryElement.getLinkedObjectID().toString());
					}

					filter.append(")");
					if (it.hasNext()) {
						filter.append("\n || ");
					}
				}
				filter.append("\n)");
			}
		}
		if (logger.isInfoEnabled())
			logger.info(filter.toString());
		q.setFilter(filter.toString());
	}
	
	/**
	 * Returns the string of the {@link IssueSubject}.
	 * @return a string of the {@link IssueSubject}
	 */
	public String getIssueSubject()
	{
		return issueSubject;
	}

	/**
	 * Helper that removes the '.*' from the beginning and end of the given string.
	 * @param pattern the regexp pattern that should be cleansed of the '.*'
	 * @return the pattern without '.*'
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

	/**
	 * Sets the {@link String} of {@link IssueSubject}
	 * @param issueSubject
	 */
	public void setIssueSubject(String issueSubject)
	{
		final String oldIssueSubject = removeRegexpSearch(this.issueSubject);
		this.issueSubject = issueSubject;
		notifyListeners(FieldName.issueSubject, oldIssueSubject, issueSubject);
	}

	/**
	 * Returns true if the string of {@link IssueSubject} represents a regular expression.
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
	 * @param issueSubjectNComment The issueComment to set
	 */
	public void setIssueSubjectNComment(String issueSubjectNComment)
	{
		final String oldIssueSubjectNComment = removeRegexpSearch(this.issueSubjectNComment);
		this.issueSubjectNComment = issueSubjectNComment.toLowerCase();
		notifyListeners(FieldName.issueSubjectNComment, oldIssueSubjectNComment, issueSubjectNComment);
	}

	/**
	 * Returns the combined string of {@link IssueSubject} and {@link IssueComment}.
	 * @return a string of the {@link IssueSubject} combined with string of the {@link IssueComment}
	 */
	public String getIssueSubjectNComment() {
		return issueSubjectNComment;
	}

	/**
	 * Returns true if the combined string of {@link IssueSubject} and {@link IssueComment} represents a regular expression.
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
	 * Returns the string of {@link IssueComment}.
	 * @return a string of the {@link IssueComment}
	 */
	public String getIssueComment() {
		return issueComment;
	}

	/**
	 * Set the string to search in the issue comments for. This can either be a regular expression
	 * (set {@link #setIssueCommentRegex(boolean)} to <code>true</code> then) or a string
	 * that should be contained in one of the comments of the Issues to find.
	 *
	 * @param issueComment The issueComment to set
	 */
	public void setIssueComment(String issueComment)
	{
		final String oldIssueComment = removeRegexpSearch(this.issueComment);
		this.issueComment = issueComment;
		notifyListeners(FieldName.issueComment, oldIssueComment, issueComment);
	}

	/**
	 * Returns true if the string of {@link IssueComment} represents a regular expression.
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
	 * @param issueCommentRegex The issueCommentRegex to search
	 */
	public void setIssueCommentRegex(boolean issueCommentRegex) {
		final boolean oldIssueCommentRegex = this.issueCommentRegex;
		this.issueSubjectRegex = issueCommentRegex;
		notifyListeners(FieldName.issueSubjectNCommentRegex, oldIssueCommentRegex, issueCommentRegex);
	}

	/**
	 * Returns the {@link IssueTypeID}.
	 * @return an {@link IssueTypeID}
	 */
	public IssueTypeID getIssueTypeID() {
		return issueTypeID;
	}

	/**
	 * Sets the {@link IssueTypeID}.
	 * @param issueSubject
	 */
	public void setIssueTypeID(IssueTypeID issueTypeID)
	{
		final IssueTypeID oldIssueTypeID = this.issueTypeID;
		this.issueTypeID = issueTypeID;
		notifyListeners(FieldName.issueTypeID, oldIssueTypeID, issueTypeID);
	}

	/**
	 * Returns the {@link IssueSeverityTypeID}.
	 * @return an {@link IssueSeverityTypeID}
	 */
	public IssueSeverityTypeID getIssueSeverityTypeID() {
		return issueSeverityTypeID;
	}

	/**
	 * Sets the {@link IssueSeverityTypeID}.
	 * @param issueSubject
	 */
	public void setIssueSeverityTypeID(IssueSeverityTypeID issueSeverityTypeID)
	{
		final IssueSeverityTypeID oldIssueSeverityTypeID = this.issueSeverityTypeID;
		this.issueSeverityTypeID = issueSeverityTypeID;
		notifyListeners(FieldName.issueSeverityTypeID, oldIssueSeverityTypeID, issueSeverityTypeID);
	}

	/**
	 * Returns the {@link IssuePriorityID}.
	 * @return an {@link IssuePriorityID}
	 */
	public IssuePriorityID getIssuePriorityID() {
		return issuePriorityID;
	}

	/**
	 * Sets the {@link IssuePriorityID}.
	 * @param issueSubject
	 */
	public void setIssuePriorityID(IssuePriorityID issuePriorityID)
	{
		final IssuePriorityID oldIssuePriorityID = this.issuePriorityID;
		this.issuePriorityID = issuePriorityID;
		notifyListeners(FieldName.issuePriorityID, oldIssuePriorityID, issuePriorityID);
	}

	/**
	 * Returns the {@link IssueResolutionID}.
	 * @return an {@link IssueResolutionID}
	 */
	public IssueResolutionID getIssueResolutionID() {
		return issueResolutionID;
	}

	/**
	 * Sets the {@link IssueResolutionID}.
	 * @param issueSubject
	 */
	public void setIssueResolutionID(IssueResolutionID issueResolutionID)
	{
		final IssueResolutionID oldIssueResolutionID = this.issueResolutionID;
		this.issueResolutionID = issueResolutionID;
		notifyListeners(FieldName.issueResolutionID, oldIssueResolutionID, issueResolutionID);
	}

	/**
	 * Returns the {@link UserID} of the {@link Issue}'s reporter.
	 * @return an {@link UserID} of the {@link Issue}'s reporter
	 */
	public UserID getReporterID() {
		return reporterID;
	}

	/**
	 * Sets the {@link UserID} of the {@link Issue}'s reporter.
	 * @param reporterID The {@link UserID} to set.
	 */
	public void setReporterID(final UserID reporterID)
	{
		final UserID oldReporterID = this.reporterID;
		// Must be resolved during execution, because when initalising as System-User it does not work 
//		if (reporterID != null && reporterID.equals(User.USER_ID_CURRENT_USER))	// User#USER_ID_CURRENT_USER is utilised as "virtual" user that is replaced by the real currently logged-in user when performing certain kinds of stored queries.
//			setCurrentUserAsReporter();
//		else
			this.reporterID = reporterID;
		notifyListeners(FieldName.reporterID, oldReporterID, this.reporterID);
	}

	/**
	 * Returns the {@link UserID} of the {@link Issue}'s assignee.
	 * @return an {@link UserID} of the {@link Issue}'s assignee
	 */
	public UserID getAssigneeID() {
		return assigneeID;
	}

	/**
	 * Sets the {@link UserID} of the {@link Issue}'s assignee.
	 * @param assigneeID The {@link UserID} to set.
	 */
	public void setAssigneeID(final UserID assigneeID)
	{
		final UserID oldAssigneeID = this.assigneeID;
		// Must be resolved during execution, because when initalising as System-User it does not work
//		if (assigneeID != null && assigneeID.equals(User.USER_ID_CURRENT_USER))	// User#USER_ID_CURRENT_USER is utilised as "virtual" user that is replaced by the real currently logged-in user when performing certain kinds of stored queries.
//			setCurrentUserAsAssignee();
//		else
			this.assigneeID = assigneeID;
		notifyListeners(FieldName.assigneeID, oldAssigneeID, this.assigneeID);
	}

	/**
	 * Returns the {@link Date} of the {@link Issue}'s working time range starting time.
	 * @return an {@link Date} of the {@link Issue}'s working time range starting time
	 */
	public Date getIssueWorkTimeRangeFrom() {
		return issueWorkTimeRangeFrom;
	}

	/**
	 * Sets the {@link Date}.
	 * @param issueSubject
	 */
	public void setIssueWorkTimeRangeFrom(Date issueWorkTimeRangeFrom)
	{
		final Date oldIssueWorkTimeRangeFrom = this.issueWorkTimeRangeFrom;
		this.issueWorkTimeRangeFrom = issueWorkTimeRangeFrom;
		notifyListeners(FieldName.issueWorkTimeRangeFrom, oldIssueWorkTimeRangeFrom, issueWorkTimeRangeFrom);
	}

	/**
	 * Returns the {@link Date} of the {@link Issue}'s working time range starting time.
	 * @return an {@link Date} of the {@link Issue}'s working time range ending time
	 */
	public Date getIssueWorkTimeRangeTo() {
		return issueWorkTimeRangeTo;
	}

	/**
	 * Sets the {@link Date}.
	 * @param issueSubject
	 */
	public void setIssueWorkTimeRangeTo(Date issueWorkTimeRangeTo)
	{
		final Date oldIssueWorkTimeRangeTo = this.issueWorkTimeRangeTo;
		this.issueWorkTimeRangeTo = issueWorkTimeRangeTo;
		notifyListeners(FieldName.issueWorkTimeRangeTo, oldIssueWorkTimeRangeTo, issueWorkTimeRangeTo);
	}

	/**
	 * Returns the {@link IssueLinkTypeID}.
	 * @return an {@link IssueLinkTypeID}
	 */
	public IssueLinkTypeID getIssueLinkTypeID() {
		return issueLinkTypeID;
	}

	/**
	 * Sets the {@link IssueLinkTypeID}.
	 * @param issueSubject
	 */
	public void setIssueLinkTypeID(IssueLinkTypeID issueLinkTypeID)
	{
		final IssueLinkTypeID oldIssueLinkTypeID = this.issueLinkTypeID;
		this.issueLinkTypeID = issueLinkTypeID;
		notifyListeners(FieldName.issueLinkTypeID, oldIssueLinkTypeID, issueLinkTypeID);
	}

	/**
	 * Returns the {@link Date} of {@link Issue}'s created time.
	 * @return a {@link Date} of {@link Issue}'s created time
	 */
	public Date getCreateTimestamp() {
		return createTimestamp;
	}

	/**
	 * Sets the {@link Date}.
	 * @param issueSubject
	 */
	public void setCreateTimestamp(Date createTimestamp)
	{
		final Date oldCreateTimestamp = this.createTimestamp;
		this.createTimestamp = createTimestamp;
		notifyListeners(FieldName.createTimestamp, oldCreateTimestamp, createTimestamp);
	}

	/**
	 * Returns the {@link Date} of {@link Issue}'s updated time
	 * @return a {@link Date} of {@link Issue}'s updated time
	 */
	public Date getUpdateTimestamp() {
		return updateTimestamp;
	}

	/**
	 * Sets the {@link Date}.
	 * @param updateTimestamp
	 */
	public void setUpdateTimestamp(Date updateTimestamp)
	{
		final Date oldUpdateTimestamp = this.updateTimestamp;
		this.updateTimestamp = updateTimestamp;
		notifyListeners(FieldName.updateTimestamp, oldUpdateTimestamp, updateTimestamp);
	}

	/**
	 * Returns the {@link Long} of {@link Issue}'s deadline time period
	 * @return a {@link Long} of {@link Issue}'s deadline time period
	 */
	public Long getDeadlineTimePeriod() {
		return deadlineTimePeriod;
	}

	/**
	 * Sets the {@link Long}.
	 * @param deadlineTimePeriod
	 */
	public void setDeadlineTimePeriod(Long deadlineTimePeriod)
	{
		final Long oldDeadlineTimePeriod = this.deadlineTimePeriod;
		this.deadlineTimePeriod = deadlineTimePeriod;
		notifyListeners(FieldName.deadlineTimePeriod, oldDeadlineTimePeriod, deadlineTimePeriod);
	}
//	/**
//	 * Returns the {@link Date} of {@link Issue}'s deadline time
//	 * @return a {@link Date} of {@link Issue}'s deadline time
//	 */
//	public Date getDeadlineAfterTimestamp() {
//		return deadlineAfterTimestamp;
//	}
//
//	/**
//	 * Sets the {@link Date}.
//	 * @param deadlineTimestamp
//	 */
//	public void setDeadlineAfterTimestamp(Date deadlineAfterTimestamp)
//	{
//		final Date oldDeadlineAfterTimestamp = this.deadlineAfterTimestamp;
//		this.deadlineAfterTimestamp = deadlineAfterTimestamp;
//		notifyListeners(FieldName.deadlineAfterTimestamp, oldDeadlineAfterTimestamp, deadlineAfterTimestamp);
//	}
//
//	/**
//	 * Returns the {@link Date} of {@link Issue}'s deadline time
//	 * @return a {@link Date} of {@link Issue}'s deadline time
//	 */
//	public Date getDeadlineBeforeTimestamp() {
//		return deadlineBeforeTimestamp;
//	}
//
//	/**
//	 * Sets the {@link Date}.
//	 * @param deadlineTimestamp
//	 */
//	public void setDeadlineBeforeTimestamp(Date deadlineBeforeTimestamp)
//	{
//		final Date oldDeadlineBeforeTimestamp = this.deadlineBeforeTimestamp;
//		this.deadlineBeforeTimestamp = deadlineBeforeTimestamp;
//		notifyListeners(FieldName.deadlineBeforeTimestamp, oldDeadlineBeforeTimestamp, deadlineBeforeTimestamp);
//	}

	/**
	 * Returns the {@link Set} of {@link ProjectID}.
	 * @return a {@link Set} of {@link ProjectID}
	 */
	public Set<ProjectID> getProjectIDs() {
		if (projectIDs == null) {
			projectIDs = new HashSet<ProjectID>();
		}
		return projectIDs;
	}

	/**
	 * Sets the {@link Set} of {@link ProjectID}s.
	 * @param issueSubject
	 */
	public void setProjectIDs(Set<ProjectID> projectIDs)
	{
		final Set<ProjectID> oldProjectIDs = this.projectIDs;
		this.projectIDs = projectIDs;
		notifyListeners(FieldName.projectIDs, oldProjectIDs, projectIDs);
	}

	/**
	 * Returns the String of the issue state
	 * @return
	 */
	public String getJbpmNodeName() {
		return jbpmNodeName;
	}

	/**
	 * Sets the String of the issue state.
	 * @param jbpmNodeName
	 */
	public void setJbpmNodeName(String jbpmNodeName) {
		final String oldJbpmNodeName = this.jbpmNodeName;
		this.jbpmNodeName = jbpmNodeName;
		notifyListeners(FieldName.jbpmNodeName, oldJbpmNodeName, jbpmNodeName);
	}

	public ProcessDefinitionID getProcessDefinitionID() {
		return processDefinitionID;
	}

	public void setProcessDefinitionID(ProcessDefinitionID processDefinitionID) {
		final ProcessDefinitionID oldProcessDefinitionID = this.processDefinitionID;
		this.processDefinitionID = processDefinitionID;
		notifyListeners(FieldName.processDefinitionID, oldProcessDefinitionID, processDefinitionID);
	}

	public Set<IssueLinkQueryElement> getIssueLinkQueryElements() {
		if (issueLinkQueryElements == null)
			issueLinkQueryElements = new HashSet<IssueLinkQueryElement>();

		//  should we copy this? Or maybe make it read-only? IMHO it should not be modified, hence I'll make the result read-only.
		// You should think about getProjectIDs and setProjectIDs - are they behaving correctly and logically?
		// Who changes this instance when? If the result of this method is supposed to be modified outside, why is there a setter method?
		// From Yo, yes you are absolutely correct! ;-) I didn't think about this. So I change this to non-modifiable object.
		return Collections.unmodifiableSet(issueLinkQueryElements);
	}

	public void setIssueLinkQueryElements(Set<IssueLinkQueryElement> issueLinkQueryElements) {
		// TODO @Chairat: I took the same strategy as in setProjectIDs, but is the linkedObjectIDs- (as well as the projectIDs-) Set
		// really copied? If not and you simply set the same object that you obtained in getLinkedObjectIDs() again (after modifying it),
		// IMHO this contract with getters and setters makes no sense. So what's the strategy? Is the object really replaced (who copies it?)
		// or do you set a reference to the same object?
		// Marco.
		final Set<IssueLinkQueryElement> oldIssueLinkQueryElements = this.issueLinkQueryElements;
		this.issueLinkQueryElements = issueLinkQueryElements;
		// WARNING: If oldLinkedObjectIDs references the same Set as the one that is passed as linkedObjects, these listeners
		// might never be triggered (they check, if old and new are equal). See the TODOs above - either it is a contract that getters and setters
		// are guarantied to be called with different instances of the Set or you must pass oldLinkedObjectIDs == null here.
		// At the moment, this looks like a contradiction.
		// Please document the behaviour of the getter+setter and the way a UI is supposed to work with it (imagine
		// someone else writes UI for this class). Alternatively, you could always copy the set here in the setter, too.
		notifyListeners(FieldName.issueLinkQueryElements, oldIssueLinkQueryElements, issueLinkQueryElements);
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	protected Class<Issue> initCandidateClass()
	{
		return Issue.class;
	}

	/**
	 * Sets the current user as the {@link Issue}'s assignee.
	 */
	public void setCurrentUserAsAssignee() {
		this.assigneeID = GlobalSecurityReflector.sharedInstance().getUserDescriptor().getUserObjectID();
	}

	/**
	 * Sets the current user as the {@link Issue}'s reporter.
	 */
	public void setCurrentUserAsReporter() {
		this.reporterID = GlobalSecurityReflector.sharedInstance().getUserDescriptor().getUserObjectID();
	}
}