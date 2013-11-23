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

package org.nightlabs.jfire.trade.endcustomer;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import org.nightlabs.jfire.trade.endcustomer.id.EndCustomerReplicationPolicyDescriptionID;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.endcustomer.id.EndCustomerReplicationPolicyDescriptionID"
 *		detachable="true"
 *		table="JFireTrade_EndCustomerReplicationPolicyDescription"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, endCustomerReplicationPolicyID"
 *
 * @jdo.fetch-group name="EndCustomerReplicationPolicy.description" fields="endCustomerReplicationPolicy, texts"
 */
@PersistenceCapable(
	objectIdClass=EndCustomerReplicationPolicyDescriptionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_EndCustomerReplicationPolicyDescription")
@FetchGroups(
	@FetchGroup(
		name="EndCustomerReplicationPolicy.description",
		members={@Persistent(name="endCustomerReplicationPolicy"), @Persistent(name="texts")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class EndCustomerReplicationPolicyDescription extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long endCustomerReplicationPolicyID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private EndCustomerReplicationPolicy endCustomerReplicationPolicy;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EndCustomerReplicationPolicyDescription() { }

	public EndCustomerReplicationPolicyDescription(EndCustomerReplicationPolicy endCustomerReplicationPolicy) {
		this.organisationID = endCustomerReplicationPolicy.getOrganisationID();
		this.endCustomerReplicationPolicyID = endCustomerReplicationPolicy.getEndCustomerReplicationPolicyID();
		this.endCustomerReplicationPolicy = endCustomerReplicationPolicy;
		this.texts = new HashMap<String, String>();
	}

	/**
	 * key: String languageID<br/>
	 * value: String description
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireTrade_EndCustomerReplicationPolicyDescription_texts"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		table="JFireTrade_EndCustomerReplicationPolicyDescription_texts",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected Map<String, String> texts;

	@Override
	protected Map<String, String> getI18nMap() {
		return texts;
	}

	@Override
	protected String getFallBackValue(String languageID) {
		return "";
	}

	public String getOrganisationID() {
		return organisationID;
	}

	public long getEndCustomerReplicationPolicyID() {
		return endCustomerReplicationPolicyID;
	}

	public EndCustomerReplicationPolicy getEndCustomerReplicationPolicy()
	{
		return endCustomerReplicationPolicy;
	}

}
