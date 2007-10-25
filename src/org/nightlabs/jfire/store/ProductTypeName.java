package org.nightlabs.jfire.store;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.StaticFieldMetaData;
import org.nightlabs.jdo.inheritance.JDOSimpleFieldInheriter;

/**
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.store.id.ProductTypeNameID"
 *		detachable="true"
 *		table="JFireTrade_ProductTypeName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeID"
 *
 * @jdo.fetch-group name="ProductType.name" fields="productType, names"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default" fields="names"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default" fields="names"
 *
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 */
public class ProductTypeName 
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
	private ProductType productType;
	
	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireTrade_ProductTypeName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProductTypeName() { }

	public ProductTypeName(ProductType product)
	{
		this.productType = product;
		this.organisationID = product.getOrganisationID();
		this.productTypeID = product.getProductTypeID();
		this.names = new HashMap<String, String>();
	}
	
	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map getI18nMap()
	{
		return names;
	}	
	
	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
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
	public ProductType getProductType()
	{
		return productType;
	}

	public FieldMetaData getFieldMetaData(String fieldName)
	{
		if ("names".equals(fieldName))
			return new StaticFieldMetaData(fieldName);

		return null;
	}

	public FieldInheriter getFieldInheriter(String fieldName)
	{
		return new JDOSimpleFieldInheriter();
	}
}
