package org.nightlabs.jfire.issue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.issue.history.IssueHistoryItem;
import org.nightlabs.jfire.issue.history.id.IssueHistoryItemID;
import org.nightlabs.jfire.issue.id.IssueCommentID;
import org.nightlabs.jfire.issue.id.IssueFileAttachmentID;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.id.IssueLinkID;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.issue.id.IssueMarkerID;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueResolutionID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.issue.issuemarker.IssueMarker;
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.issue.project.ProjectType;
import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.issue.project.id.ProjectTypeID;
import org.nightlabs.jfire.security.id.UserID;

@Remote
public interface IssueManagerRemote {

	Set<IssueFileAttachmentID> getIssueFileAttachmentIDs();

	List<IssueFileAttachment> getIssueFileAttachments(Collection<IssueFileAttachmentID> issueFileAttachmentIDs, String[] fetchGroups, int maxFetchDepth);

	List<IssueWorkTimeRange> getIssueWorkTimeRanges(Collection<IssueWorkTimeRange> issueWorkTimeRangeIDs, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Stores a project type to the datastore.
	 * @param projectType the project type to store
	 * @param get true if you want to get the stored project type
	 * @param fetchGroups the fetchGroups that used for specify fields to be detached from the datastore
	 * @param maxFetchDepth specifies the number of level of the object to be fetched
	 */
	ProjectType storeProjectType(ProjectType projectType, boolean get, String[] fetchGroups, int maxFetchDepth);

	void deleteProjectType(ProjectTypeID projectTypeID);

	List<ProjectType> getProjectTypes(Collection<ProjectTypeID> projectTypeIDs, String[] fetchGroups, int maxFetchDepth);

	Set<ProjectTypeID> getProjectTypeIDs();

	Project storeProject(Project project, boolean get, String[] fetchGroups, int maxFetchDepth);

	void deleteProject(ProjectID projectID);

	List<Project> getProjects(Collection<ProjectID> projectIDs, String[] fetchGroups, int maxFetchDepth);

	Set<ProjectID> getProjectIDs();

	Collection<ProjectID> getRootProjectIDs(String organisationID);

	Collection<ProjectID> getProjectIDsByParentProjectID(ProjectID projectID);

	Collection<ProjectID> getProjectIDsByProjectTypeID(ProjectTypeID projectTypeID);

	List<IssueComment> getIssueComments(Collection<IssueCommentID> issueCommentIDs, String[] fetchGroups, int maxFetchDepth);

	IssueComment storeIssueComment(IssueComment issueComment, boolean get, String[] fetchGroups, int maxFetchDepth);


	List<IssueMarker> getIssueMarkers(Collection<IssueMarkerID> issueMarkerIDs, String[] fetchGroups, int maxFetchDepth);

	Set<IssueMarkerID> getIssueMarkerIDs();

	Set<IssueMarkerID> getIssueMarkerIDs(IssueID issueID);

	IssueMarker storeIssueMarker(IssueMarker issueMarker, boolean get, String[] fetchGroups, int maxFetchDepth);


	List<IssueLinkType> getIssueLinkTypes(Collection<IssueLinkTypeID> issueLinkTypeIDs, String[] fetchGroups, int maxFetchDepth);

	Set<IssueLinkTypeID> getIssueLinkTypeIDs(Class<? extends Object> linkedObjectClass);

	Set<IssueLinkTypeID> getIssueLinkTypeIDs();

	List<IssueLink> getIssueLinks(Collection<IssueLinkID> issueLinkIDs, String[] fetchGroups, int maxFetchDepth);

	Set<IssueLinkID> getIssueLinkIDs();

	/**
	 * @deprecated Use {@link #getIssueLinkIDs()} instead! This method will soon be removed!
	 */
	@Deprecated
	Collection<IssueLinkID> getIssueLinkIDsByOrganisationIDAndLinkedObjectID(String organisationID, ObjectID linkedObjectID);

	Map<ObjectID, Long> getIssueLinkCounts(Collection<? extends ObjectID> linkedObjectIDs);

	Collection<IssueLinkID> getIssueLinkIDs(ObjectID linkedObjectID);

	/**
	 * Stores the given Issue. If the issue is a new issue, do the initializing process instance.
	 * If not new, do the issue history creation process & check the assignee for doing the
	 * state assignment.
	 *
	 * @param issue The issue to be stored
	 * @param get If true the created I will be returned else null
	 * @param fetchGroups The fetchGroups the returned Issue should be detached with
	 */
	Issue storeIssue(Issue issue, boolean get, String[] fetchGroups, int maxFetchDepth);

	Issue storeIssue(Issue issue, String signalJbpmTransitionName, boolean get, String[] fetchGroups, int maxFetchDepth);

	/**
	 * Delete an <code>Issue</code> by the given <code>IssueID</code>.
	 * And also delete its <code>State</code> and <code>IssueLocal</code>.
	 */
	void deleteIssue(IssueID issueID);

	/**
	 * Signal the issue to change its <code>State</code>.
	 *
	 * @param issueID The issueID to be changed
	 * @param jbpmTransitionName a node name that defined in JbpmConstants
	 * @param get If true the created I will be returned else null
	 * @param fetchGroups The fetchGroups the returned Issue should be detached with
	 */
	Issue signalIssue(IssueID issueID, String jbpmTransitionName, boolean get, String[] fetchGroups, int maxFetchDepth);

	/**
	 * @param queries the QueryCollection containing all queries that shall be chained
	 *		in order to retrieve the result. The result of one query is passed to the
	 *		next one using the {@link AbstractJDOQuery#setCandidates(Collection)}.
	 */
	Set<IssueID> getIssueIDs(QueryCollection<? extends AbstractJDOQuery> queries);

	List<Issue> getIssues(Collection<IssueID> issueIDs, String[] fetchGroups, int maxFetchDepth);

	Set<IssueID> getIssueIDs();

	Set<IssueID> getIssueByProjectID(ProjectID projectID);

	Set<IssueID> getIssueByProjectTypeID(ProjectTypeID projectTypeID);

	IssueHistoryItem storeIssueHistoryItem(IssueHistoryItem issueHistory, boolean get, String[] fetchGroups, int maxFetchDepth);

	Collection<IssueHistoryItem> storeIssueHistoryItems(Collection<IssueHistoryItem> issueHistoryItems, boolean get, String[] fetchGroups, int maxFetchDepth);

	Collection<IssueHistoryItemID> getIssueHistoryItemIDsByIssueID(IssueID issueID);

	List<IssueHistoryItem> getIssueHistoryItems(Collection<IssueHistoryItemID> issueHistoryIDs, String[] fetchGroups, int maxFetchDepth);

	IssueType storeIssueType(IssueType issueType, boolean get, String[] fetchGroups, int maxFetchDepth);

	List<IssueType> getIssueTypes(Collection<IssueTypeID> issueTypeIDs, String[] fetchGroups, int maxFetchDepth);

	Set<IssueTypeID> getIssueTypeIDs();

	IssuePriority storeIssuePriority(IssuePriority issuePriority, boolean get, String[] fetchGroups, int maxFetchDepth);

	Set<IssuePriorityID> getIssuePriorityIDs();

	List<IssuePriority> getIssuePriorities(Collection<IssuePriorityID> issuePriorityIDs, String[] fetchGroups, int maxFetchDepth);

	IssueSeverityType storeIssueSeverityType(IssueSeverityType issueSeverityType, boolean get, String[] fetchGroups, int maxFetchDepth);

	Set<IssueSeverityTypeID> getIssueSeverityTypeIDs();

	List<IssueSeverityType> getIssueSeverityTypes(Collection<IssueSeverityTypeID> issueSeverityTypeIDs, String[] fetchGroups, int maxFetchDepth);

	IssueResolution storeIssueResolution(IssueResolution issueResolution, boolean get, String[] fetchGroups, int maxFetchDepth);

	Collection<IssueResolution> getIssueResolutions(String[] fetchGroups, int maxFetchDepth);

	List<IssueResolution> getIssueResolutions(Collection<IssueResolutionID> issueResolutionIDs, String[] fetchGroups, int maxFetchDepth);

	void sendRemindEMail(String messageString, String subject, UserID senderID, Set<UserID> recipientIDs);

	void initialise() throws Exception;

	void convertIssueLinkHistoryItemLinkedObjectID();

	List<IssueCommentID> getIssueCommentIDs(IssueID issueID);

	List<IssueCommentID> getIssueCommentIDsOfIssueOfIssueLink(IssueLinkID issueLinkID);

	Map<IssueLinkID, Long> getIssueCommentCountsOfIssueOfIssueLinks(Collection<IssueLinkID> issueLinkIDs);

	Collection<IssueLinkID> getIssueLinkIDsForIssueAndLinkedObjectClasses(IssueID issueID, Set<Class<?>> linkedObjectClasses);

	void deleteIssueComment(IssueCommentID issueCommentID);
}