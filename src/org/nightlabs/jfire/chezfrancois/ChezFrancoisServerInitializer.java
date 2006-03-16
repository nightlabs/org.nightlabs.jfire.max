package org.nightlabs.jfire.chezfrancois;

import org.nightlabs.jfire.serverinit.ServerInitializer;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.OrganisationNotFoundException;

public class ChezFrancoisServerInitializer extends ServerInitializer
{
	private static final String ORGANISATION_ID = "jfire.chezfrancois.co.th";

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
    		try {
    			jfsm.getOrganisationConfig(ORGANISATION_ID);
    		} catch (OrganisationNotFoundException x) {
    			// do initialization!
    			jfsm.createOrganisation(ORGANISATION_ID, "Chez François Wine Store", "francois", "test", true);

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
