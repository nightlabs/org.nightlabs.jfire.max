package org.nightlabs.jfire.issue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.issue.history.IssueHistory;
import org.nightlabs.jfire.issue.history.id.IssueHistoryID;
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

	@RolesAllowed("_Guest_")
	Set<IssueFileAttachmentID> getIssueFileAttachmentIDs();

	@RolesAllowed("_Guest_")
	List<IssueFileAttachment> getIssueFileAttachments(
			Collection<IssueFileAttachmentID> issueFileAttachmentIDs,
			String[] fetchGroups, int maxFetchDepth);

	//IssueWorkTimeRange//
	@RolesAllowed("_Guest_")
	List<IssueWorkTimeRange> getIssueWorkTimeRanges(
			Collection<IssueWorkTimeRange> issueWorkTimeRangeIDs,
			String[] fetchGroups, int maxFetchDepth);

	//ProjectType//
	/**
	 * Stores a project type to the datastore.
	 * @param projectType the project type to store
	 * @param get true if you want to get the stored project type
	 * @param fetchGroups the fetchGroups that used for specify fields to be detached from the datastore
	 * @param maxFetchDepth specifies the number of level of the object to be fetched
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	ProjectType storeProjectType(ProjectType projectType, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void deleteProjectType(ProjectTypeID projectTypeID);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<ProjectType> getProjectTypes(Collection<ProjectTypeID> projectTypeIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<ProjectTypeID> getProjectTypeIDs();

	//Project//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	Project storeProject(Project project, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void deleteProject(ProjectID projectID);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<Project> getProjects(Collection<ProjectID> projectIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<ProjectID> getProjectIDs();

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Collection<ProjectID> getRootProjectIDs(String organisationID);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Collection<ProjectID> getProjectIDsByParentProjectID(ProjectID projectID);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Collection<ProjectID> getProjectIDsByProjectTypeID(
			ProjectTypeID projectTypeID);

	//IssueComment//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<IssueComment> getIssueComments(
			Collection<IssueCommentID> issueCommentIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	IssueComment storeIssueComment(IssueComment issueComment, boolean get,
			String[] fetchGroups, int maxFetchDepth);


	//IssueMarker//
	List<IssueMarker> getIssueMarkers(Collection<IssueMarkerID> issueMarkerIDs, String[] fetchGroups, int maxFetchDepth);

	Set<IssueMarkerID> getIssueMarkerIDs();

	Set<IssueMarkerID> getIssueMarkerIDs(IssueID issueID);

	IssueMarker storeIssueMarker(IssueMarker issueMarker, boolean get, String[] fetchGroups, int maxFetchDepth);



	//IssueLinkType//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<IssueLinkType> getIssueLinkTypes(
			Collection<IssueLinkTypeID> issueLinkTypeIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<IssueLinkTypeID> getIssueLinkTypeIDs(
			Class<? extends Object> linkedObjectClass);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<IssueLinkTypeID> getIssueLinkTypeIDs();

	//IssueLink//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<IssueLink> getIssueLinks(Collection<IssueLinkID> issueLinkIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<IssueLinkID> getIssueLinkIDs();

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Collection<IssueLinkID> getIssueLinkIDsByOrganisationIDAndLinkedObjectID(
			String organisationID, String linkedObjectID);

	//Issue//
	/**
	 * Stores the given Issue. If the issue is a new issue, do the initializing process instance.
	 * If not new, do the issue history creation process & check the assignee for doing the
	 * state assignment.
	 *
	 * @param issue The issue to be stored
	 * @param get If true the created I will be returned else null
	 * @param fetchGroups The fetchGroups the returned Issue should be detached with
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	Issue storeIssue(Issue issue, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * Delete an <code>Issue</code> by the given <code>IssueID</code>.
	 * And also delete its <code>State</code> and <code>IssueLocal</code>.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void deleteIssue(IssueID issueID);

	/**
	 * Signal the issue to change its <code>State</code>.
	 *
	 * @param issueID The issueID to be changed
	 * @param jbpmTransitionName a node name that defined in JbpmConstants
	 * @param get If true the created I will be returned else null
	 * @param fetchGroups The fetchGroups the returned Issue should be detached with
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	Issue signalIssue(IssueID issueID, String jbpmTransitionName, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @param queries the QueryCollection containing all queries that shall be chained
	 *		in order to retrieve the result. The result of one query is passed to the
	 *		next one using the {@link AbstractJDOQuery#setCandidates(Collection)}.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<IssueID> getIssueIDs(QueryCollection<? extends AbstractJDOQuery> queries);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<Issue> getIssues(Collection<IssueID> issueIDs, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<IssueID> getIssueIDs();

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<Issue> getIssueByProjectID(ProjectID projectID);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<Issue> getIssueByProjectTypeID(ProjectTypeID projectTypeID);

	//IssueHistory//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	IssueHistory storeIssueHistory(IssueHistory issueHistory, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Collection<IssueHistoryID> getIssueHistoryIDsByIssueID(IssueID issueID);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<IssueHistory> getIssueHistories(
			Collection<IssueHistoryID> issueHistoryIDs, String[] fetchGroups,
			int maxFetchDepth);

	//IssueType//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	IssueType storeIssueType(IssueType issueType, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<IssueType> getIssueTypes(Collection<IssueTypeID> issueTypeIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<IssueTypeID> getIssueTypeIDs();

	//IssuePriority//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	IssuePriority storeIssuePriority(IssuePriority issuePriority, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<IssuePriorityID> getIssuePriorityIDs();

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<IssuePriority> getIssuePriorities(
			Collection<IssuePriorityID> issuePriorityIDs, String[] fetchGroups,
			int maxFetchDepth);

	//IssueSeverityType//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	IssueSeverityType storeIssueSeverityType(
			IssueSeverityType issueSeverityType, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<IssueSeverityTypeID> getIssueSeverityTypeIDs();

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<IssueSeverityType> getIssueSeverityTypes(
			Collection<IssueSeverityTypeID> issueSeverityTypeIDs,
			String[] fetchGroups, int maxFetchDepth);

	//IssueResolution//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	IssueResolution storeIssueResolution(IssueResolution issueResolution,
			boolean get, String[] fetchGroups, int maxFetchDepth);

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	Collection getIssueResolutions(String[] fetchGroups, int maxFetchDepth)
			throws ModuleException;

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	List<IssueResolution> getIssueResolutions(
			Collection<IssueResolutionID> issueResolutionIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void sendRemindEMail(String messageString, String subject, UserID senderID,
			Set<UserID> recipientIDs);

	//Bean//
	/**
	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialise method.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initialise() throws Exception;

}