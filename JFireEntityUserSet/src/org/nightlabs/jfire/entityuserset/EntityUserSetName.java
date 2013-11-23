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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.entityuserset;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import org.nightlabs.jfire.entityuserset.id.EntityUserSetNameID;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.entityuserset.id.EntityUserSetNameID"
 *		detachable="true"
 *		table="JFireEntityUserSet_EntityUserSetName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, entityClassName, entityUserSetID"
 *
 * @jdo.fetch-group name="EntityUserSet.name" fields="entityUserSet, names"
 *
 * @jdo.fetch-group name="FetchGroupsEntityUserSet.replicateToReseller" fields="entityUserSet, names"
 */
@PersistenceCapable(
	objectIdClass=EntityUserSetNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireEntityUserSet_EntityUserSetName")
@FetchGroups({
	@FetchGroup(
		name="EntityUserSet.name",
		members={@Persistent(name="entityUserSet"), @Persistent(name="names")}),
	@FetchGroup(
		name="FetchGroupsEntityUserSet.replicateToReseller",
		members={@Persistent(name="entityUserSet"), @Persistent(name="names")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class EntityUserSetName extends I18nText
{
	private static final long serialVersionUID = 1L;
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
	private String entityClassName;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String entityUserSetID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private EntityUserSet<?> entityUserSet;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EntityUserSetName() {
	}

	public EntityUserSetName(EntityUserSet<?> entityUserSet) {
		this.entityUserSet = entityUserSet;
		this.organisationID = entityUserSet.getOrganisationID();
		this.entityClassName = entityUserSet.getEntityClassName();
		this.entityUserSetID = entityUserSet.getEntityUserSetID();
		this.names = new HashMap<String, String>();
	}

	/**
	 * key: String languageID<br/>
	 * value: String name
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireEntityUserSet_EntityUserSetName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireEntityUserSet_EntityUserSetName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, String> names;

	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}

	@Override
	protected String getFallBackValue(String languageID) {
		return organisationID + '/' + entityUserSetID;
	}

	public String getOrganisationID() {
		return organisationID;
	}
	public String getEntityClassName() {
		return entityClassName;
	}
	public String getEntityUserSetID() {
		return entityUserSetID;
	}

	public EntityUserSet<?> getEntityUserSet() {
		return entityUserSet;
	}
}
