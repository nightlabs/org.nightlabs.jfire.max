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

import org.apache.log4j.Logger;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.NotWritableException;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.util.Util;

import org.nightlabs.jfire.store.id.ProductTypeFieldMetaDataID;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductTypeFieldMetaDataID"
 *		detachable="true"
 *		table="JFireTrade_ProductTypeFieldMetaData"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeID, fieldName"
 *
 * @jdo.fetch-group name="ProductType.fieldMetaDataMap" fields="productType" fetch-groups="default"
 */
@PersistenceCapable(
	objectIdClass=ProductTypeFieldMetaDataID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_ProductTypeFieldMetaData")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="ProductType.fieldMetaDataMap",
		members=@Persistent(name="productType"))
)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ProductTypeFieldMetaData
implements org.nightlabs.inheritance.FieldMetaData, Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ProductTypeFieldMetaData.class);

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String productTypeID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String fieldName;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProductType productType;

	/**
	 * Whether or not the field may be changed by children.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private byte writableByChildren = FieldMetaData.WRITABLEBYCHILDREN_YES;

	/**
	 * writable is set to false if the mother has writableByChildren
	 * set to false.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean writable = true;

	/**
	 * If true, the value of the child is automatically updated if the
	 * mother's field is changed.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean valueInherited = true;

	protected ProductTypeFieldMetaData() {
		if (logger.isTraceEnabled()) {
//			logger.trace("[" + ObjectIDUtil.intObjectIDFieldToString(System.identityHashCode(this)) + "] <init>()", new Exception("STACKTRACE"));
			logger.trace("[" + ObjectIDUtil.intObjectIDFieldToString(System.identityHashCode(this)) + "] <init>()");
		}
	}

	public ProductTypeFieldMetaData(ProductType productType, String fieldName)
	{
		setProductType(productType);
		setFieldName(fieldName);
		if (logger.isTraceEnabled()) {
//			logger.trace("[" + ObjectIDUtil.intObjectIDFieldToString(System.identityHashCode(this)) + "] <init>(ProductType, String): productType.pk="+productType.getPrimaryKey()+" fieldName=" + fieldName, new Exception("STACKTRACE"));
			logger.trace("[" + ObjectIDUtil.intObjectIDFieldToString(System.identityHashCode(this)) + "] <init>(ProductType, String): productType.pk="+productType.getPrimaryKey()+" fieldName=" + fieldName);
		}
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
	 * @return Returns the fieldName.
	 */
	public String getFieldName()
	{
		return fieldName;
	}
	/**
	 * @param fieldName The fieldName to set.
	 */
	protected void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}

	/**
	 * @return Returns the product.
	 */
	public ProductType getProductType()
	{
		return productType;
	}
	/**
	 * @param product The product to set.
	 */
	protected void setProductType(ProductType productType)
	{
		if (productType == null)
			throw new NullPointerException("productType must not be null!");
		if (productType.getOrganisationID() == null)
			throw new NullPointerException("productType.organisationID must not be null!");
		if (productType.getProductTypeID() == null)
			throw new NullPointerException("productType.productTypeID must not be null!");
		this.organisationID = productType.getOrganisationID();
		this.productTypeID = productType.getProductTypeID();
		this.productType = productType;
	}

	/**
	 * @see org.nightlabs.inheritance.ProductInfoFieldMetaData#getWritableByChildren()
	 */
	public byte getWritableByChildren()
	{
		return writableByChildren;
	}
	/**
	 * @see org.nightlabs.inheritance.ProductInfoFieldMetaData#setWritableByChildren(byte)
	 */
	public void setWritableByChildren(byte writableByChildren)
	{
		this.writableByChildren = writableByChildren;
	}

	/**
	 * @return Returns the writable.
	 */
	public boolean isWritable()
	{
		return writable;
	}
	/**
	 * @param writable The writable to set.
	 */
	public void setWritable(boolean writable)
	{
		this.writable = writable;
	}
	/**
	 * @see org.nightlabs.inheritance.ProductInfoFieldMetaData#assertWritable()
	 */
	public void assertWritable() throws NotWritableException
	{
		if (!isWritable())
			throw new NotWritableException("Field \""+getFieldName()+"\" is not writeable!");
	}

	/**
	 * @return Returns the valueInherited.
	 */
	public boolean isValueInherited()
	{
		return valueInherited;
	}
	/**
	 * @param valueInherited The valueInherited to set.
	 */
	public void setValueInherited(boolean valueInherited)
	{
		if (this.valueInherited == valueInherited)
			return;

		if (!writable && !valueInherited)
			throw new IllegalStateException("The field is not writable, thus the value must be inherited. Cannot set valueInherited to false!");

		if (logger.isTraceEnabled()) {
			logger.trace("[" + ObjectIDUtil.intObjectIDFieldToString(System.identityHashCode(this)) + "] setValueInherited: "+valueInherited+" for field "+fieldName+" and productType "+productType.getPrimaryKey());
		}

		this.valueInherited = valueInherited;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Util.hashCode(organisationID);
		result = prime * result + Util.hashCode(productTypeID);
		result = prime * result + Util.hashCode(fieldName);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final ProductTypeFieldMetaData o = (ProductTypeFieldMetaData) obj;
		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.productTypeID, o.productTypeID) &&
				Util.equals(this.fieldName, o.fieldName);
	}

}
