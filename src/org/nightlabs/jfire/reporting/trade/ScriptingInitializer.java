package org.nightlabs.jfire.reporting.trade;

import java.io.File;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.scripting.ScriptCategory;
import org.nightlabs.jfire.servermanager.JFireServerManager;

/**
 * Initializes the scripts for the trade reporting module.
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ScriptingInitializer {
	
	/**
	 * Uses a {@link org.nightlabs.jfire.scripting.ScriptingInitializer} to add
	 * all scripts in the script subdirectory of the JFireReportingTrade.ear.
	 * It will create a root category for all scripts
	 * 
	 * @param pm The PersistenceManager to use.
	 * @param jfireServerManager The ServerManager to use.
	 * @param organisationID The organisationID the stored scirpts will have. 
	 * @throws ModuleException
	 */
	public static void initialize(
			PersistenceManager pm, 
			JFireServerManager jfireServerManager,
			String organisationID			
		) 
	throws ModuleException 
	{
		ScriptCategory rootCategory = org.nightlabs.jfire.scripting.ScriptingInitializer.createCategory(
				pm, null, 
				organisationID, 
				ScriptingTradeConstants.SCRIPT_REGISTRY_ITEM_TYPE_ROOT, 
				ScriptingTradeConstants.SCRIPT_REGISTRY_ITEM_ID_CATEGORY_ROOT);
		rootCategory.getName().setText(Locale.ENGLISH.getLanguage(), "JFire Trade Scripting");
		rootCategory.getName().setText(Locale.GERMAN.getLanguage(), "JFire Trade Scripting");
		
		
		String j2eeBaseDir = jfireServerManager.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		String scriptDirSuffix = JFireReportingTradeEAR.MODULE_NAME+".ear"+File.separator+"script";
		File scriptDir = new File(j2eeBaseDir, scriptDirSuffix);
		File[] subDirs = scriptDir.listFiles();
		for (int i = 0; i < subDirs.length; i++) {
			if (subDirs[i].isDirectory()) {
				new org.nightlabs.jfire.scripting.ScriptingInitializer(
						scriptDirSuffix+File.separator+subDirs[i].getName(),
						rootCategory,
						ScriptingTradeConstants.SCRIPT_REGISTRY_ITEM_TYPE_TRADE_SCRIPT,
						jfireServerManager,
						pm, 
						organisationID
					).initialize();
			}
		}
	}

}
