/**
 * 
 */
package org.nightlabs.jfire.reporting.trade.config;

import java.util.Collection;
import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.config.ConfigModule;
import org.nightlabs.jfire.config.ConfigModuleInitialiser;
import org.nightlabs.jfire.config.id.ConfigModuleInitialiserID;
import org.nightlabs.jfire.reporting.config.ReportLayoutAvailEntry;
import org.nightlabs.jfire.reporting.config.ReportLayoutConfigModule;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.trade.ReportingTradeConstants;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.config.ConfigModuleInitialiser"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class ReportLayoutCfModInitialiserArticleContainerLayouts extends ConfigModuleInitialiser {
	
	/**
	 * Log4J Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(ReportLayoutCfModInitialiserArticleContainerLayouts.class);

	private static final String INITIALISER_ID_DEFAULT_ARTICLE_CONTAINER_LAYOUTS = "ReportLayoutInitialiser-DefaultArticleContainerLayouts";
	
	/**
	 */
	public ReportLayoutCfModInitialiserArticleContainerLayouts(String organisationID) {
		super(organisationID, ReportLayoutConfigModule.class.getName(), INITIALISER_ID_DEFAULT_ARTICLE_CONTAINER_LAYOUTS);
	}

	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.config.ConfigModuleInitialiser#initialiseConfigModule(org.nightlabs.jfire.config.ConfigModule)
	 */
	@Override
	public void initialiseConfigModule(PersistenceManager pm, ConfigModule configModule) {
		if (!(configModule instanceof ReportLayoutConfigModule))
			return;
		ReportLayoutConfigModule cfMod = (ReportLayoutConfigModule) configModule;
		setUserReportLayoutAvailEntry(pm, cfMod, ReportingTradeConstants.REPORT_REGISTRY_ITEM_TYPE_INVOICE);
		setUserReportLayoutAvailEntry(pm, cfMod, ReportingTradeConstants.REPORT_REGISTRY_ITEM_TYPE_OFFER);
		setUserReportLayoutAvailEntry(pm, cfMod, ReportingTradeConstants.REPORT_REGISTRY_ITEM_TYPE_ORDER);
		setUserReportLayoutAvailEntry(pm, cfMod, ReportingTradeConstants.REPORT_REGISTRY_ITEM_TYPE_DELIVERY_NOTE);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation does nothing!
	 * </p>
	 * @see org.nightlabs.jfire.config.ConfigModuleInitialiser#initialiseDetachedConfigModule(org.nightlabs.jfire.config.ConfigModule)
	 */
	@Override
	public void initialiseDetachedConfigModule(ConfigModule configModule) {
		// Does nothing!!
	}
	
	private static void setUserReportLayoutAvailEntry(PersistenceManager pm, ReportLayoutConfigModule cfMod, String reportRegistryItemType) {
		logger.debug("Setting ReportLayoutAvailEntry for type "+reportRegistryItemType);
		ReportLayoutAvailEntry entry = cfMod.getAvailEntry(reportRegistryItemType);
		Collection items = ReportRegistryItem.getReportRegistryItemByType(pm, cfMod.getOrganisationID(), reportRegistryItemType);
		logger.debug("Search for ReportLayouts produced "+items.size()+" items");
		for (Iterator iter = items.iterator(); iter.hasNext();) {
			ReportRegistryItem item = (ReportRegistryItem) iter.next();
			if (item instanceof ReportLayout) {
				entry.getAvailableReportLayoutKeys().add(JDOHelper.getObjectId(item).toString());
				// set the default, the last one will then be the real default
				entry.setDefaultReportLayoutKey(JDOHelper.getObjectId(item).toString());
				logger.debug("Added "+JDOHelper.getObjectId(item).toString()+" to availability set.");
			}
		}
	}
	

	public static final ConfigModuleInitialiserID getConfigModuleInitialiserID(String organisationID) {
		return ConfigModuleInitialiserID.create(
				organisationID,
				ReportLayoutConfigModule.class.getName(),
				INITIALISER_ID_DEFAULT_ARTICLE_CONTAINER_LAYOUTS
		);
	}
}
 