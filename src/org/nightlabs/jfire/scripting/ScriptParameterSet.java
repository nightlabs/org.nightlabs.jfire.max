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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.StoreCallback;

/**
 * A ScriptParameterSet can only be manipulated by the owner organisation. Hence, it contains
 * always (in all datastores) the same parameters and is everywhere (except in its "home" datastore)
 * read only.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.scripting.id.ScriptParameterSetID"
 *		detachable="true"
 *		table="JFireScripting_ScriptParameterSet"
 *
 * @jdo.create-objectid-class field-order="organisationID, scriptParameterSetID"
 * 
 * @jdo.fetch-group name="ScriptParameterSet.parameters" fetch-groups="default" fields="parameters"
 * @jdo.fetch-group name="ScriptParameterSet.name" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="ScriptParameterSet.this" fetch-groups="default" fields="parameters, name"
 * 
 * @jdo.query
 *		name="getParameterSetsByOrganisation"
 *		query="SELECT
 *			WHERE this.organisationID == paramOrganisationID
 *			PARAMETERS String paramOrganisationID
 *			import java.lang.String"
 * 
 */
 public class ScriptParameterSet
		implements Serializable, StoreCallback
{
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_PARAMETERS = "ScriptParameterSet.parameters";
	public static final String FETCH_GROUP_NAME = "ScriptParameterSet.name";
	public static final String FETCH_THIS_SCRIPT_PARAMETER_SET = "ScriptParameterSet.this";	
	
	public static final String QUERY_GET_PARAMETER_SETS_BY_ORGANISATION = "getParameterSetsByOrganisation";

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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ScriptParameterSetName name;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ScriptParameterSet() { }

	public ScriptParameterSet(String organisationID, long scriptParameterSetID)
	{
		this.organisationID = organisationID;
		this.scriptParameterSetID = scriptParameterSetID;
		this.name = new ScriptParameterSetName(this);
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
	
	public static String getPrimaryKey(String organisationID, long scriptParameterSetID)
	{
		return organisationID + '/' + scriptParameterSetID;
	}

	public ScriptParameterSetName getName() {
		return name;
	}

	public void jdoPreStore() {
		if (!JDOHelper.isNew(this)) 
			return;
		
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Could not get PersistenceManager jdoPreStore()");
		
		
		if (scriptParameterSetID < 0) {
			// TODO: add check for organisationID
			ScriptRegistry scriptRegistry = ScriptRegistry.getScriptRegistry(pm);
			scriptParameterSetID = scriptRegistry.createScriptParameterSetID();
			for (ScriptParameter parameter : getParameters()) {
				if (parameter.getScriptParameterSetID() != scriptParameterSetID)
					parameter.setScriptParameterSetID(scriptParameterSetID);
			}
		}
		
		// TODO: trigger change event ??
	}

	/**
	 * Get all ParameterSets of an organisation.
	 * 
	 * @param pm The PersistenceManager to use.
	 * @param organisationID The organisation
	 * @return
	 */
	public static Collection getParameterSetsByOrganisation(PersistenceManager pm, String organisationID) {
		Query q = pm.newNamedQuery(ScriptParameterSet.class, QUERY_GET_PARAMETER_SETS_BY_ORGANISATION);
		return (Collection)q.execute(organisationID);
	}
}
