package org.nightlabs.jfire.scripting;

import java.util.Set;

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * <p>
 * A parameter is declared by the script and defined (i.e. parameter value passed) shortly before
 * execution. Hence, it can happen, that a script declares (see {@link ScriptRegistryItem#getParameterSet()})
 * more parameters than are passed before execution. In this case, this exception is thrown.
 * </p>
 * <p>
 * In other words: It is thrown by {@link ScriptExecutor#prepare(Script, java.util.Map)}, if not all
 * parameters declared by {@link ScriptRegistryItem#getParameterSet()} have been passed (defined).
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class UndefinedParameterValueException
		extends ScriptException
{
	private static final long serialVersionUID = 1L;

	private ScriptRegistryItemID scriptRegistryItemID;

	private Set<String> undefinedParameterIDs;

	public UndefinedParameterValueException(
			ScriptRegistryItemID scriptRegistryItemID,
			Set<String> undefinedParameterIDs)
	{
		super("Not all parameters have values assigned (" + scriptRegistryItemID.toString() + ")! Undefined: " + undefinedParameterIDs);
		this.scriptRegistryItemID = scriptRegistryItemID;
		this.undefinedParameterIDs = undefinedParameterIDs;
	}

	public ScriptRegistryItemID getScriptRegistryItemID()
	{
		return scriptRegistryItemID;
	}

	public Set<String> getUndefinedParameterIDs()
	{
		return undefinedParameterIDs;
	}
}
