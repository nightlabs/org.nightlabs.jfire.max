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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.reporting.oda.client.jdoql;

import java.util.Map;

import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.reporting.ReportManager;
import org.nightlabs.jfire.reporting.ReportManagerUtil;
import org.nightlabs.jfire.reporting.oda.jdoql.AbstractJDOQLProxy;
import org.nightlabs.jfire.reporting.oda.jdoql.JDOQLResultSetMetaData;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ClientJDOQLProxy extends AbstractJDOQLProxy {



	private JDOQLResultSetMetaData metaData;
	
	public IResultSetMetaData getMetaData() throws OdaException {
		if (metaData == null)
			metaData = fetchMetaData(getQuery());
		return metaData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#executeQuery()
	 */
	public IResultSet executeQuery() throws OdaException {
		return executeQuery(getQuery(), getNamedParameters(), (JDOQLResultSetMetaData)getMetaData());
	}
	
	public static JDOQLResultSetMetaData fetchMetaData(String queryText) {
		try {
			ReportManager reportManager = ReportManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			return reportManager.getQueryMetaData(Login.getLogin().getOrganisationID(), queryText);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static IResultSet executeQuery( String queryText, Map parameters, JDOQLResultSetMetaData metaData) {
		try {
			ReportManager reportManager = ReportManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			return reportManager.fetchJDOQLResultSet(
					queryText,
					parameters,
					metaData
				);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
