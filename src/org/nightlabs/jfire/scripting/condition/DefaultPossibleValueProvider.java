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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.ScriptRegistry;

/**
 * The default implementation of the IPossibleValueProvider,
 * takes the result of the script with the given ScriptRegistryItemID
 * and returns the extent of all instances of the same class as possible
 * values, from the datastore.
 *  
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class DefaultPossibleValueProvider 
extends AbstractPossibleValueProvider 
{
	private static final Logger logger = Logger.getLogger(DefaultPossibleValueProvider.class);
	
//	public DefaultPossibleValueProvider(ScriptRegistryItemID scriptID,
//			PersistenceManager pm) 
//	{
//		super(scriptID);
//		this.pm = pm;
//	}
//	private PersistenceManager pm;
	
//	public DefaultPossibleValueProvider(ScriptRegistryItemID scriptID) {
//		super(scriptID);
//	}
	
	private static final List<Object> EMPTY_LIST = new ArrayList<Object>(0);
	public List<Object> getPossibleValues() 
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		ScriptRegistry scriptRegistry = ScriptRegistry.getScriptRegistry(pm);
		Script script = scriptRegistry.getScript(getScriptRegistryItemID().scriptRegistryItemType, 
				getScriptRegistryItemID().scriptRegistryItemID);
		try {
			Class resultClass = script.getResultClass();
			if (PersistenceCapable.class.isAssignableFrom(resultClass)) {
				Extent extent = pm.getExtent(resultClass);
				List<Object> possibleValues = new LinkedList<Object>();
				for (Iterator it = extent.iterator(); it.hasNext(); ) {
					possibleValues.add(it.next());
				}
				if (logger.isDebugEnabled())
					logger.debug(possibleValues.size()+" possible values for class "+resultClass);
				return possibleValues;					
			} else {
				logger.warn("resultClass "+script.getResultClassName()+" of script scriptRegistryItemID "+getScriptRegistryItemID()+" is no instance of PersistenceCapable!");
			}			
		} catch (ClassNotFoundException e) {
			logger.error("resultClass "+script.getResultClassName()+" of script with ScriptRegistryItemID "+getScriptRegistryItemID()+" was not found!", e);
		}
		return EMPTY_LIST;
	}
	
//	public List<Object> getPossibleValues() 
//	{
//		ScriptRegistry scriptRegistry = ScriptRegistry.getScriptRegistry(pm);
//		Script script = scriptRegistry.getScript(getScriptRegistryItemID().scriptRegistryItemType, 
//				getScriptRegistryItemID().scriptRegistryItemID);
//		List scripts = new ArrayList<Script>(1);
//		scripts.add(script);
//		Map<String, Object> parameterValues = new HashMap<String, Object>();		
//		try {
//			Map<ScriptRegistryItemID, Object> results = scriptRegistry.execute(scripts, parameterValues);
//			if (results != null) {
//				Object result = results.get(getScriptRegistryItemID());
//				if (result instanceof PersistenceCapable) {
//					Extent extent = pm.getExtent(result.getClass());
//					List<Object> possibleValues = new LinkedList<Object>();
//					for (Iterator it = extent.iterator(); it.hasNext(); ) {
//						possibleValues.add(it.next());
//					}
//					if (logger.isDebugEnabled())
//						logger.debug(possibleValues.size()+" possible values for class "+result.getClass());
//					return possibleValues;					
//				} else {
//					logger.warn("resultClass of script scriptRegistryItemID "+getScriptRegistryItemID()+" is no instance of PersistenceCapable!");
//				}
//			}
//		} catch (Exception e) {
//			logger.error("getPossibleValues() produced the following error "+e);			
//		}
//		return EMPTY_LIST;
//	}

}
