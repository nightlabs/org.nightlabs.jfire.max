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

import org.eclipse.birt.core.framework.IPlatformContext;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.reporting.JFireReportingEAR;

/**
 * {@link IPlatformContext} for BIRT within the JFire Server.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ServerPlatformContext implements IPlatformContext {

	private String root;
	
	public ServerPlatformContext() {
		try {
			this.root = new File(JFireReportingEAR.getEARDir(), JFireReportingEAR.BIRT_RUNTIME_SUBDIR).getAbsolutePath();
		} catch (ModuleException e) {
			throw new IllegalStateException("Could not instantiate ServerPlatformContext");
		}
	}
	
	public String getPlatform() {
		return root;
	}

}
