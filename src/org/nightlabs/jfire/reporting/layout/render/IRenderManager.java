package org.nightlabs.jfire.reporting.layout.render;

import java.io.File;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.reporting.ReportingEngine;

/**
 * Helper to render reports on the server. Instances of {@link RenderManager}
 * can be obtained from a {@link ReportingEngine} of which one is registered per reporting-backend type.
 * 
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface IRenderManager
{
	public static final String DEFAULT_ENTRY_FILE_NAME = "renderedLayout";
	
	/**
	 * Lets the underlying report engine render the given report with the given params in the given format.
	 * The report will be rendered into the default temporary folder for
	 * rendered report layouts (see {@link ReportLayoutRendererUtil#prepareRenderedLayoutOutputFolder()}).
	 * Also it will set the entry file-name of the report to {@link #DEFAULT_ENTRY_FILE_NAME}.
	 * 
	 * @param pm
	 * @param renderRequest
	 *
	 * @see #renderReport(PersistenceManager, RenderReportRequest, String, File, boolean)
	 * @throws RenderReportException If rendering the report fails.
	 */
	public RenderedReportLayout renderReport(
			PersistenceManager pm,
			RenderReportRequest renderRequest
	)
	throws RenderReportException;

	/**
	 * Lets the underlying report engine render the given report with the given params in the given format.
	 * <p>
	 * This will produce a {@link RenderedReportLayout} with the values of its {@link RenderedReportLayout.Header}
	 * corresponding the given fileName
	 * </p>
	 *
	 * @param pm The PersistenceManager to retrieve the ReportLayout from.
	 * @param renderRequest The request for rendering, holding the layout id to be rendered, the format it should be rendered to and the report parameters
	 * @param fileName The name that should be used for the report entry file (without file-extension)
	 * @param layoutRoot The root folder to render the report into
	 * @param prepareForTransfer Whether the results data should be set.
	 * @throws RenderReportException If rendering the report fails.
	 */
	public RenderedReportLayout renderReport(
			PersistenceManager pm,
			RenderReportRequest renderRequest,
			String fileName,
			File layoutRoot,
			boolean prepareForTransfer
		) 
	throws RenderReportException;
}
