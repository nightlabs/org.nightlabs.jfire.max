/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import java.util.Map;

import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.ScriptException;

/**
 * Common interface for {@link ScriptExecutor}s (JavaClass, JavaScript)
 * that are used for BIRT reporting tasks.
 * <p>
 * Implementations of this (= extensions of basic script executors) are
 * registered to the {@link ScriptRegistry} as executors when JFireReporting
 * is deployed.
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public interface ReportingScriptExecutor {

	/**
	 * Returns the ODA IResultSetMetaData for the given script.
	 * 
	 * @param script The script to execute.
	 * @param queryPropertySet The property set for the query. It is build from the query string.
	 * @return The ODA IResultSetMetaData for the given script.
	 * @throws ScriptException Might throw a ScriptException.
	 */
	public IResultSetMetaData getResultSetMetaData(Script script, JFSQueryPropertySet queryPropertySet) throws ScriptException;
	
	/**
	 * Returns the ODA IResultSet for the given script.
	 * 
	 * @param script The script to execute.
	 * @param parameters The parameters for the script execution.
	 * @param queryPropertySet The property set for the query. It is build from the query string.
	 * @return The ODA IResultSet for the given script.
	 * @throws ScriptException Might throw a ScriptException.
	 */
	public IResultSet getResultSet(Script script, Map<String, Object> parameters, JFSQueryPropertySet queryPropertySet) throws ScriptException;
}
