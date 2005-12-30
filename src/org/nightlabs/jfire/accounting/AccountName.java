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
 *		objectid-class="org.nightlabs.jfire.accounting.id.AccountNameID"
 *		detachable="true"
 *		table="JFireTrade_AccountName"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class
 *		field-order="organisationID, anchorTypeID, anchorID"
 */
public class AccountName extends I18nText {

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String anchorTypeID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String anchorID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Account account;
	
	
	/**
	 * @deprecated Only for JDO!
	 */
	protected AccountName() {
	}

	public AccountName(Account account) {
		this.organisationID = account.getOrganisationID();
		this.anchorTypeID = account.getAnchorTypeID();
		this.anchorID = account.getAnchorID();
		this.account = account;
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
	 *		table="JFireTrade_AccountName_names"
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
		return anchorID;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public String getAnchorTypeID()
	{
		return anchorTypeID;
	}

	public String getAnchorID() {
		return anchorID;
	}

	public Account getAccount()
	{
		return account;
	}

}
