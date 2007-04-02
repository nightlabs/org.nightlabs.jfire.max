package org.nightlabs.jfire.trade;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * This interface should be implemented by Objects which carry graphical
 * layout data files for products
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface ILayout 
{
	/**
	 * returns the binary data of the layout file
	 * @return the binary data of the layout file
	 */
	byte[] getFileData();
	
	/**
	 * returns the name of the layout file
	 * @return the name of the layout file
	 */
	String getFileName();
	
	/**
	 * returns the timestamp of the layout file
	 * @return the timestamp of the layout file
	 */
	Date getFileTimestamp();
	
	/**
	 * returns the organisationID
	 * @return the organisationID
	 */
	String getOrganisationID();
	
	/**
	 * 
	 * @param f the file to load
	 */
	void loadFile(File f) throws IOException;
	
	/**
	 * If <tt>f</tt> exists and is a directory, the <tt>fileName</tt> will be appended to it.
	 * If <tt>f</tt> does not exist, it is assumed to be a file which will be created. If it exists
	 * and is a file, the file will be overwritten! If any directory within the path to the given file
	 * is missing, it will automatically be created.
	 * @param f the file to save
	 */
	void saveFile(File f) throws IOException;
}