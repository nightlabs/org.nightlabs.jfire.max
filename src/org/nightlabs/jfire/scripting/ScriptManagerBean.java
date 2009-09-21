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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.scripting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.scripting.id.ScriptParameterID;
import org.nightlabs.jfire.scripting.id.ScriptParameterSetID;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @ejb.bean name="jfire/ejb/JFireScripting/ScriptManager"
 *					 jndi-name="jfire/ejb/JFireScripting/ScriptManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class ScriptManagerBean
extends BaseSessionBeanImpl
implements ScriptManagerRemote
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ScriptManagerBean.class);

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() throws Exception
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireScriptingEAR.MODULE_NAME);
			if (moduleMetaData == null) {
				logger.info("Initialization of JFireScripting started ...");

				// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
//				Version version = new Version(0, 9, 5, 0, "beta");
//				moduleMetaData = new ModuleMetaData(JFireScriptingEAR.MODULE_NAME, version, version);
				moduleMetaData = pm.makePersistent(
						ModuleMetaData.createModuleMetaDataFromManifest(JFireScriptingEAR.MODULE_NAME, JFireScriptingEAR.class)
				);
				logger.info("Persisted ModuleMetaData for JFireScripting.");
			}
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#getScriptRegistryItem(org.nightlabs.jfire.scripting.id.ScriptRegistryItemID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ScriptRegistryItem getScriptRegistryItem (
			ScriptRegistryItemID scriptRegistryItemID,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			ScriptRegistryItem reportRegistryItem = (ScriptRegistryItem)pm.getObjectById(scriptRegistryItemID);
			ScriptRegistryItem result = pm.detachCopy(reportRegistryItem);
			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#getScriptRegistryItems(java.util.List, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public List<ScriptRegistryItem> getScriptRegistryItems (
			List<ScriptRegistryItemID> scriptRegistryItemIDs,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			List<ScriptRegistryItem> result = new ArrayList<ScriptRegistryItem>();
			for (ScriptRegistryItemID itemID : scriptRegistryItemIDs) {
				ScriptRegistryItem item = (ScriptRegistryItem)pm.getObjectById(itemID);
				result.add(pm.detachCopy(item));
			}

			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#getTopLevelScriptRegistryItems(java.lang.String, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ScriptRegistryItem> getTopLevelScriptRegistryItems (
			String organisationID,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			Collection<ScriptRegistryItem> topLevelItems = ScriptRegistryItem.getTopScriptRegistryItemsByOrganisationID(pm, organisationID);
			Collection<ScriptRegistryItem> result = pm.detachCopyAll(topLevelItems);
			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#getTopLevelScriptRegistryItemCarriers(java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ScriptRegistryItemCarrier> getTopLevelScriptRegistryItemCarriers (String organisationID)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			Collection<ScriptRegistryItem> topLevelItems = ScriptRegistryItem.getTopScriptRegistryItemsByOrganisationID(pm, organisationID);
			Collection<ScriptRegistryItemCarrier> result = new HashSet<ScriptRegistryItemCarrier>();
			for (Iterator<ScriptRegistryItem> iter = topLevelItems.iterator(); iter.hasNext();) {
				ScriptRegistryItem item = iter.next();
				result.add(new ScriptRegistryItemCarrier(null, item, true));
			}
			return result;
		} finally {
			pm.close();
		}
	}

//	/**
//	 *
//	 * @param topLevelIDs a Set of {@link ScriptRegistryItemID}s which {@link ScriptRegistryItemCarrier} you want to get for
//	 * @throws ModuleException
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 * @ejb.transaction type="Required"
//	 */
//	public Collection<ScriptRegistryItemCarrier> getTopLevelScriptRegistryItemCarriers (Set<ScriptRegistryItemID> topLevelIDs)
//	throws ModuleException
//	{
//		PersistenceManager pm;
//		pm = getPersistenceManager();
//		try {
////			Collection topLevelItems = ScriptRegistryItem.getTopScriptRegistryItemsByOrganisationID(pm, organisationID);
//			Collection topLevelItems = pm.getObjectsById(topLevelIDs);
//			Collection<ScriptRegistryItemCarrier> result = new HashSet<ScriptRegistryItemCarrier>();
//			for (Iterator iter = topLevelItems.iterator(); iter.hasNext();) {
//				ScriptRegistryItem item = (ScriptRegistryItem) iter.next();
//				result.add(new ScriptRegistryItemCarrier(null, item, true));
//			}
//			return result;
//		} finally {
//			pm.close();
//		}
//	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#storeRegistryItem(org.nightlabs.jfire.scripting.ScriptRegistryItem, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ScriptRegistryItem storeRegistryItem (
			ScriptRegistryItem reportRegistryItem,
			boolean get,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, reportRegistryItem, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#getScriptParameterSets(java.lang.String, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ScriptParameterSet> getScriptParameterSets(
			String organisationID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			Collection<ScriptParameterSet> paramSets = ScriptParameterSet.getParameterSetsByOrganisation(pm, organisationID);
			Collection<ScriptParameterSet> pSets = pm.detachCopyAll(paramSets);
			Collection<ScriptParameterSet> result = new HashSet<ScriptParameterSet>();
			for (Iterator<ScriptParameterSet> iter = pSets.iterator(); iter.hasNext();) {
				ScriptParameterSet paramSet = iter.next();
				result.add(paramSet);
			}
			return result;
		} finally {
			pm.close();
		}
	}
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public List<ScriptParameter> getScriptParameters(Collection<ScriptParameterID> scriptParameterIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, scriptParameterIDs,ScriptParameter.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#getAllScriptParameterSetIDs(java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Set<ScriptParameterSetID> getAllScriptParameterSetIDs(String organisationID)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			Collection<ScriptParameterSet> paramSets = ScriptParameterSet.getParameterSetsByOrganisation(pm, organisationID);
			return NLJDOHelper.getObjectIDSet(paramSets);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#getScriptParameterSet(org.nightlabs.jfire.scripting.id.ScriptParameterSetID, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ScriptParameterSet getScriptParameterSet(
			ScriptParameterSetID scriptParameterSetID,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			ScriptParameterSet parameterSet = (ScriptParameterSet)pm.getObjectById(scriptParameterSetID);
			return pm.detachCopy(parameterSet);
		} finally {
			pm.close();
		}
	}
	
	
	

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#getScriptParameterSetsForScriptRegistryItemIDs(java.util.Collection, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ScriptParameterSet> getScriptParameterSetsForScriptRegistryItemIDs(
			Collection<ScriptRegistryItemID> scriptParameterSetID,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			Collection<ScriptParameterSet> parameterSets = new HashSet<ScriptParameterSet>();
			for (ScriptRegistryItemID itemID : scriptParameterSetID) {
				ScriptRegistryItem item = (ScriptRegistryItem) pm.getObjectById(itemID);
				if (item.getParameterSet() != null)
					parameterSets.add(item.getParameterSet());
			}
			return pm.detachCopyAll(parameterSets);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#getScriptParameterSets(java.util.Collection, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ScriptParameterSet> getScriptParameterSets(
			Collection<ScriptParameterSetID> scriptParameterSetIDs,
			String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			return NLJDOHelper.getDetachedObjectSet(pm, scriptParameterSetIDs, ScriptParameterSet.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#createParameterSet(org.nightlabs.i18n.I18nText, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ScriptParameterSet createParameterSet (I18nText name, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			ScriptRegistry.getScriptRegistry(pm);
			long setID = IDGenerator.nextID(ScriptParameterSet.class);
			ScriptParameterSet set = new ScriptParameterSet(getOrganisationID(), setID);
			set.getName().copyFrom(name);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);
			pm.makePersistent(set);
			return pm.detachCopy(set);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#storeParameterSet(org.nightlabs.jfire.scripting.ScriptParameterSet, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ScriptParameterSet storeParameterSet (
			ScriptParameterSet scriptParameterSet,
			boolean get,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, scriptParameterSet, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	
	public IScriptParameter storeParameter(
			IScriptParameter scriptParameter,
			boolean get,
			String[] fetchGroups, int maxFetchDepth
		)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try{
			return NLJDOHelper.storeJDO(pm, scriptParameter, get, fetchGroups, maxFetchDepth);
		}finally{
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#getTopLevelScriptRegistryItemCarriers(java.lang.String, java.lang.String)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public Collection<ScriptRegistryItemCarrier> getTopLevelScriptRegistryItemCarriers (
			String organisationID, String scriptRegistryItemType)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			Collection<ScriptRegistryItem> topLevelItems = ScriptRegistryItem.getTopScriptRegistryItemsByOrganisationIDAndType(
					pm, organisationID, scriptRegistryItemType);
			Collection<ScriptRegistryItemCarrier> result = new HashSet<ScriptRegistryItemCarrier>();
			for (Iterator<ScriptRegistryItem> iter = topLevelItems.iterator(); iter.hasNext();) {
				ScriptRegistryItem item = iter.next();
				result.add(new ScriptRegistryItemCarrier(null, item, true));
			}
			return result;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#getScriptRegistry(java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ScriptRegistry getScriptRegistry(String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setGroups(fetchGroups);
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			ScriptRegistry scriptRegistry = (ScriptRegistry) pm.getObjectById(ScriptRegistry.SINGLETON_ID);
			scriptRegistry = pm.detachCopy(scriptRegistry);
			return scriptRegistry;
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.scripting.ScriptManagerRemote#getScriptRegistry()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public ScriptRegistry getScriptRegistry()
	{
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(ScriptRegistry.FETCH_GROUP_THIS_SCRIPT_REGISTRY);
			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT); // TODO really? Marco.
			ScriptRegistry scriptRegistry = (ScriptRegistry) pm.getObjectById(ScriptRegistry.SINGLETON_ID);
			scriptRegistry = pm.detachCopy(scriptRegistry);
			return scriptRegistry;
		} finally {
			pm.close();
		}
	}
	
	@RolesAllowed("_Guest_")
	@Override
	public Collection<String> getLanguages()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			ScriptRegistry scriptRegistry = ScriptRegistry.getScriptRegistry(pm);
			Collection<String> languages = scriptRegistry.getRegisteredLanguages();
			return new ArrayList<String>(languages);
		} finally {
			pm.close();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
