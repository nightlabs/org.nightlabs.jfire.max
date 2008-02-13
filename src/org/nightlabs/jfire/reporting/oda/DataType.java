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

package org.nightlabs.jfire.reporting.oda;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Public declarations concerning BIRT datatypes.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class DataType {
	protected DataType() {}
	
	public static final int STRING = 1;
	public static final int INTEGER = 2;
	public static final int DOUBLE = 3;
	public static final int BOOLEAN = 4;
	public static final int DATE = 5;
	public static final int BIGDECIMAL = 6;
	public static final int TIME = 92;
	public static final int TIMESTAMP = 93;
	public static final int BLOB = 0;
	public static final int CLOB = 10;
	public static final int JAVA_OBJECT = 2000;
	public static final int UNKNOWN = 0;

	public static final String N_STRING = "String";
	public static final String N_INTEGER = "Integer";
	public static final String N_DOUBLE = "Double";
	public static final String N_BOOLEAN = "Boolean";
	public static final String N_DATE = "Date";
	public static final String N_BIGDECIMAL = "BigDecimal(Long)";
	public static final String N_TIME = "Time";
	public static final String N_TIMESTAMP = "Timestamp";
	public static final String N_BLOB = "Blob";
	public static final String N_CLOB = "Clob";
	public static final String N_UNKNOWN = "Unknown";
	
	
//	public static final Integer K_STRING = new Integer(STRING);;
//	public static final Integer K_INTEGER = new Integer(INTEGER);
//	public static final Integer K_DOUBLE = new Integer(DOUBLE);
//	public static final Integer K_BOOLEAN = new Integer(BOOLEAN);
//	public static final Integer K_DATE = new Integer(DATE);
//	public static final Integer K_BIGDECIMAL = new Integer(BIGDECIMAL);
////	public static final Integer K_TIME = new Integer(TIME);
////	public static final Integer K_TIMESTAMP = new Integer(TIMESTAMP);
//	public static final Integer K_BLOB = new Integer(BLOB);
//	public static final Integer K_CLOB = new Integer(CLOB);
	
	private static final Map<Integer, Class[]> types2Classes = new HashMap<Integer, Class[]>();
	private static final Map<Class, Integer> classes2Types = new HashMap<Class, Integer>();
	private static final Map<Integer, String> types2Names = new HashMap<Integer, String>();
	private static final Map<Integer, Integer> sqlTypes2dataTypes = new HashMap<Integer, Integer>();
	private static final Map<Integer, Integer> dataTypes2sqlTypes = new HashMap<Integer, Integer>();
	
	static {
		types2Classes.put(STRING, new Class[] {String.class});
		types2Classes.put(INTEGER, new Class[] {Integer.class});
		types2Classes.put(DOUBLE, new Class[] {Double.class, Float.class});
		types2Classes.put(DATE, new Class[] {Date.class});
		types2Classes.put(BIGDECIMAL, new Class[] {Long.class, BigDecimal.class});
		types2Classes.put(BLOB, new Class[] {Serializable.class});
		types2Classes.put(CLOB, new Class[] {Serializable.class});
		types2Classes.put(BOOLEAN, new Class[] {Boolean.class});
		
		classes2Types.put(String.class, STRING);
		classes2Types.put(Integer.class, INTEGER);
		classes2Types.put(Double.class, DOUBLE);
		classes2Types.put(Date.class, DATE);
		classes2Types.put(Long.class, BIGDECIMAL);
		classes2Types.put(Serializable.class, BLOB);
		classes2Types.put(Serializable.class, CLOB);
		classes2Types.put(Boolean.class, BOOLEAN);
//		classes2Types.put(java.sql..class, CLOB);
		
		types2Names.put(STRING, N_STRING);
		types2Names.put(INTEGER, N_INTEGER);
		types2Names.put(DOUBLE, N_DOUBLE);
		types2Names.put(DATE, N_DATE);
		types2Names.put(TIME, N_TIME);
		types2Names.put(TIMESTAMP, N_TIMESTAMP);
		types2Names.put(BIGDECIMAL, N_BIGDECIMAL);
		types2Names.put(BOOLEAN, N_BOOLEAN);
		types2Names.put(BLOB, N_BLOB);
		types2Names.put(CLOB, N_CLOB);
		
		indexDataTypeToSQLType(java.sql.Types.BIT, BOOLEAN);
		indexDataTypeToSQLType(java.sql.Types.TINYINT, INTEGER);
		indexDataTypeToSQLType(java.sql.Types.SMALLINT, INTEGER);
		indexDataTypeToSQLType(java.sql.Types.INTEGER, INTEGER);
		indexDataTypeToSQLType(java.sql.Types.BIGINT, BIGDECIMAL);
		indexDataTypeToSQLType(java.sql.Types.FLOAT, DOUBLE);
		indexDataTypeToSQLType(java.sql.Types.REAL, DOUBLE);
		indexDataTypeToSQLType(java.sql.Types.DOUBLE, DOUBLE);
		indexDataTypeToSQLType(java.sql.Types.NUMERIC, BIGDECIMAL);
		indexDataTypeToSQLType(java.sql.Types.DECIMAL, DOUBLE);
		indexDataTypeToSQLType(java.sql.Types.CHAR, STRING);
		indexDataTypeToSQLType(java.sql.Types.VARCHAR, STRING);
		indexDataTypeToSQLType(java.sql.Types.LONGVARCHAR, STRING);
		indexDataTypeToSQLType(java.sql.Types.DATE, DATE);
		indexDataTypeToSQLType(java.sql.Types.LONGVARCHAR, STRING);
		indexDataTypeToSQLType(java.sql.Types.TIME, TIME);
		indexDataTypeToSQLType(java.sql.Types.TIMESTAMP, TIMESTAMP);
		indexDataTypeToSQLType(java.sql.Types.BINARY, BLOB);
		indexDataTypeToSQLType(java.sql.Types.VARBINARY, BLOB);
		indexDataTypeToSQLType(java.sql.Types.LONGVARBINARY, BLOB);
		indexDataTypeToSQLType(java.sql.Types.LONGVARCHAR, UNKNOWN);
		indexDataTypeToSQLType(java.sql.Types.OTHER, UNKNOWN);
		indexDataTypeToSQLType(java.sql.Types.JAVA_OBJECT, JAVA_OBJECT);
		indexDataTypeToSQLType(java.sql.Types.LONGVARCHAR, UNKNOWN);
		indexDataTypeToSQLType(java.sql.Types.ARRAY, UNKNOWN);
		indexDataTypeToSQLType(java.sql.Types.BLOB, BLOB);
		indexDataTypeToSQLType(java.sql.Types.CLOB, CLOB);
		indexDataTypeToSQLType(java.sql.Types.REF, UNKNOWN);
		indexDataTypeToSQLType(java.sql.Types.DATALINK, UNKNOWN);
		indexDataTypeToSQLType(java.sql.Types.BOOLEAN, BOOLEAN);
	}
	
	private static void indexDataTypeToSQLType(int sqlType, int dataType) {
		sqlTypes2dataTypes.put(sqlType, dataType);
		dataTypes2sqlTypes.put(dataType, sqlType);
	}
	
	public static Class[] dataTypeToClasses(int dataType) {
		Class[] types = types2Classes.get(new Integer(dataType));
		if (types == null)
			return new Class[] {String.class};
		return types;
	}
	
	public static int classToDataType(Class dataType) {
		Integer type = classes2Types.get(dataType);
		if (type != null)
			return type.intValue();
//		return UNKNOWN;
		return STRING;
	}
	
	public static String getTypeName(int dataType) {
		String name = types2Names.get(new Integer(dataType));
		if (name != null)
			return name;
		return N_UNKNOWN;
	}
	
	public static int sqlTypeToDataType(int sqlType) {
		Integer result = sqlTypes2dataTypes.get(sqlType);
		if (result == null)
			return UNKNOWN;
		return result;
	}
	
	public static int dataTypeToSQLType(int dataType) {
		Integer result = dataTypes2sqlTypes.get(dataType);
		if (result == null)
			return Types.NULL; // TODO: Find better solution for UNKNOWN sql type
		return result;
	}
	
	
}
