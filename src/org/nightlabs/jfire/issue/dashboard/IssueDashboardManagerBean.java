/**
 * 
 */
package org.nightlabs.jfire.issue.dashboard;

import javax.ejb.Stateless;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;

/**
 * @author Daniel Mazurek
 *
 */
@Stateless
public class IssueDashboardManagerBean extends BaseSessionBeanImpl implements IssueDashboardManagerRemote 
{
	private static final String MODULE_ID = "JFireIssueTrackingDashboardEAR";

	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(IssueDashboardManagerBean.class);
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.issue.dashboard.IssueDashboardManagerRemote#initialise()
	 */
	@Override
	public void initialise() throws Exception {
		PersistenceManager pm = createPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, MODULE_ID);
			if (moduleMetaData == null) {
				moduleMetaData = ModuleMetaData.initModuleMetadata(pm, MODULE_ID, IssueDashboardManagerBean.class);

				if (logger.isDebugEnabled()) {
					logger.debug("Creating IssueDashboardGadgetsConfigModuleInitialiser");
				}
				IssueDashboardGadgetsConfigModuleInitialiser dashboardLayoutConfigModuleInitialiser = new IssueDashboardGadgetsConfigModuleInitialiser();
				pm.makePersistent(dashboardLayoutConfigModuleInitialiser);
			}
		} finally {
			pm.close();
		}
	}

}
