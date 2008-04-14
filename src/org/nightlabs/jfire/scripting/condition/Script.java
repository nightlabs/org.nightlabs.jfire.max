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

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.scripting.IScript;
import org.nightlabs.jfire.scripting.IScriptParameterSet;
import org.nightlabs.jfire.scripting.ScriptParameterSet;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;


/**
 * Implementation of the {@link IScript} interface which is NOT a JDO Object.
 * This Implementation is e.g. used for so called visible scripts
 * in the project JFireScriptingEditor2D.
 *  
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class Script
implements IScript
{
	private static final long serialVersionUID = 1L;
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
	
	private boolean simpleScript = true;
	/**
	 * Return the simpleScript.
	 * @return the simpleScript
	 */
	public boolean isSimpleScript() {
		return simpleScript;
	}
	/**
	 * Sets the simpleScript.
	 * @param simpleScript the simpleScript to set
	 */
	public void setSimpleScript(boolean simpleScript) {
		this.simpleScript = simpleScript;
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
	 * the script language dependent script
	 */
	private String text;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * a map which maps variable names of the scriptText to {@link ScriptRegistryItemID}
	 * key: variable name of the scriptText
	 * value: the corresponding {@link ScriptRegistryItemID}
	 */
	private Map<String, ScriptRegistryItemID> variableName2ScriptID = new HashMap<String, ScriptRegistryItemID>();
	public Map<String, ScriptRegistryItemID> getVariableName2ScriptID() {
		return variableName2ScriptID;
	}
	public void setVariableName2ScriptID(Map<String, ScriptRegistryItemID> variableName2ScriptID) {
		this.variableName2ScriptID = variableName2ScriptID;
	}

	private transient Map<ScriptRegistryItemID, String> scriptID2variableName = null;
	public Map<ScriptRegistryItemID, String> getScriptID2VariableName() {
		if (scriptID2variableName == null) {
			HashMap<ScriptRegistryItemID, String> m = new HashMap<ScriptRegistryItemID, String>();
			for (Map.Entry<String, ScriptRegistryItemID> me : variableName2ScriptID.entrySet())
				m.put(me.getValue(), me.getKey());

			scriptID2variableName = m;
		}
		return scriptID2variableName;
	}

	/**
	 * a map which maps the variable names of the scriptText to the values
	 * key: variable name of the scriptText
	 * value: the value of the script with the corresponding ScriptRegistryItemID, which can be determined from
	 * {@link Script#getVariableName2ScriptID()}
	 */
	private transient Map<String, Object> variableName2Value = null;
	public Map<String, Object> getVariableName2Value()
	{
		if (variableName2Value == null) {
			variableName2Value = new HashMap<String, Object>();
		}
		return variableName2Value;
	}
	public void setVariableName2Value(Map<String, Object> variableName2Value) {
		this.variableName2Value = variableName2Value;
	}

	private IScriptParameterSet parameterSet = null;
	public IScriptParameterSet getParameterSet() 
	{
		if (parameterSet == null) {
			parameterSet = new org.nightlabs.jfire.scripting.ScriptParameterSet(
					Organisation.DEV_ORGANISATION_ID, IDGenerator.nextID(ScriptParameterSet.class));
		}
		return parameterSet;
	}
		
	//***************************** Compatibility Reasons for IScript **************************

	private transient String[] fetchGroups = null;
	public String[] getFetchGroups() {
		return fetchGroups;
	}
	
	private transient int maxFetchDepth = 0;
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
