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

import org.nightlabs.inheritance.NotWritableException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductTypeLocalFieldMetaDataID"
 *		detachable="true"
 *		table="JFireTrade_ProductTypeLocalFieldMetaData"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeID, fieldName"
 *
 * @jdo.fetch-group name="ProductTypeLocal.fieldMetaDataMap" fields="productTypeLocal"
 */
public class ProductTypeLocalFieldMetaData
implements org.nightlabs.inheritance.FieldMetaData, Serializable
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
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String fieldName;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductTypeLocal productTypeLocal;

	/**
	 * Whether or not the field may be changed by children.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private byte writableByChildren = ProductTypeLocalFieldMetaData.WRITABLEBYCHILDREN_YES;

	/**
	 * writable is set to false if the mother has writableByChildren
	 * set to false.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean writable = true;

	/**
	 * If true, the value of the child is automatically updated if the
	 * mother's field is changed.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean valueInherited = true;

	protected ProductTypeLocalFieldMetaData() { }
	public ProductTypeLocalFieldMetaData(ProductTypeLocal productTypeLocal, String fieldName)
	{
		setProductTypeLocal(productTypeLocal);
		setFieldName(fieldName);
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
	public ProductTypeLocal getProductTypeLocal()
	{
		return productTypeLocal;
	}
	/**
	 * @param product The product to set.
	 */
	protected void setProductTypeLocal(ProductTypeLocal productTypeLocal)
	{
		if (productTypeLocal == null)
			throw new NullPointerException("productType must not be null!");
		if (productTypeLocal.getOrganisationID() == null)
			throw new NullPointerException("productType.organisationID must not be null!");
		if (productTypeLocal.getProductTypeID() == null)
			throw new NullPointerException("productType.productTypeID must not be null!");
		this.organisationID = productTypeLocal.getOrganisationID();
		this.productTypeID = productTypeLocal.getProductTypeID();
		this.productTypeLocal = productTypeLocal;
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
		if (!writable && !valueInherited)
			throw new IllegalStateException("The field is not writable, thus the value must be inherited. Cannot set valueInherited to false!");

		this.valueInherited = valueInherited;
	}

}
