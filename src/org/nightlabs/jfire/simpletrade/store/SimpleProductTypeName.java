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

package org.nightlabs.jfire.simpletrade.store;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.MapFieldInheriter;
import org.nightlabs.inheritance.NotWritableException;
import org.nightlabs.inheritance.SimpleFieldInheriter;
import org.nightlabs.jfire.store.ProductType;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.simpletrade.store.id.SimpleProductTypeNameID"
 *		detachable="true"
 *		table="JFireSimpleTrade_SimpleProductTypeName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeID"
 *
 * @jdo.fetch-group name="ProductType.name" fields="simpleProductType, names"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default" fields="names"
 */
public class SimpleProductTypeName
extends I18nText
implements Inheritable
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
	private String productTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private SimpleProductType simpleProductType;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireSimpleTrade_SimpleProductTypeName_names"
	 *
	 * @jdo.join
	 */
	protected Map names = new HashMap();

	protected SimpleProductTypeName()
	{
	}

	public SimpleProductTypeName(SimpleProductType product)
	{
		this.simpleProductType = product;
		this.organisationID = product.getOrganisationID();
		this.productTypeID = product.getProductTypeID();
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap()
	{
		return names;
	}

	/**
	 * This variable contains the name in a certain language after localization.
	 *
	 * @see #localize(String)
	 * @see #detachCopyLocalized(String, javax.jdo.PersistenceManager)
	 *
	 * @jdo.field persistence-modifier="transactional" default-fetch-group="false"
	 */
	protected String name;

	/**
	 * @see org.nightlabs.i18n.I18nText#setText(java.lang.String)
	 */
	protected void setText(String localizedValue)
	{
		name = localizedValue;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getText()
	 */
	public String getText()
	{
		return name;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return ProductType.getPrimaryKey(organisationID, productTypeID);
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
	 * @return Returns the simpleProductType.
	 */
	public SimpleProductType getSimpleProductType()
	{
		return simpleProductType;
	}

	protected static class StaticFieldMetaData
	implements FieldMetaData
	{
		private String fieldName;

		public StaticFieldMetaData(String fieldName)
		{
			this.fieldName = fieldName;
		}

		public String getFieldName()
		{
			return fieldName;
		}

		public byte getWritableByChildren()
		{
			return WRITABLEBYCHILDREN_YES;
		}

		public void setWritableByChildren(byte writableByChildren) { }

		public boolean isWritable()
		{
			return true;
		}

		public void assertWritable() throws NotWritableException { }

		public void setWritable(boolean writable) { }

		public boolean isValueInherited()
		{
			return true;
		}

		public void setValueInherited(boolean valueInherited) { }
	}

	public FieldMetaData getFieldMetaData(String fieldName)
	{
		if ("names".equals(fieldName))
			return new StaticFieldMetaData(fieldName);

		return null;
	}

	public FieldInheriter getFieldInheriter(String fieldName)
	{
		return new SimpleFieldInheriter();
	}
}
