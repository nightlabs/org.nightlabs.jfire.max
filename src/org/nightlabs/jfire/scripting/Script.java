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

import java.util.Collection;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.scripting.ScriptRegistryItem"
 *		detachable="true"
 *		table="JFireScripting_Script"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.query
 *		name="getScriptsByTypeAndID"
 *		query="SELECT
 *			WHERE
 *				this.scriptRegistryItemType == pScriptRegistryItemType &&
 *				this.scriptRegistryItemID == pScriptRegistryItemID
 *			PARAMETERS String pScriptRegistryItemType, String pScriptRegistryItemID
 *			import java.lang.String"
 *
 * @jdo.query
 *		name="getScriptsByTypeAndResultClass"
 *		query="SELECT
 *			WHERE
 *				this.scriptRegistryItemType == pScriptRegistryItemType &&
 *				this.resultClassName == pResultClassName
 *			PARAMETERS String pScriptRegistryItemType, String pResultClassName
 *			import java.lang.String"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireScripting_Script")
@Queries({
	@javax.jdo.annotations.Query(
		name=Script.QUERY_GET_SCRIPTS_BY_TYPE_AND_ID,
		value="SELECT WHERE this.scriptRegistryItemType == pScriptRegistryItemType && this.scriptRegistryItemID == pScriptRegistryItemID PARAMETERS String pScriptRegistryItemType, String pScriptRegistryItemID import java.lang.String"),
	@javax.jdo.annotations.Query(
		name=Script.QUERY_GET_SCRIPTS_BY_TYPE_AND_RESULT_CLASS,
		value="SELECT WHERE this.scriptRegistryItemType == pScriptRegistryItemType && this.resultClassName == pResultClassName PARAMETERS String pScriptRegistryItemType, String pResultClassName import java.lang.String")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Script
		extends ScriptRegistryItem
		implements IScript
{
	private static final long serialVersionUID = 1L;

	private static final String QUERY_GET_SCRIPTS_BY_TYPE_AND_ID = "getScriptsByTypeAndID";
	public static Collection<Script> getScripts(PersistenceManager pm, String scriptRegistryItemType, String scriptRegistryItemID)
	{
		Query q = pm.newNamedQuery(Script.class, QUERY_GET_SCRIPTS_BY_TYPE_AND_ID);
		return (Collection<Script>) q.execute(scriptRegistryItemType, scriptRegistryItemID);
	}

	private static final String QUERY_GET_SCRIPTS_BY_TYPE_AND_RESULT_CLASS = "getScriptsByTypeAndResultClass";
	public static Collection<Script> getScriptsByTypeAndResult(PersistenceManager pm,
			String scriptRegistryItemType, String resultClassName)
	{
		Query q = pm.newNamedQuery(Script.class, QUERY_GET_SCRIPTS_BY_TYPE_AND_RESULT_CLASS);
		return (Collection<Script>) q.execute(scriptRegistryItemType, resultClassName);
	}
	
	/**
	 * This field stores the language of the script. Currently,
	 * the {@link ScriptExecutorJavaScript} supports {@link ScriptExecutorJavaScript#LANGUAGE_JAVA_SCRIPT}}
	 * ({@value ScriptExecutorJavaScript#LANGUAGE_JAVA_SCRIPT}) and
	 * you can write java classes (working with or without the script text)
	 * which should extend {@link ScriptExecutorJavaClass} and support
	 * {@link ScriptExecutorJavaClass#LANGUAGE_JAVA_CLASS}.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String language;

	/**
	 * This is the fully qualified class name specifying the type of the script's result.
	 * The default value is: <code>java.lang.Object</code>
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String resultClassName = Object.class.getName();

	/**
	 * This field stores the actual code of the script which will be executed.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column sql-type="CLOB"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@Column(sqlType="CLOB")
	private String text;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected Script() { }

	public Script(ScriptCategory parent, String organisationID, String scriptRegistryItemType, String scriptRegistryItemID)
	{
		super(organisationID, scriptRegistryItemType, scriptRegistryItemID);
		this.setParent(parent);
		try {
			this.setParameterSet(parent.getParameterSet());
		} catch(JDODetachedFieldAccessException e) {
			// When detached without parameterSet don't bother -> see ScriptRegistryItem.preStore
			System.out.println("DEBUG: Script instantiated with detached parent, parameterSet could not be set as it was not detached.");
		}
		// TODO remove this if jdo doesn't accept null arrays
//		fetchGroups = new String[] {
//			FetchPlan.DEFAULT
//		};
	}

	@Override
	public ScriptCategory getParent()
	{
		return super.getParent();
	}

	/**
	 * Gets the actual code (usually in java script).
	 *
	 * @return Returns the actual script code.
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * Sets the actual code.
	 *
	 * @param text The script code that shall be executed.
	 */
	public void setText(String text)
	{
		this.text = text;
	}

	/**
	 * Accessor to the language of the script. Currently,
	 * the {@link ScriptExecutorJavaScript} supports {@link ScriptExecutorJavaScript#LANGUAGE_JAVA_SCRIPT}}
	 * ({@value ScriptExecutorJavaScript#LANGUAGE_JAVA_SCRIPT}) and
	 * you can write java classes (working with or without the script text)
	 * which should extend {@link ScriptExecutorJavaClass} and support
	 * {@link ScriptExecutorJavaClass#LANGUAGE_JAVA_CLASS}.
	 *
	 * @return Returns the language.
	 */
	public String getLanguage()
	{
		return language;
	}

	/**
	 * Sets the script language.
	 *
	 * @param language The language.
	 */
	public void setLanguage(String language)
	{
		this.language = language;
	}

	public String getResultClassName()
	{
		return resultClassName;
	}

	public void setResultClassName(String resultClassName)
	{
		if (resultClassName == null)
			throw new IllegalArgumentException("resultClassName must not be null!");

		this.resultClassName = resultClassName;
	}

	public void setResultClass(Class<?> resultClass)
	{
		if (resultClass == null)
			throw new IllegalArgumentException("resultClass must not be null!");

		this.resultClassName = resultClass.getName();
	}

	public Class<?> getResultClass()
		throws ClassNotFoundException
	{
		try {
			return Class.forName(resultClassName, true, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException x) {
			// ignore
		}
		return Class.forName(resultClassName);
	}

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="array"
	 *		element-type="String"
	 *		table="JFireScripting_Script_fetchGroups"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireScripting_Script_fetchGroups",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private String[] fetchGroups;

	public String[] getFetchGroups() {
		return fetchGroups;
	}
	public void setFetchGroups(String[] fetchGroups) {
		this.fetchGroups = fetchGroups;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int maxFetchDepth = 1;
	
	public int getMaxFetchDepth() {
		return maxFetchDepth;
	}
	public void setMaxFetchDepth(int maxFetchDepth) {
		this.maxFetchDepth = maxFetchDepth;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean needsDetach = false;
	
	public void setNeedsDetach(boolean needsDetach) {
		this.needsDetach = needsDetach;
	}
	public boolean isNeedsDetach() {
		return needsDetach;
	}
}
