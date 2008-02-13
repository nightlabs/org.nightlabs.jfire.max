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
 *		persistence-capable-superclass="ProductTypeLocalFieldMetaData"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 *
 * @jdo.fetch-group name="ProductTypeLocal.fieldMetaDataMap" fields="mapEntryMetaDataMap" fetch-groups="default"
 */
public class ProductTypeLocalMapFieldMetaData
	extends ProductTypeLocalFieldMetaData
	implements org.nightlabs.inheritance.MapFieldMetaData, Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="ProductTypeLocalMapEntryMetaData"
	 *		dependent-value="true"
	 *		mapped-by="mapFieldMetaData"
	 *
	 * @jdo.key mapped-by="key"
	 */
	protected Map<String, ProductTypeLocalMapEntryMetaData> mapEntryMetaDataMap;

	protected ProductTypeLocalMapFieldMetaData() { }

	public ProductTypeLocalMapFieldMetaData(ProductTypeLocal productTypeLocal, String fieldName)
	{
		super(productTypeLocal, fieldName);
		mapEntryMetaDataMap = new HashMap<String, ProductTypeLocalMapEntryMetaData>();
	}

	@Override
	public org.nightlabs.inheritance.MapEntryMetaData getMapEntryMetaData(Object key)
	{
		if (!(key instanceof String))
			throw new IllegalArgumentException("This implementation of ProductTypeLocalMapFieldMetaData supports only String keys!");

		String skey = (String)key;

		ProductTypeLocalMapEntryMetaData memd = mapEntryMetaDataMap.get(skey);
		if (memd == null) {
			memd = new ProductTypeLocalMapEntryMetaData(this, skey);
			mapEntryMetaDataMap.put(skey, memd);
		}
		return memd;
	}

	@Override
	public void removeMapEntryMetaData(Object key)
	{
		mapEntryMetaDataMap.remove(key);
	}

}
