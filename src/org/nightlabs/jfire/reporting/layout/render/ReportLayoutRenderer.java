/**
 * 
 */
package org.nightlabs.jfire.reporting.layout.render;

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
	
	public RenderedReportLayout renderReport( 			
			PersistenceManager pm,
			ReportRegistryItemID reportRegistryItemID, 
			IRunAndRenderTask task,			
			Map<String,Object> parsedParams,
			Birt.OutputFormat format
		) throws EngineException;

}
