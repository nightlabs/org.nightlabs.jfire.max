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
	 * 
	 */
	public ServerJFSDriver() {
		super();
		System.out.println("********************************************************");
		System.out.println("********************************************************");
		System.out.println("           JFSDriver intantiated");
		System.out.println("********************************************************");
		System.out.println("********************************************************");
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSDriver#createProxyFactory()
	 */
	@Override
	protected IJFSQueryProxyFactory createProxyFactory() {
		return new ServerJFSQueryProxyFactory();
	}

}
