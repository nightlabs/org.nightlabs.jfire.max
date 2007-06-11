/**
 * 
 */
package org.nightlabs.jfire.reporting.oda.jfs;

import org.nightlabs.jfire.scripting.AbstractScriptExecutorJavaClassDelegate;

/**
 * Base class for {@link ScriptExecutorJavaClassReportingDelegate}s, that serve as 
 * datasource for BIRT queries.
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


	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReportingDelegate#setJFSQueryPropertySet(org.nightlabs.jfire.reporting.oda.jfs.JFSQueryPropertySet)
	 */
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
}
