/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs.server;

import java.math.BigDecimal;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.reporting.oda.ParameterMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSQueryProxy;
import org.nightlabs.jfire.reporting.oda.jfs.ReportingScriptExecutor;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.ScriptExecutor;
import org.nightlabs.jfire.scripting.ScriptParameterSet;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.scripting.ScriptRegistryItem;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * Actual implementation of the JFS ODA Query. 
 * <p>
 * When this driver is queried it will try to lookup
 * a {@link Script} that, when executed by the right {@link ScriptExecutor} (see {@link ReportingScriptExecutor}),
 * will provide ODA result set data.
 *  
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

	private IParameterMetaData parameterMetaData;
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.AbstractJFSQueryProxy#getParameterMetaData()
	 */
	@Override
	public IParameterMetaData getParameterMetaData() 
	throws OdaException 
	{
		if (parameterMetaData == null) {
			ScriptRegistryItemID itemID = getScriptRegistryItemID();
			parameterMetaData = getScriptParameterMetaData(itemID);
		}
		return parameterMetaData;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Calls {@link #getJFSResultSet(ScriptRegistryItemID, Map)} with the
	 * parameters of this script.
	 * 
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#executeQuery()
	 */
	public IResultSet executeQuery() throws OdaException {
		try {
			return getJFSResultSet(getScriptRegistryItemID(), getNamedParameters());
		} catch (ModuleException e) {
			OdaException ex = new OdaException("Could not get ResultSet "+e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Calls {@link #getJFSResultSetMetaData(ScriptRegistryItemID)} with the
	 * {@link ScriptRegistryItemID} associated to the calling data set.
	 *  
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
	
	/**
	 * Returns the script associated to the dataset. Scripts are associated
	 * by referencing them with the String representation of their {@link ScriptRegistryItemID} 
	 * int the query property of the dataset.
	 * 
	 * @param pm The PersistenceManager to lookup the script with.
	 * @param itemID The script's id.
	 * @return An instance of {@link Script}. Note that and {@link IllegalArgumentException} will be
	 * 	thrown when the script registry item referenced is not a Script (maybe a category).
	 */
	private static Script getScript(PersistenceManager pm, ScriptRegistryItemID itemID) {
		return validateScriptRegistryItem((ScriptRegistryItem) pm.getObjectById(itemID));
	}
	
	/**
	 * Checks whether the item is a {@link Script}
s	 */
	private static Script validateScriptRegistryItem(ScriptRegistryItem item) {
		if (item == null)
			throw new NullPointerException("The ScriptRegistryItem is null ");
		if (!Script.class.isAssignableFrom(item.getClass()))
			throw new IllegalArgumentException("The ScriptRegistryItem is not an instance or subclass of Script, but "+item.getClass().getName());
		
		return (Script)item;
	}	

	/**
	 * Lookup and create a new executor for the given script.
	 * <p>
	 * This method checks if the executor implements the {@link ReportingScriptExecutor}
	 * interface so it can be used to generate ODA result set data, if not an {@link IllegalStateException}
	 * will be thrown.
	 *  
	 * @param pm The PersistenceManager to use.
	 * @param script The Script to create the executor for.
	 * @return A {@link ReportingScriptExecutor} for the given script.
	 * @throws ModuleException When something fails while looking up the executor.
	 */
	private static ReportingScriptExecutor createReportingScriptExecutor(PersistenceManager pm, Script script)
	throws ModuleException
	{
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
	
	/**
	 * Obtains a (new ?!?) PersistenceManager with the help of {@link Lookup}.
	 */
	public static PersistenceManager getPersistenceManager() 
	{
		Lookup lookup;
		lookup = new Lookup(SecurityReflector.getUserDescriptor().getOrganisationID());
		return lookup.getPersistenceManager();
	}
	
	/**
	 * Shorcut to {@link #getJFSResultSetMetaData(PersistenceManager, ScriptRegistryItemID)}
	 */
	public static IResultSetMetaData getJFSResultSetMetaData(ScriptRegistryItemID scriptRegistryItemID)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return getJFSResultSetMetaData(pm, scriptRegistryItemID);
		} finally {
			pm.close();
		}
	}

	/**
	 * Shortcut to #getJFSResultSet(PersistenceManager, ScriptRegistryItemID, Map<String, Object>)}
	 */
	public static IResultSet getJFSResultSet(ScriptRegistryItemID scriptRegistryItemID, Map<String, Object> parameters)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {			
			return getJFSResultSet(pm, scriptRegistryItemID, parameters);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Lookup the {@link ReportingScriptExecutor} and let him execute {@link ReportingScriptExecutor#getResultSetMetaData(Script)}.
	 * 
	 * @param pm The PersistenceManager to lookup the executor.
	 * @param scriptRegistryItemID The scriptRegistryItemID that will be delegate to (does the real work).
	 * @return An {@link IResultSetMetaData} created by the Script referenced by the given scriptRegistryItemID.
	 * @throws ModuleException
	 */
	public static IResultSetMetaData getJFSResultSetMetaData(PersistenceManager pm, ScriptRegistryItemID scriptRegistryItemID)
	throws ModuleException
	{
		Script script = getScript(pm, scriptRegistryItemID);
		return createReportingScriptExecutor(pm, script).getResultSetMetaData(script);
	}

	/**
	 * Lookup the {@link ReportingScriptExecutor} and let him execute {@link ReportingScriptExecutor#getResultSet(Script, Map)}.
	 * 
	 * @param pm The PersistenceManager to lookup the executor.
	 * @param scriptRegistryItemID The scriptRegistryItemID that will be delegate to (does the real work).
	 * @param parameters The parameters for the script to execute.
	 * @return An {@link IResultSetMetaData} created by the Script referenced by the given scriptRegistryItemID.
	 * 
	 * @throws ModuleException
	 */
	public static IResultSet getJFSResultSet(PersistenceManager pm, ScriptRegistryItemID scriptRegistryItemID, Map<String, Object> parameters)
	throws ModuleException
	{
		Script script = getScript(pm, scriptRegistryItemID);
		// if this script relies on a persistenceManager parameter, provide one ;-)
		// this is legacy code (JavaClass)scripts can now obtain the pm from their executor
		if (script.getParameterSet().getParameter("persistenceManager", false) != null)
			parameters.put("persistenceManager", pm);
		return createReportingScriptExecutor(pm, script).getResultSet(script, parameters);
	}
	
	public static IParameterMetaData getScriptParameterMetaData(ScriptRegistryItemID itemID)
	throws OdaException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return getScriptParameterMetaData(pm, itemID);
		} finally {
			pm.close();
		}
	}
	
	public static IParameterMetaData getScriptParameterMetaData(PersistenceManager pm, ScriptRegistryItemID itemID)
	throws OdaException
	{
		Script script = getScript(pm, itemID);
		ScriptParameterSet paramSet = script.getParameterSet();
		return ParameterMetaData.createMetaDataFromParameterSet(paramSet);
	}
}
