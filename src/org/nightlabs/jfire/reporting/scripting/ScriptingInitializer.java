package org.nightlabs.jfire.reporting.scripting;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.reporting.JFireReportingEAR;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.ScriptCategory;
import org.nightlabs.jfire.scripting.ScriptParameterSet;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.scripting.ScriptRegistryItem;
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
//		pm.deletePersistentAll((Collection) pm.newQuery(Script.class).execute());
//		pm.deletePersistentAll((Collection) pm.newQuery(ScriptCategory.class).execute());

		ScriptRegistry scriptRegistry = ScriptRegistry.getScriptRegistry(pm);
//		ScriptCategory baseCategoryGeneral;
//		ScriptCategory baseCategoryInvoiceData;
//		ScriptCategory baseCategoryOrderData;
		ScriptCategory rootCategory;
		ScriptParameterSet paramSet;

		// Ipanema Ticketing Root Category
		rootCategory = org.nightlabs.jfire.scripting.ScriptingInitializer.createCategory(
				pm, null, 
				organisationID, 
				ScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_CATEGORY_ROOT, 
				ScriptingConstants.SCRIPT_REGISTRY_ITEM_ID_CATEGORY_ROOT);
		rootCategory.getName().setText(Locale.ENGLISH.getLanguage(), "JFire Reporting");
		rootCategory.getName().setText(Locale.GERMAN.getLanguage(), "JFire Reporting");
		
//		baseCategoryGeneral = org.nightlabs.jfire.scripting.ScriptingInitializer.createCategory(
//				pm, rootCategory, organisationID,
//				ScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_CATEGORY_GENERAL,
//				ScriptingConstants.SCRIPT_REGISTRY_ITEM_ID_CATEGORY_GENERAL);
//		baseCategoryGeneral.getName().setText(Locale.ENGLISH.getLanguage(), "General");		
//		baseCategoryGeneral.getName().setText(Locale.GERMAN.getLanguage(), "Allgemein");		
//
//		
//		baseCategoryInvoiceData = org.nightlabs.jfire.scripting.ScriptingInitializer.createCategory(
//				pm, rootCategory, organisationID,
//				ScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_CATEGORY_INVOICEDATA,
//				ScriptingConstants.SCRIPT_REGISTRY_ITEM_ID_CATEGORY_INVOICEDATA);
//		baseCategoryInvoiceData.getName().setText(Locale.ENGLISH.getLanguage(), "Invoice-Data");		
//		baseCategoryInvoiceData.getName().setText(Locale.GERMAN.getLanguage(), "Rechnungs-Daten");
//		
//		baseCategoryOrderData = org.nightlabs.jfire.scripting.ScriptingInitializer.createCategory(
//				pm, rootCategory, organisationID,
//				ScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_CATEGORY_INVOICEDATA,
//				ScriptingConstants.SCRIPT_REGISTRY_ITEM_ID_CATEGORY_INVOICEDATA);
//		baseCategoryOrderData.getName().setText(Locale.ENGLISH.getLanguage(), "Order-Data");		
//		baseCategoryOrderData.getName().setText(Locale.GERMAN.getLanguage(), "Angebots-Daten");
//		
		// Parameter Sets
//		if (baseCategory.getParameterSet() == null) {
//			paramSet = new ScriptParameterSet(organisationID, scriptRegistry.createScriptParameterSetID());
//			paramSet.createParameter(ScriptingConstants.PARAMETER_ID_PERSISTENCE_MANAGER).setScriptParameterClass(PersistenceManager.class);
//			paramSet.createParameter(ScriptingConstants.PARAMETER_ID_TICKET_ID).setScriptParameterClass(ProductID.class);
//			baseCategory.setParameterSet(paramSet);
//		}
		
		
		String j2eeBaseDir = jfireServerManager.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory();
		File scriptDir = new File(j2eeBaseDir, "JFireReporting.ear/script");
		File[] subDirs = scriptDir.listFiles();
		for (int i = 0; i < subDirs.length; i++) {
			if (subDirs[i].isDirectory()) {
				new org.nightlabs.jfire.scripting.ScriptingInitializer(
						"JFireReporting.ear/script/"+subDirs[i].getName(),
						rootCategory,
						ScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_REPORTING_SCRIPT,
						jfireServerManager,
						pm, 
						Organisation.DEVIL_ORGANISATION_ID
					).initialize();
			}
		}
		
//		new org.nightlabs.jfire.scripting.ScriptingInitializer(
//				"JFireReporting.ear/script/InvoiceData",
//				baseCategoryInvoiceData,
//				ScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_CATEGORY_INVOICEDATA,
//				jfireServerManager,
//				pm, 
//				Organisation.DEVIL_ORGANISATION_ID
//			).initialize();
//
//		new org.nightlabs.jfire.scripting.ScriptingInitializer(
//				"JFireReporting.ear/script/OrderData",
//				baseCategoryOrderData,
//				ScriptingConstants.SCRIPT_REGISTRY_ITEM_TYPE_CATEGORY_ORDERDATA,
//				jfireServerManager,
//				pm, 
//				Organisation.DEVIL_ORGANISATION_ID
//			).initialize();
	}

}
