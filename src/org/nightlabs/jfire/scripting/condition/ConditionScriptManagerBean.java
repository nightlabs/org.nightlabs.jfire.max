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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.ScriptRegistry;
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
			PossibleValueProvider valueProvider = PossibleValueProvider.getPossibleValueProvider(pm, scriptRegistryItemID);
			Collection possibleValues = valueProvider.getPossibleValues();
			ILabelProvider valueLabelProvider = valueProvider.getLabelProvider();
			
			ScriptConditioner sc = new ScriptConditioner(scriptRegistryItemID, variableName, 
					compareOperators, possibleValues, valueLabelProvider);
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
	
}
