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

package org.nightlabs.jfire.trade;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.trade.id.CustomerGroupNameID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.CustomerGroupNameID"
 *		detachable="true"
 *		table="JFireTrade_CustomerGroupName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, customerGroupID"
 *
 * @jdo.fetch-group name="CustomerGroup.name" fields="customerGroup, names"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fields="customerGroup, names"
 */
@PersistenceCapable(
	objectIdClass=CustomerGroupNameID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_CustomerGroupName")
@FetchGroups({
	@FetchGroup(
		name="CustomerGroup.name",
		members={@Persistent(name="customerGroup"), @Persistent(name="names")}),
	@FetchGroup(
		name="FetchGroupsPriceConfig.edit",
		members={@Persistent(name="customerGroup"), @Persistent(name="names")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class CustomerGroupName extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String customerGroupID;

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
	 *		table="JFireTrade_CustomerGroupName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_CustomerGroupName_names",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, String> names;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private CustomerGroup customerGroup;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected CustomerGroupName()
	{
	}

	public CustomerGroupName(CustomerGroup customerGroup)
	{
		this.organisationID = customerGroup.getOrganisationID();
		this.customerGroupID = customerGroup.getCustomerGroupID();
		this.customerGroup = customerGroup;
		names = new HashMap<String, String>();
	}

	@Override
	protected Map<String, String> getI18nMap()
	{
		return names;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return CustomerGroup.getPrimaryKey(organisationID, customerGroupID);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getCustomerGroupID()
	{
		return customerGroupID;
	}

	public CustomerGroup getCustomerGroup()
	{
		return customerGroup;
	}

}
