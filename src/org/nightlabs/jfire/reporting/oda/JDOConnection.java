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

package org.nightlabs.jfire.reporting.oda;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

/**
 * Common type for oda IConnections in JFireReporting.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public abstract class JDOConnection implements IConnection {
	
	public static final String PROPERTY_ORGANISATION_ID = "organisationID";
	
	private Map currentProperties;
	
	protected Map getCurrentProperties() {
		return currentProperties;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#open(java.util.Properties)
	 */
	public void open(Properties connProperties) throws OdaException {
		// TODO: obtain IJDOQueryProxy
		currentProperties = connProperties;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#setAppContext(java.lang.Object)
	 */
	public void setAppContext(Object context) throws OdaException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#close()
	 */
	public void close() throws OdaException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#isOpen()
	 */
	public boolean isOpen() throws OdaException {
		// TODO Auto-generated method stub
		return currentProperties != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#getMetaData(java.lang.String)
	 */
	public IDataSetMetaData getMetaData(String dataSetType) throws OdaException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#getMaxQueries()
	 */
	public int getMaxQueries() throws OdaException {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#commit()
	 */
	public void commit() throws OdaException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#rollback()
	 */
	public void rollback() throws OdaException {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Checks the connectionProperties and returns the organisationID set.
	 */
	public static boolean checkConnectonProperties(Map connectionProperties) {
//		Object oID = connectionProperties.get(JDOConnection.PROPERTY_ORGANISATION_ID);
//		if (oID == null)
//			throw new IllegalArgumentException("Property "+JDOConnection.PROPERTY_ORGANISATION_ID+" is not set for this connection");
//		if (!(oID instanceof String))
//			throw new IllegalArgumentException("Property "+JDOConnection.PROPERTY_ORGANISATION_ID+" is not an instance of String?!?");
//		String organisationID = (String)oID;
//		if ("".equals(organisationID))
//			throw new IllegalArgumentException("Property "+JDOConnection.PROPERTY_ORGANISATION_ID+" is not set.");
//		return organisationID;
		return true;
	}

	public static Map createJDOConnectonProperties() {
		Map result = new HashMap(1);
//		result.put(JDOConnection.PROPERTY_ORGANISATION_ID, organisationID);
		return result;
	}
	
	
}
