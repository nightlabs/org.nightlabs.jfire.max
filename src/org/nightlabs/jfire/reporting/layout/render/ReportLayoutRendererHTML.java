/**
 * 
 */
package org.nightlabs.jfire.reporting.layout.render;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import javax.jdo.PersistenceManager;

import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderContext;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.nightlabs.jfire.reporting.Birt;
import org.nightlabs.jfire.reporting.Birt.OutputFormat;

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

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.layout.render.ReportLayoutRenderer#renderReport(javax.jdo.PersistenceManager, org.nightlabs.jfire.reporting.layout.render.RenderReportRequest, org.eclipse.birt.report.engine.api.IRunAndRenderTask, java.lang.String, java.io.File, boolean)
	 */
	@SuppressWarnings("unchecked")
	public RenderedReportLayout renderReport(PersistenceManager pm,
			RenderReportRequest renderRequest,
			IRunAndRenderTask task,
			String fileName, File layoutRoot,
			boolean prepareForTransfer
		)
	throws EngineException
	{
		if (renderRequest.getOutputFormat() != Birt.OutputFormat.html)
			throw new IllegalArgumentException(this.getClass().getName()+" was asked to render a report to format "+renderRequest.getOutputFormat()+" altough it is registered to "+getOutputFormat());
		
		
		HTMLRenderOption options = new HTMLRenderOption( );
		options.setOutputFormat(renderRequest.getOutputFormat().toString());
		
		HTMLRenderContext renderContext = new HTMLRenderContext( );
		renderContext.setImageDirectory(layoutRoot.getAbsolutePath().toString()+File.separator+"images");
		renderContext.setBaseImageURL("images"); //$NON-NLS-1$

		HashMap appContext = new HashMap( );
		appContext.put(EngineConstants.APPCONTEXT_HTML_RENDER_CONTEXT, renderContext);
		task.setAppContext(appContext);
		
		options.setOutputFileName(layoutRoot.getAbsolutePath().toString()+File.separator+fileName+".html");
//		options.setOutputStream(outputStream);
		task.setRenderOption(options);
		
		task.setParameterValues(renderRequest.getParameters());
		
		task.run();
		
		RenderedReportLayout result = new RenderedReportLayout(renderRequest.getReportRegistryItemID(), renderRequest.getOutputFormat(), new Date());
		if (prepareForTransfer)
			ReportLayoutRendererUtil.prepareRenderedLayoutForTransfer(layoutRoot, result, fileName, true);
		
		return result;
	}

}
