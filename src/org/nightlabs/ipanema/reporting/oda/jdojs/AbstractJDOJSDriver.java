package org.nightlabs.ipanema.reporting.oda.jdojs;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDriver;
import org.eclipse.datatools.connectivity.oda.LogConfiguration;
import org.eclipse.datatools.connectivity.oda.OdaException;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public abstract class AbstractJDOJSDriver implements IDriver {

	private Object appContext;
	private IJDOJSProxyFactory proxyFactory;
	
	public AbstractJDOJSDriver() {
		System.out.println("*****************************************");
		System.out.println("************ JDOJSDriver instantiated **");
		System.out.println("*****************************************");
	}
	
	protected abstract IJDOJSProxyFactory createProxyFactory();
	
	protected IJDOJSProxyFactory getProxyFactory() {
		if (proxyFactory == null)
			proxyFactory = createProxyFactory();
		return proxyFactory;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDriver#getConnection(java.lang.String)
	 */
	public IConnection getConnection(String dataSourceId) throws OdaException {
		return new JDOJSConnection(getProxyFactory());
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDriver#setLogConfiguration(org.eclipse.datatools.connectivity.oda.LogConfiguration)
	 */
	public void setLogConfiguration(LogConfiguration logConfig) throws OdaException {
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDriver#getMaxConnections()
	 */
	public int getMaxConnections() throws OdaException {
		return 0;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDriver#setAppContext(java.lang.Object)
	 */
	public void setAppContext(Object context) throws OdaException {
		this.appContext = context;
		if (appContext instanceof IJDOJSProxyFactory)
			this.proxyFactory = (IJDOJSProxyFactory)appContext;
		else
			this.proxyFactory = null;
	}
}
