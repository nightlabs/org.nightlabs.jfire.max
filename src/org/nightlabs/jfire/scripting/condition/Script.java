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

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.jfire.scripting.IScript;
import org.nightlabs.jfire.scripting.IScriptParameterSet;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;


/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class Script 
implements IScript
{
	public Script(String language, String text, Map<String, ScriptRegistryItemID> variableName2ScriptID) 
	{
		if (language == null)
			throw new IllegalArgumentException("Param language must not be null");
		
		if (text == null)
			throw new IllegalArgumentException("Param text must not be null");

		if (variableName2ScriptID == null)
			throw new IllegalArgumentException("Param variableName2ScriptID must not be null");
		
		this.language = language;
		this.text = text;
		this.variableName2ScriptID = variableName2ScriptID;
	}
	
	/**
	 * the name of the script language
	 */
	private String language;
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * the script language dependend script
	 */
	private String text;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * a map which maps variablenames of the scriptText to {@link ScriptRegistryItemID}
	 * key: variablename of the scriptText
	 * value: the corresponding {@link ScriptRegistryItemID}
	 */
	private Map<String, ScriptRegistryItemID> variableName2ScriptID = new HashMap<String, ScriptRegistryItemID>();
	public Map<String, ScriptRegistryItemID> getVariableName2ScriptID() {
		return variableName2ScriptID;		
	}	
	public void setVariableName2ScriptID(Map<String, ScriptRegistryItemID> variableName2ScriptID) {
		this.variableName2ScriptID = variableName2ScriptID;
	}
	 
	/**
	 * a map which maps the variablenames of the scriptText to the values
	 * key: variablename of the scriptText
	 * value: the value of the script with the corresponding ScriptRegistryItemID, which can be determined from
	 * {@link Script#getVariableName2ScriptID()}
	 */
	private transient Map<String, Object> variableName2Value = new HashMap<String, Object>();
	public Map<String, Object> getVariableName2Value() {
		return variableName2Value;
	}
	public void setVariableName2Value(Map<String, Object> variableName2Value) {
		this.variableName2Value = variableName2Value;
	}
	
	//***************************** Compatibilty Reasons for IScript **************************
	private IScriptParameterSet parameterSet = new ConditionScriptParameterSet(); 
	public IScriptParameterSet getParameterSet() {
		return parameterSet; 
	}	
	
	private String[] fetchGroups = null; 
	public String[] getFetchGroups() {
		return fetchGroups;
	}
	
	private int maxFetchDepth = 0;
	public int getMaxFetchDepth() {
		return maxFetchDepth;
	}
			
	public Class getResultClass()
	throws ClassNotFoundException
	{
		return Boolean.class;
	}
	
	public boolean isNeedsDetach() {
		return false;
	}
	
	public String getResultClassName() {
		return Boolean.class.getName();
	}

}
