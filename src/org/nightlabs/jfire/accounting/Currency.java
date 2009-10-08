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

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.l10n.NumberFormatter;
import org.nightlabs.util.Util;

/**
 * An instance of this class represents a <a href="http://en.wikipedia.org/wiki/Currency">currency</a>
 * used for money exchanges. A currency is a measurement unit for money. It is used together with a numeric
 * value (e.g. 5) to represent a specific monetary amount (e.g. 5 EUR or 5 $US).
 * <p>
 * In JFire a monetary value is always stored as <code>long</code> (in contrast to a floating point number) in order to
 * prevent rounding errors. As most currencies have a fractional part (e.g. 1 Euro has 100 cents), though,
 * {@link Currency} defines the {@link #getDecimalDigitCount() number of decimal digits}. For example, the
 * currency "EUR" has 2 decimal digits and therefore a value of 1000 in the database means 10.00 EUR.
 * </p>
 * <p>
 * You can transform the <code>long</code> value of a money-amount to a floating point value by using {@link #toDouble(long)}.
 * For the opposite direction, you can use {@link #toLong(double)}.
 * </p>
 * <p>
 * To display a monetary value, you should use {@link NumberFormatter#formatCurrency(long, org.nightlabs.l10n.Currency)}
 * (or one of the other overloaded <code>formatCurrency(...)</code> methods).
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author vince - vince at guinaree dot com
 */
@PersistenceCapable(
		objectIdClass=CurrencyID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireTrade_Currency"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Currency
implements Serializable, org.nightlabs.l10n.Currency
{
	private static final long serialVersionUID = 1L;

	/**
	 * This is the ISO 4217 identifier for the currency. Usually a two or three-letter-abbreviation.
	 */
	@PrimaryKey
	@Column(length=100)
	private String currencyID;

	@Persistent(
			nullValue=NullValue.EXCEPTION,
			persistenceModifier=PersistenceModifier.PERSISTENT
	)
	private String currencySymbol;

	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private int decimalDigitCount = -1;

	protected Currency() { }

	public Currency(String currencyID, String currencySymbol, int decimalDigitCount) {
		if (currencyID == null)
			throw new IllegalArgumentException("currencyID must not be null!");

		if (currencySymbol == null)
			throw new IllegalArgumentException("currencySymbol must not be null!");

		if (decimalDigitCount < 0)
			throw new IllegalArgumentException("decimalDigitCount must be >= 0! It is: " + decimalDigitCount);

		this.currencyID = currencyID;
		this.currencySymbol = currencySymbol;
		this.decimalDigitCount = decimalDigitCount;
	}

	@Override
	public String getCurrencyID()
	{
		return currencyID;
	}

	public void setDecimalDigitCount(int decimalDigitCount) {
		this.decimalDigitCount = decimalDigitCount;
	}

	@Override
	public int getDecimalDigitCount()
	{
		return decimalDigitCount;
	}

	@Override
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

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof Currency)) return false;
		Currency o = (Currency) obj;
		return Util.equals(o.currencyID, this.currencyID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(currencyID);
	}

	/**
	 * Returns the given amount in the double value of this currency.
	 * <p>
	 *   amount / 10^(decimalDigitCount)
	 * <p>
	 *
	 * @param amount The amount to convert
	 * @return the approximate value as double - there might be rounding differences.
	 */
	public double toDouble(long amount) {
		return amount / Math.pow(10, getDecimalDigitCount());
	}

	/**
	 * Convert the given amount to the long value of this currency.
	 * <p>
	 *   amount * 10^(decimalDigitCount)
	 * <p>
	 *
	 * @param amount The amount to convert
	 * @return the approximate value as long - there might be rounding differences.
	 */
	public long toLong(double amount) {
		return (long)(amount * Math.pow(10, getDecimalDigitCount()));
	}
}
