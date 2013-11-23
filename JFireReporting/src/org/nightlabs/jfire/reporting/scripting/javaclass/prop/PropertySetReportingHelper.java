/**
 * 
 */
package org.nightlabs.jfire.reporting.scripting.javaclass.prop;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.prop.datafield.ImageDataField;
import org.nightlabs.jfire.prop.id.DataFieldID;
import org.nightlabs.jfire.reporting.JFireReportingHelper;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererUtil;

/**
 * Helper to be used from within JavaScript of a BIRT layout.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class PropertySetReportingHelper {

	private static final String NO_IMAGE_URL = "file:///" + Long.toHexString(System.currentTimeMillis()) + ".jpg";
	
	/**
	 * 
	 */
	public PropertySetReportingHelper() {
	}

	public static String getImageLink(String imageDataFieldIDStr) {
		if (imageDataFieldIDStr == null || "".equals(imageDataFieldIDStr)) {
			return NO_IMAGE_URL;
		} else {
			return getImageURL(imageDataFieldIDStr).toString();
		}
	}
	
	public static URL getImageURL(String imageDataFieldIDStr) {
		DataFieldID dataFieldID = null;
		try {
			dataFieldID = new DataFieldID(imageDataFieldIDStr);
		} catch (Exception e) {
			throw new IllegalArgumentException("The string " + imageDataFieldIDStr + " is not correctly formatted String representation of a " + DataFieldID.class);
		}
		PersistenceManager pm = JFireReportingHelper.getPersistenceManager();
		ImageDataField imageDataField = null;
		try {
			imageDataField = (ImageDataField) pm.getObjectById(dataFieldID);
		} catch (JDOObjectNotFoundException e) {
			throw new IllegalArgumentException("The given string " + imageDataFieldIDStr + " does not reference an existing " + ImageDataField.class.getSimpleName(), e);
		} catch (ClassCastException e) {
			throw new IllegalStateException("The given string " + imageDataFieldIDStr + " does not reference an " + ImageDataField.class.getSimpleName(), e);
		}
		File reportFolder = ReportLayoutRendererUtil.getRenderedLayoutOutputFolder();
		File imageDir = new File(reportFolder, "propertyset-images");
		imageDir.mkdirs();
		
		File imageFile = null;
		try {
			imageFile = imageDataField.saveToDir(imageDir);
		} catch (IOException e) {
			throw new RuntimeException("Could not store image file!", e);
		}
		try {
			return imageFile.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("Could not convert the image file object to an URL.", e);
		}
	}
}
