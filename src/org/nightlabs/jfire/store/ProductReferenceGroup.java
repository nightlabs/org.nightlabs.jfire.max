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

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductReferenceGroupID"
 *		detachable="true"
 *		table="JFireTrade_ProductReferenceGroup"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, anchorTypeID, productReferenceGroupID, productOrganisationID, productProductID"
 */
public class ProductReferenceGroup
implements Serializable
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String anchorTypeID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="201"
	 */
	private String productReferenceGroupID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String productOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private long productProductID;

	/**
	 * This must be in the range -1 &lt; quantity &lt; 1 at the end of a transaction. During the
	 * transaction, it might be more or less.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int quantity = 0;

	private ProductReference significantProductReference = null;
	
	public ProductReferenceGroup(String organisationID, String anchorTypeID, String productReferenceGroupID, String productOrganisationID, long productProductID)
	{
		this.organisationID = organisationID;
		this.anchorTypeID = anchorTypeID;
		this.productReferenceGroupID = productReferenceGroupID;
		this.productOrganisationID = productOrganisationID;
		this.productProductID = productProductID;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected ProductReferenceGroup() { }

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getAnchorTypeID()
	{
		return anchorTypeID;
	}
	public String getProductReferenceGroupID()
	{
		return productReferenceGroupID;
	}
	public String getProductOrganisationID()
	{
		return productOrganisationID;
	}
	public long getProductProductID()
	{
		return productProductID;
	}

	public String getPrimaryKey()
	{
		return organisationID + '/' + anchorTypeID + '/' + productReferenceGroupID + '/' + productOrganisationID + '/' + Long.toHexString(productProductID);
	}

	public int getQuantity()
	{
		return quantity;
	}
	protected void setQuantity(int quantity)
	{
		this.quantity = quantity;

		if (quantity == 0 && significantProductReference != null)
			significantProductReference = null;
	}

	/**
	 * @return Returns the ProductReference that caused {@link #quantity} to be 1 or -1. Returns <code>null</code>,
	 *		if quantity == 0.
	 */
	public ProductReference getSignificantProductReference()
	{
		return significantProductReference;
	}
	protected void setSignificantProductReference(
			ProductReference significantProductReference)
	{
		this.significantProductReference = significantProductReference;
	}
}
