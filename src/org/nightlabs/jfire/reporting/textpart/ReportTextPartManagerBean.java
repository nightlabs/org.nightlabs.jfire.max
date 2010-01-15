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

import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.reporting.RoleConstants;
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
 */@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class ReportTextPartManagerBean
extends BaseSessionBeanImpl
implements ReportTextPartManagerRemote
{
	private static final long serialVersionUID = 20080927L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.textpart.ReportTextPartManagerRemote#getReportTextPartConfiguration(org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID, boolean, java.lang.String[], int)
	 */	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed(RoleConstants.editReport_roleID)
	public ReportTextPartConfiguration getReportTextPartConfiguration(
			ReportRegistryItemID reportRegistryItemID,
			boolean synthesize, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm;
		pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.textpart.ReportTextPartManagerRemote#getReportTextPartConfigurationID(org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID)
	 */	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed(RoleConstants.editReport_roleID)
	public ReportTextPartConfigurationID getReportTextPartConfigurationID(ReportRegistryItemID reportRegistryItemID) {
		PersistenceManager pm;
		pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.textpart.ReportTextPartManagerRemote#getReportTextPartConfigurations(java.util.Set, java.lang.String[], int)
	 */	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed(RoleConstants.editReport_roleID)
	public Set<ReportTextPartConfiguration> getReportTextPartConfigurations(
			Set<ReportTextPartConfigurationID> configurationIDs, String[] fetchGroups, int maxFetchDepth
	) {
		PersistenceManager pm;
		pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(pm, configurationIDs, ReportTextPartConfiguration.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.textpart.ReportTextPartManagerRemote#getReportTextPartConfiguration(org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID, org.nightlabs.jdo.ObjectID, boolean, java.lang.String[], int)
	 */	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed(RoleConstants.renderReport_roleID)
	public ReportTextPartConfiguration getReportTextPartConfiguration(
			ReportRegistryItemID reportRegistryItemID, ObjectID linkedObjectID,
			boolean synthesize, String[] fetchGroups, int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.textpart.ReportTextPartManagerRemote#storeLinkedObjectReportTextPartConfiguration(org.nightlabs.jfire.reporting.textpart.ReportTextPartConfiguration, boolean, java.lang.String[], int)
	 */	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed(RoleConstants.renderReport_roleID)
	public ReportTextPartConfiguration storeLinkedObjectReportTextPartConfiguration(
			ReportTextPartConfiguration reportTextPartConfiguration,
			boolean get, String[] fetchGroups, int maxFetchDepth) {

		PersistenceManager pm = createPersistenceManager();
		try {
			if (reportTextPartConfiguration.getLinkedObjectID() == null) {
				throw new IllegalStateException("This method can't store a ReportTextPartConfiguration that is not linked to an object (linkedObjectID == null).");
			}
			return NLJDOHelper.storeJDO(pm, reportTextPartConfiguration, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.textpart.ReportTextPartManagerRemote#storeReportTextPartConfiguration(org.nightlabs.jfire.reporting.textpart.ReportTextPartConfiguration, boolean, java.lang.String[], int)
	 */	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed(RoleConstants.editReport_roleID)
	public ReportTextPartConfiguration storeReportTextPartConfiguration(
			ReportTextPartConfiguration reportTextPartConfiguration,
			boolean get, String[] fetchGroups, int maxFetchDepth) {

		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, reportTextPartConfiguration, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
}
