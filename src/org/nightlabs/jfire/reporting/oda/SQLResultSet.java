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

import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.IClob;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

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
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#close()
	 */
	public void close() throws OdaException {
		try {
			resultSet.close();
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#findColumn(java.lang.String)
	 */
	public int findColumn(String columnName) throws OdaException {
		try {
			return resultSet.findColumn(columnName);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBigDecimal(int)
	 */
	public BigDecimal getBigDecimal(int columnIndex) throws OdaException {
		try {
			return resultSet.getBigDecimal(columnIndex);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBigDecimal(java.lang.String)
	 */
	public BigDecimal getBigDecimal(String columnName) throws OdaException {
		try {
			return resultSet.getBigDecimal(columnName);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBlob(int)
	 */
	public IBlob getBlob(int columnIndex) throws OdaException {
		if (getMetaData().getColumnType(columnIndex) == DataType.JAVA_OBJECT)
			return new JavaObjectBlob(getObject(columnIndex));
		throw new UnsupportedOperationException("NIY");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getBlob(java.lang.String)
	 */
	public IBlob getBlob(String columnName) throws OdaException {
		if (getMetaData().getColumnType(findColumn(columnName)) == DataType.JAVA_OBJECT)
			return new JavaObjectBlob(getObject(columnName));
		throw new UnsupportedOperationException("NIY");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getClob(int)
	 */
	public IClob getClob(int arg0) throws OdaException {
		throw new UnsupportedOperationException("NIY");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getClob(java.lang.String)
	 */
	public IClob getClob(String arg0) throws OdaException {
		throw new UnsupportedOperationException("NIY");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDate(int)
	 */
	public Date getDate(int columnIndex) throws OdaException {
		try {
			return resultSet.getDate(columnIndex);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDate(java.lang.String)
	 */
	public Date getDate(String columnName) throws OdaException {
		try {
			return resultSet.getDate(columnName);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDouble(int)
	 */
	public double getDouble(int columnIndex) throws OdaException {
		try {
			return resultSet.getDouble(columnIndex);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getDouble(java.lang.String)
	 */
	public double getDouble(String columnName) throws OdaException {
		try {
			return resultSet.getDouble(columnName);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getInt(int)
	 */
	public int getInt(int columnIndex) throws OdaException {
		try {
			if (resultSet.getMetaData().getColumnType(columnIndex) == Types.BOOLEAN)
				return resultSet.getBoolean(columnIndex) ? 1 : 0;
			else
				return resultSet.getInt(columnIndex);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getInt(java.lang.String)
	 */
	public int getInt(String columnName) throws OdaException {
		try {
			int columnIndex = findColumn(columnName);
			if (resultSet.getMetaData().getColumnType(columnIndex) == Types.BOOLEAN)
				return resultSet.getBoolean(columnIndex) ? 1 : 0;
			else
				return resultSet.getInt(columnIndex);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	private IResultSetMetaData metaData;

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getMetaData()
	 */
	public IResultSetMetaData getMetaData() throws OdaException {
		if (metaData == null) {
			try {
				metaData = createMetaDataFromTable(resultSet);
			} catch (SQLException e) {
				throw new OdaException(e);
			}
		}
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getRow()
	 */
	public int getRow() throws OdaException {
		try {
			return resultSet.getRow();
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getString(int)
	 */
	public String getString(int columnIndex) throws OdaException {
		try {
			return resultSet.getString(columnIndex);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getString(java.lang.String)
	 */
	public String getString(String columnName) throws OdaException {
		try {
			return resultSet.getString(columnName);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTime(int)
	 */
	public Time getTime(int columnIndex) throws OdaException {
		try {
			return resultSet.getTime(columnIndex);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTime(java.lang.String)
	 */
	public Time getTime(String columnName) throws OdaException {
		try {
			return resultSet.getTime(columnName);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTimestamp(int)
	 */
	public Timestamp getTimestamp(int columnIndex) throws OdaException {
		try {
			return resultSet.getTimestamp(columnIndex);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#getTimestamp(java.lang.String)
	 */
	public Timestamp getTimestamp(String columnName) throws OdaException {
		try {
			return resultSet.getTimestamp(columnName);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBoolean(String columnName) throws OdaException {
		try {
			return resultSet.getBoolean(columnName);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBoolean(int columnIndex) throws OdaException {
		try {
			return resultSet.getBoolean(columnIndex);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	public Object getObject(String columnName) throws OdaException {
		try {
			return resultSet.getObject(columnName);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	public Object getObject(int columnIndex) throws OdaException {
		try {
			return resultSet.getObject(columnIndex);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#next()
	 */
	public boolean next() throws OdaException {
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
			throw new OdaException(e);
		}
		return haveNext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#setMaxRows(int)
	 */
	public void setMaxRows(int rows) throws OdaException {
		try {
			resultSet.setFetchSize(rows);
		} catch (Exception e) {
			throw new OdaException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IResultSet#wasNull()
	 */
	public boolean wasNull() throws OdaException {
		try {
			return resultSet.wasNull();
		} catch (Exception e) {
			throw new OdaException(e);
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
