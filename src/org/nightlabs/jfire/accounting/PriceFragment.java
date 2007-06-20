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


/**
 * PriceFragments are optional, that means the sum of all priceFragments is usually
 * less or equal the total price. It may theoretically happen that a fragment
 * is negative and therefore the sum of the known fragments might be greater
 * than the total price.
 * 
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.PriceFragmentID"
 *		detachable="true"
 *		table="JFireTrade_PriceFragment"
 *
 * @jdo.inheritance strategy = "new-table"
 * 
 * @jdo.create-objectid-class
 *		field-order="organisationID, priceConfigID, priceID, priceFragmentTypePK"
 *
 * @jdo.fetch-group name="PriceFragment.price" fields="price"
 * @jdo.fetch-group name="PriceFragment.currency" fields="currency"
 * @jdo.fetch-group name="PriceFragment.priceFragmentType" fields="priceFragmentType"
 * @jdo.fetch-group name="PriceFragment.this" fields="priceFragmentType, price, currency"
 *
 * @jdo.fetch-group name="Price.fragments" fields="price"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="price, currency, priceFragmentType"
 */
public class PriceFragment
	implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_PRICE = "PriceFragment.price";
	public static final String FETCH_GROUP_CURRENCY = "PriceFragment.currency";
	public static final String FETCH_GROUP_PRICE_FRAGMENT_TYPE = "PriceFragment.priceFragmentType";
	public static final String FETCH_GROUP_THIS_PRICE_FRAGMENT = "PriceFragment.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long priceConfigID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long priceID;

//	/**
//	 * @jdo.field primary-key="true"
//	 * @jdo.column length="100"
//	 */
//	private String priceFragmentTypeOrganisationID;
//	
//	/**
//	 * @jdo.field primary-key="true"
//	 * @jdo.column length="100"
//	 */
//	private String priceFragmentTypeID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="201"
	 */
	private String priceFragmentTypePK;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceFragmentType priceFragmentType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Price price;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Currency currency;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long amount = 0;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean virtual = false;
	
	protected PriceFragment() { }

	public PriceFragment(Price price, PriceFragmentType priceFragmentType)
	{
		this.price = price;
		this.organisationID = price.getOrganisationID();
		this.priceConfigID = price.getPriceConfigID();
		this.priceID = price.getPriceID();
		this.currency = price.getCurrency();
//		this.priceFragmentTypeOrganisationID = priceFragmentType.getOrganisationID();
//		this.priceFragmentTypeID = priceFragmentType.getPriceFragmentTypeID();
		this.priceFragmentType = priceFragmentType;
		this.priceFragmentTypePK = this.priceFragmentType.getPrimaryKey();
	}

	public PriceFragment(Price price, PriceFragment origPriceFragment)
	{
		this.price = price;
		this.organisationID = price.getOrganisationID();
		this.priceConfigID = price.getPriceConfigID();
		this.priceID = price.getPriceID();
		this.currency = price.getCurrency();
//		this.priceFragmentTypeOrganisationID = origPriceFragment.priceFragmentTypeOrganisationID;
//		this.priceFragmentTypeID = origPriceFragment.getPriceFragmentTypeID();
		this.priceFragmentType = origPriceFragment.getPriceFragmentType();
		this.priceFragmentTypePK = this.priceFragmentType.getPrimaryKey();
		this.amount = origPriceFragment.getAmount();
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the priceConfigID.
	 */
	public long getPriceConfigID()
	{
		return priceConfigID;
	}
	/**
	 * @return Returns the priceID.
	 */
	public long getPriceID()
	{
		return priceID;
	}
//	/**
//	 * @return Returns the priceFragmentTypeOrganisationID.
//	 */
//	public String getPriceFragmentTypeOrganisationID()
//	{
//		return priceFragmentTypeOrganisationID;
//	}
//	/**
//	 * @return Returns the priceFragmentTypeID.
//	 */
//	public String getPriceFragmentTypeID()
//	{
//		return priceFragmentTypeID;
//	}
	/**
	 * @return Returns the priceFragmentType.
	 */
	public PriceFragmentType getPriceFragmentType()
	{
		return priceFragmentType;
	}
	public String getPriceFragmentTypePK()
	{
		return priceFragmentTypePK;
	}
	/**
	 * @return Returns the amount.
	 */
	public long getAmount()
	{
		return amount;
	}
//	/**
//	 * @return Returns the amount.
//	 */
//	public long getAmountAbsoluteValue()
//	{
//		return Math.abs(amount);
//	}

	/**
	 * @param amount The amount to set.
	 */
	public void setAmount(long amount)
	{
		if (PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID.equals(priceFragmentType.getOrganisationID()) &&
		    PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID.equals(priceFragmentType.getPriceFragmentTypeID()))
			this.price._setAmount(amount);

		this.amount = amount;
	}

	public Currency getCurrency() {
		return currency;
	}
	

	public Price getPrice() {
		return price;
	}
	
	public boolean isVirtual() {
		return virtual;
	}
	
	void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}
}
