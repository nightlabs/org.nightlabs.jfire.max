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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.reporting.classloader.ReportingClassLoader;
import org.nightlabs.jfire.reporting.config.ReportLayoutConfigModule;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportLayoutLocalisationData;
import org.nightlabs.jfire.reporting.layout.ReportRegistry;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportLayoutLocalisationDataID;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.layout.render.IRenderManager;
import org.nightlabs.jfire.reporting.layout.render.RenderReportException;
import org.nightlabs.jfire.reporting.layout.render.RenderReportRequest;
import org.nightlabs.jfire.reporting.layout.render.RenderedReportLayout;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererUtil;
import org.nightlabs.jfire.reporting.oda.JFireReportingOdaException;
import org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryPropertySetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSQueryPropertySet;
import org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReporting;
import org.nightlabs.jfire.reporting.oda.server.jfs.ServerJFSQueryProxy;
import org.nightlabs.jfire.reporting.scripting.ScriptingInitialiser;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.scripting.ScriptingIntialiserException;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.ResolveSecuringAuthorityStrategy;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.timepattern.TimePatternFormatException;
import org.nightlabs.util.CacheDirTag;
import org.nightlabs.util.IOUtil;

/**
 * Manager that gives access to {@link ReportRegistryItem}s and other objects linked to them.
 * This is also the entry point for rendering report layouts.
 *
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
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class ReportManagerBean
extends BaseSessionBeanImpl
implements ReportManagerRemote
{
	private static final long serialVersionUID = 20080926L;

	private static final Logger logger = Logger.getLogger(ReportManagerBean.class);

	/**
	 * Called by {@link #initialise()} and registeres the reporting
	 * config-modules in their config-setup.
	 */
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

	private static IReportEngineInitaliser reportEngineInitaliser;

	private static IReportEngineInitaliser createReportInitialiser(URL[] urls) {
		Class<?> clazz;
		String className = "org.nightlabs.jfire.reporting.birt.BirtReportEngineInitaliser";
		try {
			clazz = ReportingClassLoader.createSharedInstance(urls).loadClass(className);
			IReportEngineInitaliser reportEngineInitaliser = (IReportEngineInitaliser) clazz.newInstance();
			return reportEngineInitaliser;
		} catch (ClassNotFoundException e) {
			logger.error("Could not load class "+className);
			return null;
		} catch (InstantiationException e) {
			logger.error("Could not instantiate class "+className);
			return null;
		} catch (IllegalAccessException e) {
			logger.error("Could not access class "+className);
			return null;
		}
	}

	private IReportEngineInitaliser getReportEngineInitaliser() {
		return reportEngineInitaliser;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise()
	throws Exception
	{
//		// TODO: Better check if platform initialized. Propose on birt forum.
//		if (false) { // it seems this block is not necessary at all - the engine seems to start OSGI itself. Marco.
//			JFireServerManager jfireServerManager = getJFireServerManager();
//			try {
//				try {
//					// Classloader wrapper to avoid loading of Mozilla Rhino deployed in JBoss (1.7)
//					ReportingClassLoader wrapperClassLoader = getClassLoader(ReportManagerBean.class.getClassLoader(), null);
//					Thread.currentThread().setContextClassLoader(wrapperClassLoader);
//
////					wrapperClassLoader.loadClass("org.nightlabs.jfire.reporting.platform.ServerPlatformContext");
////					wrapperClassLoader.loadClass("org.eclipse.birt.report.engine.api.EngineConfig");
////					wrapperClassLoader.loadClass("org.eclipse.birt.core.framework.Platform");
//
//					ServerPlatformContext platformContext = new ServerPlatformContext();
//					System.setProperty(Platform.PROPERTY_BIRT_HOME, platformContext.getPlatform());
//					EngineConfig config = new EngineConfig();
//					config.setEngineHome(platformContext.getPlatform());
//					config.setLogConfig(platformContext.getPlatform(), java.util.logging.Level.ALL);
//					config.setPlatformContext(platformContext);
//
//					config.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, wrapperClassLoader);
//					Platform.setContextClassLoader(wrapperClassLoader);
//
//					Platform.startup(config);
//
//					ClassLoader osgiClassLoader = Platform.getContextClassLoader();
//					wrapperClassLoader.setOsgiClassLoader(osgiClassLoader);
//					config.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, wrapperClassLoader);
//					Platform.setContextClassLoader(wrapperClassLoader);
//				} catch (Throwable t) {
//					logger.log(Level.ERROR, "Initializing BIRT Platform failed!", t);
//				}
//			} finally {
//				jfireServerManager.close();
//			}
//		}

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
		pm = createPersistenceManager();
		JFireServerManager jfireServerManager = getJFireServerManager();
		try {
			String earName = "JFireReportingEAR.ear";
			File jfReportingEarFile = new File(jfireServerManager.getJFireServerConfigModule().getJ2ee().getJ2eeDeployBaseDirectory(), earName);

			File tmpBaseDir;
			try {
				tmpBaseDir = new File(IOUtil.createUserTempDir("jfire_server.", null), "ear");
				if (!tmpBaseDir.isDirectory())
					tmpBaseDir.mkdirs();

				if (!tmpBaseDir.isDirectory())
					throw new IOException("Could not create directory: " + tmpBaseDir.getAbsolutePath());
			} catch (IOException e) {
				throw new ScriptingIntialiserException(e);
			}

			CacheDirTag cacheDirTag = new CacheDirTag(tmpBaseDir);
			try {
				cacheDirTag.tag("JFire - http://www.jfire.org", true, false);
			} catch (IOException e) {
				logger.warn("initialise: " + e, e);
			}

			File tmpEarDir = new File(tmpBaseDir, earName);
			IOUtil.unzipArchiveIfModified(jfReportingEarFile, tmpEarDir);

			List<URL> urls = new ArrayList<URL>();
			for (File f : tmpEarDir.listFiles()) {
				if (f.getName().startsWith("org.nightlabs.jfire.reporting.birt")) {
					urls.add(f.toURI().toURL());
				}
			}
			if (reportEngineInitaliser == null) {
				synchronized (ReportManagerBean.class) {
					if (reportEngineInitaliser == null)
						reportEngineInitaliser = createReportInitialiser(urls.toArray(new URL[urls.size()]));
				}
			}

			// init layout renderer
			ReportRegistry registry = ReportRegistry.getReportRegistry(pm);
			try {
				IReportEngineInitaliser initaliser = getReportEngineInitaliser();
				if (initaliser != null) {
					initaliser.registerReportRenderer(registry);
				}
//				registry.registerReportRenderer(Birt.OutputFormat.html.toString(), ReportLayoutRendererHTML.class);
//				registry.registerReportRenderer(Birt.OutputFormat.pdf.toString(), ReportLayoutRendererPDF.class);
//				for (Birt.OutputFormat format : Birt.OutputFormat.values()) {
//					if (format != Birt.OutputFormat.html && format != Birt.OutputFormat.pdf) {
//						registry.registerReportRenderer(format.toString(), ReportLayoutRendererGeneric.class);
//					}
//				}
			} catch (Exception e) {
				logger.warn("Could not initially register HTML ReportLayoutRenderer when initializing ReportRegistry.", e);
			}

			// Init scripts before module meta data check
			ScriptRegistry.getScriptRegistry(pm).registerScriptExecutorClass(ScriptExecutorJavaClassReporting.class);

//			IReportEngineInitaliser initaliser = getReportEngineInitaliser();
//			if (initaliser != null) {
//				initaliser.initRegisterScripts(pm);
//			}

			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireReportingEAR.MODULE_NAME);
			if (moduleMetaData == null) {

				logger.info("Initialization of JFireReporting started ...");


				// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
				moduleMetaData =  pm.makePersistent(
						ModuleMetaData.createModuleMetaDataFromManifest(JFireReportingEAR.MODULE_NAME, JFireReportingEAR.class)
				);
				logger.info("Persisted ModuleMetaData for JFireReporting with version " + moduleMetaData.getSchemaVersion());

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

	/**
	 * Create (if necessary) a task that will cleanup the temporary report folders
	 * using {@link #cleanupRenderedReportLayoutFolders(TaskID)}.
	 */
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
			logger.debug("Task already initialised");
			return;
		}
		task = new Task(
				taskID,
				User.getUser(pm, getOrganisationID(), User.USER_ID_SYSTEM),
				ReportManagerRemote.class,
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#cleanupRenderedReportLayoutFolders(org.nightlabs.jfire.timer.id.TaskID)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void cleanupRenderedReportLayoutFolders(TaskID taskID)
	throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			ReportLayoutRendererUtil.cleanupRenderedReportLayoutFolders();
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#getJFSResultSetMetaData(org.nightlabs.jfire.reporting.oda.jfs.JFSQueryPropertySet)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("org.nightlabs.jfire.reporting.editReport")
	public IResultSetMetaData getJFSResultSetMetaData(JFSQueryPropertySet queryPropertySet) throws ScriptException, InstantiationException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return ServerJFSQueryProxy.getJFSResultSetMetaData(pm, queryPropertySet.getScriptRegistryItemID(), queryPropertySet);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#getJFSQueryPropertySetMetaData(org.nightlabs.jfire.scripting.id.ScriptRegistryItemID)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("org.nightlabs.jfire.reporting.editReport")
	public IJFSQueryPropertySetMetaData getJFSQueryPropertySetMetaData(ScriptRegistryItemID scriptID) throws ScriptException, InstantiationException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return ServerJFSQueryProxy.getJFSQueryPropertySetMetaData(pm, scriptID);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#getJFSResultSet(org.nightlabs.jfire.reporting.oda.jfs.JFSQueryPropertySet, java.util.Map)
	 */
	@TransactionAttribute(TransactionAttributeType.NEVER)
	@RolesAllowed("org.nightlabs.jfire.reporting.editReport")
	public IResultSet getJFSResultSet(
			JFSQueryPropertySet queryPropertySet,
			Map<String, Object> parameters
		) throws ScriptException, InstantiationException
	{
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#getJFSParameterMetaData(org.nightlabs.jfire.scripting.id.ScriptRegistryItemID)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("org.nightlabs.jfire.reporting.editReport")
	public IParameterMetaData getJFSParameterMetaData(
			ScriptRegistryItemID scriptRegistryItemID
		) throws JFireReportingOdaException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return ServerJFSQueryProxy.getScriptParameterMetaData(
					pm,
					scriptRegistryItemID
			);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#getReportRegistryItems(java.util.List, org.nightlabs.jfire.security.id.RoleID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("org.nightlabs.jfire.reporting.renderReport")
	public List<ReportRegistryItem> getReportRegistryItems (
			List<ReportRegistryItemID> reportRegistryItemIDs, RoleID filterRoleID,
			String[] fetchGroups, int maxFetchDepth
	)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			List<ReportRegistryItem> result = new ArrayList<ReportRegistryItem>();
			try{
				for (ReportRegistryItemID itemID : reportRegistryItemIDs) {
					ReportRegistryItem item = (ReportRegistryItem)pm.getObjectById(itemID);
					result.add(pm.detachCopy(item));
				}
			}
			catch (JDOObjectNotFoundException e) {
				// ignore it
			}
			return Authority.filterSecuredObjects(pm, result, getPrincipal(), filterRoleID, ResolveSecuringAuthorityStrategy.organisation);	
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#getReportRegistryItems(java.util.List, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("org.nightlabs.jfire.reporting.renderReport")
	public List<ReportRegistryItem> getReportRegistryItems (
			List<ReportRegistryItemID> reportRegistryItemIDs,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		return getReportRegistryItems(reportRegistryItemIDs, RoleConstants.renderReport, fetchGroups, maxFetchDepth);
	}
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#getReportRegistryItemIDsForParent(org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID, org.nightlabs.jfire.security.id.RoleID)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("org.nightlabs.jfire.reporting.renderReport")
	public Collection<ReportRegistryItemID> getReportRegistryItemIDsForParent(ReportRegistryItemID reportRegistryItemID, RoleID filterRoleID) {
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			ReportRegistryItem item = (ReportRegistryItem) pm.getObjectById(reportRegistryItemID);
			Collection<ReportRegistryItemID> result = Collections.emptyList();
			if (item instanceof ReportCategory) {
				result = new ArrayList<ReportRegistryItemID>(ReportRegistryItem.getReportRegistryItemIDsForParent(pm, (ReportCategory) item));
			}
			return Authority.filterSecuredObjectIDs(pm, result, getPrincipal(), filterRoleID, ResolveSecuringAuthorityStrategy.organisation);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#getReportRegistryItemIDsForParent(org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("org.nightlabs.jfire.reporting.renderReport")
	public Collection<ReportRegistryItemID> getReportRegistryItemIDsForParent(ReportRegistryItemID reportRegistryItemID) {
		return getReportRegistryItemIDsForParent(reportRegistryItemID, RoleConstants.renderReport);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#getTopLevelReportRegistryItemIDs(org.nightlabs.jfire.security.id.RoleID)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("org.nightlabs.jfire.reporting.renderReport")
	public Collection<ReportRegistryItemID> getTopLevelReportRegistryItemIDs(RoleID filterRoleID) {
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			Collection<ReportRegistryItem> topLevelItems = ReportRegistryItem.getTopReportRegistryItems(pm, getOrganisationID());
			Collection<ReportRegistryItemID> result = new HashSet<ReportRegistryItemID>(topLevelItems.size());
			for (ReportRegistryItem item : topLevelItems) {
				result.add((ReportRegistryItemID) JDOHelper.getObjectId(item));
			}
			Authority.filterSecuredObjectIDs(pm, result, getPrincipal(), filterRoleID, ResolveSecuringAuthorityStrategy.organisation);
			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#getTopLevelReportRegistryItemIDs()
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("org.nightlabs.jfire.reporting.renderReport")
	public Collection<ReportRegistryItemID> getTopLevelReportRegistryItemIDs() {
		return getTopLevelReportRegistryItemIDs(RoleConstants.renderReport);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#storeRegistryItem(org.nightlabs.jfire.reporting.layout.ReportRegistryItem, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.reporting.editReport")
	public ReportRegistryItem storeRegistryItem (
			ReportRegistryItem reportRegistryItemToStore,
			boolean get,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			// first check if user is allowed to store the registry item
			// check if user is allowed to render
			if (JDOHelper.isDetached(reportRegistryItemToStore)) {
				// if the object is not new, it might have an authority assigned and
				// role checking on EJB method call level might not be sufficient
				ReportRegistryItem registryItemPersistent = (ReportRegistryItem) pm.getObjectById(JDOHelper.getObjectId(reportRegistryItemToStore));
				Authority.resolveSecuringAuthority(pm, registryItemPersistent, ResolveSecuringAuthorityStrategy.organisation)
					.assertContainsRoleRef(getPrincipal(), RoleConstants.renderReport);
			}

			ReportRegistryItem item = pm.makePersistent(reportRegistryItemToStore);

			item.applyInheritance();

			if (!get)
				return null;

			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);

			return pm.detachCopy(item);
		} finally {
			pm.close();
		}
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#deleteRegistryItem(org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.reporting.editReport")
	public void deleteRegistryItem (ReportRegistryItemID reportRegistryItemID)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			ReportRegistryItem item = null;
			try {
				item = (ReportRegistryItem) pm.getObjectById(reportRegistryItemID);
			} catch (JDOObjectNotFoundException e) {
				return;
			}
			// Do the object level access right check also before deleting
			Authority.resolveSecuringAuthority(pm, item, ResolveSecuringAuthorityStrategy.organisation)
				.assertContainsRoleRef(getPrincipal(), RoleConstants.renderReport);
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
	 * Get the {@link ReportingManagerFactory} for the actual organisationID.
	 *
	 * @return The {@link ReportingManagerFactory} for the actual organisationID.
	 */
	protected ReportingManagerFactory getReportingManagerFactory() throws NamingException
	{
		return ReportingManagerFactory.getReportingManagerFactory(getInitialContext(getOrganisationID()), getOrganisationID());
	}

//	/**
//	 * Get the {@link ReportEnginePool} of this server.
//	 *
//	 * @return The {@link ReportEnginePool} of this server.
//	 */
//	protected ReportEnginePool getReportEnginePool() throws NamingException
//	{
//		return ReportEnginePool.getInstance(getInitialContext(getOrganisationID()));
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#renderReportLayout(org.nightlabs.jfire.reporting.layout.render.RenderReportRequest)
	 */
	@TransactionAttribute(TransactionAttributeType.NEVER)
	@RolesAllowed("org.nightlabs.jfire.reporting.renderReport")
	public RenderedReportLayout renderReportLayout(RenderReportRequest renderReportRequest)
	throws NamingException, RenderReportException
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			// check if user is allowed to render
			ReportRegistryItem registryItem = (ReportRegistryItem) pm.getObjectById(renderReportRequest.getReportRegistryItemID());
			Authority.resolveSecuringAuthority(pm, registryItem, ResolveSecuringAuthorityStrategy.organisation)
				.assertContainsRoleRef(getPrincipal(), RoleConstants.renderReport);

			IReportEngineInitaliser initaliser = getReportEngineInitaliser();
			if (initaliser != null) {
				initaliser.initReportEngine();
				IRenderManager rm = initaliser.createRenderManager();
				return rm.renderReport(pm, renderReportRequest);
			}
			else
				return null;

//			ReportEngine engine;
//			engine = getReportingManagerFactory().getReportEngine();
//			ReportEnginePool enginePool = getReportEnginePool();
//			try {
//				engine = enginePool.borrowReportEngine();
//			} catch (Exception e) {
//				throw new RenderReportException("Could not borrow ReportEngine.", e);
//			}
//			try {
//				RenderManager rm = new RenderManager(engine);
//				return rm.renderReport(pm, renderReportRequest);
//			} finally {
//				try {
//					enginePool.returnReportEngine(engine);
//				} catch (Exception e) {
//					throw new RenderReportException("Could not return ReportEngine", e);
//				}
//			}
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#execJDOQL(java.lang.String, java.util.Map, java.lang.String[])
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.reporting.editReport")
	public Collection execJDOQL (
			String jdoql,
			Map params,
			String[] fetchGroups
		)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#getReportLayoutLocalisationBundle(org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.reporting.editReport")
	public Collection<ReportLayoutLocalisationData> getReportLayoutLocalisationBundle(
			ReportRegistryItemID reportLayoutID,
			String[] fetchGroups, int maxFetchDepth
	) {
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			ReportLayout reportLayout = (ReportLayout) pm.getObjectById(reportLayoutID);

			// now do the access right check also on object level
			Authority.resolveSecuringAuthority(pm, reportLayout, ResolveSecuringAuthorityStrategy.organisation)
				.assertContainsRoleRef(getPrincipal(), RoleConstants.editReport);

			Collection<ReportLayoutLocalisationData> bundle = ReportLayoutLocalisationData.getReportLayoutLocalisationBundle(pm, reportLayout);
			return pm.detachCopyAll(bundle);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#storeReportLayoutLocalisationBundle(java.util.Collection, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.reporting.editReport")
	public Collection<ReportLayoutLocalisationData> storeReportLayoutLocalisationBundle(
			Collection<ReportLayoutLocalisationData> bundle, boolean get, String[] fetchGroups, int maxFetchDepth
	) {
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Set<ReportRegistryItemID> checkItemIDs = new HashSet<ReportRegistryItemID>();
			// collect all item IDs to check because its possible that there are several referenced in the collection of bundles
			for (ReportLayoutLocalisationData data : bundle) {
				ReportRegistryItemID itemID = ReportRegistryItemID.create(
						data.getOrganisationID(), data.getReportRegistryItemType(), data.getReportRegistryItemID());
				checkItemIDs.add(itemID);
			}
			// now check the access rights for all referenced
			for (ReportRegistryItemID itemID : checkItemIDs) {
				ReportRegistryItem item = (ReportRegistryItem) pm.getObjectById(itemID);
				Authority.resolveSecuringAuthority(pm, item, ResolveSecuringAuthorityStrategy.organisation)
					.assertContainsRoleRef(getPrincipal(), RoleConstants.editReport);
			}
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
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#importReportLayoutZipFile(java.io.File)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.reporting.editReport")
	public boolean importReportLayoutZipFile(final File reportLayoutZipFile, ReportRegistryItemID reportCategoryID, I18nText name) {
		try {
			//Unzip into tmp folder
			File tmpFolder = IOUtil.createUserTempDir("jfire_report.imported.", null);
			IOUtil.unzipArchive(reportLayoutZipFile, tmpFolder);
			tmpFolder.deleteOnExit();
			
			//Create tmp layout file
			File tmpFile = File.createTempFile(IOUtil.getFileNameWithoutExtension(reportLayoutZipFile.getName()), ".rptdesign", tmpFolder);
			tmpFile.deleteOnExit();
			
			//Create template file
			File[] templateFiles = tmpFolder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isFile() && IOUtil.getFileNameWithoutExtension(pathname.getName()).equals(IOUtil.getFileNameWithoutExtension(reportLayoutZipFile.getName()));
				}
			});
			
			if (templateFiles == null) 
				return false;

			ReportingInitialiser.importTemplateToLayoutFile(templateFiles[0], tmpFile);
			
			PersistenceManager pm = createPersistenceManager();

			ReportCategory category = (ReportCategory)pm.getObjectById(reportCategoryID);
			ReportLayout layout = new ReportLayout(category, category.getOrganisationID(), "REPORT_REGISTRY_ITEM_TYPE_UNKNOWN", IOUtil.getFileNameWithoutExtension(reportLayoutZipFile.getName()));
//			layout.getName().copyFrom(name); //TODO: Uncomment this line.
			layout.getName().setText(Locale.ENGLISH, "Hello");
			layout.loadFile(templateFiles[0]);
			layout = pm.makePersistent(layout);
			category.addChildItem(layout);
			
			//From createReportLocalisationData
			//TODO: Should we do like this?
			pm.getExtent(ReportLayoutLocalisationData.class);

			File resourceFolder = new File(tmpFolder, "resource");
			File[] resourceFiles = resourceFolder.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile() && pathname.getName().startsWith(reportLayoutZipFile.getName());
				}
			});
			
			if (resourceFiles == null)
				return false;
			for (File resFile : resourceFiles) {
				String locale = ReportLayoutLocalisationData.extractLocale(resFile.getName());
				if (locale == null)
					locale = "";
				ReportLayoutLocalisationDataID localisationDataID = ReportLayoutLocalisationDataID.create(
						layout.getOrganisationID(), layout.getReportRegistryItemType(), layout.getReportRegistryItemID(), locale
				);
				ReportLayoutLocalisationData localisationData = null;
				try {
					localisationData = (ReportLayoutLocalisationData) pm.getObjectById(localisationDataID);
					localisationData.getLocale();
				} catch (JDOObjectNotFoundException e) {
					localisationData = new ReportLayoutLocalisationData(layout, locale);
					localisationData = pm.makePersistent(localisationData);
				}
				try {
					localisationData.loadFile(resFile);
				} catch (IOException e) {
					throw new ReportingInitialiserException("Could not load localisatino file "+resFile, e);
				}
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
   
		return true;
	}
}
