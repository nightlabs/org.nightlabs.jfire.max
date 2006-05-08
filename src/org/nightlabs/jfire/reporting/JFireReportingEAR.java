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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector;
import org.nightlabs.jfire.servermanager.j2ee.SecurityReflector.UserDescriptor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class JFireReportingEAR {

	protected JFireReportingEAR() {}
	
	public static final String MODULE_NAME = "JFireReporting"; 
	
	// TODO: Move this method to SecurityReflector or somewhere else in the ServerManager
	public static Lookup getLookup() throws ModuleException {
		SecurityReflector securityReflector = null;
		try {
			securityReflector = SecurityReflector.lookupSecurityReflector(new InitialContext());
		} catch (NamingException e) {
			throw new ModuleException(e);			
		}
		UserDescriptor userDescriptor = securityReflector.whoAmI();
		Lookup lookup = null;
		lookup = new Lookup(userDescriptor.getOrganisationID());
		return lookup;
	}
}
