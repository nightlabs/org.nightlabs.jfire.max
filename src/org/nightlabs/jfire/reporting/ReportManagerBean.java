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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.UserConfigSetup;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.reporting.config.ReportLayoutConfigModule;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportLayoutLocalisationData;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.layout.render.RenderReportException;
import org.nightlabs.jfire.reporting.layout.render.RenderReportRequest;
import org.nightlabs.jfire.reporting.layout.render.RenderedReportLayout;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererUtil;
import org.nightlabs.jfire.reporting.oda.JFireReportingOdaException;
import org.nightlabs.jfire.reporting.oda.jfs.IJFSQueryPropertySetMetaData;
import org.nightlabs.jfire.reporting.oda.jfs.JFSQueryPropertySet;
import org.nightlabs.jfire.reporting.oda.jfs.ScriptExecutorJavaClassReporting;
import org.nightlabs.jfire.reporting.oda.server.jfs.ServerJFSQueryProxy;
import org.nightlabs.jfire.reporting.parameter.ReportParameterManagerRemote;
import org.nightlabs.jfire.reporting.parameter.config.AcquisitionParameterConfig;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionUseCase;
import org.nightlabs.jfire.reporting.parameter.config.ValueAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ValueConsumerBinding;
import org.nightlabs.jfire.reporting.parameter.config.ValueProviderConfig;
import org.nightlabs.jfire.reporting.parameter.config.id.ReportParameterAcquisitionSetupID;
import org.nightlabs.jfire.reporting.scripting.ScriptingInitialiser;
import org.nightlabs.jfire.reporting.textpart.ReportTextPart;
import org.nightlabs.jfire.reporting.textpart.ReportTextPartConfiguration;
import org.nightlabs.jfire.reporting.textpart.ReportTextPartManagerRemote;
import org.nightlabs.jfire.scripting.ScriptException;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.ResolveSecuringAuthorityStrategy;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.timepattern.TimePatternFormatException;
import org.nightlabs.util.IOUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise()
	throws Exception
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		JFireServerManager jfireServerManager = getJFireServerManager();
		try {

			// Init scripts before module meta data check
			ScriptRegistry.getScriptRegistry(pm).registerScriptExecutorClass(ScriptExecutorJavaClassReporting.class);


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
	@RolesAllowed(RoleConstants.editReport_roleID)
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
	@RolesAllowed(RoleConstants.editReport_roleID)
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
	@RolesAllowed(RoleConstants.editReport_roleID)
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
	@RolesAllowed(RoleConstants.editReport_roleID)
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
	@RolesAllowed(RoleConstants.renderReport_roleID)
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
	@RolesAllowed(RoleConstants.renderReport_roleID)
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
	@RolesAllowed(RoleConstants.renderReport_roleID)
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
	@RolesAllowed(RoleConstants.renderReport_roleID)
	public Collection<ReportRegistryItemID> getReportRegistryItemIDsForParent(ReportRegistryItemID reportRegistryItemID) {
		return getReportRegistryItemIDsForParent(reportRegistryItemID, RoleConstants.renderReport);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#getTopLevelReportRegistryItemIDs(org.nightlabs.jfire.security.id.RoleID)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed(RoleConstants.renderReport_roleID)
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
	@RolesAllowed(RoleConstants.renderReport_roleID)
	public Collection<ReportRegistryItemID> getTopLevelReportRegistryItemIDs() {
		return getTopLevelReportRegistryItemIDs(RoleConstants.renderReport);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#storeRegistryItem(org.nightlabs.jfire.reporting.layout.ReportRegistryItem, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed(RoleConstants.editReport_roleID)
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
	@RolesAllowed(RoleConstants.editReport_roleID)
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
			pm.flush();
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#renderReportLayout(org.nightlabs.jfire.reporting.layout.render.RenderReportRequest)
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed(RoleConstants.renderReport_roleID)
	public RenderedReportLayout renderReportLayout(RenderReportRequest renderReportRequest)
	throws NamingException, RenderReportException
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			return ReportLayoutRendererUtil.renderReport(pm, renderReportRequest);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#execJDOQL(java.lang.String, java.util.Map, java.lang.String[])
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed(RoleConstants.editReport_roleID)
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
	@RolesAllowed(RoleConstants.editReport_roleID)
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
	@RolesAllowed(RoleConstants.editReport_roleID)
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

	private static final String TMP_FOLDER_IMPORT_PREFIX = "jfire_report.server.imported.";
	private static final String TMP_FOLDER_IMPORT_SUFFIX = ".report";
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#importReportLayoutZipFile(java.io.File)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.reporting.editReport")
	public boolean importReportLayoutZipFile(byte[] zipFileData, ReportRegistryItemID registryItemID) {
		File tmpFolder = null;
		try {
			//Unzip into tmp folder
			tmpFolder = IOUtil.createUserTempDir(TMP_FOLDER_IMPORT_PREFIX, TMP_FOLDER_IMPORT_SUFFIX);

			File tmpZipFile = new File(tmpFolder, "data.zip");
			FileOutputStream fos = new FileOutputStream(tmpZipFile);
			fos.write(zipFileData);
			File reportFolder = new File(tmpFolder, "zipData");
			reportFolder.mkdir();
			IOUtil.unzipArchive(tmpZipFile, reportFolder);
			tmpFolder.deleteOnExit();

			ReportCategory reportCategory;
			PersistenceManager pm = createPersistenceManager();
			try {
				pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
				reportCategory = (ReportCategory)pm.getObjectById(registryItemID);

				ReportingInitialiser reportingInitialiser = new ReportingInitialiser(tmpFolder, 
						"", 
						reportCategory, 
						pm, 
						getOrganisationID());
				reportingInitialiser.initialise();
			} finally {
				pm.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (tmpFolder != null)
				tmpFolder.delete();
		}
		return true;
	}

	private static final String TMP_FOLDER_EXPORT_PREFIX = "jfire_report.server.exported.";
	private static final String TMP_FOLDER_EXPORT_SUFFIX = ".report";

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.ReportManagerRemote#importReportLayoutZipFile(java.io.File)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("org.nightlabs.jfire.reporting.editReport")
	public boolean exportReportLayout(String layoutName, ReportRegistryItemID layoutID) {
		ReportParameterAcquisitionSetup parameterSetup = null;
		Set<ReportRegistryItemID> itemIDs = new HashSet<ReportRegistryItemID>(1);
		itemIDs.add(layoutID);
		ReportParameterManagerRemote rpm = JFireEjb3Factory.getRemoteBean(ReportParameterManagerRemote.class, SecurityReflector.getInitialContextProperties());
		Map<ReportRegistryItemID, ReportParameterAcquisitionSetupID> ids = rpm.getReportParameterAcquisitionSetupIDs(itemIDs);
		ReportParameterAcquisitionSetupID setupID = ids.get(rpm);
		Set<ReportParameterAcquisitionSetupID> rpasIDs = new HashSet<ReportParameterAcquisitionSetupID>();
		rpasIDs.add(setupID);
		parameterSetup = rpm.getReportParameterAcquisitionSetups(rpasIDs, 
				new String[] {FetchPlan.DEFAULT, 
				ReportParameterAcquisitionSetup.FETCH_GROUP_VALUE_ACQUISITION_SETUPS,
				ReportParameterAcquisitionSetup.FETCH_GROUP_DEFAULT_USE_CASE,
				ReportParameterAcquisitionUseCase.FETCH_GROUP_NAME,
				ReportParameterAcquisitionUseCase.FETCH_GROUP_DESCRIPTION,
				ValueAcquisitionSetup.FETCH_GROUP_VALUE_CONSUMER_BINDINGS,
				ValueAcquisitionSetup.FETCH_GROUP_VALUE_PROVIDER_CONFIGS,
				ValueAcquisitionSetup.FETCH_GROUP_PARAMETER_CONFIGS,
				ValueProviderConfig.FETCH_GROUP_MESSAGE,
				ValueConsumerBinding.FETCH_GROUP_CONSUMER,
				ValueConsumerBinding.FETCH_GROUP_PROVIDER}, 
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT).iterator().next();

		ReportTextPartConfiguration reportTextPartConfiguration = null;
		ReportTextPartManagerRemote rtpm = JFireEjb3Factory.getRemoteBean(ReportTextPartManagerRemote.class, SecurityReflector.getInitialContextProperties());
		reportTextPartConfiguration = rtpm.getReportTextPartConfiguration(layoutID, 
				false, 
				new String[] { FetchPlan.DEFAULT, 
				ReportTextPartConfiguration.FETCH_GROUP_REPORT_TEXT_PARTS,
				ReportTextPart.FETCH_GROUP_CONTENT,
				ReportTextPart.FETCH_GROUP_NAME }, 
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);

		PersistenceManager pm;
		pm = createPersistenceManager();

		ReportLayout layout = null;

		String[] fetchGroups = new String[] {
				FetchPlan.DEFAULT,
				ReportRegistryItem.FETCH_GROUP_NAME
		};

		try {
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setGroups(fetchGroups);

			layout = (ReportLayout)pm.getObjectById(layoutID);
		} finally {
			pm.close();
		}


		File tmpFolder = null;
		File exportFile = new File(tmpFolder, "ReportLayout_" + layout.getOrganisationID()+ "_" + layout.getReportRegistryItemID() + ".rptdesign");
		try {
			//Report File
			tmpFolder = IOUtil.createUserTempDir(TMP_FOLDER_EXPORT_PREFIX, TMP_FOLDER_EXPORT_SUFFIX);

			if (!exportFile.exists()) {
				try {
					exportFile.createNewFile();
				} catch (IOException e) {
					throw new IllegalStateException("Could not create temporary file for remote layout: "+exportFile.getAbsolutePath(), e); //$NON-NLS-1$
				}
			}
			try {
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(exportFile));
				try {
					InputStream in = layout.createReportDesignInputStream();
					try {
						IOUtil.transferStreamData(in, out);
					} finally {
						in.close();
					}
				} finally {
					out.close();
				}
			} catch (FileNotFoundException e) {
				throw new IllegalStateException("Could not find temporary file for remote layout: " + exportFile.getAbsolutePath()); //$NON-NLS-1$
			} catch (IOException e) {
				throw new RuntimeException("Could not write temporary file for remote layout: " + exportFile.getAbsolutePath(), e); //$NON-NLS-1$
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (tmpFolder != null)
				tmpFolder.delete();
		}

		//Properties Files
		Collection<ReportLayoutLocalisationData> bundle = getReportLayoutLocalisationBundle(
				layoutID,
				new String[] {FetchPlan.DEFAULT, ReportLayoutLocalisationData.FETCH_GROUP_LOCALISATOIN_DATA},
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT
		);
		File resourceFolder = new File(tmpFolder, "resource"); //$NON-NLS-1$
		resourceFolder.mkdirs();
		for (ReportLayoutLocalisationData data : bundle) {
			String l10nFileName = layoutName;
			if ("".equals(data.getLocale())) //$NON-NLS-1$
				l10nFileName = l10nFileName + ".properties"; //$NON-NLS-1$
			else
				l10nFileName = l10nFileName + "_" + data.getLocale() + ".properties";  //$NON-NLS-1$ //$NON-NLS-2$

			try {
				File dataFile = new File(resourceFolder, l10nFileName);
				dataFile.createNewFile();
				InputStream in = data.createLocalisationDataInputStream();
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dataFile));
				try {
					IOUtil.transferStreamData(in, out);
				} finally {
					in.close();
					out.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		//Generates descriptor file(content.xml)
		File descriptorFile = new File(tmpFolder, ReportingConstants.DESCRIPTOR_FILE);
		try {
			descriptorFile.createNewFile();

			//Create document
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			/*****ReportCategory-node*****/
			Element reportCategoryNode = doc.createElement(ReportingConstants.REPORT_CATEGORY_ELEMENT);
			reportCategoryNode.setAttribute(ReportingConstants.REPORT_CATEGORY_ELEMENT_ATTRIBUTE_ID, layout.getParentCategoryID().reportRegistryItemID);
			reportCategoryNode.setAttribute(ReportingConstants.REPORT_CATEGORY_ELEMENT_ATTRIBUTE_TYPE, layoutID.reportRegistryItemType);
			doc.appendChild(reportCategoryNode);

			//Report
			Element reportNode = doc.createElement(ReportingConstants.REPORT_ELEMENT);
			reportNode.setAttribute(ReportingConstants.REPORT_ELEMENT_ATTRIBUTE_FILE, exportFile.getName());
			reportNode.setAttribute(ReportingConstants.REPORT_ELEMENT_ATTRIBUTE_ID, layoutID.reportRegistryItemID);
			reportNode.setAttribute(ReportingConstants.REPORT_ELEMENT_ATTRIBUTE_ENGINE_TYPE, "BIRT");
			reportNode.setAttribute(ReportingConstants.REPORT_ELEMENT_ATTRIBUTE_OVERWRITE_ON_INIT, "true"); //Has to overwrite the old file
			reportCategoryNode.appendChild(reportNode);

			//Report-names
			generateI18nElements(doc, reportNode, ReportingConstants.REPORT_ELEMENT_NAME, layout.getName());

			//Report-descriptions
			generateI18nElements(doc, reportNode, ReportingConstants.REPORT_ELEMENT_DESCRIPTION, layout.getDescription());

			//Parameter-acquisition
			Element parameterAcquisition = doc.createElement(ReportingConstants.PARAMETER_ACQUISITION_ELEMENT);
			reportNode.appendChild(parameterAcquisition);

			int idNo = 0;
			Map<Object, Integer> object2IdNo = new HashMap<Object, Integer>();

			//Use-case
			Map<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup> valueAcquisitionSetups = 
				parameterSetup.getValueAcquisitionSetups();
			for (Map.Entry<ReportParameterAcquisitionUseCase, ValueAcquisitionSetup> setup : valueAcquisitionSetups.entrySet()) {
				Element useCaseNode = doc.createElement(ReportingConstants.USE_CASE_ELEMENT);
				useCaseNode.setAttribute(ReportingConstants.USE_CASE_ATTRIBUTE_ID, setup.getKey().getReportParameterAcquisitionUseCaseID());
				useCaseNode.setAttribute(ReportingConstants.USE_CASE_ATTRIBUTE_DEFAULT, "true");

				generateI18nElements(doc, useCaseNode, ReportingConstants.USE_CASE_ELEMENT_NAME, setup.getKey().getName());
				generateI18nElements(doc, useCaseNode, ReportingConstants.USE_CASE_ELEMENT_DESCRIPTION, setup.getKey().getDescription());

				//Parameters
				Element parametersNode = doc.createElement("parameters");
				useCaseNode.appendChild(parametersNode);

				int idx = 0;
				for (AcquisitionParameterConfig parameterConfig : setup.getValue().getParameterConfigs()) {
					Element parameterNode = doc.createElement(ReportingConstants.PARAMETER_ELEMENT);
					parameterNode.setAttribute(ReportingConstants.PARAMETER_ELEMENT_ATTRIBUTE_ID, Integer.toString(idx++));
					parameterNode.setAttribute(ReportingConstants.PARAMETER_ELEMENT_ATTRIBUTE_NAME, parameterConfig.getParameterID()); //TODO!!! WATCHME!!! Name == PARAMETER_ID ;-)
					parameterNode.setAttribute(ReportingConstants.PARAMETER_ELEMENT_ATTRIBUTE_TYPE, parameterConfig.getParameterType());
					parameterNode.setAttribute(ReportingConstants.PARAMETER_ELEMENT_ATTRIBUTE_X, Integer.toString(parameterConfig.getX()));
					parameterNode.setAttribute(ReportingConstants.PARAMETER_ELEMENT_ATTRIBUTE_Y, Integer.toString(parameterConfig.getY()));

					object2IdNo.put(parameterConfig, idNo);
					idNo++;

					parametersNode.appendChild(parameterNode);
				}

				//Value-provider-configs
				Element valueProviderConfigsNode = doc.createElement(ReportingConstants.VALUE_PROVIDER_CONFIGS_ELEMENT);
				useCaseNode.appendChild(valueProviderConfigsNode);

				for (ValueProviderConfig valueProviderConfig : setup.getValue().getValueProviderConfigs()) {
					Element providerConfigNode = doc.createElement(ReportingConstants.PROVIDER_CONFIG_ELEMENT);
					providerConfigNode.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_ID, Integer.toString(idx++));
					providerConfigNode.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_ORGANISATION_ID, "dev.jfire.org");
					providerConfigNode.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_CATEGORY_ID, valueProviderConfig.getValueProviderCategoryID());
					providerConfigNode.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_VALUE_PROVIDER_ID, valueProviderConfig.getValueProviderID());
					providerConfigNode.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_PAGE_INDEX, Integer.toString(valueProviderConfig.getPageIndex()));
					providerConfigNode.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_PAGE_ROW, Integer.toString(valueProviderConfig.getPageRow()));
					providerConfigNode.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_PAGE_COLUMN, Integer.toString(valueProviderConfig.getPageColumn()));
					providerConfigNode.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_ALLOW_NULL_OUTPUT_VALUE, Boolean.toString(valueProviderConfig.isAllowNullOutputValue()));
					providerConfigNode.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_SHOW_MESSAGE_IN_HEADER, Boolean.toString(valueProviderConfig.isShowMessageInHeader()));
					providerConfigNode.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_GROW_VERTICALLY, Boolean.toString(valueProviderConfig.isGrowVertically()));
					providerConfigNode.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_X, Integer.toString(valueProviderConfig.getX()));
					providerConfigNode.setAttribute(ReportingConstants.PROVIDER_CONFIG_ELEMENT_ATTRIBUTE_Y, Integer.toString(valueProviderConfig.getY()));

					object2IdNo.put(valueProviderConfig, idNo);
					idNo++;

					generateI18nElements(doc, providerConfigNode, ReportingConstants.PROVIDER_CONFIG_ELEMENT_MESSAGE, valueProviderConfig.getMessage());

					valueProviderConfigsNode.appendChild(providerConfigNode);
				}

				//Value-consumer-bindings
				Element valueConsumerBindingsNode = doc.createElement(ReportingConstants.VALUE_CONSUMER_BINDINGS_ELEMENT);
				useCaseNode.appendChild(valueConsumerBindingsNode);

				for (ValueConsumerBinding valueConsumerBinding : setup.getValue().getValueConsumerBindings()) {
					Element consumerBindingNode = doc.createElement(ReportingConstants.VALUE_CONSUMER_BINDING_ELEMENT);

					Element bindingProvider = doc.createElement(ReportingConstants.BINDING_PROVIDER_ELEMENT);
					bindingProvider.setAttribute(ReportingConstants.BINDING_PROVIDER_ELEMENT_ATTRIBUTE_ID, String.valueOf(object2IdNo.get(valueConsumerBinding.getProvider())));

					Element bindingParameter = doc.createElement(ReportingConstants.BINDING_PARAMETER_ELEMENT);
					bindingParameter.setAttribute(ReportingConstants.BINDING_PARAMETER_ELEMENT_ATTRIBUTE_NAME, valueConsumerBinding.getParameterID());

					Element bindingConsumer = doc.createElement(ReportingConstants.BINDING_CONSUMER_ELEMENT); 
					bindingConsumer.setAttribute(ReportingConstants.BINDING_CONSUMER_ELEMENT_ATTRIBUTE_ID, String.valueOf(object2IdNo.get(valueConsumerBinding.getConsumer())));

					consumerBindingNode.appendChild(bindingProvider);
					consumerBindingNode.appendChild(bindingParameter);
					consumerBindingNode.appendChild(bindingConsumer);

					object2IdNo.put(valueConsumerBinding, idNo);
					idNo++;

					valueConsumerBindingsNode.appendChild(consumerBindingNode);
				}

				parameterAcquisition.appendChild(useCaseNode);	
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, ReportingConstants.DESCRIPTOR_FILE_ENCODING);
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, ReportingConstants.DESCRIPTOR_FILE_DOCTYPE_SYSTEM);
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, ReportingConstants.DESCRIPTOR_FILE_DOCTYPE_PUBLIC); 

			//Write the XML document to a file
			//initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);

			String xmlString = result.getWriter().toString();

			IOUtil.writeTextFile(descriptorFile, xmlString);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		//----------End------------------

		if (reportTextPartConfiguration != null) {
			//Generate report text part configuration file (<reportID>.ReportTextPartConfiguration.xml)
			File textPartConfigurationFile = new File(tmpFolder, layoutName + ReportingConstants.TEXT_PART_CONFIGURATION_FILE_SUFFIX);
			try {
				textPartConfigurationFile.createNewFile();

				//Create document
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document reportTextPartDoc = docBuilder.newDocument();

				/*****reportTextPartConfiguration-node*****/
				Element reportTextPartConfigurationNode = reportTextPartDoc.createElement(ReportingConstants.REPORT_TEXT_PART_CONFIGURATION_ELEMENT);
				reportTextPartDoc.appendChild(reportTextPartConfigurationNode);

				for (ReportTextPart reportTextPart : reportTextPartConfiguration.getReportTextParts()) {
					//reportTextPart
					Element reportTextPartNode = reportTextPartDoc.createElement(ReportingConstants.REPORT_TEXT_PART_ELEMENT);
					reportTextPartNode.setAttribute(ReportingConstants.REPORT_TEXT_PART_ELEMENT_ATTRIBUTE_ID, reportTextPart.getReportTextPartID());
					reportTextPartNode.setAttribute(ReportingConstants.REPORT_TEXT_PART_ELEMENT_ATTRIBUTE_TYPE, reportTextPart.getType().toString());
					reportTextPartConfigurationNode.appendChild(reportTextPartNode);

					//name
					generateI18nElements(reportTextPartDoc, reportTextPartNode, ReportingConstants.REPORT_TEXT_PART_NAME_ELEMENT, reportTextPart.getName());

					//content
					generateI18nElements(reportTextPartDoc, reportTextPartNode, ReportingConstants.REPORT_TEXT_PART_CONTENT_ELEMENT, reportTextPart.getContent());
				}

				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, ReportingConstants.DESCRIPTOR_FILE_ENCODING);
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, ReportingConstants.DESCRIPTOR_FILE_DOCTYPE_SYSTEM);
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, ReportingConstants.DESCRIPTOR_FILE_DOCTYPE_PUBLIC); 

				//Write the XML document to a file
				//initialize StreamResult with File object to save to file
				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(reportTextPartDoc);
				transformer.transform(source, result);

				String xmlString = result.getWriter().toString();

				IOUtil.writeTextFile(textPartConfigurationFile, xmlString);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		//----------End------------------

		return true;
	}

	private void generateI18nElements(Document document, Element parentElement, String elementName, I18nText i18nText) {
		for (Entry<String, String> entry : i18nText.getTexts()) {
			Element element = document.createElement(elementName);
			element.setAttribute("language", entry.getKey());
			element.setTextContent(entry.getValue());

			parentElement.appendChild(element);
		}
	}
}