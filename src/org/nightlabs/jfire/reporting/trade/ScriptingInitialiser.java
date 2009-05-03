package org.nightlabs.jfire.reporting.trade;

import java.io.File;
import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.reporting.scripting.ScriptingConstants;
import org.nightlabs.jfire.scripting.ScriptCategory;
import org.nightlabs.jfire.scripting.ScriptingIntialiserException;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.servermanager.JFireServerManager;

/**
 * Initializes the scripts for the trade reporting module.
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
	 * @throws ScriptingIntialiserException
	 * @throws ModuleException
	 */
	public static void initialize(
			PersistenceManager pm,
			JFireServerManager jfireServerManager,
			String organisationID
		) throws ScriptingIntialiserException
	{
		ScriptRegistryItemID rootCatID = ScriptRegistryItemID.create(
				organisationID, ScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_ROOT,
				ScriptingConstants.SCRIPT_REGISTRY_ITEM_ID_CATEGORY_ROOT);

		ScriptCategory rootCategory = null;
		try {
			rootCategory = (ScriptCategory) pm.getObjectById(rootCatID);
		} catch (JDOObjectNotFoundException e) {
			throw new IllegalStateException("Could not find root Reporting ScriptCategory. Was it intialized correctly?", e);
		}

		ScriptCategory tradeCategory = org.nightlabs.jfire.scripting.ScriptingInitialiser.createCategory(
				pm, rootCategory,
				organisationID,
				ScriptingTradeConstants.SCRIPT_REGISTRY_ITEM_TYPE_ROOT,
				ScriptingTradeConstants.SCRIPT_REGISTRY_ITEM_ID_CATEGORY_TRADE);
		tradeCategory.getName().setText(Locale.ENGLISH.getLanguage(), "Trade scripts");
		tradeCategory.getName().setText(Locale.GERMAN.getLanguage(), "Trade scripts");


		String j2eeBaseDir = jfireServerManager.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		File ear = new File(j2eeBaseDir, JFireReportingTradeEAR.MODULE_NAME + ".ear");
		String scriptDirSuffix = "script";
		new org.nightlabs.jfire.scripting.ScriptingInitialiser(
				ear,
				scriptDirSuffix,
				tradeCategory,
				pm,
				organisationID
		).initialise();
	}

}
