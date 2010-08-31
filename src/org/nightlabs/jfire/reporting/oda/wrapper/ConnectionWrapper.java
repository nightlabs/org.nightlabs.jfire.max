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

package org.nightlabs.jfire.reporting.oda.wrapper;

import java.util.Properties;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;

import com.ibm.icu.util.ULocale;

/**
 * Common type for oda IConnections in JFireReporting.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ConnectionWrapper implements IConnection {
	
	private org.eclipse.datatools.connectivity.oda.jfire.IConnection delegate;

	public ConnectionWrapper(org.eclipse.datatools.connectivity.oda.jfire.IConnection delegate) {
		super();
		this.delegate = delegate;
	}

	public void close() throws OdaException {
		try {
			delegate.close();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void commit() throws OdaException {
		try {
			delegate.commit();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getMaxQueries() throws OdaException {
		try {
			return delegate.getMaxQueries();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public IDataSetMetaData getMetaData(String dataSetType) throws OdaException {
		try {
			return new ConnectionMetaDataWrapper(this, delegate.getMetaData(dataSetType));
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public boolean isOpen() throws OdaException {
		try {
			return delegate.isOpen();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public IQuery newQuery(String dataSetType) throws OdaException {
		try {
			return new QueryWrapper(delegate.newQuery(dataSetType));
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void open(Properties connProperties) throws OdaException {
		try {
			delegate.open(connProperties);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void rollback() throws OdaException {
		try {
			delegate.rollback();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setAppContext(Object context) throws OdaException {
		try {
			delegate.setAppContext(context);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setLocale(ULocale locale) throws OdaException {
		// FIXME: Alex: Implement
	}
	
	
	
}
