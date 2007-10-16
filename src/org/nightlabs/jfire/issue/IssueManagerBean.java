package org.nightlabs.jfire.issue;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.issue.id.IssueID;

/**
 * @author Chairat Kongarayawetchakun - chairatk[AT]nightlabs[DOT]de
 * 
 * @ejb.bean name="jfire/ejb/JFireIssueTracking/IssueManager"	
 *           jndi-name="jfire/ejb/JFireIssueTracking/IssueManager"
 *           type="Stateless" 
 *           transaction-type="Container"
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
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public Issue storeIssue(Issue issue, boolean get, String[] fetchGroups, int maxFetchDepth){
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
	
//	/**
//	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialise method.
//	 *
//	 * @ejb.interface-method
//	 * @ejb.transaction type = "Required"
//	 * @ejb.permission role-name="_System_"
//	 */
//	public void initialise() throws IOException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getExtent(ModeOfDelivery.class);
//			try {
//				pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_ID_MANUAL);
//
//				// it already exists, hence initialization is already done
//				return;
//			} catch (JDOObjectNotFoundException x) {
//				// not yet initialized
//			}
//
//
//			pm.makePersistent(new EditLockTypeDeliveryNote(EditLockTypeDeliveryNote.EDIT_LOCK_TYPE_ID));
//		} finally {
//			pm.close();
//		}
//	}
}
