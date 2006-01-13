package org.nightlabs.jfire.scripting;

/**
 * It's recommended to extend {@link AbstractScriptExecutorJavaClassDelegate}.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface ScriptExecutorJavaClassDelegate
{
	public ScriptExecutorJavaClass getScriptExecutorJavaClass();
	public void setScriptExecutorJavaClass(ScriptExecutorJavaClass scriptExecutorJavaClass);

	public void doPrepare()
	throws ScriptException;

	public Object doExecute()
	throws ScriptException;
}
