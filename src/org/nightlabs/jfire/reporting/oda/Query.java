/**
 * 
 */
package org.nightlabs.jfire.reporting.oda;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;
import org.jpox.poid.MaxPoidGenerator;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public abstract class Query implements IQuery {

	private int paramMaxID = 0;
	private Map<Integer, Object> parameters = new HashMap<Integer, Object>();
	private Map<String, Integer> paramNames = new HashMap<String, Integer>();
//	private Map<Integer, Object> 
	private Map<String, String> properties = new HashMap<String, String>();
	
	private Object appContext;
	private int maxRows = 0;
	
	private SortSpec sortSpec;
	
	/**
	 * 
	 */
	public Query() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Map getParameters() {
		return parameters;
	}
	
	public Object getParameter(String name) {
		return parameters.get(name);
	}
	
	public String getProperty(String name) {
		return properties.get(name);
	}
	
	public void setParameter(int paramID, Object value) {
//		if (!paramNames.contains(paramName))
//			paramNames.add(paramName);
		paramMaxID = Math.max(paramID, paramMaxID);
		parameters.put(new Integer(paramID), value);
	}
	

	/**
	 * Clears the parameter map.
	 */
	public void clearParameters() {
		parameters.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.report.oda.jdo.IJDOQueryProxy#setParameter(java.lang.String, java.lang.Object)
	 */
	public void setParameter(String paramName, Object value) {
		parameters.put(getParameterID(paramName), value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setAppContext(java.lang.Object)
	 */
	public void setAppContext(Object context) throws OdaException {
		this.appContext = context;
	}
	
	/**
	 * Returns the appContext set by {@link #setAppContext(Object)}
	 */
	public Object getAppContext() {
		return appContext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String name, String value) throws OdaException {
		properties.put(name, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setMaxRows(int)
	 */
	public void setMaxRows(int max) throws OdaException {
		this.maxRows = max;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMaxRows()
	 */
	public int getMaxRows() throws OdaException {
		return maxRows;
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
	
	public int getParameterID(String paramName) {
		Integer paramID = paramNames.get(paramName);
		if (paramID == null) {
			paramID = new Integer(paramMaxID++);
			paramNames.put(paramName, paramID);
		}
		return paramID.intValue();
	}
	
	public String getParameterName(int paramId) {
		// TODO: Improve param name resolving
		Integer intID = new Integer(paramId);
		if (paramNames.containsValue(intID)) {
			for (Map.Entry entry : paramNames.entrySet()) {
				if (entry.getValue().equals(intID))
					return (String)entry.getKey();
			}
		}
		return null;
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
		this.sortSpec = sortBy;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getSortSpec()
	 */
	public SortSpec getSortSpec() throws OdaException {
		return sortSpec;
	}
}
