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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.JDOHelper;
import javax.jdo.spi.PersistenceCapable;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ConstrainedConditionScriptParser 
{
	private static final Logger logger = Logger.getLogger(ConstrainedConditionScriptParser.class);
	
//	private static ConstrainedConditionScriptParser sharedInstance = null;
//	public static ConstrainedConditionScriptParser sharedInstance() {
//		if (sharedInstance == null) {
//			sharedInstance = new ConstrainedConditionScriptParser();
//		}
//		return sharedInstance;
//	}
//	
//	protected ConstrainedConditionScriptParser() {
//		
//	}
	
	private Collection<ScriptConditioner> scriptConditioners;
	public ConstrainedConditionScriptParser(Collection<ScriptConditioner> scriptConditioners) 
	{
		this.scriptConditioners = scriptConditioners;
		variableName2ScriptID = new HashMap<String, ScriptRegistryItemID>();
		objectIDString2Value = new HashMap<String, Object>();
		for (ScriptConditioner conditioner : scriptConditioners) {
			variableName2ScriptID.put(conditioner.getVariableName(), 
					conditioner.getScriptRegistryItemID());
			for (Object value : conditioner.getPossibleValues()) {
				if (value instanceof PersistenceCapable) {
					ObjectID objectID = (ObjectID) JDOHelper.getObjectId(value);
					objectIDString2Value.put(objectID.toString(), value);
				}
				else {
					objectIDString2Value.put(String.valueOf(value), value);
				}
			}
		}
	}
	
	private Map<String, ScriptRegistryItemID> variableName2ScriptID;
	private Map<String, Object> objectIDString2Value;
	
	// special characters for regular expressions
	private static Set<String> specialCharacters = new HashSet<String>();
	static {
//	specialCharacters.add("\\");
		specialCharacters.add("}");
		specialCharacters.add("{");
		specialCharacters.add("[");
		specialCharacters.add("]");
		specialCharacters.add("^");
		specialCharacters.add(".");
		specialCharacters.add("$");
		specialCharacters.add("&");
		specialCharacters.add("-");
		specialCharacters.add("+");
		specialCharacters.add("?");
		specialCharacters.add("|");
	}
	
	public Set<String> getSpecialCharacters() {
		return specialCharacters;
	}
	
	public ICondition getCondition(ConstrainedConditionGenerator generator, String scriptText) 
	{
		if (scriptText == null)
			throw new IllegalArgumentException("param scriptText must NOT be null!");

// convert sth. like the following line:
//		JDOHelper.getObjectId(myVariable) == ObjectIDUtil.createObjectID("jdo/org.b.MyClassID?fieldA=0")
//
// to sth. like this:
//		myVariable == jdo/org.b.MyClassID?fieldA=0

		scriptText = scriptText.replaceAll("JDOHelper.getObjectId\\((.*?)\\)", "$1");
		scriptText = scriptText.replaceAll("ObjectIDUtil.createObjectID\\(\\\"(.*?)\\\"\\)", "$1");

		return parseConditionContainer(scriptText, generator);
	}
	
	private boolean isContainer(String text, ConstrainedConditionGenerator generator) 
	{
		// check if simpleCondition or container and call corresponding method
		int openContainerIndex = -1; 
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (openContainerIndex != -1) {
				if (String.valueOf(c).equals(generator.getOpenContainerString()) &&
						i == openContainerIndex + 1) 
				{
					return true;
				}							
			}	
			if (String.valueOf(c).equals(generator.getOpenContainerString())) {
				openContainerIndex = i;
			}
		}
		return false;
	}
	
//	private ICondition parseConditionContainer(String text, ConstrainedConditionGenerator generator) 
//	{
//		while (isContainer(text, generator)) 
//		{
//			System.out.println("containerText = "+text);
//			List<String> subContainer = getContainerSubStrings(text, 
//					generator.getOpenContainerString(), 
//					generator.getCloseContainerString());
//			if (subContainer.size() == 0) {
//				return parseSimpleCondition(generator, text);
//			}
//			ConditionContainer conditionContainer = new ConditionContainer();
//			for (String container : subContainer) {
//				text = text.replace(container, "");
//				ICondition condition = parseConditionContainer(container, generator);
//				conditionContainer.addCondition(condition);
//				System.out.println("subContainer = "+container);
//			}			
//			text = text.replace(generator.getOpenContainerString(), "");
//			text = text.replace(generator.getCloseContainerString(), "");
//			System.out.println("combineText = "+text);
//			Pattern combinePattern = Pattern.compile(getCombineOperatorRegEx(generator));
//			Matcher combinePatternMatcher = combinePattern.matcher(text);
//			if (combinePatternMatcher.find()) {
////				while (combinePatternMatcher.find()) {
////					int groupCount = combinePatternMatcher.groupCount();						
////					System.out.println("groupCount = "+groupCount);
//				int start = combinePatternMatcher.regionStart();
//				int end = combinePatternMatcher.regionEnd();
//				String combineOperator = text.substring(start, end);
//
//				System.out.println("start = "+start);
//				System.out.println("end = "+end);
//				System.out.println("combineOperator = "+combineOperator);
//				
//				conditionContainer.setCombineOperator(generator.getCombineOperator(combineOperator));
//				return conditionContainer;										
//			}
//			else {
//				StringBuffer sb = new StringBuffer();
//				for (String string : generator.getCombineOperators()) {
//					sb.append(string+" , ");
//				}
//				throw new IllegalArgumentException("Param text "+text+" does not contain any of " + 
//						" the following combineOperators!");
//			}	
//		}
//		throw new IllegalArgumentException("Param text "+text+" is not valid");
////		return parseSimpleCondition(generator, text);
//	}
	
	private ICondition parseConditionContainer(String text, ConstrainedConditionGenerator generator) 
	{
//		System.out.println("containerText = "+text);
		List<String> subContainer = getContainerSubStrings(text, 
				generator.getOpenContainerString(), 
				generator.getCloseContainerString());
		if (subContainer.size() == 0) {
			return parseSimpleCondition(generator, text);
		}
		if (subContainer.size() == 1) 
		{
			// remove surrounding brackets
			int firstIndex = text.indexOf(generator.getOpenContainerString());
			int lastIndex = text.lastIndexOf(generator.getCloseContainerString());
			text = text.substring(firstIndex+1, lastIndex);
			return parseConditionContainer(text, generator);			
		}
		else {
			ConditionContainer conditionContainer = new ConditionContainer();
			for (String container : subContainer) {
				text = text.replace(container, "");
				ICondition condition = parseConditionContainer(container, generator);
				conditionContainer.addCondition(condition);
//				System.out.println("subContainer = "+container);
			}			
			text = text.replace(generator.getOpenContainerString(), "");
			text = text.replace(generator.getCloseContainerString(), "");
//			System.out.println("combineText = "+text);
			String combineOperatorRegEx = getCombineOperatorRegEx(generator);
//			System.out.println("combineOperatorRegEx = "+combineOperatorRegEx);
			Pattern combinePattern = Pattern.compile(combineOperatorRegEx);
			Matcher combinePatternMatcher = combinePattern.matcher(text);
			if (combinePatternMatcher.find()) {
//					while (combinePatternMatcher.find()) {
//						int groupCount = combinePatternMatcher.groupCount();						
//						System.out.println("groupCount = "+groupCount);
				
//				int start = combinePatternMatcher.regionStart();
//				int end = combinePatternMatcher.regionEnd();
				int start = combinePatternMatcher.start();
				int end = combinePatternMatcher.end();

				String combineOperator = text.substring(start, end);

//				System.out.println("start = "+start);
//				System.out.println("end = "+end);
//				System.out.println("combineOperator = "+combineOperator);
				
				conditionContainer.setCombineOperator(generator.getCombineOperator(combineOperator));
				return conditionContainer;										
			}
			else {
				StringBuffer sb = new StringBuffer();
				for (String string : generator.getCombineOperators()) {
					sb.append(string+" , ");
				}
				throw new IllegalArgumentException("Param text "+text+" does not contain any of " + 
						" the following combineOperators!");
			}				
		}
	}	
	
	private List<String> getContainerSubStrings(String text, String openContainerString, String closeContainerString) 
	{
		List<String> containers = new ArrayList<String>();
		int firstIndex = -1;
		int lastIndex = -1;
		int counter = 0;
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (String.valueOf(c).equals(openContainerString)) {
				if (firstIndex == -1)
					firstIndex = i;				
				counter++;						
			}
			if (String.valueOf(c).equals(closeContainerString)) {
				counter--;						
			}
			if (firstIndex != -1 && counter == 0) {
				lastIndex = i;
				String container = text.substring(firstIndex+1, lastIndex);
				containers.add(container);
				firstIndex = -1;
				lastIndex = -1;
				counter = 0;
			}
		}
		return containers;
	}
	
	private ISimpleCondition parseSimpleCondition(ConstrainedConditionGenerator generator, String scriptText) 
	{
//		System.out.println("simpleConditionText = "+scriptText);
		String variableRegEx = getVariableRegEx(generator);
		Pattern variablePattern = Pattern.compile(variableRegEx);
		Matcher variableMatcher = variablePattern.matcher(scriptText);
		if (variableMatcher.find()) {
			String variable = variableMatcher.group();
			variable = variable.replace(generator.getVariableString(), "");
//			System.out.println("variable = "+variable);
			String compareOperatorRegEx = getCompareOperatorRegEx(generator);
			Pattern compareOperatorPattern = Pattern.compile(compareOperatorRegEx);
			Matcher compareOperatorMatcher = compareOperatorPattern.matcher(scriptText);
			if (compareOperatorMatcher.find()) {
				String compareOperator = compareOperatorMatcher.group();
//				System.out.println("compareOperator = "+compareOperator);
				CompareOperator co = generator.getCompareOperator(compareOperator);
				int index = scriptText.indexOf(compareOperator);
				String value = "";
				if (index != -1) {
					int endIndex = scriptText.indexOf(generator.getCloseContainerString(), index);
					if (endIndex != -1)
						value = scriptText.substring(index + compareOperator.length(), endIndex);
					else
						value = scriptText.substring(index + compareOperator.length());
//					System.out.println("value = "+value);
//					System.out.println();
				} else {
					throw new IllegalArgumentException("Param scriptText "+scriptText+" does " +
							"not contain a value");
				}
				
				if (logger.isDebugEnabled()) {
					logger.debug("simpleConditionText = "+scriptText);
					logger.debug("variable = "+variable);
					logger.debug("compareOperator = "+compareOperator);
					logger.debug("value = "+value);
				}
				
				ScriptRegistryItemID scriptID = variableName2ScriptID.get(variable);
				Object valueObject = objectIDString2Value.get(value);
				SimpleCondition simpleCondition = new SimpleCondition(scriptID, co, valueObject);
				return simpleCondition;				
			}
			else {
				StringBuffer sb = new StringBuffer();
				for (String compareOperator : generator.getCompareOperators()) {
					sb.append(compareOperator+" , ");
				}
				throw new IllegalArgumentException("Param scriptText "+scriptText+" does " +
						"not contain one of the following compareOperators " +
						sb.toString());			  								
			}
		}
		else
			throw new IllegalArgumentException("Param scriptText "+scriptText+" does " +
					"not contain a variableIdentifier " +
					generator.getVariableString());			  
	}

	private String getVariableRegEx(ConstrainedConditionGenerator generator) 
	{
		StringBuffer sb = new StringBuffer();
		sb.append(checkString(generator.getVariableString()));
		sb.append("([^");
		sb.append(getCompareOperatorRegEx(generator));
		sb.append("]*)");
		String regEx = sb.toString();
//		if (logger.isDebugEnabled()) {
//			logger.debug("variableRegEx = "+regEx);
//		}
		return regEx;
	}
		
	private String getCompareOperatorRegEx(ConstrainedConditionGenerator generator) 
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < generator.getCompareOperators().size(); i++) 
		{
			String compareOperator = generator.getCompareOperators().get(i);
			String checkedCompareOperator = checkString(compareOperator);
			sb.append(checkedCompareOperator);
			if (i != generator.getCompareOperators().size()-1)
				sb.append("|");
		}
		return sb.toString();
	}

	private String getCombineOperatorRegEx(ConstrainedConditionGenerator generator) 
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < generator.getCombineOperators().size(); i++) 
		{
			String combineOperator = generator.getCombineOperators().get(i);
			String checkedCombineOperator = checkString(combineOperator);
			sb.append(checkedCombineOperator);
			if (i != generator.getCompareOperators().size()-1)
				sb.append("|");
		}
		return sb.toString();
	}	
	
	private String checkString(String s) 
	{
			String originalString = new String(s);
			// first check for escape
			if (s.contains("\\")) {
				s = s.replace(s, "\\"+"\\");
			}

			for (String specialCharacter : getSpecialCharacters()) {
				if (s.contains(specialCharacter)) {
					s = s.replace(specialCharacter, "\\"+specialCharacter);
				}
			}
//			if (logger.isDebugEnabled()) {
//				logger.debug("originalString = "+originalString);
//				logger.debug("replacedString = "+s);
//			}
			return s;
	}
	
//	private String specialCharacterRegEx;
//	private String getSpecialCharacterRegEx() {
//		if (specialCharacterRegEx == null) {
//			StringBuffer sb = new StringBuffer();
//			for (String specialCharacter : getSpecialCharacters()) {
//				sb.append("[\\"+specialCharacter+"]");				
//			} 
//			specialCharacterRegEx = sb.toString();
//			if (logger.isDebugEnabled()) {
//				logger.debug("specialCharacterRegEx = "+specialCharacterRegEx); 
//			}
//		}
//		return specialCharacterRegEx;
//	}	
	
}
