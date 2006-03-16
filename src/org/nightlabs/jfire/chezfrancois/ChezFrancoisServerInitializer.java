package org.nightlabs.jfire.chezfrancois;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.serverinit.ServerInitializer;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.OrganisationNotFoundException;

public class ChezFrancoisServerInitializer extends ServerInitializer
{
	public static final String ORGANISATION_ID_WINE_STORE = "jfire.chezfrancois.co.th";

	@Override
	public void initialize()
	throws Exception
	{
//		TransactionManager transactionManager = getJ2EEVendorAdapter().getTransactionManager(getInitialContext());

//		boolean doCommit = false;
//		transactionManager.begin();
//    try {
    	JFireServerManager jfsm = getJFireServerManagerFactory().getJFireServerManager();
    	try {
    		if (jfsm.isNewServerNeedingSetup()) {
    			Logger logger = Logger.getLogger(ChezFrancoisServerInitializer.class);
    			logger.error("Server initialization is not possible, because the basic server configuration is not complete yet! Configure and reboot the server!");
    			return;
    		}

    		try {
    			jfsm.getOrganisationConfig(ORGANISATION_ID_WINE_STORE);
    		} catch (OrganisationNotFoundException x) {
    			// do initialization!
    			jfsm.createOrganisation(ORGANISATION_ID_WINE_STORE, "Chez François Wine Store", "francois", "test", true);
    		}
    	} finally {
    		jfsm.close();
    	}

//			doCommit = true;
//    } finally {
//    	if (doCommit)
//    		transactionManager.commit();
//    	else
//    		transactionManager.rollback();
//    }
	}

}
