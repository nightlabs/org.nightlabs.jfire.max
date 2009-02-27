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

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.endcustomer.id.EndCustomerTransferPolicyDescriptionID"
 *		detachable="true"
 *		table="JFireTrade_EndCustomerTransferPolicyDescription"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, endCustomerTransferPolicyID"
 *
 * @jdo.fetch-group description="EndCustomerTransferPolicy.description" fields="endCustomerTransferPolicy, descriptions"
 */
public class EndCustomerTransferPolicyDescription extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long endCustomerTransferPolicyID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private EndCustomerTransferPolicy endCustomerTransferPolicy;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EndCustomerTransferPolicyDescription() { }

	public EndCustomerTransferPolicyDescription(EndCustomerTransferPolicy endCustomerTransferPolicy) {
		this.organisationID = endCustomerTransferPolicy.getOrganisationID();
		this.endCustomerTransferPolicyID = endCustomerTransferPolicy.getEndCustomerTransferPolicyID();
		this.endCustomerTransferPolicy = endCustomerTransferPolicy;
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
	 *		table="JFireTrade_EndCustomerTransferPolicyDescription_texts"
	 *
	 * @jdo.join
	 */
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

	public long getEndCustomerTransferPolicyID() {
		return endCustomerTransferPolicyID;
	}

	public EndCustomerTransferPolicy getEndCustomerTransferPolicy()
	{
		return endCustomerTransferPolicy;
	}

}
