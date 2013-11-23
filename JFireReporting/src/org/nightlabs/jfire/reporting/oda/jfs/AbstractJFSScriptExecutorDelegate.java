/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import org.nightlabs.jfire.scripting.AbstractScriptExecutorJavaClassDelegate;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptExecutorJavaClass;

/**
 * Base class for {@link ScriptExecutorJavaClassReportingDelegate}s, that serve as
 * data-source for BIRT queries.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public abstract class AbstractJFSScriptExecutorDelegate
extends AbstractScriptExecutorJavaClassDelegate
implements ScriptExecutorJavaClassReportingDelegate
{

	private JFSQueryPropertySet queryPropertySet;
	
	/**
	 * 
	 */
	public AbstractJFSScriptExecutorDelegate() {
	}


	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate#setJFSQueryPropertySet(org.nightlabs.jfire.reporting.oda.jfs.JFSQueryPropertySet)
	 */
	@Override
	public void setJFSQueryPropertySet(JFSQueryPropertySet queryPropertySet) {
		this.queryPropertySet = queryPropertySet;
	}

	/**
	 * Returns the {@link JFSQueryPropertySet} of this delegate,
	 * that were set before prepare and getMetaData.
	 * 
	 * @return The {@link JFSQueryPropertySet} of this delegate,
	 * 		that were set before prepare and getMetaData.
	 */
	public JFSQueryPropertySet getJFSQueryPropertySet() {
		return queryPropertySet;
	}

	/**
	 * Get the value of the data-set parameter with the given parameterName. This will be the value
	 * that was bound to the data-set in the BIRT report layout.
	 * <p>
	 * Note, that this parameters are set by the execution layer and might be de-serialised object
	 * parameter values.
	 * </p>
	 * 
	 * @param <T> The type of parameter expected.
	 * @param parameterName The name of the parameter.
	 * @param clazz The type of the parameter expected.
	 * @return The value of the parameter with the given name, or <code>null</code> if that
	 *         parameter is not defined.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getObjectParameterValue(String parameterName, Class<T> clazz) {
		Object obj = getParameterValue(parameterName);
		if ("".equals(obj) || JFSParameterUtil.DUMMY_DEFAULT_PARAMETER_VALUE.equals(obj))
			return null;
		return (T) obj;
	}
	
	/**
	 * Default implementation of this method returns an empty instance of {@link JFSQueryPropertySetMetaData}.
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate#getJFSQueryPropertySetMetaData()
	 */
	@Override
	public IJFSQueryPropertySetMetaData getJFSQueryPropertySetMetaData() {
		return new JFSQueryPropertySetMetaData();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation does nothing.
	 * </p>
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#doPrepare()
	 */
	public void doPrepare() throws ScriptException {
	}
	
	private ScriptExecutorJavaClass scriptExecutorJavaClass;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#getScriptExecutorJavaClass()
	 */
	@Override
	public ScriptExecutorJavaClass getScriptExecutorJavaClass() {
		return scriptExecutorJavaClass;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptExecutorJavaClassDelegate#setScriptExecutorJavaClass(org.nightlabs.jfire.scripting.ScriptExecutorJavaClass)
	 */
	@Override
	public void setScriptExecutorJavaClass(ScriptExecutorJavaClass scriptExecutorJavaClass) {
		this.scriptExecutorJavaClass = scriptExecutorJavaClass;
	}
	
}
