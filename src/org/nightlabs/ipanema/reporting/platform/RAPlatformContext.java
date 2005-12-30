/**
 * 
 */
package org.nightlabs.ipanema.reporting.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.core.framework.IPlatformContext;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class RAPlatformContext implements IPlatformContext {

	/* (non-Javadoc)
	 * @see org.eclipse.birt.core.framework.IPlatformContext#getFileList(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public List getFileList(String homeFolder, String subFolder, boolean includingFiles, boolean relativeFileList) 
	{
		File file = new File(homeFolder, subFolder);
		File[] files = file.listFiles();
		if (files == null)
			return new ArrayList();
		List result = new ArrayList();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory())
				result.add(files[i].getAbsolutePath());
			else 
				if (includingFiles)
					result.add(files[i].getAbsolutePath());
		}
		if (relativeFileList) {
			for (int i = 0; i < result.size(); i++) {
				String fileName = (String)result.get(i);
				fileName = fileName.substring( homeFolder.length() );
				if ( !fileName.startsWith(File.pathSeparator) )
					fileName = File.pathSeparator + fileName; 
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.birt.core.framework.IPlatformContext#getInputStream(java.lang.String, java.lang.String)
	 */
	public InputStream getInputStream(String folder, String fileName) {
		InputStream in = null;		

		File file =  new File( folder, fileName );
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File not found "+folder+File.pathSeparator+fileName);
		}
		return in;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.birt.core.framework.IPlatformContext#getURL(java.lang.String, java.lang.String)
	 */
	public URL getURL(String folder, String fileName) {
		File file =  new File( folder, fileName );
		try {
			return file.toURL();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("URL malformed for "+folder+File.pathSeparator+fileName);
		}
	}

}
