/**
 * 
 */
package org.nightlabs.ipanema.reporting.platform;

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
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IParameterGroupDefn;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.ReportEngine;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.ipanema.reporting.Birt;
import org.nightlabs.ipanema.reporting.layout.RenderedReportLayout;
import org.nightlabs.ipanema.reporting.layout.ReportLayout;
import org.nightlabs.ipanema.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.ipanema.servermanager.j2ee.SecurityReflector;
import org.nightlabs.ipanema.servermanager.j2ee.SecurityReflector.UserDescriptor;

import com.sun.org.apache.xpath.internal.FoundIndex;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ReportingManager {

	private Logger LOGGER = Logger.getLogger(ReportingManager.class);
	
	private ReportingManagerFactory factory;
	private UserDescriptor userDescriptor;
	
	
	/**
	 * 
	 */
	public ReportingManager(ReportingManagerFactory factory) {
		this.factory = factory;
		InitialContext initCtx;
		try {
			initCtx = new InitialContext();
			userDescriptor = SecurityReflector.lookupSecurityReflector(initCtx).whoAmI();
			initCtx.close();
		} catch (NamingException e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
	}
	
	
	public void open() {
		
	}
	
	public void close() {
		
	}
	
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
		HTMLRenderOption options = new HTMLRenderOption( );
		options.setOutputFormat(format.toString());
		
		DataBuffer dataBuffer = null;
		OutputStream outputStream = null;
		RenderedReportLayout result = new RenderedReportLayout();
		try {
			try {
				dataBuffer = new DataBuffer(512, Integer.MAX_VALUE, (File)null);
				outputStream = dataBuffer.createOutputStream();
			} catch (IOException e) {
				e.printStackTrace();		
			}
			
			
			options.setOutputStream(outputStream);
			task.setRenderOption( options );
			
			HashMap<String,Object> parsedParams = parseReportParams(reportEngine, report, params);
			
			// TODO: Birt does only support HashMaps here, maybe this should change
			task.setParameterValues(parsedParams);
			
			task.run();
		} finally {			
			try {
				outputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		result.setOutputFormat(format);
		result.setTimestamp(new Date());
		try {
			result.setData(dataBuffer.createByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return result;
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

