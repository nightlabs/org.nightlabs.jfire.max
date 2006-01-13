package org.nightlabs.jfire.scripting;

public abstract class AbstractScriptExecutorJavaClassDelegate
		implements ScriptExecutorJavaClassDelegate
{
	private ScriptExecutorJavaClass scriptExecutorJavaClass;

	public ScriptExecutorJavaClass getScriptExecutorJavaClass()
	{
		return scriptExecutorJavaClass;
	}

	public void setScriptExecutorJavaClass(
			ScriptExecutorJavaClass scriptExecutorJavaClass)
	{
		this.scriptExecutorJavaClass = scriptExecutorJavaClass;
	}

}
