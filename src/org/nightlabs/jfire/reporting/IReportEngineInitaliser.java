package org.nightlabs.jfire.reporting;

import org.nightlabs.jfire.reporting.layout.ReportRegistry;
import org.nightlabs.jfire.reporting.layout.render.IRenderManager;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public interface IReportEngineInitaliser
{
	void initReportEngine();

	IRenderManager createRenderManager();

	void registerReportRenderer(ReportRegistry registry);
}
