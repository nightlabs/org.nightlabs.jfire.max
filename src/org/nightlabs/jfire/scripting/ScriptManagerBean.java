/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.scripting;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.scripting.id.ScriptParameterSetID;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @ejb.bean name="jfire/ejb/JFireScripting/ScriptManager"	
 *					 jndi-name="jfire/ejb/JFireScripting/ScriptManager"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class ScriptManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	public static final Logger LOGGER = Logger.getLogger(ScriptManagerBean.class);

	/**
	 * @see com.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see com.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}
	
	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"	
	 */
	public void ejbCreate() throws CreateException
	{
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }
	
	
	/**
	 * This method is called by the datastore initialization mechanism.
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="JFireReport-admin"
	 * @ejb.transaction type = "Required"
	 */
	public void initialize() 
	throws ModuleException 
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireScriptingEAR.MODULE_NAME);
			if (moduleMetaData == null) {
			
				LOGGER.info("Initialization of JFireReporting started ...");
	
				
				// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
				moduleMetaData = new ModuleMetaData(
						JFireScriptingEAR.MODULE_NAME, "1.0.0-0-beta", "1.0.0-0-beta");
				pm.makePersistent(moduleMetaData);
				LOGGER.info("Persisted ModuleMetaData for JFireReporting with version 1.0.0-0-beta");

			}
			
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public void test()
	throws ModuleException
	{
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ScriptRegistryItem getScriptRegistryItem (
			ScriptRegistryItemID scriptRegistryItemID,
			String[] fetchGroups
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ScriptRegistryItem reportRegistryItem = (ScriptRegistryItem)pm.getObjectById(scriptRegistryItemID);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			ScriptRegistryItem result = (ScriptRegistryItem) pm.detachCopy(reportRegistryItem);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public List<ScriptRegistryItem> getScriptRegistryItems (
			List<ScriptRegistryItemID> scriptRegistryItemIDs,
			String[] fetchGroups
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			List<ScriptRegistryItem> result = new ArrayList<ScriptRegistryItem>();
			for (ScriptRegistryItemID itemID : scriptRegistryItemIDs) {
				ScriptRegistryItem item = (ScriptRegistryItem)pm.getObjectById(itemID);
				result.add((ScriptRegistryItem)pm.detachCopy(item));
			}

			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection getTopLevelScriptRegistryItems (
			String organisationID,
			String[] fetchGroups
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			Collection topLevelItems = ScriptRegistryItem.getTopScriptRegistryItems(pm, organisationID);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			Collection result = (Collection) pm.detachCopyAll(topLevelItems);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection<ScriptRegistryItemCarrier> getTopLevelScriptRegistryItemCarriers (String organisationID)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			Collection topLevelItems = ScriptRegistryItem.getTopScriptRegistryItems(pm, organisationID);
			Collection<ScriptRegistryItemCarrier> result = new HashSet<ScriptRegistryItemCarrier>();
			for (Iterator iter = topLevelItems.iterator(); iter.hasNext();) {
				ScriptRegistryItem item = (ScriptRegistryItem) iter.next();
				result.add(new ScriptRegistryItemCarrier(null, item, true));
			}
			return result;
		} finally {
			pm.close();
		}
	}
	
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public ScriptRegistryItem storeRegistryItem (
			ScriptRegistryItem reportRegistryItem,
			boolean get,
			String[] fetchGroups
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			return (ScriptRegistryItem)NLJDOHelper.storeJDO(pm, reportRegistryItem, get, fetchGroups);
		} finally {
			pm.close();
		}
	}

	
	
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection<ScriptParameterSet> getScriptParameterSets(
			String organisationID, 
			String[] fetchGroups
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			Collection paramSets = ScriptParameterSet.getParameterSetsByOrganisation(pm, organisationID);
			Collection pSets = pm.detachCopyAll(paramSets);
			Collection<ScriptParameterSet> result = new HashSet<ScriptParameterSet>();
			for (Iterator iter = pSets.iterator(); iter.hasNext();) {
				ScriptParameterSet paramSet = (ScriptParameterSet) iter.next();
				result.add(paramSet);
			}
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ScriptParameterSet getScriptParameterSet(
			ScriptParameterSetID scriptParameterSetID, 
			String[] fetchGroups
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			ScriptParameterSet parameterSet = (ScriptParameterSet)pm.getObjectById(scriptParameterSetID);
			return (ScriptParameterSet)pm.detachCopy(parameterSet);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public ScriptParameterSet storeParameterSet (
			ScriptParameterSet scriptParameterSet,
			boolean get,
			String[] fetchGroups
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			return (ScriptParameterSet)NLJDOHelper.storeJDO(pm, scriptParameterSet, get, fetchGroups);
		} finally {
			pm.close();
		}
	}
		
}
