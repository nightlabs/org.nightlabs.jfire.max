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

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.annotation.Implement;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.SimpleFieldInheriter;
import org.nightlabs.inheritance.StaticFieldMetaData;
import org.nightlabs.jfire.store.ProductType;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.voucher.store.id.VoucherTypeNameID"
 *		detachable="true"
 *		table="JFireVoucher_VoucherTypeName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeID"
 *
 * @jdo.fetch-group name="ProductType.name" fields="voucherType, names"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default" fields="names"
 */
public class VoucherTypeName
extends I18nText
implements Inheritable
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
	private VoucherType voucherType;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireVoucher_SimpleProductTypeName_names"
	 *
	 * @jdo.join
	 */
	protected Map names = new HashMap();

	protected VoucherTypeName()
	{
	}

	public VoucherTypeName(VoucherType voucherType)
	{
		this.voucherType = voucherType;
		this.organisationID = voucherType.getOrganisationID();
		this.productTypeID = voucherType.getProductTypeID();
	}

	@Implement
	protected Map getI18nMap()
	{
		return names;
	}

	@Implement
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

	public VoucherType getVoucherType()
	{
		return voucherType;
	}

	@Implement
	public FieldMetaData getFieldMetaData(String fieldName)
	{
		if ("names".equals(fieldName))
			return new StaticFieldMetaData(fieldName);

		return null;
	}

	@Implement
	public FieldInheriter getFieldInheriter(String fieldName)
	{
		return new SimpleFieldInheriter();
	}
}
