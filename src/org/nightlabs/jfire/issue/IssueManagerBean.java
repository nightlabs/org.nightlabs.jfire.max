package org.nightlabs.jfire.issue;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.issue.history.IssueHistory;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.id.IssuePriorityID;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.issue.id.IssueTypeID;

/**
 * @author Chairat Kongarayawetchakun - chairat[AT]nightlabs[DOT]de
 * 
 * @ejb.bean name="jfire/ejb/JFireIssueTracking/IssueManager"	
 *           jndi-name="jfire/ejb/JFireIssueTracking/IssueManager"
 *           type="Stateless" 
 *
 * @ejb.util generate = "physical"
 */
public class IssueManagerBean 
extends BaseSessionBeanImpl
implements SessionBean{

	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(IssueManagerBean.class);

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
	 * Creates a new issue history. This method is only usable, if the user (principal)
	 * is an organisation, because this organisation will automatically be set
	 * as the user for the new Issue History.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public IssueHistory createIssueHistory(IssueHistory issueHistory, boolean get, String[] fetchGroups, int maxFetchDepth){
		PersistenceManager pm = getPersistenceManager();
		try {
			if (!issueHistory.getOrganisationID().equals(getOrganisationID()))
				throw new IllegalArgumentException("Given Issue was created for a different organisation, can not store to this datastore!");

			IssueHistory result = NLJDOHelper.storeJDO(pm, issueHistory, get, fetchGroups, maxFetchDepth);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Creates a new issue. This method is only usable, if the user (principal)
	 * is an organisation, because this organisation will automatically be set
	 * as the user for the new Issue.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Issue createIssueWithoutAttachedDocument(Issue issue, boolean get, String[] fetchGroups, int maxFetchDepth){
		PersistenceManager pm = getPersistenceManager();
		try {
			if (!issue.getOrganisationID().equals(getOrganisationID()))
				throw new IllegalArgumentException("Given Issue was created for a different organisation, can not store to this datastore!");

			Issue result = NLJDOHelper.storeJDO(pm, issue, get, fetchGroups, maxFetchDepth);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Creates a new issue. This method is only usable, if the user (principal)
	 * is an organisation, because this organisation will automatically be set
	 * as the user for the new Issue.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Issue createIssueWithAttachedDocument(Issue issue, ObjectID objectID, boolean get, String[] fetchGroups, int maxFetchDepth){
		PersistenceManager pm = getPersistenceManager();
		try {
			if (!issue.getOrganisationID().equals(getOrganisationID()))
				throw new IllegalArgumentException("Given Issue was created for a different organisation, can not store to this datastore!");

			Issue result = NLJDOHelper.storeJDO(pm, issue, get, fetchGroups, maxFetchDepth);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
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
	 * @ejb.transaction type="Supports"
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
	 * @ejb.transaction type="Supports"
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
	 * @ejb.transaction type = "Required"
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getIssueStatus(String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Query q = pm.newQuery(IssueStatus.class);
			return pm.detachCopyAll((Collection)q.execute());
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Collection getIssueSeverityTypesByIssueTypeID(IssueTypeID issueTypeID, String[] fetchGroups, int maxFetchDepth)
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
	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialise method.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise() throws IOException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
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

			
			// check, whether the datastore is already initialized
			
			pm.getExtent(IssueType.class);
			try {
				pm.getObjectById(IssueTypeID.create(getOrganisationID(), "Default"), true);
				return; // already initialized
			} catch (JDOObjectNotFoundException x) {
				// datastore not yet initialized
			}
			
			
			IssueType issueType = new IssueType(getOrganisationID(), "Default");
			issueType.getName().setText(Locale.ENGLISH.getLanguage(), "Default");
			pm.makePersistent(issueType);
			
			IssueType issueType2 = new IssueType(getOrganisationID(), "Customer");
			issueType2.getName().setText(Locale.ENGLISH.getLanguage(), "Customer");
			pm.makePersistent(issueType2);
			
			IssueType issueType3 = new IssueType(getOrganisationID(), "Officer");
			issueType3.getName().setText(Locale.ENGLISH.getLanguage(), "Officer");
			pm.makePersistent(issueType3);
			
			// check, whether the datastore is already initialized
			pm.getExtent(IssueSeverityType.class);
			try {
				pm.getObjectById(IssueSeverityTypeID.create(IssueSeverityType.ISSUE_SEVERITY_TYPE_BLOCK, "Default", getOrganisationID()), true);
				return; // already initialized
			} catch (JDOObjectNotFoundException x) {
				// datastore not yet initialized
			}
			
			// Create the statuses
			IssueSeverityType issueSeverityType;

			issueSeverityType = new IssueSeverityType(issueType, IssueSeverityType.ISSUE_SEVERITY_TYPE_MINOR);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Minor");
			pm.makePersistent(issueSeverityType);

			issueSeverityType = new IssueSeverityType(issueType, IssueSeverityType.ISSUE_SEVERITY_TYPE_MAJOR);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Major");
			pm.makePersistent(issueSeverityType);
			
			issueSeverityType = new IssueSeverityType(issueType, IssueSeverityType.ISSUE_SEVERITY_TYPE_CRASH);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Crash");
			pm.makePersistent(issueSeverityType);
			
			issueSeverityType = new IssueSeverityType(issueType, IssueSeverityType.ISSUE_SEVERITY_TYPE_BLOCK);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Block");
			pm.makePersistent(issueSeverityType);
			
			issueSeverityType = new IssueSeverityType(issueType, IssueSeverityType.ISSUE_SEVERITY_TYPE_FEATURE);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Feature");
			pm.makePersistent(issueSeverityType);
			
			issueSeverityType = new IssueSeverityType(issueType, IssueSeverityType.ISSUE_SEVERITY_TYPE_TRIVIAL);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Trivial");
			pm.makePersistent(issueSeverityType);
			
			issueSeverityType = new IssueSeverityType(issueType, IssueSeverityType.ISSUE_SEVERITY_TYPE_TEXT);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Text");
			pm.makePersistent(issueSeverityType);
			
			issueSeverityType = new IssueSeverityType(issueType, IssueSeverityType.ISSUE_SEVERITY_TYPE_TWEAK);
			issueSeverityType.getIssueSeverityTypeText().setText(Locale.ENGLISH.getLanguage(), "Tweak");
			pm.makePersistent(issueSeverityType);
			
			////////////////////////////////////////////////////////
			// Create the priorities
			// check, whether the datastore is already initialized
			pm.getExtent(IssuePriority.class);
			try {
				pm.getObjectById(IssuePriorityID.create(IssuePriority.ISSUE_PRIORITY_NORMAL, issueType.getIssueTypeID(), getOrganisationID()), true);
				return; // already initialized
			} catch (JDOObjectNotFoundException x) {
				// datastore not yet initialized
			}
			IssuePriority issuePriority;

			issuePriority = new IssuePriority(issueType, IssuePriority.ISSUE_PRIORITY_NONE);
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "None");
			pm.makePersistent(issuePriority);

			issuePriority = new IssuePriority(issueType, IssuePriority.ISSUE_PRIORITY_LOW);
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Low");
			pm.makePersistent(issuePriority);
			
			issuePriority = new IssuePriority(issueType, IssuePriority.ISSUE_PRIORITY_NORMAL);
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Normal");
			pm.makePersistent(issuePriority);
			
			issuePriority = new IssuePriority(issueType, IssuePriority.ISSUE_PRIORITY_HIGH);
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "High");
			pm.makePersistent(issuePriority);
			
			issuePriority = new IssuePriority(issueType, IssuePriority.ISSUE_PRIORITY_URGENT);
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Urgent");
			pm.makePersistent(issuePriority);
			
			issuePriority = new IssuePriority(issueType, IssuePriority.ISSUE_PRIORITY_IMMEDIATE);
			issuePriority.getIssuePriorityText().setText(Locale.ENGLISH.getLanguage(), "Immediate");
			pm.makePersistent(issuePriority);
		} finally {
			pm.close();
		}
	}
}
