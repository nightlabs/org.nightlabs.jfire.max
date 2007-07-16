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

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductTypeGroupNameID"
 *		detachable="true"
 *		table="JFireTrade_ProductTypeGroupName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeGroupID"
 *
 * @jdo.fetch-group name="ProductTypeGroup.name" fields="productTypeGroup, names"
 */
public class ProductTypeGroupName extends I18nText {

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String productTypeGroupID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductTypeGroup productTypeGroup;
	
	
	/**
	 * @deprecated Only for JDO!
	 */
	protected ProductTypeGroupName() {
	}

	public ProductTypeGroupName(ProductTypeGroup productTypeGroup) {
		this.organisationID = productTypeGroup.getOrganisationID();
		this.productTypeGroupID = productTypeGroup.getProductTypeGroupID();
		this.productTypeGroup = productTypeGroup;
		this.names = new HashMap();
	}

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireTrade_ProductTypeGroupName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map names;
	
	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap() {
		return names;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID) {
		return productTypeGroupID;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getProductTypeGroupID()
	{
		return productTypeGroupID;
	}

	public ProductTypeGroup getProductTypeGroup()
	{
		return productTypeGroup;
	}

}
