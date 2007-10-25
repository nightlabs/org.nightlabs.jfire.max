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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.ObjectIDUtil;

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
		implements Serializable, IScriptParameterSet //, StoreCallback
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
	 *		dependent-value="true"
	 *
	 * @jdo.key mapped-by="scriptParameterID"
	 */
	private Map<String, ScriptParameter> parameters;

	/**
	 * TODO: Is this needed
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="ScriptParameter"
	 *		mapped-by="scriptParameterSet"
	 *		dependent-element="true"
	 */
	private List<ScriptParameter> orderedParameters;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="scriptParameterSet" dependent="true"
	 */
	private ScriptParameterSetName name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int nextParameterOrderNumber;	

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ScriptParameterSet() { }

	public ScriptParameterSet(String organisationID, long scriptParameterSetID)
	{
		this.organisationID = organisationID;
		this.scriptParameterSetID = scriptParameterSetID;
		this.name = new ScriptParameterSetName(this);
		parameters = new HashMap();
		nextParameterOrderNumber = 0;
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
//		Collection<IScriptParameter> params = CollectionUtil.castCollection(parameters.values()); 
//		return Collections.unmodifiableCollection(params);
	}
	
	public SortedSet<ScriptParameter> getSortedParameters() {
		SortedSet sortedParams = new TreeSet(parameters.values());
		return Collections.unmodifiableSortedSet(sortedParams);
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
		res.setOrderNumber(nextParameterOrderNumber++);
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

	/**
	 * Removes all parameters from this set.
	 */
	public void removeAllParameters() {
		parameters.clear();
		nextParameterOrderNumber = 0;
	}
	
	public static String getPrimaryKey(String organisationID, long scriptParameterSetID)
	{
		return organisationID + '/' + ObjectIDUtil.longObjectIDFieldToString(scriptParameterSetID);
	}

	public ScriptParameterSetName getName() {
		return name;
	}

//	/**
//	 * TODO: Remove this StoreCallback, ScriptParameterSets should be created 
//	 * with ID from IDGenerator
//	 */
//	public void jdoPreStore() {
//		if (!JDOHelper.isNew(this)) 
//			return;
//		
//		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//		if (pm == null)
//			throw new IllegalStateException("Could not get PersistenceManager jdoPreStore()");
//		
//		
//		if (scriptParameterSetID < 0) {
//			// TODO: add check for organisationID
//			ScriptRegistry scriptRegistry = ScriptRegistry.getScriptRegistry(pm);
//			scriptParameterSetID = scriptRegistry.createScriptParameterSetID();
//			for (Iterator it = getParameters().iterator(); it.hasNext(); ) {
//				ScriptParameter parameter = (ScriptParameter) it.next();
//				if (parameter.getScriptParameterSetID() != scriptParameterSetID)
//					parameter.setScriptParameterSetID(scriptParameterSetID);
//			}
//		}
//
//		ScriptParameterSetChangeEvent.addChangeEventToController(
//				pm,
//				ScriptParameterSetChangeEvent.EVENT_TYPE_SET_ADDED,
//				this
//			);		
//	}

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
