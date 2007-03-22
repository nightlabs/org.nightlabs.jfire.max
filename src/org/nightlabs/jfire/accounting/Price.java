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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.PriceID"
 *		detachable="true"
 *		table="JFireTrade_Price"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class
 *		field-order="organisationID, priceConfigID, priceID"
 *
 * @jdo.fetch-group name="Price.currency" fields="currency"
 * @jdo.fetch-group name="Price.fragments" fields="fragments"
 * @jdo.fetch-group name="Price.this" fetch-groups="default" fields="currency, fragments"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="currency, fragments"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default" fields="currency"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default" fields="currency"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default" fields="currency"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default" fields="currency"
 */
public class Price
	implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_CURRENCY = "Price.currency";
	public static final String FETCH_GROUP_FRAGMENTS = "Price.fragments";
	public static final String FETCH_GROUP_THIS_PRICE = "Price.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long priceConfigID = -1;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long priceID = -1;
	/////// end PK /////


	/////// begin normal fields ///////
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Currency currency;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long amount = 0;

	/**
	 * key: String priceFragmentTypePK<br/>
	 * value: PriceFragment priceFragment
	 * <p>
	 * A total price can contain various fragments that need to be known for
	 * legal reasons. E.g. the VAT is managed as a fragment with a certain key
	 * like "vat-de-16" or "vat-de-7". Other data that one trade partner
	 * wants/allows the other to be known might be transferred in here as well
	 * E.g. the system fees might be declared like this. The price fragments
	 * declared here are used by an Accountant to book the right amounts on the
	 * right Accounts.
	 * <p>
	 * Note, that PriceFragments can overlap or be incomplete and therefore their sum is NOT
	 * the <tt>Price.amount</tt> (or only by accident).
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="PriceFragment"
	 *		dependent-value="true"
	 *		mapped-by="price"
	 *
	 * @jdo.key mapped-by="priceFragmentTypePK"
	 */
	protected Map<String, PriceFragment> fragments;

	/////// end normal fields ////////

	/**
	 * @deprecated Only for JDO!
	 */
	protected Price() { }

	/**
	 * This constructor is used to create a price within a TariffPrice object
	 * in the general price grid.
	 * 
	 * @param tariffPrice
	 * @param currency
	 */
	public Price(String organisationID, long priceConfigID, long priceID, Currency currency)
	{
		this.organisationID = organisationID;
		this.priceConfigID = priceConfigID;
		this.priceID = priceID;
		if (currency == null)
			throw new NullPointerException("currency");
		this.currency = currency;
		this.fragments = new HashMap<String, PriceFragment>();
	}
	
	public static String getPrimaryKey(String organisationID, long priceConfigID, long priceID)
	{
		return organisationID + '/' + Long.toHexString(priceConfigID) + '/' + Long.toHexString(priceID);
	}

	public String getPrimaryKey()
	{
		return getPrimaryKey(organisationID, priceConfigID, priceID);
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
	/**
	 * @return Returns the currency.
	 */
	public Currency getCurrency()
	{
		return currency;
	}

	/**
	 * @return Returns a Collection of PriceFragment.
	 */
	public Collection getFragments()
	{
		return fragments.values();
	}

	/**
	 * @return Returns the amount.
	 */
	public long getAmount()
	{
		return amount;
	}
	/**
	 * @param amount The amount to set.
	 */
	public void setAmount(long amount)
	{
		PriceFragment priceFragmentTotal = getPriceFragment(
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID, PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID, false);

		if (priceFragmentTotal == null) {
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			if (pm == null)
				throw new IllegalStateException("This instance of Price ("+getPrimaryKey()+") does neither have a PriceFragment for the PriceFragmentType \"_Total_\" nor is it attached to a datastore (=> cannot lookup the PriceFragmentType). You must either use setAmount(PriceFragmentType, long) or call this method when the PriceFragment already exists or when this Price is currently persistent (connected to the datastore).");

			PriceFragmentType pftTotal = (PriceFragmentType) pm.getObjectById(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL);
			priceFragmentTotal = createPriceFragment(pftTotal);
		}

		priceFragmentTotal.setAmount(amount);
	}

	/**
	 * This method is called by {@link PriceFragment#setAmount(long)}, if it represents the total price.
	 */
	protected void _setAmount(long amount)
	{
		this.amount = amount;
	}
	
	/**
	 * Returns the mathematical absolute value of this price's amount. 
	 */
	public long getAmountAbsoluteValue() {
		return Math.abs(amount);
	}

	public PriceFragment getPriceFragment(
			String priceFragmentTypePK, boolean throwExceptionIfNotExistent)
	{
		PriceFragment fragment = (PriceFragment) fragments.get(priceFragmentTypePK);
		if (fragment == null && throwExceptionIfNotExistent)
			throw new IllegalArgumentException("No PriceFragment registered with priceFragmentTypePK=\""+priceFragmentTypePK+"\"!");
		return fragment;
	}

	public PriceFragment getPriceFragment(
			String priceFragmentTypeOrganisationID,
			String priceFragmentTypeID, boolean throwExceptionIfNotExistent)
	{
		PriceFragment fragment = (PriceFragment) fragments.get(
				PriceFragmentType.getPrimaryKey(priceFragmentTypeOrganisationID, priceFragmentTypeID));
		if (fragment == null && throwExceptionIfNotExistent)
			throw new IllegalArgumentException("No PriceFragment registered with priceFragmentTypeOrganisationID=\""+priceFragmentTypeOrganisationID+"\", priceFragmentTypeID=\""+priceFragmentTypeID+"\"!");
		return fragment;
	}

	public PriceFragment createPriceFragment(PriceFragmentType priceFragmentType)
	{
		if (priceFragmentType == null)
			throw new NullPointerException("priceFragmentType");

		PriceFragment fragment = (PriceFragment) fragments.get(priceFragmentType.getPrimaryKey());
		if (fragment == null) {
			fragment = new PriceFragment(this, priceFragmentType);
			fragments.put(priceFragmentType.getPrimaryKey(), fragment);
		}
		return fragment;
	}

	public long getAmount(PriceFragmentType priceFragmentType)
	{
		return getAmount(priceFragmentType.getOrganisationID(), priceFragmentType.getPriceFragmentTypeID());
	}
	public long getAmount(String priceFragmentTypePK)
	{
		if (priceFragmentTypePK == null ||
				PriceFragmentType.getPrimaryKey(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID, PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID).equals(priceFragmentTypePK))
			return amount;

		PriceFragment priceFragment = getPriceFragment(priceFragmentTypePK, false);
		if (priceFragment != null)
			return priceFragment.getAmount();

		return 0;
	}

	protected void addPriceFragment(PriceFragment priceFragment)
	{
		fragments.put(priceFragment.getPriceFragmentTypePK(), priceFragment);
	}

	/**
	 * @return Returns the amount.
	 */
	public long getAmount(String priceFragmentTypeOrganisationID, String priceFragmentTypeID)
	{
		if (priceFragmentTypeOrganisationID != null && priceFragmentTypeID == null)
			throw new IllegalArgumentException("priceFragmentTypeOrganisationID is not null, but priceFragmentTypeID is null! Either none or both must be null!");

		if (priceFragmentTypeOrganisationID == null && priceFragmentTypeID != null)
			throw new IllegalArgumentException("priceFragmentTypeOrganisationID is null, but priceFragmentTypeID is not null! Either none or both must be null!");

		if (priceFragmentTypeID == null || (
						PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID.equals(priceFragmentTypeOrganisationID) &&
						PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID.equals(priceFragmentTypeID)))
			return amount;

		PriceFragment priceFragment = getPriceFragment(priceFragmentTypeOrganisationID, priceFragmentTypeID, false);
		if (priceFragment != null)
			return priceFragment.getAmount();

		return 0;
	}
	/**
	 * @param amount The amount to set.
	 */
	public void setAmount(PriceFragmentType priceFragmentType, long amount)
	{
		// the PriceFragment for the _Total_ PriceFragmentType will call _setAmount(...) here
		PriceFragment priceFragment = createPriceFragment(priceFragmentType);
		priceFragment.setAmount(amount);
	}

	public void clearFragments()
	{
		fragments.clear();
	}
	/**
	 * This method finds the local PriceFragment with the same priceFragmentID and
	 * adds the amount of the given PriceFragment to it. If there is no local PriceFragment
	 * existing for the given PriceFragmentType, it will be created.
	 *
	 * @param priceFragment
	 */
	public void sumPriceFragment(PriceFragment priceFragment)
	{
		PriceFragmentType priceFragmentType = priceFragment.getPriceFragmentType();
		String priceFragmentTypePK = priceFragmentType.getPrimaryKey();
		PriceFragment localPriceFragment = (PriceFragment) fragments.get(priceFragmentTypePK);
		if (localPriceFragment == null) {
			localPriceFragment = new PriceFragment(this, priceFragmentType);
			fragments.put(priceFragmentTypePK, localPriceFragment);
		}
		localPriceFragment.setAmount(localPriceFragment.getAmount() + priceFragment.getAmount());
	}
	
	public void sumPrice(Price price)
	{
		this.amount += price.getAmount();
		for (Iterator it = price.getFragments().iterator(); it.hasNext(); ) {
			PriceFragment fragment = (PriceFragment) it.next();
			sumPriceFragment(fragment);
		}
	}
}
