/**
 * 
 */
package org.nightlabs.jfire.reporting.layout.render;

import java.io.File;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.nightlabs.jfire.reporting.Birt;
import org.nightlabs.jfire.reporting.Birt.OutputFormat;
import org.nightlabs.jfire.reporting.layout.ReportRegistry;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;

/**
 * ReportLayoutRenderer are responsible for rendering a report to a specific
 * format. They will be asked to create a {@link RenderedReportLayout}
 * that can be send to clients.
 * 
 * ReportLayoutRenderer are registered to the {@link ReportRegistry} on a specific
 * format. 
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public interface ReportLayoutRenderer {

	public OutputFormat getOutputFormat();
	
	/**
	 * Render the report layout given by reportRegistryItemID to the 
	 * format of the implementation of this renderer.
	 * 
	 * @param pm A {@link PersistenceManager} that might be used to obtain information from the datastore.
	 * @param reportRegistryItemID The id of the layout to render. Usually used to create a {@link RenderedReportLayout} with.
	 * @param task The task that will be run to render the layout. 
	 * This should be configred appropriately (set options, paths etc.). 
	 * Renderes should run this task to create the rendered layout.
	 * @param parsedParams The map of ready parsed parameters. This should be passed to the task.
	 * @param format The {@link Birt.OutputFormat} to use (should match the format the renderer is registered to).
	 * @param fileName The name that should be used as entry-file-name for the report. (Without file-extension. This will be the output-formats name).
	 * @param layoutRoot The folder that should be used to create the report in.
	 * @param prepareResultForTransfer Whether or not to set the result's data {@link RenderedReportLayout#getData())} to the appropriate content. See {@link ReportLayoutRendererUtil#prepareRenderedLayoutForTransfer(File, RenderedReportLayout, String, boolean)}
	 * 
	 * @return A new {@link RenderedReportLayout}.
	 * 
	 * @throws EngineException
	 */
	public RenderedReportLayout renderReport( 			
			PersistenceManager pm,
			ReportRegistryItemID reportRegistryItemID, 
			IRunAndRenderTask task,			
			Map<String,Object> parsedParams,
			Birt.OutputFormat format,
			String fileName,
			File layoutRoot,
			boolean prepareResultForTransfer
		) throws EngineException;
}
