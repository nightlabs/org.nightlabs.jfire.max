/*
 * Created 	on Mar 16, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.accounting;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.PriceFragmentTypeNameID"
 *		detachable="true"
 *		table="JFireTrade_PriceFragmentTypeName"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class
 *		field-order="organisationID, priceFragmentTypeID"
 */
public class PriceFragmentTypeName extends I18nText {

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID; 
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String priceFragmentTypeID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceFragmentType priceFragmentType;
	
	
	/**
	 * @deprecated Only for JDO!
	 */
	protected PriceFragmentTypeName() {
	}
	
	public PriceFragmentTypeName(PriceFragmentType priceFragmentType) {
		this.priceFragmentType = priceFragmentType;
		this.organisationID = priceFragmentType.getOrganisationID();
		this.priceFragmentTypeID = priceFragmentType.getPriceFragmentTypeID();
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
	 *		table="JFireTrade_PriceFragmentTypeName_names"
	 *
	 * @jdo.join
	 */
	protected Map names = new HashMap();
	
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
		return priceFragmentTypeID;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getPriceFragmentTypeID() {
		return priceFragmentTypeID;
	}

	public PriceFragmentType getPriceFragmentType()
	{
		return priceFragmentType;
	}
}
