/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import org.eclipse.datatools.connectivity.oda.jfire.IConnection;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;
import org.nightlabs.jfire.reporting.oda.Driver;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public abstract class AbstractJFSDriver extends Driver {

	private IJFSQueryProxyFactory proxyFactory;
	
	public AbstractJFSDriver() {
	}
	
	protected abstract IJFSQueryProxyFactory createProxyFactory();
	
	protected IJFSQueryProxyFactory getProxyFactory() {
		if (proxyFactory == null)
			proxyFactory = createProxyFactory();
		return proxyFactory;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDriver#getConnection(java.lang.String)
	 */
	public IConnection getConnection(String dataSourceId) throws JFireOdaException {
		return new JFSConnection(getProxyFactory());
	}

}
