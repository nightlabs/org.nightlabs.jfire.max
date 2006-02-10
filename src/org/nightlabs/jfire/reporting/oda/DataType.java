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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Public declarations concerning BIRT datatypes.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[ÐOT]de>
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
//	public static final int TIME = 92;
//	public static final int TIMESTAMP = 93;
	public static final int BLOB = 0;
	public static final int CLOB = 10;
	public static final int UNKNOWN = 0;

	public static final String N_STRING = "String"; 
	public static final String N_INTEGER = "Integer";
	public static final String N_DOUBLE = "Double";
	public static final String N_BOOLEAN = "Boolean";
	public static final String N_DATE = "Date";
	public static final String N_BIGDECIMAL = "BigDecimal(Long)";
//	public static final int TIME = 92;
//	public static final int TIMESTAMP = 93;
	public static final String N_BLOB = "Blob";
	public static final String N_CLOB = "Clob";
	public static final String N_UNKNOWN = "Unknown";
	
	
	public static final Integer K_STRING = new Integer(STRING);; 
	public static final Integer K_INTEGER = new Integer(INTEGER);
	public static final Integer K_DOUBLE = new Integer(DOUBLE);
	public static final Integer K_BOOLEAN = new Integer(BOOLEAN);
	public static final Integer K_DATE = new Integer(DATE);
	public static final Integer K_BIGDECIMAL = new Integer(BIGDECIMAL);
//	public static final Integer K_TIME = new Integer(TIME);
//	public static final Integer K_TIMESTAMP = new Integer(TIMESTAMP);
	public static final Integer K_BLOB = new Integer(BLOB);
	public static final Integer K_CLOB = new Integer(CLOB);
	
	private static final Map types2Classes = new HashMap();
	private static final Map classes2Types = new HashMap();
	private static final Map types2Names = new HashMap();
	
	static {
		types2Classes.put(K_STRING, new Class[] {String.class});
		types2Classes.put(K_INTEGER, new Class[] {Integer.class});
		types2Classes.put(K_DOUBLE, new Class[] {Double.class});
		types2Classes.put(K_DATE, new Class[] {Date.class});
		types2Classes.put(K_BIGDECIMAL, new Class[] {Long.class});
		types2Classes.put(K_BLOB, new Class[] {Serializable.class});
		types2Classes.put(K_CLOB, new Class[] {Serializable.class});
		
		classes2Types.put(String.class, K_STRING);
		classes2Types.put(Integer.class, K_INTEGER);
		classes2Types.put(Double.class, K_DOUBLE);
		classes2Types.put(Date.class, K_DATE);
		classes2Types.put(Long.class, K_BIGDECIMAL);
		classes2Types.put(Serializable.class, K_BLOB);
		classes2Types.put(Serializable.class, K_CLOB);
		
		types2Classes.put(K_STRING, N_STRING);
		types2Classes.put(K_INTEGER, N_INTEGER);
		types2Classes.put(K_DOUBLE, N_DOUBLE);
		types2Classes.put(K_DATE, N_DATE);
		types2Classes.put(K_BIGDECIMAL, N_BIGDECIMAL);
		types2Classes.put(K_BLOB, N_BLOB);
		types2Classes.put(K_CLOB, N_CLOB);
	}
	
	public static Class[] dataTypeToClasses(int dataType) {		
		Class[] types = (Class[])types2Classes.get(new Integer(dataType));
		if (types == null)
			return new Class[] {String.class};
		return types;
	}
	
	public static int classToDataType(Class dataType) {
		Integer type = (Integer)classes2Types.get(dataType);
		if (type != null)
			return type.intValue();
		return UNKNOWN;
	}
	
	public static String getTypeName(int dataType) {
		String name = (String)types2Names.get(new Integer(dataType));
		if (name != null)
			return name;
		return N_UNKNOWN;
	}
	
	
}
