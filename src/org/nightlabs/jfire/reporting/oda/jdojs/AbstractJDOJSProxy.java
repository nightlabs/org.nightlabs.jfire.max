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

package org.nightlabs.jfire.reporting.oda.jdojs;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;

/**
 * Base type for a JDO JavaScript Query (DataSet) in JFireReporting.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public abstract class AbstractJDOJSProxy implements IJDOJSProxy {

	
	private Map parameters = new HashMap();
	private Map properties = new HashMap();
	
	private String prepareScript;
	private String fetchScript;
	

	public void setPrepareScript(String prepareScript) {
		this.prepareScript = prepareScript;
	}
	
	/**
	 * Returns the prepare JavaScript
	 */
	public String getPrepareScript() {
		if (prepareScript == null)
			prepareScript = (String)properties.get(IJDOJSProxy.PROPERTY_PREPARE_SCRIPT);
		return prepareScript;
	}

	/**
	 * Returns the fetch JavaScript
	 */
	public String getFetchScript() {
		if (fetchScript == null)
			fetchScript = (String)properties.get(IJDOJSProxy.PROPERTY_FETCH_SCRIPT);
		return fetchScript;
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.report.oda.jdo.IJDOQueryProxy#clearParameters()
	 */
	public void clearParameters() {
		parameters.clear();
	}
	
	public Map getParameters() {
		return parameters;
	}

//	/* (non-Javadoc)
//	 * @see org.nightlabs.report.oda.jdo.IJDOQueryProxy#setParameter(java.lang.String, java.lang.Object)
//	 */
//	public void setParameter(String paramName, Object value) {
//		if (!paramNames.contains(paramName))
//			paramNames.add(paramName);
//		parameters.put(paramName, value);
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.report.oda.jdo.IJDOQueryProxy#setParameter(java.lang.String, java.lang.Object)
	 */
	public void setParameter(int paramID, Object value) {
//		if (!paramNames.contains(paramName))
//			paramNames.add(paramName);
		parameters.put(new Integer(paramID), value);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setAppContext(java.lang.Object)
	 */
	public void setAppContext(Object context) throws OdaException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String name, String value) throws OdaException {
		properties.put(name, value);
		System.out.println("******** Set property name "+name+" value "+value);
	}
	
	/**
	 * Returns the properties of thsi proxy.
	 */
	public Map getProperties() {
		return properties;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#close()
	 */
	public void close() throws OdaException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setMaxRows(int)
	 */
	public void setMaxRows(int max) throws OdaException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMaxRows()
	 */
	public int getMaxRows() throws OdaException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#clearInParameters()
	 */
	public void clearInParameters() throws OdaException {
		clearParameters();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setInt(java.lang.String, int)
	 */
	public void setInt(String parameterName, int value) throws OdaException {
		setInt(getParameterID(parameterName), value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setInt(int, int)
	 */
	public void setInt(int parameterId, int value) throws OdaException {
		setParameter(parameterId, new Integer(value));
	}
	
//	public String getParameterName(int parameterId) {
//		if (paramNames.size() > 0 && paramNames.size()+1 >= parameterId ) {
//			return (String)paramNames.get(parameterId-1);
//		}
//		else
//			throw new IllegalArgumentException("Parameter with id "+parameterId+" was not set yet");
//	}
	
	public int getParameterID(String paramName) {
		// TODO: implement !!
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDouble(java.lang.String, double)
	 */
	public void setDouble(String parameterName, double value) throws OdaException {
		setDouble(getParameterID(parameterName), value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDouble(int, double)
	 */
	public void setDouble(int parameterId, double value) throws OdaException {
		setParameter(parameterId, new Double(value));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setBigDecimal(java.lang.String, java.math.BigDecimal)
	 */
	public void setBigDecimal(String parameterName, BigDecimal value) throws OdaException {
		setBigDecimal(getParameterID(parameterName), value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setBigDecimal(int, java.math.BigDecimal)
	 */
	public void setBigDecimal(int parameterId, BigDecimal value) throws OdaException {
		setParameter(parameterId, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setString(java.lang.String, java.lang.String)
	 */
	public void setString(String parameterName, String value) throws OdaException {
		setString(getParameterID(parameterName), value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setString(int, java.lang.String)
	 */
	public void setString(int parameterId, String value) throws OdaException {
		setParameter(parameterId, value);
	}

	/**
	 * Puts a parameter of type java.util.Date
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDate(java.lang.String, java.sql.Date)
	 */
	public void setDate(String parameterName, Date value) throws OdaException {
		setDate(getParameterID(parameterName), value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDate(int, java.sql.Date)
	 */
	public void setDate(int parameterId, Date value) throws OdaException {
		setParameter(parameterId, new java.util.Date(value.getTime()));
	}

	/**
	 * Puts a parameter of type java.util.Date
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTime(java.lang.String, java.sql.Time)
	 */
	public void setTime(String parameterName, Time value) throws OdaException {
		setTime(getParameterID(parameterName), value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTime(int, java.sql.Time)
	 */
	public void setTime(int parameterId, Time value) throws OdaException {
		setParameter(parameterId, new java.util.Date(value.getTime()));
	}

	/**
	 * Puts a parameter of type java.util.Date
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	public void setTimestamp(String parameterName, Timestamp value) throws OdaException {
		setTimestamp(getParameterID(parameterName), value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTimestamp(int, java.sql.Timestamp)
	 */
	public void setTimestamp(int parameterId, Timestamp value) throws OdaException {
		setParameter(parameterId, new java.util.Date(value.getTime()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#findInParameter(java.lang.String)
	 */
	public int findInParameter(String parameterName) throws OdaException {
		return getParameterID(parameterName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getParameterMetaData()
	 */
	public IParameterMetaData getParameterMetaData() throws OdaException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setSortSpec(org.eclipse.datatools.connectivity.oda.SortSpec)
	 */
	public void setSortSpec(SortSpec sortBy) throws OdaException {
		// TODO implement ?
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getSortSpec()
	 */
	public SortSpec getSortSpec() throws OdaException {
		// TODO implement ?
		return null;
	}

}
