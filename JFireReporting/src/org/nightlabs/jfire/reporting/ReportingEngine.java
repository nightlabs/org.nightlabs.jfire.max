/**
 * 
 */
package org.nightlabs.jfire.reporting;

import java.io.Serializable;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.reporting.id.ReportingEngineID;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.render.IRenderManager;
import org.nightlabs.jfire.reporting.layout.render.RenderReportRequest;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * {@link ReportingEngine}s represent the link to a report-engine back-end (like BIRT) that can be
 * used to render {@link ReportLayout}s. A {@link ReportingEngine} is identfied by its engine-type,
 * i.e. the engine-type is used to lookup a {@link ReportingEngine} when it is needed.
 * <p>
 * Reporting engines are used to obtain {@link IRenderManager}s to which the work of rendering a
 * report is delegated.
 * </p>
 * <p>
 * To register a new {@link ReportingEngine} persist a subclass of this in the JFire datastore with
 * the appropriate engineType.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@PersistenceCapable(
		objectIdClass=ReportingEngineID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireReporting_ReportingEngine")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class ReportingEngine implements Serializable {

	private static final long serialVersionUID = 20100106L;

	@PrimaryKey
	private String organisationID;

	/** Identifier of an engine-type. This is used to lookup a {@link ReportingEngine} for a certain ReportLayout */
	@PrimaryKey
	private String engineType;
	
	/**
	 * @deprecated Only for JDO
	 */
	protected ReportingEngine() {
		super();
	}

	/**
	 * Constructs a new {@link ReportingEngine}.
	 * 
	 * @param organisationID The organisationID of the engine.
	 * @param engineType The engineType of the engine.
	 */
	protected ReportingEngine(String organisationID, String engineType) {
		this.organisationID = organisationID;
		this.engineType = engineType;
	}
	
	/**
	 * {@link ReportingEngine}s should provide a new {@link IRenderManager} and prepare it for
	 * processing the given {@link RenderReportRequest}. Implementations might return different
	 * types of {@link IRenderManager}s for different output-formats of the request.
	 * 
	 * @return A new instance of {@link IRenderManager} ready process a {@link RenderReportRequest},
	 */
	public abstract IRenderManager createRenderManager(RenderReportRequest renderReportRequest);
	
	
	/**
	 * @return The organisationID pk-part of this {@link ReportingEngine}.
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Returns the engineType pk-part of this {@link ReportingEngine}. A {@link ReportingEngine}s
	 * engineType is its main identification attribute and is used to lookup an engine for a certain
	 * {@link ReportLayout}.
	 * 
	 * @return The engineType pk-part of this {@link ReportingEngine}.
	 */
	public String getEngineType() {
		return engineType;
	}

	/**
	 * Returns the {@link ReportingEngine} of the given type, or <code>null</code> when it can not
	 * be found. Note that this method will fallback to the engine-type "BIRT" if an empty type is
	 * given.
	 * 
	 * @param pm {@link PersistenceManager} to use.
	 * @param engineType The engine-type a {@link ReportingEngine} should be searched for. 
	 * @return The {@link ReportingEngine} of the given type, or <code>null</code>
	 *            when it can not be found
	 */
	public static ReportingEngine getReportingEngine(PersistenceManager pm, String engineType) {
		String searchType = engineType;
		if (engineType == null || engineType.isEmpty())
			searchType = "BIRT";
		ReportingEngineID reportingEngineID = ReportingEngineID.create(SecurityReflector.getUserDescriptor().getOrganisationID(),
				searchType);
		try {
			return (ReportingEngine) pm.getObjectById(reportingEngineID);
		} catch (JDOObjectNotFoundException e) {
			return null;
		}
	}
}
