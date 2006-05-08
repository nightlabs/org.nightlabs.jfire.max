/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs.server;

import java.util.Map;

import javax.jdo.PersistenceManager;

import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.reporting.JFireReportingEAR;
import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSQueryProxy;
import org.nightlabs.jfire.reporting.oda.jfs.ReportingScriptExecutor;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.ScriptExecutor;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.scripting.ScriptRegistryItem;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ServerJFSQueryProxy extends AbstractJFSQueryProxy {

	/**
	 * 
	 */
	public ServerJFSQueryProxy(Map properties) {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#close()
	 */
	public void close() throws OdaException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#executeQuery()
	 */
	public IResultSet executeQuery() throws OdaException {
		try {
			return getJFSResultSet(getScriptRegistryItemID(), getParameters());
		} catch (ModuleException e) {
			OdaException ex = new OdaException("Could not get MetaData "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMetaData()
	 */
	public IResultSetMetaData getMetaData() throws OdaException {
		try {
			return getJFSResultSetMetaData(getScriptRegistryItemID());
		} catch (ModuleException e) {
			OdaException ex = new OdaException("Could not get MetaData "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}
	
	private static Script validateScriptRegistryItem(ScriptRegistryItem item) {
		if (item == null)
			throw new NullPointerException("The ScriptRegistryItem is null ");
		if (!Script.class.isAssignableFrom(item.getClass()))
			throw new IllegalArgumentException("The ScriptRegistryItem is not an instance or subclass of Script, but "+item.getClass().getName());
		
		return (Script)item;
	}
	
	private static ReportingScriptExecutor getReportingScriptExecutor(PersistenceManager pm, ScriptRegistryItemID scriptRegistryItemID)
	throws ModuleException
	{
		Script script = validateScriptRegistryItem((ScriptRegistryItem) pm.getObjectById(scriptRegistryItemID));
		ScriptExecutor executor = null;
		try {
			executor = ScriptRegistry.getScriptRegistry(pm).createScriptExecutor(script.getLanguage());
		} catch (Exception e) {
			throw new ModuleException(e);
		}
		if (!(executor instanceof ReportingScriptExecutor))
			throw new IllegalStateException("The ScriptExecutor bound to language "+script.getLanguage()+" does not implement "+ReportingScriptExecutor.class.getName());
		return (ReportingScriptExecutor)executor;
	}
	
	public static IResultSetMetaData getJFSResultSetMetaData(ScriptRegistryItemID scriptRegistryItemID)
	throws ModuleException
	{
		Lookup lookup = JFireReportingEAR.getLookup();
		PersistenceManager pm = null;
		try {
			pm = lookup.getPersistenceManager();
			return getJFSResultSetMetaData(pm, scriptRegistryItemID);
		} finally {
			pm.close();
		}
	}
	
	public static IResultSet getJFSResultSet(ScriptRegistryItemID scriptRegistryItemID, Map parameters)
	throws ModuleException
	{
		Lookup lookup = JFireReportingEAR.getLookup();
		PersistenceManager pm = null;
		try {
			pm = lookup.getPersistenceManager();
			return getJFSResultSet(pm, scriptRegistryItemID, parameters);
		} finally {
			pm.close();
		}
	}
	
	public static IResultSetMetaData getJFSResultSetMetaData(PersistenceManager pm, ScriptRegistryItemID scriptRegistryItemID)
	throws ModuleException
	{
		return getReportingScriptExecutor(pm, scriptRegistryItemID).getResultSetMetaData();
	}

	public static IResultSet getJFSResultSet(PersistenceManager pm, ScriptRegistryItemID scriptRegistryItemID, Map parameters)
	throws ModuleException
	{
		return getReportingScriptExecutor(pm, scriptRegistryItemID).getResultSet(parameters);
	}
}
