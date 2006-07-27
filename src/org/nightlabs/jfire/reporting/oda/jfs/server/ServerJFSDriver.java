/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs.server;

import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSDriver;
import org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryProxyFactory;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ServerJFSDriver extends AbstractJFSDriver {

	/**
	 * LOG4J logger used by this class
	 */
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(ServerJFSDriver.class);
	
	/**
	 * 
	 */
	public ServerJFSDriver() {
		super();
		if(logger.isDebugEnabled()) {
			logger.debug("********************************************************");
			logger.debug("********************************************************");
			logger.debug("           JFSDriver intantiated");
			logger.debug("********************************************************");
			logger.debug("********************************************************");
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSDriver#createProxyFactory()
	 */
	@Override
	protected IJFSQueryProxyFactory createProxyFactory() {
		return new ServerJFSQueryProxyFactory();
	}

}
