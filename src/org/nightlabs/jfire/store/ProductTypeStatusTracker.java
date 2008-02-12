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

package org.nightlabs.jfire.store;

import java.io.Serializable;

import org.nightlabs.jfire.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductTypeStatusTrackerID"
 *		detachable="true"
 *		table="JFireTrade_ProductTypeStatusTracker"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeID"
 */
public class ProductTypeStatusTracker
implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String productTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductType productType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductTypeStatus currentStatus;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int nextStatusID = 0;

	protected ProductTypeStatusTracker()
	{
	}

	public ProductTypeStatusTracker(ProductType productType, User user)
	{
		this.productType = productType;
		this.organisationID = productType.getOrganisationID();
		this.productTypeID = productType.getProductTypeID();

		newCurrentStatus(user);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the productTypeID.
	 */
	public String getProductTypeID()
	{
		return productTypeID;
	}
	/**
	 * @return Returns the productType.
	 */
	public ProductType getProductType()
	{
		return productType;
	}

	public void newCurrentStatus(User user)
	{
		currentStatus = new ProductTypeStatus(this, createStatusID(), user);
	}

	public ProductTypeStatus getCurrentStatus()
	{
		return currentStatus;
	}

	protected synchronized int createStatusID()
	{
		int res = nextStatusID;
		nextStatusID = res + 1;
		return res;
	}
}
