/**
 * 
 */
package org.nightlabs.jfire.reporting.oda;

import org.eclipse.datatools.connectivity.oda.IDriver;
import org.eclipse.datatools.connectivity.oda.LogConfiguration;
import org.eclipse.datatools.connectivity.oda.OdaException;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public abstract class Driver implements IDriver {

	private Object appContext;
	private LogConfiguration logConfiguration;
	
	/**
	 * 
	 */
	public Driver() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDriver#getMaxConnections()
	 */
	public int getMaxConnections() throws OdaException {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDriver#setAppContext(java.lang.Object)
	 */
	public void setAppContext(Object context) throws OdaException {
		this.appContext = context;
	}
	
	/**
	 * Returns the appContext set by {@link #setAppContext(Object)}
	 */
	public Object getAppContext() {
		return appContext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDriver#setLogConfiguration(org.eclipse.datatools.connectivity.oda.LogConfiguration)
	 */
	public void setLogConfiguration(LogConfiguration logConfiguration) throws OdaException {
		this.logConfiguration = logConfiguration;
	}
	
	/**
	 * Returns the logConfiguration set by {@link #setLogConfiguration(LogConfiguration)}
	 */
	public LogConfiguration getLogConfiguration() {
		return logConfiguration;
	}

}
