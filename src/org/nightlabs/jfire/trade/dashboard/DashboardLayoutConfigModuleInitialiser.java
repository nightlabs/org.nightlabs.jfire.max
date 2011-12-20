package org.nightlabs.jfire.trade.dashboard;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.ConfigModuleInitialiser;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;
import org.nightlabs.jfire.dashboard.DashboardLayoutConfigModule;
import org.nightlabs.jfire.dashboard.resource.Messages;
import org.nightlabs.jfire.idgenerator.IDGenerator;

@PersistenceCapable(
	identityType = IdentityType.APPLICATION,
	detachable = "true")
@Inheritance(strategy = InheritanceStrategy.SUPERCLASS_TABLE)
public class DashboardLayoutConfigModuleInitialiser extends
	ConfigModuleInitialiser {

	public DashboardLayoutConfigModuleInitialiser() {
		super(IDGenerator.getOrganisationID(),
			DashboardLayoutConfigModule.class.getName(),
			"DashboardGadgetLastCustomersFactory", -1);
	}
	
	@Override
	public void initialiseConfigModule(PersistenceManager pm,
		ConfigModule configModule) {
		createLastCustomersEntry((DashboardLayoutConfigModule) configModule);

	}

	@Override
	public void initialiseDetachedConfigModule(ConfigModule configModule) {
		createLastCustomersEntry((DashboardLayoutConfigModule) configModule);

	}

	private void createLastCustomersEntry(DashboardLayoutConfigModule cfMod) {
		DashboardGadgetLayoutEntry lastCustomersEntry = cfMod.createEditLayoutEntry("DashboardGadgetLastCustomersFactory");
		
		cfMod.addEditLayoutEntry(lastCustomersEntry);

		
		lastCustomersEntry.setConfig(null); 	// TODO where to create/initialise the config object of type DashboardGadgetLastCustomersConfig ?
		
		initializeLastCustomersGadgetName(lastCustomersEntry.getEntryName());
	}
	
	
	public static void initializeLastCustomersGadgetName(I18nText gadgetName) {
		gadgetName.readFromProperties(
			Messages.BUNDLE_NAME, 
			DashboardLayoutConfigModuleInitialiser.class.getClassLoader(), 
			DashboardLayoutConfigModuleInitialiser.class.getName() + ".lastCustomersGadget.title");
	}
}
