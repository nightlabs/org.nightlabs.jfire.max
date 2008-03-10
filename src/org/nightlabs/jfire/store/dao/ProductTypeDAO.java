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

import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ProductTypeDAO
extends BaseProductTypeDAO
{
	protected ProductTypeDAO() {
		super();
	}
	
	// TODO: Implement authority checking
	public List<ProductType> getProductTypes(Set<ProductTypeID> productTypeIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor progressMonitor)
	{
		return getJDOObjects(null, productTypeIDs, fetchGroups, maxFetchDepth, progressMonitor);
	}

	private static ProductTypeDAO sharedInstance;

	public static ProductTypeDAO sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new ProductTypeDAO();
		return sharedInstance;
	}

}
