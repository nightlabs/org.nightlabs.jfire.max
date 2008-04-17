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

package org.nightlabs.jfire.voucher.store;

import java.util.Map;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.store.ProductTypeSearchFilter;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * @author Marco Schulze - Marco at NightLabs dot de
 */
public class VoucherTypeSearchFilter
	extends ProductTypeSearchFilter
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param conjunction
	 */
	public VoucherTypeSearchFilter(int conjunction) {
		super(conjunction);
	}

	@Override
	@Implement
	protected void prepareQuery(Set<Class<?>> imports, StringBuffer vars,
			StringBuffer filter, StringBuffer params, Map<String, Object> paramMap,
			StringBuffer result) {
	
		
		filter.append("this.published && this.saleable");
	
	
		AnchorID vendorID = getVendorID();
		if (vendorID != null) {

			filter.append(" && this.vendor.organisationID == :vendorOrganisationID");
			filter.append(" && this.vendor.anchorTypeID == :vendorAnchorTypeID");
			filter.append(" && this.vendor.anchorID == :vendorAnchorID");
			paramMap.put("vendorOrganisationID", vendorID.organisationID);
			paramMap.put("vendorAnchorTypeID", vendorID.anchorTypeID);
			paramMap.put("vendorAnchorID", vendorID.anchorID);
		}
	
	
	
	
	
	
	}

	
	@Override
	protected Class<?> initCandidateClass() {
		return VoucherType.class;
	}
	
	

}
