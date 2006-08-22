/**
 * 
 */
package org.nightlabs.jfire.reporting.layout.render;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.reporting.JFireReportingEAR;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector;
import org.nightlabs.util.Utils;

/**
 * Helper class for {@link ReportLayoutRenderer} to unify their behaviour.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ReportLayoutRendererUtil {

	/**
	 * Retruns after asuring that a folder exists, that can be uniquely addressed
	 * using the sessionID of the actual user.
	 *  
	 * @return The root folder for renderedReportLayouts of the actual session.
	 */
	public static File prepareRenderedLayoutOutputFolder() {
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
		return layoutRoot;
	}
	
	/**
	 * Takes the root Folder of a rendered BIRT Layout and populates the given {@link RenderedReportLayout}'s
	 * data with its contents. If doZip is true, the whole folder will be zipped and set as data. If it is false
	 * this method tries to locate 'renderedLayout.'[OUTPUTFORMAT] and set this as data (not zipped of course).
	 *  
	 * @param layoutRoot The root folder for the rendered layout (e.g. containig one html file and several images, or a single pdf).
	 * @param reportLayout The {@link RenderedReportLayout} with a valid header. The data member of this instance will be manipulated (populated with the file data).
	 * @param doZip Whether to zip contents or not. (Note, that if not ziped, only one entry can be added: renderedReport.[OUTPUTFORMAT])
	 */
	public static void prepareRenderedLayoutForTransfer(File layoutRoot, RenderedReportLayout reportLayout, boolean doZip) {
		// zip the complete folder
		File transferFile = null; 
		
		if (doZip) {
			reportLayout.getHeader().setZipped(true);
			transferFile = new File(layoutRoot, "renderedLayout.zip");
			try {			
				Utils.zipFolder(transferFile, layoutRoot);
			} catch (IOException e) {
				throw new IllegalStateException("Could not zip the rendered layout.", e);
			}
		} 
		else {
			reportLayout.getHeader().setZipped(false);
			transferFile = new File(layoutRoot, "renderedLayout."+reportLayout.getHeader().getOutputFormat().toString());
		}
		
		
		BufferedInputStream buf;
		try {
			buf = new BufferedInputStream(new FileInputStream(transferFile));
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Could not find zip file "+transferFile, e);
		}
		try {
			DataBuffer dataBuffer = null;
			try {
				dataBuffer = new DataBuffer(buf);
			} catch (IOException e) {
				throw new IllegalStateException("Could not create DataBuffer!", e);
			}
			reportLayout.setData(dataBuffer.createByteArray());
		} catch (IOException e) {
			throw new IllegalStateException("Could not create the rendered report data", e);
		} finally {
			try {
				buf.close();
			} catch (IOException e) {
				throw new IllegalStateException("Could not close FileInputStream", e);
			}
		}
	}
	
}
