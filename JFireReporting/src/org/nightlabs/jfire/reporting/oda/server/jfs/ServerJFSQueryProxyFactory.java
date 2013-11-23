/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.server.jfs;

import java.util.Map;

import org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryProxy;
import org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryProxyFactory;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ServerJFSQueryProxyFactory implements IJFSQueryProxyFactory {

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryProxyFactory#createJFSQueryProxy(java.util.Map)
	 */
	public IJFSQueryProxy createJFSQueryProxy(Map proxyProperties) {
		return new ServerJFSQueryProxy(proxyProperties);
	}

}
