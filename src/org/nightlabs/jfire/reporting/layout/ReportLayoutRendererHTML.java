/**
 * 
 */
package org.nightlabs.jfire.reporting.layout;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import javax.jdo.PersistenceManager;
import javax.naming.NamingException;

import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderContext;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.nightlabs.ModuleException;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.base.InitException;
import org.nightlabs.jfire.reporting.Birt;
import org.nightlabs.jfire.reporting.JFireReportingEAR;
import org.nightlabs.jfire.reporting.Birt.OutputFormat;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.servermanager.JFireServerManagerUtil;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector;
import org.nightlabs.util.Utils;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ReportLayoutRendererHTML implements ReportLayoutRenderer {

	/**
	 * 
	 */
	public ReportLayoutRendererHTML() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.layout.ReportLayoutRenderer#getOutputFormat()
	 */
	public OutputFormat getOutputFormat() {
		return Birt.OutputFormat.html;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.layout.ReportLayoutRenderer#renderReport(javax.jdo.PersistenceManager, org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID, org.eclipse.birt.report.engine.api.IRunAndRenderTask, java.util.Map, org.nightlabs.jfire.reporting.Birt.OutputFormat)
	 */
	public RenderedReportLayout renderReport(PersistenceManager pm,
			ReportRegistryItemID reportRegistryItemID, IRunAndRenderTask task,
			Map<String, Object> parsedParams, OutputFormat format
		)
	throws EngineException
	{
		if (format != Birt.OutputFormat.html)
			throw new IllegalArgumentException(this.getClass().getName()+" was asked to render a report to format "+format+" altough it is registered to "+getOutputFormat());
		HTMLRenderOption options = new HTMLRenderOption( );
		options.setOutputFormat(format.toString());
		
		HTMLRenderContext renderContext = new HTMLRenderContext( );
		File earDir;
		try {
			earDir = JFireReportingEAR.getEARDir();
		} catch (Exception e) {
			throw new IllegalStateException("Could not obtain archive directory!",e);
		}
		File layoutRoot;
		layoutRoot = new File(earDir, "birt"+File.separator+"rendered"+File.separator+SecurityReflector.getUserDescriptor().getSessionID());
		
		if (layoutRoot.exists()) {
			if (!Utils.deleteDirectoryRecursively(layoutRoot))
				throw new IllegalStateException("Could not delete rendered report tmp folder "+layoutRoot);
		}
		if (!layoutRoot.exists()) {
			if (!layoutRoot.mkdirs())
				throw new IllegalStateException("Could not create rendered report tmp folder "+layoutRoot);
		}
		
		renderContext.setImageDirectory(layoutRoot.getAbsolutePath().toString()+File.separator+"images");
		renderContext.setBaseImageURL("images"); //$NON-NLS-1$

		HashMap appContext = new HashMap( );
		appContext.put(EngineConstants.APPCONTEXT_HTML_RENDER_CONTEXT, renderContext);
		task.setAppContext( appContext );
		
		options.setOutputFileName(layoutRoot.getAbsolutePath().toString()+File.separator+"renderedLayout.html");
//		options.setOutputStream(outputStream);
		task.setRenderOption( options );
		
		task.setParameterValues(parsedParams);
		
		task.run();
		
		RenderedReportLayout result = new RenderedReportLayout(reportRegistryItemID, format, new Date());

		// zip the complete folder
		File zip = new File(layoutRoot, "renderedLayout.zip");
		try {
			Utils.zipFolder(zip, layoutRoot);
		} catch (IOException e) {
			throw new IllegalStateException("Could not zip the rendered layout.", e);
		}
		BufferedInputStream buf;
		try {
			buf = new BufferedInputStream(new FileInputStream(zip));
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Could not find zip file "+zip, e);
		}
		try {
			DataBuffer dataBuffer = null;
			try {
				dataBuffer = new DataBuffer(buf);
			} catch (IOException e) {
				throw new IllegalStateException("Could not create DataBuffer!", e);
			}
			result.setData(dataBuffer.createByteArray());
		} catch (IOException e) {
			throw new IllegalStateException("Could not create the rendered report data", e);
		} finally {
			try {
				buf.close();
			} catch (IOException e) {
				throw new IllegalStateException("Could not close FileInputStream", e);
			}
		}
		
		
//		DataBuffer dataBuffer = null;
//		OutputStream outputStream = null;
//		RenderedReportLayout result = new RenderedReportLayout(reportRegistryItemID, format, new Date());
//		try {
//			try {
//				dataBuffer = new DataBuffer(512, Integer.MAX_VALUE, (File)null);
//				outputStream = dataBuffer.createOutputStream();
//			} catch (IOException e) {
//				e.printStackTrace();		
//			}
//			
//			
//			options.setOutputFileName(layoutRoot.getAbsolutePath().toString()+File.separator+"renderedLayout.html");
////			options.setOutputStream(outputStream);
//			task.setRenderOption( options );
//			
//			task.setParameterValues(parsedParams);
//			
//			task.run();
//		} finally {			
//			try {
//				outputStream.close();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		}
//		try {
//			result.setData(dataBuffer.createByteArray());
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
		
		return result;
	}

}
