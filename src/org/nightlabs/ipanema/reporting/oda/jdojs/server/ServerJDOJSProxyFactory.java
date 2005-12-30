/**
 * 
 */
package org.nightlabs.ipanema.reporting.oda.jdojs.server;

import java.util.Map;

import org.nightlabs.ipanema.reporting.oda.jdojs.IJDOJSProxy;
import org.nightlabs.ipanema.reporting.oda.jdojs.IJDOJSProxyFactory;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ServerJDOJSProxyFactory implements IJDOJSProxyFactory {

	/* (non-Javadoc)
	 * @see org.nightlabs.ipanema.reporting.oda.jdojs.IJDOJSProxyFactory#createJDOJavaScriptProxy(java.util.Map)
	 */
	public IJDOJSProxy createJDOJavaScriptProxy(Map proxyProperties) {
		return new ServerJDOJSProxy(); // JDOJSConnection.checkConnectonProperties(proxyProperties));
	}

}
