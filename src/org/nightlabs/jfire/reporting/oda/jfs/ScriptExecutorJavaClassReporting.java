package org.nightlabs.jfire.reporting.oda.jfs;

import java.util.Map;

import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClass;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate;

/**
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ScriptExecutorJavaClassReporting extends ScriptExecutorJavaClass implements ReportingScriptExecutor {

	/**
	 * 
	 */
	public ScriptExecutorJavaClassReporting() {
		super();
	}

	/**
	 * Returns the ready casted delegate.
	 */
	protected ScriptExecutorJavaClassReportingDelegate getReportingDelegate()
	throws ScriptException
	{
		ScriptExecutorJavaClassDelegate delegate = getDelegate();
		try {
			return (ScriptExecutorJavaClassReportingDelegate)delegate;
		} catch (ClassCastException e) {
			throw new ScriptException("Delegate of type "+delegate.getClass().getName()+" does not implement "+ScriptExecutorJavaClassReportingDelegate.class.getName(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ReportingScriptExecutor#getResultSetMetaData(org.nightlabs.jfire.scripting.Script)
	 */
	public IResultSetMetaData getResultSetMetaData(Script script)
	throws ScriptException
	{
		prepare(script, null, false);
		return getReportingDelegate().getResultSetMetaData();
	}

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ReportingScriptExecutor#getResultSet(org.nightlabs.jfire.scripting.Script, java.util.Map)
	 */
	public IResultSet getResultSet(Script script, Map<String, Object> parameters)
	throws ScriptException
	{
		prepare(script, parameters);
		Object result = execute();
		try {
			return (IResultSet)result;
		} catch (ClassCastException e) {
			throw new ScriptException("The delegate of type "+getDelegate().getClass().getName()+" did not return an implementation of "+IResultSet.class.getName()+" upon execution but "+result.getClass().getName(), e);
		}
	}

	

}
