package org.nightlabs.jfire.reporting.scripting;

import java.io.File;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.reporting.JFireReportingEAR;
import org.nightlabs.jfire.scripting.ScriptCategory;
import org.nightlabs.jfire.scripting.ScriptingIntialiserException;
import org.nightlabs.jfire.servermanager.JFireServerManager;

/**
 * Initialises the scripts for the trade reporting module.
 *
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ScriptingInitialiser {

	/**
	 * Uses a {@link org.nightlabs.jfire.scripting.ScriptingInitialiser} to add
	 * all scripts in the script subdirectory of the JFireReportingTrade.ear.
	 * It will create a root category for all scripts
	 *
	 * @param pm The PersistenceManager to use.
	 * @param jfireServerManager The ServerManager to use.
	 * @param organisationID The organisationID the stored scirpts will have.
	 * @throws ModuleException
	 */
	public static void initialise(
			PersistenceManager pm,
			JFireServerManager jfireServerManager,
			String organisationID
		)
	throws ScriptingIntialiserException
	{
		ScriptCategory rootCategory = org.nightlabs.jfire.scripting.ScriptingInitialiser.createCategory(
				pm, null,
				organisationID,
				ScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_ROOT,
				ScriptingConstants.SCRIPT_REGISTRY_ITEM_ID_CATEGORY_ROOT);
		rootCategory.getName().setText(Locale.ENGLISH.getLanguage(), "Reporting");
		rootCategory.getName().setText(Locale.GERMAN.getLanguage(), "Reporting");

		ScriptCategory baseCategory = org.nightlabs.jfire.scripting.ScriptingInitialiser.createCategory(
				pm, rootCategory,
				organisationID,
				ScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_ROOT,
				ScriptingConstants.SCRIPT_REGISTRY_ITEM_ID_CATEGORY_BASE);
		baseCategory.getName().setText(Locale.ENGLISH.getLanguage(), "Base scripts");
		baseCategory.getName().setText(Locale.GERMAN.getLanguage(), "Base scripts");

		String j2eeBaseDir = jfireServerManager.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		File ear = new File(j2eeBaseDir, JFireReportingEAR.MODULE_NAME + ".ear");
		String scriptDirSuffix = "script";
		new org.nightlabs.jfire.scripting.ScriptingInitialiser(
				ear,
				scriptDirSuffix,
				baseCategory,
				pm,
				organisationID
		).initialise();

	}

}
