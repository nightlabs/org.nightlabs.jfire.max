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

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.listener.SecurityChangeController;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.timepattern.TimePatternFormatException;
import org.nightlabs.version.MalformedVersionException;


/**
 * @ejb.bean name="jfire/ejb/JFireChezFrancois/ChezFrancoisDatastoreInitialiser"
 *					 jndi-name="jfire/ejb/JFireChezFrancois/ChezFrancoisDatastoreInitialiser"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class ChezFrancoisDatastoreInitialiserBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ChezFrancoisDatastoreInitialiserBean.class);

	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	@Override
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}

	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate() throws CreateException
	{
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

	/**
	 * This method is called by the datastore initialisation mechanism.
	 * It populates the datastore with the demo data.
	 * @throws MalformedVersionException
	 * @throws TimePatternFormatException
	 *
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public void initialise()
	throws Exception
	{
		if (hasRootOrganisation() && getOrganisationID().equals(getRootOrganisationID()))
			return;

		ChezFrancoisDatastoreInitialiserLocal initialiser = ChezFrancoisDatastoreInitialiserUtil.getLocalHome().create();
		initialiser.createModuleMetaData();

		initialiser.createDemoData_JFireSimpleTrade();
		initialiser.createDemoData_JFireVoucher();
		initialiser.createDemoData_JFireDynamicTrade();
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void createModuleMetaData()
	throws MalformedVersionException
	{
		logger.trace("createModuleMetaData: begin");

		PersistenceManager pm = this.getPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, "JFireChezFrancois");
			if (moduleMetaData != null)
				return;

			logger.debug("Initialization of JFireChezFrancois started...");

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = new ModuleMetaData(
					"JFireChezFrancois", "0.9.5-0-beta", "0.9.5-0-beta");
			pm.makePersistent(moduleMetaData);

			Workstation ws = new Workstation(getOrganisationID(), "ws00");
			ws.setDescription("Workstation 00");
			ws = pm.makePersistent(ws);

		} finally {
			pm.close();
			logger.trace("createModuleMetaData: end");
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="RequiresNew"
	 */
	public void createDemoData_JFireVoucher()
	throws Exception
	{
		try {
			Class.forName("org.nightlabs.jfire.voucher.store.VoucherType");
		} catch (ClassNotFoundException x) {
			logger.warn("initialise: JFireVoucher is not deployed. Cannot create demo data for this module.");
			return;
		}

		PersistenceManager pm = this.getPersistenceManager();
		try {
			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {
				new InitialiserVoucher(pm, getPrincipal()).createDemoData();
				successful = true;
			} finally {
				SecurityChangeController.endChanging(successful);
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="RequiresNew"
	 */
	public void createDemoData_JFireDynamicTrade()
	throws Exception
	{
		try {
			Class.forName("org.nightlabs.jfire.dynamictrade.store.DynamicProductType");
		} catch (ClassNotFoundException x) {
			logger.warn("initialise: JFireDynamicTrade is not deployed. Cannot create demo data for this module.");
			return;
		}

		PersistenceManager pm = this.getPersistenceManager();
		try {
			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {
				new InitialiserDynamicTrade(pm, getPrincipal()).createDemoData();
				successful = true;
			} finally {
				SecurityChangeController.endChanging(successful);
			}
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="RequiresNew"
	 */
	public void createDemoData_JFireSimpleTrade()
	throws Exception
	{
		try {
			Class.forName("org.nightlabs.jfire.simpletrade.store.SimpleProductType");
		} catch (ClassNotFoundException x) {
			logger.warn("initialise: JFireSimpleTrade is not deployed. Cannot create demo data for this module.");
			return;
		}

		PersistenceManager pm = this.getPersistenceManager();
		try {
			boolean successful = false;
			SecurityChangeController.beginChanging();
			try {
				new InitialiserSimpleTrade(pm, getPrincipal()).createDemoData();
				successful = true;
			} finally {
				SecurityChangeController.endChanging(successful);
			}
		} finally {
			pm.close();
		}
	}

}
