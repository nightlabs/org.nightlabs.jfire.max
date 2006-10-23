/**
 * 
 */
package org.nightlabs.jfire.reporting.oda;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jdo.PersistenceManager;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;
import org.nightlabs.jfire.reporting.JFireReportingHelper;

/**
 * Common implementation of {@link IQuery} that can be (and is) used 
 * by all ODA drivers in JFireReporting project.
 * <p>
 * It handles parameters and properties for the query, so the actual
 * driver only has to implement the data acquisition based on these values.
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public abstract class Query implements IQuery {

	public static final String PARAM_NAME_PREFIX = "param";
	
	private int paramMaxID = 0;
//	private ParameterMetaData parameterMetaData = null;
	private SortedMap<Integer, Object> parameters = new TreeMap<Integer, Object>();
	private Map<String, Integer> paramNames = new HashMap<String, Integer>();
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

	/**
	 * Returns all parameter values in a {@link List}.
	 * 
	 * @return All parameter values in a {@link List}.
	 */
	public List<Object> getParameters() {
		List<Object> result = new ArrayList<Object>(parameters.size());
		// iterating the sorted map will give the values
		// sorted by the keys in ascending order, so
		// they will be sorted by their position.
		for (Object param : parameters.values()) {
			result.add(param);
		}
		return result;
	}

	/**
	 * Returns all parameter values associated to a parameter name.
	 * If no name could be assigned to a parameter by quering the
	 * {@link IParameterMetaData} (beeing {@link NamedParameterMetaData})
	 * than the parameter will be named in the schema {@link #PARAM_NAME_PREFIX}+parameterPosition.
	 * The parameterPosition is 1-based so this will result in param1, param2 ...
	 * 
	 * @return All parameter values associated to a name.
	 * @throws OdaException
	 */
	public Map<String, Object> getNamedParameters()
	throws OdaException
	{
		mapParameterNames();
		Map<String, Object> namedParams = new HashMap<String, Object>(parameters.size());
		for (Map.Entry<String, Integer> nameEntry : paramNames.entrySet()) {
			namedParams.put(nameEntry.getKey(), parameters.get(nameEntry.getValue()));
		}
		return namedParams;
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

	/**
	 * 
	 * @param paramName
	 * @param value
	 */
	public void setParameter(String paramName, Object value)
	throws OdaException
	{
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

	/**
	 * Maps the parameter set to their names. The names are either determined
	 * by the ParameterMetaData of this Query (in case it implements {@link NamedParameterMetaData}).
	 * All additional parameters are named "param{PARAM_NO}"
	 * 
	 * @throws OdaException
	 */
	private void mapParameterNames() 
	throws OdaException 
	{
		IParameterMetaData pMetaData = getParameterMetaData();
		int mParamIDFromMetaData = 0;
		paramNames.clear();
		if (pMetaData instanceof NamedParameterMetaData) {
			NamedParameterMetaData nMetaData = (NamedParameterMetaData)pMetaData;
			for (mParamIDFromMetaData = 1; mParamIDFromMetaData<=nMetaData.getParameterCount(); mParamIDFromMetaData++) {
				paramNames.put(nMetaData.getParameterName(mParamIDFromMetaData), mParamIDFromMetaData);
			}
		}
		for (int i = mParamIDFromMetaData +1; i<=parameters.size(); i++) {
			paramNames.put("param"+i, i);
		}
	}
	
	public int getParameterID(String paramName)
	throws OdaException
	{
		mapParameterNames();
		Integer paramID = paramNames.get(paramName);
		if (paramID == null) {
			paramID = new Integer(paramMaxID++);
			paramNames.put(paramName, paramID);
		}
		return paramID.intValue();
	}
	
	public String getParameterName(int paramId)
	throws OdaException
	{
		mapParameterNames();
		Integer intID = new Integer(paramId);
		if (paramNames.containsValue(intID)) {
			for (Map.Entry entry : paramNames.entrySet()) {
				if (entry.getValue().equals(intID))
					return (String)entry.getKey();
			}
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns null.
	 * 
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getParameterMetaData()
	 */
	public IParameterMetaData getParameterMetaData() throws OdaException {
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
	
	/**
	 * Uses the {@link JFireReportingHelper} to obtain a 
	 * {@link PersistenceManager} for the currently rendered BIRT report.
	 * <p>
	 * Please not that this method can 
	 * <b>only be used within a JFire J2EE Server environment</b>
	 * <p>
	 * Most likely it will be used by subclasses of Query to provide access
	 * to the datastore.
	 * 
	 * @return The {@link PersistenceManager} for this query obtained by the {@link JFireReportingHelper}.
	 */
	public PersistenceManager getPersistenceManager() {
		return JFireReportingHelper.getPersistenceManager();
	}
}
