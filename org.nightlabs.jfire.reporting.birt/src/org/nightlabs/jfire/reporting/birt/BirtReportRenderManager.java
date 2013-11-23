package org.nightlabs.jfire.reporting.birt;

import java.io.Serializable;

import javax.jdo.PersistenceManager;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.HTMLCompleteImageHandler;
import org.eclipse.birt.report.engine.api.HTMLEmitterConfig;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.ReportEngine;
import org.nightlabs.jfire.reporting.Birt;
import org.nightlabs.jfire.reporting.birt.layout.render.RenderManager;
import org.nightlabs.jfire.reporting.birt.layout.render.ReportLayoutRendererGeneric;
import org.nightlabs.jfire.reporting.birt.layout.render.ReportLayoutRendererHTML;
import org.nightlabs.jfire.reporting.birt.layout.render.ReportLayoutRendererPDF;
import org.nightlabs.jfire.reporting.birt.platform.ServerPlatformContext;
import org.nightlabs.jfire.reporting.birt.platform.ServerResourceLocator;
import org.nightlabs.jfire.reporting.classloader.ReportingClassLoader;
import org.nightlabs.jfire.reporting.engine.birt.IRenderManagerFactory;
import org.nightlabs.jfire.reporting.engine.birt.ReportLayoutRendererRegistry;
import org.nightlabs.jfire.reporting.layout.render.IRenderManager;
import org.nightlabs.jfire.reporting.layout.render.RenderReportRequest;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class BirtReportRenderManager
implements IRenderManagerFactory, Serializable
{
	private static final long serialVersionUID = 20100111L;
	
	private ReportEngine reportEngine;

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.IRenderManagerFactory#createRenderManager(org.nightlabs.jfire.reporting.birt.layout.render.RenderReportRequest)
	 */
	@Override
	public IRenderManager createRenderManager(RenderReportRequest renderReportRequest) {
		initReportEngine();
		return new RenderManager(reportEngine);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.IReportEngineInitaliser#createReportEngine()
	 */
	protected void initReportEngine() {
		if (reportEngine == null) {
			reportEngine = _createReportEngine();
		}
	}

	private ReportEngine _createReportEngine()
	{
		ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(ReportingClassLoader.sharedInstance());

			EngineConfig config = new EngineConfig( );
			config.setPlatformContext(new ServerPlatformContext());
			config.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, ReportingClassLoader.sharedInstance());

			try {
				Platform.startup(config);
			} catch (BirtException e) {
				throw new RuntimeException(e);
			}
			// set the OSGI Classloader
			// There is nothing like *the* OSGI classloader. What we set here, was the *Context*ClassLoader. The
			// class ContextFinder, which is by default OSGI's Thread-ContextClassLoader, tries to find out the
			// current OSGI-bundle (i.e. plugin) by analysing the current call stack.
			// That's why it doesn't work as intended (the call stack leads to JBoss' UnifiedClassLoaders to be found, only).
			// What we would need here to use OSGI correctly would be a ClassLoader that finds dependencies from BIRT's plugin
			// and asks these bundle-class-loaders. Thus, we take the easier route of simply putting all JARs into our
			// CL - at least temporarily. Marco & Daniel.
//			ReportingClassLoader.sharedInstance().setOsgiClassLoader(Platform.getContextClassLoader());
//			IBundle bundle = Platform.getBundle("org.nightlabs.jfire.reporting.birt.classloader");

			// TODO: Add configuration for other formats/emitters as well -> the appropriate ReportLayoutRenderer configure it

			// Create the emitter configuration.
			HTMLEmitterConfig hc = new HTMLEmitterConfig( );
			// Use the "HTML complete" image handler to write the files to disk.
			HTMLCompleteImageHandler imageHandler = new HTMLCompleteImageHandler( );
			hc.setImageHandler( imageHandler );
			// Associate the configuration with the HTML output format.
			config.setEmitterConfiguration( IRenderOption.OUTPUT_FORMAT_HTML, hc );
			ReportEngine reportEngine = new ReportEngine(config);
			reportEngine.getConfig().setResourceLocator(new ServerResourceLocator());

			return reportEngine;
		} finally {
			Thread.currentThread().setContextClassLoader(originalContextClassLoader);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.IRenderManagerFactory#initialize(javax.jdo.PersistenceManager)
	 */
	@Override
	public void initialize(PersistenceManager pm) {
		try {
			ReportLayoutRendererRegistry reportLayoutRendererRegistry = ReportLayoutRendererRegistry.getReportRegistry(pm);
			reportLayoutRendererRegistry.registerReportRenderer(Birt.OutputFormat.html.toString(), ReportLayoutRendererHTML.class);
			reportLayoutRendererRegistry.registerReportRenderer(Birt.OutputFormat.pdf.toString(), ReportLayoutRendererPDF.class);
			for (Birt.OutputFormat format : Birt.OutputFormat.values()) {
				if (format != Birt.OutputFormat.html && format != Birt.OutputFormat.pdf) {
					reportLayoutRendererRegistry.registerReportRenderer(format.toString(), ReportLayoutRendererGeneric.class);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
