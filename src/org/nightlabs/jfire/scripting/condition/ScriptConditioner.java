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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ScriptConditioner 
implements Serializable
{
	private static final Logger logger = Logger.getLogger(ScriptConditioner.class);
	
	public ScriptConditioner(
			ScriptRegistryItemID scriptID,
			org.nightlabs.jfire.scripting.Script script,
			String variableName, 
			List<CompareOperator> compareOperators, 
			Collection<Object> possibleValues,  
			String valueLabelProviderClassName)			
	{
		if (scriptID == null)
			throw new IllegalArgumentException("Param scriptID must NOT be null!");
		
		if (variableName == null)
			throw new IllegalArgumentException("Param variableName must NOT be null!");

		if (compareOperators == null)
			throw new IllegalArgumentException("Param compareOperators must NOT be null!");

		if (compareOperators.isEmpty())
			throw new IllegalArgumentException("Param compareOperators must NOT be empty!");

		if (script == null)
			throw new IllegalArgumentException("Param script must NOT be null!");
		
		this.scriptRegistryItemID = scriptID;
		this.variableName = variableName;
		this.possibleValues = possibleValues;
		this.compareOperators = compareOperators;
		this.labelProviderClassName = valueLabelProviderClassName;
		this.script = script;
	}
	
	private ScriptRegistryItemID scriptRegistryItemID;
	public ScriptRegistryItemID getScriptRegistryItemID() {
		return scriptRegistryItemID;
	}
	public void setScriptRegistryItemID(ScriptRegistryItemID scriptRegistryItemID) {
		this.scriptRegistryItemID = scriptRegistryItemID;
	}
	
	private org.nightlabs.jfire.scripting.Script script;
	public org.nightlabs.jfire.scripting.Script getScript() {
		return script;
	}
	public void setScript(org.nightlabs.jfire.scripting.Script script) {
		this.script = script;
	}
	
	// TODO: Must come from client
	private String variableName;
	public String getVariableName() {
		return variableName;
	}
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	
	private Collection<Object> possibleValues;
	public Collection<Object> getPossibleValues() {
		return possibleValues;
	}
	public void setPossibleValues(Collection<Object> possibleValues) {
		this.possibleValues = possibleValues;
	}
		
	private List<CompareOperator> compareOperators;
	public List<CompareOperator> getCompareOperators() {
		return compareOperators;
	}
	public void setCompareOperators(List<CompareOperator> compareOperators) {
		this.compareOperators = compareOperators;
	}
	
	private transient ILabelProvider valueLabelProvider;
	public ILabelProvider getValueLabelProvider() 
	{
		if (valueLabelProvider == null && labelProviderClassName != null) {
			try {
				Class labelProviderClass = Class.forName(labelProviderClassName);
				Object lp = labelProviderClass.newInstance();
				if (lp instanceof ILabelProvider) {
					valueLabelProvider = (ILabelProvider) lp;
				}				
			} catch (Exception e) {
				logger.warn("There occured an error while trying to create an instance for the class "+
						labelProviderClassName, e);
				valueLabelProvider = new LabelProvider();
			}			
		}
		return valueLabelProvider;
	}
	
	private String labelProviderClassName;
	public void setValueLabelProviderClassName(String labelProviderClassName) {
		this.labelProviderClassName = labelProviderClassName;
	}
	
//	private boolean possibleValuesAreObjectIDs = false;
//	public boolean isPossibleValuesAreObjectIDs() {
//		return possibleValuesAreObjectIDs;
//	}
//	public void setPossibleValuesAreObjectIDs(boolean possibleValuesAreObjectIDs) {
//		this.possibleValuesAreObjectIDs = possibleValuesAreObjectIDs;
//	}
	
}
