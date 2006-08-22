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
 * {@link IPlatformContext} for Birt within the JFire Server.
 * TODO: This needs some more thoughts!
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class RAPlatformContext implements IPlatformContext {

	private String root;
	
	public RAPlatformContext(String root) {
		this.root = root;
	}
	
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

	public String getPlatform() {
		return root;
	}

}
