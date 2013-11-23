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

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.scripting.id.ScriptParameterID;
import org.nightlabs.util.Util;

/**
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.scripting.id.ScriptParameterID"
 *		detachable="true"
 *		table="JFireScripting_ScriptParameter"
 *
 * @jdo.create-objectid-class field-order="organisationID, scriptParameterSetID, scriptParameterID"
 *
 * @jdo.fetch-group name="ScriptParameter.scriptParameterSet" fetch-groups="default" fields="scriptParameterSet"
 * @jdo.fetch-group name="ScriptParameter.this" fetch-groups="default" fields="scriptParameterSet"
 *
 */
@PersistenceCapable(
	objectIdClass=ScriptParameterID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireScripting_ScriptParameter")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name="ScriptParameter.scriptParameterSet",
		members=@Persistent(name="scriptParameterSet")),
	@FetchGroup(
		fetchGroups={"default"},
		name="ScriptParameter.this",
		members=@Persistent(name="scriptParameterSet"))
})
public class ScriptParameter
		implements Serializable, Comparable<IScriptParameter>, IScriptParameter
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
	 */
	@PrimaryKey
	private long scriptParameterSetID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String scriptParameterID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private ScriptParameterSet scriptParameterSet;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private String scriptParameterClassName;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int orderNumber;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ScriptParameter() {}

	public ScriptParameter(ScriptParameterSet scriptParameterSet, String scriptParameterID)
	{
		this.scriptParameterSet = scriptParameterSet;
		this.organisationID = scriptParameterSet.getOrganisationID();
		this.scriptParameterSetID = scriptParameterSet.getScriptParameterSetID();
		this.scriptParameterID = scriptParameterID;
		this.scriptParameterClassName = Object.class.getName();
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public long getScriptParameterSetID()
	{
		return scriptParameterSetID;
	}

	void setScriptParameterSetID(long scriptParameterSetID) {
		this.scriptParameterSetID = scriptParameterSetID;
	}

	public String getScriptParameterID()
	{
		return scriptParameterID;
	}

	public void setScriptParameterID(String scriptParameterID) {
		this.scriptParameterID = scriptParameterID;
	}

	public ScriptParameterSet getScriptParameterSet()
	{
		return scriptParameterSet;
	}

	public String getScriptParameterClassName()
	{
		return scriptParameterClassName;
	}
	public void setScriptParameterClassName(String scriptParameterClassName)
	{
		if (scriptParameterClassName == null)
			throw new IllegalArgumentException("scriptParameterClassName must not be null!");

		this.scriptParameterClassName = scriptParameterClassName;
	}
	public Class getScriptParameterClass()
		throws ClassNotFoundException
	{
		return Class.forName(scriptParameterClassName);
	}
	public void setScriptParameterClass(Class scriptParameterClass)
	{
		if (scriptParameterClass == null)
			throw new IllegalArgumentException("scriptParameterClass must not be null!");

		this.scriptParameterClassName = scriptParameterClass.getName();
	}

	protected void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public int getOrderNumber() {
		return orderNumber;
	}


	@Override
	public int hashCode() {
		return
			Util.hashCode(organisationID) ^
			Util.hashCode(scriptParameterSetID) ^
			Util.hashCode(scriptParameterID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (obj.getClass() != this.getClass())
			return false;

		ScriptParameter other = (ScriptParameter) obj;

		return
			Util.equals(this.organisationID, other.organisationID) &&
			this.scriptParameterSetID == other.scriptParameterSetID &&
			Util.equals(this.scriptParameterID, other.scriptParameterID);
	}

	/**
	 * Comparing Script Parameters by their order number
	 */
	public int compareTo(IScriptParameter o) {
		if (!(o instanceof IScriptParameter))
			return 0;

		int otherOrderNumber = (o).getOrderNumber();

		if (this.orderNumber == otherOrderNumber)
			return 0;

		return this.orderNumber < otherOrderNumber ? -1 : 1;
	}

}
