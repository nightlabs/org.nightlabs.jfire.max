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

package org.nightlabs.jfire.reporting.platform;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.management.RuntimeErrorException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderContext;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.ReportEngine;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.reporting.Birt;
import org.nightlabs.jfire.reporting.layout.RenderedReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportRegistry;
import org.nightlabs.jfire.reporting.layout.ReportLayoutRenderer;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector.UserDescriptor;

import com.sun.org.apache.xpath.internal.FoundIndex;

/**
 * Helper to render reports on the server.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ReportingManager {

//	private Logger LOGGER = Logger.getLogger(ReportingManager.class);
	
	private ReportingManagerFactory factory;
	
	/**
	 * 
	 */
	public ReportingManager(ReportingManagerFactory factory) {
		this.factory = factory;
	}
	
	
	public void open() {
		
	}
	
	public void close() {
		
	}
	
	/**
	 * Lets BIRT render the given report with the given params in the given format.
	 * 
	 * @param pm The PersistenceManager to retrieve the ReportLayout from.
	 * @param reportRegistryItemID The ReportLayoutID to render.
	 * @param params The parameters to render this report with. 
	 * @param format The format to render to.
	 * @throws EngineException
	 */
	public RenderedReportLayout renderReport(
			PersistenceManager pm,
			ReportRegistryItemID reportRegistryItemID, 
			Map<String,Object> params,
			Birt.OutputFormat format
		) 
	throws EngineException
	{
		ReportLayout reportLayout = null;
		try {
			reportLayout = (ReportLayout)pm.getObjectById(reportRegistryItemID);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("The ReportRegistryItemID with id "+reportRegistryItemID+" is not of type ReportLayout.");
		}
		ReportEngine reportEngine = factory.getReportEngine();
		
		InputStream inputStream = new ByteArrayInputStream(reportLayout.getReportDesign());
		IReportRunnable report = reportEngine.openReportDesign(inputStream);
		IRunAndRenderTask task = reportEngine.createRunAndRenderTask(report);
		
		ReportRegistry registry = ReportRegistry.getReportRegistry(pm);
		ReportLayoutRenderer renderer = null; 
		try {
			renderer = registry.createReportRenderer(format);
		} catch (Exception e) {
			throw new EngineException("Could not create ReportLayoutRenderer for OutputFormat "+format, e);
		}
		
		HashMap<String,Object> parsedParams = parseReportParams(reportEngine, report, params);
		
		return renderer.renderReport(pm, reportRegistryItemID, task, parsedParams, format);
	}


	public static HashMap<String,Object> parseReportParams( ReportEngine engine, IReportRunnable report, Map values )
	{
		HashMap<String,Object> result = new HashMap<String,Object>();
		if ( values.isEmpty( ) )
			return result;

		IGetParameterDefinitionTask task = engine.createGetParameterDefinitionTask( report );
		// TODO: find alternative to deprecated getParameterDefns 
		Collection params = task.getParameterDefns( false );
	
		for (Iterator iterator = values.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			
			IScalarParameterDefn found = null;
			
			for (Iterator iter = params.iterator(); iter.hasNext();) {
				IParameterDefnBase param = (IParameterDefnBase) iter.next();
				
				if ( param instanceof IParameterGroupDefn )
					continue;
				if ( param.getName( ).equals( name ) )
				{
					found = (IScalarParameterDefn) param;
					break;
				}
			}
			if ( found == null )
			{
				System.err.println( "Parameter " + name + " not found in the report." );
				continue;
			}
			Object value = values.get( name );
			result.put( name, value );
		}
		return result;
	}	
	
}

