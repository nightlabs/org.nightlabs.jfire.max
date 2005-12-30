/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jdoql.server;

import org.nightlabs.jfire.reporting.oda.jdoql.AbstractJDOQLDriver;
import org.nightlabs.jfire.reporting.oda.jdoql.IJDOQueryProxyFactory;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ServerJDOQLDriver extends AbstractJDOQLDriver {

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jdoql.AbstractJDOQLDriver#createProxyFactory()
	 */
	protected IJDOQueryProxyFactory createProxyFactory() {
		return new ServerJDOQLProxyFactory();
	}

}
