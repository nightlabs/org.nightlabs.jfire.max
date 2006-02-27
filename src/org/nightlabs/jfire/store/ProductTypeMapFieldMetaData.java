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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="ProductTypeFieldMetaData"
 *		detachable="true"
 *		table="JFireTrade_MapFieldMetaData"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class ProductTypeMapFieldMetaData
	extends ProductTypeFieldMetaData
	implements org.nightlabs.inheritance.MapFieldMetaData, Serializable
{
	/**
	 * key: String key<br/>
	 * value: ProductTypeMapEntryMetaData mapEntryMetaData
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="ProductTypeMapEntryMetaData"
	 *		dependent-value="true"
	 *		mapped-by="mapFieldMetaData"
	 *
	 * @jdo.key mapped-by="key"
	 */
	protected Map mapEntryMetaDataMap = new HashMap();

	protected ProductTypeMapFieldMetaData() { }

	/**
	 * @param product
	 * @param fieldName
	 */
	public ProductTypeMapFieldMetaData(ProductType product, String fieldName)
	{
		super(product, fieldName);
	}

	/**
	 * @see org.nightlabs.inheritance.MapFieldMetaData#getMapEntryMetaData(java.lang.Object)
	 */
	public org.nightlabs.inheritance.MapEntryMetaData getMapEntryMetaData(Object key)
	{
		if (!(key instanceof String))
			throw new IllegalArgumentException("This implementation of ProductTypeMapFieldMetaData supports only String keys!");

		String skey = (String)key;
		
		ProductTypeMapEntryMetaData memd = (ProductTypeMapEntryMetaData)mapEntryMetaDataMap.get(skey);
		if (memd == null) {
			memd = new ProductTypeMapEntryMetaData(this, skey);
			mapEntryMetaDataMap.put(skey, memd);
		}
		return memd;
	}

	/**
	 * @see org.nightlabs.inheritance.MapFieldMetaData#removeMapEntryMetaData(java.lang.Object)
	 */
	public void removeMapEntryMetaData(Object key)
	{
		mapEntryMetaDataMap.remove(key);
	}

}
