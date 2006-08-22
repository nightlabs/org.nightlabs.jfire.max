/**
 * 
 */
package org.nightlabs.jfire.reporting.layout.render;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import javax.jdo.PersistenceManager;
import javax.naming.NamingException;

import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderContext;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.nightlabs.ModuleException;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.base.InitException;
import org.nightlabs.jfire.reporting.Birt;
import org.nightlabs.jfire.reporting.JFireReportingEAR;
import org.nightlabs.jfire.reporting.Birt.OutputFormat;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.servermanager.JFireServerManagerUtil;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector;
import org.nightlabs.util.Utils;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ReportLayoutRendererHTML implements ReportLayoutRenderer {

	/**
	 * 
	 */
	public ReportLayoutRendererHTML() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.layout.ReportLayoutRenderer#getOutputFormat()
	 */
	public OutputFormat getOutputFormat() {
		return Birt.OutputFormat.html;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.layout.ReportLayoutRenderer#renderReport(javax.jdo.PersistenceManager, org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID, org.eclipse.birt.report.engine.api.IRunAndRenderTask, java.util.Map, org.nightlabs.jfire.reporting.Birt.OutputFormat)
	 */
	public RenderedReportLayout renderReport(PersistenceManager pm,
			ReportRegistryItemID reportRegistryItemID, IRunAndRenderTask task,
			Map<String, Object> parsedParams, OutputFormat format
		)
	throws EngineException
	{
		if (format != Birt.OutputFormat.html)
			throw new IllegalArgumentException(this.getClass().getName()+" was asked to render a report to format "+format+" altough it is registered to "+getOutputFormat());
		File layoutRoot = ReportLayoutRendererUtil.prepareRenderedLayoutOutputFolder();
		
		HTMLRenderOption options = new HTMLRenderOption( );
		options.setOutputFormat(format.toString());
		
		HTMLRenderContext renderContext = new HTMLRenderContext( );
		renderContext.setImageDirectory(layoutRoot.getAbsolutePath().toString()+File.separator+"images");
		renderContext.setBaseImageURL("images"); //$NON-NLS-1$

		HashMap appContext = new HashMap( );
		appContext.put(EngineConstants.APPCONTEXT_HTML_RENDER_CONTEXT, renderContext);
		task.setAppContext( appContext );
		
		options.setOutputFileName(layoutRoot.getAbsolutePath().toString()+File.separator+"renderedLayout.html");
//		options.setOutputStream(outputStream);
		task.setRenderOption( options );
		
		task.setParameterValues(parsedParams);
		
		task.run();
		
		RenderedReportLayout result = new RenderedReportLayout(reportRegistryItemID, format, new Date());
		ReportLayoutRendererUtil.prepareRenderedLayoutForTransfer(layoutRoot, result, true);
		
		return result;
	}

}
