package org.nightlabs.jfire.issue;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.FetchPlanBackup;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.JDOQueryCollectionDecorator;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireBaseEAR;
import org.nightlabs.jfire.editlock.EditLockType;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.issue.history.IssueHistory;
import org.nightlabs.jfire.issue.id.IssueCommentID;
import org.nightlabs.jfire.issue.id.IssueFileAttachmentID;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.issue.id.IssueLocalID;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueResolutionID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.issue.jbpm.JbpmConstants;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.util.Util;

/**
 * @author Chairat Kongarayawetchakun - chairat[AT]nightlabs[DOT]de
 * 
 * @ejb.bean name="jfire/ejb/JFireIssueTracking/IssueManager"	
 *           jndi-name="jfire/ejb/JFireIssueTracking/IssueManager"
 *           type="Stateless" 
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public class IssueManagerBean 
extends BaseSessionBeanImpl
implements SessionBean
{

	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(IssueManagerBean.class);

	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}

	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		logger.debug(this.getClass().getName() + ".ejbCreate()");
	}

	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbRemove()");
	}

	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbActivate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbPassivate()");
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<IssueID> getIssueIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Issue.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<IssueID>((Collection<? extends IssueID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<IssueFileAttachmentID> getIssueFileAttachmentIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(IssueFileAttachment.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<IssueFileAttachmentID>((Collection<? extends IssueFileAttachmentID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<IssueID> getIssueIDs(QueryCollection<? extends AbstractJDOQuery> queries)
	{
		if (queries == null)
			return null;

		if (! Issue.class.isAssignableFrom(queries.getResultClass()))
		{
			throw new RuntimeException("Given QueryCollection has invalid return type! " +
					"Invalid return type= "+ queries.getResultClassName());
		}

		PersistenceManager pm = getPersistenceManager();
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
			Collection<? extends Issue> issues = (Collection<? extends Issue>) decoratedCollection.executeQueries();

			return NLJDOHelper.getObjectIDSet(issues);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<Issue> getIssues(Collection<IssueID> issueIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueIDs, Issue.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueFileAttachment> getIssueFileAttachments(Collection<IssueFileAttachmentID> issueFileAttachmentIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueFileAttachmentIDs, IssueFileAttachment.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<IssueSeverityTypeID> getIssueSeverityTypeIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(IssueSeverityType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<IssueSeverityTypeID>((Collection<? extends IssueSeverityTypeID>) q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueSeverityType> getIssueSeverityTypes(Collection<IssueSeverityTypeID> issueSeverityTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueSeverityTypeIDs, IssueSeverityType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Collection<IssueHistory> getIssueHistoryByIssue(Issue issue)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newNamedQuery(IssueHistory.class, "getIssueHistoriesByOrganisationIDAndIssueID");
			Map<String, Object> params = new HashMap<String, Object>(3);
			params.put("issueID", issue.getIssueID());
			params.put("organisationID", issue.getOrganisationID());
			
			Collection<IssueHistory> ih = getIssueHistoryByIssue(pm, issue);
			return ih;
		} finally {
			pm.close();
		}
	}

	/**
	 * @param pm The <code>PersistenceManager</code> that should be used to access the datastore.
	 * @param issue
	 * @return Returns instances of <code>IssueHistory</code>.
	 */
	@SuppressWarnings("unchecked")
	protected static Collection<IssueHistory> getIssueHistoryByIssue(PersistenceManager pm, Issue issue)
	{
		final Query q = pm.newNamedQuery(IssueHistory.class, IssueHistory.QUERY_ISSUE_HISTORIES_BY_ORGANISATION_ID_AND_ISSUE_ID);
		Map<String, Object> params = new HashMap<String, Object>(3);
		params.put("issueID", issue.getIssueID());
		params.put("organisationID", issue.getOrganisationID());
		return (Collection<IssueHistory>)q.executeWithMap(params);
	}

//	/**
//	* @throws ModuleException
//	*
//	* @ejb.interface-method
//	* @ejb.transaction type="Required"
//	* @ejb.permission role-name="_Guest_"
//	*/
//	public Collection getIssuePriorities(String[] fetchGroups, int maxFetchDepth)
//	throws ModuleException
//	{
//	PersistenceManager pm = getPersistenceManager();
//	try {
//	pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//	if (fetchGroups != null)
//	pm.getFetchPlan().setGroups(fetchGroups);

//	Query q = pm.newQuery(IssuePriority.class);
//	return pm.detachCopyAll((Collection)q.execute());
//	} finally {
//	pm.close();
//	}
//	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<IssuePriorityID> getIssuePriorityIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(IssuePriority.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<IssuePriorityID>((Collection)q.execute()); 
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssuePriority> getIssuePriorities(Collection<IssuePriorityID> issuePriorityIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issuePriorityIDs, IssuePriority.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getIssueResolutions(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(IssueResolution.class);
			return pm.detachCopyAll((Collection)q.execute());
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueResolution> getIssueResolutions(Collection<IssueResolutionID> issueResolutionIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueResolutionIDs, IssueResolution.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

//	/**
//	* @throws ModuleException
//	*
//	* @ejb.interface-method
//	* @ejb.transaction type="Required"
//	* @ejb.permission role-name="_Guest_"
//	*/
//	public Collection getIssueTypes(String[] fetchGroups, int maxFetchDepth)
//	throws ModuleException
//	{
//	PersistenceManager pm = getPersistenceManager();
//	try {
//	return NLJDOHelper.getDetachedObjectList(pm, userGroupIDs, null, fetchGroups, maxFetchDepth);
//	}
//	finally {
//	pm.close();
//	}

//	PersistenceManager pm = getPersistenceManager();
//	try {
//	pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//	if (fetchGroups != null)
//	pm.getFetchPlan().setGroups(fetchGroups);

//	Query q = pm.newQuery(IssueType.class);
//	return pm.detachCopyAll((Collection)q.execute());
//	} finally {
//	pm.close();
//	}
//	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueType> getIssueTypes(Collection<IssueTypeID> issueTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueTypeIDs, IssueType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<IssueTypeID> getIssueTypeIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			final Query allIDsQuery = pm.newNamedQuery(IssueType.class, IssueType.QUERY_ALL_ISSUETYPE_IDS);
			return new HashSet<IssueTypeID>((Collection<? extends IssueTypeID>)allIDsQuery.execute());
		} finally {
			pm.close();
		}
	}


//	/**
//	* @throws ModuleException
//	*
//	* @ejb.interface-method
//	* @ejb.transaction type="Required"
//	* @ejb.permission role-name="_Guest_"
//	*/
//	public Collection getIssueComments(String[] fetchGroups, int maxFetchDepth)
//	{
//	PersistenceManager pm = getPersistenceManager();
//	try {
//	pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//	if (fetchGroups != null)
//	pm.getFetchPlan().setGroups(fetchGroups);

//	Query q = pm.newQuery(IssueComment.class);
//	return pm.detachCopyAll((Collection)q.execute());
//	} finally {
//	pm.close();
//	}
//	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueComment> getIssueComments(Collection<IssueCommentID> issueCommentIDs, 
			String[] fetchGroups, 
			int maxFetchDepth)
			{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueCommentIDs, IssueComment.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
			}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<IssueLinkType> getIssueLinkTypes(Collection<IssueLinkTypeID> issueLinkTypeIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, issueLinkTypeIDs, IssueLinkType.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<IssueLinkTypeID> getIssueLinkTypeIDs(Class<? extends Object> linkedObjectClass)
	{
		PersistenceManager pm = getPersistenceManager();
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

	/**
	 * @ejb.interface-method
	 * @!ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Set<IssueLinkTypeID> getIssueLinkTypeIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(IssueLinkType.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<IssueLinkTypeID>((Collection)q.execute()); 
		} finally {
			pm.close();
		}
	}


	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Issue storeIssue(Issue issue, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		Issue pIssue = null;
		try {
			boolean isNewIssue = !JDOHelper.isDetached(issue);

			if (isNewIssue) {
				pIssue = pm.makePersistent(issue);
				
				IssueType type;

				if (JFireBaseEAR.JPOX_WORKAROUND_FLUSH_ENABLED) {				
					pm.flush();
					// create the ProcessInstance for new Issues
					// TODO: WORKAROUND: Calling createProcessInstanceForIssue on pIssue.getIssueType() says that this IssueType is not persistent ?!?
					type = (IssueType) pm.getObjectById(JDOHelper.getObjectId(pIssue.getIssueType()));
					if (type == null) {
						throw new IllegalStateException("Could not create ProcessInstance for new Issue as its type is null");
					}
				}
				else
					type = pIssue.getIssueType();

				type.createProcessInstanceForIssue(pIssue);
			}
			else {
				if (issue.getCreateTimestamp() != null) {
					issue.setUpdateTimestamp(new Date());
				}

				IssueID issueID = (IssueID) JDOHelper.getObjectId(issue);
				Issue oldPersistentIssue = (Issue) pm.getObjectById(issueID);

				IssueHistory issueHistory = new IssueHistory(oldPersistentIssue, issue, IDGenerator.nextID(IssueHistory.class));
				storeIssueHistory(issueHistory, false, new String[]{FetchPlan.DEFAULT}, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

				if (issue.getCreateTimestamp() != null) {
					issue.setUpdateTimestamp(new Date());
				}

				boolean doUnassign;
				if (oldPersistentIssue == null)
					doUnassign = false;
				else {
					try {
						doUnassign = oldPersistentIssue.getAssignee() != null && issue.getAssignee() == null;
					} catch (JDODetachedFieldAccessException x) {
						doUnassign = false;
					}
				}

				boolean doAssign;
				try {
					doAssign = issue.getAssignee() != null;
					if (doAssign && oldPersistentIssue != null)
						doAssign = !Util.equals(issue.getAssignee(), oldPersistentIssue.getAssignee());
				} catch (JDODetachedFieldAccessException x) {
					doAssign = false;
				}

				String jbpmTransitionName = null;
				if (doAssign)
					jbpmTransitionName = JbpmConstants.TRANSITION_NAME_ASSIGN;

				if (doUnassign)
					jbpmTransitionName = JbpmConstants.TRANSITION_NAME_UNASSIGN;

				if (jbpmTransitionName != null) {
					// performing a transition might cause the fetch-plan to be modified => backup + restore
					FetchPlanBackup fetchPlanBackup = NLJDOHelper.backupFetchPlan(pm.getFetchPlan());
					JbpmContext jbpmContext = JbpmLookup.getJbpmConfiguration().createJbpmContext();
					try {
						ProcessInstance processInstance = jbpmContext.getProcessInstanceForUpdate(issue.getIssueLocal().getJbpmProcessInstanceId());
						if (processInstance.getRootToken().getNode().hasLeavingTransition(jbpmTransitionName))
							processInstance.signal(jbpmTransitionName);
					} finally {
						jbpmContext.close();
					}
					NLJDOHelper.restoreFetchPlan(pm.getFetchPlan(), fetchPlanBackup);
				}

				pIssue = pm.makePersistent(issue);
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

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */	
	public IssueHistory storeIssueHistory(IssueHistory issueHistory, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, issueHistory, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */	
	public IssueType storeIssueType(IssueType issueType, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, issueType, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */	
	public IssuePriority storeIssuePriority(IssuePriority issuePriority, boolean get, String[] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, issuePriority, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */	
	public IssueSeverityType storeIssueSeverityType(IssueSeverityType issueSeverityType, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try{
			return NLJDOHelper.storeJDO(pm, issueSeverityType, get, fetchGroups, maxFetchDepth);
		}//try
		finally{
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */	
	public IssueResolution storeIssueResolution(IssueResolution issueResolution, boolean get, String[] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, issueResolution, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */	
	public IssueComment storeIssueComment(IssueComment issueComment, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, issueComment, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */	
	public void deleteIssue(IssueID issueID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getExtent(Issue.class, true);
			Issue issue = (Issue) pm.getObjectById(issueID);

			//FIXME: We can not remove states righnow. Don't know why??????

			pm.getExtent(State.class, true);
			for (State state : issue.getStates()) {
				pm.deletePersistent(state);		
			}
			pm.flush();

			pm.getExtent(IssueLocal.class, true);
			pm.deletePersistent(issue.getStatableLocal());
			pm.flush();

			pm.getExtent(Issue.class, true);
			pm.deletePersistent(issue);
			pm.flush();
		}//try
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Issue signalIssue(IssueID issueID, String jbpmTransitionName, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
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

	/**
	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialise method.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise() throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			// The complete method is executed in *one* transaction. So if one thing fails, all fail.
			// => We check once at the beginning, if this module has already been initialised.
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireIssueTrackingEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of " + JFireIssueTrackingEAR.MODULE_NAME + " started...");

			pm.makePersistent(new ModuleMetaData(
					JFireIssueTrackingEAR.MODULE_NAME, "0.9.5.0.0.beta", "0.9.5.0.0.beta")
			);

//			// check, whether the datastore is already initialized
//			pm.getExtent(IssueStatus.class);
//			try {
//			pm.getObjectById(IssueStatusID.create("N"), true);
//			return; // already initialized
//			} catch (JDOObjectNotFoundException x) {
//			// datastore not yet initialized
//			}

//			// Create the statuses
//			IssueStatus issueStatus;

//			issueStatus = new IssueStatus(IssueStatus.ISSUE_STATUS_NEW);
//			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "New");
//			pm.makePersistent(issueStatus);

//			issueStatus = new IssueStatus(IssueStatus.ISSUE_STATUS_FEEDBACK);
//			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Feedback");
//			pm.makePersistent(issueStatus);

//			issueStatus = new IssueStatus(IssueStatus.ISSUE_STATUS_ACKNOWLEDGED);
//			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Acknowledged");
//			pm.makePersistent(issueStatus);

//			issueStatus = new IssueStatus(IssueStatus.ISSUE_STATUS_CONFIRMED);
//			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Confirmed");
//			pm.makePersistent(issueStatus);

//			issueStatus = new IssueStatus(IssueStatus.ISSUE_STATUS_RESOLVED);
//			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Resolved");
//			pm.makePersistent(issueStatus);

//			issueStatus = new IssueStatus(IssueStatus.ISSUE_STATUS_CLOSE);
//			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Close");
//			pm.makePersistent(issueStatus);

			IssueType issueType = new IssueType(getOrganisationID(), "Default");
			issueType.getName().setText(Locale.ENGLISH.getLanguage(), "Default");
			issueType = pm.makePersistent(issueType);

			// Create the statuses
			IssueSeverityType issueSeverityType;

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_MINOR);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Minor");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_MAJOR);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Major");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_CRASH);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Crash");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_BLOCK);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Block");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_FEATURE);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Feature");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TRIVIAL);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Trivial");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TEXT);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Text");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_TWEAK);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Tweak");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);
			////////////////////////////////////////////////////////
			// Create the priorities
			// check, whether the datastore is already initialized
			IssuePriority issuePriority;

			issuePriority = new IssuePriority(getOrganisationID(), "None");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "None");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);

			issuePriority = new IssuePriority(getOrganisationID(), "Low");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Low");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);

			issuePriority = new IssuePriority(getOrganisationID(), "Normal");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Normal");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);

			issuePriority = new IssuePriority(getOrganisationID(), "High");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "High");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);

			issuePriority = new IssuePriority(getOrganisationID(), "Urgent");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Urgent");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);

			issuePriority = new IssuePriority(getOrganisationID(), "Immediate");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Immediate");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);

			// Create the resolutions
			IssueResolution issueResolution;

			issueResolution = new IssueResolution(getOrganisationID(), IssueResolution.ISSUE_RESOLUTION_ID_NOT_ASSIGNED.issueResolutionID);
			issueResolution.getName().setText(Locale.ENGLISH.getLanguage(), "Not assigned");
			issueResolution = pm.makePersistent(issueResolution);
			issueType.getIssueResolutions().add(issueResolution);

			issueResolution = new IssueResolution(getOrganisationID(), "Open");
			issueResolution.getName().setText(Locale.ENGLISH.getLanguage(), "Open");
			issueResolution = pm.makePersistent(issueResolution);
			issueType.getIssueResolutions().add(issueResolution);

			issueResolution = new IssueResolution(getOrganisationID(), "Fixed");
			issueResolution.getName().setText(Locale.ENGLISH.getLanguage(), "Fixed");
			issueResolution = pm.makePersistent(issueResolution);
			issueType.getIssueResolutions().add(issueResolution);

			issueResolution = new IssueResolution(getOrganisationID(), "Reopened");
			issueResolution.getName().setText(Locale.ENGLISH.getLanguage(), "Reopened");
			issueResolution = pm.makePersistent(issueResolution);
			issueType.getIssueResolutions().add(issueResolution);

			issueResolution = new IssueResolution(getOrganisationID(), "NotFixable");
			issueResolution.getName().setText(Locale.ENGLISH.getLanguage(), "Not fixable");
			issueResolution = pm.makePersistent(issueResolution);
			issueType.getIssueResolutions().add(issueResolution);

			issueResolution = new IssueResolution(getOrganisationID(), "WillNotFix");
			issueResolution.getName().setText(Locale.ENGLISH.getLanguage(), "Won't fix");
			issueResolution = pm.makePersistent(issueResolution);
			issueType.getIssueResolutions().add(issueResolution);

			// create the process definitions.
			issueType.readProcessDefinition(IssueType.class.getResource("jbpm/status/"));

			// Create the issueLinkTypes
			IssueLinkType issueLinkType;

			issueLinkType = new IssueLinkType(IssueLinkType.ISSUE_LINK_TYPE_ID_RELATED);
			issueLinkType.getName().setText(Locale.ENGLISH.getLanguage(), "Related");
			issueLinkType.addLinkedObjectClass(Object.class);
			issueLinkType = pm.makePersistent(issueLinkType);

			issueLinkType = new IssueLinkTypeParentChild(IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_PARENT);
			issueLinkType.getName().setText(Locale.ENGLISH.getLanguage(), "Parent of");
			issueLinkType.addLinkedObjectClass(Issue.class);
			issueLinkType = pm.makePersistent(issueLinkType);

			issueLinkType = new IssueLinkTypeParentChild(IssueLinkTypeParentChild.ISSUE_LINK_TYPE_ID_CHILD);
			issueLinkType.getName().setText(Locale.ENGLISH.getLanguage(), "Child of");
			issueLinkType.addLinkedObjectClass(Issue.class);
			issueLinkType = pm.makePersistent(issueLinkType);

			issueLinkType = new IssueLinkTypeDuplicate(IssueLinkTypeDuplicate.ISSUE_LINK_TYPE_ID_DUPLICATE);
			issueLinkType.getName().setText(Locale.ENGLISH.getLanguage(), "Duplicate");
			issueLinkType.addLinkedObjectClass(Issue.class);
			issueLinkType = pm.makePersistent(issueLinkType);

			EditLockType issueEditLock = new EditLockType(EditLockTypeIssue.EDIT_LOCK_TYPE_ID);
			issueEditLock = pm.makePersistent(issueEditLock);
			//------------------------------------------------
		} finally {
			pm.close();
		}
	}
}
