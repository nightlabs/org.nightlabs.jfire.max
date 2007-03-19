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

import org.nightlabs.util.Utils;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.accounting.id.CurrencyID"
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
	private static final long serialVersionUID = 1L;

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

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof Currency)) return false;
		Currency o = (Currency) obj;
		return Utils.equals(o.currencyID, this.currencyID);
	}

	@Override
	public int hashCode()
	{
		return Utils.hashCode(currencyID);
	}
}
