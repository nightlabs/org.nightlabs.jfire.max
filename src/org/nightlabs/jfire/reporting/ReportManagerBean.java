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

package org.nightlabs.jfire.reporting;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.reporting.config.ReportLayoutConfigModule;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportLayoutLocalisationData;
import org.nightlabs.jfire.reporting.layout.ReportRegistry;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.layout.render.RenderManager;
import org.nightlabs.jfire.reporting.layout.render.RenderReportException;
import org.nightlabs.jfire.reporting.layout.render.RenderReportRequest;
import org.nightlabs.jfire.reporting.layout.render.RenderedReportLayout;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererHTML;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererPDF;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererUtil;
import org.nightlabs.jfire.reporting.oda.JFireReportingOdaException;
import org.nightlabs.jfire.reporting.oda.jdojs.JDOJSResultSet;
import org.nightlabs.jfire.reporting.oda.jdojs.JDOJSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jdoql.JDOQLMetaDataParser;
import org.nightlabs.jfire.reporting.oda.jdoql.JDOQLResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSQueryPropertySet;
import org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReporting;
import org.nightlabs.jfire.reporting.oda.server.jdojs.ServerJDOJSProxy;
import org.nightlabs.jfire.reporting.oda.server.jdoql.ServerJDOQLProxy;
import org.nightlabs.jfire.reporting.oda.server.jfs.ServerJFSQueryProxy;
import org.nightlabs.jfire.reporting.platform.ServerPlatformContext;
import org.nightlabs.jfire.reporting.scripting.ScriptingInitialiser;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.scripting.ScriptingIntialiserException;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.timepattern.TimePatternFormatException;
import org.nightlabs.version.MalformedVersionException;

/**
 * TODO: Unify method names for ResultSet and ResultSetMetaData getter (also in Dirvers, and Queries)
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @ejb.bean name="jfire/ejb/JFireReporting/ReportManager"
 *					 jndi-name="jfire/ejb/JFireReporting/ReportManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class ReportManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ReportManagerBean.class);

	/**
	 * @see com.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see com.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
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
	
	
	@SuppressWarnings("unchecked")
	private void initRegisterConfigModules(PersistenceManager pm)
	{
		// Register all Reporting-ConfigModules
		ConfigSetup configSetup = ConfigSetup.getConfigSetup(
				pm,
				getOrganisationID(),
				UserConfigSetup.CONFIG_SETUP_TYPE_USER
			);
		configSetup.getConfigModuleClasses().add(ReportLayoutConfigModule.class.getName());
		ConfigSetup.ensureAllPrerequisites(pm);
	}
	
//	private void initDefaultCatReportLayout(PersistenceManager pm, ReportCategory cat, File earDir, String catType, String germanName, String englishName)
//	throws ModuleException
//	{
//		File layoutDesign = new File(earDir, "Default-"+catType+".rptdesign");
//		logger.info("Checking default report layout fo catType "+catType+" file: "+layoutDesign);
//		if (layoutDesign.exists()) {
//			logger.info("File: "+layoutDesign+" existing");
//			ReportLayout layout = new ReportLayout(pm, cat, null);
//			try {
//				layout.loadFile(layoutDesign);
//			} catch (IOException e) {
//				logger.error("Could not read ReportLayout file for default offer layout: ", e);
//			}
//			layout.getName().setText(Locale.ENGLISH.getLanguage(), englishName);
//			layout.getName().setText(Locale.GERMAN.getLanguage(), germanName);
//			logger.info("Persisting default layout for "+catType);
//			pm.makePersistent(layout);
//			logger.info("Persisting default layout for "+catType+" ...  DONE");
//
//			Collection configs = Config.getConfigsByType(pm, getOrganisationID(), UserConfigSetup.CONFIG_TYPE_USER_CONFIG);
//			for (Iterator iter = configs.iterator(); iter.hasNext();) {
//				Config config = (Config) iter.next();
//				ReportLayoutConfigModule configModule = (ReportLayoutConfigModule)config.createConfigModule(ReportLayoutConfigModule.class, null);
////				ReportLayoutConfigModule configModule = (ReportLayoutConfigModule)ConfigModule.getAutoCreateConfigModule(pm, config, ReportLayoutConfigModule.class, null);
//				configModule.getAvailEntry(catType).setDefaultReportLayoutKey(JDOHelper.getObjectId(layout).toString());
//				logger.info("Set default for ReportLayoutConfigModule for category "+catType+" and Config "+config.getConfigKey());
//			}
//			logger.info("Created new default report layout for catType "+catType);
//		}
//	}
	
//	private void initRegisterCategoriesAndLayouts(PersistenceManager pm, JFireServerManager jfireServerManager)
//	throws ModuleException
//	{
//		// TODO: Init report categories and layouts like spcripting
//		File earDir = new File(
//				jfireServerManager.getJFireServerConfigModule()
//				.getJ2ee().getJ2eeDeployBaseDirectory()+
//				"JFireReporting.ear"
//			);
//
////		 Register internal report categories if not existent
//		ReportCategory offerCat = ReportCategory.getReportCategory(
//				pm,
//				getOrganisationID(),
//				ReportCategory.INTERNAL_CATEGORY_TYPE_OFFER
//		);
//		if (offerCat == null) {
//			offerCat = new ReportCategory(
//					null,
//					getOrganisationID(),
//					ReportCategory.INTERNAL_CATEGORY_TYPE_OFFER,
//					true
//			);
//			offerCat.getName().setText(Locale.ENGLISH.getLanguage(), "Offer Layouts");
//			offerCat.getName().setText(Locale.GERMAN.getLanguage(), "Angebots-Vorlagen");
//			pm.makePersistent(offerCat);
//		}
//		initDefaultCatReportLayout(
//				pm,
//				offerCat,
//				earDir,
//				ReportCategory.INTERNAL_CATEGORY_TYPE_OFFER,
//				"Standard Angebots-Vorlage",
//				"Default offer layout"
//			);
//
//
//		ReportCategory orderCat = ReportCategory.getReportCategory(
//				pm,
//				getOrganisationID(),
//				ReportCategory.INTERNAL_CATEGORY_TYPE_ORDER
//		);
//		if (orderCat == null) {
//			orderCat = new ReportCategory(
//					pm,
//					null,
//					getOrganisationID(),
//					ReportCategory.INTERNAL_CATEGORY_TYPE_ORDER,
//					true
//			);
//			orderCat.getName().setText(Locale.ENGLISH.getLanguage(), "Order Layouts");
//			orderCat.getName().setText(Locale.GERMAN.getLanguage(), "Auftrags-Vorlagen");
//			pm.makePersistent(orderCat);
//		}
//
//		initDefaultCatReportLayout(
//				pm,
//				orderCat,
//				earDir,
//				ReportCategory.INTERNAL_CATEGORY_TYPE_ORDER,
//				"Standard Auftrags-Vorlage",
//				"Default order layout"
//			);
//
//
//		ReportCategory invoiceCat = ReportCategory.getReportCategory(
//				pm,
//				getOrganisationID(),
//				ReportCategory.INTERNAL_CATEGORY_TYPE_INVOICE
//		);
//		if (invoiceCat == null) {
//			invoiceCat = new ReportCategory(
//					pm,
//					null,
//					getOrganisationID(),
//					ReportCategory.INTERNAL_CATEGORY_TYPE_INVOICE,
//					true
//			);
//			invoiceCat.getName().setText(Locale.ENGLISH.getLanguage(), "Invoice Layouts");
//			invoiceCat.getName().setText(Locale.GERMAN.getLanguage(), "Rechnungs-Vorlagen");
//			pm.makePersistent(invoiceCat);
//		}
//
//		initDefaultCatReportLayout(
//				pm,
//				invoiceCat,
//				earDir,
//				ReportCategory.INTERNAL_CATEGORY_TYPE_INVOICE,
//				"Standard Rechnungs-Vorlage",
//				"Default invoice layout"
//			);
//
//
//		ReportCategory deliveryNoteCat = ReportCategory.getReportCategory(
//				pm,
//				getOrganisationID(),
//				ReportCategory.INTERNAL_CATEGORY_TYPE_DELIVERY_NOTE
//		);
//		if (deliveryNoteCat == null) {
//			deliveryNoteCat = new ReportCategory(
//					pm,
//					null,
//					getOrganisationID(),
//					ReportCategory.INTERNAL_CATEGORY_TYPE_DELIVERY_NOTE,
//					true
//			);
//			deliveryNoteCat.getName().setText(Locale.ENGLISH.getLanguage(), "Deliverynote Layouts");
//			deliveryNoteCat.getName().setText(Locale.GERMAN.getLanguage(), "Lieferschein-Vorlagen");
//			pm.makePersistent(deliveryNoteCat);
//		}
//
//		initDefaultCatReportLayout(
//				pm,
//				deliveryNoteCat,
//				earDir,
//				ReportCategory.INTERNAL_CATEGORY_TYPE_DELIVERY_NOTE,
//				"Standard Lieferschein-Vorlage",
//				"Default deliverynote layout"
//			);
//
//
//		ReportCategory generalCat = ReportCategory.getReportCategory(
//				pm,
//				getOrganisationID(),
//				ReportCategory.CATEGORY_TYPE_GENERAL
//			);
//		if (generalCat == null) {
//			generalCat = new ReportCategory(
//					pm,
//					null,
//					getOrganisationID(),
//					ReportCategory.CATEGORY_TYPE_GENERAL,
//					true
//			);
//			generalCat.getName().setText(Locale.ENGLISH.getLanguage(), "General");
//			generalCat.getName().setText(Locale.GERMAN.getLanguage(), "Allgemein");
//			pm.makePersistent(generalCat);
//		}
//
//		initDefaultCatReportLayout(
//				pm,
//				generalCat,
//				earDir,
//				ReportCategory.CATEGORY_TYPE_GENERAL,
//				"Sales Statistic",
//				"Umsatz Statistik"
//			);
//	}
	
	private void initRegisterScripts(PersistenceManager pm, JFireServerManager jfireServerManager) throws InstantiationException, IllegalAccessException
	{
		ScriptRegistry.getScriptRegistry(pm).registerScriptExecutorClass(ScriptExecutorJavaClassReporting.class);
	}
	
	/**
	 * This method is called by the datastore initialization mechanism.
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws MalformedVersionException
	 * @throws ScriptingIntialiserException
	 * @throws ModuleException
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void initialise() throws InstantiationException, IllegalAccessException, MalformedVersionException, ScriptingIntialiserException
	{
		// TODO: Better check if platform initialized. Propose on birt forum.
		if (false) {
			JFireServerManager jfireServerManager = getJFireServerManager();
			try {
				try {
 
					ServerPlatformContext platformContext = new ServerPlatformContext();
					System.setProperty(Platform.PROPERTY_BIRT_HOME, platformContext.getPlatform());
					EngineConfig config = new EngineConfig();
					config.setEngineHome(platformContext.getPlatform());
					config.setLogConfig(platformContext.getPlatform(), java.util.logging.Level.ALL);
					
					config.setPlatformContext(platformContext);
					Platform.startup(config);
//					Platform.initialize(platformContext);
				} catch (Throwable t) {
					logger.log(Level.ERROR, "Initializing BIRT Platform failed!", t);
				}
			} finally {
				jfireServerManager.close();
			}
		}

		try {
			InitialContext initialContext = new InitialContext();
			try {
				new ReportingManagerFactory(initialContext, getOrganisationID()); // registers itself in JNDI
			} finally {
				initialContext.close();
			}
		} catch (NamingException x) {
			throw new RuntimeException(x); // local JNDI should always be available!
		}

		PersistenceManager pm;
		pm = getPersistenceManager();
		JFireServerManager jfireServerManager = getJFireServerManager();
		try {

			// init layout renderer
			ReportRegistry registry = ReportRegistry.getReportRegistry(pm);
			try {
				registry.registerReportRenderer(ReportLayoutRendererHTML.class);
				registry.registerReportRenderer(ReportLayoutRendererPDF.class);
			} catch (Exception e) {
				logger.warn("Could not initially register HTML ReportLayoutRenderer when initializing ReportRegistry.", e);
			}
			
			// Init scripts before module metat data check
			initRegisterScripts(pm, jfireServerManager);
			
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireReportingEAR.MODULE_NAME);
			if (moduleMetaData == null) {
			
				logger.info("Initialization of JFireReporting started ...");
	
				
				// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
				moduleMetaData = new ModuleMetaData(
						JFireReportingEAR.MODULE_NAME, "0.9.5-0-beta", "0.9.5-0-beta");
				pm.makePersistent(moduleMetaData);
				logger.info("Persisted ModuleMetaData for JFireReporting with version 0.9.5-0-beta");

				initRegisterConfigModules(pm);
				logger.info("Initialized Reporting ConfigModules");
				
			}
			
			logger.info("Intializing JFireReporting basic scripts");
			ScriptingInitialiser.initialise(pm, jfireServerManager, Organisation.DEV_ORGANISATION_ID);

			// intialise the cleanup of the render folder
			initialiseCleanupRenderedReportLayoutFoldersTask(pm);

		} finally {
			pm.close();
			jfireServerManager.close();
		}
		
	}
	
	protected void initialiseCleanupRenderedReportLayoutFoldersTask(PersistenceManager pm) {
		TaskID taskID = TaskID.create(
				getOrganisationID(),
				Task.TASK_TYPE_ID_SYSTEM,
				ReportLayoutRendererUtil.class.getSimpleName()+"#cleanupRenderedReportLayoutFolders"
		);
		Task task = null;
		try {
			task = (Task) pm.getObjectById(taskID);
		} catch (JDOObjectNotFoundException e) {
			task = null;
		}
		if (task != null) {
			logger.info("Task already initialised");
			return;
		}
		task = new Task(
				taskID,
				User.getUser(pm, getOrganisationID(), User.USER_ID_SYSTEM),
				ReportManagerHome.JNDI_NAME,
				"cleanupRenderedReportLayoutFolders"
			);
		task.getName().setText(Locale.ENGLISH.getLanguage(), "Cleanup rendered report layout folders");
		task.getDescription().setText(Locale.ENGLISH.getLanguage(), "This task deletes old folder used for rendering reports");
		try {
			task.getTimePatternSet().createTimePattern(
				"*", // year
				"*", // month
				"*", // day
				"*", // dayOfWeek
				"*", //  hour
				"15"); // minute
		} catch (TimePatternFormatException e) {
			throw new RuntimeException(e);
		}

		task.setEnabled(true);
		pm.makePersistent(task);
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void cleanupRenderedReportLayoutFolders(TaskID taskID)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			ReportLayoutRendererUtil.cleanupRenderedReportLayoutFolders();
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Never"
	 */
	public IResultSet fetchJDOQLResultSet(
			String queryText,
			Map parameters,
			JDOQLResultSetMetaData metaData
		)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return ServerJDOQLProxy.executeQuery(
					pm,
					queryText,
					parameters,
					metaData,
					true,
					new String[] {FetchPlan.ALL}
				);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public JDOQLResultSetMetaData getQueryMetaData(String organisationID, String queryText)
	{
		return JDOQLMetaDataParser.parseJDOQLMetaData(queryText);
	}
	
	/**
	 * TODO: This can be done in the client = speedup
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public JDOJSResultSetMetaData prepareJDOJSQuery(String prepareScript)
	{
		return ServerJDOJSProxy.prepareJDOJSQuery(prepareScript);
	}
	
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Never"
	 */
	public JDOJSResultSet fetchJDOJSResultSet(
			JDOJSResultSetMetaData metaData,
			String prepareScript,
			Map<String, Object> parameters
		)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return ServerJDOJSProxy.fetchJDOJSResultSet(
					pm,
					metaData,
					prepareScript,
					parameters
				);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws InstantiationException
	 * @throws ScriptException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Never"
	 */
	public IResultSetMetaData getJFSResultSetMetaData(JFSQueryPropertySet queryPropertySet) throws ScriptException, InstantiationException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return ServerJFSQueryProxy.getJFSResultSetMetaData(pm, queryPropertySet.getScriptRegistryItemID(), queryPropertySet);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws InstantiationException
	 * @throws ScriptException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Never"
	 */
	public IResultSet getJFSResultSet(
			JFSQueryPropertySet queryPropertySet,
			Map<String, Object> parameters
		) throws ScriptException, InstantiationException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return ServerJFSQueryProxy.getJFSResultSet(
					pm,
					queryPropertySet.getScriptRegistryItemID(),
					queryPropertySet,
					parameters
			);
		} finally {
			pm.close();
		}
	}
	
	/**
	 *
	 * @throws JFireReportingOdaException
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Never"
	 */
	public IParameterMetaData getJFSParameterMetaData(
			ScriptRegistryItemID scriptRegistryItemID
		) throws JFireReportingOdaException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return ServerJFSQueryProxy.getScriptParameterMetaData(
					pm,
					scriptRegistryItemID
			);
		} finally {
			pm.close();
		}
	}
	
	
	/**
	 * Returns the {@link ReportRegistryItem} with the given id.
	 * It will be detached with the given fetch-groups and fetch-depth.
	 * 
	 * @param reportRegistryItemID The id of the {@link ReportRegistryItem} to fetch.
	 * @param fetchGroups The fetch-groups to detach the item with.
	 * @param maxFetchDepth The maximum fetch-depth while detaching.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public ReportRegistryItem getReportRegistryItem (
			ReportRegistryItemID reportRegistryItemID,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ReportRegistryItem reportRegistryItem = (ReportRegistryItem)pm.getObjectById(reportRegistryItemID);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			ReportRegistryItem result = pm.detachCopy(reportRegistryItem);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Returns the {@link ReportRegistryItem}s represented by the given list of {@link ReportRegistryItemID}s.
	 * All will be detached with the given fetch-groups.
	 * 
	 * @param reportRegistryItemIDs The list of id of items to fetch.
	 * @param fetchGroups The fetch-groups to detach the items with.
	 * @param maxFetchDepth The maximum fetch-depth while detaching.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public List<ReportRegistryItem> getReportRegistryItems (
			List<ReportRegistryItemID> reportRegistryItemIDs,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			List<ReportRegistryItem> result = new ArrayList<ReportRegistryItem>();
			for (ReportRegistryItemID itemID : reportRegistryItemIDs) {
				ReportRegistryItem item = (ReportRegistryItem)pm.getObjectById(itemID);
				result.add(pm.detachCopy(item));
			}

			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Returns the {@link ReportRegistryItemID}s of all {@link ReportRegistryItem}s
	 * that are direct children of the given reportRegistryItemID.
	 * 
	 * @param reportRegistryItemID The id of the parent to search the children for.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Collection<ReportRegistryItemID> getReportRegistryItemIDsForParent(ReportRegistryItemID reportRegistryItemID)
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ReportRegistryItem item = (ReportRegistryItem) pm.getObjectById(reportRegistryItemID);
			if (item instanceof ReportCategory) {
				return new ArrayList<ReportRegistryItemID>(ReportRegistryItem.getReportRegistryItemIDsForParent(pm, (ReportCategory) item));
			} else {
				// only ReportCategorys can have children
				return Collections.emptyList();
			}
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Returns all {@link ReportRegistryItem}s for the given organisationID that do
	 * not have a parent.
	 * 
	 * @param organisationID The organisationID to search top-level items for.
	 * @param fetchGroups The fetch-groups to detach the found items with.
	 * @param maxFetchDepth The maximum fetch-depth while detaching.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Collection getTopLevelReportRegistryItems (
			String organisationID,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			Collection topLevelItems = ReportRegistryItem.getTopReportRegistryItems(pm, organisationID);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			Collection result = pm.detachCopyAll(topLevelItems);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Returns all {@link ReportRegistryItemID}s that do not have a parent.
	 * These will be only for the organisationID of the calling user.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Collection<ReportRegistryItemID> getTopLevelReportRegistryItemIDs ()
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			Collection<ReportRegistryItem> topLevelItems = ReportRegistryItem.getTopReportRegistryItems(pm, getOrganisationID());
			Collection<ReportRegistryItemID> result = new HashSet<ReportRegistryItemID>(topLevelItems.size());
			for (ReportRegistryItem item : topLevelItems) {
				result.add((ReportRegistryItemID) JDOHelper.getObjectId(item));
			}
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Get the {@link ReportingManagerFactory} for the actual organisationID.
	 * 
	 * @return The {@link ReportingManagerFactory} for the actual organisationID.
	 */
	protected ReportingManagerFactory getReportingManagerFactory() throws NamingException
	{
		return ReportingManagerFactory.getReportingManagerFactory(getInitialContext(getOrganisationID()), getOrganisationID());
	}
	
	/**
	 * Stores the given {@link ReportRegistryItem} to the datastore
	 * of the organiation of the calling user.
	 * 
	 * @param reportRegistryItem The item to store.
	 * @param get Wheter a detached copy of the stored item should be returned.
	 * @param fetchGroups If get is <code>true</code>, this defines the fetch-groups the
	 * 		retuned item will be detached with.
	 * @param maxFetchDepth If get is <code>true</code>, this defines the maximum fetch-depth
	 * 		when detaching.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public ReportRegistryItem storeRegistryItem (
			ReportRegistryItem reportRegistryItem,
			boolean get,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, reportRegistryItem, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	
	/**
	 * Delete the {@link ReportRegistryItem} with the given id.
	 * 
	 * @param reportRegistryItemID The id of the item to delete.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public void deleteRegistryItem (ReportRegistryItemID reportRegistryItemID)
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ReportRegistryItem item = null;
			try {
				item = (ReportRegistryItem) pm.getObjectById(reportRegistryItemID);
			} catch (JDOObjectNotFoundException e) {
				return;
			}
			ReportCategory parent = item.getParentCategory();
			if (parent != null && (parent instanceof ReportCategory)) {
				parent.getChildItems().remove(item);
			}
			pm.deletePersistent(item);
		} finally {
			pm.close();
		}
	}
	

	/**
	 * Renders the {@link ReportLayout} referenced by the given {@link RenderReportRequest}
	 * and returns the resulting {@link RenderedReportLayout}.
	 * 
	 * @param renderReportRequest The request defining wich report to render, to which format it
	 * 		should be rendered and wich Locale should be applied etc.
	 * 
	 * @throws NamingException Might be thrown while resoving the {@link ReportingManagerFactory}.
	 * @throws RenderReportException Might be thrown if rendering the report fails.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Never"
	 */
	public RenderedReportLayout renderReportLayout(RenderReportRequest renderReportRequest)
	throws NamingException, RenderReportException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			RenderManager rm = getReportingManagerFactory().createRenderManager();
			try {
				return rm.renderReport(pm, renderReportRequest);
			} finally {
				rm.close();
			}
		} finally {
			pm.close();
		}
	}
	
	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Collection execJDOQL (
			String jdoql,
			Map params,
			String[] fetchGroups
		)
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(jdoql);
			logger.info("Excecuting JDOQL : ");
			logger.info("");
			logger.info(jdoql);
			logger.info("");
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			Object result = q.executeWithMap(params);
			if (result instanceof Collection)
				return NLJDOHelper.getDetachedQueryResultAsList(pm, (Collection<?>)result);
			else
				return NLJDOHelper.getDetachedQueryResultAsList(pm, result);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Returns the {@link ReportLayoutLocalisationData} for the given report layout id.
	 * The localisation data contains localisation labels for the report.
	 * 
	 * @param reportLayoutID The id of the layout to get the localisation data for.
	 * @param fetchGroups The fetch-groups to detach the localisation data with.
	 * @param maxFetchDepth The maximum fetch-depth when detaching.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@SuppressWarnings("unchecked")
	public Collection<ReportLayoutLocalisationData> getReportLayoutLocalisationBundle(
			ReportRegistryItemID reportLayoutID,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			
			ReportLayout reportLayout = (ReportLayout) pm.getObjectById(reportLayoutID);
			Collection<ReportLayoutLocalisationData> bundle = ReportLayoutLocalisationData.getReportLayoutLocalisationBundle(pm, reportLayout);
			return pm.detachCopyAll(bundle);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * Stores the given {@link ReportLayoutLocalisationData} to the datastore
	 * of the organiation of the calling user.
	 * 
	 * @param bundle The bundle to store.
	 * @param get Wheter a detached copy of the stored bundle should be returned.
	 * @param fetchGroups If get is <code>true</code>, this defines the fetch-groups the
	 * 		retuned bundle will be detached with.
	 * @param maxFetchDepth If get is <code>true</code>, this defines the maximum fetch-depth
	 * 		when detaching.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@SuppressWarnings("unchecked")
	public Collection<ReportLayoutLocalisationData> storeReportLayoutLocalisationBundle(
			Collection<ReportLayoutLocalisationData> bundle, boolean get, String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			if (get) {
				Collection<ReportLayoutLocalisationData> result = new ArrayList<ReportLayoutLocalisationData>(bundle.size());
				for (ReportLayoutLocalisationData data : bundle) {
					result.add(NLJDOHelper.storeJDO(pm, data, get, fetchGroups, maxFetchDepth));
				}
				return result;
			}
			else {
				for (ReportLayoutLocalisationData data : bundle) {
					pm.makePersistent(data);
				}
				return null;
			}
		} finally {
			pm.close();
		}
	}
}
