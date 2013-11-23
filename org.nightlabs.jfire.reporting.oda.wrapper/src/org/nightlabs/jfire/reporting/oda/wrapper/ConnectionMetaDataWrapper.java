/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.wrapper;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ConnectionMetaDataWrapper implements IDataSetMetaData {

	private org.eclipse.datatools.connectivity.oda.jfire.IDataSetMetaData delegate;
	private IConnection connection;
	
	public ConnectionMetaDataWrapper(IConnection connection, org.eclipse.datatools.connectivity.oda.jfire.IDataSetMetaData delegate) {
		this.delegate = delegate;
		this.connection = connection;
	}

	public IConnection getConnection()
			throws OdaException {
		return connection;
	}
	
	public int getDataSourceMajorVersion() throws OdaException {
		try {
			return delegate.getDataSourceMajorVersion();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getDataSourceMinorVersion() throws OdaException {
		try {
			return delegate.getDataSourceMinorVersion();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public IResultSet getDataSourceObjects(
			String s, String s1, String s2, String s3) throws OdaException {
		return null;
	}

	public String getDataSourceProductName() throws OdaException {
		return null;
	}

	public String getDataSourceProductVersion() throws OdaException {
		return null;
	}

	public int getSortMode() {
		return delegate.getSortMode();
	}

	public int getSQLStateType() throws OdaException {
		try {
			return delegate.getSQLStateType();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public boolean supportsInParameters() throws OdaException {
		try {
			return delegate.supportsInParameters();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public boolean supportsMultipleOpenResults() throws OdaException {
		try {
			return delegate.supportsMultipleOpenResults();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public boolean supportsMultipleResultSets() throws OdaException {
		try {
			return delegate.supportsMultipleResultSets();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public boolean supportsNamedParameters() throws OdaException {
		try {
			return delegate.supportsNamedParameters();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public boolean supportsNamedResultSets() throws OdaException {
		try {
			return delegate.supportsNamedResultSets();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public boolean supportsOutParameters() throws OdaException {
		try {
			return delegate.supportsOutParameters();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}
	
	
}
