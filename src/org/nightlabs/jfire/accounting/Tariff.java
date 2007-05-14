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

import java.io.Serializable;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.accounting.id.TariffID;

/**
 * TODO Shall I really put this class into JFireTrade? Or is it Ticketing specific?
 *			Does it help me here or does it make things complicated?
 *			To have it here makes sense, if I define directly in CustomerGroup which Tariffs
 *			they're allowed to sell. Or should I better use our Authority based ACL to manage
 *			Tariffs? Or should a CustomerGroup automatically create an Authority? That seems to
 *			make sense. We wanted to make Authorities markable "internal" and then use them for
 *			such purposes...
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.TariffID"
 *		detachable="true"
 *		table="JFireTrade_Tariff"
 *
 * @jdo.inheritance strategy = "new-table"
 * 
 * @jdo.create-objectid-class
 *		field-order="organisationID, tariffID"
 *		include-body="id/TariffID.body.inc"
 *
 * @jdo.fetch-group name="Tariff.name" fields="name"
 * @jdo.fetch-group name="Tariff.this" fetch-groups="default" fields="name"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fields="name"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fields="name"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fields="name"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fields="name"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fields="name"
 */
public class Tariff
	implements Serializable
{
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_NAME = "Tariff.name";
	public static final String FETCH_GROUP_THIS_TARIFF = "Tariff.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long tariffID = -1;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="tariff"
	 */
	private TariffName name;

	public Tariff() { }

	public Tariff(String organisationID, long tariffID)
	{
		this.organisationID = organisationID;
		this.tariffID = tariffID;
		this.primaryKey = getPrimaryKey(organisationID, tariffID);
		this.name = new TariffName(this);
	}

	public static String getPrimaryKey(String organisationID, long tariffID)
	{
		return organisationID + '/' + Long.toHexString(tariffID);
	}
	
	/**
	 * Get the JDO object id.
	 * @return the JDO object id.
	 */
	public TariffID getObjectId()
	{
		return (TariffID)JDOHelper.getObjectId(this);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the tariffID.
	 */
	public long getTariffID()
	{
//		if (tariffID == null)
//			return -1;
		return tariffID;
	}
//	/**
//	 * @param tariffID The tariffID to set.
//	 */
//	public void setTariffID(long tariffID)
//	{
//		this.tariffID = tariffID;
//		this.primaryKey = getPrimaryKey(getOrganisationID(), tariffID);
//		this.name.setTariffID(tariffID);
//	}
//	public static String getPrimaryKey(String organisationID, String tariffID)
//	{
//		return organisationID + '/' + tariffID;
//	}
	/**
	 * @return Returns the primaryKey.
	 */
	public String getPrimaryKey()
	{
		return primaryKey;
	}
	
//	/**
//	 * @see javax.jdo.listener.StoreCallback#jdoPreStore()
//	 */
//	public void jdoPreStore()
//	{
//		if (tariffID < 0) {
//			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//			this.setTariffID(
//					TariffRegistry.getTariffRegistry(pm).createTariffID());
//		}
//	}

	/**
	 * @return Returns the name.
	 */
	public TariffName getName()
	{
		return name;
	}

//	/**
//	 * @see org.nightlabs.i18n.Localizable#localize(java.lang.String)
//	 */
//	public void localize(String languageID)
//	{
//		name.localize(languageID);
//	}
//
//	/**
//	 * @see org.nightlabs.jdo.LocalizedDetachable#detachCopyLocalized(java.lang.String, javax.jdo.PersistenceManager)
//	 */
//	public LocalizedDetachable detachCopyLocalized(String languageID, PersistenceManager pm)
//	{
//		Tariff tariff = (Tariff) pm.detachCopy(this);
//		tariff.name.localize(languageID, this.name);
//		return tariff;
//	}
}
