/**
 * 
 */
package org.nightlabs.jfire.trade.dashboard;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;

@Stateless
public class DashboardManagerBean extends BaseSessionBeanImpl implements DashboardManagerRemote {

	private static final String MODULE_ID = "JFireTradeDashboardEAR";

	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(DashboardManagerBean.class);
	
	public DashboardManagerBean() {
	}
	
	@Override
	public void initialise() throws Exception {
		PersistenceManager pm = createPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, MODULE_ID);
			if (moduleMetaData == null) {
				moduleMetaData = ModuleMetaData.initModuleMetadata(pm, MODULE_ID, DashboardManagerBean.class);
				intialiseNewOrganisation(pm);
			}
		} finally {
			pm.close();
		}
	}
	
	private void intialiseNewOrganisation(PersistenceManager pm) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating DashboardLayoutConfigModuleInitialiser");
		}
		DashboardLayoutConfigModuleInitialiser dashboardLayoutConfigModuleInitialiser = new DashboardLayoutConfigModuleInitialiser();
		pm.makePersistent(dashboardLayoutConfigModuleInitialiser);
	}

	@Override
	public List<Object> retrieveLastCustomers(int amount) {
		// TODO Auto-generated method stub
		return new ArrayList<Object>();
	}

}
