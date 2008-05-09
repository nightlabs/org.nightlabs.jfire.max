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
import java.util.Locale;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
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
	 * There seems to be a heisenbug in JPOX which causes it to fail sometimes with a "mc closed" error. Therefore, we simply perform
	 * the initialisation twice (if the first time succeeded, the second call is a noop anymway).
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public void initialise2()
	throws Exception
	{
		initialise();
	}

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
		initialiser.createDemoTimerTask();

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
		logger.info("createModuleMetaData: begin");

		PersistenceManager pm = this.getPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, "JFireChezFrancois");
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of JFireChezFrancois started...");

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = new ModuleMetaData(
					"JFireChezFrancois", "0.9.3-0-beta", "0.9.3-0-beta");
			pm.makePersistent(moduleMetaData);


		} finally {
			pm.close();
			logger.info("createModuleMetaData: end");
		}
	}

	private String getDisplayName()
	{
		String displayName = "Chez Francois";
		if (ChezFrancoisServerInitialiser.ORGANISATION_ID_RESELLER.equals(getOrganisationID())) {
			displayName = "Reseller";
		}
		return displayName;
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="RequiresNew"
	 */
	public void createDemoTimerTask() throws TimePatternFormatException
	{
		logger.info("createDemoTimerTask: begin");

		PersistenceManager pm = this.getPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getFetchPlan().setMaxFetchDepth(1);

			// registering demo timer task
			TaskID taskID = TaskID.create(
					// Organisation.DEV_ORGANISATION_ID, // the task can be modified by the organisation and thus it's maybe more logical to use the real organisationID - not dev
					getOrganisationID(),
					Task.TASK_TYPE_ID_SYSTEM, "ChezFrancois-DemoTimerTask");
			try {
				Task task = (Task) pm.getObjectById(taskID);
				task.getActiveExecID(); // WORKAROUND for jpox heisenbug
			} catch (JDOObjectNotFoundException x) {
				Task task = new Task(
						taskID.organisationID, taskID.taskTypeID, taskID.taskID,
						User.getUser(pm, getOrganisationID(), User.USERID_SYSTEM),
						ChezFrancoisDatastoreInitialiserHome.JNDI_NAME,
						"demoTimerTask");

				task.getName().setText(Locale.ENGLISH.getLanguage(), getDisplayName() + " Demo Timer Task");
				task.getDescription().setText(Locale.ENGLISH.getLanguage(), "This task demonstrates how to use the JFire Timer.");

				task.getTimePatternSet().createTimePattern(
						"*", // year
						"*", // month
						"*", // day
						"mon-fri", // dayOfWeek
						"*", //  hour
				"*/2"); // minute

				task.getTimePatternSet().createTimePattern(
						"*", // year
						"*", // month
						"*", // day
						"sat,sun", // dayOfWeek
						"*", //  hour
				"1-59/2"); // minute

				task.setEnabled(true);
				pm.makePersistent(task);
			}
		} finally {
			pm.close();
			logger.info("createDemoTimerTask: end");
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
			new InitialiserVoucher(pm, getPrincipal()).createDemoData();
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
			new InitialiserDynamicTrade(pm, getPrincipal()).createDemoData();
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
			new InitialiserSimpleTrade(pm, getPrincipal()).createDemoData();
		} finally {
			pm.close();
		}
	}

	/**
	 * This is a demo task doing nothing.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public void demoTimerTask(TaskID taskID)
	{
		int sleepSec = 30;

		PersistenceManager pm = getPersistenceManager();
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("***************************************************************************************************************");
				logger.debug("***************************************************************************************************************");
				logger.debug("***************************************************************************************************************");
				logger.debug("***************************************************************************************************************");
				logger.debug("***************************************************************************************************************");
				logger.debug("***************************************************************************************************************");
				logger.debug("demoTimerTask: entered for taskID " + taskID);
				logger.debug("demoTimerTask: sleeping " + sleepSec + " sec");
			}

			try {
				Thread.sleep(1000L * sleepSec);
			} catch (InterruptedException ignore) { }

			if (logger.isDebugEnabled())
				logger.debug("demoTimerTask: about to exit for taskID " + taskID);

		} finally {
			pm.close();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("demoTimerTask: pm closed - exiting for taskID " + taskID);
			logger.debug("***************************************************************************************************************");
			logger.debug("***************************************************************************************************************");
			logger.debug("***************************************************************************************************************");
			logger.debug("***************************************************************************************************************");
			logger.debug("***************************************************************************************************************");
			logger.debug("***************************************************************************************************************");
		}
	}

}
