/**
 * 
 */
package org.nightlabs.jfire.reporting.engine.birt;

import java.io.IOException;

import javax.ejb.Remote;
import javax.naming.NamingException;

/**
 * Local interface for initialization.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@Remote
public interface ReportingEngineBirtManagerRemote {

	/**
	 * Initializes the module. Registers the {@link ReportingEngineBirt} and the
	 * {@link IRenderManagerFactory}.
	 * 
	 * @throws IOException When writing/reading temporary data fails.
	 * @throws NamingException Authentication exceptions.
	 */
	void initialise() throws IOException, NamingException;
}
