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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.reporting.oda.jdoql;

import java.sql.ResultSetMetaData;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Parser for JDOQL to get {@link org.eclipse.datatools.connectivity.oda.IResultSetMetaData}
 * without executing the query.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class JDOQLMetaDataParser {
	private static final String S_FIELD = "[a-zA-Z0-9_\\-\\.\\(\\)\"]+";
	private static final String S_PART = "(" + S_FIELD + ")\\s*AS\\s*(" + S_FIELD + ")|(" + S_FIELD + ")";
//	private static final String S_MULTIPLE = "(^\\s*SELECT\\s+)*"+ S_PART +"+\\s*,*(?:FROM\\s.*)*";
	private static final String S_PAT = "(?:SELECT\\s+|,\\s*)(" + S_PART + ")";
	
	public static final Pattern PATTERN_SELECT = Pattern.compile(S_PAT, Pattern.CASE_INSENSITIVE);

	private static final String CC_CLASS = "[a-zA-Z0-9_\\-\\.]+";
	private static final String CC_PAT = "(?:FROM\\s+)((" + CC_CLASS + "))";
	
	public static final Pattern PATTERN_CANDIDATE = Pattern.compile(CC_PAT, Pattern.CASE_INSENSITIVE);
	
	/**
	 * Parses the given query and makes a guess on the result meta data.
	 */
	public static JDOQLResultSetMetaData parseJDOQLMetaData(String query) {
		if (query == null)
			throw new IllegalArgumentException("Can not parse null-query!");
		Matcher mSelect = PATTERN_SELECT.matcher(query);
		Matcher mCandidate = PATTERN_CANDIDATE.matcher(query);
		JDOQLResultSetMetaData metaData = new JDOQLResultSetMetaData();
		
		if (!mCandidate.find())
			throw new IllegalArgumentException("No candidate class defined in query: "+query);
		String candidate = mCandidate.group(1);
		
		int colCount = 0;
		while (mSelect.find()) {
			colCount++;
			String columnText = mSelect.group(1);
			String selectStatement = (mSelect.group(2) == null) ? columnText : mSelect.group(2);
			String columnName = (mSelect.group(3) == null) ? columnText : mSelect.group(3);
			metaData.setColumn(
				colCount,
				columnName,
				getResultColumnDataType(candidate, selectStatement),
				ResultSetMetaData.columnNullable
			);
		}
		
//		metaData.s
		
		return metaData;
	}
	
	private static Class getResultColumnDataType(String candidate, String selectStatement) {
		// TODO: implement getResultColumnDataType
		return String.class;
	}
	
	/* ******************* Test ******************* */
	
	public static void main(String[] args) {
		testSelect();
		testCC();
	}
	
	public static void testSelect() {
		Matcher m = PATTERN_SELECT.matcher("SELECT this.amount as amount ,this.owner.name.get(\"en\") as name, xx AS xxx, FROMDT blubb FROM Test, blubb WHERE Trallali AS muell bla");
		System.out.println(PATTERN_SELECT.toString());
		System.out.println("**************************");
		while (m.find()) {
			System.out.println("---");
			for (int i=0; i<m.groupCount(); i++) {
//				if (i != 0)
					System.out.println("Group "+i+": "+m.group(i));
			}
		}
		System.out.println("**************************");
		
	}

	public static void testCC() {
		Matcher m = PATTERN_CANDIDATE.matcher("SELECT this.amount as amount ,this.owner.name.get(\"en\") as name, xx AS xxx, FROMDT blubb FROM org.nightlabs.Test, blubb WHERE Trallali AS muell bla from what ever here as will from selection");
		System.out.println(PATTERN_CANDIDATE.toString());
		System.out.println("**************************");
		while (m.find()) {
			System.out.println("---");
			for (int i=0; i<m.groupCount(); i++) {
//				if (i != 0)
					System.out.println("Group "+i+": "+m.group(i));
			}
		}
		System.out.println("**************************");
	}
	 
}
