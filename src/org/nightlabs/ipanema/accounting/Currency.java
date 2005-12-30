/*
 * Created on 27.10.2004
 */
package org.nightlabs.ipanema.accounting;

import java.io.Serializable;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.ipanema.accounting.id.CurrencyID"
 *		detachable = "true"
 *		table="JFireTrade_Currency"
 *
 * @jdo.create-objectid-class
 *
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class Currency
	implements Serializable, org.nightlabs.l10n.Currency
{
	/**
	 * This is the ISO or whatever standard for the currency. Usually a two or three-letter-abbreviation.
	 *
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String currencyID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String currencySymbol;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int decimalDigitCount = -1;

	protected Currency() { }

	public Currency(String currencyID, String currencySymbol, int decimalDigitCount) {
		if (currencyID == null)
			throw new NullPointerException("currencyID must not be null!");

		this.currencyID = currencyID;
		this.currencySymbol = currencySymbol;
		this.decimalDigitCount = decimalDigitCount;
	}

	/**
	 * @return Returns the currencyID.
	 */
	public String getCurrencyID()
	{
		return currencyID;
	}

	/**
	 * @see org.nightlabs.l10n.Currency#getDecimalDigitCount()
	 */
	public int getDecimalDigitCount()
	{
		return decimalDigitCount;
	}

	/**
	 * @see org.nightlabs.l10n.Currency#getCurrencySymbol()
	 */
	public String getCurrencySymbol()
	{
		return currencySymbol;
	}
	/**
	 * @param currencySymbol The currencySymbol to set.
	 */
	public void setCurrencySymbol(String currencySymbol)
	{
		this.currencySymbol = currencySymbol;
	}
}
