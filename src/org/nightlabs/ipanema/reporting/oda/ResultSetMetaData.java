package org.nightlabs.ipanema.reporting.oda;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public abstract class ResultSetMetaData implements IResultSetMetaData, Serializable {
	
	
	
	public static class Column implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private String name;
		private int dataType;
		
		public Column(String name, int dataType) {
			this.name = name;
			this.dataType = dataType;
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
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnCount()
	 */
	public int getColumnCount() throws OdaException {
		return columns.size();
	}
	
	
	private Map columns = new HashMap();
	private Map colIdxByNames = new HashMap();
	
	/**
	 * Returns the Column for the given 1-based 
	 * column index.
	 */
	private Column getColumn(int index) {
		Column col = (Column)columns.get(new Integer(index));
		if (col == null)
			throw new IllegalArgumentException("No column for index "+index+" registered.");
		return col;
	}
	
	/**
	 * Returns the 1-based index of the column with the given name.
	 * @return
	 */
	public int getColumnIndex(String columnName) {
		Integer idx = (Integer)colIdxByNames.get(columnName);
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
	public void setColumn(int index, String colName, Class dataType) {
		setColumn(index, new Column(colName, DataType.classToDataType(dataType)));
	}
	
	/**
	 * Sets the column meta data for the given index 
	 */
	public void setColumn(int index, String colName, int dataType) {
		setColumn(index, new Column(colName, dataType));		
	}

	/**
	 * Sets the column metadata for the column with index 'columns.size()+1'.
	 */
	public void addColumn(String colName, int dataType) {
		setColumn(columns.size()+1, colName, dataType);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnName(int)
	 */
	public String getColumnName(int index) throws OdaException {
		return getColumn(index).getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnLabel(int)
	 */
	public String getColumnLabel(int index) throws OdaException {
		return getColumn(index).getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnType(int)
	 */
	public int getColumnType(int index) throws OdaException {
		return getColumnDataType(index);
	}
	
	public int getColumnDataType(int index) {
		return getColumn(index).getDataType();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnTypeName(int)
	 */
	public String getColumnTypeName(int index) throws OdaException {
		return DataType.getTypeName(getColumnType(index)).toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnDisplayLength(int)
	 */
	public int getColumnDisplayLength(int index) throws OdaException {
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getPrecision(int)
	 */
	public int getPrecision(int index) throws OdaException {
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getScale(int)
	 */
	public int getScale(int index) throws OdaException {
		// TODO Auto-generated method stub
		return 2;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSetMetaData#isNullable(int)
	 */
	public int isNullable(int index) throws OdaException {
		return 0;
	}
}

