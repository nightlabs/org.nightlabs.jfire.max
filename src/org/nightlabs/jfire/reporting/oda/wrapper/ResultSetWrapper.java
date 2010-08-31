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
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.IClob;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.jfire.JFireOdaException;


/**
 * Common type for all oda resultsets in JFireReporting.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ResultSetWrapper implements IResultSet, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private org.eclipse.datatools.connectivity.oda.jfire.IResultSet delegate;

	public ResultSetWrapper(
			org.eclipse.datatools.connectivity.oda.jfire.IResultSet delegate) {
		super();
		this.delegate = delegate;
	}

	public void close() throws OdaException {
		try {
			delegate.close();
		} catch (JFireOdaException e) {
			e.printStackTrace();
		}
	}

	public int findColumn(String s) throws OdaException {
		try {
			return delegate.findColumn(s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public BigDecimal getBigDecimal(int i) throws OdaException {
		try {
			return delegate.getBigDecimal(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public BigDecimal getBigDecimal(String s) throws OdaException {
		try {
			return delegate.getBigDecimal(s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public IBlob getBlob(int i) throws OdaException {
		try {
			return new BlobWrapper(delegate.getBlob(i));
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public IBlob getBlob(String s) throws OdaException {
		try {
			return new BlobWrapper(delegate.getBlob(s));
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public boolean getBoolean(int i) throws OdaException {
		try {
			return delegate.getBoolean(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public boolean getBoolean(String s) throws OdaException {
		try {
			return delegate.getBoolean(s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public IClob getClob(int i) throws OdaException {
		// FIXME: Alex: Implement
		return null;
	}

	public IClob getClob(String s) throws OdaException {
		// FIXME: Alex: Implement
		return null;
	}

	public Date getDate(int i) throws OdaException {
		try {
			return delegate.getDate(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public Date getDate(String s) throws OdaException {
		try {
			return delegate.getDate(s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public double getDouble(int i) throws OdaException {
		try {
			return delegate.getDouble(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public double getDouble(String s) throws OdaException {
		try {
			return delegate.getDouble(s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getInt(int i) throws OdaException {
		try {
			return delegate.getInt(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getInt(String s) throws OdaException {
		try {
			return delegate.getInt(s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public IResultSetMetaData getMetaData() throws OdaException {
		try {
			return new ResultSetMetaData(delegate.getMetaData());
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public Object getObject(int i) throws OdaException {
		try {
			return delegate.getObject(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public Object getObject(String s) throws OdaException {
		try {
			return delegate.getObject(s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public int getRow() throws OdaException {
		try {
			return delegate.getRow();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public String getString(int i) throws OdaException {
		try {
			return delegate.getString(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public String getString(String s) throws OdaException {
		try {
			return delegate.getString(s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public Time getTime(int i) throws OdaException {
		try {
			return delegate.getTime(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public Time getTime(String s) throws OdaException {
		try {
			return delegate.getTime(s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public Timestamp getTimestamp(int i) throws OdaException {
		try {
			return delegate.getTimestamp(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public Timestamp getTimestamp(String s) throws OdaException {
		try {
			return delegate.getTimestamp(s);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public boolean next() throws OdaException {
		try {
			return delegate.next();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public void setMaxRows(int i) throws OdaException {
		try {
			delegate.setMaxRows(i);
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}

	public boolean wasNull() throws OdaException {
		try {
			return delegate.wasNull();
		} catch (JFireOdaException e) {
			throw new OdaException(e);
		}
	}
	
	
}

