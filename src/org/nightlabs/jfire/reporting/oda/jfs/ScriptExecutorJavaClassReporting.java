package org.nightlabs.jfire.reporting.oda.jfs;

import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClass;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate;

/**
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ScriptExecutorJavaClassReporting extends ScriptExecutorJavaClass {

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
	
	public IResultSetMetaData getResultSetMetaData()
	throws ScriptException
	{
		return getReportingDelegate().getResultSetMetaData();
	}
	
	public IResultSet getResultSet()
	throws ScriptException
	{
		Object result = execute();
		try {
			return (IResultSet)result;
		} catch (ClassCastException e) {
			throw new ScriptException("The delegate of type "+getDelegate().getClass().getName()+" did not return an implementation of "+IResultSet.class.getName()+" upon execution but "+result.getClass().getName(), e);
		}
	}

}
