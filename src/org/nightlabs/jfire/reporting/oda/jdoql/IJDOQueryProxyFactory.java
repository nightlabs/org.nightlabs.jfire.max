package org.nightlabs.jfire.reporting.oda.jdoql;

import java.util.Map;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public interface IJDOQueryProxyFactory {
	
	public IJDOQueryProxy createJDOQueryProxy(Map proxyProperties);
}
