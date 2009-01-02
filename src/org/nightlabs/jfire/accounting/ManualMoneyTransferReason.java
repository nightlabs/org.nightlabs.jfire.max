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

package org.nightlabs.jfire.accounting;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.annotation.Implement;
import org.nightlabs.i18n.I18nText;

/**
 * @author Chairat Kongarayawetchakun - chairatk at nightlabs dot de
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.ManualMoneyTransferReasonID"
 *		detachable="true"
 *		table="JFireTrade_ManualMoneyTransferReason"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, transferTypeID, transferID"
 *
 * @jdo.fetch-group name="ManualMoneyTransfer.reason" fields="manualMoneyTransfer, texts"
 */
public class ManualMoneyTransferReason extends I18nText
{
	private static final long serialVersionUID = 1L; // Added this field. Marco.

	// TODO incomplete and wrong primary key!!!
	// Should be the same as the one of ManualMoneyTransfer (which is the one of the root-class in the hierarchy - i.e. Transfer).

//	/////// begin primary key ///////
//	/**
//	 * @jdo.field primary-key="true"
//	 * @jdo.column length="100"
//	 */
//	private String producedMoneyTransferID;
//
////	/**
////	 * @jdo.field primary-key="true"
////	 * @jdo.column length="100"
////	 */
////	private String organisationID;
//
//	/**
//	 * @jdo.field primary-key="true"
//	 * @jdo.column length="100"
//	 */
//	private String producedMoneyTransferSubjectID;
//	/////// end primary key ///////

	// @Chairat: because it fails building, I already fixed the PK. And please update your XDoclet plugin (*DELETE* the old one):
	// https://www.jfire.org/modules/newbb/forum_4_topic_id_155.html

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String transferTypeID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long transferID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ManualMoneyTransfer manualMoneyTransfer;

	/**
	 * key: String languageID<br/>
	 * value: String text
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireTrade_ManualMoneyTransferReason_texts"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map<String, String> texts;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ManualMoneyTransferReason() { }

	public ManualMoneyTransferReason(ManualMoneyTransfer manualMoneyTransfer)
	{
		this.organisationID = manualMoneyTransfer.getOrganisationID();
		this.transferTypeID = manualMoneyTransfer.getTransferTypeID();
		this.transferID = manualMoneyTransfer.getTransferID();
		this.manualMoneyTransfer = manualMoneyTransfer;
		this.texts = new HashMap<String, String>();
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getTransferTypeID()
	{
		return transferTypeID;
	}
	public long getTransferID()
	{
		return transferID;
	}

	public ManualMoneyTransfer getManualMoneyTransfer()
	{
		return manualMoneyTransfer;
	}

	@Override
	@Implement
	protected Map<String, String> getI18nMap()
	{
		return texts;
	}

	@Override
	@Implement
	protected String getFallBackValue(String languageID)
	{
		return "";
	}
}