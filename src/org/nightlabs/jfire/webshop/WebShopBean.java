/**
 * 
 */
package org.nightlabs.jfire.webshop;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.webshop.id.WebCustomerID;

/**
 * @author khaled
 * 
 * @ejb.bean name="jfire/ejb/JFireWebShopBase/WebShop"
 *           jndi-name="jfire/ejb/JFireWebShopBase/WebShop" type="Stateless"
 *           transaction-type="Container"
 * 
 * @ejb.util generate="physical"
 */
public abstract class WebShopBean
extends BaseSessionBeanImpl
implements SessionBean
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(WebShopBean.class);

	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}

	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		if (logger.isDebugEnabled())
			logger.debug(this.getClass().getName() + ".ejbCreate()");
	}
	
	/**
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		if (logger.isDebugEnabled())
			logger.debug(this.getClass().getName() + ".ejbRemove()");
	}

	public void ejbActivate() throws EJBException, RemoteException
	{
		if (logger.isDebugEnabled())
			logger.debug(this.getClass().getName() + ".ejbActivate()");
	}
	
	public void ejbPassivate() throws EJBException, RemoteException
	{
		if (logger.isDebugEnabled())
			logger.debug(this.getClass().getName() + ".ejbPassivate()");
	}
		
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */	
	public List<WebCustomer> getWebCustomers(Set<WebCustomerID> webCustomerIDs, 
			String[] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, webCustomerIDs, WebCustomer.class, 
					fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}		
	}
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */	
	public WebCustomer getWebCustomer(WebCustomerID webCustomerID, String[] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			WebCustomer webCustomer = (WebCustomer)pm.getObjectById(webCustomerID);
			return (WebCustomer) pm.detachCopy(webCustomer);
		} finally {
			pm.close();
		}	
	}
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */		
	public WebCustomer createWebCustomer(
			String webCustomerID, String password, Person person,
			boolean get, String[] fetchGroups, int maxFetchDepth)
	throws DuplicateIDException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (isWebCustomerIDExisting(webCustomerID, pm))
				throw new DuplicateIDException("webCustomerID \""+webCustomerID+"\" already exists!");

			if (get) {
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				if (fetchGroups != null)
					pm.getFetchPlan().setGroups(fetchGroups);
			}
			WebCustomer webCustomer = new WebCustomer(getOrganisationID(), webCustomerID);
			webCustomer.setPassword(password);
			LegalEntity legalEntity;

			// TODO the following lines work only locally - are we always in the core server here?!
			person = (Person) pm.makePersistent(person);
			legalEntity = Trader.getTrader(pm).setPersonToLegalEntity(person, true);
//			try {
//				TradeManagerLocal tm = TradeManagerUtil.getLocalHome().create(); // TODO are we in the core server here?!
//				legalEntity = tm.storePersonAsLegalEntity(person, false, 
//						new String[] {FetchPlan.DEFAULT, Person.FETCH_GROUP_FULL_DATA}, 
//						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
//			} catch (Exception x) {
//				throw new RuntimeException(x);
//			}
			webCustomer.setLegalEntity(legalEntity);
			webCustomer = (WebCustomer) pm.makePersistent(webCustomer);
			if (!get)
				return null;

			return (WebCustomer) pm.detachCopy(webCustomer);
		} finally {
			pm.close();
		}
	}
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */			
	public boolean isWebCustomerIDExisting(String webCustomerID, PersistenceManager pm) 
	{
		WebCustomerID id = WebCustomerID.create(getOrganisationID(), webCustomerID);
		try {
			pm.getObjectById(id);
		} catch (JDOObjectNotFoundException e) {
			return false;
		}
		return true;
	}
	/**
	 * This is to accelerate a demand by not detaching the webCustomer.
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */			
	public String getPassword(String webCustomerID) 
	{
		PersistenceManager pm = getPersistenceManager();
		WebCustomerID id = WebCustomerID.create(getOrganisationID(), webCustomerID);
		try {
			WebCustomer wbc = (WebCustomer)pm.getObjectById(id);
			return wbc.getPassword();
		}	
		catch (JDOObjectNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */			
	public boolean isWebCustomerIDExisting(String webCustomerID) 
	{
		WebCustomerID id = WebCustomerID.create(getOrganisationID(), webCustomerID);
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getObjectById(id);
		} catch (JDOObjectNotFoundException e) {
			return false;
		} finally {
			pm.close();
		}		
		return true;
	}
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */	
	public WebCustomer storeWebCustomer(WebCustomer webCustomer, boolean get, 
			String[] fetchGroups, int maxFetchDepth) 
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return (WebCustomer) NLJDOHelper.storeJDO(pm, webCustomer, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

}
