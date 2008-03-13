package org.nightlabs.jfire.issue;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jdo.query.JDOQueryCollectionDecorator;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.editlock.EditLockType;
import org.nightlabs.jfire.issue.config.StoredIssueQuery;
import org.nightlabs.jfire.issue.history.IssueHistory;
import org.nightlabs.jfire.issue.id.IssueCommentID;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.id.IssueLinkTypeID;
import org.nightlabs.jfire.issue.id.IssueLocalID;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueResolutionID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.jfire.issue.id.StoredIssueQueryID;
import org.nightlabs.jfire.jbpm.JbpmLookup;
import org.nightlabs.jfire.jbpm.graph.def.State;

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
implements SessionBean{

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
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Issue storeIssue(Issue issue, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			boolean isNewIssue = !JDOHelper.isDetached(issue);
			
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			if (issue.getCreateTimestamp() != null) {
				issue.setUpdateTimestamp(new Date());
			}
			
			Issue pIssue = pm.makePersistent(issue);

			if (isNewIssue) {
				pm.flush();
				// create the ProcessInstance for new Issues
				// TODO: WORKAROUND: Calling createProcessInstanceForIssue on pIssue.getIssueType() says that this IssueType is not persistent ?!?
				IssueType type = (IssueType) pm.getObjectById(JDOHelper.getObjectId(pIssue.getIssueType()));
				if (type == null) {
					throw new IllegalStateException("Could not create ProcessInstance for new Issue as its type is null");
				}
				type.createProcessInstanceForIssue(pIssue);
			}
			
			if (!get)
				return null;

			return pm.detachCopy(pIssue);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
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
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getIssuePriorities(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(IssuePriority.class);
			return pm.detachCopyAll((Collection)q.execute());
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
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
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
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

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getIssueTypes(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(IssueType.class);
			return pm.detachCopyAll((Collection)q.execute());
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getIssueSeverityTypes(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(IssueSeverityType.class);
			return pm.detachCopyAll((Collection)q.execute());
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getIssueComments(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(IssueComment.class);
			return pm.detachCopyAll((Collection)q.execute());
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getStoredIssueQuery(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(StoredIssueQuery.class);
			return pm.detachCopyAll((Collection)q.execute());
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<IssueID> getIssueIDs(QueryCollection<Issue, ? extends AbstractJDOQuery<? extends Issue>> queries)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);

			JDOQueryCollectionDecorator<Issue, ? extends AbstractSearchQuery<? extends Issue>> decoratedCollection;
			if (queries instanceof JDOQueryCollectionDecorator)
			{
				decoratedCollection = (JDOQueryCollectionDecorator<Issue, ? extends AbstractSearchQuery<? extends Issue>>) queries;
			}
			else
			{
				decoratedCollection = new JDOQueryCollectionDecorator<Issue, AbstractSearchQuery<? extends Issue>>(queries);
			}
			
			decoratedCollection.setPersistenceManager(pm);
			Collection<? extends Issue> issues = decoratedCollection.executeQueries();

			return NLJDOHelper.getObjectIDSet(issues);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
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
	 * @throws ModuleException
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getIssueLinkTypesByLinkedObjectClass(Class<? extends Object> linkedObjectClass)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try
		{
			Query query = pm.newQuery(IssueLinkType.class);
			StringBuffer filter = new StringBuffer();
			
			if (linkedObjectClass != null) {
				filter.append("( this.linkableObjectClassNames.contains(varName) && (varName.matches(\"" + linkedObjectClass.getName() + "\") ||  varName.matches(\"" + Object.class.getName() + "\")))");
				query.declareVariables(String.class.getName() + " varName");
			}
			
			
			logger.info(filter.toString());
			query.setFilter(filter.toString());
			
			Collection issueLinkTypes = (Collection)query.execute(linkedObjectClass);
			Collection result = new HashSet();
			
			for(Object issueLinkType : issueLinkTypes)
				result.add(JDOHelper.getObjectId(issueLinkType));

			return result;
		}
		finally {
			pm.close();
		}
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
	public StoredIssueQuery storeStoredIssueQuery(StoredIssueQuery storedIssueQuery, boolean get, String[] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, storedIssueQuery, get, fetchGroups, maxFetchDepth);
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
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */	
	public void deleteStoredIssueQuery(StoredIssueQueryID storedIssueQueryID)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getExtent(StoredIssueQuery.class, true);
			StoredIssueQuery storedIssueQuery = (StoredIssueQuery) pm.getObjectById(storedIssueQueryID);
			pm.deletePersistent(storedIssueQuery);
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
	public IssueHistory createIssueHistory(IssueHistory issueHistory, boolean get, String[] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		try{
			return NLJDOHelper.storeJDO(pm, issueHistory, get, fetchGroups, maxFetchDepth);
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
				pm.getFetchPlan().setGroups(fetchGroups);
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
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
					JFireIssueTrackingEAR.MODULE_NAME, "0.9.3.0.0.beta", "0.9.3.0.0.beta")
			);

//			// check, whether the datastore is already initialized
//			pm.getExtent(IssueStatus.class);
//			try {
//				pm.getObjectById(IssueStatusID.create("N"), true);
//				return; // already initialized
//			} catch (JDOObjectNotFoundException x) {
//				// datastore not yet initialized
//			}
//
//			// Create the statuses
//			IssueStatus issueStatus;
//
//			issueStatus = new IssueStatus(IssueStatus.ISSUE_STATUS_NEW);
//			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "New");
//			pm.makePersistent(issueStatus);
//
//			issueStatus = new IssueStatus(IssueStatus.ISSUE_STATUS_FEEDBACK);
//			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Feedback");
//			pm.makePersistent(issueStatus);
//			
//			issueStatus = new IssueStatus(IssueStatus.ISSUE_STATUS_ACKNOWLEDGED);
//			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Acknowledged");
//			pm.makePersistent(issueStatus);
//			
//			issueStatus = new IssueStatus(IssueStatus.ISSUE_STATUS_CONFIRMED);
//			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Confirmed");
//			pm.makePersistent(issueStatus);
//			
//			issueStatus = new IssueStatus(IssueStatus.ISSUE_STATUS_RESOLVED);
//			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Resolved");
//			pm.makePersistent(issueStatus);
//			
//			issueStatus = new IssueStatus(IssueStatus.ISSUE_STATUS_CLOSE);
//			issueStatus.getIssueStatusText().setText(Locale.ENGLISH.getLanguage(), "Close");
//			pm.makePersistent(issueStatus);

			IssueType issueType = new IssueType(getOrganisationID(), "Default");
			issueType.getName().setText(Locale.ENGLISH.getLanguage(), "Default");
			issueType = pm.makePersistent(issueType);

			IssueType issueType2 = new IssueType(getOrganisationID(), "Customer");
			issueType2.getName().setText(Locale.ENGLISH.getLanguage(), "Customer");
			issueType2 = pm.makePersistent(issueType2);

			// Create the statuses
			IssueSeverityType issueSeverityType;

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_MINOR);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Minor");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);
			issueType2.getIssueSeverityTypes().add(issueSeverityType);

			issueSeverityType = new IssueSeverityType(getOrganisationID(), IssueSeverityType.ISSUE_SEVERITY_TYPE_MAJOR);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Major");
			issueSeverityType = pm.makePersistent(issueSeverityType);
			issueType.getIssueSeverityTypes().add(issueSeverityType);
			issueType2.getIssueSeverityTypes().add(issueSeverityType);
			
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
			issueType2.getIssuePriorities().add(issuePriority);

			issuePriority = new IssuePriority(getOrganisationID(), "Low");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Low");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);
			issueType2.getIssuePriorities().add(issuePriority);
			
			issuePriority = new IssuePriority(getOrganisationID(), "Normal");
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Normal");
			issuePriority = pm.makePersistent(issuePriority);
			issueType.getIssuePriorities().add(issuePriority);
			issueType2.getIssuePriorities().add(issuePriority);
			
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

			issueResolution = new IssueResolution(getOrganisationID(), "Fix");
			issueResolution.getName().setText(Locale.ENGLISH.getLanguage(), "Fix");
			issueResolution = pm.makePersistent(issueResolution);
			issueType.getIssueResolutions().add(issueResolution);
			issueType2.getIssueResolutions().add(issueResolution);
			
			
			// create the process definitions.
			issueType.readProcessDefinition(IssueType.class.getResource("jbpm/status/"));
			issueType2.readProcessDefinition(IssueType.class.getResource("jbpm/status/"));
	
			// Create the issueLinkTypes
			IssueLinkType issueLinkType;

			issueLinkType = new IssueLinkType(getOrganisationID(), "Related");
			issueLinkType.getName().setText(Locale.ENGLISH.getLanguage(), "Related");
			issueLinkType.getLinkableObjectClassNames().add("java.lang.Object");
			issueLinkType = pm.makePersistent(issueLinkType);

			issueLinkType = new IssueLinkType(getOrganisationID(), "Parent of");
			issueLinkType.getName().setText(Locale.ENGLISH.getLanguage(), "Parent of");
			issueLinkType.getLinkableObjectClassNames().add("org.nightlabs.jfire.issue.Issue");
			issueLinkType = pm.makePersistent(issueLinkType);
			
			issueLinkType = new IssueLinkType(getOrganisationID(), "Child of");
			issueLinkType.getName().setText(Locale.ENGLISH.getLanguage(), "Child of");
			issueLinkType.getLinkableObjectClassNames().add("org.nightlabs.jfire.issue.Issue");
			issueLinkType = pm.makePersistent(issueLinkType);
			
			issueLinkType = new IssueLinkType(getOrganisationID(), "Duplicate");
			issueLinkType.getName().setText(Locale.ENGLISH.getLanguage(), "Duplicate");
			issueLinkType.getLinkableObjectClassNames().add("org.nightlabs.jfire.issue.Issue");
			issueLinkType = pm.makePersistent(issueLinkType);
			////////////////////////////////////////////////////////
			
			EditLockType issueEditLock = new EditLockType(EditLockTypeIssue.EDIT_LOCK_TYPE_ID);
			issueEditLock = pm.makePersistent(issueEditLock);
			//------------------------------------------------
		} finally {
			pm.close();
		}
	}
}
