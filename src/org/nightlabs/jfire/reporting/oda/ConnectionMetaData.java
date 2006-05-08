/**
 * 
 */
package org.nightlabs.jfire.reporting.oda;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.OdaException;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ConnectionMetaData implements IDataSetMetaData {

	private IConnection connection;
	
	public ConnectionMetaData(IConnection connection) {
		this.connection = connection;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getConnection()
	 */
	public IConnection getConnection() throws OdaException {
		return connection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getDataSourceMajorVersion()
	 */
	public int getDataSourceMajorVersion() throws OdaException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getDataSourceMinorVersion()
	 */
	public int getDataSourceMinorVersion() throws OdaException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getDataSourceObjects(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public IResultSet getDataSourceObjects(String arg0, String arg1,
			String arg2, String arg3) throws OdaException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getDataSourceProductName()
	 */
	public String getDataSourceProductName() throws OdaException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getDataSourceProductVersion()
	 */
	public String getDataSourceProductVersion() throws OdaException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getSQLStateType()
	 */
	public int getSQLStateType() throws OdaException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#getSortMode()
	 */
	public int getSortMode() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#supportsInParameters()
	 */
	public boolean supportsInParameters() throws OdaException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#supportsMultipleOpenResults()
	 */
	public boolean supportsMultipleOpenResults() throws OdaException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#supportsMultipleResultSets()
	 */
	public boolean supportsMultipleResultSets() throws OdaException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#supportsNamedParameters()
	 */
	public boolean supportsNamedParameters() throws OdaException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#supportsNamedResultSets()
	 */
	public boolean supportsNamedResultSets() throws OdaException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDataSetMetaData#supportsOutParameters()
	 */
	public boolean supportsOutParameters() throws OdaException {
		// TODO Auto-generated method stub
		return false;
	}

}
