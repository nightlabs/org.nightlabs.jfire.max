/*
 * Created on 18.10.2004
 */
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
	 *		dependent="true"
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
