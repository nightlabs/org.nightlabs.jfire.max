/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.nightlabs.jfire.reporting.oda.Connection;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class JFSConnection extends Connection {

	private IJFSQueryProxyFactory proxyFactory;
	
	/**
	 * 
	 */
	public JFSConnection(IJFSQueryProxyFactory proxyFactory) {
		super();
		this.proxyFactory = proxyFactory;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#newQuery(java.lang.String)
	 */
	public IQuery newQuery(String arg0) throws OdaException {
		if (getConnectionProperties() == null)
			throw new IllegalStateException("Connection properties are not assigned. Maybe open() was never called!");
		return proxyFactory.createJFSQueryProxy(getConnectionProperties());
	}

}
