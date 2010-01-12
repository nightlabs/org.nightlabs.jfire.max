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

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.listener.StoreCallback;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.util.Util;

/**
 * Base class for objects in the JFire scripting tree and provides the
 * common properties of these objects like a name, a description and
 * a {@link ScriptParameterSet}.
 * <p>
 * Subclasses are {@link ScriptCategory} that is a container for other categories and
 * {@link Script} the actual script.
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.scripting.id.ScriptRegistryItemID"
 *		detachable="true"
 *		table="JFireScripting_ScriptRegistryItem"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, scriptRegistryItemType, scriptRegistryItemID"
 *
 * @jdo.fetch-group name="ScriptRegistryItem.parent" fetch-groups="default" fields="parent"
 * @jdo.fetch-group name="ScriptRegistryItem.name" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="ScriptRegistryItem.description" fetch-groups="default" fields="description"
 * @jdo.fetch-group name="ScriptRegistryItem.this" fetch-groups="default, ScriptRegistryItem.name, ScriptRegistryItem.description" fields="parent, name, description"
 *
 * @jdo.query
 *		name="getTopLevelScriptRegistryItemsByOrganisationID"
 *		query="SELECT
 *			WHERE this.organisationID == paramOrganisationID &&
 *            this.parent == null
 *			PARAMETERS String paramOrganisationID
 *			import java.lang.String"
 *
 * @jdo.query
 *		name="getTopLevelScriptRegistryItems"
 *		query="SELECT
 *			WHERE this.parent == null
 *			import java.lang.String"
 *
 * @jdo.query
 *		name="getTopLevelScriptRegistryItemsByType"
 *		query="SELECT
 *			WHERE this.parent == null &&
 *						this.scriptRegistryItemType == pScriptRegistryItemType
 *			PARAMETERS String pScriptRegistryItemType
 *			import java.lang.String"
 *
 * @jdo.query
 *		name="getTopLevelScriptRegistryItemsByOrganisationIDAndType"
 *		query="SELECT
 *			WHERE this.parent == null &&
 *						this.organisationID == pOrganisationID &&
 *						this.scriptRegistryItemType == pScriptRegistryItemType
 *			PARAMETERS String pOrganisationID, String pScriptRegistryItemType
 *			import java.lang.String"
 *
 */
@PersistenceCapable(
	objectIdClass=ScriptRegistryItemID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireScripting_ScriptRegistryItem")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="ScriptRegistryItem.parent",
		members=@Persistent(name="parent")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ScriptRegistryItem.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ScriptRegistryItem.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description")),	
	@FetchGroup(
		fetchGroups={"default"},
		name=ScriptRegistryItem.FETCH_GROUP_PARAMETER_SET,
		members=@Persistent(name="parameterSet")),		
	@FetchGroup(
		fetchGroups={"default", ScriptRegistryItem.FETCH_GROUP_NAME, ScriptRegistryItem.FETCH_GROUP_DESCRIPTION},
		name=ScriptRegistryItem.FETCH_GROUP_THIS_SCRIPT_REGISTRY_ITEM,
		members={@Persistent(name="parent"), @Persistent(name="name"), @Persistent(name="description")})
})
@Queries({
	@javax.jdo.annotations.Query(
		name=ScriptRegistryItem.QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS_BY_ORGANISATION_ID,
		value="SELECT WHERE this.organisationID == paramOrganisationID && this.parent == null PARAMETERS String paramOrganisationID import java.lang.String"),
	@javax.jdo.annotations.Query(
		name=ScriptRegistryItem.QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS,
		value="SELECT WHERE this.parent == null import java.lang.String"),
	@javax.jdo.annotations.Query(
		name=ScriptRegistryItem.QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS_BY_TYPE,
		value="SELECT WHERE this.parent == null && this.scriptRegistryItemType == pScriptRegistryItemType PARAMETERS String pScriptRegistryItemType import java.lang.String"),
	@javax.jdo.annotations.Query(
				name=ScriptRegistryItem.QUERY_GET_SCRIPT_REGISTRY_IDPARENT_ITEMS,
				value="SELECT JDOHelper.getObjectId(this) WHERE this.parent == :paramParent"),		
	@javax.jdo.annotations.Query(
		name=ScriptRegistryItem.QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS_BY_ORGANISATION_ID_AND_TYPE,
		value="SELECT WHERE this.parent == null && this.organisationID == pOrganisationID && this.scriptRegistryItemType == pScriptRegistryItemType PARAMETERS String pOrganisationID, String pScriptRegistryItemType import java.lang.String")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class ScriptRegistryItem
		implements Serializable, StoreCallback
{
	private static final long serialVersionUID = 9221181132208442543L;

	private static final String QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS_BY_ORGANISATION_ID_AND_TYPE = "getTopLevelScriptRegistryItemsByOrganisationIDAndType";
	private static final String QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS_BY_TYPE = "getTopLevelScriptRegistryItemsByType";
	private static final String QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS_BY_ORGANISATION_ID = "getTopLevelScriptRegistryItemsByOrganisationID";
	private static final String QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS = "getTopLevelScriptRegistryItems";
	private static final String QUERY_GET_SCRIPT_REGISTRY_IDPARENT_ITEMS = "getScriptRegistryItemIDsForParent";
	
	public static final String FETCH_GROUP_PARENT = "ScriptRegistryItem.parentItem";
	public static final String FETCH_GROUP_NAME = "ScriptRegistryItem.name";
	public static final String FETCH_GROUP_PARAMETER_SET = "ScriptRegistryItem.parameterSet";
	public static final String FETCH_GROUP_DESCRIPTION = "ScriptRegistryItem.description";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_SCRIPT_REGISTRY_ITEM = "ScriptRegistryItem.this";


	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String scriptRegistryItemType;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String scriptRegistryItemID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ScriptCategory parent;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ScriptParameterSet parameterSet;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="scriptRegistryItem"
	 */
	@Persistent(
		mappedBy="scriptRegistryItem",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private ScriptRegistryItemName name;

	/**
	 * @jdo.field persistence-modifier="persistent"  mapped-by="scriptRegistryItem"
	 */
	@Persistent(
		mappedBy="scriptRegistryItem",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private ScriptRegistryItemDescription description;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ScriptRegistryItem() { }

	/**
	 * Create a new {@link ScriptRegistryItem} with the given primary key parameters.
	 * This is intended to be used as superconstructor of extending classes.
	 *
	 * @param organisationID The organisationID.
	 * @param scriptRegistryItemType The scriptRegistryItemType.
	 * @param scriptRegistryItemID The scriptRegistryItemID.
	 */
	protected ScriptRegistryItem(String organisationID, String scriptRegistryItemType, String scriptRegistryItemID)
	{
		this.organisationID = organisationID;
		this.scriptRegistryItemType = scriptRegistryItemType;
		this.scriptRegistryItemID = scriptRegistryItemID;
		this.name = new ScriptRegistryItemName(this);
		this.description = new ScriptRegistryItemDescription(this);
	}

	/**
	 * Returns the String representation of the primary key of the item
	 * represented by the given parameters. It is the "/" separated
	 * concatenation of the given parameters.
	 *
	 * @param organisationID The organisationID.
	 * @param scriptRegistryItemType The scriptRegistryItemType.
	 * @param scriptRegistryItemID The scriptRegistryItemID.
	 * @return The String representation of the primary key of the item
	 * 		represented by the given parameters.
	 */
	public static String getPrimaryKey(String organisationID, String scriptRegistryItemType, String scriptRegistryItemID)
	{
		return organisationID + '/' + scriptRegistryItemType + '/' + scriptRegistryItemID;
	}

	/**
	 * Returns the organisationID of this item.
	 * This is part of the primary key.
	 *
	 * @return The organisationID of this item.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	/**
	 * Returns the scriptRegistryItemType of this item.
	 * This is part of the primary key.
	 *
	 * @return The scriptRegistryItemType of this item.
	 */
	public String getScriptRegistryItemType()
	{
		return scriptRegistryItemType;
	}

	/**
	 * Returns the scriptRegistryItemID of this item.
	 * This is part of the primary key.
	 *
	 * @return The scriptRegistryItemID of this item.
	 */
	public String getScriptRegistryItemID()
	{
		return scriptRegistryItemID;
	}

	/**
	 * Returns the parent of this item.
	 * Parents always are {@link ScriptCategory}s.
	 *
	 * @return The parent of this item.
	 */
	protected ScriptCategory getParent()
	{
		return parent;
	}

	/**
	 * Sets the parent of this item.
	 *
	 * @param parent The parent of this item.
	 */
	protected void setParent(ScriptCategory parent)
	{
		this.parent = parent;
	}

	/**
	 * Returns the {@link ScriptParameterSet} of this
	 * {@link ScriptRegistryItem}. In case this is a
	 * {@link ScriptCategory} this parameter-set is
	 * applied to {@link Script}s created with this as
	 * parent.
	 *
	 * @return the {@link ScriptParameterSet} of this {@link ScriptRegistryItem}
	 */
	public ScriptParameterSet getParameterSet()
	{
		return parameterSet;
	}

	/**
	 * Returns the name of this {@link ScriptRegistryItem}.
	 *
	 * @return The name of this {@link ScriptRegistryItem}.
	 */
	public ScriptRegistryItemName getName() {
		return name;
	}

	/**
	 * Returns the desciption of this {@link ScriptRegistryItem}.
	 *
	 * @return The desciption of this {@link ScriptRegistryItem}.
	 */
	public ScriptRegistryItemDescription getDescription() {
		return description;
	}

	/**
	 * Set the {@link ScriptParameterSet} of this item.
	 *
	 * @param parameterSet The parameterSet to set for this item.
	 */
	public void setParameterSet(ScriptParameterSet parameterSet)
	{
		this.parameterSet = parameterSet;
	}

	/**
	 * Checks for equality of the primary key fields.
	 * @return Whether the primary key fields match.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof ScriptRegistryItem))
			return false;

		ScriptRegistryItem other = (ScriptRegistryItem) obj;

		return
				Util.equals(this.organisationID,           other.organisationID) &&
				Util.equals(this.scriptRegistryItemType,   other.scriptRegistryItemType) &&
				Util.equals(this.scriptRegistryItemID,     other.scriptRegistryItemID);
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return
				Util.hashCode(organisationID) ^
				Util.hashCode(scriptRegistryItemType) ^
				Util.hashCode(scriptRegistryItemID);
	}
	
	/**
	 * Returns all {@link ScriptRegistryItem}s that have with the given scriptCategory
	 * as parent.
	 *
	 * @param pm The {@link PersistenceManager} to use.
	 * @param scriptCategory The {@link scriptCategory} children should be searched for.
	 * @return all {@link ScriptRegistryItem}s that have with the given scriptCategory
	 * 		as parent.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<ScriptRegistryItemID> getReportRegistryItemIDsForParent(PersistenceManager pm, ScriptCategory reportCategory) {
		Query q = pm.newNamedQuery(ScriptRegistryItem.class, QUERY_GET_SCRIPT_REGISTRY_IDPARENT_ITEMS);
		return (Collection<ScriptRegistryItemID>) q.execute(reportCategory);
	}
	
	/**
	 * Returns all top level (parent == null) ScriptRegistryItems for the given organisationID
	 * If organisation is null the top level registry items
	 * for all organisation are returned.
	 *
	 * @param pm The PersistenceManager to use.
	 * @param organisationID The organisationID to use.
	 */
	public static Collection<ScriptRegistryItem> getTopScriptRegistryItemsByOrganisationID(PersistenceManager pm,
			String organisationID)
	{
		if (organisationID == null || "".equals(organisationID))
			return getTopScriptRegistryItems(pm);
		Query q = pm.newNamedQuery(ScriptRegistryItem.class, QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS_BY_ORGANISATION_ID);
		return (Collection)q.execute(organisationID);
	}

	/**
	 * Returns all top level (parent == null) ScriptRegistryItems for all
	 * organisations
	 *
	 * @param pm The PersistenceManager to use.
	 */
	public static Collection<ScriptRegistryItem> getTopScriptRegistryItems(PersistenceManager pm)
	{
		Query q = pm.newNamedQuery(ScriptRegistryItem.class, QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS);
		return (Collection)q.execute();
	}

	/**
	 * Returns all top level (parent == null) ScriptRegistryItems with the given
	 * scriptRegistryItemType
	 *
	 * @param pm The PersistenceManager to use.
	 * @param scriptRegistryItemType the ScriptRegistryItemType to filter
	 */
	public static Collection<ScriptRegistryItem> getTopScriptRegistryItemsByType(PersistenceManager pm,
			String scriptRegistryItemType)
	{
		Query q = pm.newNamedQuery(ScriptRegistryItem.class, QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS_BY_TYPE);
		return (Collection)q.execute(scriptRegistryItemType);
	}

	/**
	 * Returns all top level (parent == null) ScriptRegistryItems for the given organisationID
	 * and scriptRegistryItemType
	 * If organisation is null the top level registry items
	 * for all organisation are returned.
	 *
	 * @param pm The PersistenceManager to use.
	 * @param organisationID The organisationID to use.
	 * @param scriptRegistryItemType the scriptRegistryItemType to use
	 */
	public static Collection<ScriptRegistryItem> getTopScriptRegistryItemsByOrganisationIDAndType(
			PersistenceManager pm, String organisationID, String scriptRegistryItemType)
	{
		if (organisationID == null || "".equals(organisationID))
			return getTopScriptRegistryItemsByType(pm, scriptRegistryItemType);
		Query q = pm.newNamedQuery(ScriptRegistryItem.class, QUERY_GET_TOPLEVEL_SCRIPT_REGISTRY_ITEMS_BY_ORGANISATION_ID_AND_TYPE);
		return (Collection)q.execute(organisationID, scriptRegistryItemType);
	}

	public void jdoPreStore()
	{
		if (!JDOHelper.isNew(this))
			return;

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Could not get PersistenceManager jdoPreStore()");

		ScriptRegistryItem _parent = getParent();
		if (getParameterSet() == null && parent != null) {
			if (JDOHelper.isDetached(parent)) {
				ScriptRegistryItem pParent = null;
				boolean setFromPersistent = false;
				try {
					pParent = (ScriptRegistryItem) pm.getObjectById(JDOHelper.getObjectId(parent));
					setFromPersistent = true;
				} catch(JDOObjectNotFoundException e) {}
				if (setFromPersistent) {
					setParameterSet(pParent.getParameterSet());
				}
				else {
					try {
						this.setParameterSet(_parent.getParameterSet());
					} catch (JDODetachedFieldAccessException e) {
						// TODO: Log with logger?? when made transient -> Nullpointerexception
						Logger.getLogger(ScriptRegistryItem.class).error("Could not set the parameterSet initially from null to the parents one");
					}
				}
			}
		}
	}

}
