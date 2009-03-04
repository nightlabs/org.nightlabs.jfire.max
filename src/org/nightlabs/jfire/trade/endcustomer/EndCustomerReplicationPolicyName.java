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
import org.nightlabs.jdo.ObjectIDUtil;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.endcustomer.id.EndCustomerReplicationPolicyNameID"
 *		detachable="true"
 *		table="JFireTrade_EndCustomerReplicationPolicyName"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, endCustomerReplicationPolicyID"
 *
 * @jdo.fetch-group name="EndCustomerReplicationPolicy.name" fields="endCustomerReplicationPolicy, names"
 */
public class EndCustomerReplicationPolicyName extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long endCustomerReplicationPolicyID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private EndCustomerReplicationPolicy endCustomerReplicationPolicy;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EndCustomerReplicationPolicyName() { }

	public EndCustomerReplicationPolicyName(EndCustomerReplicationPolicy endCustomerReplicationPolicy) {
		this.organisationID = endCustomerReplicationPolicy.getOrganisationID();
		this.endCustomerReplicationPolicyID = endCustomerReplicationPolicy.getEndCustomerReplicationPolicyID();
		this.endCustomerReplicationPolicy = endCustomerReplicationPolicy;
		this.names = new HashMap<String, String>();
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
	 *		table="JFireTrade_EndCustomerReplicationPolicyName_names"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> names;

	@Override
	protected Map<String, String> getI18nMap() {
		return names;
	}

	@Override
	protected String getFallBackValue(String languageID) {
		return ObjectIDUtil.longObjectIDFieldToString(endCustomerReplicationPolicyID);
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
