/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.chezfrancois;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.serverinit.ServerInitializer;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.OrganisationNotFoundException;

public class ChezFrancoisServerInitializer extends ServerInitializer
{
	public static final String ORGANISATION_ID_WINE_STORE = "chezfrancois.jfire.org";

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
//    			jfsm.createOrganisation(ORGANISATION_ID_WINE_STORE, "Chez François Wine Store", "francois", "test", true);
    			jfsm.createOrganisation(ORGANISATION_ID_WINE_STORE, "Chez Francois Wine Store", "francois", "test", true);
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
