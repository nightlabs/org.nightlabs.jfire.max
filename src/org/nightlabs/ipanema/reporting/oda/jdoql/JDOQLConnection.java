/**
 * 
 */
package org.nightlabs.ipanema.reporting.oda.jdoql;

import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;

import org.nightlabs.ipanema.reporting.oda.JDOConnection;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class JDOQLConnection extends JDOConnection {
	
	private IJDOQueryProxyFactory proxyFactory;
	
	public JDOQLConnection(IJDOQueryProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#newQuery(java.lang.String)
	 */
	public IQuery newQuery(String dataSetType) throws OdaException {
		if (getCurrentProperties() == null)
			throw new IllegalStateException("Connection properties are not assigned. Maybe open() was never called!");
		return proxyFactory.createJDOQueryProxy(getCurrentProperties());
	}

}
