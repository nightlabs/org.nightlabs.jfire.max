package org.nightlabs.jfire.scripting;

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

public class ResultClassMismatchException
		extends ScriptException
{
	private static final long serialVersionUID = 1L;

	private ScriptRegistryItemID scriptRegistryItemID;
	private Class expectedResultClass;
	private Class actualResultClass;

	public ResultClassMismatchException(
			ScriptRegistryItemID scriptRegistryItemID,
			Class expectedResultClass,
			Class actualResultClass)
	{
		super("Expected and actual result class do not match (" + scriptRegistryItemID.toString() + ")! Expected: " + expectedResultClass.getName() + " Actual: " + actualResultClass.getName());
		this.scriptRegistryItemID = scriptRegistryItemID;
		this.expectedResultClass = expectedResultClass;
		this.actualResultClass = actualResultClass;
	}

	public Class getActualResultClass()
	{
		return actualResultClass;
	}

	public Class getExpectedResultClass()
	{
		return expectedResultClass;
	}

	public ScriptRegistryItemID getScriptRegistryItemID()
	{
		return scriptRegistryItemID;
	}

}
