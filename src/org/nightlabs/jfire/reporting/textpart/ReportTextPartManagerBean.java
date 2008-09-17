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
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
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
	private static final long serialVersionUID = 20080916L;

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

}
