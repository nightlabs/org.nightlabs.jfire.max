/*
 * Created on Jan 5, 2005
 */
package org.nightlabs.jfire.accounting.pay;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.nightlabs.i18n.I18nText;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorNameID"
 *		detachable="true"
 *		table="JFireTrade_ServerPaymentProcessorName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="ServerPaymentProcessorName.names" fields="names"
 * @jdo.fetch-group name="ServerPaymentProcessorName.serverPaymentProcessor" fields="serverPaymentProcessor"
 * @jdo.fetch-group name="ServerPaymentProcessorName.this" fetch-groups="default" fields="serverPaymentProcessor, names"
 */
public class ServerPaymentProcessorName extends I18nText
{
	public static final String FETCH_GROUP_NAMES = "ServerPaymentProcessorName.names";
	public static final String FETCH_GROUP_SERVER_PAYMENT_PROCESSOR = "ServerPaymentProcessorName.serverPaymentProcessor";
	public static final String FETCH_GROUP_THIS_SERVER_PAYMENT_PROCESSOR_NAME = "ServerPaymentProcessorName.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String serverPaymentProcessorID;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ServerPaymentProcessorName()
	{
	}

	public ServerPaymentProcessorName(ServerPaymentProcessor serverPaymentProcessor)
	{
		this.serverPaymentProcessor = serverPaymentProcessor;
		this.organisationID = serverPaymentProcessor.getOrganisationID();
		this.serverPaymentProcessorID = serverPaymentProcessor.getServerPaymentProcessorID();
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ServerPaymentProcessor serverPaymentProcessor;

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
	 *		table="JFireTrade_ServerPaymentProcessorName_names"
	 *
	 * @jdo.join
	 */
	protected Map names = new HashMap();

	/**
	 * This variable contains the name in a certain language after localization.
	 *
	 * @see #localize(String)
	 * @see #detachCopyLocalized(String, PersistenceManager)
	 *
	 * @jdo.field persistence-modifier="transactional" default-fetch-group="false"
	 */
	protected String name;

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
	public void setText(String localizedValue)
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
		return ServerPaymentProcessor.getPrimaryKey(organisationID, serverPaymentProcessorID);
	}

	public ServerPaymentProcessor getServerPaymentProcessor()
	{
		return serverPaymentProcessor;
	}

}
