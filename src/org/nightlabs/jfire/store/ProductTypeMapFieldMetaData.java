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

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.Value;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="ProductTypeFieldMetaData"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 *
 * @jdo.fetch-group name="ProductType.fieldMetaDataMap" fields="mapEntryMetaDataMap" fetch-groups="default"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="ProductType.fieldMetaDataMap",
		members=@Persistent(name="mapEntryMetaDataMap"))
)
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class ProductTypeMapFieldMetaData
	extends ProductTypeFieldMetaData
	implements org.nightlabs.inheritance.MapFieldMetaData, Serializable
{
	private static final long serialVersionUID = 1L;

	/**
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
	@Persistent(
		mappedBy="mapFieldMetaData",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="key")
	@Value(dependent="true")
	protected Map<String, ProductTypeMapEntryMetaData> mapEntryMetaDataMap;

	protected ProductTypeMapFieldMetaData() { }

	public ProductTypeMapFieldMetaData(ProductType productType, String fieldName)
	{
		super(productType, fieldName);
		mapEntryMetaDataMap = new HashMap<String, ProductTypeMapEntryMetaData>();
	}

	@Override
	public org.nightlabs.inheritance.MapEntryMetaData getMapEntryMetaData(Object key)
	{
		if (!(key instanceof String))
			throw new IllegalArgumentException("This implementation of ProductTypeMapFieldMetaData supports only String keys!");

		String skey = (String)key;
		
		ProductTypeMapEntryMetaData memd = mapEntryMetaDataMap.get(skey);
		if (memd == null) {
			memd = new ProductTypeMapEntryMetaData(this, skey);
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
