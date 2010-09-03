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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;

/**
 * Common type for all oda resultset metadata in JFireReporting.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ResultSetMetaData implements IResultSetMetaData, Serializable {
	
	private static final long serialVersionUID = 1L;

	public static class Column implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private String name;
		private int dataType;
		private int nullable;
		
		public Column(String name, int dataType, int nullable) {
			this.name = name;
			this.dataType = dataType;
			this.nullable = nullable;
		}
		public int getDataType() {
			return dataType;
		}
		public void setDataType(int dataType) {
			this.dataType = dataType;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int isNullable() {
			return nullable;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData#getColumnCount()
	 */
	public int getColumnCount() throws JFireOdaException {
		return columns.size();
	}
	
	
	private Map<Integer, Column> columns = new HashMap<Integer, Column>();
	private Map<String, Integer> colIdxByNames = new HashMap<String, Integer>();
	
	/**
	 * Returns the Column for the given 1-based
	 * column index.
	 */
	private Column getColumn(int index) {
		Column col = columns.get(new Integer(index));
		if (col == null)
			throw new IllegalArgumentException("No column for index "+index+" registered.");
		return col;
	}
	
	/**
	 * Returns the 1-based index of the column with the given name.
	 * @return
	 */
	public int getColumnIndex(String columnName) {
		Integer idx = colIdxByNames.get(columnName);
		if (idx == null)
			throw new IllegalArgumentException("No column with name "+columnName+" is registered.");
		return idx.intValue();
	}
	
	/**
	 * Sets the given column to the given 1-based index.
	 */
	public void setColumn(int index, Column col) {
		Integer idx = new Integer(index);
		columns.put(idx, col);
		colIdxByNames.put(col.getName(), idx);
	}
	
	/**
	 * Sets the metaData for the given 1-based column to the given
	 * name and datatype.
	 */
	public void setColumn(int index, String colName, Class dataType, int nullable) {
		setColumn(index, new Column(colName, DataType.classToDataType(dataType), nullable));
	}
	
	/**
	 * Sets the column meta data for the given index.
	 * 
	 */
	public void setColumn(int index, String colName, int dataType, int nullable) {
		setColumn(index, new Column(colName, dataType, nullable));
	}

	/**
	 * Sets the column metadata for the column with index 'columns.size()+1'.
	 */
	public void addColumn(String colName, int dataType, int nullable) {
		setColumn(columns.size()+1, colName, dataType, nullable);
	}
	
	/**
	 * Sets the column metadata for the column with index 'columns.size()+1'.
	 * Nullable will be {@link java.sql.ResultSetMetaData#columnNullable}.
	 */
	public void addColumn(String colName, int dataType) {
		setColumn(columns.size()+1, colName, dataType, IResultSetMetaData.columnNullable);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData#getColumnName(int)
	 */
	public String getColumnName(int index) throws JFireOdaException {
		return getColumn(index).getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData#getColumnLabel(int)
	 */
	public String getColumnLabel(int index) throws JFireOdaException {
		return getColumn(index).getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData#getColumnType(int)
	 */
	public int getColumnType(int index) throws JFireOdaException {
		return getColumnDataType(index);
	}
	
	public int getColumnDataType(int index) {
		return getColumn(index).getDataType();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData#getColumnTypeName(int)
	 */
	public String getColumnTypeName(int index) throws JFireOdaException {
		return DataType.getTypeName(getColumnType(index)).toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData#getColumnDisplayLength(int)
	 */
	public int getColumnDisplayLength(int index) throws JFireOdaException {
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData#getPrecision(int)
	 */
	public int getPrecision(int index) throws JFireOdaException {
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData#getScale(int)
	 */
	public int getScale(int index) throws JFireOdaException {
		// TODO Auto-generated method stub
		return 2;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData#isNullable(int)
	 */
	public int isNullable(int index) throws JFireOdaException {
		return getColumn(index).isNullable();
	}
}

