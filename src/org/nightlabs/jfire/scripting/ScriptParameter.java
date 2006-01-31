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

public class ScriptParameter
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
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String scriptParameterID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private ScriptParameterSet scriptParameterSet;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private String scriptParameterClassName;

	/**
	 * @deprecated Only for JDO!
	 */
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
	public String getScriptParameterID()
	{
		return scriptParameterID;
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
}
