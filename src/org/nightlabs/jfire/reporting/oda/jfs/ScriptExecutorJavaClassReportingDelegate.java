package org.nightlabs.jfire.reporting.oda.jfs;

import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.ResultSetMetaData;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate;

/**
 * Inherited ScriptExecutor delegate for JavaClasses that adds reporting functionality. 
 * Besides the {@link #getResultSetMetaData()} method script executor delegates for
 * reporting are obliged to return an implementation of {@link IResultSet}
 * in their {@link ScriptExecutorJavaClassDelegate#doExecute()} method.
 *  
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public interface ScriptExecutorJavaClassReportingDelegate extends ScriptExecutorJavaClassDelegate {
	
	/**
	 * Should return the ResultSetMetaData for this delegate.
	 * It is recomended to inherit {@link ResultSetMetaData} for the results 
	 * of this method.
	 * 
	 * @return A ResultSetMetaData describing the colums this delegate returns on execution.
	 * @throws ScriptException When the script fails to return the metadata
	 */
	IResultSetMetaData getResultSetMetaData() throws ScriptException;
	
	/**
	 * Set the {@link JFSQueryPropertySet} for the execution of this script delegate.
	 * This will be set before the associated BIRT query will be prepared.
	 * 
	 * @param queryPropertySet The properties for this script delegate.
	 */
	void setJFSQueryPropertySet(JFSQueryPropertySet queryPropertySet);
}
