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

package org.nightlabs.jfire.reporting.parameter.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.reporting.parameter.ValueProvider;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Join;
import org.nightlabs.jfire.reporting.parameter.config.id.ValueProviderConfigMessageID;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * Description i18n text for {@link ValueProvider}s.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
/**
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.parameter.config.id.ValueProviderConfigMessageID"
 *		detachable = "true"
 *		table="JFireReporting_ValueProviderConfigMessage"
 *
 * @jdo.create-objectid-class field-order="organisationID, valueAcquisitionSetupID, valueProviderOrganisationID, valueProviderCategoryID, valueProviderID, valueProviderConfigID"
 * 
 * @jdo.fetch-group name="ValueProviderConfig.message" fetch-groups="default" fields="names"
 */@PersistenceCapable(
	objectIdClass=ValueProviderConfigMessageID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireReporting_ValueProviderConfigMessage")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="ValueProviderConfig.message",
		members=@Persistent(name="names"))
)

public class ValueProviderConfigMessage extends I18nText implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */	@PrimaryKey

	private long valueAcquisitionSetupID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String valueProviderOrganisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String valueProviderCategoryID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String valueProviderID;
	
	/**
	 * @jdo.field primary-key="true"
	 */	@PrimaryKey

	private long valueProviderConfigID;
	
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private ValueProviderConfig valueProviderConfig;
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ValueProviderConfigMessage() {
	}

	public ValueProviderConfigMessage(ValueProviderConfig valueProviderConfig) {
		validatePrimaryKeyFields(valueProviderConfig);
		this.names = new HashMap<String, String>();
	}
	
	void validatePrimaryKeyFields(ValueProviderConfig valueProviderConfig) {
		this.organisationID = valueProviderConfig.getOrganisationID();
		this.valueAcquisitionSetupID = valueProviderConfig.getValueAcquisitionSetupID();
		this.valueProviderOrganisationID = valueProviderConfig.getValueProviderOrganisationID();
		this.valueProviderCategoryID = valueProviderConfig.getValueProviderCategoryID();
		this.valueProviderID = valueProviderConfig.getValueProviderID();
		this.valueProviderConfig = valueProviderConfig;
		this.valueProviderConfigID = valueProviderConfig.getValueProviderConfigID();
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
	 *		default-fetch-group="true"
	 *		table="JFireReporting_ValueProviderConfigMessage_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireReporting_ValueProviderConfigMessage_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)

	protected Map<String, String> names;
	
	/**
	 * @see com.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}

	/**
	 * @see com.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID) {
		return organisationID + "/" + valueProviderID;
	}

	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return the valueProviderID
	 */
	public String getValueProviderID() {
		return valueProviderID;
	}

	/**
	 * @return the valueProviderCategoryID
	 */
	public String getValueProviderCategoryID() {
		return valueProviderCategoryID;
	}

	/**
	 * @return the valueAcquisitionSetupID
	 */
	public long getValueAcquisitionSetupID() {
		return valueAcquisitionSetupID;
	}

	/**
	 * @return the valueProviderConfig
	 */
	public ValueProviderConfig getValueProviderConfig() {
		return valueProviderConfig;
	}

	/**
	 * @return the valueProviderOrganisationID
	 */
	public String getValueProviderOrganisationID() {
		return valueProviderOrganisationID;
	}
	
	public long getValueProviderConfigID() {
		return valueProviderConfigID;
	}
	
}
