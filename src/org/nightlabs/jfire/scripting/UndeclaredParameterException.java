package org.nightlabs.jfire.scripting;

import java.util.Set;

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * <p>
 * A parameter is declared by the script and defined (i.e. parameter value passed) shortly before
 * execution. Hence, it can happen, that a script does not declare (see {@link ScriptRegistryItem#getParameterSet()})
 * a parameter that is passed before execution. In this case, this exception is thrown.
 * </p>
 * <p>
 * In other words: It is thrown by {@link ScriptExecutor#prepare(Script, java.util.Map)}, if not all
 * parameters passed to the {@link ScriptExecutor} are declared by {@link ScriptRegistryItem#getParameterSet()}.
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class UndeclaredParameterException
		extends ScriptException
{
	private static final long serialVersionUID = 1L;

	private ScriptRegistryItemID scriptRegistryItemID;

	private Set<String> undeclaredParameterIDs;

	public UndeclaredParameterException(
			ScriptRegistryItemID scriptRegistryItemID,
			Set<String> undefinedParameterIDs)
	{
		super("Not all parameters for which values were passed are declared (" + scriptRegistryItemID.toString() + ")! Undeclared: " + undefinedParameterIDs);
		this.scriptRegistryItemID = scriptRegistryItemID;
		this.undeclaredParameterIDs = undefinedParameterIDs;
	}

	public ScriptRegistryItemID getScriptRegistryItemID()
	{
		return scriptRegistryItemID;
	}

	public Set<String> getUndeclaredParameterIDs()
	{
		return undeclaredParameterIDs;
	}
}
