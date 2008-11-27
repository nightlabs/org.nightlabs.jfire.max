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
import org.eclipse.birt.report.engine.api.ReportEngine;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
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
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererGeneric;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererHTML;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererPDF;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererUtil;
import org.nightlabs.jfire.reporting.oda.JFireReportingOdaException;
import org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryPropertySetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSQueryPropertySet;
import org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReporting;
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
public abstract class ReportManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 20080926L;
	
	private static final Logger logger = Logger.getLogger(ReportManagerBean.class);

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
	 * {@inheritDoc}
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

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
	
	/**
	 * 
	 * @param pm
	 * @param jfireServerManager
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void initRegisterScripts(PersistenceManager pm, JFireServerManager jfireServerManager) throws InstantiationException, IllegalAccessException
	{
		ScriptRegistry.getScriptRegistry(pm).registerScriptExecutorClass(ScriptExecutorJavaClassReporting.class);
	}

	/**
	 * This method is called by the organisation-init system and is not intended to be called directly.
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
				registry.registerReportRenderer(Birt.OutputFormat.html.toString(), ReportLayoutRendererHTML.class);
				registry.registerReportRenderer(Birt.OutputFormat.pdf.toString(), ReportLayoutRendererPDF.class);
				registry.registerReportRenderer(Birt.OutputFormat.xls.toString(), ReportLayoutRendererGeneric.class);
				registry.registerReportRenderer(Birt.OutputFormat.ppt.toString(), ReportLayoutRendererGeneric.class);
				registry.registerReportRenderer(Birt.OutputFormat.ps.toString(), ReportLayoutRendererGeneric.class);
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
	 * This method is called from a timer-task it is not intended to be called directly.
	 * The method will clean folders temporarily used for reporting. 
	 *  
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
	 * Returns the result-set meta-data for the query/script referenced by the given {@link JFSQueryPropertySet}
	 * (its scriptRegistryItemID to be precise). Note, that the properties in the given queryPropertySet might
	 * be necessary to determine the result-set meta-data, this is why this method does not operate on a 
	 * {@link ScriptRegistryItemID} but on {@link JFSQueryPropertySet}.
	 * <p>
	 * This method delegates to {@link ServerJFSQueryProxy} and will be called only for report design-time.
	 * It therefore is tagged with permission role {@link RoleConstants#editReport}.
	 * </p>
	 * 
	 * @throws InstantiationException If instantiating the referenced script fails.
	 * @throws ScriptException If creating the meta-data fails.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
	 * @ejb.transaction type="Supports"
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
	 * Obtains the {@link IJFSQueryPropertySetMetaData} of the referenced script.
	 * <p>
	 * This method delegates to {@link ServerJFSQueryProxy} and will be called only for report desing-time.
	 * It therefore is tagged with permission role {@link RoleConstants#editReport}.
	 * </p>
	 *
	 * @throws ScriptException If getting the meta-data fails.
	 * @throws InstantiationException If creating the executor fails.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
	 * @ejb.transaction type="Supports"
	 */
	public IJFSQueryPropertySetMetaData getJFSQueryPropertySetMetaData(ScriptRegistryItemID scriptID) throws ScriptException, InstantiationException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return ServerJFSQueryProxy.getJFSQueryPropertySetMetaData(pm, scriptID);
		} finally {
			pm.close();
		}
	}

	/**
	 * Executes the script referenced by the given {@link JFSQueryPropertySet} with
	 * the given queryPropertySet and the given parameters. 
	 * <p>
	 * This method delegates to {@link ServerJFSQueryProxy} and will be called only for report desing-time.
	 * It therefore is tagged with permission role {@link RoleConstants#editReport}.
	 * </p>
	 *  
	 * @throws InstantiationException If instantiating the script fails.
	 * @throws ScriptException If the script execution fails.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
	 * @ejb.transaction type="Never" @!This is tagged with Never as we don't want write operations to be performed by the script that will be executed by this call. 
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
	 * Returns the parameter meta-data for the referenced script.
	 * <p>
	 * This method delegates to {@link ServerJFSQueryProxy} and will be called only for report desing-time.
	 * It therefore is tagged with permission role {@link RoleConstants#editReport}.
	 * </p>
	 * 
	 * @throws JFireReportingOdaException
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
	 * @ejb.transaction type="Supports"
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
	 * Returns the {@link ReportRegistryItem}s represented by the given list of {@link ReportRegistryItemID}s.
	 * All will be detached with the given fetch-groups.
	 *
	 * @param reportRegistryItemIDs The list of id of items to fetch.
	 * @param fetchGroups The fetch-groups to detach the items with.
	 * @param maxFetchDepth The maximum fetch-depth while detaching.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.renderReport"
	 * @ejb.transaction type="Supports"
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
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.renderReport"
	 * @ejb.transaction type="Supports"
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
	 * Returns all {@link ReportRegistryItemID}s that do not have a parent.
	 * These will be only for the organisationID of the calling user.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.renderReport"
	 * @ejb.transaction type="Supports"
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
	 * Get the {@link ReportEnginePool} of this server.
	 *
	 * @return The {@link ReportEnginePool} of this server.
	 */
	protected ReportEnginePool getReportEnginePool() throws NamingException
	{
		return ReportEnginePool.getInstance(getInitialContext(getOrganisationID()));
	}

	/**
	 * Stores the given {@link ReportRegistryItem} to the datastore
	 * of the organisation of the calling user.
	 *
	 * @param reportRegistryItem The item to store.
	 * @param get Wheter a detached copy of the stored item should be returned.
	 * @param fetchGroups If get is <code>true</code>, this defines the fetch-groups the
	 * 		retuned item will be detached with.
	 * @param maxFetchDepth If get is <code>true</code>, this defines the maximum fetch-depth
	 * 		when detaching.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
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
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
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
	 * @param renderReportRequest The request defining which report to render, to which format it
	 * 		should be rendered and which Locale should be applied etc.
	 *
	 * @throws NamingException Might be thrown while resolving the {@link ReportingManagerFactory}.
	 * @throws RenderReportException Might be thrown if rendering the report fails.
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.renderReport"
	 * @ejb.transaction type="Never" @!Tagged with Never, so the data-sources can't perform write operations.
	 */
	public RenderedReportLayout renderReportLayout(RenderReportRequest renderReportRequest)
	throws NamingException, RenderReportException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
//			RenderManager rm = getReportingManagerFactory().createRenderManager();
			ReportEnginePool enginePool = getReportEnginePool();
			ReportEngine engine;
			try {
				engine = enginePool.borrowReportEngine();
			} catch (Exception e) {
				throw new RenderReportException("Could not borrow ReportEngine.", e);
			}
			try {
				RenderManager rm = new RenderManager(engine);
				return rm.renderReport(pm, renderReportRequest);
			} finally {
				try {
					enginePool.returnReportEngine(engine);
				} catch (Exception e) {
					throw new RenderReportException("Could not return ReportEngine", e);
				}
			}
		} finally {
			pm.close();
		}
	}

	/**
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
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
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
	 * @ejb.transaction type="Required"
	 */
	public Collection<ReportLayoutLocalisationData> getReportLayoutLocalisationBundle(
			ReportRegistryItemID reportLayoutID,
			String[] fetchGroups, int maxFetchDepth
	) {
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
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
	 * @ejb.transaction type="Required"
	 */
	public Collection<ReportLayoutLocalisationData> storeReportLayoutLocalisationBundle(
			Collection<ReportLayoutLocalisationData> bundle, boolean get, String[] fetchGroups, int maxFetchDepth
	) {
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
