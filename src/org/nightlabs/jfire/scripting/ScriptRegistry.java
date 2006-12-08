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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.scripting.id.ScriptRegistryID;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.util.Utils;

/**
 * <p>
 * This class is the entry point for the management of scripts. Scripts
 * </p>
 * <p>
 * This is a JDO singleton - i.e. one instance per datastore managed by JDO.
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class = "org.nightlabs.jfire.scripting.id.ScriptRegistryID"
 *		detachable="true"
 *		table="JFireScripting_ScriptRegistry"
 *
 * @jdo.create-objectid-class
 * 
 * @jdo.inheritance strategy="new-table"
 */
public class ScriptRegistry
{

	/**
	 * @jdo.field primary-key="true"
	 */
	private int scriptRegistryID;

	public static final ScriptRegistryID SINGLETON_ID = ScriptRegistryID.create(0); 

	public static ScriptRegistry getScriptRegistry(PersistenceManager pm)
	{
		Iterator it = pm.getExtent(ScriptRegistry.class).iterator();
		if (it.hasNext())
			return (ScriptRegistry) it.next();

		ScriptRegistry reg = new ScriptRegistry(SINGLETON_ID.scriptRegistryID);
		pm.makePersistent(reg);

		try {
			reg.registerScriptExecutorClass(
					ScriptExecutorJavaScript.class);

			reg.registerScriptExecutorClass(
					ScriptExecutorJavaClass.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		reg.organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();

		return reg;
	}

	/**
	 * key: String language (see {@link Script#getLanguage()})<br/>
	 * value: String scriptExecutorClassName (fully qualified name of a class extending {@link ScriptExecutor})
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireScripting_ScriptRegistry_language2ScriptExecutorClassName"
	 *
	 * @jdo.join
	 */
	private Map language2ScriptExecutorClassName;

	/**
	 * key: String fileExtension<br/>
	 * value: String language
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireScripting_ScriptRegistry_fileExtension2Language"
	 *
	 * @jdo.join
	 */
	private Map fileExtension2Language;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long nextScriptParameterSetID = 0;

	/**
	 * @deprecated for JDO only 
	 */
	protected ScriptRegistry() { }

	/**
	 * Don't call this constructor directly. Use {@link #getScriptExecutorRegistry(PersistenceManager) } instead!
	 */
	protected ScriptRegistry(int scriptRegistyID) {
		this.scriptRegistryID = scriptRegistyID;
	}
	
	public int getScriptRegistryID() {
		return scriptRegistryID;
	}

	/**
	 * Binds a class to a language. A previous binding (if existing) to the same language
	 * is overriden by a call to this method. There can only be one
	 * class bound to a language.
	 * @param clazz A class which has a default constructor and extends {@link ScriptExecutor}.
	 *
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 *
	 * @see #unbindLanguage(String)
	 */
	public void registerScriptExecutorClass(Class clazz)
	throws InstantiationException, IllegalAccessException
	{
		if (!ScriptExecutor.class.isAssignableFrom(clazz))
			throw new ClassCastException("Class " + clazz.getName() + " does not extend " + ScriptExecutor.class.getName());

		ScriptExecutor se = (ScriptExecutor) clazz.newInstance();
		String lang = se.getLanguage();

		unbindLanguage(lang);

		language2ScriptExecutorClassName.put(lang, clazz.getName());
		String[] fes = se.getFileExtensions();
		for (int i = 0; i < fes.length; i++) {
			String fileExtension = fes[i];
			fileExtension2Language.put(fileExtension, lang);
		}
	}

	/**
	 * If there is a class bound to the given <code>language</code>, this binding will be removed.
	 * If there is no binding, this method will silently return without any action.
	 *
	 * @param language The language that shall be unbound.
	 *
	 * @see #registerScriptExecutorClass(Class)
	 */
	public void unbindLanguage(String language)
	{
		if (language2ScriptExecutorClassName.remove(language) == null)
			return;

		Set fileExtensions = new HashSet();
		for (Iterator it = fileExtension2Language.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry entry = (Map.Entry)it.next();
			if (entry.getValue().equals(language))
				fileExtensions.add(entry.getKey());
		}
		for (Iterator it = fileExtensions.iterator(); it.hasNext();) {
			Object fileExtension = it.next();
			fileExtension2Language.remove(fileExtension);
		}
	}

	/**
	 * @param language The language for which to lookup the class name and load the class.
	 * @param throwExceptionIfNotFound If <code>true</code> and there's no binding for the <code>language</code>,
	 *		an {@link IllegalArgumentException} will be thrown. If <code>false</code>, it returns silently <code>null</code>
	 *		in this case.
	 * @return Returns either a class which extends {@link ScriptExecutor} or <code>null</code>
	 *		(if <code>throwExceptionIfNotFound == false</code>).
	 * @throws ClassNotFoundException
	 */
	public Class getScriptExecutorClass(String language, boolean throwExceptionIfNotFound)
		throws ClassNotFoundException, IllegalArgumentException
	{
		String className = (String) language2ScriptExecutorClassName.get(language);
		if (className == null) {
			if (throwExceptionIfNotFound)
				throw new IllegalArgumentException("The language \"" + language + "\" is unknown: No ScriptExecutor class bound!");

			return null;
		}

		return Class.forName(className);
	}

	/**
	 * @param language The script's language. There must have been a ScriptExecutor bound before
	 *		(if the language is not internally supported).
	 * @return Returns a new instance of ScriptExecutor.
	 * @throws IllegalArgumentException If no binding exists for the language.
	 * @throws ClassNotFoundException If the class name bound to the language doesn't resolve to a class.
	 * @throws IllegalAccessException If the class cannot be instantiated because of a security problem (see {@link Class#newInstance()} for details).
	 * @throws InstantiationException If the class cannot be instantiated for whatever reason (see {@link Class#newInstance()} for details).
	 */
	public ScriptExecutor createScriptExecutor(String language)
		throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		ScriptExecutor scriptExecutor = (ScriptExecutor) getScriptExecutorClass(language, true).newInstance();
		scriptExecutor.setPersistenceManager(getPersistenceManager());
		return scriptExecutor;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of ScriptRegistry is currently not attached to a datastore. Cannot obtain PersistenceManager!");

		return pm;
	}

	private static String rootOrganisationID = null;

	/**
	 * Searches a script for the given type and id. If multiple scripts are found with the
	 * same id, it uses first the one with the local organisation id, second the one of
	 * the root organisation and third the devil organisation description. If none of them
	 * matches, because two (or more) other organisations have conflicting ids, an exception is thrown,
	 * because the result would otherwise be arbitrary and might change from method call to method call.
	 *
	 * @param scriptRegistryItemType
	 * @param scriptRegistryItemID
	 * @return Returns either <code>null</code> or the instance of Script that fits the rules best.
	 */
	public Script getScript(String scriptRegistryItemType, String scriptRegistryItemID)
	{
		PersistenceManager pm = getPersistenceManager();

		Collection scripts = Script.getScripts(pm, scriptRegistryItemType, scriptRegistryItemID);

		if (scripts.isEmpty())
			return null;

		if (scripts.size() == 1)
			return (Script) scripts.iterator().next();

		Map scriptsByOrganisationID = new HashMap();
		for (Iterator it = scripts.iterator(); it.hasNext(); ) {
			Script script = (Script) it.next();
			scriptsByOrganisationID.put(script.getOrganisationID(), script);
		}

		Script script = (Script) scriptsByOrganisationID.get(organisationID);
		if (script != null)
			return script;


		if (rootOrganisationID == null) {
			InitialContext initialContext;
			try {
				initialContext = new InitialContext();
			} catch (NamingException e) {
				throw new RuntimeException(e);
			}
			try {
				rootOrganisationID = Organisation.getRootOrganisationID(initialContext);
			} finally {
				try {
					initialContext.close();
				} catch (NamingException e) {
					throw new RuntimeException(e);
				}
			}
		}

		script = (Script) scriptsByOrganisationID.get(rootOrganisationID);
		if (script != null)
			return script;


		script = (Script) scriptsByOrganisationID.get(Organisation.DEVIL_ORGANISATION_ID);
		if (script != null)
			return script;

		throw new IllegalStateException("Have multiple scripts for scriptRegistryItemType=\"" + scriptRegistryItemType + "\" and scriptRegistryItemID=\"" + scriptRegistryItemID + "\" and they do neither come from the root nor from the devil organisation!");
	}
	
	public Collection getRegisteredLanguages() {
		return language2ScriptExecutorClassName.keySet();
	}
	
	public Collection getRegisteredFileExtensions() {
		return fileExtension2Language.keySet();
	}

	public String getLanguageByFileName(String fileName, boolean throwExceptionIfNotFound)
	{
		String res = getLanguageByFileExtension(Utils.getFileExtension(fileName), false);
		if (throwExceptionIfNotFound && res == null)
			throw new IllegalArgumentException("There is no language registered for the fileExtension of the file: " + fileName);

		return res;
	}

	public String getLanguageByFileExtension(String fileExtension, boolean throwExceptionIfNotFound)
	{
		String res = (String) fileExtension2Language.get(fileExtension);
		if (throwExceptionIfNotFound && res == null)
			throw new IllegalArgumentException("There is no language registered for the given fileExtension: " + fileExtension);

		return res;
	}

	public Map<ScriptRegistryItemID, Object> execute(List<Script> scripts, Map<String, Object> parameterValues)
	throws
		IllegalArgumentException,
		ClassNotFoundException,
		InstantiationException,
		IllegalAccessException,
		ScriptException
	{
		List<Map<String, Object>> parameterValuesList = new ArrayList<Map<String,Object>>(scripts.size());
		for (int i = 0; i < scripts.size(); ++i) {
			parameterValuesList.add(parameterValues);
		}
		return execute(scripts, parameterValuesList);
	}

	public Map<ScriptRegistryItemID, Object> execute(List<Script> scripts, List<Map<String, Object>> parameterValues)
	throws
		IllegalArgumentException,
		ClassNotFoundException,
		InstantiationException,
		IllegalAccessException,
		ScriptException
	{
		if (scripts.size() != parameterValues.size())
			throw new IllegalArgumentException("scripts.size() != parameterValues.size()");

		Map<ScriptRegistryItemID, Object> res = new HashMap<ScriptRegistryItemID, Object>();
		Iterator<Map<String,Object>> parameterValuesIterator = parameterValues.iterator();
		for (Script script : scripts) {
			ScriptExecutor scriptExecutor = createScriptExecutor(script.getLanguage());
			scriptExecutor.prepare(script,
					parameterValuesIterator.next());
			res.put(
					(ScriptRegistryItemID) JDOHelper.getObjectId(script),
					scriptExecutor.execute());
		}
		return res;
	}
	
	public List<Object> executeScripts(List<IScript> scripts, List<Map<String, Object>> parameterValues)
	throws
		IllegalArgumentException,
		ClassNotFoundException,
		InstantiationException,
		IllegalAccessException,
		ScriptException
	{
		if (scripts.size() != parameterValues.size())
			throw new IllegalArgumentException("scripts.size() != parameterValues.size()");

		List<Object> res = new ArrayList<Object>(scripts.size());
		Iterator<Map<String,Object>> parameterValuesIterator = parameterValues.iterator();
		for (IScript script : scripts) {
			ScriptExecutor scriptExecutor = createScriptExecutor(script.getLanguage());
			scriptExecutor.prepare(script, parameterValuesIterator.next());
			res.add(scriptExecutor.execute());
		}
		return res;		
	}
	
	public Map<ScriptRegistryItemID, Script> getScripts(Collection<ScriptRegistryItemID> scriptItemsIDs) 
	{
		if (scriptItemsIDs == null)
			return null;
		
		Map<ScriptRegistryItemID, Script> scriptID2Script = new HashMap<ScriptRegistryItemID, Script>(scriptItemsIDs.size());
		for (ScriptRegistryItemID itemID : scriptItemsIDs) {
			Script script = getScript(itemID.scriptRegistryItemType, itemID.scriptRegistryItemID);
			scriptID2Script.put(itemID, script);
		}
		return scriptID2Script;
	}
}
