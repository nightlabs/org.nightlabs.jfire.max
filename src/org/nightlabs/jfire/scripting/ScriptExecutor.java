/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

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
	 * You must implement this method and return the language that your Executor implementation
	 * supports.
	 * @return Returns a unique string identifying the script language (e.g. "JavaScript", "com.foo.MyScriptLanguage").
	 */
	public abstract String getLanguage();

	/**
	 * You must implement this method and return the file extensions (without the leading dot)
	 * of your files (if files are used instead of the datastore). Note, that the first
	 * entry is considered the default file extension.
	 * @return Returns sth. like "js" or "asp".
	 */
	public abstract String[] getFileExtensions();

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
		prepare(script, parameterValues, true);
	}
	
	/**
	 * This is a shortcut for preparing the executor without knowing
	 * parameterValues yet (use with validateParams = false).
	 * Subclasses may use this in combination with {@link #setParameterValues(Map)}.
	 * 
	 * @param script The script that shall be executed.
	 * @param parameterValues The parameter values or <code>null</code>, if no
	 *		parameters are required.
	 * @param validateParams Whether to validate parameters or not
	 * @throws ScriptException If sth. goes wrong while preparing. Especially
	 *		{@link UndefinedParameterValueException}s and {@link UndeclaredParameterException}s
	 *		might happen.
	 */
	protected void prepare(Script script, Map<String, Object> parameterValues, boolean validateParams) 
		throws ScriptException
	{
		preparing = true;
		try {
			if (script == null)
				throw new IllegalArgumentException("script must not be null!");
	
			this.script = script;
	
			if (parameterValues == null)
				parameterValues = new HashMap<String, Object>(0);
	
			this.parameterValues = parameterValues;
	
			if (validateParams)
				validateParameters();
			doPrepare();
			prepared = true;
		} finally {
			preparing = false;
		}
	}
	
	/**
	 * Sets and validates the parameterValues for the execution of a script.
	 * Use this only if you don't know parameters when calling {@link #prepare(Script, Map)}
	 * and you have to use {@link #prepare(Script, Map, boolean)}.
	 *  
	 * @param parameterValues The parameterValues to set or <code>null</code> if none needed
	 * @throws ScriptException Thrown when validation fails
	 */
	protected void setParameterValues(Map<String, Object> parameterValues) 
		throws ScriptException 
	{
		if (parameterValues == null)
			parameterValues = new HashMap<String, Object>(0);

		this.parameterValues = parameterValues;
		validateParameters();
	}

	/**
	 * Called on {@link #prepare(Script, Map)} after the 
	 * validation of parameters.
	 * Default implementation does nothing. Subclasses my
	 * add functionality.
	 * 
	 * @throws ScriptException Might throw a ScriptException
	 */
	protected void doPrepare()
		throws ScriptException
	{ }

	public boolean isPrepared()
	{
		return prepared;
	}

	private boolean preparing = false;

	protected boolean isPreparing()
	{
		return preparing;
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
		Set<String> undefinedParameterIDs = null;
		if (parameterSet != null)
			undefinedParameterIDs = new HashSet<String>(parameterSet.getParameterIDs());
		else
			undefinedParameterIDs = new HashSet<String>();

		// get the defined parameter values and put them into "undeclared" set,
		// because we will remove all that are declared.
		Set<String> undeclaredParameterIDs = new HashSet<String>(getParameterValues().keySet());

		undefinedParameterIDs.removeAll(undeclaredParameterIDs);
		if (parameterSet != null)
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

	/**
	 * Returns the script set with {@link #prepare(Script, Map)}. 
	 * If prepare was not called prior to this method
	 * an {@link IllegalStateException} will be thrown.
	 * 
	 * @return The script to execute.
	 */
	public Script getScript()
	{
		if (!isPrepared() && !preparing)
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
		if (!isPrepared() && !preparing)
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
