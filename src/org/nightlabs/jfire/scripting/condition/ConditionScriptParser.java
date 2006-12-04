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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ConditionScriptParser 
{
	private static final Logger logger = Logger.getLogger(ConditionScriptParser.class);

	static Set<String> specialCharacters = new HashSet<String>();
	static {
		specialCharacters.add("}");
		specialCharacters.add("{");
		specialCharacters.add("[");
		specialCharacters.add("]");
		specialCharacters.add("^");
		specialCharacters.add(".");
		specialCharacters.add("\\");
		specialCharacters.add("$");
		specialCharacters.add("&");
		specialCharacters.add("-");
		specialCharacters.add("+");
		specialCharacters.add("?");
	}
	
	public Set<String> getSpecialCharacters() {
		return specialCharacters;
	}
	
	public ICondition getCondition(IConditionGenerator generator, String scriptText) 
	{
		if (scriptText == null)
			throw new IllegalArgumentException("param scriptText must NOT be null!");

//		String openContainer = checkString(generator.getOpenContainerString());
//		String closeContainer = checkString(generator.getCloseContainerString());
//		Pattern containerPattern = Pattern.compile(openContainer+"(.*?)"+closeContainer);
//		String[] container = containerPattern.split(scriptText);
		return parseContainer(generator, scriptText);
	}

	private ICondition parseContainer(IConditionGenerator generator, String scriptText)
	{
		// check if simpleCondition or container and call corespodning method
		return null;
	}
	
	private List<String> getContainerSubStrings(String text, String openContainerString, String closeContainerString) 
	{
//		(($variable4==Value4)||(($variable==Value)&&(($variable2!=Value2)||($variable3!=Value3)))||($variable5>Valu5))
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
			if (String.valueOf(c).equals(openContainerString)) {
				counter--;						
			}
			if (firstIndex != -1 && counter == 0) {
				lastIndex = i;
				String container = text.substring(firstIndex, lastIndex);
				containers.add(container);
				firstIndex = -1;
				lastIndex = -1;
				counter = 0;
			}
		}
		return containers;
	}
	
	private ISimpleCondition parseSimpleCondition(IConditionGenerator generator, String scriptText) 
	{
		String[] variableSplit = getVariablePattern(generator).split(scriptText);
		String variable = variableSplit[0];  
		String[] compareOperatorSplit = Pattern.compile(getCompareOperatorRegEx(generator)).split(scriptText);
		String compareOperator = compareOperatorSplit[0];
		CompareOperator co = generator.getCompareOperator(compareOperator);
		int index = scriptText.indexOf(compareOperator);
		String value = "";
		if (index != -1) {
			value = scriptText.substring(index + compareOperator.length());
		}		
		
		if (logger.isDebugEnabled()) {
			logger.debug("variable = "+variable);
			logger.debug("compareOperator = "+compareOperator);
			logger.debug("value = "+value);
		}
		
		SimpleCondition simpleCondition = new SimpleCondition(variable, co, value);
		return simpleCondition;
	}

	private Pattern getVariablePattern(IConditionGenerator generator) 
	{
		StringBuffer sb = new StringBuffer();
		sb.append(checkString(generator.getVariableString()));
		sb.append("([^");
		sb.append(getCompareOperatorRegEx(generator));
		sb.append("]*)");
		String regEx = sb.toString();
		if (logger.isDebugEnabled()) {
			logger.debug("variableRegEx = "+regEx);
		}
		return Pattern.compile(regEx);
	}
		
	private String getCompareOperatorRegEx(IConditionGenerator generator) 
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

	private String getCombineOperatorRegEx(IConditionGenerator generator) 
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
//		if (s.matches(getSpecialCharacterRegEx())) {
			String originalString = new String(s);
			for (String specialCharacter : getSpecialCharacters()) {
				if (s.contains(specialCharacter)) {
					s = s.replace(specialCharacter, "\\"+specialCharacter);
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("originalString = "+originalString);
				logger.debug("replacedString = "+s);
			}
			return s;
//		}			
//		else
//			return s;
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
