/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.wrapper;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;
import org.eclipse.datatools.connectivity.oda.spec.QuerySpecification;

/**
 * Common implementation of {@link IQuery} that can be (and is) used
 * by all ODA drivers in JFireReporting project.
 * <p>
 * It handles parameters and properties for the query, so the actual
 * driver only has to implement the data acquisition based on these values.
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class QueryWrapper implements IQuery {

	private org.eclipse.datatools.connectivity.oda.jfire.IQuery delegate;

	public QueryWrapper(org.eclipse.datatools.connectivity.oda.jfire.IQuery delegate) {
		super();
		this.delegate = delegate;
	}

	public void clearInParameters() throws OdaException {
		try {
			delegate.clearInParameters();
		} catch (JFireOdaException e) {
			// TODO Auto-generated catch block
			throw new OdaException(e);
		}
	}

	public void close() throws OdaException {
		try {
			delegate.close();
		} catch (JFireOdaException e) {
			// TODO Auto-generated catch block
			throw new OdaException(e);
		}
	}

	public org.eclipse.datatools.connectivity.oda.IResultSet executeQuery() throws OdaException {
		try {
			return new ResultSetWrapper(delegate.executeQuery());
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int findInParameter(String s) throws OdaException {
		try {
			return delegate.findInParameter(s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getMaxRows() throws OdaException {
		try {
			return delegate.getMaxRows();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public org.eclipse.datatools.connectivity.oda.IResultSetMetaData getMetaData() throws OdaException {
		try {
			return new ResultSetMetaData(delegate.getMetaData());
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public org.eclipse.datatools.connectivity.oda.IParameterMetaData getParameterMetaData()
			throws OdaException {
		try {
			return new ParameterMetaDataWrapper(delegate.getParameterMetaData());
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void prepare(String s) throws OdaException {
		try {
			delegate.prepare(s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setAppContext(Object obj) throws OdaException {
		try {
			delegate.setAppContext(obj);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setBigDecimal(int i, BigDecimal bigdecimal)
			throws OdaException {
		try {
			delegate.setBigDecimal(i, bigdecimal);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setBigDecimal(String s, BigDecimal bigdecimal)
			throws OdaException {
		try {
			delegate.setBigDecimal(s, bigdecimal);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setBoolean(int i, boolean flag) throws OdaException {
		try {
			delegate.setBoolean(i, flag);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setBoolean(String s, boolean flag) throws OdaException {
		try {
			delegate.setBoolean(s, flag);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setDate(int i, Date date) throws OdaException {
		try {
			delegate.setDate(i, date);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setDate(String s, Date date) throws OdaException {
		try {
			delegate.setDate(s, date);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setDouble(int i, double d) throws OdaException {
		try {
			delegate.setDouble(i, d);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setDouble(String s, double d) throws OdaException {
		try {
			delegate.setDouble(s, d);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setInt(int i, int j) throws OdaException {
		try {
			delegate.setInt(i, j);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setInt(String s, int i) throws OdaException {
		try {
			delegate.setInt(s, i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setMaxRows(int i) throws OdaException {
		try {
			delegate.setMaxRows(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setNull(int i) throws OdaException {
		try {
			delegate.setNull(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setNull(String s) throws OdaException {
		try {
			delegate.setNull(s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setObject(int i, Object obj) throws OdaException {
		try {
			delegate.setObject(i, obj);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setObject(String s, Object obj) throws OdaException {
		try {
			delegate.setObject(s, obj);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setProperty(String s, String s1) throws OdaException {
		try {
			delegate.setProperty(s, s1);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setString(int i, String s) throws OdaException {
		try {
			delegate.setString(i, s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setString(String s, String s1) throws OdaException {
		try {
			delegate.setString(s, s1);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setTime(int i, Time time) throws OdaException {
		try {
			delegate.setTime(i, time);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setTime(String s, Time time) throws OdaException {
		try {
			delegate.setTime(s, time);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setTimestamp(int i, Timestamp timestamp)
			throws OdaException {
		try {
			delegate.setTimestamp(i, timestamp);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setTimestamp(String s, Timestamp timestamp)
			throws OdaException {
		try {
			delegate.setTimestamp(s, timestamp);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	@Override
	public void cancel() throws OdaException, UnsupportedOperationException {
		// FIXME: Alex: Implement
		throw new UnsupportedOperationException("NYI");
	}

	@Override
	public String getEffectiveQueryText() {
		// FIXME: Alex: Implement
		throw new UnsupportedOperationException("NYI");
	}

	@Override
	public SortSpec getSortSpec() throws OdaException {
		// FIXME: Alex: Implement
		throw new UnsupportedOperationException("NYI");
	}

	@Override
	public QuerySpecification getSpecification() {
		// FIXME: Alex: Implement
		return null;
	}

	@Override
	public void setSortSpec(SortSpec sortBy) throws OdaException {
		// FIXME: Alex: Implement
		throw new UnsupportedOperationException("NYI");
	}

	@Override
	public void setSpecification(QuerySpecification querySpec)
			throws OdaException, UnsupportedOperationException {
		// FIXME: Alex: Implement
		throw new UnsupportedOperationException("NYI");
	}
	
	
	
}
