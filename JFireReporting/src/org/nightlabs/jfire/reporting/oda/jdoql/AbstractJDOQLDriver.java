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

package org.nightlabs.jfire.reporting.oda.jdoql;

import org.eclipse.datatools.connectivity.oda.jfire.IConnection;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;
import org.nightlabs.jfire.reporting.oda.Driver;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public abstract class AbstractJDOQLDriver extends Driver {

	/**
	 * LOG4J logger used by this class
	 */
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(AbstractJDOQLDriver.class);
	
	private IJDOQueryProxyFactory proxyFactory;
	
	public AbstractJDOQLDriver() {
		if(logger.isDebugEnabled()) {
			logger.debug("*****************************************");
			logger.debug("************ JDOQLDriver instantiated **");
			logger.debug("*****************************************");
		}
	}
	
	protected abstract IJDOQueryProxyFactory createProxyFactory();
	
	protected IJDOQueryProxyFactory getProxyFactory() {
		if (proxyFactory == null)
			proxyFactory = createProxyFactory();
		return proxyFactory;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IDriver#getConnection(java.lang.String)
	 */
	public IConnection getConnection(String dataSourceId) throws JFireOdaException {
		return new JDOQLConnection(getProxyFactory());
	}

}
