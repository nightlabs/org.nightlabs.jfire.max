/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.reporting;

import java.io.File;
import java.io.Serializable;
import java.util.logging.Level;

import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.eclipse.birt.core.framework.IPlatformContext;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.HTMLCompleteImageHandler;
import org.eclipse.birt.report.engine.api.HTMLEmitterConfig;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.ReportEngine;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.render.RenderManager;
import org.nightlabs.jfire.reporting.platform.ServerPlatformContext;
import org.nightlabs.jfire.reporting.platform.ServerResourceLocator;
import org.nightlabs.util.Util;

/**
 * {@link ReportingManagerFactory} is the entry point for operations with 
 * stored {@link ReportLayout}s. One instance of the factory is bound to JNDI
 * for each organisation (see {@link #ReportingManagerFactory(InitialContext, String)}).
 * <p>
 * Currently the factory allows access to a configured BIRT {@link ReportEngine} that
 * should be used to create new engine tasks. Additionally the factory can serve  
 * {@link RenderManager} that help rendering reports according to user-given parameters. 
 * 
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ReportingManagerFactory implements Serializable {
	
	/**
	 * The Log4J Logger used for this class.
	 */
	private static final Logger logger = Logger.getLogger(ReportingManagerFactory.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String JNDI_PREFIX = "java:/jfire/reportingManagerFactory/";
	
	private ReportEngine reportEngine;
	
	/**
	 * Creates a new ReportingManagerFactory and binds it into JNDI with
	 * {@link #JNDI_PREFIX} followed by the given organisatinID as name.
	 * 
	 * @param ctx The initial context to use.
	 * @param organisationID The organisationID this factory should be used for
	 * @throws NamingException 
	 */
	public ReportingManagerFactory(InitialContext ctx, String organisationID)
	throws NamingException
	{
		try {
			ctx.createSubcontext("java:/jfire");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		try {
			ctx.createSubcontext("java:/jfire/reportingManagerFactory");
		} catch (NameAlreadyBoundException e) {
			// ignore
		}

		ctx.bind(getJNDIName(organisationID), this);
	}

	/**
	 * Get (and create if necessary) the ReportEngine for this factory (organisation).
	 * {@link ReportEngine}'s methods hopefully are threadsafe and can be called without
	 * any additional care.
	 */
	public ReportEngine getReportEngine() {
		if (reportEngine == null) {
			try {
				EngineConfig config = new EngineConfig( );
				config.setPlatformContext(new ServerPlatformContext());
				// TODO: Add configuration for other formats/emitters as well -> the appropriate ReportLayoutRenderer configure it

//				Create the emitter configuration.
				HTMLEmitterConfig hc = new HTMLEmitterConfig( );
//				Use the "HTML complete" image handler to write the files to disk.
				HTMLCompleteImageHandler imageHandler = new HTMLCompleteImageHandler( );
				hc.setImageHandler( imageHandler );
//				Associate the configuration with the HTML output format.
				config.setEmitterConfiguration( IRenderOption.OUTPUT_FORMAT_HTML, hc );			
				config.setLogConfig(new File(JFireReportingEAR.getEARDir(), "log").getAbsolutePath(), Level.ALL);
				reportEngine = new ReportEngine(config);
				reportEngine.getConfig().setResourceLocator(new ServerResourceLocator());
//				reportEngine.setLogger();
			} catch (Exception e) {
				logger.error("Could not create ReportEngine", e);
				reportEngine = null;
			}
		}
		return reportEngine;
	}
	
	/**
	 * Obtain a new instance of {@link RenderManager} as a helper to render reports.
	 * 
	 * @return A new instance of {@link RenderManager}.
	 */
	public RenderManager createRenderManager() {
		return new RenderManager(this);
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

	public static void main(String[] args) {
		EngineConfig config = new EngineConfig( );
//		config.setPlatformContext(new ServerPlatformContext());
		config.setPlatformContext(new IPlatformContext() {
			public String getPlatform() {
				return (new File("dist/birt")).getAbsolutePath();
			}
		});

		// TODO: Add configuration for other formats/emitters as well -> the appropriate ReportLayoutRenderer configure it
		long time = System.currentTimeMillis();
//		Create the emitter configuration.
		HTMLEmitterConfig hc = new HTMLEmitterConfig( );
//		Use the "HTML complete" image handler to write the files to disk.
		HTMLCompleteImageHandler imageHandler = new HTMLCompleteImageHandler( );
		hc.setImageHandler( imageHandler );
//		Associate the configuration with the HTML output format.
		config.setEmitterConfiguration( IRenderOption.OUTPUT_FORMAT_HTML, hc );			
		ReportEngine reportEngine = new ReportEngine(config);
		reportEngine.getConfig().setResourceLocator(new ServerResourceLocator());
		System.out.println(Util.getTimeDiffString(time));
	}
	
}
