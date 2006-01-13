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
