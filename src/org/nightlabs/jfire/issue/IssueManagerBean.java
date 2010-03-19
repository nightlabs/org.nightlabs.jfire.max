package org.nightlabs.jfire.issue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.JDOQueryCollectionDecorator;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.editlock.EditLockType;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.history.IssueCommentHistoryItem;
import org.nightlabs.jfire.issue.history.IssueHistoryItem;
import org.nightlabs.jfire.issue.history.IssueHistoryItemFactory;
import org.nightlabs.jfire.issue.history.IssueLinkHistoryItem;
import org.nightlabs.jfire.issue.history.id.IssueHistoryItemID;
import org.nightlabs.jfire.issue.id.IssueCommentID;
import org.nightlabs.jfire.issue.id.IssueFileAttachmentID;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.id.IssueLinkID;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.issue.id.IssueLocalID;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueResolutionID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.issue.issuemarker.IssueMarker;
import org.nightlabs.jfire.issue.issuemarker.id.IssueMarkerID;
import org.nightlabs.jfire.issue.jbpm.JbpmConstantsIssue;
import org.nightlabs.jfire.issue.project.Project;
import org.nightlabs.jfire.issue.project.ProjectType;
import org.nightlabs.jfire.issue.project.id.ProjectID;
import org.nightlabs.jfire.issue.project.id.ProjectTypeID;
import org.nightlabs.jfire.issue.prop.IssueStruct;
import org.nightlabs.jfire.issue.query.IssueQuery;
import org.nightlabs.jfire.issue.resource.Messages;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.person.PersonSearchFilter;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.datafield.TextDataField;
import org.nightlabs.jfire.prop.search.PropSearchFilter;
import org.nightlabs.jfire.query.store.BaseQueryStore;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.dao.UserDAO;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.progress.NullProgressMonitor;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.Util;

/**
 * An EJB session bean provides methods for managing every objects used in the issue tracking.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 * @author marco schulze - marco at nightlabs dot de
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class IssueManagerBean extends BaseSessionBeanImpl
implements IssueManagerRemote
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(IssueManagerBean.class);


	//IssueFileAttachment//
	@RolesAllowed("_Guest_")
	@Override
	public Set<IssueFileAttachmentID> getIssueFileAttachmentIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(IssueFileAttachment.class);
			q.setResult("JDOHelper.getObjectId(this)");

			return CollectionUtil.createHashSetFromCollection( q.execute() );
//			return new HashSet<IssueFileAttachmentID>((Collection<? extends IssueFileAttachmentID>) q.execute());
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public List<IssueFileAttachment> getIssueFileAttachments(Collection<IssueFileAttachmentID> issueFileAttachmentIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueFileAttachmentIDs, IssueFileAttachment.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	//IssueWorkTimeRange//
	@RolesAllowed("_Guest_")
	@Override
	public List<IssueWorkTimeRange> getIssueWorkTimeRanges(Collection<IssueWorkTimeRange> issueWorkTimeRangeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueWorkTimeRangeIDs, IssueWorkTimeRange.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	//ProjectType//
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ProjectType storeProjectType(ProjectType projectType, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, projectType, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public void deleteProjectType(ProjectTypeID projectTypeID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getExtent(ProjectType.class, true);
			ProjectType projectType = (ProjectType) pm.getObjectById(projectTypeID);
			pm.deletePersistent(projectType);
			pm.flush();
		}//try
		finally {
			pm.close();
		}//finally
	}

	@RolesAllowed("_Guest_")
	@Override
	public List<ProjectType> getProjectTypes(Collection<ProjectTypeID> projectTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, projectTypeIDs, ProjectType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<ProjectTypeID> getProjectTypeIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(ProjectType.class);
			q.setResult("JDOHelper.getObjectId(this)");

			return CollectionUtil.createHashSetFromCollection( q.execute() );
			//return new HashSet<ProjectTypeID>((Collection<? extends ProjectTypeID>) q.execute());
		} finally {
			pm.close();
		}
	}

	//Project//
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Project storeProject(Project project, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			boolean isNew = !JDOHelper.isDetached(project);
			if (!isNew) {
				project.setUpdateTimestamp(new Date());
			}
			return NLJDOHelper.storeJDO(pm, project, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public void deleteProject(ProjectID projectID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getExtent(Project.class, true);
			Project project = (Project) pm.getObjectById(projectID);
			pm.deletePersistent(project);
			pm.flush();
		}//try
		finally {
			pm.close();
		}//finally
	}

	@RolesAllowed("_Guest_")
	@Override
	public List<Project> getProjects(Collection<ProjectID> projectIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, projectIDs, Project.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<ProjectID> getProjectIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(Project.class);
			q.setResult("JDOHelper.getObjectId(this)");

			return CollectionUtil.createHashSetFromCollection( q.execute() );
//			return new HashSet<ProjectID>((Collection<? extends ProjectID>) q.execute());
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Collection<ProjectID> getRootProjectIDs(String organisationID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newNamedQuery(Project.class, "getRootProjects");
			Map<String, Object> params = new HashMap<String, Object>(1);
			params.put("organisationID", organisationID);

			return NLJDOHelper.getObjectIDSet(CollectionUtil.castCollection( q.executeWithMap(params) ));
//			return NLJDOHelper.getObjectIDSet((Collection<Project>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Collection<ProjectID> getProjectIDsByParentProjectID(ProjectID projectID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newNamedQuery(Project.class, "getProjectsByParentProjectID");
			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("organisationID", projectID.organisationID);
			params.put("parentProjectID", projectID.projectID);

			return NLJDOHelper.getObjectIDSet(CollectionUtil.castCollection( q.executeWithMap(params) ));
//			return NLJDOHelper.getObjectIDSet((Collection<Project>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Collection<ProjectID> getProjectIDsByProjectTypeID(ProjectTypeID projectTypeID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newNamedQuery(Project.class, "getProjectsByProjectTypeID");
			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("organisationID", projectTypeID.organisationID);
			params.put("projectTypeID", projectTypeID.projectTypeID);

			return NLJDOHelper.getObjectIDSet(CollectionUtil.castCollection( q.executeWithMap(params) ));
//			return NLJDOHelper.getObjectIDSet((Collection<Project>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

//	//ProjectPhase//
//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type="Required"
//	 */
//	public ProjectPhase storeProjectPhase(ProjectPhase projectPhase, boolean get, String[] fetchGroups, int maxFetchDepth)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			//			boolean isNew = !JDOHelper.isDetached(projectPhase);
//			return NLJDOHelper.storeJDO(pm, projectPhase, get, fetchGroups, maxFetchDepth);
//		}//try
//		finally {
//			pm.close();
//		}//finally
//	}
//
//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	@SuppressWarnings("unchecked")
//	public Set<ProjectPhaseID> getProjectPhaseIDs()
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			Query q = pm.newQuery(ProjectPhase.class);
//			q.setResult("JDOHelper.getObjectId(this)");
//			return new HashSet<ProjectPhaseID>((Collection<? extends ProjectPhaseID>) q.execute());
//		} finally {
//			pm.close();
//		}
//	}
//
//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	@SuppressWarnings("unchecked")
//	public List<ProjectPhase> getProjectPhases(Collection<ProjectPhaseID> projectPhaseIDs, String[] fetchGroups, int maxFetchDepth)
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			return NLJDOHelper.getDetachedObjectList(pm, projectPhaseIDs, ProjectPhase.class, fetchGroups, maxFetchDepth);
//		} finally {
//			pm.close();
//		}
//	}



	//IssueComment//
	@RolesAllowed("_Guest_")
	@Override
	public List<IssueComment> getIssueComments(Collection<IssueCommentID> issueCommentIDs, String[] fetchGroups,int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueCommentIDs, IssueComment.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public IssueComment storeIssueComment(IssueComment issueComment, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		
		boolean isNew = !JDOHelper.isDetached(issueComment);
		if (!isNew) {
			issueComment.setUpdateTimestamp(new Date());
			IssueHistoryItem issueHistoryItem = new IssueCommentHistoryItem(issueComment.getUser(), issueComment.getIssue(), issueComment, false);
			storeIssueHistoryItem(issueHistoryItem, false, new String[]{FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		}
		
		issueComment = pm.makePersistent(issueComment);
		if (!get)
			return null;

		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
		if (fetchGroups != null)
			pm.getFetchPlan().setGroups(fetchGroups);
		return NLJDOHelper.storeJDO(pm, issueComment, get, fetchGroups, maxFetchDepth);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public void deleteIssueComment(IssueCommentID issueCommentID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getExtent(IssueComment.class, true);
			IssueComment issueComment = (IssueComment) pm.getObjectById(issueCommentID);
			pm.deletePersistent(issueComment);
			pm.flush();
		}//try
		finally {
			pm.close();
		}//finally
	}
	// --- 8< --- KaiExperiments: since 14.05.2009 ------------------
	// --[IssueMarker]--
	@RolesAllowed("_Guest_")
	@Override
	public List<IssueMarker> getIssueMarkers(Collection<IssueMarkerID> issueMarkerIDs, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
		try     { return NLJDOHelper.getDetachedObjectList(pm, issueMarkerIDs, IssueMarker.class, fetchGroups, maxFetchDepth); }
		finally { pm.close(); }
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<IssueMarkerID> getIssueMarkerIDs() {
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(IssueMarker.class);
			q.setResult("JDOHelper.getObjectId(this)");
			Collection<IssueMarkerID> c = CollectionUtil.castCollection((Collection<?>) q.execute());
			return new HashSet<IssueMarkerID>(c);
		} finally {
			pm.close();
		}
	}

	// TODO This is not necessary. Will remove this later. Kai.
	@RolesAllowed("_Guest_")
	@Override
	public Set<IssueMarkerID> getIssueMarkerIDs(IssueID issueID) {
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(IssueLinkType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			q.setFilter("JDOHelper.getObjectId(this.Issue) == :issueID");
			Collection<IssueMarkerID> c = CollectionUtil.castCollection((Collection<?>) q.execute(issueID));
			return new HashSet<IssueMarkerID>(c);
		} finally {
			pm.close();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public IssueMarker storeIssueMarker(IssueMarker issueMarker, boolean get, String[] fetchGroups, int maxFetchDepth) {
		assert issueMarker != null;
		PersistenceManager pm = createPersistenceManager();
		try     { return NLJDOHelper.storeJDO(pm, issueMarker, get, fetchGroups, maxFetchDepth); }
		finally { pm.close(); }
	}
	// ------ KaiExperiments ----- >8 -------------------------------



	//IssueLinkType//
	@RolesAllowed("_Guest_")
	@Override
	public List<IssueLinkType> getIssueLinkTypes(Collection<IssueLinkTypeID> issueLinkTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueLinkTypeIDs, IssueLinkType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<IssueLinkTypeID> getIssueLinkTypeIDs(Class<? extends Object> linkedObjectClass)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			if (linkedObjectClass == null) {
				linkedObjectClass = Object.class;
			}
			Set<IssueLinkType> issueLinkTypes = IssueLinkType.getIssueLinkTypes(pm, linkedObjectClass);
			return NLJDOHelper.getObjectIDSet(issueLinkTypes);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<IssueLinkTypeID> getIssueLinkTypeIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(IssueLinkType.class);
			q.setResult("JDOHelper.getObjectId(this)");

			return CollectionUtil.createHashSetFromCollection( q.execute() );
//			return new HashSet<IssueLinkTypeID>((Collection)q.execute());
		} finally {
			pm.close();
		}
	}

	//IssueLink//
	@RolesAllowed("_Guest_")
	@Override
	public List<IssueLink> getIssueLinks(Collection<IssueLinkID> issueLinkIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueLinkIDs, IssueLink.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<IssueLinkID> getIssueLinkIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(IssueLink.class);
			q.setResult("JDOHelper.getObjectId(this)");

			return CollectionUtil.createHashSetFromCollection( q.execute() );
//			return new HashSet<IssueLinkID>((Collection)q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @deprecated Use {@link #getIssueLinkIDs()} instead! This method will soon be removed!
	 */
	@Deprecated
	@RolesAllowed("_Guest_")
	@Override
	public Collection<IssueLinkID> getIssueLinkIDsByOrganisationIDAndLinkedObjectID(String organisationID, ObjectID linkedObjectID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			// NEVER DO THIS!!! Never reference a query outside the class which declared it! Use a public static method in that class instead!
			// I refactored the code below and commented the old stuff out. Marco.
//			Query q = pm.newNamedQuery(IssueLink.class, "getIssueLinksByOrganisationIDAndLinkedObjectID");
//			Map<String, Object> params = new HashMap<String, Object>(2);
//			params.put("organisationID", organisationID);
//			params.put("linkedObjectID", linkedObjectID);
//
//			return NLJDOHelper.getObjectIDSet((Collection<IssueLink>) q.executeWithMap(params));
			if (organisationID == null)
				organisationID = getOrganisationID();

			return NLJDOHelper.getObjectIDSet(IssueLink.getIssueLinksByOrganisationIDAndLinkedObjectID(pm, organisationID, linkedObjectID));
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Map<ObjectID, Long> getIssueLinkCounts(Collection<? extends ObjectID> linkedObjectIDs)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Map<ObjectID, Long> result = new HashMap<ObjectID, Long>(linkedObjectIDs.size());
			for (ObjectID linkedObjectID : linkedObjectIDs) {
				long count = IssueLink.getIssueLinkCount(pm, linkedObjectID);
				result.put(linkedObjectID, count);
			}
			return result;
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Collection<IssueLinkID> getIssueLinkIDs(ObjectID linkedObjectID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return new HashSet<IssueLinkID>(IssueLink.getIssueLinkIDs(pm, linkedObjectID));
		} finally {
			pm.close();
		}
	}

	//Issue//
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Issue storeIssue(Issue issue, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		return storeIssue(issue, null, get, fetchGroups, maxFetchDepth);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Issue storeIssue(Issue issue, String signalJbpmTransitionName, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		Issue pIssue = null;
		try {
			//check if the issue is new.
			boolean isNewIssue = !JDOHelper.isDetached(issue);

			if (isNewIssue) {
				if (issue.getProject() == null) {
					issue.setProject((Project)pm.getObjectById(Project.PROJECT_ID_DEFAULT));
				}
				pIssue = pm.makePersistent(issue);
				// TODO BEGIN WORKAROUND - see Issue.jdoPreStore()
				issue.ensureIntegrity();
				// END WORKAROUND

//				IssueType type;
//
//				if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED) {
//					pm.flush();
//					// create the ProcessInstance for new Issues
//					// TO DO: WORKAROUND: Calling createProcessInstanceForIssue on pIssue.getIssueType() says that this IssueType is not persistent ?!?
//					type = (IssueType) pm.getObjectById(JDOHelper.getObjectId(pIssue.getIssueType()));
//					if (type == null) {
//						throw new IllegalStateException("Could not create ProcessInstance for new Issue as its type is null");
//					}
//				}
//				else
//					type = pIssue.getIssueType();
//
//				type.createProcessInstanceForIssue(pIssue); // already done in StoreCallback
			}
			else {
				// --[ On Timestamp ]-------------------------------------------------------------------------------------------|
				if (issue.getCreateTimestamp() != null)
					issue.setUpdateTimestamp(new Date());

				// --[ Obtain references: oldIssue, User ]----------------------------------------------------------------------|
				IssueID issueID = (IssueID) JDOHelper.getObjectId(issue);
				Issue oldPersistentIssue = (Issue) pm.getObjectById(issueID);
				User user = SecurityReflector.getUserDescriptor().getUser(pm);

				// Ensure that we never accidentally delete a IssueComment (e.g. by persisting a
				// modified Issue which doesn't have all comments as it was not yet updated on the
				// client side.
				List<IssueComment> issueComments = null;
				try {
					issueComments = issue.getComments();
				} catch (JDODetachedFieldAccessException x) {
					// ignore - was not detached
				}
				if (issueComments != null) {
					for (IssueComment issueComment : oldPersistentIssue.getComments()) {
						if (!issueComments.contains(issueComment))
							issue.addComment(issueComment);
					}
				}

				// --[ On IssueHistoryItems ]-----------------------------------------------------------------------------------|
				Collection<IssueHistoryItem> issueHistoryItems = IssueHistoryItemFactory.createIssueHistoryItems(pm, user, oldPersistentIssue, issue); // <-- Seems OK and holding.
				storeIssueHistoryItems(issueHistoryItems, false, new String[]{FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

				// --[ On Assignees ]-------------------------------------------------------------------------------------------|
				boolean doUnassign = false;
				if (oldPersistentIssue != null) {
					try
					{
						doUnassign = oldPersistentIssue.getAssignee() != null && issue.getAssignee() == null;
					}
					catch (JDODetachedFieldAccessException x) { doUnassign = false; }
				}

				boolean doAssign = false;
				if (oldPersistentIssue != null)
				{
					try
					{
						if (issue.getAssignee() != null)
							doAssign = !Util.equals(issue.getAssignee(), oldPersistentIssue.getAssignee());
					}
					catch (JDODetachedFieldAccessException x) { doUnassign = false; }
				}

				String jbpmTransitionName = signalJbpmTransitionName;
				if (jbpmTransitionName == null) {
					if (doAssign)
						jbpmTransitionName = JbpmConstantsIssue.TRANSITION_NAME_ASSIGN;

					if (doUnassign)
						jbpmTransitionName = JbpmConstantsIssue.TRANSITION_NAME_UNASSIGN;
				}

				pIssue = pm.makePersistent(issue);

				if (jbpmTransitionName != null) {
//					FetchPlanBackup fetchPlanBackup = NLJDOHelper.backupFetchPlan(pm.getFetchPlan());
					JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
					try {
						ProcessInstance processInstance = jbpmContext.getProcessInstanceForUpdate(issue.getIssueLocal().getJbpmProcessInstanceId());
						Token token = processInstance.getRootToken();
						if (token.getNode().hasLeavingTransition(jbpmTransitionName))
							signalIssue(issueID, jbpmTransitionName, get, fetchGroups, maxFetchDepth);
					} finally {
						jbpmContext.close();
					}
//						NLJDOHelper.restoreFetchPlan(pm.getFetchPlan(), fetchPlanBackup);

//					// performing a transition might cause the fetch-plan to be modified => backup + restore
//					FetchPlanBackup fetchPlanBackup = NLJDOHelper.backupFetchPlan(pm.getFetchPlan());
//					JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
//					try {
//						ProcessInstance processInstance = jbpmContext.getProcessInstanceForUpdate(issue.getIssueLocal().getJbpmProcessInstanceId());
//						Token token = processInstance.getRootToken();
//						if (token.getNode().hasLeavingTransition(jbpmTransitionName))
//							token.signal(jbpmTransitionName);
//
//					} finally {
//						jbpmContext.close();
//					}
//					NLJDOHelper.restoreFetchPlan(pm.getFetchPlan(), fetchPlanBackup);
				}


			}

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(pIssue);
		} finally {
			pm.close();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public void deleteIssue(IssueID issueID) {

		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getExtent(Issue.class, true);
			Issue issue = (Issue) pm.getObjectById(issueID);
			// Note(s):
			//  1. See Issue.jdoPreDelete() -- routines there include the deletion of States, in both this Issue and its related IssueLocal.

			//  2. Handle the IssueHistoryItems attached to this Issue. Kai
			//     IMHO this should happen before the IssueLocal is deleted, because the IssueLocal might be referenced by an IssueHistoryItem (though this is currently never the case).
			//     Please revert this change, if it breaks deletion, because this is not essential - it's just a little bit cleaner, IMHO. Marco.
			pm.getExtent(IssueHistoryItem.class, true);
			Collection<IssueHistoryItem> historyItems = IssueHistoryItem.getIssueHistoryItemsByIssue(pm, issueID);
			pm.deletePersistentAll(historyItems);
			pm.flush();

			//   3. Handle the IssueLocal.
			pm.getExtent(IssueLocal.class, true);
			pm.deletePersistent(issue.getStatableLocal());
			pm.flush();

			// 4. Finally, handle the Issue itself.
			pm.getExtent(Issue.class, true);
			pm.deletePersistent(issue);
			pm.flush();

		} finally {
			pm.close();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Issue signalIssue(IssueID issueID, String jbpmTransitionName, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			IssueLocal issueLocal = (IssueLocal) pm.getObjectById(IssueLocalID.create(issueID));
			JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();

			try {
				ProcessInstance processInstance = jbpmContext.getProcessInstanceForUpdate(issueLocal.getJbpmProcessInstanceId());
				processInstance.signal(jbpmTransitionName);
			} finally {
				jbpmContext.close();
			}

			pm.flush();

			if (get) {
				pm.getExtent(Issue.class);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
				else
					pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS | FetchPlan.DETACH_UNLOAD_FIELDS);
				Issue issue = (Issue) pm.getObjectById(issueID);
				return pm.detachCopy(issue);
			}

		} finally {
			pm.close();
		}

		return null;
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<IssueID> getIssueIDs(QueryCollection<? extends AbstractJDOQuery> queries)
	{
		if (queries == null)
			return null;

		if (! Issue.class.isAssignableFrom(queries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ queries.getResultClassName());
		}

		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			JDOQueryCollectionDecorator<? extends AbstractSearchQuery> decoratedCollection;
			if (queries instanceof JDOQueryCollectionDecorator)
			{
				decoratedCollection = (JDOQueryCollectionDecorator<? extends AbstractSearchQuery>) queries;
			}
			else
			{
				decoratedCollection = new JDOQueryCollectionDecorator<AbstractSearchQuery>(queries);
			}

			decoratedCollection.setPersistenceManager(pm);
			return NLJDOHelper.getObjectIDSet( CollectionUtil.castCollection(decoratedCollection.executeQueries()) );
//			Collection<? extends Issue> issues = (Collection<? extends Issue>) decoratedCollection.executeQueries();
//			return NLJDOHelper.getObjectIDSet(issues);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public List<Issue> getIssues(Collection<IssueID> issueIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueIDs, Issue.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<IssueID> getIssueIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(Issue.class);
			q.setResult("JDOHelper.getObjectId(this)");

			return CollectionUtil.createHashSetFromCollection( q.execute() );
//			return new HashSet<IssueID>((Collection<? extends IssueID>) q.execute());
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<IssueID> getIssueByProjectID(ProjectID projectID) {
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newNamedQuery(Issue.class, "getIssuesByProjectID");
			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("organisationID", projectID.organisationID);
			params.put("projectID", projectID.projectID);

			return NLJDOHelper.getObjectIDSet( CollectionUtil.castCollection(q.executeWithMap(params)) );
//			return NLJDOHelper.getObjectIDSet((Collection<Issue>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<IssueID> getIssueByProjectTypeID(ProjectTypeID projectTypeID) {
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newNamedQuery(Issue.class, "getIssuesByProjectTypeID");
			Map<String, Object> params = new HashMap<String, Object>(2);
			params.put("organisationID", projectTypeID.organisationID);
			params.put("projectTypeID", projectTypeID.projectTypeID);

			return NLJDOHelper.getObjectIDSet( CollectionUtil.castCollection(q.executeWithMap(params)) );
//			return NLJDOHelper.getObjectIDSet((Collection<Issue>) q.executeWithMap(params));
		} finally {
			pm.close();
		}
	}

	//IssueHistoryItem//
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public IssueHistoryItem storeIssueHistoryItem(IssueHistoryItem issueHistoryItem, boolean get, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
		try     { return NLJDOHelper.storeJDO(pm, issueHistoryItem, get, fetchGroups, maxFetchDepth); }
		finally { pm.close(); }
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<IssueHistoryItem> storeIssueHistoryItems(Collection<IssueHistoryItem> issueHistoryItems, boolean get, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
		try     { return NLJDOHelper.storeJDOCollection(pm, issueHistoryItems, get, fetchGroups, maxFetchDepth); }
		finally { pm.close(); }
	}

	@RolesAllowed("_Guest_")
	@Override
	public Collection<IssueHistoryItemID> getIssueHistoryItemIDsByIssueID(IssueID issueID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getObjectIDSet(IssueHistoryItem.getIssueHistoryItemsByIssue(pm, issueID));
		} finally {
			pm.close();
		}
	}

//	@RolesAllowed("_Guest_")
//	@Override
//	public Set<IssueHistoryItemID> getIssueHistoryItemIDs()
//	{
//		PersistenceManager pm = createPersistenceManager();
//		try {
//			Query q = pm.newQuery(IssueHistoryItem.class);
//			q.setResult("JDOHelper.getObjectId(this)");
//
//			return CollectionUtil.createHashSetFromCollection( q.execute() );
//		} finally {
//			pm.close();
//		}
//	}

	@RolesAllowed("_Guest_")
	@Override
	public List<IssueHistoryItem> getIssueHistoryItems(Collection<IssueHistoryItemID> issueHistoryIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueHistoryIDs, IssueHistoryItem.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	//IssueType//
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public IssueType storeIssueType(IssueType issueType, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {			
			return doStoreIssueType(pm, issueType, null, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public IssueType storeIssueType(IssueType issueType, byte[] processDefinitionAsZip, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {			
			return doStoreIssueType(pm, issueType, processDefinitionAsZip, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}
	
	private IssueType doStoreIssueType(PersistenceManager pm, IssueType issueType, byte[] processDefinitionAsZip, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		URL processDefinitionURL = null;
		IssueType result = NLJDOHelper.storeJDO(pm, issueType, get, fetchGroups, maxFetchDepth);
		IssueType attached = (IssueType) pm.getObjectById(JDOHelper.getObjectId(issueType));
		// if a process definition is given unzip it, assign the URL and persist
		if (processDefinitionAsZip != null) {
			File zipFile = null;
			File unzippedFileDir = null;
			try {
				zipFile = File.createTempFile("issuelink-processdefinition-zip", String.valueOf(System.currentTimeMillis()));
				unzippedFileDir = File.createTempFile("issuelink-processdefinition-unzip", String.valueOf(System.currentTimeMillis()));			
				InputStream in = new ByteArrayInputStream(processDefinitionAsZip);
				OutputStream out = new FileOutputStream(zipFile);
				try {
					IOUtil.transferStreamData(in, out);	
				} finally {
					in.close();
					out.close();
				}
				IOUtil.unzipArchive(zipFile, unzippedFileDir);
				processDefinitionURL = unzippedFileDir.toURI().toURL();
				attached.readProcessDefinition(processDefinitionURL);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (zipFile != null)
					zipFile.delete();
				if (unzippedFileDir != null)
					unzippedFileDir.delete();
			}
		}
		else {
			if (attached.getProcessDefinition() == null) {
				try {
					// if no process definition is provided use the default
					processDefinitionURL = IssueType.class.getResource("jbpm/status/");
					attached.readProcessDefinition(processDefinitionURL);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return result;
	}
	
	@RolesAllowed("_Guest_")
	@Override
	public List<IssueType> getIssueTypes(Collection<IssueTypeID> issueTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueTypeIDs, IssueType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<IssueTypeID> getIssueTypeIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return CollectionUtil.createHashSetFromCollection(IssueType.getIssueTypeIDs(pm));
		} finally {
			pm.close();
		}
	}

	//IssuePriority//
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public IssuePriority storeIssuePriority(IssuePriority issuePriority, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, issuePriority, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<IssuePriorityID> getIssuePriorityIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(IssuePriority.class);
			q.setResult("JDOHelper.getObjectId(this)");

			return CollectionUtil.createHashSetFromCollection( q.execute() );
//			return new HashSet<IssuePriorityID>((Collection)q.execute());
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public List<IssuePriority> getIssuePriorities(Collection<IssuePriorityID> issuePriorityIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issuePriorityIDs, IssuePriority.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	//IssueSeverityType//
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public IssueSeverityType storeIssueSeverityType(IssueSeverityType issueSeverityType, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try{
			return NLJDOHelper.storeJDO(pm, issueSeverityType, get, fetchGroups, maxFetchDepth);
		}//try
		finally{
			pm.close();
		}//finally
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<IssueSeverityTypeID> getIssueSeverityTypeIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(IssueSeverityType.class);
			q.setResult("JDOHelper.getObjectId(this)");

			return CollectionUtil.createHashSetFromCollection( q.execute() );
//			return new HashSet<IssueSeverityTypeID>((Collection<? extends IssueSeverityTypeID>) q.execute());
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public List<IssueSeverityType> getIssueSeverityTypes(Collection<IssueSeverityTypeID> issueSeverityTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueSeverityTypeIDs, IssueSeverityType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	//IssueResolution//
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public IssueResolution storeIssueResolution(IssueResolution issueResolution, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, issueResolution, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<IssueResolution> getIssueResolutions(String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(IssueResolution.class);
			@SuppressWarnings("unchecked")
			Collection<IssueResolution> c = pm.detachCopyAll((Collection<IssueResolution>)q.execute());
			return c;
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public List<IssueResolution> getIssueResolutions(Collection<IssueResolutionID> issueResolutionIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueResolutionIDs, IssueResolution.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public void sendRemindEMail(String messageString, String subject, UserID senderID, Set<UserID> recipientIDs)
	{
		User sender = UserDAO.sharedInstance().getUser(senderID,
				new String[]{User.FETCH_GROUP_PERSON, User.FETCH_GROUP_NAME},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());
		String senderEmail = "";
		try {
			senderEmail = ((TextDataField)sender.getPerson().getDataField(PersonStruct.INTERNET_EMAIL)).getText();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		List<User> recipients = UserDAO.sharedInstance().getUsers(recipientIDs,
				new String[]{User.FETCH_GROUP_PERSON, User.FETCH_GROUP_NAME},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, new NullProgressMonitor());

		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", "classic.asianet.co.th");
		props.setProperty("mail.user", senderEmail);
		props.setProperty("mail.password", "");
		Session session = Session.getDefaultInstance(props, null);
		try {
			Transport transport = session.getTransport();
			MimeMessage message = new MimeMessage(session);
			message.setSubject(subject);
			message.setContent(messageString, "text/plain");
			message.setFrom(new InternetAddress(senderEmail));

			try {
				for (User recipient : recipients) {
					String recipientEmail =
						((TextDataField)recipient.getPerson().getDataField(PersonStruct.INTERNET_EMAIL)).getText();
					message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			message.setRecipient(Message.RecipientType.TO, new InternetAddress("chairat@guinaree.com"));

			transport.connect();
			transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
			transport.close();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		//		PersistenceManager pm = getPersistenceManager();
		//		try {
		//			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
		//			if (fetchGroups != null)
		//				pm.getFetchPlan().setGroups(fetchGroups);
		//
		//			Query q = pm.newQuery(IssueResolution.class);
		//		} finally {
		//			pm.close();
		//		}
	}

	//Bean//
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			UserID systemUserID = UserID.create(getOrganisationID(), getUserID());
			User systemUser = (User)pm.getObjectById(systemUserID);

			// WORKAROUND JPOX Bug to avoid problems with creating workflows as State.statable is defined as interface and has subclassed implementations
			pm.getExtent(Issue.class);

			IssueStruct.getIssueStruct(pm);

			// The complete method is executed in *one* transaction. So if one thing fails, all fail.
			// => We check once at the beginning, if this module has already been initialised.
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireIssueTrackingEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of " + JFireIssueTrackingEAR.MODULE_NAME + " started...");

			moduleMetaData = pm.makePersistent(
					ModuleMetaData.createModuleMetaDataFromManifest(JFireIssueTrackingEAR.MODULE_NAME, JFireIssueTrackingEAR.class)
			);

			String baseName = "org.nightlabs.jfire.issue.resource.messages";
			ClassLoader loader = IssueManagerBean.class.getClassLoader();

/*			IssueType issueTypeDefault = new IssueType(getOrganisationID(), IssueType.DEFAULT_ISSUE_TYPE_ID);
			issueTypeDefault.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueTypeDefault"); //$NON-NLS-1$
			issueTypeDefault = pm.makePersistent(issueTypeDefault);

			// Create the statuses
			IssueSeverityType issueSeverityTypeMinor = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_MINOR);
			issueSeverityTypeMinor.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeMinor"); //$NON-NLS-1$
			issueSeverityTypeMinor = pm.makePersistent(issueSeverityTypeMinor);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeMinor);

			IssueSeverityType issueSeverityTypeMajor = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_MAJOR);
			issueSeverityTypeMajor.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeMajor"); //$NON-NLS-1$
			issueSeverityTypeMajor = pm.makePersistent(issueSeverityTypeMajor);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeMajor);

			IssueSeverityType issueSeverityTypeCrash = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_CRASH);
			issueSeverityTypeCrash.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeCrash"); //$NON-NLS-1$
			issueSeverityTypeCrash = pm.makePersistent(issueSeverityTypeCrash);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeCrash);

			IssueSeverityType issueSeverityTypeBlock = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_BLOCK);
			issueSeverityTypeBlock.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeBlock"); //$NON-NLS-1$
			issueSeverityTypeBlock = pm.makePersistent(issueSeverityTypeBlock);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeBlock);

			IssueSeverityType issueSeverityTypeFeature = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_FEATURE);
			issueSeverityTypeFeature.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeFeature"); //$NON-NLS-1$
			issueSeverityTypeFeature = pm.makePersistent(issueSeverityTypeFeature);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeFeature);

			IssueSeverityType issueSeverityTypeTrivial = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TRIVIAL);
			issueSeverityTypeTrivial.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeTrivial"); //$NON-NLS-1$
			issueSeverityTypeTrivial = pm.makePersistent(issueSeverityTypeTrivial);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeTrivial);

			IssueSeverityType issueSeverityTypeText = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TEXT);
			issueSeverityTypeText.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeText"); //$NON-NLS-1$
			issueSeverityTypeText = pm.makePersistent(issueSeverityTypeText);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeText);

			IssueSeverityType issueSeverityTypeTweak = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TWEAK);
			issueSeverityTypeTweak.getIssueSeverityTypeText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueSeverityTypeTweak"); //$NON-NLS-1$
			issueSeverityTypeTweak = pm.makePersistent(issueSeverityTypeTweak);
			issueTypeDefault.getIssueSeverityTypes().add(issueSeverityTypeTweak);

			////////////////////////////////////////////////////////
			// Create the priorities
			// check, whether the datastore is already initialized
			IssuePriority issuePriorityNone = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_NONE);
			issuePriorityNone.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issuePriorityNone"); //$NON-NLS-1$
			issuePriorityNone = pm.makePersistent(issuePriorityNone);
			issueTypeDefault.getIssuePriorities().add(issuePriorityNone);

			IssuePriority issuePriorityLow = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_LOW);
			issuePriorityLow.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issuePriorityLow"); //$NON-NLS-1$
			issuePriorityLow = pm.makePersistent(issuePriorityLow);
			issueTypeDefault.getIssuePriorities().add(issuePriorityLow);

			IssuePriority issuePriorityNormal = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_NORMAL);
			issuePriorityNormal.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issuePriorityNormal"); //$NON-NLS-1$
			issuePriorityNormal = pm.makePersistent(issuePriorityNormal);
			issueTypeDefault.getIssuePriorities().add(issuePriorityNormal);

			IssuePriority issuePriorityHigh = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_HIGH);
			issuePriorityHigh.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issuePriorityHigh"); //$NON-NLS-1$
			issuePriorityHigh = pm.makePersistent(issuePriorityHigh);
			issueTypeDefault.getIssuePriorities().add(issuePriorityHigh);

			IssuePriority issuePriorityUrgent = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_URGENT);
			issuePriorityUrgent.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issuePriorityUrgent"); //$NON-NLS-1$
			issuePriorityUrgent = pm.makePersistent(issuePriorityUrgent);
			issueTypeDefault.getIssuePriorities().add(issuePriorityUrgent);

			IssuePriority issuePriorityImmediate = new IssuePriority(getOrganisationID(), IssuePriority.ISSUE_PRIORITY_IMMEDIATE);
			issuePriorityImmediate.getIssuePriorityText().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issuePriorityImmediate"); //$NON-NLS-1$
			issuePriorityImmediate = pm.makePersistent(issuePriorityImmediate);
			issueTypeDefault.getIssuePriorities().add(issuePriorityImmediate);


			// Create the resolutions
			IssueResolution issueResolutionNotAssigned = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_ID_NOT_ASSIGNED.issueResolutionID);
			issueResolutionNotAssigned.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueResolutionNotAssigned"); //$NON-NLS-1$
			issueResolutionNotAssigned = pm.makePersistent(issueResolutionNotAssigned);
			issueTypeDefault.getIssueResolutions().add(issueResolutionNotAssigned);

			IssueResolution issueResolutionOpen = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_OPEN);
			issueResolutionOpen.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueResolutionOpen"); //$NON-NLS-1$
			issueResolutionOpen = pm.makePersistent(issueResolutionOpen);
			issueTypeDefault.getIssueResolutions().add(issueResolutionOpen);

			IssueResolution issueResolutionFixed = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_FIXED);
			issueResolutionFixed.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueResolutionFixed"); //$NON-NLS-1$
			issueResolutionFixed = pm.makePersistent(issueResolutionFixed);
			issueTypeDefault.getIssueResolutions().add(issueResolutionFixed);

			IssueResolution issueResolutionReopened = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_REOPENED);
			issueResolutionReopened.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueResolutionReopened"); //$NON-NLS-1$
			issueResolutionReopened = pm.makePersistent(issueResolutionReopened);
			issueTypeDefault.getIssueResolutions().add(issueResolutionReopened);

			IssueResolution issueResolutionNotFixable = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_NOTFIXABLE);
			issueResolutionNotFixable.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueResolutionNotFixable"); //$NON-NLS-1$
			issueResolutionNotFixable = pm.makePersistent(issueResolutionNotFixable);
			issueTypeDefault.getIssueResolutions().add(issueResolutionNotFixable);

			IssueResolution issueResolutionWillNotFix = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_WILLNOTFIX);
			issueResolutionWillNotFix.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueResolutionWillNotFix"); //$NON-NLS-1$
			issueResolutionWillNotFix = pm.makePersistent(issueResolutionWillNotFix);
			issueTypeDefault.getIssueResolutions().add(issueResolutionWillNotFix);

			// Create the process definitions.
			issueTypeDefault.readProcessDefinition(IssueType.class.getResource("jbpm/status/"));

			IssueType issueTypeCustomer = new IssueType(getOrganisationID(), "Customer");
			issueTypeCustomer.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueTypeCustomer"); //$NON-NLS-1$
			issueTypeCustomer = pm.makePersistent(issueTypeCustomer);

			// Create the process definitions.
			issueTypeCustomer.readProcessDefinition(IssueType.class.getResource("jbpm/status/"));



			// ---[ IssueLinkTypes ]--------------------------------------------------------------------------------------------| Start |---
			IssueLinkType issueLinkTypeRelated = new IssueLinkType(IssueLinkType.ISSUE_LINK_TYPE_ID_RELATED);
			issueLinkTypeRelated.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueLinkTypeRelated"); //$NON-NLS-1$
			issueLinkTypeRelated.addLinkedObjectClass(Object.class);
			issueLinkTypeRelated.addNotLinkedObjectClass(Issue.class);
			issueLinkTypeRelated = pm.makePersistent(issueLinkTypeRelated);

			IssueLinkType issueLinkTypeRelatedIssue = new IssueLinkTypeIssueToIssue(IssueLinkTypeIssueToIssue.ISSUE_LINK_TYPE_ID_RELATED_ISSUE);
			issueLinkTypeRelatedIssue.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueLinkTypeRelatedIssue"); //$NON-NLS-1$
			issueLinkTypeRelatedIssue.addLinkedObjectClass(Issue.class);
			issueLinkTypeRelatedIssue = pm.makePersistent(issueLinkTypeRelatedIssue);

			IssueLinkType issueLinkTypeParent = new IssueLinkTypeParentChild(IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_PARENT);
			issueLinkTypeParent.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.issueLinkTypeParent"); //$NON-NLS-1$

			issueLinkTypeParent.addLinkedObjectClass(Issue.class);
			issueLinkTypeParent = pm.makePersistent(issueLinkTypeParent);

			IssueLinkType issueLinkTypeChild = new IssueLinkTypeParentChild(IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_CHILD);
			issueLinkTypeChild.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.IssueManagerBean.issueLinkTypeChild" ); //$NON-NLS-1$
			issueLinkTypeChild.addLinkedObjectClass(Issue.class);
			issueLinkTypeChild = pm.makePersistent(issueLinkTypeChild);

			IssueLinkType issueLinkTypeIsDuplicate = new IssueLinkTypeDuplicate(IssueLinkTypeDuplicate.ISSUE_LINK_TYPE_ID_IS_DUPLICATE);
			issueLinkTypeIsDuplicate.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.IssueManagerBean.issueLinkTypeIsDuplicate" ); //$NON-NLS-1$
			issueLinkTypeIsDuplicate.addLinkedObjectClass(Issue.class);
			issueLinkTypeIsDuplicate = pm.makePersistent(issueLinkTypeIsDuplicate);

			IssueLinkType issueLinkTypeHasDuplicate = new IssueLinkTypeDuplicate(IssueLinkTypeDuplicate.ISSUE_LINK_TYPE_ID_HAS_DUPLICATE);
			issueLinkTypeHasDuplicate.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.IssueManagerBean.issueLinkTypeHasDuplicate" ); //$NON-NLS-1$
			issueLinkTypeHasDuplicate.addLinkedObjectClass(Issue.class);
			issueLinkTypeHasDuplicate = pm.makePersistent(issueLinkTypeHasDuplicate);
			// ---[ IssueLinkTypes ]----------------------------------------------------------------------------------------------| End |---



			// ---[ ProjectTypes ]----------------------------------------------------------------------------------------------| Start |---
			pm.getExtent(ProjectType.class);

			ProjectType projectTypeDefault = new ProjectType(ProjectType.PROJECT_TYPE_ID_DEFAULT);
			projectTypeDefault.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.projectTypeDefault"); //$NON-NLS-1$

			projectTypeDefault = pm.makePersistent(projectTypeDefault);

			ProjectType projectTypeSoftware = new ProjectType(IDGenerator.getOrganisationID(), "software");
			projectTypeSoftware.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.software"); //$NON-NLS-1$

			projectTypeSoftware = pm.makePersistent(projectTypeSoftware);

			// Create the projects
			pm.getExtent(Project.class);

			Project projectDefault = new Project(Project.PROJECT_ID_DEFAULT);
			projectDefault.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.projectDefault"); //$NON-NLS-1$

			projectDefault.setProjectType(projectTypeDefault);
			projectDefault = pm.makePersistent(projectDefault);

			Project jfireProject = new Project(IDGenerator.getOrganisationID(), IDGenerator.nextID(Project.class));
			jfireProject.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.jfireProject"); //$NON-NLS-1$
			jfireProject.setProjectType(projectTypeSoftware);
			jfireProject = pm.makePersistent(jfireProject);*/
			// ---[ ProjectTypes ]------------------------------------------------------------------------------------------------| End |---



			// ---[ IssueMarkers ]----------------------------------------------------------------------------------------------| Start |---
			IssueMarker issueMarker_Email = new IssueMarker(false);
			assignIssueMarkerIcon16x16(issueMarker_Email, "IssueMarker-email.16x16.png");
			issueMarker_Email.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.issuemarker.IssueMarkerEmail.name");
			issueMarker_Email.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.issuemarker.IssueMarkerEmail.description");
			issueMarker_Email = pm.makePersistent(issueMarker_Email);

			IssueMarker issueMarker_Phone = new IssueMarker(false);
			assignIssueMarkerIcon16x16(issueMarker_Phone, "IssueMarker-phone.16x16.png");
			issueMarker_Phone.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.issuemarker.IssueMarkerPhone.name");
			issueMarker_Phone.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.issuemarker.IssueMarkerPhone.description");
			issueMarker_Phone = pm.makePersistent(issueMarker_Phone);


			IssueMarker issueMarker_EmailTodo = new IssueMarker(false);
			assignIssueMarkerIcon16x16(issueMarker_EmailTodo, "IssueMarker-email-todo.16x16.png");
			issueMarker_EmailTodo.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.issuemarker.IssueMarkerEmailTodo.name");
			issueMarker_EmailTodo.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.issuemarker.IssueMarkerEmailTodo.description");
			issueMarker_EmailTodo = pm.makePersistent(issueMarker_EmailTodo);

			IssueMarker issueMarker_PhoneTodo = new IssueMarker(false);
			assignIssueMarkerIcon16x16(issueMarker_PhoneTodo, "IssueMarker-phone-todo.16x16.png");
			issueMarker_PhoneTodo.getName().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.issuemarker.IssueMarkerPhoneTodo.name");
			issueMarker_PhoneTodo.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.issuemarker.IssueMarkerPhoneTodo.description");
			issueMarker_PhoneTodo = pm.makePersistent(issueMarker_PhoneTodo);
			// ---[ IssueMarkers ]------------------------------------------------------------------------------------------------| End |---



			// TODO Move the following DEMO Issues to a demo-data-creation module; yet to be created. Coming soon... stay tuned ;-)
			//      Done. Moved to InitialiserIssueTracking.
//			// ---[ Issues ]----------------------------------------------------------------------------------------------------| Start |---
//			pm.getExtent(Issue.class);
//
//			int totDemoIssueCnt = 56;
//			boolean isCreateArbitraryIssueToIssueLinks = !true;
//			List<IssuePriority> def_issuePriorities = issueTypeDefault.getIssuePriorities();
//			List<IssueResolution> def_issueResolutions = issueTypeDefault.getIssueResolutions();
//			List<IssueSeverityType> def_issueSeverityTypes = issueTypeDefault.getIssueSeverityTypes();
//			List<Issue> demoIssues = new ArrayList<Issue>(totDemoIssueCnt);
//
//			Random rndGen = new Random( System.currentTimeMillis() );
//			for (int i=0; i<totDemoIssueCnt; i++) {
//				Issue demoIssue = new Issue(true, issueTypeDefault);
//
//				// Essential issue settings
//				demoIssue.setReporter(systemUser);
//				demoIssue.setIssuePriority( def_issuePriorities.get( rndGen.nextInt(def_issuePriorities.size()) ) );
//				demoIssue.setIssueResolution( def_issueResolutions.get( rndGen.nextInt(def_issueResolutions.size()) ) );
//				demoIssue.setIssueSeverityType( def_issueSeverityTypes.get( rndGen.nextInt(def_issueSeverityTypes.size()) ) );
//
//				// Subject and description
//				demoIssue.getSubject().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.IssueManagerBean.issueSubject" + (i+1)); //$NON-NLS-1$
//				demoIssue.getDescription().readFromProperties(baseName, loader, "org.nightlabs.jfire.issue.IssueManagerBean.issueDescription" + (i+1)); //$NON-NLS-1$
//
//				// Markers
//				if (rndGen.nextInt(100) < 40)	demoIssue.addIssueMarker(issueMarker_Phone);
//				if (rndGen.nextInt(100) < 65)	demoIssue.addIssueMarker(issueMarker_Email);
//
//
//				// Randomly (and playfully?) create links between Issues.
//				if (isCreateArbitraryIssueToIssueLinks && demoIssues.size() > 5) {
//					List<Integer> usedIndexRefs = new ArrayList<Integer>();
//					while (rndGen.nextInt(100) < 67 && usedIndexRefs.size() < demoIssues.size()/2) {
//						int index = rndGen.nextInt(demoIssues.size());
//						if ( !usedIndexRefs.contains(index) ) {
//							demoIssue.createIssueLink(issueLinkTypeParent, demoIssues.get(index));
//							usedIndexRefs.add(index);
//						}
//					}
//				}
//
//
//				// Done and store.
//				demoIssues.add(demoIssue);
//				storeIssue(demoIssue, false, new String[0], NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//			}
//			// ---[ Issues ]------------------------------------------------------------------------------------------------------| End |---



			// ---[ Predefined Query Stores ]-----------------------------------------------------------------------------------| Start |---
//			ProcessDefinitionID processDefinitionID = (ProcessDefinitionID)JDOHelper.getObjectId(issueTypeDefault.getProcessDefinition());
			IssueQuery newIssueIssueQuery = new IssueQuery();
			newIssueIssueQuery.clearQuery();
			newIssueIssueQuery.setAllFieldsDisabled();
			newIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.processDefinitionID, true);
			newIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.jbpmNodeName, true);
			newIssueIssueQuery.setJbpmNodeName(JbpmConstantsIssue.NODE_NAME_NEW);

			IssueQuery unassignedIssueIssueQuery = new IssueQuery();
			unassignedIssueIssueQuery.clearQuery();
			unassignedIssueIssueQuery.setAllFieldsDisabled();
			unassignedIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.processDefinitionID, true);
			unassignedIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.jbpmNodeName, true);
			unassignedIssueIssueQuery.setJbpmNodeName(JbpmConstantsIssue.NODE_NAME_OPEN);

			IssueQuery resolvedIssueIssueQuery = new IssueQuery();
			resolvedIssueIssueQuery.clearQuery();
			resolvedIssueIssueQuery.setAllFieldsDisabled();
			resolvedIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.processDefinitionID, true);
			resolvedIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.jbpmNodeName, true);
			resolvedIssueIssueQuery.setJbpmNodeName(JbpmConstantsIssue.NODE_NAME_RESOLVED);

			IssueQuery acknowledgedIssueIssueQuery = new IssueQuery();
			acknowledgedIssueIssueQuery.clearQuery();
			acknowledgedIssueIssueQuery.setAllFieldsDisabled();
			acknowledgedIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.processDefinitionID, true);
			acknowledgedIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.jbpmNodeName, true);
			acknowledgedIssueIssueQuery.setJbpmNodeName(JbpmConstantsIssue.NODE_NAME_ACKNOWLEDGED);

			IssueQuery closedIssueIssueQuery = new IssueQuery();
			closedIssueIssueQuery.clearQuery();
			closedIssueIssueQuery.setAllFieldsDisabled();
			closedIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.processDefinitionID, true);
			closedIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.jbpmNodeName, true);
			closedIssueIssueQuery.setJbpmNodeName(JbpmConstantsIssue.NODE_NAME_CLOSED);

			IssueQuery confirmedIssueIssueQuery = new IssueQuery();
			confirmedIssueIssueQuery.clearQuery();
			confirmedIssueIssueQuery.setAllFieldsDisabled();
			confirmedIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.processDefinitionID, true);
			confirmedIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.jbpmNodeName, true);
			confirmedIssueIssueQuery.setJbpmNodeName(JbpmConstantsIssue.NODE_NAME_CONFIRMED);

			IssueQuery rejectedIssueIssueQuery = new IssueQuery();
			rejectedIssueIssueQuery.clearQuery();
			rejectedIssueIssueQuery.setAllFieldsDisabled();
			rejectedIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.processDefinitionID, true);
			rejectedIssueIssueQuery.setFieldEnabled(IssueQuery.FieldName.jbpmNodeName, true);
			rejectedIssueIssueQuery.setJbpmNodeName(JbpmConstantsIssue.NODE_NAME_REJECTED);

			//1 Unassigned Issues
			pm.getExtent(BaseQueryStore.class);

			QueryCollection<IssueQuery> queryCollection = new QueryCollection<IssueQuery>(Issue.class);
			queryCollection.add(unassignedIssueIssueQuery);

			BaseQueryStore queryStore = new BaseQueryStore(systemUser,
					IDGenerator.nextID(BaseQueryStore.class), queryCollection);

			queryStore.setQueryCollection(queryCollection);
			queryStore.setPubliclyAvailable(true);
			queryStore.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreUnassigned"); //$NON-NLS-1$
			queryStore.getDescription().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreUnassigned"); //$NON-NLS-1$
			queryStore.serialiseCollection();
			queryStore = pm.makePersistent(queryStore);

			//2 Resolved Issues
			queryCollection = new QueryCollection<IssueQuery>(Issue.class);
			queryCollection.add(resolvedIssueIssueQuery);

			queryStore = new BaseQueryStore(systemUser,
					IDGenerator.nextID(BaseQueryStore.class), queryCollection);

			queryStore.setQueryCollection(queryCollection);
			queryStore.setPubliclyAvailable(true);
			queryStore.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreResolved"); //$NON-NLS-1$
			queryStore.getDescription().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreResolved"); //$NON-NLS-1$
			queryStore.serialiseCollection();
			queryStore = pm.makePersistent(queryStore);

			//3 Acknowledged Issues
			queryCollection = new QueryCollection<IssueQuery>(Issue.class);
			queryCollection.add(acknowledgedIssueIssueQuery);

			queryStore = new BaseQueryStore(systemUser,
					IDGenerator.nextID(BaseQueryStore.class), queryCollection);

			queryStore.setQueryCollection(queryCollection);
			queryStore.setPubliclyAvailable(true);
			queryStore.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreAcknowledged"); //$NON-NLS-1$
			queryStore.getDescription().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreAcknowledged"); //$NON-NLS-1$
			queryStore.serialiseCollection();
			queryStore = pm.makePersistent(queryStore);

			//4 Closed Issues
			queryCollection = new QueryCollection<IssueQuery>(Issue.class);
			queryCollection.add(closedIssueIssueQuery);

			queryStore = new BaseQueryStore(systemUser,
					IDGenerator.nextID(BaseQueryStore.class), queryCollection);

			queryStore.setQueryCollection(queryCollection);
			queryStore.setPubliclyAvailable(true);
			queryStore.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreclosed"); //$NON-NLS-1$
			queryStore.getDescription().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreclosed"); //$NON-NLS-1$
			queryStore.serialiseCollection();
			queryStore = pm.makePersistent(queryStore);

			//5 Confirmed Issues
			queryCollection = new QueryCollection<IssueQuery>(Issue.class);
			queryCollection.add(confirmedIssueIssueQuery);

			queryStore = new BaseQueryStore(systemUser,
					IDGenerator.nextID(BaseQueryStore.class), queryCollection);

			queryStore.setQueryCollection(queryCollection);
			queryStore.setPubliclyAvailable(true);
			queryStore.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreConfirmed"); //$NON-NLS-1$
			queryStore.getDescription().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreConfirmed"); //$NON-NLS-1$
			queryStore.serialiseCollection();
			queryStore = pm.makePersistent(queryStore);

			//6 Rejected Issues
			queryCollection = new QueryCollection<IssueQuery>(Issue.class);
			queryCollection.add(rejectedIssueIssueQuery);

			queryStore = new BaseQueryStore(systemUser,
					IDGenerator.nextID(BaseQueryStore.class), queryCollection);

			queryStore.setQueryCollection(queryCollection);
			queryStore.setPubliclyAvailable(true);
			queryStore.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreRejected"); //$NON-NLS-1$
			queryStore.getDescription().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreRejected"); //$NON-NLS-1$
			queryStore.serialiseCollection();
			queryStore = pm.makePersistent(queryStore);

			//7 New Issues
			queryCollection = new QueryCollection<IssueQuery>(Issue.class);
			queryCollection.add(newIssueIssueQuery);

			queryStore = new BaseQueryStore(systemUser,
					IDGenerator.nextID(BaseQueryStore.class), queryCollection);

			queryStore.setQueryCollection(queryCollection);
			queryStore.setPubliclyAvailable(true);
			queryStore.getName().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreNew"); //$NON-NLS-1$
			queryStore.getDescription().readFromProperties(baseName, loader,
			"org.nightlabs.jfire.issue.IssueManagerBean.queryStoreNew"); //$NON-NLS-1$
			queryStore.serialiseCollection();
			queryStore = pm.makePersistent(queryStore);

			//EditLock
			EditLockType issueEditLock = new EditLockType(EditLockTypeIssue.EDIT_LOCK_TYPE_ID);
			issueEditLock = pm.makePersistent(issueEditLock);
			//------------------------------------------------
		} finally {
			pm.close();
		}
	}

	// Note: (From current standards)
	// 1. objClassName: Offer (jdo/org.nightlabs.jfire.trade.id.OfferID?organisationID=chezfrancois.jfire.org&offerIDPrefix=2009&offerID=2)
	// 2. objClassName: Order (jdo/org.nightlabs.jfire.trade.id.OrderID?organisationID=chezfrancois.jfire.org&orderIDPrefix=2009&orderID=8)
	// 3. objClassName: Invoice (jdo/org.nightlabs.jfire.accounting.id.InvoiceID?organisationID=chezfrancois.jfire.org&invoiceIDPrefix=2009&invoiceID=3)
	// 4. objClassName: DeliveryNote (jdo/org.nightlabs.jfire.store.id.DeliveryNoteID?organisationID=chezfrancois.jfire.org&deliveryNoteIDPrefix=2009&deliveryNoteID=5)
	// 5. objClassName: Issue (jdo/org.nightlabs.jfire.issue.id.IssueID?organisationID=chezfrancois.jfire.org&issueID=1)
	// 6. objClassName: PropertySet (jdo/org.nightlabs.jfire.prop.id.PropertySetID?organisationID=chezfrancois.jfire.org&propertySetID=1)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void convertIssueLinkHistoryItemLinkedObjectID() {
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(IssueHistoryItem.class);
			q.setResult("JDOHelper.getObjectId(this)");
			Collection<IssueHistoryItemID> issueHistoryItemIDs = (Collection<IssueHistoryItemID>)q.execute();
			List<IssueHistoryItem> issueHistoryItems =
				getIssueHistoryItems(issueHistoryItemIDs, new String[] {FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

			for (IssueHistoryItem issueHistoryItem : issueHistoryItems) {
				if (issueHistoryItem instanceof IssueLinkHistoryItem) {
					IssueLinkHistoryItem issueLinkHistoryItem = (IssueLinkHistoryItem) issueHistoryItem;
					StringBuilder newLinkedObjectID = new StringBuilder();
					String linkedObjectID = issueLinkHistoryItem.getLinkedObjectID();
					if (!linkedObjectID.toLowerCase().startsWith("jdo")) {
						String className = issueLinkHistoryItem.getLinkedObjectClassName();
						if (className.equals("Order")) { //---> organisationID + '/' + orderIDPrefix + '/' + ObjectIDUtil.longObjectIDFieldToString(orderID);
							String[] primaryKeyStrings = linkedObjectID.split("/");
							String organisationID = primaryKeyStrings[0];
							String orderIDPrefix = primaryKeyStrings[1];
							String longObjectIDString = primaryKeyStrings[2];
							newLinkedObjectID.append("jdo/org.nightlabs.jfire.trade.id.OrderID?");
							newLinkedObjectID.append("organisationID=").append(organisationID);
							newLinkedObjectID.append("orderIDPrefix=").append(orderIDPrefix);
							newLinkedObjectID.append("offerID=").append(longObjectIDString);
						}
						else if (className.equals("Offer")) { //---> organisationID + '/' + offerIDPrefix + '/' + ObjectIDUtil.longObjectIDFieldToString(offerID);
							String[] primaryKeyStrings = linkedObjectID.split("/");
							String organisationID = primaryKeyStrings[0];
							String offerIDPrefix = primaryKeyStrings[1];
							String longObjectIDString = primaryKeyStrings[2];
							newLinkedObjectID.append("jdo/org.nightlabs.jfire.trade.id.OfferID?");
							newLinkedObjectID.append("organisationID=").append(organisationID);
							newLinkedObjectID.append("offerIDPrefix=").append(offerIDPrefix);
							newLinkedObjectID.append("offerID=").append(longObjectIDString);
						}
						else if (className.equals("Invoice")) { //---> organisationID + '/' + invoiceIDPrefix + '/' + ObjectIDUtil.longObjectIDFieldToString(invoiceID);
							String[] primaryKeyStrings = linkedObjectID.split("/");
							String organisationID = primaryKeyStrings[0];
							String invoiceIDPrefix = primaryKeyStrings[1];
							String longObjectIDString = primaryKeyStrings[2];
							newLinkedObjectID.append("jdo/org.nightlabs.jfire.accounting.id.InvoiceID?");
							newLinkedObjectID.append("organisationID=").append(organisationID);
							newLinkedObjectID.append("invoiceIDPrefix=").append(invoiceIDPrefix);
							newLinkedObjectID.append("invoiceID=").append(longObjectIDString);
						}
						else if (className.equals("Person")) { //---> ((Person)linkedObject).getDisplayName();
							String personName = linkedObjectID; //(jdo/org.nightlabs.jfire.prop.id.PropertySetID?organisationID=chezfrancois.jfire.org&propertySetID=1)
							String organisationID = issueLinkHistoryItem.getOrganisationID();
							PropSearchFilter searchFilter = new PersonSearchFilter();
							searchFilter.setFieldValue(PersonStruct.PERSONALDATA_NAME.structFieldID, personName);
						}
						else if (className.equals("Issue")) { //---> ObjectIDUtil.longObjectIDFieldToString(issue.getIssueID()) + " " + issue.getSubject().getText()
							String[] primaryKeyStrings = linkedObjectID.split(" ");
							String organisationID = issueLinkHistoryItem.getOrganisationID();
							String issueID = primaryKeyStrings[0];
							newLinkedObjectID.append("jdo/org.nightlabs.jfire.issue.id.IssueID?");
							newLinkedObjectID.append("organisationID=").append(organisationID);
							newLinkedObjectID.append("issueID=").append(issueID);
						}
						else {
							//don't know yet!!!
						}

						issueLinkHistoryItem.setLinkedObjectID(newLinkedObjectID.toString());
					}
				}
			}
		}
		finally {
			pm.close();
		}

//		List<IssueLink> issueLinks = getIssueLinks(getIssueLinkIDs(), new String[] {FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//		for (IssueLink issueLink : issueLinks) {
//			issueLink.getLinkedObject()
//		}
//		Query q = pm.newQuery(IssueHistoryItem.class);
//		q.setResult("JDOHelper.getObjectId(this)");
//		Set<IssueHistoryItemID> issueHistoryItemIDs = (Set<IssueHistoryItemID>)q.execute();

//		List<IssueHistoryItem> issueHistoryItems = getIssueHistoryItems(issueHistoryItemIDs, new String[] { FetchPlan.DEFAULT }, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//		for (IssueHistoryItem issueHistoryItem : issueHistoryItems) {
//			if (issueHistoryItem instanceof IssueLinkHistoryItem) {
//				IssueLinkHistoryItem issueLinkHistoryItem = (IssueLinkHistoryItem) issueHistoryItem;
//				if (!issueLinkHistoryItem.getLinkedObjectID().startsWith("jdo")) {
//					issueLinkHistoryItem.setLinkedObjectID(issueLinkHistoryItem.get)
//				}
//			}
//		}
	}

	private static void assignIssueMarkerIcon16x16(IssueMarker issueMarker, String iconFileName) throws IOException
	{
		InputStream in = Messages.class.getResourceAsStream(iconFileName);
		if (in == null)
			throw new IllegalArgumentException("There is no resource named \"" + iconFileName + "\" in the package " + Messages.class.getPackage().getName() + "!");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtil.transferStreamData(in, out);
		in.close();
		out.close();

		if (logger.isDebugEnabled())
			logger.debug("--> Received: iconFilename: " + iconFileName);

		issueMarker.setIcon16x16Data(out.toByteArray());
	}

	@Override
	public List<IssueCommentID> getIssueCommentIDs(IssueID issueID) {
		PersistenceManager pm = createPersistenceManager();
		try {
			Issue issue = (Issue) pm.getObjectById(issueID);
			return NLJDOHelper.getObjectIDList(issue.getComments());
		} finally {
			pm.close();
		}
	}

	@Override
	public List<IssueCommentID> getIssueCommentIDsOfIssueOfIssueLink(IssueLinkID issueLinkID) {
		PersistenceManager pm = createPersistenceManager();
		try {
			IssueLink issueLink = (IssueLink) pm.getObjectById(issueLinkID);
			return NLJDOHelper.getObjectIDList(issueLink.getIssue().getComments());
		} finally {
			pm.close();
		}
	}

	@Override
	public Map<IssueLinkID, Long> getIssueCommentCountsOfIssueOfIssueLinks(Collection<IssueLinkID> issueLinkIDs) {
		PersistenceManager pm = createPersistenceManager();
		try {
			Map<IssueLinkID, Long> result = new HashMap<IssueLinkID, Long>(issueLinkIDs.size());
			for (IssueLinkID issueLinkID : issueLinkIDs) {
				IssueLink issueLink = (IssueLink) pm.getObjectById(issueLinkID);
				result.put(issueLinkID, (long)issueLink.getIssue().getComments().size());
			}
			return result;
		} finally {
			pm.close();
		}
	}

	@Override
	public Collection<IssueLinkID> getIssueLinkIDsForIssueAndLinkedObjectClasses(IssueID issueID, Set<Class<?>> linkedObjectClasses) {
		PersistenceManager pm = createPersistenceManager();
		try {
			Issue issue;
			try {
				issue = (Issue) pm.getObjectById(issueID);
			} catch (JDOObjectNotFoundException x) {
				return Collections.emptySet();
			}

			if (linkedObjectClasses == null) {
				linkedObjectClasses = new HashSet<Class<?>>(1);
				linkedObjectClasses.add(Object.class);
			}

			Collection<IssueLinkID> result = new HashSet<IssueLinkID>();
			iterateIssueLinks: for (IssueLink issueLink : issue.getIssueLinks()) {
				for (Class<?> linkedObjectClass : linkedObjectClasses) {
					if (linkedObjectClass.isAssignableFrom(issueLink.getLinkedObjectClass())) {
						IssueLinkID issueLinkID = (IssueLinkID) JDOHelper.getObjectId(issueLink);
						if (issueLinkID == null)
							throw new IllegalStateException("JDOHelper.getObjectId(issueLinkID) returned null! " + issueLinkID);

						result.add(issueLinkID);
						continue iterateIssueLinks;
					}
				}
			}
			return result;
		} finally {
			pm.close();
		}
	}
}