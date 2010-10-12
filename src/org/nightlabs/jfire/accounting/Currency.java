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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

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

	public Currency(final String currencyID, final String currencySymbol, final int decimalDigitCount) {
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
	public void setCurrencySymbol(final String currencySymbol)
	{
		this.currencySymbol = currencySymbol;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof Currency)) return false;
		final Currency o = (Currency) obj;
		return Util.equals(o.currencyID, this.currencyID);
	}

	@Override
	public String toString() {
		return super.toString() + '[' + currencyID + ']';
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(currencyID);
	}

	@Override
	public double toDouble(final long amount) {
		return amount / pow10(getDecimalDigitCount());
	}

	private static int pow10(final int decimalDigitCount)
	{
		final BigInteger pow10 = BigInteger.valueOf(10L).pow(decimalDigitCount);
		if (pow10.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0)
			throw new IllegalArgumentException("Result too big; it exceeds the integer range!!!");

		return pow10.intValue();
	}

	@Override
	public long toLong(final double amount) {
		return Math.round(amount * pow10(getDecimalDigitCount()));
		// 2010-02-03: Switched from (long)(...) cast to Math.round(...), because the following code
		// otherwise produces a wrong result (4079 instead of 4080). Marco.
		//				public static void main(String[] args) {
		//					BigDecimal a = new BigDecimal(10200).setScale(2);
		//					BigDecimal b = a.divide(new BigDecimal(250));
		//					System.out.println(b);
		//					double d = b.doubleValue();
		//					System.out.println(d);
		//
		//					long l = (long) (d * Math.pow(10, 2));
		//					System.out.println(l);
		//
		//					l = (long) (d * 100);
		//					System.out.println(l);
		//				}
	}

	@Override
	public BigDecimal toBigDecimal(final long amount)
	{
		return new BigDecimal(amount).divide(BigDecimal.valueOf(pow10(getDecimalDigitCount())), getDecimalDigitCount(), RoundingMode.HALF_EVEN);
	}

	@Override
	public long toLong(final BigDecimal amount)
	{
		return amount.multiply(BigDecimal.valueOf(pow10(getDecimalDigitCount()))).round(new MathContext(0, RoundingMode.HALF_EVEN)).longValueExact();
	}
}
