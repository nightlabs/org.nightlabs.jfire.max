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

import org.nightlabs.jfire.store.id.ProductTypeID;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.store.id.NestedProductTypeID;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

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
 *		table="JFireTrade_NestedProductTypeLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="packageProductTypeOrganisationID, packageProductTypeProductTypeID, innerProductTypeOrganisationID, innerProductTypeProductTypeID"
 *
 * @jdo.fetch-group name="NestedProductTypeLocal.packageProductTypeLocal" fields="packageProductTypeLocal"
 * @jdo.fetch-group name="NestedProductTypeLocal.innerProductTypeLocal" fields="innerProductTypeLocal"
 * @jdo.fetch-group name="NestedProductTypeLocal.this" fetch-groups="default" fields="packageProductTypeLocal, innerProductTypeLocal"
 *
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default"
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default"
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default"
 * @!jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default"
 */
@PersistenceCapable(
	objectIdClass=NestedProductTypeID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_NestedProductTypeLocal")
@FetchGroups({
	@FetchGroup(
		name=NestedProductTypeLocal.FETCH_GROUP_PACKAGE_PRODUCT_TYPE,
		members=@Persistent(name="packageProductTypeLocal")),
	@FetchGroup(
		name=NestedProductTypeLocal.FETCH_GROUP_INNER_PRODUCT_TYPE,
		members=@Persistent(name="innerProductTypeLocal")),
	@FetchGroup(
		fetchGroups={"default"},
		name=NestedProductTypeLocal.FETCH_GROUP_THIS_PACKAGED_PRODUCT_TYPE,
		members={@Persistent(name="packageProductTypeLocal"), @Persistent(name="innerProductTypeLocal")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class NestedProductTypeLocal
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_PACKAGE_PRODUCT_TYPE = "NestedProductTypeLocal.packageProductTypeLocal";
	public static final String FETCH_GROUP_INNER_PRODUCT_TYPE = "NestedProductTypeLocal.innerProductTypeLocal";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_PACKAGED_PRODUCT_TYPE = "NestedProductTypeLocal.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String packageProductTypeOrganisationID = null;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String packageProductTypeProductTypeID = null;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String innerProductTypeOrganisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String innerProductTypeProductTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String innerProductTypePrimaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProductTypeLocal packageProductTypeLocal;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProductTypeLocal innerProductTypeLocal;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int quantity = 1;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient ProductTypeID packageProductTypeID;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient ProductTypeID innerProductTypeID;

	/**
	 * @deprecated This constructor exists only for JDO.
	 */
	@Deprecated
	protected NestedProductTypeLocal()
	{
	}

	/**
	 * @param packageProductTypeLocal Might be <tt>null</tt> for virtual toplevel-pointer.
	 * @param innerProductTypeLocal
	 * @param quantity
	 */
	public NestedProductTypeLocal(ProductTypeLocal packageProductTypeLocal, ProductTypeLocal innerProductTypeLocal, int quantity)
	{
		this(packageProductTypeLocal, innerProductTypeLocal);
		this.quantity = quantity;
	}
	public NestedProductTypeLocal(ProductTypeLocal packageProductTypeLocal, ProductTypeLocal innerProductTypeLocal)
	{
		this.packageProductTypeLocal = packageProductTypeLocal;
		this.innerProductTypeLocal = innerProductTypeLocal;
		if (packageProductTypeLocal != null) {
			this.packageProductTypeOrganisationID = packageProductTypeLocal.getOrganisationID();
			this.packageProductTypeProductTypeID = packageProductTypeLocal.getProductTypeID();
		}
		this.innerProductTypeOrganisationID = innerProductTypeLocal.getOrganisationID();
		this.innerProductTypeProductTypeID = innerProductTypeLocal.getProductTypeID();
		this.innerProductTypePrimaryKey = ProductType.getPrimaryKey(this.innerProductTypeOrganisationID, this.innerProductTypeProductTypeID);
	}

	/**
	 * @return Returns the innerProductTypeLocal.
	 */
	public ProductTypeLocal getInnerProductTypeLocal()
	{
		return innerProductTypeLocal;
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
	 * @return Returns the packageProductTypeLocal.
	 */
	public ProductTypeLocal getPackageProductTypeLocal()
	{
		return packageProductTypeLocal;
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

	public ProductTypeID getPackageProductTypeID()
	{
		if (packageProductTypeID == null)
			packageProductTypeID = ProductTypeID.create(packageProductTypeOrganisationID, packageProductTypeProductTypeID);

		return packageProductTypeID;
	}
	public ProductTypeID getInnerProductTypeID()
	{
		if (innerProductTypeID == null)
			innerProductTypeID = ProductTypeID.create(innerProductTypeOrganisationID, innerProductTypeProductTypeID);

		return innerProductTypeID;
	}
}
