package org.nightlabs.jfire.trade.dashboard;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.clientui.layout.GridData;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.ConfigModuleInitialiser;
import org.nightlabs.jfire.dashboard.DashboardGadgetLayoutEntry;
import org.nightlabs.jfire.dashboard.DashboardLayoutConfigModule;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.trade.dashboard.resource.Messages;

@PersistenceCapable(
	identityType = IdentityType.APPLICATION,
	detachable = "true")
@Inheritance(strategy = InheritanceStrategy.SUPERCLASS_TABLE)
public class TradeDashboardGadgetsConfigModuleInitialiser extends
	ConfigModuleInitialiser {

	public TradeDashboardGadgetsConfigModuleInitialiser(String organisationID) {
		super(organisationID,
			DashboardLayoutConfigModule.class.getName(),
			"TradeDashboardGadgets", 5000);
	}
	
	@Override
	public void initialiseConfigModule(PersistenceManager pm, ConfigModule configModule) {
		createLastCustomersGadgetConfig((DashboardLayoutConfigModule<?>) configModule);
		createInvoiceGadgetConfig((DashboardLayoutConfigModule<?>) configModule);
	}

	@Override
	public void initialiseDetachedConfigModule(ConfigModule configModule) {
		createLastCustomersGadgetConfig((DashboardLayoutConfigModule<?>) configModule);
		createInvoiceGadgetConfig((DashboardLayoutConfigModule<?>) configModule);
	}

	@SuppressWarnings("unchecked")
	private void createLastCustomersGadgetConfig(DashboardLayoutConfigModule cfMod) {
		DashboardGadgetLayoutEntry lastCustomersEntry = cfMod.createEditLayoutEntry("DashboardGadgetLastCustomersFactory");
		lastCustomersEntry.getGridData().setGrabExcessHorizontalSpace(true);
		lastCustomersEntry.getGridData().setHorizontalAlignment(GridData.FILL);
		cfMod.addEditLayoutEntry(lastCustomersEntry);

		DashboardGadgetLastCustomersConfig config = new DashboardGadgetLastCustomersConfig();
		config.setAmountLastCustomers(DashboardGadgetLastCustomersConfig.initialAmountOfCustomersInDashboard);
		lastCustomersEntry.setConfig(config);
		
		initializeLastCustomersGadgetName(lastCustomersEntry.getEntryName());
	}
	
	@SuppressWarnings("unchecked")
	private void createInvoiceGadgetConfig(DashboardLayoutConfigModule cfMod) {
		DashboardGadgetLayoutEntry invoiceEntry = cfMod.createEditLayoutEntry("DashboardGadgetInvoiceFactory");
		invoiceEntry.getGridData().setGrabExcessHorizontalSpace(true);
		invoiceEntry.getGridData().setHorizontalAlignment(GridData.FILL);
		cfMod.addEditLayoutEntry(invoiceEntry);

		DashboardGadgetInvoiceConfig config = new DashboardGadgetInvoiceConfig();
		config.setAmountOfInvoices(DashboardGadgetInvoiceConfig.initialAmountOfInvoicesInDashboard);
		config.setInvoiceQueryItemId(null);
		invoiceEntry.setConfig(config);
		
		initializeInvoiceGadgetName(invoiceEntry.getEntryName());
	}

	public static void initializeLastCustomersGadgetName(I18nText gadgetName) {
		initializeGadgetName(gadgetName, "lastCustomersGadget.title");
	}
	
	public static void initializeInvoiceGadgetName(I18nText gadgetName) {
		initializeGadgetName(gadgetName, "invoiceGadget.title");
	}
	
	public static void initializeGadgetName(I18nText gadgetName, String nameKeySuffix) {
		gadgetName.readFromProperties(
			Messages.BUNDLE_NAME, 
			TradeDashboardGadgetsConfigModuleInitialiser.class.getClassLoader(), 
			TradeDashboardGadgetsConfigModuleInitialiser.class.getName() + "." + nameKeySuffix);
	}
}
