/**
 * 
 */
package org.nightlabs.jfire.webshop;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.TradeManager;
import org.nightlabs.jfire.trade.TradeManagerUtil;
import org.nightlabs.jfire.webshop.id.WebCustomerID;

/**
 * @author Khaled
 *
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
	public Collection<WebCustomer> getWebCustomers(Set<WebCustomerID> webCustomerIDs, 
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
	 * @ejb.transaction type="Required"
	 */		
	public void createWebCustomer(String webCustomerID, String password, Person person)
	throws DoublicateIDException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			if (!isWebCustomerIDExisting(webCustomerID, pm)) {
				WebCustomer webCustomer = new WebCustomer(webCustomerID, password);
				TradeManager tm = TradeManagerUtil.getHome().create();
				LegalEntity legalEntity = tm.storePersonAsLegalEntity(person, false, 
						new String[] {FetchPlan.DEFAULT, Person.FETCH_GROUP_FULL_DATA}, 
						NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				webCustomer.setLegalEntity(legalEntity);
				pm.makePersistent(webCustomer);
			}
			throw new DoublicateIDException("Id "+webCustomerID+" already exists!");			
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		finally {
			pm.close();
		}
	}
	
	protected boolean isWebCustomerIDExisting(String webCustomerID, PersistenceManager pm) 
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
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */		
	public String getNewPassword(WebCustomerID webCustomerID) 
	{
		// TODO: Implement this
		return null;
	}
}
