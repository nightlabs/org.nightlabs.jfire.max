/**
 *
 */
package org.nightlabs.jfire.reporting.oda;

import java.io.Serializable;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.jfire.IBlob;
import org.eclipse.datatools.connectivity.oda.jfire.IClob;
import org.eclipse.datatools.connectivity.oda.jfire.IResultSet;
import org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;

/**
 * An implementation of {@link IResultMetaData} that uses
 * a list of sql {@link ResultSet} to provide Data.
 * <p>
 * It imposes only one constraint on the ResultSets used:
 * They need to be {@link Serializable}.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class SQLResultSet implements IResultSet, Serializable {

	private static final long serialVersionUID = 1L;

	private ResultSet resultSet;
//	private int currentResultSetIdx = 0;
	private List<ResultSet> resultSets = new ArrayList<ResultSet>(1);

	/**
	 *
	 */
	public SQLResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
		resultSets.add(resultSet);
	}

	public void addResultSet(ResultSet newResultSet) {
		resultSets.add(newResultSet);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#close()
	 */
	public void close() throws JFireOdaException {
		try {
			resultSet.close();
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#findColumn(java.lang.String)
	 */
	public int findColumn(String columnName) throws JFireOdaException {
		try {
			return resultSet.findColumn(columnName);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getBigDecimal(int)
	 */
	public BigDecimal getBigDecimal(int columnIndex) throws JFireOdaException {
		try {
			return resultSet.getBigDecimal(columnIndex);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getBigDecimal(java.lang.String)
	 */
	public BigDecimal getBigDecimal(String columnName) throws JFireOdaException {
		try {
			return resultSet.getBigDecimal(columnName);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getBlob(int)
	 */
	public IBlob getBlob(int columnIndex) throws JFireOdaException {
		if (getMetaData().getColumnType(columnIndex) == DataType.JAVA_OBJECT)
			return new JavaObjectBlob(getObject(columnIndex));
		throw new UnsupportedOperationException("NIY");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getBlob(java.lang.String)
	 */
	public IBlob getBlob(String columnName) throws JFireOdaException {
		if (getMetaData().getColumnType(findColumn(columnName)) == DataType.JAVA_OBJECT)
			return new JavaObjectBlob(getObject(columnName));
		throw new UnsupportedOperationException("NIY");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getClob(int)
	 */
	public IClob getClob(int arg0) throws JFireOdaException {
		throw new UnsupportedOperationException("NIY");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getClob(java.lang.String)
	 */
	public IClob getClob(String arg0) throws JFireOdaException {
		throw new UnsupportedOperationException("NIY");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getDate(int)
	 */
	public Date getDate(int columnIndex) throws JFireOdaException {
		try {
			return resultSet.getDate(columnIndex);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getDate(java.lang.String)
	 */
	public Date getDate(String columnName) throws JFireOdaException {
		try {
			return resultSet.getDate(columnName);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getDouble(int)
	 */
	public double getDouble(int columnIndex) throws JFireOdaException {
		try {
			return resultSet.getDouble(columnIndex);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getDouble(java.lang.String)
	 */
	public double getDouble(String columnName) throws JFireOdaException {
		try {
			return resultSet.getDouble(columnName);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getInt(int)
	 */
	public int getInt(int columnIndex) throws JFireOdaException {
		try {
			if (resultSet.getMetaData().getColumnType(columnIndex) == Types.BOOLEAN)
				return resultSet.getBoolean(columnIndex) ? 1 : 0;
			else
				return resultSet.getInt(columnIndex);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getInt(java.lang.String)
	 */
	public int getInt(String columnName) throws JFireOdaException {
		try {
			int columnIndex = findColumn(columnName);
			if (resultSet.getMetaData().getColumnType(columnIndex) == Types.BOOLEAN)
				return resultSet.getBoolean(columnIndex) ? 1 : 0;
			else
				return resultSet.getInt(columnIndex);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	private IResultSetMetaData metaData;

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getMetaData()
	 */
	public IResultSetMetaData getMetaData() throws JFireOdaException {
		if (metaData == null) {
			try {
				metaData = createMetaDataFromTable(resultSet);
			} catch (SQLException e) {
				throw new JFireOdaException(e);
			}
		}
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getRow()
	 */
	public int getRow() throws JFireOdaException {
		try {
			return resultSet.getRow();
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getString(int)
	 */
	public String getString(int columnIndex) throws JFireOdaException {
		try {
			return resultSet.getString(columnIndex);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getString(java.lang.String)
	 */
	public String getString(String columnName) throws JFireOdaException {
		try {
			return resultSet.getString(columnName);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getTime(int)
	 */
	public Time getTime(int columnIndex) throws JFireOdaException {
		try {
			return resultSet.getTime(columnIndex);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getTime(java.lang.String)
	 */
	public Time getTime(String columnName) throws JFireOdaException {
		try {
			return resultSet.getTime(columnName);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getTimestamp(int)
	 */
	public Timestamp getTimestamp(int columnIndex) throws JFireOdaException {
		try {
			return resultSet.getTimestamp(columnIndex);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#getTimestamp(java.lang.String)
	 */
	public Timestamp getTimestamp(String columnName) throws JFireOdaException {
		try {
			return resultSet.getTimestamp(columnName);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBoolean(String columnName) throws JFireOdaException {
		try {
			return resultSet.getBoolean(columnName);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBoolean(int columnIndex) throws JFireOdaException {
		try {
			return resultSet.getBoolean(columnIndex);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	public Object getObject(String columnName) throws JFireOdaException {
		try {
			return resultSet.getObject(columnName);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	public Object getObject(int columnIndex) throws JFireOdaException {
		try {
			return resultSet.getObject(columnIndex);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#next()
	 */
	public boolean next() throws JFireOdaException {
		boolean haveNext = false;
		try {
			haveNext = resultSet.next();
			if (!haveNext) {
				int idx = resultSets.indexOf(resultSet)+1;
				if (idx > 0 && idx < resultSets.size()) {
					this.resultSet = resultSets.get(idx);
					return next();
				}
			}
//
//			if (!haveNext) {
//				currentResultSetIdx++;
//				if (currentResultSetIdx > 0 && currentResultSetIdx < resultSets.size()) {
//					while (currentResultSetIdx < resultSets.size() && !haveNext) {
//						haveNext = resultSets.get(currentResultSetIdx).next();
//						currentResultSetIdx++;
//					}
//					if (haveNext)
//						this.resultSet = resultSets.get(currentResultSetIdx);
//					return haveNext;
//				}
//			}
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
		return haveNext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#setMaxRows(int)
	 */
	public void setMaxRows(int rows) throws JFireOdaException {
		try {
			resultSet.setFetchSize(rows);
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IResultSet#wasNull()
	 */
	public boolean wasNull() throws JFireOdaException {
		try {
			return resultSet.wasNull();
		} catch (Exception e) {
			throw new JFireOdaException(e);
		}
	}

	/**
	 * Returns the currently active resultSet
	 * in the list of resultSets managed by this one.
	 *
	 * @return The currently active resultSet.
	 */
	public ResultSet getActiveResultSet() {
		return resultSet;
	}

	/**
	 * Initializes the this resultSet so it can
	 * be iterated from the beginning.
	 * <p>
	 * This method will make the first resultSet
	 * in the list the active one and will set
	 * all contained sets to beforeFirst();
	 *
	 * @throws SQLException
	 */
	public void init() throws SQLException {
		if (resultSets.size() <= 0)
			resultSet = null;
		else {
			resultSet = resultSets.get(0);
			resultSet.beforeFirst();
			for (int i = 1; i < resultSets.size(); i++) {
				resultSets.get(i).beforeFirst();
			}
		}
	}

	/**
	 * Creates an Implementation of {@link IResultSetMetaData} based
	 * on the meta data of the given SQL resultSet.
	 *
	 * @param resultSet The resultSet to map.
	 * @return A new {@link ResultSetMetaData} with columns mapped from the given resultSet.
	 * @throws SQLException
	 */
	public static ResultSetMetaData createMetaDataFromTable(ResultSet resultSet)
	throws SQLException
	{
		ResultSetMetaData metaData = new ResultSetMetaData();
		for (int i=1; i<=resultSet.getMetaData().getColumnCount(); i++) {
			metaData.addColumn(
					resultSet.getMetaData().getColumnName(i),
					DataType.sqlTypeToDataType(resultSet.getMetaData().getColumnType(i)),
					resultSet.getMetaData().isNullable(i)
				);
		}
		return metaData;
	}

}
