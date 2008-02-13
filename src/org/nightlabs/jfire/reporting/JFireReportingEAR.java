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

package org.nightlabs.jfire.reporting;

import java.io.File;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerUtil;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class JFireReportingEAR {

	protected JFireReportingEAR() {}
	
	public static final String MODULE_NAME = "JFireReporting";
	public static final String BIRT_RUNTIME_SUBDIR = "birt";
	
	public static File getEARDir()
	throws ModuleException
	{
		JFireServerManager jFireServerManager;
		try {
			jFireServerManager = JFireServerManagerUtil.getJFireServerManager();
		} catch (Exception e) {
			throw new ModuleException("Could not get JFireServerManager!", e);
		}
		try {
			File earDir = new File(
						new File(jFireServerManager.getJFireServerConfigModule()
								.getJ2ee().getJ2eeDeployBaseDirectory()
							),
					"JFireReporting.ear"
				);
			return earDir;
		} finally {
			jFireServerManager.close();
		}
	}
}
