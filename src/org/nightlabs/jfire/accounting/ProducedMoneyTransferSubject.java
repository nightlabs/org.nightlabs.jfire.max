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

import org.nightlabs.i18n.I18nText;

/**
 * @author Chairat Kongarayawetchakun - chairatk at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.ProducedMoneyTransferSubjectID"
 *		detachable="true"
 *		table="JFireTrade_ProducedMoneyTransferSubject"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="producedMoneyTransferID"
 *
 * @jdo.fetch-group name="ProducedMoneyTransfer.subject" fields="producedMoneyTransfer, subjects"
 */
public class ProducedMoneyTransferSubject extends I18nText
{
	/////// begin primary key ///////
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String producedMoneyTransferID;

//	/**
//	 * @jdo.field primary-key="true"
//	 * @jdo.column length="100"
//	 */
//	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String producedMoneyTransferSubjectID;
	/////// end primary key ///////

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProducedMoneyTransfer producedMoneyTransfer;

	/**
	 * key: String languageID<br/>
	 * value: String subject
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		table="JFireTrade_ProducedMoneyTransferSubject_subjects"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	protected Map subjects = new HashMap();

	protected ProducedMoneyTransferSubject()
	{
	}

	public ProducedMoneyTransferSubject(ProducedMoneyTransfer producedMoneyTransfer)
	{
//		this.producedMoneyTransferID = producedMoneyTransfer.getTransferTypeID();
//		this.organisationID = producedMoneyTransfer.getOrganisationID();
//		this.regionID = region.getRegionID();
		this.producedMoneyTransfer = producedMoneyTransfer;
	}

	/**
	 * @return Returns the countryID.
	 */
	public String getProducedMoneyTransferID()
	{
		return producedMoneyTransferID;
	}

//	/**
//	 * @return Returns the organisationID.
//	 */
//	public String getOrganisationID()
//	{
//		return organisationID;
//	}

	/**
	 * @return Returns the region.
	 */
	public ProducedMoneyTransfer getProducedMoneyTransfer()
	{
		return producedMoneyTransfer;
	}

	/**
	 * @return Returns the regionID.
	 */
	public String getProducedMoneyTransferSubjectID()
	{
		return producedMoneyTransferSubjectID;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	protected Map getI18nMap()
	{
		return subjects;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	protected String getFallBackValue(String languageID)
	{
		return producedMoneyTransfer == null ? languageID : producedMoneyTransfer.getPrimaryKey();
	}
}