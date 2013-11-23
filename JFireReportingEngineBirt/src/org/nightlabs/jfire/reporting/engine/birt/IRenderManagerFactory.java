/**
 * 
 */
package org.nightlabs.jfire.reporting.engine.birt;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.render.IRenderManager;
import org.nightlabs.jfire.reporting.layout.render.RenderReportRequest;

/**
 * An implementation of this factory is bound to JNDI once per organisation 
 * in a server instance and is responsible for providing instances of {@link IRenderManager}s
 * that render {@link ReportLayout}s.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface IRenderManagerFactory {
	
	/**
	 * This method is called once when the factory is created
	 * and bound to JNDI. Implementations can perform initialization
	 * here and ensure their prerequisites are fulfilled.  
	 * 
	 * @param pm The {@link PersistenceManager} to access the datastore with.
	 */
	void initialize(PersistenceManager pm);
	
	/**
	 * After the {@link IRenderManagerFactory} is obtained from JNDI
	 * it is asked to provide a new {@link IRenderManager} and prepare
	 * it ready for processing the given {@link RenderReportRequest}.
	 * Implementations might return different types of {@link IRenderManager}s
	 * for different output-formats of the request.
	 * 
	 * @return A new instance of {@link IRenderManager} ready process a {@link RenderReportRequest},
	 */
	IRenderManager createRenderManager(RenderReportRequest renderReportRequest);
}
