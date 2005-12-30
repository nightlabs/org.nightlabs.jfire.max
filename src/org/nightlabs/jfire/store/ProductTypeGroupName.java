/*
 * Created 	on Mar 16, 2005
 * 					by alex
 *
 */
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
	 *		dependent="true"
	 *		default-fetch-group="true"
	 *		table="JFireTrade_ProductTypeGroupName_names"
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
	protected void setText(String localizedValue) {
		name = localizedValue;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getText()
	 */
	public String getText() {
		return name;
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