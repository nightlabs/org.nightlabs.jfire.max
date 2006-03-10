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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.util.Utils;

public class ScriptingInitializer 
{
	protected Logger LOGGER = Logger.getLogger(ScriptingInitializer.class);

	private String scriptSubDir;
	private ScriptCategory baseCategory;
	private JFireServerManager jfsm;
	private PersistenceManager pm;
	private String organisationID;
	private String scriptRegistryItemType;

	/**
	 * 
	 * @param parent optional may be null
	 * @param organisationID the organisation ID
	 * @param scriptRegistryItemType 
	 * @param scriptRegistryItemID
	 */
	public static final ScriptCategory createCategory(PersistenceManager pm, ScriptCategory parent, String organisationID, 
			String scriptRegistryItemType, String scriptRegistryItemID)  
	{
		ScriptCategory category;
		try {
			category = (ScriptCategory) pm.getObjectById(ScriptRegistryItemID.create(organisationID, scriptRegistryItemType, scriptRegistryItemID));			
		} catch (JDOObjectNotFoundException e) {
			category = new ScriptCategory(parent, organisationID, scriptRegistryItemType, scriptRegistryItemID);
			if (parent == null)
				pm.makePersistent(category);
			else
				parent.addChild(category);
		}
		return category;
	}
	
	/**
	 * @param scriptSubDir This is the relative directory under the deploy base directory (e.g. "IpanemaTicketing.ear/script/Ticket")
	 * @param baseCategory All directories/files within the scriptSubDir will be created as sub-categories/scripts of this category.
	 * @param jfsm
	 * @param pm
	 * @param scriptRegistryItemType is the type (identifier) for the scripts in categories, categories get the scriptRegistryItemType from their parent 
	 * @param organisationID If you're writing a JFire Community Project, this is {@link Organisation#DEVIL_ORGANISATION_ID}.
	 */
	public ScriptingInitializer(
			String scriptSubDir, ScriptCategory baseCategory, String scriptRegistryItemType,
			JFireServerManager jfsm, PersistenceManager pm, String organisationID)
	{
		this.scriptSubDir = scriptSubDir;
		this.baseCategory = baseCategory;
		this.jfsm = jfsm;
		this.pm = pm;
		this.organisationID = organisationID;
		this.scriptRegistryItemType = scriptRegistryItemType;		
	}

	private ScriptRegistry scriptRegistry = null;
	protected ScriptRegistry getScriptRegistry()
	{
		if (scriptRegistry == null)
			scriptRegistry = ScriptRegistry.getScriptRegistry(pm);

		return scriptRegistry;
	}

	protected Collection<String> getFileExtensions()  
	{
		ScriptRegistry scriptRegistry = ScriptRegistry.getScriptRegistry(pm);
		return scriptRegistry.getRegisteredFileExtensions();
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type = "Required"
	 * @ejb.permission role-name="_Guest_"
	 */	
	public void initialize() 
	throws ModuleException 
	{
		String j2eeBaseDir = jfsm.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		File scriptDir = new File(j2eeBaseDir, scriptSubDir);

		if (!scriptDir.exists())
			throw new IllegalStateException("Script directory does not exist: " + scriptDir.getAbsolutePath());

		LOGGER.info("BEGIN initialization of Scripts");	
//		initDefaultParameterSets();
		createScriptCategories(scriptDir, baseCategory);
	}

	// TODO: implement parsing of content.xml
	private void createScriptCategories(File dir, ScriptCategory parent) 
	throws ModuleException
	{  
		String categoryID = dir.getName();
		ScriptCategory category;
		try {
			pm.getExtent(ScriptCategory.class);
			category = (ScriptCategory) pm.getObjectById(
					ScriptRegistryItemID.create(
							organisationID,
							parent.getScriptRegistryItemType(),
							categoryID));			
		} catch(JDOObjectNotFoundException e) {
			category = new ScriptCategory(parent, organisationID, categoryID);
			parent.addChild(category);
			category.getName().setText(Locale.ENGLISH.getLanguage(), categoryID);			
		}
		LOGGER.info("create Script Category = "+category.getName());
		File[] scripts = dir.listFiles(scriptFileNameFilter);
		for (int j=0; j<scripts.length; j++) {
			File scriptFile = scripts[j];
			
			String scriptID = Utils.getFileNameWithoutExtension(scriptFile.getName());			
			try {			
				LOGGER.info("create Script = " + scriptID);				
				String scriptContent = Utils.readTextFile(scriptFile);
				LOGGER.debug("scriptContent = " + scriptContent);
				Script script;
				try {
					pm.getExtent(Script.class);
					script = (Script) pm.getObjectById(ScriptRegistryItemID.create(
							organisationID,
							scriptRegistryItemType,
							scriptID)
					);
				} catch (JDOObjectNotFoundException e) {
					script = new Script(category, organisationID, scriptRegistryItemType, scriptID);
					category.addChild(script);
					script.getName().setText(Locale.ENGLISH.getLanguage(), scriptID);														
				}
				script.setText(scriptContent);
				script.setLanguage(getScriptRegistry().getLanguageByFileName(scriptFile.getName(), true));
			} catch (Exception e) {
				LOGGER.warn("could NOT create script "+scriptID+"!", e);
			}
		}

		File[] subDirs = dir.listFiles(dirFileFilter);		
		for (int i=0; i<subDirs.length; i++) {
			createScriptCategories(subDirs[i], category);
		}
	}

//	protected String getScriptContent(File f) 
//	throws FileNotFoundException, IOException 
//	{
//		FileInputStream fin = new FileInputStream(f);
////		InputStreamReader reader = new InputStreamReader(fin, "utf-8");
//		DataInputStream din = new DataInputStream(fin);
//		StringBuffer sb = new StringBuffer();
//		while(din.available() != 0) {
//			sb.append(din.readUTF());
//		}
//		din.close();
//		fin.close();
//		return null;
//	}

	protected FileFilter dirFileFilter = new FileFilter() {	
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}	
	};

	private FilenameFilter scriptFileNameFilter = new FilenameFilter()
	{	
		public boolean accept(File dir, String name) 
		{				
			String fileExtension = Utils.getFileExtension(name);
			for (Iterator<String> it = getFileExtensions().iterator(); it.hasNext(); ) {
				String registeredFileExt = it.next();
				if (registeredFileExt.equals(fileExtension))
					return true;
			}
			return false;
		}	
	};

//	// init Default ParameterSets
//	private void initDefaultParameterSets() 
//	{
//		ScriptRegistry scriptRegistry = ScriptRegistry.getScriptRegistry(pm);
//		ScriptParameterSet paramSet = new ScriptParameterSet(organisationID, scriptRegistry.createScriptParameterSetID());
//		paramSet.createParameter(ScriptingConstants.PARAMETER_ID_PERSISTENCE_MANAGER).setScriptParameterClass(PersistenceManager.class);
//		paramSet.createParameter(ScriptingConstants.PARAMETER_ID_TICKET_ID).setScriptParameterClass(ProductID.class);
//	}
}
