/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.reporting.layout.render;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.ReportEngine;
import org.nightlabs.jfire.reporting.JFireReportingHelper;
import org.nightlabs.jfire.reporting.ReportingManagerFactory;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportRegistry;
import org.nightlabs.jfire.reporting.platform.ServerResourceLocator;

/**
 * Helper to render reports on the server. Instances of {@link RenderManager}
 * can be obtained by a {@link ReportingManagerFactory} of wich one instance
 * is bound to JNDI for each Organisation.
 * 
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class RenderManager {

	/**
	 * LOG4J logger used by this class
	 */
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(RenderManager.class);
	
	private ReportingManagerFactory factory;
	
	/**
	 * 
	 */
	public RenderManager(ReportingManagerFactory factory) {
		this.factory = factory;
	}
	
	
	public void open() {
		
	}
	
	public void close() {
		
	}

	/**
	 * Lets BIRT render the given report with the given params in the given format.
	 * The report will be rendered into the default temporary folder for 
	 * rendered report layouts. See {@link ReportLayoutRendererUtil#prepareRenderedLayoutOutputFolder()}.
	 * Also it will set the entry file-name of the report to "renderedReport".
	 *
	 * @see #renderReport(PersistenceManager, RenderReportRequest, String, File, boolean)
	 * @throws EngineException
	 */
	public RenderedReportLayout renderReport(
			PersistenceManager pm,
			RenderReportRequest renderRequest
		)
	throws RenderReportException
	{
		File layoutRoot = ReportLayoutRendererUtil.prepareRenderedLayoutOutputFolder();
		return renderReport(pm, renderRequest, "renderedLayout", layoutRoot, true);
	}
	
	/**
	 * Lets BIRT render the given report with the given params in the given format.
	 * 
	 * @param pm The PersistenceManager to retrieve the ReportLayout from.
	 * @param renderRequest The request for rendering, holding the layout id to be rendered, the format it should be rendered to and the report parameters
	 * @param fileName The name that should be used for the report entry file (without file-extension)
	 * @param layoutRoot The root folder to render the report into
	 * @param prepareForTransfer Whether the results data should be set.
	 * @throws EngineException
	 */
	public RenderedReportLayout renderReport(
			PersistenceManager pm,
			RenderReportRequest renderRequest,
			String fileName,
			File layoutRoot,
			boolean prepareForTransfer
		) 
	throws RenderReportException
	{
		ReportLayout reportLayout = null;
		try {
			reportLayout = (ReportLayout)pm.getObjectById(renderRequest.getReportRegistryItemID());
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("The ReportRegistryItemID with id "+renderRequest.getReportRegistryItemID()+" is not of type ReportLayout.");
		}
		if (logger.isDebugEnabled())
			logger.debug("Rendering report "+reportLayout.getReportRegistryItemID()+" to outputformat: "+renderRequest.getOutputFormat());
		
		ReportEngine reportEngine = factory.getReportEngine();
		if (logger.isDebugEnabled())
			logger.debug("Have report engine");
		
		ServerResourceLocator.setCurrentReportLayout(reportLayout);
		try {

			InputStream inputStream = reportLayout.createReportDesignInputStream();
			IReportRunnable report = null;
			try {
				report = reportEngine.openReportDesign(inputStream);
			} catch (EngineException e) {
				throw new RenderReportException(e);
			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			if (logger.isDebugEnabled())
				logger.debug("Opened reportlayout, creating RunAndRenderTask");
			IRunAndRenderTask task = reportEngine.createRunAndRenderTask(report);
			
			Locale locale = Locale.getDefault();
			if (renderRequest.getLocale() != null)				
				locale = renderRequest.getLocale();
			else {
				// TODO: Add the users locale
			}
			
			task.setLocale(locale);

			if (logger.isDebugEnabled())
				logger.debug("Creating ReportLayoutRenderer");
			ReportRegistry registry = ReportRegistry.getReportRegistry(pm);
			ReportLayoutRenderer renderer = null; 
			try {
				renderer = registry.createReportRenderer(renderRequest.getOutputFormat());
			} catch (Exception e) {
				throw new RenderReportException("Could not create ReportLayoutRenderer for OutputFormat "+renderRequest.getOutputFormat(), e);
			}
			if (logger.isDebugEnabled())
				logger.debug("Have ReportLayoutRenderer: "+renderer.getClass().getName());

			HashMap<String,Object> parsedParams = parseReportParams(reportEngine, report, renderRequest.getParameters());
			renderRequest.setParameters(parsedParams);

			logger.debug("Have report renderer, delegating render work");
			JFireReportingHelper.open(pm, parsedParams, locale, false);
			RenderedReportLayout result = null;
			// Ask the renderer to do the work
			try {
				result = renderer.renderReport(pm, renderRequest, task, fileName, layoutRoot, prepareForTransfer);
			} catch (Exception e) {
				System.err.println("Caught RunAndRenderTask exception");
				e.printStackTrace();
				throw new RenderReportException("RunAndRenderTask aborted with errors", ExceptionUtils.getRootCause(e));
			} finally {
				JFireReportingHelper.close();
			}
			// Check for rendering errors
			if (task.getErrors().size() > 0) {
				Collection<Throwable> errors = new ArrayList<Throwable>(task.getErrors().size());
				for (int i = 0; i < task.getErrors().size(); i++) {
					Throwable t = (Throwable) task.getErrors().get(0);
					errors.add(new RenderReportException(t.getMessage(), t)); // Exception wrapped as some things contain non-serializable members.
				}
				result.getHeader().setRenderingErrors(errors);
			}
			return result;
			
		} finally {
			ServerResourceLocator.setCurrentReportLayout(null);
		}
	}


	public static HashMap<String,Object> parseReportParams( ReportEngine engine, IReportRunnable report, Map values )
	{
		HashMap<String,Object> result = new HashMap<String,Object>();
		if (values.isEmpty())
			return result;

		IGetParameterDefinitionTask task = engine.createGetParameterDefinitionTask(report);
		// TODO: find alternative to deprecated getParameterDefns 
		Collection params = task.getParameterDefns(false);
	
		for (Iterator iterator = values.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			
			IScalarParameterDefn found = null;
			
			for (Iterator iter = params.iterator(); iter.hasNext();) {
				IParameterDefnBase param = (IParameterDefnBase) iter.next();
				
				if (param instanceof IParameterGroupDefn)
					continue;
				if (param.getName().equals(name))
				{
					found = (IScalarParameterDefn) param;
					break;
				}
			}
			if ( found == null )
			{
				logger.error( "Parameter " + name + " not found in the report." );
				continue;
			}
			Object value = values.get(name);
			result.put(name, value);
		}
		return result;
	}	
	
}

