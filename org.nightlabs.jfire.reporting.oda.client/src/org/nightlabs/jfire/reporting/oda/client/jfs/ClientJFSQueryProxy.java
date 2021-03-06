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

package org.nightlabs.jfire.reporting.oda.client.jfs;

import java.util.Map;

import org.eclipse.datatools.connectivity.oda.jfire.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.jfire.IResultSet;
import org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;
import org.nightlabs.jfire.reporting.oda.client.ReportingOdaClientPlugin;
import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSQueryProxy;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ClientJFSQueryProxy extends AbstractJFSQueryProxy {

	/**
	 * 
	 */
	public ClientJFSQueryProxy(Map properties) {
		super();
	}

	private ScriptRegistryItemID lastParamMetaDataItemID;
	private IParameterMetaData lastParameterMetaData;
	
	@Override
	public IParameterMetaData getParameterMetaData() throws JFireOdaException {
		ScriptRegistryItemID itemID = getScriptRegistryItemID();
		if (itemID == null)
			return null;
		if (itemID.equals(lastParamMetaDataItemID))
			return lastParameterMetaData;
		lastParamMetaDataItemID = itemID;
		try {
			lastParameterMetaData = ReportingOdaClientPlugin.getReportManager().getJFSParameterMetaData(itemID);
		} catch (Exception e) {
			JFireOdaException ex = new JFireOdaException("Could not get ParameterMetaData: "+e.getMessage()); //$NON-NLS-1$
			ex.initCause(e);
			throw ex;
		}
		return lastParameterMetaData;
	}
	
	/**
	 * Does nothing.
	 * 
	 * @see org.eclipse.datatools.connectivity.oda.jfire.IQuery#close()
	 */
	public void close() throws JFireOdaException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#executeQuery()
	 */
	public IResultSet executeQuery() throws JFireOdaException {
		try {
			return ReportingOdaClientPlugin.getReportManager().getJFSResultSet(
					getJFSQueryPropertySet(), getNamedParameters()
				);
		} catch (Exception e) {
			JFireOdaException ex = new JFireOdaException("Could not get ResultSet: "+e.getMessage()); //$NON-NLS-1$
			ex.initCause(e);
			throw ex;
		}
	}

	
	private ScriptRegistryItemID lastMetaDataItemID;
	private IResultSetMetaData lastMetaData;
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMetaData()
	 */
	public IResultSetMetaData getMetaData() throws JFireOdaException {
		ScriptRegistryItemID itemID = getJFSQueryPropertySet().getScriptRegistryItemID();
		if (itemID == null)
			return null;
		if (itemID.equals(lastMetaDataItemID))
			return lastMetaData;
		lastMetaDataItemID = itemID;
		try {
			lastMetaData = ReportingOdaClientPlugin.getReportManager().getJFSResultSetMetaData(getJFSQueryPropertySet());
		} catch (Exception e) {
			JFireOdaException ex = new JFireOdaException("Could not get ResultSetMetaData: "+e.getMessage()); //$NON-NLS-1$
			ex.initCause(e);
			throw ex;
		}
		return lastMetaData;
	}

}
