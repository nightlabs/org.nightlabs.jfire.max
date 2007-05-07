/**
 * 
 */
package org.nightlabs.jfire.reporting.trade;

import java.io.File;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.reporting.ReportingInitializerException;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.servermanager.JFireServerManager;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ReportingInitializer {

	public static void initialize(
			PersistenceManager pm, 
			JFireServerManager jfireServerManager,
			String organisationID
		) throws ReportingInitializerException 
	{
		ReportCategory rootCategory = org.nightlabs.jfire.reporting.ReportingInitializer.createCategory(
				pm, null, 
				organisationID, 
				ReportingTradeConstants.REPORT_REGISTRY_ITEM_TYPE_ROOT, 
				ReportingTradeConstants.REPORT_REGISTRY_ITEM_ID_CATEGORY_ROOT, true
			);
		rootCategory.getName().setText(Locale.ENGLISH.getLanguage(), "JFire Trade Scripting");
		rootCategory.getName().setText(Locale.GERMAN.getLanguage(), "JFire Trade Scripting");
		
		
		String j2eeBaseDir = jfireServerManager.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		String reportDirSuffix = JFireReportingTradeEAR.MODULE_NAME+".ear"+File.separator+"report";
		File reportDir = new File(j2eeBaseDir, reportDirSuffix);
		File[] subDirs = reportDir.listFiles();
		for (int i = 0; i < subDirs.length; i++) {
			if (subDirs[i].isDirectory()) {
				new org.nightlabs.jfire.reporting.ReportingInitializer(
						reportDirSuffix+File.separator+subDirs[i].getName(),
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
