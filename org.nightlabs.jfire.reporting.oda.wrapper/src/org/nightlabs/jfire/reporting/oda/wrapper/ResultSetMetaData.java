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

import java.io.Serializable;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;

/**
 * Common type for all oda resultset metadata in JFireReporting.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ResultSetMetaData implements IResultSetMetaData, Serializable {
	
	private org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData delegate;
	
	public ResultSetMetaData(org.eclipse.datatools.connectivity.oda.jfire.IResultSetMetaData delegate) {
		this.delegate = delegate;
	}


	public int getColumnCount() throws OdaException {
		try {
			return delegate.getColumnCount();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getColumnDisplayLength(int index) throws OdaException {
		try {
			return delegate.getColumnDisplayLength(index);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public String getColumnLabel(int index) throws OdaException {
		try {
			return delegate.getColumnLabel(index);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public String getColumnName(int index) throws OdaException {
		try {
			return delegate.getColumnName(index);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getColumnType(int index) throws OdaException {
		try {
			return delegate.getColumnType(index);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public String getColumnTypeName(int index) throws OdaException {
		try {
			return delegate.getColumnTypeName(index);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getPrecision(int index) throws OdaException {
		try {
			return delegate.getPrecision(index);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getScale(int index) throws OdaException {
		try {
			return delegate.getScale(index);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int isNullable(int index) throws OdaException {
		try {
			return delegate.isNullable(index);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}
}

