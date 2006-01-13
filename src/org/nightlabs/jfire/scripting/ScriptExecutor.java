package org.nightlabs.jfire.scripting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * <p>
 * This is the base class for all script executors. A script executor is created
 * on the fly in order to execute one or more scripts. After it has been used, it
 * is quickly discarded.
 * </p>
 * <p>
 * The ScriptExecutor is created via
 * {@link ScriptRegistry#createScriptExecutor(String)}. As
 * one executor can execute many scripts, you can cache the executors - but only
 * within a transaction.
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public abstract class ScriptExecutor
{
	private boolean prepared = false;
	private Script script;
	private Map<String, Object> parameterValues;

	/**
	 * @param script The script that shall be executed.
	 * @param parameterValues The parameter values or <code>null</code>, if no
	 *		parameters are required.
	 * @throws ScriptException If sth. goes wrong while preparing. Especially
	 *		{@link UndefinedParameterValueException}s and {@link UndeclaredParameterException}s
	 *		might happen.
	 */
	public final void prepare(Script script, Map<String, Object> parameterValues)
		throws ScriptException
	{
		if (script == null)
			throw new IllegalArgumentException("script must not be null!");

		this.script = script;

		if (parameterValues == null)
			parameterValues = new HashMap<String, Object>(0);

		this.parameterValues = parameterValues;

		validateParameters();
		doPrepare();
		prepared = true;
	}

	protected void doPrepare()
		throws ScriptException
	{ }

	public boolean isPrepared()
	{
		return prepared;
	}

	/**
	 * @param script The script which declared the parameters.
	 * @param parameterValues The parameter values that are defined.
	 * @return Returns all those <code>scriptParameterID</code>s that have been declared
	 *		by the script, but not been passed values for.
	 *
	 * @see UndefinedParameterValueException
	 */
	protected static Set<String> getUndefinedParameterIDs(Script script, Map<String, Object> parameterValues)
	{
		ScriptParameterSet parameterSet = script.getParameterSet();
		Set<String> res = new HashSet<String>(parameterSet.getParameterIDs());
		res.removeAll(parameterValues.keySet());
		return res;
	}

	/**
	 * @param script
	 * @param parameterValues
	 * @return
	 */
	protected static Set<String> getUndeclaredParameterIDs(Script script, Map<String, Object> parameterValues)
	{
		ScriptParameterSet parameterSet = script.getParameterSet();
		Set<String> res = new HashSet<String>(parameterValues.keySet());
		res.removeAll(parameterSet.getParameterIDs());
		return res;
	}

	/**
	 * This method checks, whether all parameters required by the script have been defined. And
	 * whether all parameter values correspond to declared parameters (they must match exactly).
	 */
	protected void validateParameters()
			throws ScriptException
	{
		ScriptParameterSet parameterSet = getScript().getParameterSet();

		// get the declared parameters and put them into "undefined" set, because
		// we will remove all that are defined.
		Set<String> undefinedParameterIDs = new HashSet<String>(parameterSet.getParameterIDs());

		// get the defined parameter values and put them into "undeclared" set,
		// because we will remove all that are declared.
		Set<String> undeclaredParameterIDs = getParameterValues().keySet();

		undefinedParameterIDs.removeAll(undeclaredParameterIDs);
		undeclaredParameterIDs.removeAll(parameterSet.getParameterIDs());

		if (!undefinedParameterIDs.isEmpty())
			throw new UndefinedParameterValueException(
					(ScriptRegistryItemID) JDOHelper.getObjectId(script),
					undefinedParameterIDs);

		if (!undeclaredParameterIDs.isEmpty())
			throw new UndeclaredParameterException(
					(ScriptRegistryItemID) JDOHelper.getObjectId(script),
					undeclaredParameterIDs);
	}

	public Script getScript()
	{
		if (!isPrepared())
			throw new IllegalStateException("Cannot obtain script prior to prepare(...)! Call prepare(...) first!");

		return script;
	}

	/**
	 * Accessor for parameter values.
	 * <p>
	 * key: String scriptParameterID (see {@link ScriptParameter#getScriptParameterID()})<br/>
	 * value: Object value (the value assigned to the parameter specified by <code>scriptParameterID</code>
	 * </p>
	 * <p>
	 * You can obtain the declared parameters via <code>{@link #getScript()}.{@link ScriptRegistryItem#getParameterSet() getParameterSet()}</code>
	 * </p>
	 *
	 * @return Returns a map with all defined parameter values. Never returns <code>null</code> (even if
	 *		{@link #prepare(Script, Map) } was called with <code>parameterValues == null</code>).
	 */
	public Map<String, Object> getParameterValues()
	{
		if (!isPrepared())
			throw new IllegalStateException("Cannot obtain parameter values prior to prepare(...)! Call prepare(...) first!");

		return parameterValues;
	}

	/**
	 * This method checks whether it's prepared and calls {@link #doExecute() } then. You
	 * must not override this method - implement {@link #doExecute() } instead!
	 *
	 * @return Returns the result of the evaluation of the script.
	 * @throws ScriptException
	 */
	@SuppressWarnings("unchecked")
	public final Object execute()
		throws ScriptException
	{
		if (!isPrepared())
			throw new IllegalStateException("You called execute() without prior prepare(...)! Call prepare(...) first!");

		Object result = doExecute();

		// check result type against declared type in script
		if (result == null)
			return null;

		Class resultClass = result.getClass();
		try {
			if (!script.getResultClass().isAssignableFrom(resultClass))
				throw new ResultClassMismatchException(
						(ScriptRegistryItemID) JDOHelper.getObjectId(script), script.getResultClass(), resultClass);
		} catch (ClassNotFoundException cnf) {
			throw new ScriptException(cnf);
		}

		return result;
	}

	/**
	 * This method is called by {@link #execute() }. Implement it and return the result of
	 * your script's execution.
	 *
	 * @return Returns the result of the script's evaluation.
	 * @throws ScriptException
	 */
	protected abstract Object doExecute()
		throws ScriptException;
}
