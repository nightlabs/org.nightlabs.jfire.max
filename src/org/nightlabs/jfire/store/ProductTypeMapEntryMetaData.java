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

import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.NotWritableException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="datastore"
 *		detachable="true"
 *		table="JFireTrade_MapEntryMetaData"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class ProductTypeMapEntryMetaData
	implements org.nightlabs.inheritance.MapEntryMetaData, Serializable
{
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	private String fieldName;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	private String key;

	/**
	 * Whether or not the field may be changed by children.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private byte writableByChildren = FieldMetaData.WRITABLEBYCHILDREN_YES;

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

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductTypeMapFieldMetaData mapFieldMetaData;

	protected ProductTypeMapEntryMetaData() { }
	public ProductTypeMapEntryMetaData(ProductTypeMapFieldMetaData mapFieldMetaData, String key)
	{
		this.mapFieldMetaData = mapFieldMetaData;
		this.fieldName = mapFieldMetaData.getFieldName();
		this.key = key;
	}

	/**
	 * @return Returns the fieldName.
	 */
	public String getFieldName()
	{
		return fieldName;
	}

	/**
	 * @return Returns the key.
	 */
	public Object getKey()
	{
		return key;
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
	 * @see org.nightlabs.inheritance.ProductInfoFieldMetaData#isWritable()
	 */
	public boolean isWritable()
	{
		return writable;
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
	 * @see org.nightlabs.inheritance.ProductInfoFieldMetaData#setWritable(boolean)
	 */
	public void setWritable(boolean writable)
	{
		this.writable = writable;
	}

	/**
	 * @see org.nightlabs.inheritance.ProductInfoFieldMetaData#isValueInherited()
	 */
	public boolean isValueInherited()
	{
		return valueInherited;
	}

	/**
	 * @see org.nightlabs.inheritance.ProductInfoFieldMetaData#setValueInherited(boolean)
	 */
	public void setValueInherited(boolean valueInherited)
	{
		this.valueInherited = valueInherited;
	}

}
