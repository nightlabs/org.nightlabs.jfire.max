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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
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
		
	protected ScriptConditioner getScriptConditioner(PersistenceManager pm, 
			ScriptRegistryItemID scriptRegistryItemID, 
			Map<String, Object> parameterValues, int valueLimit)
	{		
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
		
		String variableName = scriptRegistryItemID.scriptRegistryItemID;
//		String variableName = script.getName().getText();
		Collection possibleValues = Collections.EMPTY_LIST;
		String valueLabelProviderClassName = LabelProvider.class.getName();			
		PossibleValueProviderID valueProviderID = PossibleValueProviderID.create(
				scriptRegistryItemID.organisationID, 
				scriptRegistryItemID.scriptRegistryItemType, 
				scriptRegistryItemID.scriptRegistryItemID);
		try {
			PossibleValueProvider valueProvider = (PossibleValueProvider) pm.getObjectById(valueProviderID);
			pm.getFetchPlan().setGroup(PossibleValueProvider.FETCH_GROUP_THIS_POSSIBLE_VALUE_PROVIDER);				
			possibleValues = valueProvider.getPossibleValues(parameterValues, valueLimit);
			valueLabelProviderClassName = valueProvider.getLabelProviderClassName();
		} 
		catch (JDOObjectNotFoundException e) {
			// If no valueprovider is registered use the default
			PossibleValueProvider provider = PossibleValueProvider.getDefaultPossibleValueProvider(getPersistenceManager(), script); 
			possibleValues = provider.getPossibleValues(parameterValues, valueLimit);
			logger.info("No possible values found for ScriptRegistryItemID "+scriptRegistryItemID+", use DefaultPossibleValueProvider!");				
		}
		
		ScriptConditioner sc = new ScriptConditioner(scriptRegistryItemID, variableName, 
				compareOperators, possibleValues, valueLabelProviderClassName);		
//		sc.setPossibleValuesAreObjectIDs(script.isNeedsDetach());
		
//		if (logger.isDebugEnabled()) {
			logger.info("scriptRegistryItemID = "+scriptRegistryItemID);
			logger.info("variableName = "+variableName);
			logger.info("scriptResultClass = "+script.getResultClassName());
			logger.info("compareOperators.size() = "+compareOperators.size());
			logger.info("possibleValues.size() = "+possibleValues.size());	
			logger.info("valueLabelProviderClassName = "+valueLabelProviderClassName);				
//		}
		return sc;
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ScriptConditioner getScriptConditioner(ScriptRegistryItemID scriptRegistryItemID, 
			Map<String, Object> parameterValues, int valueLimit)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			return getScriptConditioner(pm, scriptRegistryItemID, parameterValues, valueLimit);
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
	public Map<ScriptRegistryItemID, ScriptConditioner> getScriptConditioner(
			Map<ScriptRegistryItemID, Map<String, Object>> scriptID2Paramters, int valueLimit)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		Map<ScriptRegistryItemID, ScriptConditioner> scriptID2ScriptConditioner = 
			new HashMap<ScriptRegistryItemID, ScriptConditioner>(scriptID2Paramters.size());		
		try {
			for (Map.Entry<ScriptRegistryItemID, Map<String, Object>> entry : scriptID2Paramters.entrySet()) {
				ScriptConditioner sc = getScriptConditioner(pm, entry.getKey(), entry.getValue(), valueLimit);
				scriptID2ScriptConditioner.put(entry.getKey(), sc);			
			}
			return scriptID2ScriptConditioner;
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
	public Set<ScriptRegistryItemID> getConditionContextScriptIDs(String organisationID, 
			String conditionContextProviderID)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ConditionContextProviderID providerID = ConditionContextProviderID.create(organisationID, conditionContextProviderID);
			List providers = NLJDOHelper.getDetachedObjectList(
					pm, new Object[] {providerID}, null, 
					new String[] {ConditionContextProvider.FETCH_GROUP_SCRIPT_REGISTRY_ITEM_IDS}, 
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			
			ConditionContextProvider provider = (ConditionContextProvider) providers.iterator().next();
			Set<ScriptRegistryItemID> scriptIDs = provider.getScriptRegistryItemIDs();			
			return scriptIDs;
		} finally {
			pm.close();
		}		
	}
	
}
