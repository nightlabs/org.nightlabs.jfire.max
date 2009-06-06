package org.nightlabs.jfire.reporting.layout.render;

import java.io.File;

import javax.jdo.PersistenceManager;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public interface IRenderManager
{
	public RenderedReportLayout renderReport(
			PersistenceManager pm,
			RenderReportRequest renderRequest
	)
	throws RenderReportException;

	public RenderedReportLayout renderReport(
			PersistenceManager pm,
			RenderReportRequest renderRequest,
			String fileName,
			File layoutRoot,
			boolean prepareForTransfer
		)
	throws RenderReportException;
}
