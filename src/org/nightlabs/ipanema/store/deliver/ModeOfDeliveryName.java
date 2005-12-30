/*
 * Created on May 31, 2005
 */
package org.nightlabs.ipanema.store.deliver;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.ipanema.store.deliver.id.ModeOfDeliveryNameID"
 *		detachable="true"
 *		table="JFireTrade_ModeOfDeliveryName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="ModeOfDeliveryName.names" fields="names"
 * @jdo.fetch-group name="ModeOfDeliveryName.this" fetch-groups="default" fields="names"
 */
public class ModeOfDeliveryName extends I18nText
{
	public static final String FETCH_GROUP_NAMES = "ModeOfDeliveryName.names";
	public static final String FETCH_GROUP_THIS_MODE_OF_PAYMENT_NAME = "ModeOfDeliveryName.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String modeOfDeliveryID;

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
	 *		table="JFireTrade_ModeOfDeliveryName_names"
	 *
	 * @jdo.join
	 */
	protected Map names = new HashMap();

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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ModeOfDelivery modeOfDelivery;

	/**
	 * @deprecated Only for JDO! 
	 */
	protected ModeOfDeliveryName() { }
	
	public ModeOfDeliveryName(ModeOfDelivery modeOfDelivery)
	{
		this.modeOfDelivery = modeOfDelivery;
		this.organisationID = modeOfDelivery.getOrganisationID();
		this.modeOfDeliveryID = modeOfDelivery.getModeOfDeliveryID();
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap()
	{
		return names;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#setText(java.lang.String)
	 */
	protected void setText(String localizedValue)
	{
		this.name = localizedValue;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getText()
	 */
	public String getText()
	{
		return name;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return ModeOfDelivery.getPrimaryKey(organisationID, modeOfDeliveryID);
	}

}
