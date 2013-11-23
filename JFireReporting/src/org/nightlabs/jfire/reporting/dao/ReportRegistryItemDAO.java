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

package org.nightlabs.jfire.reporting.dao;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.ejb.CreateException;
import javax.jdo.JDOHelper;
import javax.naming.NamingException;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.reporting.ReportManagerRemote;
import org.nightlabs.jfire.reporting.layout.ReportCategory;
import org.nightlabs.jfire.reporting.layout.ReportLayout;
import org.nightlabs.jfire.reporting.layout.ReportRegistryItem;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * DAO object for {@link ReportRegistryItem}s,
 * i.e. {@link ReportCategory}s and {@link ReportLayout}s.
 *
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ReportRegistryItemDAO
extends BaseJDOObjectDAO<ReportRegistryItemID, ReportRegistryItem>
{

	/**
	 *
	 */
	public ReportRegistryItemDAO() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Collection<ReportRegistryItem> retrieveJDOObjects(
			Set<ReportRegistryItemID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		ReportManagerRemote rm = getEjbProvider().getRemoteBean(ReportManagerRemote.class);
		return rm.getReportRegistryItems(new ArrayList<ReportRegistryItemID>(objectIDs), fetchGroups, maxFetchDepth);
	}

	/**
	 * Get the {@link ReportRegistryItem} for the given reportRegistryItemID
	 *
	 * @param reportRegistryItemID The id of the {@link ReportRegistryItem} to fetch.
	 * @param fetchGroups The fetch-groups to detach the item with.
	 * @param monitor The monitor to provide progress.
	 * @return A detached copy of the {@link ReportRegistryItem} with the given id.
	 */
	public ReportRegistryItem getReportRegistryItem(ReportRegistryItemID reportRegistryItemID, String[] fetchGroups, ProgressMonitor monitor) {
		return getJDOObject(null, reportRegistryItemID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	public Collection<ReportRegistryItem> getReportRegistryItems(Set<ReportRegistryItemID> itemIDs, String[] fetchGroups, ProgressMonitor monitor) {
		return getJDOObjects(null, itemIDs, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	private static ReportRegistryItemDAO sharedInstance;

	/**
	 * Returns the static/shared instance of the {@link ReportRegistryItemDAO}
	 * @return The static/shared instance of the {@link ReportRegistryItemDAO}
	 */
	public static ReportRegistryItemDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ReportRegistryItemDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new ReportRegistryItemDAO();
			}
		}
		return sharedInstance;
	}

	/**
	 * Stores the given {@link ReportRegistryItem} on the JFire server.
	 * <p>
	 * If get is true, the newly stored item will be returned. It will
	 * be a copy, detached with the given fetchGroups.
	 * </p>
	 *
	 * @param reportRegistryItem The item to save.
	 * @param get Whether this method should return a detached copy of the newly saved item.
	 * @param fetchGroups If get is <code>true</code> this defines the fetchgroups the returned item will be detached with.
	 * @param maxFetchDepth If get is <code>true</code> this defines the maxFetchDepth set to the fetchplan when the returned item is detached.
	 * @param monitor The monitor this method can report progress to.
	 * @return A detached copy of the newly stored item, or <code>nul</code> when get is <code>false</code>.
	 *
	 * @throws RemoteException
	 * @throws CreateException
	 * @throws NamingException
	 */
	public ReportRegistryItem storeReportRegistryItem(
			ReportRegistryItem reportRegistryItem, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	throws RemoteException, CreateException, NamingException
	{
		ReportManagerRemote rm = getEjbProvider().getRemoteBean(ReportManagerRemote.class);
		if (get) {
			getCache().removeByObjectID(JDOHelper.getObjectId(reportRegistryItem), false);
			ReportRegistryItem item = rm.storeRegistryItem(reportRegistryItem, get, fetchGroups, maxFetchDepth);
			getCache().put(null, item, fetchGroups, maxFetchDepth);
			return item;
		} else
			return rm.storeRegistryItem(reportRegistryItem, get, fetchGroups, maxFetchDepth);
	}
}
