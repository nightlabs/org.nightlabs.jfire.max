/**
 * 
 */
package org.nightlabs.ipanema.reporting.oda;

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

import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.IClob;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import org.nightlabs.ipanema.reporting.oda.jdojs.JDOJSResultSetMetaData;


/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public abstract class ResultSet implements IResultSet, Serializable {

	private Collection collection;
	private transient Iterator iterator;
	private int currPos = 0;
	private Object currRow = null;
	private List currRowCols = null;
	private ResultSetMetaData metaData = null;
	
	
	public ResultSet(ResultSetMetaData metaData) {
		setMetaData(metaData);
	}
	
	protected Collection getCollection() {
		return collection;
	}
	
	protected void setCollection(Collection collection) {
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
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getMetaData()
	 */
	public IResultSetMetaData getMetaData() throws OdaException {
		return metaData;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#close()
	 */
	public void close() throws OdaException {
		iterator = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#setMaxRows(int)
	 */
	public void setMaxRows(int max) throws OdaException {
		// TODO: Maybe implement
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#next()
	 */
	public boolean next() throws OdaException {
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
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getRow()
	 */
	public int getRow() throws OdaException {	
		return currPos;
	}

	private void checkRow() throws OdaException {
		if (currRowCols == null)
			throw new OdaException("The current rows columns are not assigned (maybe next() was never called)");
	}
	
	private Object getColObject(int index) throws OdaException {
		checkRow();
		if (index < 1 || index > currRowCols.size() )
			throw new OdaException("The given column index is invalid: "+index+". Number of columns is "+currRowCols.size());
		return currRowCols.get(index - 1);
	}
	
	private Object checkColObject(int index, Class objectClass) throws OdaException {
		Object o = getColObject(index);
		if (o != null) {
			if (!objectClass.isAssignableFrom(o.getClass()))
				throw new OdaException("Column "+index+" can not be treated as "+objectClass.getName()+" it is "+o.getClass().getName());
		}
		return o;
	}
	
	public int findColumn(String columnName) {
		return metaData.getColumnIndex(columnName);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getString(int)
	 */
	public String getString(int index) throws OdaException {				
		return (String)checkColObject(index, String.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getString(java.lang.String)
	 */
	public String getString(String columnName) throws OdaException {
		return getString(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getInt(int)
	 */
	public int getInt(int index) throws OdaException {
		return ((Number)checkColObject(index, Integer.class)).intValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getInt(java.lang.String)
	 */
	public int getInt(String columnName) throws OdaException {		
		return getInt(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDouble(int)
	 */
	public double getDouble(int index) throws OdaException {		
		return ((Number)checkColObject(index, Double.class)).doubleValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDouble(java.lang.String)
	 */
	public double getDouble(String columnName) throws OdaException {
		return getDouble(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBigDecimal(int)
	 */
	public BigDecimal getBigDecimal(int index) throws OdaException {		
		//return new BigDecimal(getDouble(index));
		return new BigDecimal(((Long)checkColObject(index, Long.class)).longValue());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBigDecimal(java.lang.String)
	 */
	public BigDecimal getBigDecimal(String columnName) throws OdaException {
		return getBigDecimal(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDate(int)
	 */
	public Date getDate(int index) throws OdaException {
		return new Date(
				((java.util.Date)checkColObject(index, java.util.Date.class))
					.getTime()
						);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDate(java.lang.String)
	 */
	public Date getDate(String columnName) throws OdaException {
		return getDate(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTime(int)
	 */
	public Time getTime(int index) throws OdaException {
		return new Time(
				((java.util.Date)checkColObject(index, java.util.Date.class))
					.getTime()
						);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTime(java.lang.String)
	 */
	public Time getTime(String columnName) throws OdaException {	
		return getTime(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTimestamp(int)
	 */
	public Timestamp getTimestamp(int index) throws OdaException {
		return new Timestamp(
				((java.util.Date)checkColObject(index, java.util.Date.class))
					.getTime()
						);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTimestamp(java.lang.String)
	 */
	public Timestamp getTimestamp(String columnName) throws OdaException {
		return getTimestamp(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBlob(int)
	 */
	public IBlob getBlob(int index) throws OdaException {
		throw new UnsupportedOperationException("NYI");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBlob(java.lang.String)
	 */
	public IBlob getBlob(String columnName) throws OdaException {
		return getBlob(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getClob(int)
	 */
	public IClob getClob(int index) throws OdaException {
		throw new UnsupportedOperationException("NYI");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getClob(java.lang.String)
	 */
	public IClob getClob(String columnName) throws OdaException {
		return getClob(findColumn(columnName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#wasNull()
	 */
	public boolean wasNull() throws OdaException {
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
	
	private Collection getCreateCollection() {
		if (collection == null)
			collection = new LinkedList();
		return collection;
	}
	
	private transient List currentAddRow = null;
	
	/**
	 * Adds a new row to the dataSet. A row must be populated
	 * with columns by {@link #addColumn(Object)}.
	 */
	public void addRow() {
		Collection col = getCreateCollection();
		IResultSetMetaData metaData = null;
		int metaDataColCount = 0;
		try {
			metaData = getMetaData();
			metaDataColCount = (metaData == null) ? 0 : metaData.getColumnCount();
		} catch (OdaException e) {
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
		
		List row = null;
		try {
			if (getMetaData() != null)
				row = new ArrayList(getMetaData().getColumnCount());
			else
				row = new ArrayList();
		} catch (OdaException e) {
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
			
		} catch (OdaException e) {
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
		Collection col = getCreateCollection();
		int mColCount = 0;
		try {
			mColCount = getMetaData().getColumnCount();
		} catch (OdaException e) {
			throw new RuntimeException(e);
		}
		if (columns.length != mColCount)
			throw new IllegalArgumentException("ColumnCount of metaData("+mColCount+") does not match count of objects for curren row ("+columns.length+")");
		
		List row = new ArrayList(columns.length);
		for (int i = 0; i < columns.length; i++) {
			checkCol(i+1, columns[i]);
			row.add(columns[i]);
		}
		col.add(row);
	}
	
	private void checkCol(int index, Object col) {
		if (col == null)
			return;
		JDOJSResultSetMetaData metaData = null;
		int dataType = 0;
		try {
			metaData = (JDOJSResultSetMetaData) getMetaData();
			dataType = metaData.getColumnType(index);
		} catch (OdaException e) {
			throw new IllegalStateException("Exception in checkCol while getting metaData: "+e.getClass().getName()+", message: "+e.getMessage());
		}
		int oType = DataType.classToDataType(col.getClass());
		if (oType != dataType)
			throw new IllegalArgumentException("Object for column "+index+" does not match the dataType expected. Object("+col.getClass().getName()+"), expected dataType: "+DataType.getTypeName(dataType));
//		ResultSetMetaData.classToDataType()
//		.getColumnClass(index).isAssignableFrom(col.getClass());
	}
	

}

