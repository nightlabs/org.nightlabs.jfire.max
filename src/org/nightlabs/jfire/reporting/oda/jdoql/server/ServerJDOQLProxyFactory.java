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

package org.nightlabs.jfire.reporting.oda.jdoql.server;

import java.util.Map;

import org.nightlabs.jfire.reporting.oda.jdoql.IJDOQueryProxy;
import org.nightlabs.jfire.reporting.oda.jdoql.IJDOQueryProxyFactory;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ServerJDOQLProxyFactory implements IJDOQueryProxyFactory {

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jdoql.IJDOQueryProxyFactory#createJDOQueryProxy()
	 */
	public IJDOQueryProxy createJDOQueryProxy(Map connectionProperties) {
//		String organisationID = JDOQLConnection.checkConnectonProperties(connectionProperties);
		return new ServerJDOQLProxy();
	}
	
}
