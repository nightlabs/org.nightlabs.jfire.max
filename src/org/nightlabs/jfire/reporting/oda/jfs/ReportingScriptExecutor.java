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
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public interface ReportingScriptExecutor {

	/**
	 * Returns the ODA IResultSetMetaData for the given script.
	 *  
	 * @param script The script to execute.
	 * @return The ODA IResultSetMetaData for the given script.
	 * @throws ScriptException Might throw a ScriptException.
	 */
	public IResultSetMetaData getResultSetMetaData(Script script) throws ScriptException;
	
	/**
	 * Returns the ODA IResultSet for the given script.
	 *  
	 * @param script The script to execute.
	 * @return The ODA IResultSet for the given script.
	 * @throws ScriptException Might throw a ScriptException.
	 */
	public IResultSet getResultSet(Script script, Map<String, Object> parameters) throws ScriptException;
}
