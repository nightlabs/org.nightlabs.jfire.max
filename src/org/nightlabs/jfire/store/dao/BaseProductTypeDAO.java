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

package org.nightlabs.jfire.store.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerUtil;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * TODO move this class to the server
 */
public class BaseProductTypeDAO
extends BaseJDOObjectDAO<ProductTypeID, ProductType>  //JDOObjectDAO<ProductTypeID, ProductType>
{
	/**
	 * This method returns a new instance of <tt>Set</tt> with those
	 * fetch groups that should always be used as a minimum.
	 * <p>
	 * Overwrite this method if you want to return more fetchgroups.
	 *
	 * @return Returns a <tt>Set</tt> with <tt>FetchPlan.DEFAULT</tt> and <tt>FetchPlan.VALUES</tt>.
	 *
	 * @see #getProductType(ProductTypeID, String[])
	 */
	protected Set<String> getMinimumFetchGroups()
	{
		Set<String> fgSet = new HashSet<String>();
		fgSet.add(FetchPlan.DEFAULT);
		return fgSet;
	}

	/**
	 * @param productTypeID
	 * @param The fetchGroups returned by {@link #getMinimumFetchGroups()} are always
	 *		included. You only need to specify additional fetchgroups or leave
	 *		this <tt>null</tt>.
	 * @return An instance of <tt>ProductType</tt> either from the <tt>Cache</tt> or
	 *		from the server.
	 */
	public ProductType getProductType(ProductTypeID productTypeID, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor progressMonitor)
	{
		Set<String> fgSet = getMinimumFetchGroups();

		if (fetchGroups == null)
			fetchGroups = fgSet.toArray(new String[fgSet.size()]);
		else {
			fgSet.addAll(Arrays.asList(fetchGroups));
			fetchGroups = fgSet.toArray(new String[fgSet.size()]);
		}

		return getJDOObject(null, productTypeID, fetchGroups, maxFetchDepth, progressMonitor);
	}

	// TODO: Implement Authority checking (needs to be in the EJB!)
	@Override
	protected Collection<ProductType> retrieveJDOObjects(Set<ProductTypeID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor progressMonitor)
	throws Exception
	{
		progressMonitor.beginTask("Loading ProductTypes", 2); //$NON-NLS-1$
		progressMonitor.worked(1);
		StoreManager sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		Collection<ProductType> productTypes = sm.getProductTypes(objectIDs, fetchGroups, maxFetchDepth);
		progressMonitor.worked(2);
		return productTypes;
	}
}
