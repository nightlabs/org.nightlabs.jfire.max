/**
 * 
 */
package org.nightlabs.jfire.reporting.engine.birt;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.reporting.ReportingEngine;
import org.nightlabs.jfire.reporting.layout.render.IRenderManager;
import org.nightlabs.jfire.reporting.layout.render.RenderReportRequest;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireReportingEngineBirt_ReportingEngineBirt")
	@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class ReportingEngineBirt extends ReportingEngine {

	private static final long serialVersionUID = 20100107L;
	
	/**
	 * Constant for the engine-type "BIRT"
	 */
	public static final String ENGINE_TYPE_BIRT = "BIRT";
	
	/**
	 * @param organisationID
	 * @param engineType
	 */
	public ReportingEngineBirt(String organisationID) {
		super(organisationID, ENGINE_TYPE_BIRT);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportingEngine#createRenderManager(org.nightlabs.jfire.reporting.layout.render.RenderReportRequest)
	 */
	@Override
	public IRenderManager createRenderManager(RenderReportRequest renderReportRequest) {
		IRenderManagerFactory renderManagerFactory = RenderManagerFactory.getRenderManagerFactory(SecurityReflector.getUserDescriptor().getOrganisationID());
		return renderManagerFactory.createRenderManager(renderReportRequest);
	}

}
