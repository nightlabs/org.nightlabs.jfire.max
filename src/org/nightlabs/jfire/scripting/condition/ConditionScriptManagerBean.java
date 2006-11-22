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
package org.nightlabs.jfire.scripting.condition;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.scripting.condition.id.ConditionContextProviderID;
import org.nightlabs.jfire.scripting.condition.id.PossibleValueProviderID;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;



/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * 
 * @ejb.bean name="jfire/ejb/JFireScripting/condition/ConditionScriptManager"	
 *					 jndi-name="jfire/ejb/JFireScripting/condition/ConditionScriptManager"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class ConditionScriptManagerBean 
extends BaseSessionBeanImpl 
implements SessionBean 
{
	private static final Logger logger = Logger.getLogger(ConditionScriptManagerBean.class);
	
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ScriptConditioner getScriptConditioner(ScriptRegistryItemID scriptRegistryItemID)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			String variableName = scriptRegistryItemID.scriptRegistryItemID;
			Script script = ScriptRegistry.getScriptRegistry(pm).getScript(
					scriptRegistryItemID.scriptRegistryItemType, 
					scriptRegistryItemID.scriptRegistryItemID);
			Class scriptResultClass;
			List<CompareOperator> compareOperators = DefaultCompareOperatorProvider.sharedInstance().getEqualOperators();
			try {
				scriptResultClass = script.getResultClass();
				compareOperators = DefaultCompareOperatorProvider.sharedInstance().getCompareOperator(scriptResultClass);			
			} catch (ClassNotFoundException e) {
				logger.error("script resultClass "+script.getResultClassName()+" of script with scriptRegistryItemID "+scriptRegistryItemID+" could not be found", e);
			}
			
			Collection possibleValues = Collections.EMPTY_LIST;
			ILabelProvider valueLabelProvider = new LabelProvider();			
//			PossibleValueProvider valueProvider = PossibleValueProvider.getPossibleValueProvider(pm, scriptRegistryItemID);
			PossibleValueProviderID valueProviderID = PossibleValueProviderID.create(
					scriptRegistryItemID.organisationID, 
					scriptRegistryItemID.scriptRegistryItemType, 
					scriptRegistryItemID.scriptRegistryItemID);
			try {
				PossibleValueProvider valueProvider = (PossibleValueProvider) pm.getObjectById(valueProviderID);
				pm.getFetchPlan().setGroup(PossibleValueProvider.FETCH_GROUP_THIS_POSSIBLE_VALUE_PROVIDER);
				valueProvider = (PossibleValueProvider) pm.detachCopy(valueProvider);				
				possibleValues = valueProvider.getPossibleValues();
				valueLabelProvider = valueProvider.getLabelProvider();								
			} catch (JDOObjectNotFoundException e) {
				// do nothing but use default values
			}
			
			ScriptConditioner sc = new ScriptConditioner(scriptRegistryItemID, variableName, 
					compareOperators, possibleValues, valueLabelProvider);
			
//			if (logger.isDebugEnabled()) {
				logger.info("scriptRegistryItemID = "+scriptRegistryItemID);
				logger.info("variableName = "+variableName);
				logger.info("scriptResultClass = "+script.getResultClassName());
				logger.info("compareOperators.size() = "+compareOperators.size());
				logger.info("possibleValues.size() = "+possibleValues.size());	
				logger.info("valueLabelProvider = "+valueLabelProvider);				
//			}
			return sc;
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
	public Map<ScriptRegistryItemID, ScriptConditioner> getScriptConditioner(Collection<ScriptRegistryItemID> scriptIDs)
	throws ModuleException
	{
		Map<ScriptRegistryItemID, ScriptConditioner> scriptID2ScriptConditioner = 
			new HashMap<ScriptRegistryItemID, ScriptConditioner>(scriptIDs.size());		
		for (ScriptRegistryItemID itemID : scriptIDs) {
			ScriptConditioner sc = getScriptConditioner(itemID);
			scriptID2ScriptConditioner.put(itemID, sc);
		}
		return scriptID2ScriptConditioner;
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */	
	public Map<ScriptRegistryItemID, ScriptConditioner> getScriptConditioner(String organisationID, 
			String conditionContextProviderID)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
//			ConditionContextProvider provider = ConditionContextProvider.getConditionContextProvider(
//					pm, organisationID, conditionContextProviderID);			
//			Set<ScriptRegistryItemID> scriptIDs = provider.getScriptRegistryItemIDs();
			
			ConditionContextProviderID providerID = ConditionContextProviderID.create(organisationID, conditionContextProviderID);
			List providers = NLJDOHelper.getDetachedObjectList(
					pm, new Object[] {providerID}, null, 
					new String[] {ConditionContextProvider.FETCH_GROUP_SCRIPT_REGISTRY_ITEM_IDS}, 
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			
			ConditionContextProvider provider = (ConditionContextProvider) providers.iterator().next();
			Set<ScriptRegistryItemID> scriptIDs = provider.getScriptRegistryItemIDs();
			
			logger.info("scriptIDs.size() = "+scriptIDs.size());
			int counter = 0;
			for (ScriptRegistryItemID itemID : scriptIDs) {
				logger.info("ScriptRegistryItemID " + (counter++) + " = "+itemID);
			}
			
			return getScriptConditioner(scriptIDs);
		} finally {
			pm.close();
		}		
	}
	
}
