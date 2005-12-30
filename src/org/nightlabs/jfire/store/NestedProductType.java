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
 * This class is a carrier for packaging a {@link org.nightlabs.jfire.store.ProductType}
 * within another. It adds additional information for the <tt>ProductType</tt> within a
 * specific package - e.g. the quantity.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.NestedProductTypeID"
 *		detachable="true"
 *		table="JFireTrade_NestedProductType"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="packageProductTypeOrganisationID, packageProductTypeProductTypeID, innerProductTypeOrganisationID, innerProductTypeProductTypeID"
 *
 * @jdo.fetch-group name="NestedProductType.packageProductType" fields="packageProductType"
 * @jdo.fetch-group name="NestedProductType.innerProductType" fields="innerProductType"
 * @jdo.fetch-group name="NestedProductType.this" fetch-groups="default" fields="packageProductType, innerProductType"
 *
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default"
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default"
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default"
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default"
 */
public class NestedProductType
implements Serializable
{
	public static final String FETCH_GROUP_PACKAGE_PRODUCT_TYPE = "NestedProductType.packageProductType";
	public static final String FETCH_GROUP_INNER_PRODUCT_TYPE = "NestedProductType.innerProductType";
	public static final String FETCH_GROUP_THIS_PACKAGED_PRODUCT_TYPE = "NestedProductType.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String packageProductTypeOrganisationID = null;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String packageProductTypeProductTypeID = null;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String innerProductTypeOrganisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String innerProductTypeProductTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String innerProductTypePrimaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductType packageProductType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductType innerProductType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int quantity = 1;

	/**
	 * @deprecated This constructor exists only for JDO.
	 */
	protected NestedProductType()
	{
	}

	/**
	 * @param packageProductType Might be <tt>null</tt> for virtual toplevel-pointer.
	 * @param innerProductType
	 * @param quantity
	 */
	public NestedProductType(ProductType packageProductType, ProductType innerProductType, int quantity)
	{
		this(packageProductType, innerProductType);
		this.quantity = quantity;
	}
	public NestedProductType(ProductType packageProductType, ProductType innerProductType)
	{
		this.packageProductType = packageProductType;
		this.innerProductType = innerProductType;
		if (packageProductType != null) {
			this.packageProductTypeOrganisationID = packageProductType.getOrganisationID();
			this.packageProductTypeProductTypeID = packageProductType.getProductTypeID();
		}
		this.innerProductTypeOrganisationID = innerProductType.getOrganisationID();
		this.innerProductTypeProductTypeID = innerProductType.getProductTypeID();
		this.innerProductTypePrimaryKey = innerProductType.getPrimaryKey();
	}

	/**
	 * @return Returns the innerProductType.
	 */
	public ProductType getInnerProductType()
	{
		return innerProductType;
	}
	/**
	 * @return Returns the innerProductTypeOrganisationID.
	 */
	public String getInnerProductTypeOrganisationID()
	{
		return innerProductTypeOrganisationID;
	}
	/**
	 * @return Returns the innerProductTypeProductTypeID.
	 */
	public String getInnerProductTypeProductTypeID()
	{
		return innerProductTypeProductTypeID;
	}
	/**
	 * @return Returns the packageProductType.
	 */
	public ProductType getPackageProductType()
	{
		return packageProductType;
	}
	/**
	 * @return Returns the packageProductTypeOrganisationID.
	 */
	public String getPackageProductTypeOrganisationID()
	{
		return packageProductTypeOrganisationID;
	}
	/**
	 * @return Returns the packageProductTypeProductTypeID.
	 */
	public String getPackageProductTypeProductTypeID()
	{
		return packageProductTypeProductTypeID;
	}
	/**
	 * @return Returns the innerProductTypePrimaryKey.
	 */
	public String getInnerProductTypePrimaryKey()
	{
		return innerProductTypePrimaryKey;
	}
	/**
	 * @return Returns the quantity.
	 */
	public int getQuantity()
	{
		return quantity;
	}
	/**
	 * @param quantity The quantity to set.
	 */
	public void setQuantity(int quantity)
	{
		if (quantity < 1)
			throw new IllegalArgumentException("quantity=" + quantity + " not allowed! Must be >= 1!");

		this.quantity = quantity;
	}
}
