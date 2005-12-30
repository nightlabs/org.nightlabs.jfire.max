/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jdoql.server;

import java.util.Map;

import org.nightlabs.jfire.reporting.oda.jdoql.IJDOQueryProxy;
import org.nightlabs.jfire.reporting.oda.jdoql.IJDOQueryProxyFactory;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ServerJDOQLProxyFactory implements IJDOQueryProxyFactory {

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jdoql.IJDOQueryProxyFactory#createJDOQueryProxy()
	 */
	public IJDOQueryProxy createJDOQueryProxy(Map connectionProperties) {
//		String organisationID = JDOQLConnection.checkConnectonProperties(connectionProperties);
		return new ServerJDOQLProxy();
	}
	
}
