package org.nightlabs.jfire.reporting.trade;

import java.io.File;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.scripting.ScriptCategory;
import org.nightlabs.jfire.servermanager.JFireServerManager;

/**
 * 
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ScriptingInitializer {
	
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
				ScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_ROOT, 
				ScriptingConstants.SCRIPT_REGISTRY_ITEM_ID_CATEGORY_ROOT);
		rootCategory.getName().setText(Locale.ENGLISH.getLanguage(), "JFire Reporting");
		rootCategory.getName().setText(Locale.GERMAN.getLanguage(), "JFire Reporting");
		
		
		String j2eeBaseDir = jfireServerManager.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		String scriptDirSuffix = JFireReportingTradeEAR.MODULE_NAME+".ear"+File.separator+"script";
		File scriptDir = new File(j2eeBaseDir, scriptDirSuffix);
		File[] subDirs = scriptDir.listFiles();
		for (int i = 0; i < subDirs.length; i++) {
			if (subDirs[i].isDirectory()) {
				new org.nightlabs.jfire.scripting.ScriptingInitializer(
						scriptDirSuffix+File.separator+subDirs[i].getName(),
						rootCategory,
						ScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_TRADE_SCRIPT,
						jfireServerManager,
						pm, 
						Organisation.DEVIL_ORGANISATION_ID
					).initialize();
			}
		}
	}

}
