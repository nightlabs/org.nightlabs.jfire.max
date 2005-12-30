package org.nightlabs.jfire.reporting.oda.jdoql;

import org.eclipse.datatools.connectivity.oda.IQuery;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public interface IJDOQueryProxy extends IQuery {
	
	/**
	 * Definition of JDOQL as query lanuage
	 */
	public static final String LANGUAGE_JDOQL = "javax.jdo.query.JDOQL";
	
	/**
	 * Set the single-string JDOQL query that should be executed.
	 * 
	 * @param query The single-string JDOQL query to be executed 
	 */
	public void setQuery(String query);
	
	/**
	 * Clear the current parameter-mapping.
	 */
	public void clearParameters();

	/**
	 * Defines a parameter mapping. The JDOQLQuery will be executed with
	 * the given value of its parameter with name paramName.
	 * 
	 * @param paramName The name of the parameter to set.
	 * @param param The value of the parameter to set.
	 */
	public void setParameter(String paramName, Object value);
}
