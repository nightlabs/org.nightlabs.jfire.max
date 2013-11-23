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

	private Map<ScriptRegistryItemID, ValueStringConverter> scriptID2ValueStringConverter = null;
	private Collection<ScriptConditioner> scriptConditioners = null;

	/**
	 * May only be used if {@link ConstrainedConditionScriptParser#getCondition(ConstrainedConditionGenerator, String, boolean)}
	 * is called with the boolean (simpleStringCondition) true
	 */
	public ConstrainedConditionScriptParser() {

	}

	public ConstrainedConditionScriptParser(Collection<ScriptConditioner> scriptConditioners) {
		this(scriptConditioners, null);
	}

	public ConstrainedConditionScriptParser(Collection<ScriptConditioner> scriptConditioners,
			Map<ScriptRegistryItemID, ValueStringConverter> scriptID2ValueStringConverter)
	{
		this.scriptConditioners = scriptConditioners;
		this.scriptID2ValueStringConverter = scriptID2ValueStringConverter;
		variableName2ScriptID = new HashMap<String, ScriptRegistryItemID>();
		valueString2Value = new HashMap<String, Object>();

		for (ScriptConditioner conditioner : scriptConditioners) {
			variableName2ScriptID.put(conditioner.getVariableName(),
					conditioner.getScriptRegistryItemID());
			for (Object value : conditioner.getPossibleValues()) {
				if (value instanceof PersistenceCapable) {
					ObjectID objectID = (ObjectID) JDOHelper.getObjectId(value);
					if (objectID != null)
						valueString2Value.put(objectID.toString(), value);
					else
						logger.error("There exists no ObjectID for the persistenceCapable "+String.valueOf(value));
				}
				else {
					valueString2Value.put(String.valueOf(value), value);
					if (logger.isDebugEnabled()) {
						logger.debug("valueString = "+String.valueOf(value));
						logger.debug("value = "+value);
					}
				}
			}
		}

	}

	private Map<String, ScriptRegistryItemID> variableName2ScriptID;
	private Map<String, Object> valueString2Value;

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

	/**
	 *
	 * @param generator the {@link ConstrainedConditionGenerator} for the given language to parse
	 * @param scriptText the text of the script
	 * @param simpleStringCondition true if the parsed text represents a simple {@link ICondition} or false if it is a {@link IConditionContainer}
	 * @return the parsed and filled {@link ICondition}
	 */
	public ICondition getCondition(ConstrainedConditionGenerator generator, String scriptText,
			boolean simpleStringCondition)
	{
		if (scriptText == null)
			throw new IllegalArgumentException("param scriptText must NOT be null!");

		System.out.println("originalScriptText = "+scriptText);

// convert sth. like the following line:
//		importPackage(Packages.javax.jdo);
//		(JDOHelper.getObjectId(myVariable).toString()==jdo/org.b.MyClassID?fieldA=0)
//
// to sth. like this:
//		myVariable == jdo/org.b.MyClassID?fieldA=0

		StringBuffer sb = new StringBuffer();
		sb.append("importPackage\\(Packages.javax.jdo\\)\\;");
		String importRegEx = sb.toString();
		scriptText = scriptText.replaceAll(importRegEx, "");

		sb = new StringBuffer();
		sb.append("JDOHelper.getObjectId\\((.*?)\\)");
		sb.append("\\.toString\\(\\)");
		String varRegEx = sb.toString();
		scriptText = scriptText.replaceAll(varRegEx, "$1");
		System.out.println("replacedScriptText = "+scriptText);

		return parseConditionContainer(scriptText, generator, simpleStringCondition);
	}

	protected ICondition parseConditionContainer(String text, ConstrainedConditionGenerator generator,
			boolean simpleStringCondition)
	{
		List<String> subContainer = getContainerSubStrings(text,
				generator.getOpenContainerString(),
				generator.getCloseContainerString());
		if (subContainer.size() == 0) {
			return getSimpleConditions(text, generator, simpleStringCondition);
		}
		if (subContainer.size() == 1)
		{
			// remove surrounding brackets
			int firstIndex = text.indexOf(generator.getOpenContainerString());
			int lastIndex = text.lastIndexOf(generator.getCloseContainerString());
			text = text.substring(firstIndex+1, lastIndex);
			return parseConditionContainer(text, generator, simpleStringCondition);
//			return parseConditionContainer(text, generator, true);
		}
		else {
			ConditionContainer conditionContainer = new ConditionContainer();
			for (String container : subContainer) {
				text = text.replace(container, "");
				ICondition condition = parseConditionContainer(container, generator, simpleStringCondition);
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
				int start = combinePatternMatcher.start();
				int end = combinePatternMatcher.end();
				String combineOperator = text.substring(start, end);
				conditionContainer.setCombineOperator(generator.getCombineOperator(combineOperator));
				return conditionContainer;
			}
			else {
				StringBuffer sb = new StringBuffer();
				for (String string : generator.getCombineOperators()) {
					sb.append(string+" , ");
				}
				throw new IllegalArgumentException("Param text "+text+" does not contain any of " +
						" the following combineOperators: "+sb.toString());
			}
		}
	}

	protected List<String> getContainerSubStrings(String text, String openContainerString, String closeContainerString)
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

	protected ICondition parseSimpleCondition(ConstrainedConditionGenerator generator,
			String scriptText, boolean simpleStringCondition)
	{
		String variableRegEx = getVariableRegEx(generator);
		Pattern variablePattern = Pattern.compile(variableRegEx);
		Matcher variableMatcher = variablePattern.matcher(scriptText);
		if (variableMatcher.find()) {
			String variable = variableMatcher.group();
			variable = variable.replace(generator.getVariableString(), "");
			String compareOperatorRegEx = getCompareOperatorRegEx(generator);
			Pattern compareOperatorPattern = Pattern.compile(compareOperatorRegEx);
			Matcher compareOperatorMatcher = compareOperatorPattern.matcher(scriptText);
			if (compareOperatorMatcher.find()) {
				String compareOperator = compareOperatorMatcher.group();
				CompareOperator co = generator.getCompareOperator(compareOperator);
				int index = scriptText.indexOf(compareOperator);
				String value = "";
				if (index != -1) {
					int endIndex = scriptText.indexOf(generator.getCloseContainerString(), index);
					if (endIndex != -1)
						value = scriptText.substring(index + compareOperator.length(), endIndex);
					else
						value = scriptText.substring(index + compareOperator.length());

					logger.debug("value before replace = "+value);
					Pattern p = Pattern.compile("\\\"(.*?)\\\"");
					Matcher matcher = p.matcher(value);
					if (matcher.find()) {
						value = value.substring(1, value.length()-1);
					}
					logger.debug("value after replace = "+value);

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

				if (simpleStringCondition) {
					SimpleStringCondition stringCondition = new SimpleStringCondition(
							variable, compareOperator, value);
					return stringCondition;
				}
				else {
					ScriptRegistryItemID scriptID = variableName2ScriptID.get(variable);
					Object valueObject = null;
					if (scriptID2ValueStringConverter != null && scriptID2ValueStringConverter.get(scriptID) != null) {
						ValueStringConverter valueStringConverter = scriptID2ValueStringConverter.get(scriptID);
						valueObject = valueStringConverter.getValue(value);
					}
					else {
						valueObject = valueString2Value.get(value);
					}
					SimpleCondition simpleCondition = new SimpleCondition(scriptID, co, valueObject);
					return simpleCondition;
				}
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

	protected String getVariableRegEx(ConstrainedConditionGenerator generator)
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

	protected String getCompareOperatorRegEx(ConstrainedConditionGenerator generator)
	{
		StringBuffer sb = new StringBuffer();
		// get all compare operator Strings and concat them with |
		for (int i = 0; i < generator.getCompareOperators().size(); i++) {
			String compareOperator = generator.getCompareOperators().get(i);
			if (compareOperator != null) {
				String checkedCompareOperator = checkString(compareOperator);
				sb.append(checkedCompareOperator);
				if (i != generator.getCompareOperators().size()-1)
					sb.append("|");
			}
		}
		// replace duplicate | if some compareOperator Strings are null
		while (sb.indexOf("||") != -1) {
			String s = sb.toString();
			s = s.replace("||", "|");
			sb = new StringBuffer(s);
		}
		// cut off | at the end if some compareOperator Strings are null
		while(sb.lastIndexOf("|") == sb.length()-1) {
			sb = new StringBuffer(sb.subSequence(0, sb.length()-1));
		}
		return sb.toString();
	}


	protected String getCombineOperatorRegEx(ConstrainedConditionGenerator generator)
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

	protected String checkString(String s)
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

	protected ICondition getSimpleConditions(String s, ConstrainedConditionGenerator generator,
			boolean simpleStringCondition)
	{
		Pattern combinePattern = Pattern.compile(getCombineOperatorRegEx(generator));
		Matcher combinePatternMatcher = combinePattern.matcher(s);
		List<String> simpleConditions = new ArrayList<String>();

		int start = 0;
		int end = 0;
		StringBuffer sb = new StringBuffer(s);
		boolean found = false;
		CombineOperator combineOperator = null;
		// get all strings between compareOperators
		while (combinePatternMatcher.find())
		{
			String group = combinePatternMatcher.group();
			if (!group.equals("")) {
				found = true;
				combineOperator = generator.getCombineOperator(group);
				end = combinePatternMatcher.start();
				String simpleCondition = sb.substring(start, end);
				simpleConditions.add(simpleCondition);
				start = combinePatternMatcher.end();
			}
		}

		// get final condition
		if (end != 0) {
			if (start != sb.length()-1) {
				String simpleCondition = sb.substring(start, sb.length());
				simpleConditions.add(simpleCondition);
				if (logger.isDebugEnabled()) {
					logger.debug("simpleCondition = "+simpleCondition);
				}
			}
		}

		// if no compareOperators have been found just add the original string
		if (!found) {
			return parseSimpleCondition(generator, s, simpleStringCondition);
		}
		else {
			IConditionContainer container = new ConditionContainer();
			container.setCombineOperator(combineOperator);
			for (String string : simpleConditions) {
				ICondition simpleCondition = parseSimpleCondition(generator, string, simpleStringCondition);
				container.addCondition(simpleCondition);
			}
			return container;
		}
	}
}
