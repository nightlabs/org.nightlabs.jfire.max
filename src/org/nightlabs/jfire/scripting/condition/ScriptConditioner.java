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

import java.util.Collection;
import java.util.List;

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ScriptConditioner 
{
	public ScriptConditioner(ScriptRegistryItemID scriptID, String variableName, 
			List<CompareOperator> compareOperators, 
			Collection<Object> possibleValues, ILabelProvider valueLabelProvider) 
	{
		if (scriptID == null)
			throw new IllegalArgumentException("Param scriptID must NOT be null!");
		
		if (variableName == null)
			throw new IllegalArgumentException("Param variableName must NOT be null!");

		if (compareOperators == null)
			throw new IllegalArgumentException("Param compareOperators must NOT be null!");

		if (compareOperators.isEmpty())
			throw new IllegalArgumentException("Param compareOperators must NOT be empty!");

		this.scriptRegistryItemID = scriptID;
		this.variableName = variableName;
		this.possibleValues = possibleValues;
		this.compareOperators = compareOperators;
		this.valueLabelProvider = valueLabelProvider;
		if (valueLabelProvider == null)
			valueLabelProvider = new LabelProvider();
	}
	
	private ScriptRegistryItemID scriptRegistryItemID;
	public ScriptRegistryItemID getScriptRegistryItemID() {
		return scriptRegistryItemID;
	}
	public void setScriptRegistryItemID(ScriptRegistryItemID scriptRegistryItemID) {
		this.scriptRegistryItemID = scriptRegistryItemID;
	}
	
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
	
	private ILabelProvider valueLabelProvider;
	public ILabelProvider getValueLabelProvider() {
		return valueLabelProvider;
	}
	public void setValueLabelProvider(ILabelProvider valueLabelProvider) {
		this.valueLabelProvider = valueLabelProvider;
	}
	
	private List<CompareOperator> compareOperators;
	public List<CompareOperator> getCompareOperators() {
		return compareOperators;
	}
	public void setCompareOperators(List<CompareOperator> compareOperators) {
		this.compareOperators = compareOperators;
	}	
}
