package org.nightlabs.jfire.reporting.oda.jfs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.jfire.reporting.JFireReportingHelper;
import org.nightlabs.jfire.reporting.oda.server.jfs.ServerJFSQueryProxy;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClass;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate;
import org.nightlabs.jfire.scripting.ScriptParameterSet;
import org.nightlabs.jfire.scripting.ScriptRegistry;

/**
 * When JFireReporting is deployed this class is registered to the {@link ScriptRegistry}
 * to be the executor for scripts with the language {@link ScriptExecutorJavaClass#LANGUAGE_JAVA_CLASS}.
 * <p>
 * It implements the {@link ReportingScriptExecutor} interface and serves as helper
 * for the JFS ODA Driver. See {@link ServerJFSQueryProxy} for an the usage of this executor.
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ScriptExecutorJavaClassReporting
extends ScriptExecutorJavaClass
implements ReportingScriptExecutor
{

	/**
	 * 
	 */
	public ScriptExecutorJavaClassReporting() {
		super();
	}

	/**
	 * Returns the ready casted delegate.
	 * 
	 * @throws ScriptException when the delegate can't be casted to {@link ScriptExecutorJavaClassReportingDelegate}
	 */
	protected ScriptExecutorJavaClassReportingDelegate getReportingDelegate(JFSQueryPropertySet queryPropertySet)
	throws ScriptException
	{
		ScriptExecutorJavaClassDelegate delegate = getDelegate();
		ScriptExecutorJavaClassReportingDelegate reportingDelegate = null;
		try {
			reportingDelegate = (ScriptExecutorJavaClassReportingDelegate) delegate;
		} catch (ClassCastException e) {
			throw new ScriptException("Delegate of type " + delegate.getClass().getName() + " does not implement " + ScriptExecutorJavaClassReportingDelegate.class.getName(), e);
		}
		reportingDelegate.setJFSQueryPropertySet(queryPropertySet);
		return reportingDelegate;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation will first fake the params for the delegate, meaning it will put
	 * itself(this) as value for all required parameters, in order to be able to prepare
	 * the script. The script meeds to be prepared as {@link #getDelegate()} will
	 * throw an IllegalStateException if not.
	 * <p>
	 * After preparation the {@link ScriptExecutorJavaClassReportingDelegate#getResultSetMetaData()}
	 * method will be called for the delegate to obtain the meta data.
	 * 
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ReportingScriptExecutor#getResultSetMetaData(org.nightlabs.jfire.scripting.Script, JFSQueryPropertySet)
	 */
	public IResultSetMetaData getResultSetMetaData(Script script, JFSQueryPropertySet queryPropertySet)
	throws ScriptException
	{
		ScriptParameterSet parameterSet = script.getParameterSet();
		Map<String, Object> fakeParams = null;
		if (parameterSet != null) {
			Set<String> ids = parameterSet.getParameterIDs();
			fakeParams = new HashMap<String, Object>();
			for (String id : ids) {
				fakeParams.put(id, this);
			}
		}
		prepare(script, fakeParams);
		ScriptExecutorJavaClassReportingDelegate reportingDelegate = getReportingDelegate(queryPropertySet);
		return reportingDelegate.getResultSetMetaData();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Prepares and executes the Query. It assumes the result to be an instance
	 * of {@link IResultSet} and throws an {@link ScriptException} otherwise.
	 * </p>
	 * <p>
	 * Additionally it will convert the parameters of the script to the value
	 * according to {@link JFireReportingHelper#getDataSetParamObject(Object)} in
	 * order to provide the scripts with the actual object parameter instead of
	 * its String representation.
	 * </p>
	 * 
	 * @throws ScriptException Not only when execution fails, but also if the returned result is
	 * 	not of type {@link IResultSet}.
	 * 
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ReportingScriptExecutor#getResultSet(org.nightlabs.jfire.scripting.Script, java.util.Map, JFSQueryPropertySet)
	 */
	public IResultSet getResultSet(Script script, Map<String, Object> parameters, JFSQueryPropertySet queryPropertySet)
	throws ScriptException
	{
		Map<String, Object> convertedParams = convertParameters(parameters);
		prepare(script, convertedParams);
		getReportingDelegate(queryPropertySet);
		Object result = execute();
		try {
			return (IResultSet) result;
		} catch (ClassCastException e) {
			throw new ScriptException("The delegate of type "+getDelegate().getClass().getName()+" did not return an implementation of "+IResultSet.class.getName()+" upon execution but "+result.getClass().getName(), e);
		}
	}
	
	/**
	 * Converts the parameters given (eventually the data-set-parameters binded to a use of a BIRT dataset).
	 * It will check if it either finds {@link JFSParameterUtil#DUMMY_DEFAULT_PARAMETER_VALUE} or a serialized
	 * Object parameter.
	 * 
	 * @param parameters The parameters to convert.
	 * @return The converted parameters.
	 */
	protected Map<String, Object> convertParameters(Map<String, Object> parameters) {
		Map<String, Object> convertedParams = new HashMap<String, Object>();
		for (Entry<String, Object> entry : parameters.entrySet()) {
			if (JFSParameterUtil.DUMMY_DEFAULT_PARAMETER_VALUE.equals(entry.getValue()))
				convertedParams.put(entry.getKey(), null);
			else
				convertedParams.put(entry.getKey(), JFireReportingHelper.getDataSetParamObject(entry.getValue()));
		}
		return convertedParams;
	}

	

}
