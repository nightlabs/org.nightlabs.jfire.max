/**
 * 
 */
package org.nightlabs.ipanema.reporting.oda.jdojs.server;

import org.nightlabs.ipanema.reporting.oda.jdojs.AbstractJDOJSDriver;
import org.nightlabs.ipanema.reporting.oda.jdojs.IJDOJSProxyFactory;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ServerJDOJSDriver extends AbstractJDOJSDriver {

	/* (non-Javadoc)
	 * @see org.nightlabs.ipanema.reporting.oda.jdojs.AbstractJDOJSDriver#createProxyFactory()
	 */
	protected IJDOJSProxyFactory createProxyFactory() {
	return new ServerJDOJSProxyFactory();
	}

}
