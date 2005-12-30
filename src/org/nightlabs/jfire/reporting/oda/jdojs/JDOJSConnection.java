/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jdojs;

import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;

import org.nightlabs.jfire.reporting.oda.JDOConnection;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class JDOJSConnection extends JDOConnection {
	
	private IJDOJSProxyFactory proxyFactory;
	
	public JDOJSConnection(IJDOJSProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#newQuery(java.lang.String)
	 */
	public IQuery newQuery(String dataSetType) throws OdaException {
		if (getCurrentProperties() == null)
			throw new IllegalStateException("Connection properties are not assigned. Maybe open() was never called!");
		return proxyFactory.createJDOJavaScriptProxy(getCurrentProperties());
	}

}
