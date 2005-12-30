/**
 * 
 */
package org.nightlabs.ipanema.reporting.platform;

import java.io.Serializable;

import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.HTMLCompleteImageHandler;
import org.eclipse.birt.report.engine.api.HTMLEmitterConfig;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.ReportEngine;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ReportingManagerFactory implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String JNDI_PREFIX = "java:/ipanema/reportingManagerFactory/";
	
	private ReportEngine reportEngine;
	
	public ReportingManagerFactory(InitialContext ctx, String organisationID)
	throws NamingException
	{
		try {
			ctx.createSubcontext("java:/ipanema");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		try {
			ctx.createSubcontext("java:/ipanema/reportingManagerFactory");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		ctx.bind(getJNDIName(organisationID), this);
	}

	/**
	 * Get (and create if neccessary) the ReportEngine for this factory (organisation).
	 */
	public ReportEngine getReportEngine() {
		if (reportEngine == null) {
			EngineConfig config = new EngineConfig( );

//			 Create the emitter configuration.
			HTMLEmitterConfig hc = new HTMLEmitterConfig( );
			

//			 Use the "HTML complete" image handler to write the files to disk.
			HTMLCompleteImageHandler imageHandler = new HTMLCompleteImageHandler( );
			hc.setImageHandler( imageHandler );

//			 Associate the configuration with the HTML output format.
			config.setEmitterConfiguration( HTMLRenderOption.OUTPUT_FORMAT_HTML, hc );			
			reportEngine = new ReportEngine(config);			
		}
		return reportEngine;
	}
	
	public ReportingManager getReportingManager() {
		return new ReportingManager(this);
	}
	
	
	/**
	 * Returns the JNDI prefix of the ReportingManagerFactory for the given organisationID. 
	 */
	public static String getJNDIName(String organisationID)
	{
		return JNDI_PREFIX + organisationID;
	}

	/**
	 * Returns the ReportingManagerFactory for the given organisationID out of the JNDI
	 * if one was bound.
	 * 
	 * @param ctx The InitialContext to use
	 * @param organisationID The organisationID the engine should be looked up for
	 * @return The ReportingManagerFactory for the given organisationID out of JNDI
	 * @throws NamingException
	 */
	public static final ReportingManagerFactory getReportingManagerFactory(InitialContext ctx, String organisationID)
	throws NamingException
	{
		return (ReportingManagerFactory) ctx.lookup(getJNDIName(organisationID));
	}	 

}
