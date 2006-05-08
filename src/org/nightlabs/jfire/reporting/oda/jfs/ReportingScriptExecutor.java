/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import java.util.Map;

import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public interface ReportingScriptExecutor {

	public IResultSetMetaData getResultSetMetaData();
	
	public IResultSet getResultSet(Map parameters);
}
