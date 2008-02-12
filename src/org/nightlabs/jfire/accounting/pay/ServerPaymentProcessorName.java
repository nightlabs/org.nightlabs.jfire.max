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

package org.nightlabs.jfire.accounting.pay;

import java.util.HashMap;
import java.util.Map;

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
 * @jdo.create-objectid-class field-order="organisationID, serverPaymentProcessorID"
 *
 * @jdo.fetch-group name="ServerPaymentProcessorName.names" fields="names"
 * @jdo.fetch-group name="ServerPaymentProcessorName.serverPaymentProcessor" fields="serverPaymentProcessor"
 * @jdo.fetch-group name="ServerPaymentProcessorName.this" fetch-groups="default" fields="serverPaymentProcessor, names"
 */
public class ServerPaymentProcessorName extends I18nText
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
	@Deprecated
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
	 *		table="JFireTrade_ServerPaymentProcessorName_names"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map names = new HashMap();

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map getI18nMap()
	{
		return names;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return ServerPaymentProcessor.getPrimaryKey(organisationID, serverPaymentProcessorID);
	}

	public ServerPaymentProcessor getServerPaymentProcessor()
	{
		return serverPaymentProcessor;
	}

}
