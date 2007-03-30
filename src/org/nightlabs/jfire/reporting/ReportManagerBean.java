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

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.reporting.config.ReportLayoutConfigModule;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportLayoutLocalisationData;
import org.nightlabs.jfire.reporting.layout.ReportRegistry;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItemCarrier;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.layout.render.RenderManager;
import org.nightlabs.jfire.reporting.layout.render.RenderReportRequest;
import org.nightlabs.jfire.reporting.layout.render.RenderedReportLayout;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererHTML;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererPDF;
import org.nightlabs.jfire.reporting.oda.jdojs.JDOJSResultSet;
import org.nightlabs.jfire.reporting.oda.jdojs.JDOJSResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jdojs.server.ServerJDOJSProxy;
import org.nightlabs.jfire.reporting.oda.jdoql.JDOQLMetaDataParser;
import org.nightlabs.jfire.reporting.oda.jdoql.JDOQLResultSetMetaData;
import org.nightlabs.jfire.reporting.oda.jdoql.server.ServerJDOQLProxy;
import org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReporting;
import org.nightlabs.jfire.reporting.oda.jfs.server.ServerJFSQueryProxy;
import org.nightlabs.jfire.reporting.platform.RAPlatformContext;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.util.Utils;

/**
 * TODO: Unify method names for ResultSet and ResultSetMetaData getter (also in Dirvers, and Queries)
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @ejb.bean name="jfire/ejb/JFireReporting/ReportManager"	
 *					 jndi-name="jfire/ejb/JFireReporting/ReportManager"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class ReportManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ReportManagerBean.class);

	/**
	 * @see com.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see com.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
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
	
	
	private void initRegisterConfigModules(PersistenceManager pm) 
	{
		// Register all Reporing config-Modules
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
	 * @throws ModuleException 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type = "Required"
	 */
	public void initialise() throws InstantiationException, IllegalAccessException, ModuleException 
	{
		// TODO: Better check if platform initialized. Propose on birt forum.
		if (true) {
			JFireServerManager jfireServerManager = getJFireServerManager();
			try {
				try {
					String birtHome = Utils.addFinalSlash(
							jfireServerManager.getJFireServerConfigModule()
							.getJ2ee().getJ2eeDeployBaseDirectory())+
							"JFireReporting.ear"+File.separator+"birt"+File.separator;
 
					System.setProperty(Platform.PROPERTY_BIRT_HOME, birtHome);
					EngineConfig config = new EngineConfig();
					config.setEngineHome(birtHome);
					config.setLogConfig(birtHome, java.util.logging.Level.ALL);					
//					PlatformConfig config = new PlatformConfig();
//					config.setProperty(Platform.PROPERTY_BIRT_HOME, birtHome);
//					config.set
					
					RAPlatformContext platformContext = new RAPlatformContext(birtHome);					
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
						JFireReportingEAR.MODULE_NAME, "1.0.0-0-beta", "1.0.0-0-beta");
				pm.makePersistent(moduleMetaData);
				logger.info("Persisted ModuleMetaData for JFireReporting with version 1.0.0-0-beta");

				initRegisterConfigModules(pm);
				logger.info("Initialized Reporting ConfigModules");
				
//				initRegisterCategoriesAndLayouts(pm, jfireServerManager);
				logger.info("Initialized Reporting Categories and Layouts");
				
			}
			
			
			
		} finally {
			pm.close();
			jfireServerManager.close();
		}
		
		
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Never"
	 */
	public IResultSet fetchJDOQLResultSet(
			String queryText, 
			Map parameters,
			JDOQLResultSetMetaData metaData
		) 
	throws ModuleException
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
		} catch (Throwable t) {
			throw new ModuleException(t);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public JDOQLResultSetMetaData getQueryMetaData(String organisationID, String queryText) 
	throws ModuleException
	{
		try {
			return JDOQLMetaDataParser.parseJDOQLMetaData(queryText);
		} catch (Throwable t) {
			throw new ModuleException(t);
		}
	}
	
	/**
	 * TODO: This can be done in the client = speedup
	 * 
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public JDOJSResultSetMetaData prepareJDOJSQuery(String prepareScript)
	throws ModuleException
	{
		return ServerJDOJSProxy.prepareJDOJSQuery(prepareScript);
	}
	
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Never"
	 */
	public JDOJSResultSet fetchJDOJSResultSet(
			JDOJSResultSetMetaData metaData, 
			String prepareScript,
			Map<String, Object> parameters
		)
	throws ModuleException
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Never"
	 */
	public IResultSetMetaData getJFSResultSetMetaData(ScriptRegistryItemID scriptRegistryItemID)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return ServerJFSQueryProxy.getJFSResultSetMetaData(pm, scriptRegistryItemID);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Never"
	 */
	public IResultSet getJFSResultSet(
			ScriptRegistryItemID scriptRegistryItemID,
			Map<String, Object> parameters
		)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return ServerJFSQueryProxy.getJFSResultSet(
					pm,
					scriptRegistryItemID,
					parameters
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
	 * @ejb.transaction type = "Never"
	 */
	public IParameterMetaData getJFSParameterMetaData(
			ScriptRegistryItemID scriptRegistryItemID
		)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			try {
				return ServerJFSQueryProxy.getScriptParameterMetaData(
						pm,
						scriptRegistryItemID
				);
			} catch (OdaException e) {
				throw new ModuleException("Failed to create parameterMetaData for JFS Query!", e);
			}
		} finally {
			pm.close();
		}
	}
	
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public ReportRegistryItem getReportRegistryItem (
			ReportRegistryItemID reportRegistryItemID,
			String[] fetchGroups, int maxFetchDepth
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ReportRegistryItem reportRegistryItem = (ReportRegistryItem)pm.getObjectById(reportRegistryItemID);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			ReportRegistryItem result = (ReportRegistryItem) pm.detachCopy(reportRegistryItem);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public List<ReportRegistryItem> getReportRegistryItems (
			List<ReportRegistryItemID> reportRegistryItemIDs,
			String[] fetchGroups, int maxFetchDepth
		)
	throws ModuleException
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
				result.add((ReportRegistryItem)pm.detachCopy(item));
			}

			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection<ReportRegistryItemID> getReportRegistryItemIDsForParent(ReportRegistryItemID reportRegistryItemID)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ReportRegistryItem item = (ReportRegistryItem) pm.getObjectById(reportRegistryItemID);
			return new ArrayList<ReportRegistryItemID>(ReportRegistryItem.getReportRegistryItemIDsForParent(pm, item));
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection getTopLevelReportRegistryItems (
			String organisationID,
			String[] fetchGroups, int maxFetchDepth
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			Collection topLevelItems = ReportRegistryItem.getTopReportRegistryItems(pm, organisationID);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			Collection result = (Collection) pm.detachCopyAll(topLevelItems);
			return result;
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection<ReportRegistryItemID> getTopLevelReportRegistryItemIDs ()
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			Collection<ReportRegistryItem> topLevelItems = ReportRegistryItem.getTopReportRegistryItems(pm);
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	public Collection<ReportRegistryItemCarrier> getTopLevelReportRegistryItemCarriers (String organisationID)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			Collection topLevelItems = ReportRegistryItem.getTopReportRegistryItems(pm, organisationID);
			Collection<ReportRegistryItemCarrier> result = new HashSet<ReportRegistryItemCarrier>();
			for (Iterator iter = topLevelItems.iterator(); iter.hasNext();) {
				ReportRegistryItem item = (ReportRegistryItem) iter.next();
				result.add(new ReportRegistryItemCarrier(null, item, true));
			}
			return result;
		} finally {
			pm.close();
		}
	}
	
	
	protected ReportingManagerFactory getReportingManagerFactory()
	throws ModuleException
	{
		try {
			return ReportingManagerFactory.getReportingManagerFactory(getInitialContext(getOrganisationID()), getOrganisationID());
		} catch (NamingException e) {
			throw new ModuleException(e);
		}
	}
	
	/**
	 * @throws ModuleException
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
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			return (ReportRegistryItem)NLJDOHelper.storeJDO(pm, reportRegistryItem, get, fetchGroups, maxFetchDepth);
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
	public void deleteRegistryItem (ReportRegistryItemID reportRegistryItemID)
	throws ModuleException
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
			ReportRegistryItem parent = item.getParentItem();
			if (parent != null && (parent instanceof ReportCategory)) {
				ReportCategory cat = (ReportCategory) parent;
				cat.getChildItems().remove(item);
			}
			pm.deletePersistent(item);
		} finally {
			pm.close();
		}
	}
	

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Never"
	 * 
	 * @param renderReportRequest
	 * @return
	 */
	public RenderedReportLayout renderReportLayout(RenderReportRequest renderReportRequest)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			RenderManager rm = getReportingManagerFactory().createRenderManager();
			try {
				try {
					return rm.renderReport(pm, renderReportRequest);
				} catch (EngineException e) {
					throw new ModuleException(e);
				}
			} finally {
				rm.close();
			}
		} finally {
			pm.close();
		}
	}
	
//	/**
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type="Required"
//	 * 
//	 * @deprecated Use {@link #renderReportLayout(RenderReportRequest)} instead.
//	 */
//	public RenderedReportLayout renderReportLayout (
//			ReportRegistryItemID reportLayoutID,
//			Map params,
//			String format
//		)
//	throws ModuleException
//	{
//		RenderReportRequest request = new RenderReportRequest();
//		request.setReportRegistryItemID(reportLayoutID);
//		request.setParameters(params);
//		request.setOutputFormat(Birt.parseOutputFormat(format));
//		return renderReportLayout(request);
//	}
	
	
	/**
	 * @throws ModuleException
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
	throws ModuleException
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
			return NLJDOHelper.getDetachedQueryResult(pm, result);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	@SuppressWarnings("unchecked")
	public Collection<ReportLayoutLocalisationData> getReportLayoutLocalisationBundle(ReportRegistryItemID reportLayoutID, String[] fetchGroups, int maxFetchDepth)
	throws ModuleException
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
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	@SuppressWarnings("unchecked")
	public Collection<ReportLayoutLocalisationData> storeReportLayoutLocalisationBundle(
			Collection<ReportLayoutLocalisationData> bundle, boolean get, String[] fetchGroups, int maxFetchDepth
		)
	throws ModuleException
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
					result.add((ReportLayoutLocalisationData) NLJDOHelper.storeJDO(pm, data, get, fetchGroups, maxFetchDepth));
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
