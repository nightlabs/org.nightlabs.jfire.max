/**
 *
 */
package org.nightlabs.jfire.reporting.birt.layout.render;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import javax.jdo.PersistenceManager;

import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderContext;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.nightlabs.jfire.reporting.engine.birt.ReportLayoutEngineException;
import org.nightlabs.jfire.reporting.engine.birt.ReportLayoutRenderer;
import org.nightlabs.jfire.reporting.layout.render.RenderReportRequest;
import org.nightlabs.jfire.reporting.layout.render.RenderedReportLayout;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererUtil;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ReportLayoutRendererGeneric implements ReportLayoutRenderer {

	/**
	 *
	 */
	public ReportLayoutRendererGeneric() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.birt.layout.render.ReportLayoutRenderer#renderReport(javax.jdo.PersistenceManager, org.nightlabs.jfire.reporting.birt.layout.render.RenderReportRequest, org.eclipse.birt.report.engine.api.IRunAndRenderTask, java.lang.String, java.io.File, boolean)
	 */
	@SuppressWarnings("unchecked")
	public RenderedReportLayout renderReport(PersistenceManager pm,
			RenderReportRequest renderRequest,
//			IRunAndRenderTask task,
			Object tsk,
			String fileName, File layoutRoot,
			boolean prepareForTransfer
		)
//	throws EngineException
	throws ReportLayoutEngineException
	{
		IRunAndRenderTask task = (IRunAndRenderTask) tsk;
		IRenderOption options = createRenderOption(renderRequest, task, fileName, layoutRoot);
		task.setRenderOption(options);

		HTMLRenderContext renderContext = new HTMLRenderContext( );
		renderContext.setImageDirectory(layoutRoot.getAbsolutePath().toString()+File.separator+"images");
		renderContext.setBaseImageURL("images"); //$NON-NLS-1$

		HashMap appContext = new HashMap( );
		appContext.put(EngineConstants.APPCONTEXT_HTML_RENDER_CONTEXT, renderContext);
		task.setAppContext(appContext);

		task.setParameterValues(renderRequest.getParameters());

		try {
			task.run();
		} catch (EngineException e) {
			throw new ReportLayoutEngineException(e);
		}

		RenderedReportLayout result = new RenderedReportLayout(renderRequest.getReportRegistryItemID(), renderRequest.getOutputFormat(), new Date());
		if (prepareForTransfer)
			ReportLayoutRendererUtil.prepareRenderedLayoutForTransfer(layoutRoot, result, fileName, true);

		return result;
	}

	protected IRenderOption createRenderOption(
			RenderReportRequest renderRequest,
			IRunAndRenderTask task,
			String fileName, File layoutRoot)
	{
		RenderOption options = new RenderOption( );
		options.setOutputFormat(renderRequest.getOutputFormat().toString());
		options.setOutputFileName(layoutRoot.getAbsolutePath().toString() + File.separator + fileName+"." + renderRequest.getOutputFormat().toString()); //$NON-NLS-1$
		return options;
	}
}
