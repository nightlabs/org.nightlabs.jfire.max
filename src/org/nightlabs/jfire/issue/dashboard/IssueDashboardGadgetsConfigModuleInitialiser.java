package org.nightlabs.jfire.issue.dashboard;

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
import org.nightlabs.jfire.issue.dashboard.resource.Messages;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
@PersistenceCapable(
		identityType = IdentityType.APPLICATION,
		detachable = "true")
@Inheritance(strategy = InheritanceStrategy.SUPERCLASS_TABLE)
public class IssueDashboardGadgetsConfigModuleInitialiser 
extends ConfigModuleInitialiser 
{
	public IssueDashboardGadgetsConfigModuleInitialiser() {
		super(IDGenerator.getOrganisationID(),
				DashboardLayoutConfigModule.class.getName(),
				"IssueDashboardGadgets", 5000); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigModuleInitialiser#initialiseConfigModule(javax.jdo.PersistenceManager, org.nightlabs.jfire.config.ConfigModule)
	 */
	@Override
	public void initialiseConfigModule(PersistenceManager pm, ConfigModule configModule) {
		createIssuesGadgetConfig((DashboardLayoutConfigModule<?>) configModule);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.config.ConfigModuleInitialiser#initialiseDetachedConfigModule(org.nightlabs.jfire.config.ConfigModule)
	 */
	@Override
	public void initialiseDetachedConfigModule(ConfigModule configModule) {
		createIssuesGadgetConfig((DashboardLayoutConfigModule<?>) configModule);
	}

	@SuppressWarnings("unchecked")
	private void createIssuesGadgetConfig(DashboardLayoutConfigModule cfMod) {
		DashboardGadgetLayoutEntry entry = cfMod.createEditLayoutEntry("DashboardGadgetIssuesFactory"); //$NON-NLS-1$
		entry.getGridData().setGrabExcessHorizontalSpace(true);
		entry.getGridData().setHorizontalAlignment(GridData.FILL);
		cfMod.addEditLayoutEntry(entry);
		
		IssueDashboardGadgetConfig config = new IssueDashboardGadgetConfig();
		config.setAmountOfIssues(10);
		config.setIssueQueryItemId(null);
		entry.setConfig(config);
		
		initializeGadgetName(entry.getEntryName(), "issueGadget.title");
	}
	
	private static void initializeGadgetName(I18nText gadgetName, String nameKey) {
		gadgetName.readFromProperties(
			Messages.BUNDLE_NAME, 
			IssueDashboardGadgetsConfigModuleInitialiser.class.getClassLoader(), 
			IssueDashboardGadgetsConfigModuleInitialiser.class.getName() + "." +nameKey);
	}
}
