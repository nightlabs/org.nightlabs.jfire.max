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

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.jfire.IBlob;
import org.eclipse.datatools.connectivity.oda.jfire.IClob;
import org.eclipse.datatools.connectivity.oda.jfire.IResultSet;
import org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;


/**
 * Common type for all oda resultsets in JFireReporting.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public abstract class ResultSet implements IResultSet, Serializable {
	private static final long serialVersionUID = 1L;
	private Collection<Object> collection;
	private transient Iterator iterator;
	private int currPos = 0;
	private Object currRow = null;
	private List<Object> currRowCols = null;
	private ResultSetMetaData metaData = null;
	
	
	public ResultSet(ResultSetMetaData metaData) {
		setMetaData(metaData);
	}
	
	protected Collection getCollection() {
		return collection;
	}
	
	protected void setCollection(Collection<Object> collection) {
		this.collection = collection;
	}
	
	protected void setIterator(Iterator iterator) {
		this.iterator = iterator;
	}
	
	protected Iterator getIterator() {
		return iterator;
	}
	
	protected void setMetaData(ResultSetMetaData metaData) {
		this.metaData = metaData;
	}
	
	protected ResultSetMetaData getResultSetMetaData() {
		return metaData;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getMetaData()
	 */
	public IResultSetMetaData getMetaData() throws JFireOdaException {
		return metaData;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#close()
	 */
	public void close() throws JFireOdaException {
		iterator = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#setMaxRows(int)
	 */
	public void setMaxRows(int max) throws JFireOdaException {
		// TODO: Maybe implement
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#next()
	 */
	@SuppressWarnings("unchecked")
	public boolean next() throws JFireOdaException {
		if (iterator == null)
			return false;
		if (iterator.hasNext()) {
			currRow = iterator.next();
			if (currRow instanceof List) {
				currRowCols = (List)currRow;
			}
			else {
				currRowCols = new ArrayList(1);
				currRowCols.add(currRow);
			}
			currPos++;
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getRow()
	 */
	public int getRow() throws JFireOdaException {
		return currPos;
	}

	private void checkRow() throws JFireOdaException {
		if (currRowCols == null)
			throw new JFireOdaException("The current rows columns are not assigned (maybe next() was never called)");
	}
	
	private Object getColObject(int index) throws JFireOdaException {
		checkRow();
		if (index < 1 || index > currRowCols.size() )
			throw new JFireOdaException("The given column index is invalid: "+index+". Number of columns is "+currRowCols.size());
		return currRowCols.get(index - 1);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T checkColObject(int index, Class<T> objectClass) throws JFireOdaException {
		Object o = getColObject(index);
		if (o != null) {
			if (!objectClass.isAssignableFrom(o.getClass()))
				throw new JFireOdaException("Column "+index+" can not be treated as "+objectClass.getName()+" it is "+o.getClass().getName());
		}
		return (T) o;
	}
	
	public int findColumn(String columnName) {
		return metaData.getColumnIndex(columnName);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getString(int)
	 */
	public String getString(int index) throws JFireOdaException {
		String s = checkColObject(index, String.class);
		return s != null ? s : "";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getString(java.lang.String)
	 */
	public String getString(String columnName) throws JFireOdaException {
		return getString(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getInt(int)
	 */
	public int getInt(int index) throws JFireOdaException {
		Integer i = checkColObject(index, Integer.class);
		return i != null ? i.intValue() : 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getInt(java.lang.String)
	 */
	public int getInt(String columnName) throws JFireOdaException {
		return getInt(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getDouble(int)
	 */
	public double getDouble(int index) throws JFireOdaException {
		Double d = checkColObject(index, Double.class);
		return d != null ? d.doubleValue() : 0d;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getDouble(java.lang.String)
	 */
	public double getDouble(String columnName) throws JFireOdaException {
		return getDouble(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getBigDecimal(int)
	 */
	public BigDecimal getBigDecimal(int index) throws JFireOdaException {
		Long l = checkColObject(index, Long.class);
		return new BigDecimal(l != null ? l.longValue() : 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getBigDecimal(java.lang.String)
	 */
	public BigDecimal getBigDecimal(String columnName) throws JFireOdaException {
		return getBigDecimal(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getDate(int)
	 */
	public Date getDate(int index) throws JFireOdaException {
		java.util.Date date = checkColObject(index, java.util.Date.class);
		return date != null ? new Date(date.getTime()) : null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getDate(java.lang.String)
	 */
	public Date getDate(String columnName) throws JFireOdaException {
		return getDate(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getTime(int)
	 */
	public Time getTime(int index) throws JFireOdaException {
		java.util.Date date = checkColObject(index, java.util.Date.class); 
		return new Time(date != null ? date.getTime() : 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getTime(java.lang.String)
	 */
	public Time getTime(String columnName) throws JFireOdaException {
		return getTime(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getTimestamp(int)
	 */
	public Timestamp getTimestamp(int index) throws JFireOdaException {
		java.util.Date date = checkColObject(index, java.util.Date.class); 
		return new Timestamp(date != null ? date.getTime() : 0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getTimestamp(java.lang.String)
	 */
	public Timestamp getTimestamp(String columnName) throws JFireOdaException {
		return getTimestamp(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getBlob(int)
	 */
	public IBlob getBlob(int index) throws JFireOdaException {
		Object o = getColObject(index);
		return DataType.getIBlob(o);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getBlob(java.lang.String)
	 */
	public IBlob getBlob(String columnName) throws JFireOdaException {
		return getBlob(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getClob(int)
	 */
	public IClob getClob(int index) throws JFireOdaException {
		throw new UnsupportedOperationException("NYI");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getClob(java.lang.String)
	 */
	public IClob getClob(String columnName) throws JFireOdaException {
		return getClob(findColumn(columnName));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean getBoolean(String columnName) throws JFireOdaException {
		return getBoolean(findColumn(columnName));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBoolean(int index) throws JFireOdaException {
		Boolean b = checkColObject(index, Boolean.class);
		return b != null ? b : false;
	}
	
	@Override
	public Object getObject(int index) throws JFireOdaException {
		return checkColObject(index, Object.class);
	}
	
	@Override
	public Object getObject(String columnName) throws JFireOdaException {
		return getObject(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#wasNull()
	 */
	public boolean wasNull() throws JFireOdaException {
		return currRow == null;
	}
	
	private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException
    {
		in.defaultReadObject();
		init();
    }
	
	public void init() {
		if (collection != null)
			iterator = collection.iterator();
		currentAddRow = null;
	}
	
	private Collection<Object> getCreateCollection() {
		if (collection == null)
			collection = new LinkedList<Object>();
		return collection;
	}
	
	private transient List<Object> currentAddRow = null;
	
	/**
	 * Adds a new row to the dataSet. A row must be populated
	 * with columns by {@link #addColumn(Object)}.
	 */
	public void addRow() {
		Collection<Object> col = getCreateCollection();
		IResultSetMetaData metaData = null;
		int metaDataColCount = 0;
		try {
			metaData = getMetaData();
			metaDataColCount = (metaData == null) ? 0 : metaData.getColumnCount();
		} catch (JFireOdaException e) {
			throw new RuntimeException(e);
		}
		if (currentAddRow != null) {
			if (metaData != null) {
				if (currentAddRow.size() != metaDataColCount)
					throw new IllegalStateException("Could not add row as not enough columns were added to the previos. Column count is "+currentAddRow+" but should be "+metaDataColCount);
			}
			else
				throw new IllegalStateException("Could not add row. MetaData is not set");
		}
		
		List<Object> row = null;
		try {
			if (getMetaData() != null)
				row = new ArrayList<Object>(getMetaData().getColumnCount());
			else
				row = new ArrayList<Object>();
		} catch (JFireOdaException e) {
			throw new RuntimeException(e);
		}
		col.add(row);
		currentAddRow = row;
	}

	/**
	 * Adds a column to the current row added by {@link #addRow()}.
	 */
	public void addColumn(Object column) {
		if (currentAddRow == null)
			throw new IllegalStateException("currentAddRow is not set call addRow() before addColumn()");
		IResultSetMetaData metaData = null;
		int metaDataColCount = 0;
		try {
			metaData = getMetaData();
			metaDataColCount = (metaData == null) ? 0 : metaData.getColumnCount();
			
		} catch (JFireOdaException e) {
			throw new RuntimeException(e);
		}
		if (metaData != null) {
			if (currentAddRow.size() == metaDataColCount)
				throw new IllegalStateException("Could not add column, as the metaData allows only "+metaDataColCount+" columns.");
		}
		else
			throw new IllegalStateException("Could not add column. MetaData is not set");
		checkCol(currentAddRow.size()+1, column);
		currentAddRow.add(column);
	}

	/**
	 * Adds a new row to the dataset with the given objects as
	 * columns
	 */
	public void addRow(Object[] columns) {
		Collection<Object> col = getCreateCollection();
		int mColCount = 0;
		try {
			mColCount = getMetaData().getColumnCount();
		} catch (JFireOdaException e) {
			throw new RuntimeException(e);
		}
		if (columns.length != mColCount)
			throw new IllegalArgumentException("ColumnCount of metaData("+mColCount+") does not match count of objects for curren row ("+columns.length+")");
		
		List<Object> row = new ArrayList<Object>(columns.length);
		for (int i = 0; i < columns.length; i++) {
			checkCol(i+1, columns[i]);
			row.add(columns[i]);
		}
		col.add(row);
	}
	
	private void checkCol(int index, Object col) {
		if (col == null)
			return;
		IResultSetMetaData metaData = null;
		int dataType = 0;
		try {
			metaData = getMetaData();
			dataType = metaData.getColumnType(index);
		} catch (JFireOdaException e) {
			throw new IllegalStateException("Exception in checkCol while getting metaData: "+e.getClass().getName()+", message: "+e.getMessage());
		}
		int oType = DataType.classToDataType(col.getClass());
		if (oType != dataType)
			throw new IllegalArgumentException("Object for column "+index+" does not match the dataType expected. Object("+col.getClass().getName()+"), expected dataType: "+DataType.getTypeName(dataType));
//		ResultSetMetaData.classToDataType()
//		.getColumnClass(index).isAssignableFrom(col.getClass());
	}
	

}

