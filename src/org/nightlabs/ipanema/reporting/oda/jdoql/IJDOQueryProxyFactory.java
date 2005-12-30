package org.nightlabs.ipanema.reporting.oda.jdoql;

import java.util.Map;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public interface IJDOQueryProxyFactory {
	
	public IJDOQueryProxy createJDOQueryProxy(Map proxyProperties);
}
