/*
 * Created on 30.10.2004
 */
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
 *		field-order="organisationID, priceConfigID, priceID, priceFragmentTypeOrganisationID, priceFragmentTypeID"
 *
 * @jdo.fetch-group name="PriceFragment.price" fields="price"
 * @jdo.fetch-group name="PriceFragment.currency" fields="currency"
 * @jdo.fetch-group name="PriceFragment.priceFragmentType" fields="priceFragmentType"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="currency, priceFragmentType"
 */
public class PriceFragment
	implements Serializable
{
	public static final String FETCH_GROUP_PRICE = "Price.price";
	public static final String FETCH_GROUP_CURRENCY = "Price.currency";
	public static final String FETCH_GROUP_PRICE_FRAGMENT_TYPE = "Price.priceFragmentType";

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

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String priceFragmentTypeOrganisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String priceFragmentTypeID;

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

	protected PriceFragment() { }

	public PriceFragment(Price price, PriceFragmentType priceFragmentType)
	{
		this.price = price;
		this.organisationID = price.getOrganisationID();
		this.priceConfigID = price.getPriceConfigID();
		this.priceID = price.getPriceID();
		this.currency = price.getCurrency();
		this.priceFragmentTypeOrganisationID = priceFragmentType.getOrganisationID();
		this.priceFragmentTypeID = priceFragmentType.getPriceFragmentTypeID();
		this.priceFragmentType = priceFragmentType;
	}

	public PriceFragment(Price price, PriceFragment origPriceFragment)
	{
		this.price = price;
		this.organisationID = price.getOrganisationID();
		this.priceConfigID = price.getPriceConfigID();
		this.priceID = price.getPriceID();
		this.currency = price.getCurrency();
		this.priceFragmentTypeOrganisationID = origPriceFragment.priceFragmentTypeOrganisationID;
		this.priceFragmentTypeID = origPriceFragment.getPriceFragmentTypeID();
		this.priceFragmentType = origPriceFragment.getPriceFragmentType();
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
	/**
	 * @return Returns the priceFragmentTypeOrganisationID.
	 */
	public String getPriceFragmentTypeOrganisationID()
	{
		return priceFragmentTypeOrganisationID;
	}
	/**
	 * @return Returns the priceFragmentTypeID.
	 */
	public String getPriceFragmentTypeID()
	{
		return priceFragmentTypeID;
	}
	/**
	 * @return Returns the priceFragmentType.
	 */
	public PriceFragmentType getPriceFragmentType()
	{
		return priceFragmentType;
	}
	/**
	 * @return Returns the amount.
	 */
	public long getAmount()
	{
		return amount;
	}
	/**
	 * @return Returns the amount.
	 */
	public long getAmountAbsoluteValue()
	{
		return Math.abs(amount);
	}
	
	/**
	 * @param amount The amount to set.
	 */
	public void setAmount(long amount)
	{
		this.amount = amount;
	}

	public Currency getCurrency() {
		return currency;
	}
	

	public Price getPrice() {
		return price;
	}
	
	
}
