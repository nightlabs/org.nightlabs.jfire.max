/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import java.util.Map;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public interface IJFSQueryProxyFactory {
	
	public IJFSQueryProxy createJFSQueryProxy(Map proxyProperties);

}
