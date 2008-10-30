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

package org.nightlabs.jfire.reporting.textpart;

import java.rmi.RemoteException;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.textpart.id.ReportTextPartConfigurationID;

/**
 * Manager giving access to {@link ReportTextPartConfiguration}s and their {@link ReportTextPart}s. 
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @ejb.bean name="jfire/ejb/JFireReporting/ReportTextPartManager"
 *					 jndi-name="jfire/ejb/JFireReporting/ReportTextPartManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 */
public abstract class ReportTextPartManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	/**
	 *
	 */
	private static final long serialVersionUID = 20080927L;

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
	 * This method returns the id of the {@link ReportTextPartConfiguration} linked to 
	 * the {@link ReportRegistryItem} referenced by the given reportRegistryItemID.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
	 * @ejb.transaction type="Supports"
	 */
	public ReportTextPartConfiguration getReportTextPartConfiguration(
			ReportRegistryItemID reportRegistryItemID,
			boolean synthesize, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ReportRegistryItem reportRegistryItem = (ReportRegistryItem) pm.getObjectById(reportRegistryItemID);
			ReportTextPartConfiguration configuration = ReportTextPartConfiguration.getReportTextPartConfiguration(
					pm, reportRegistryItem, synthesize, fetchGroups, maxFetchDepth);
			if (configuration == null) {
				return null;
			} else if (JDOHelper.getPersistenceManager(configuration) != null) {
				pm.getFetchPlan().setGroups(fetchGroups);
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				return pm.detachCopy(configuration);
			} else if (configuration.isSynthetic()) {
				return configuration;
			} else
				throw new IllegalStateException("ReportTextPartConfiguration.getReportTextPartConfiguration() returned not-attached and not-synthesized ReportTextPartConfiguration");
		} finally {
			pm.close();
		}
	}

	/**
	 * This method returns the id of the {@link ReportTextPartConfiguration} linked to 
	 * the {@link ReportRegistryItem} referenced by the given reportRegistryItemID.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
	 * @ejb.transaction type="Supports"
	 */
	public ReportTextPartConfigurationID getReportTextPartConfigurationID(ReportRegistryItemID reportRegistryItemID) {
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ReportRegistryItem reportRegistryItem = (ReportRegistryItem) pm.getObjectById(reportRegistryItemID);
			ReportTextPartConfiguration config = ReportTextPartConfiguration.getReportTextPartConfiguration(pm, reportRegistryItem);
			if (config == null)
				return null;
			return (ReportTextPartConfigurationID) JDOHelper.getObjectId(config);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
	 * @ejb.transaction type="Supports"
	 */
	public Set<ReportTextPartConfiguration> getReportTextPartConfigurations(
			Set<ReportTextPartConfigurationID> configurationIDs, String[] fetchGroups, int maxFetchDepth
	) {
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(pm, configurationIDs, ReportTextPartConfiguration.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * Searches the {@link ReportTextPartConfiguration} for the given linkedObjectID and reportRegistryItemID. 
	 * If none can be found in the data-store this method will search for a {@link ReportTextPartConfiguration} 
	 * linked to one of the parent {@link ReportCategory}s of the given reportRegistryItemID.
	 * <p>
	 * Additionally this method can be used to get a synthetic, new {@link ReportTextPartConfiguration}
	 * linked to the given linkedObjectID. A synthetic {@link ReportTextPartConfiguration} will have the
	 * values of that one found when searching for the given reportRegistryItem. The synthetic configuration 
	 * will not be persisted and only the parts of it referencing existing/persisted objects will be detached.
	 * A synthetic {@link ReportTextPartConfiguration} returned by this methot will therefore not be a
	 * detached object itself, it is rather a newly created object. 
	 * </p>
	 * 
	 * @param linkedObjectID 
	 * 			The {@link ObjectID} a linked {@link ReportTextPartConfiguration} should be found for.
	 * @param reportRegistryItemID 
	 * 			The {@link ReportRegistryItemID} to start the search for a {@link ReportTextPartConfiguration}
	 * 			that is linked to a {@link ReportRegistryItem} should be started from.
	 * @param synthesize 
	 * 			Whether to synthesize a new {@link ReportTextPartConfiguration} when none directly linked to the
	 * 			given linkedObjectID was found but one was found linked to a reportRegistryItem.
	 * @param fetchGroups
	 * 			The fetch-groups to detach the found {@link ReportTextPartConfiguration} with. Note, that this 
	 * 			fetch-groups will also be used used when synthesizing a new configuration, but then to detach those 
	 * 			parts of the the found configuration that reference already persisted object.                             
	 * @param maxFetchDepth
	 * 			The maximum fetch-depth to detach the found {@link ReportTextPartConfiguration} with. Note, that this 
	 * 			fetch-depth will also be used used when synthesizing a new configuration, but then to detach those 
	 * 			parts of the the found configuration that reference already persisted object.         
	 * @return 
	 * 			The {@link ReportTextPartConfiguration} either found (or synthesized) for the given linkedObjectID 
	 * 			or the configuration found for a {@link ReportRegistryItem}. If nothing can be found, <code>null</code> will be returned.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.renderReport"
	 * @ejb.transaction type="Supports"
	 * 
	 */
	public ReportTextPartConfiguration getReportTextPartConfiguration(
			ReportRegistryItemID reportRegistryItemID, ObjectID linkedObjectID, 
			boolean synthesize, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = getPersistenceManager();
		try {
			ReportTextPartConfiguration configuration = ReportTextPartConfiguration.getReportTextPartConfiguration(
					pm, reportRegistryItemID, linkedObjectID, synthesize, fetchGroups, maxFetchDepth);
			
			if (configuration == null)
				return null;
			
			if (JDOHelper.getPersistenceManager(configuration) != null) {
				// TODO: How to check better if the config is attached? JDOHelper.isPersistent() ?!?
				pm.getFetchPlan().setGroups(fetchGroups);
				pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
				return pm.detachCopy(configuration);
			} else if (configuration.isSynthetic())
				return configuration;
			else
				throw new IllegalStateException("ReportTextPartConfiguration.getReportTextPartConfiguration() returned not-attached and not-synthesized ReportTextPartConfiguration");
		} finally {
			pm.close();
		}
	}

	/**
	 * This method stores the given {@link ReportTextPartConfiguration} if it is 
	 * a configuration linked to an object in the datastore.
	 * 
	 * @param reportTextPartConfiguration 
	 * 			The configuration to store.
	 * @param get 
	 * 			Wheter a detached copy of the stored item should be returned.
	 * @param fetchGroups 
	 * 			If get is <code>true</code>, this defines the fetch-groups the
	 * 			retuned item will be detached with.
	 * @param maxFetchDepth 
	 * 			If get is <code>true</code>, this defines the maximum fetch-depth
	 * 			when detaching.
	 * @return
	 * 			If get is <code>true</code> the detached {@link ReportTextPartConfiguration}
	 * 			is returned, <code>null</code> otherwise.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.renderReport"
	 * @ejb.transaction type="Required"
	 */
	public ReportTextPartConfiguration storeLinkedObjectReportTextPartConfiguration(
			ReportTextPartConfiguration reportTextPartConfiguration, 
			boolean get, String[] fetchGroups, int maxFetchDepth) {
		
		PersistenceManager pm = getPersistenceManager(); 
		try {
			if (reportTextPartConfiguration.getLinkedObjectID() == null) {
				throw new IllegalStateException("This method can't store a ReportTextPartConfiguration that is not linked to an object (linkedObjectID == null).");
			}
			return NLJDOHelper.storeJDO(pm, reportTextPartConfiguration, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * This method stores the given {@link ReportTextPartConfiguration}.
	 * 
	 * @param reportTextPartConfiguration 
	 * 			The configuration to store.
	 * @param get 
	 * 			Wheter a detached copy of the stored item should be returned.
	 * @param fetchGroups 
	 * 			If get is <code>true</code>, this defines the fetch-groups the
	 * 			retuned item will be detached with.
	 * @param maxFetchDepth 
	 * 			If get is <code>true</code>, this defines the maximum fetch-depth
	 * 			when detaching.
	 * @return
	 * 			If get is <code>true</code> the detached {@link ReportTextPartConfiguration}
	 * 			is returned, <code>null</code> otherwise.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
	 * @ejb.transaction type="Required"
	 */
	public ReportTextPartConfiguration storeReportTextPartConfiguration(
			ReportTextPartConfiguration reportTextPartConfiguration, 
			boolean get, String[] fetchGroups, int maxFetchDepth) {
		
		PersistenceManager pm = getPersistenceManager(); 
		try {
			return NLJDOHelper.storeJDO(pm, reportTextPartConfiguration, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
}
