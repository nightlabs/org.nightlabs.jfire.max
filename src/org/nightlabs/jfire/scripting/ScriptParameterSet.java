package org.nightlabs.jfire.scripting;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A ScriptParameterSet can only be manipulated by the owner organisation. Hence, it contains
 * always (in all datastores) the same parameters and is everywhere (except in its "home" datastore)
 * read only.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ScriptParameterSet
		implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long scriptParameterSetID;

	/**
	 * key: String scriptParameterID<br/>
	 * value: ScriptParameter param
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ScriptParameter"
	 *		mapped-by="scriptParameterSet"
	 *
	 * @jdo.key mapped-by="scriptParameterID"
	 */
	private Map<String, ScriptParameter> parameters;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ScriptParameterSet() { }

	public ScriptParameterSet(String organisationID, long scriptParameterSetID)
	{
		this.organisationID = organisationID;
		this.scriptParameterSetID = scriptParameterSetID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public long getScriptParameterSetID()
	{
		return scriptParameterSetID;
	}

	public Set<String> getParameterIDs()
	{
		return Collections.unmodifiableSet(parameters.keySet());
	}

	public Collection<ScriptParameter> getParameters()
	{
		return Collections.unmodifiableCollection(parameters.values());
	}

	/**
	 * Creates a new parameter within this set. If there exists already one with
	 * the same scriptParameterID, the previously existing will be returned and
	 * this method does nothing.
	 *
	 * @param scriptParameterID This is the name that is usually used within the scripts
	 *		to reference the parameter.
	 * @return Returns the newly created parameter (or a previously existing one).
	 */
	public ScriptParameter createParameter(String scriptParameterID)
	{
		ScriptParameter res = parameters.get(scriptParameterID);
		if (res != null)
			return res;

		res = new ScriptParameter(this, scriptParameterID);
		parameters.put(scriptParameterID, res);
		return res;
	}

	/**
	 * @param scriptParameterID The simple (local) id as returned by {@link ScriptParameter#getScriptParameterID()}.
	 * @param throwExceptionIfNotFound If <code>false</code>, this method returns <code>null</code> if no
	 *		parameter can be found for the given <code>scriptParameterID</code>. In the same situation, an
	 *		{@link IllegalArgumentException} is thrown, if this parameter is <code>true</code>.
	 * @return Returns the parameter with the given id or <code>null</code> (if allowed, i.e. <code>throwExceptionIfNotFound == false</code>).
	 */
	public ScriptParameter getParameter(String scriptParameterID, boolean throwExceptionIfNotFound)
	{
		ScriptParameter res = parameters.get(scriptParameterID);
		if (res == null && throwExceptionIfNotFound)
			throw new IllegalArgumentException("No parameter registered with scriptParameterID=\"" + scriptParameterID + "\"!");

		return res;
	}

	public void removeParameter(String scriptParameterID)
	{
		parameters.remove(scriptParameterID);
	}
}
