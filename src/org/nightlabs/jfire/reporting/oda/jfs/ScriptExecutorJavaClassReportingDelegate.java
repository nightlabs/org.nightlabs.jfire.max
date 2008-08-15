package org.nightlabs.jfire.reporting.oda.jfs;

import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.ResultSetMetaData;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate;

/**
 * Inherited ScriptExecutor delegate for JavaClasses that adds reporting functionality. 
 * Implementations of this delegate interface are required to provide ODA meta-data
 * in the {@link #getResultSetMetaData()} method and optionally might define meta-data
 * for the {@link JFSQueryPropertySet} and return it in {@link #getJFSQueryPropertySetMetaData()}.
 * 
 * Additionally script executor delegates for reporting are obliged to return an implementation 
 * of {@link IResultSet} in their {@link ScriptExecutorJavaClassDelegate#doExecute()} method.
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 */
public interface ScriptExecutorJavaClassReportingDelegate extends ScriptExecutorJavaClassDelegate {
	
	/**
	 * Should return the ResultSetMetaData for this delegate.
	 * It is recommended to inherit {@link ResultSetMetaData} for the results
	 * of this method.
	 * 
	 * @return A ResultSetMetaData describing the columns this delegate returns on execution.
	 * @throws ScriptException When the script fails to return the meta-data
	 */
	IResultSetMetaData getResultSetMetaData() throws ScriptException;
	
	/**
	 * Returns an instance of {@link IJFSQueryPropertySetMetaData} that describes
	 * the properties this delegate accepts or even needs to run properly.
	 * 
	 * @return An instance of {@link IJFSQueryPropertySetMetaData} with a description of the required/accepted properties.
	 */
	IJFSQueryPropertySetMetaData getJFSQueryPropertySetMetaData();
	
	/**
	 * Set the {@link JFSQueryPropertySet} for the execution of this script delegate.
	 * This will be set before the associated BIRT query will be prepared and 
	 * thus can be accessed also at the time the ODA meta-data has to be returned.
	 * 
	 * @param queryPropertySet The properties for this script delegate.
	 */
	void setJFSQueryPropertySet(JFSQueryPropertySet queryPropertySet);
}
