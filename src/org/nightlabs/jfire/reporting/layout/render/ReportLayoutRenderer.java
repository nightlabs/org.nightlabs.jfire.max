/**
 *
 */
package org.nightlabs.jfire.reporting.layout.render;

import java.io.File;

import javax.jdo.PersistenceManager;

/**
 * ReportLayoutRenderer are responsible for rendering a report to a specific
 * format. They will be asked to create a {@link RenderedReportLayout}
 * that can be send to clients.
 *
 * ReportLayoutRenderer are registered to the org.nightlabs.jfire.reporting.layout.ReportRegistry on a specific
 * format.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public interface ReportLayoutRenderer {

	/**
	 * Render the report layout given by reportRegistryItemID to the
	 * format of the implementation of this renderer.
	 *
	 * @param pm A {@link PersistenceManager} that might be used to obtain information from the datastore.
	 * @param renderRequest The request for rendering, holding the layout id to be rendered, the format it should be rendered to and the report parameters.
	 * 	Note that the parameters passed here were already parsed for BIRT. These should be passed to the task.
	 * @param task The task that will be run to render the layout.
	 * This should be configured appropriately (set options, paths etc.).
	 * Renderers should run this task to create the rendered layout.
	 * @param parsedParams The map of ready parsed parameters. This should be passed to the task.
	 * @param fileName The name that should be used as entry-file-name for the report. (Without file-extension. This will be the output-formats name).
	 * @param layoutRoot The folder that should be used to create the report in.
	 * @param prepareResultForTransfer Whether or not to set the result's data {@link RenderedReportLayout#getData())} to the appropriate content. See {@link ReportLayoutRendererUtil#prepareRenderedLayoutForTransfer(File, RenderedReportLayout, String, boolean)}
	 *
	 * @return A new {@link RenderedReportLayout}.
	 *
	 * @throws ReportLayoutEngineException
	 */
	public RenderedReportLayout renderReport(
			PersistenceManager pm,
			RenderReportRequest renderRequest,
//			IRunAndRenderTask task,
			Object task,
			String fileName,
			File layoutRoot,
			boolean prepareResultForTransfer
		)
//	throws EngineException;
	throws ReportLayoutEngineException;
}
