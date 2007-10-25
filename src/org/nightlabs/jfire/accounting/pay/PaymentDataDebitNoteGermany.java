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

package org.nightlabs.jfire.accounting.pay;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.pay.PaymentData"
 *		detachable="true"
 *		table="JFireTrade_PaymentDataDebitNoteGermany"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class PaymentDataDebitNoteGermany extends PaymentData
{
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String accountHolderName;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long bankCode;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long accountNumber;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PaymentDataDebitNoteGermany() { }

	/**
	 * @param payment
	 */
	public PaymentDataDebitNoteGermany(Payment payment)
	{
		super(payment);
	}

	/**
	 * @return Returns the accountHolderName.
	 */
	public String getAccountHolderName()
	{
		return accountHolderName;
	}
	/**
	 * @param accountHolderName The accountHolderName to set.
	 */
	public void setAccountHolderName(String accountHolderName)
	{
		this.accountHolderName = accountHolderName;
	}
	/**
	 * @return Returns the accountNumber.
	 */
	public long getAccountNumber()
	{
		return accountNumber;
	}
	/**
	 * @param accountNumber The accountNumber to set.
	 */
	public void setAccountNumber(long accountNumber)
	{
		if (bankCode < 0L || bankCode > 9999999999L)
			throw new IllegalArgumentException("accountNumber must be a positive number having less than or exactly 10 digits (i.e. 0 <= accountNumber <= 9'999'999'999)!");

		this.accountNumber = accountNumber;
	}
	/**
	 * @return Returns the bankCode.
	 */
	public long getBankCode()
	{
		return bankCode;
	}
	/**
	 * @param bankCode The bankCode to set.
	 */
	public void setBankCode(long bankCode)
	{
		if (bankCode < 10000000L || bankCode > 99999999L)
			throw new IllegalArgumentException("bankCode must be a positive number having exactly 8 digits (i.e. between 10'000'000 and 99'999'999)!");

		this.bankCode = bankCode;
	}

}
