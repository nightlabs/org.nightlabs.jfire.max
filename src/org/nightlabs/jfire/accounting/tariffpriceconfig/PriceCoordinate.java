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

package org.nightlabs.jfire.accounting.tariffpriceconfig;

import java.io.Serializable;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.trade.CustomerGroup;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.tariffpriceconfig.id.PriceCoordinateID"
 *		detachable="true"
 *		table="JFireTrade_PriceCoordinate"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, priceCoordinateID"
 *
 * @jdo.fetch-group name="PriceCoordinate.priceConfig" fields="priceConfig"
 * @jdo.fetch-group name="PriceCoordinate.this" fetch-groups="default" fields="priceConfig"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fields="priceConfig"
 */
public class PriceCoordinate implements Serializable, StoreCallback, IPriceCoordinate
{
	public static final String FETCH_GROUP_PRICE_CONFIG = "PriceCoordinate.priceConfig";
	public static final String FETCH_GROUP_THIS_PRICE_COORDINATE = "PriceCoordinate.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID = null;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long priceCoordinateID = -1;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String customerGroupPK;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String tariffPK;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String currencyID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceConfig priceConfig;

	public PriceCoordinate()
	{
	}

	public PriceCoordinate(PriceConfig priceConfig, IPriceCoordinate priceCoordinate)
	{
		this.priceConfig = priceConfig;

		this.customerGroupPK = priceCoordinate.getCustomerGroupPK();
		this.tariffPK = priceCoordinate.getTariffPK();
		this.currencyID = priceCoordinate.getCurrencyID();
	}

	/**
	 * @deprecated This constructor should not be used in java code! An instance
	 * created by it, cannot be persisted into the database! It is
	 * only intended for usage as address in javascript formulas! Though it's
	 * marked as deprecated, it will NOT vanish. Deprecation is only set to make you
	 * aware of the special function of this constructor. 
	 *
	 * @param customerGroupPK Either <tt>null</tt> (which means the same <tt>CustomerGroup</tt>
	 *		as the current cell's location) or the PK of another cell's location
	 *		(see {@link CustomerGroup#getPrimaryKey()}).
	 * @param tariffPK Either <tt>null</tt> (which means the same <tt>Tariff</tt>
	 *		as the current cell's location) or the PK of another cell's location
	 *		(see {@link Tariff#getPrimaryKey()}).
	 * @param currencyID Either <tt>null</tt> (which means the same <tt>Currency</tt>
	 *		as the current cell's location) or the PK of another cell's location
	 *		(see {@link Currency#getCurrencyID()}).
	 */
	public PriceCoordinate(String customerGroupPK, String tariffPK, String currencyID)
	{
		this.customerGroupPK = customerGroupPK;
		this.tariffPK = tariffPK;
		this.currencyID = currencyID;
	}

	public PriceCoordinate(IPriceCoordinate priceCoordinate)
	{
		this.priceConfig = priceCoordinate.getPriceConfig();

		this.customerGroupPK = priceCoordinate.getCustomerGroupPK();
		this.tariffPK = priceCoordinate.getTariffPK();
		this.currencyID = priceCoordinate.getCurrencyID();
	}

	/**
	 * This constructor creates a <tt>PriceCoordinate</tt> which is identical to
	 * <tt>currentCell</tt> except for the dimensions defined in <tt>address</tt>.
	 * This means every field in <tt>address</tt> which is NOT <tt>null</tt> overrides
	 * the value from <tt>currentCell</tt>.
	 *
	 * @param currentCell The reference address.
	 * @param address All fields that are different from the reference address.
	 */
	public PriceCoordinate(IPriceCoordinate currentCell, IPriceCoordinate address)
	{
		this.priceConfig = currentCell.getPriceConfig();

		this.customerGroupPK =
				address.getCustomerGroupPK() != null ?
						address.getCustomerGroupPK() : currentCell.getCustomerGroupPK();
		this.tariffPK =
				address.getTariffPK() != null ?
						address.getTariffPK() : currentCell.getTariffPK();
		this.currencyID =
				address.getCurrencyID() != null ?
						address.getCurrencyID() : currentCell.getCurrencyID();
	}

//	/**
//	 * Every parameter - except <tt>priceCoordinate</tt> can be <tt>null</tt>. If a parameter
//	 * is null, the value from <tt>priceCoordinate</tt> is taken - otherwise the given value
//	 * overrides.
//	 *
//	 * @param priceCoordinate
//	 * @param customerGroupPK
//	 * @param tariffPK
//	 * @param currencyID
//	 */
//	public PriceCoordinate(IPriceCoordinate priceCoordinate,
//			String customerGroupPK, String tariffPK,
//			String currencyID)
//	{
//		this.priceConfig = priceCoordinate.getPriceConfig();
//
//		this.customerGroupPK = customerGroupPK != null ? customerGroupPK : priceCoordinate.getCustomerGroupPK();
//		this.tariffPK = tariffPK != null ? tariffPK : priceCoordinate.getTariffPK();
//		this.currencyID = currencyID != null ? currencyID : priceCoordinate.getCurrencyID();
//	}

	public PriceCoordinate(
			PriceConfig priceConfig,
			CustomerGroup customerGroup,
			Tariff tariff, Currency currency)
	{
		this.priceConfig = priceConfig;
		
		this.customerGroupPK = customerGroup.getPrimaryKey();
		this.tariffPK = tariff.getPrimaryKey();
		this.currencyID = currency.getCurrencyID();
	}

	/**
	 * This method ignores the member <tt>PriceCoordinate.priceConfig</tt> <strong>and
	 * the primary key</strong> to allow
	 * cross-PriceConfig-addressing of cells using <tt>PriceCoordinate</tt> instances.
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof IPriceCoordinate))
			return false;

		IPriceCoordinate other = (IPriceCoordinate)obj;

		return
				this.customerGroupPK.equals(other.getCustomerGroupPK()) &&
				this.tariffPK.equals(other.getTariffPK()) &&
				this.currencyID.equals(other.getCurrencyID());
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	protected transient String thisString = null;

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		if (thisString == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(this.getClass().getName());
			sb.append('{');
			sb.append(this.customerGroupPK);
			sb.append(',');
			sb.append(this.tariffPK);
			sb.append(',');
			sb.append(this.currencyID);
			sb.append('}');
			thisString = sb.toString();
		}
		return thisString;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	protected transient int thisHashCode = 0;

	/**
	 * This method ignores the member <tt>PriceCoordinate.priceConfig</tt> <strong>and the
	 * primary key</strong> to allow
	 * cross-PriceConfig-addressing of cells using <tt>PriceCoordinate</tt> instances.
	 *
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		if (thisHashCode == 0)
			thisHashCode = toString().hashCode();
		return thisHashCode;
	}

	public String getCurrencyID()
	{
		return currencyID;
	}

	public String getCustomerGroupPK()
	{
		return customerGroupPK;
	}

	public PriceConfig getPriceConfig()
	{
		return priceConfig;
	}

	public String getTariffPK()
	{
		return tariffPK;
	}

	public void setCurrencyID(String currencyID)
	{
		this.currencyID = currencyID;
		thisString = null;
		thisHashCode = 0;
	}

	public void setCustomerGroupPK(String customerGroupPK)
	{
		this.customerGroupPK = customerGroupPK;
		thisString = null;
		thisHashCode = 0;
	}

	public void setTariffPK(String tariffPK)
	{
		this.tariffPK = tariffPK;
		thisString = null;
		thisHashCode = 0;
	}

	/**
	 * @see javax.jdo.listener.StoreCallback#jdoPreStore()
	 */
	public void jdoPreStore()
	{
		if (priceConfig == null)
			throw new IllegalStateException("The field 'priceConfig' is null! This means, this PriceCoordinate has only been created on the fly for calculation reasons. How the hell did it come here? I cannot persist it!");

		if (organisationID == null || priceCoordinateID < 0) {
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			Accounting accounting = Accounting.getAccounting(pm);
			this.organisationID = accounting.getOrganisationID();
			this.priceCoordinateID = accounting.createPriceCoordinateID();
		}
	}
}
