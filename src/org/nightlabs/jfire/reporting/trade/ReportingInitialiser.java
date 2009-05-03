/**
 *
 */
package org.nightlabs.jfire.reporting.trade;

import java.io.File;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.reporting.ReportingInitialiserException;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.servermanager.JFireServerManager;

/**
 * @author Alexander Bieber <alex [AT] nightlabs [DOT] de>
 *
 */
public class ReportingInitialiser {

	public static void initialise(
			PersistenceManager pm,
			JFireServerManager jfireServerManager,
			String organisationID
		) throws ReportingInitialiserException
	{
		ReportCategory rootCategory = org.nightlabs.jfire.reporting.ReportingInitialiser.createCategory(
				pm, null,
				organisationID,
				ReportingTradeConstants.REPORT_REGISTRY_ITEM_TYPE_ROOT,
				ReportingTradeConstants.REPORT_REGISTRY_ITEM_ID_CATEGORY_ROOT, true
			);
		rootCategory.getName().setText(Locale.ENGLISH.getLanguage(), "JFire Trade Reports");
		rootCategory.getName().setText(Locale.GERMAN.getLanguage(), "JFire Trade Berichte");


		String j2eeBaseDir = jfireServerManager.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		File ear = new File(j2eeBaseDir, JFireReportingTradeEAR.MODULE_NAME+".ear");
		String reportDirSuffix = "report";
		new org.nightlabs.jfire.reporting.ReportingInitialiser(
				ear,
				reportDirSuffix,
				rootCategory,
				pm,
				organisationID
		).initialise();
	}

}
