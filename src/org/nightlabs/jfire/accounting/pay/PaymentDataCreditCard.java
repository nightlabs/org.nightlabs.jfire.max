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

import java.util.Calendar;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.pay.PaymentData"
 *		detachable="true"
 *		table="JFireTrade_PaymentDataCreditCard"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class PaymentDataCreditCard
extends PaymentData
{
	private static final long serialVersionUID = 1L;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String nameOnCard = null;
////	/**
////	 * @jdo.field persistence-modifier="persistent"
////	 */
////	private String firstNameOnCard = null;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String cardNumber = null;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String cvc = null;

	/**
	 * year in yyyy form, e.g. 2005
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int expiryYear = -1;

	/**
	 * month of year from 1 to 12.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int expiryMonth = -1;

	/**
	 * @deprecated Only of JDO!
	 */
	@Deprecated
	protected PaymentDataCreditCard()
	{
	}

	public PaymentDataCreditCard(Payment payment)
	{
		super(payment);
	}
	/**
	 * @return Returns the cardNumber.
	 */
	public String getCardNumber()
	{
		return cardNumber;
	}
	/**
	 * @param cardNumber The cardNumber to set.
	 */
	public void setCardNumber(String cardNumber)
	{
		this.cardNumber = cardNumber;
	}
	/**
	 * @return Returns the cvc.
	 */
	public String getCvc()
	{
		return cvc;
	}
	/**
	 * @param cvc The cvc to set.
	 */
	public void setCvc(String cvc)
	{
		this.cvc = cvc;
	}
	/**
	 * @return Returns the expiryMonth.
	 */
	public int getExpiryMonth()
	{
		return expiryMonth;
	}
	/**
	 * @param expiryMonth The expiryMonth to set.
	 */
	public void setExpiryMonth(int expiryMonth)
	{
		this.expiryMonth = expiryMonth;
	}
	/**
	 * @return Returns the expiryYear.
	 */
	public int getExpiryYear()
	{
		return expiryYear;
	}
	/**
	 * @param expiryYear The expiryYear to set.
	 */
	public void setExpiryYear(int expiryYear)
	{
		this.expiryYear = expiryYear;
	}
//	/**
//	 * @return Returns the firstNameOnCard.
//	 */
//	public String getFirstNameOnCard()
//	{
//		return firstNameOnCard;
//	}
//	/**
//	 * @param firstNameOnCard The firstNameOnCard to set.
//	 */
//	public void setFirstNameOnCard(String firstNameOnCard)
//	{
//		this.firstNameOnCard = firstNameOnCard;
//	}
	/**
	 * @return Returns the nameOnCard.
	 */
	public String getNameOnCard()
	{
		return nameOnCard;
	}
	/**
	 * @param nameOnCard The nameOnCard to set.
	 */
	public void setNameOnCard(String nameOnCard)
	{
		this.nameOnCard = nameOnCard;
	}
	
	/**
	 * @see org.nightlabs.jfire.accounting.pay.PaymentData#init()
	 */
	@Override
	public void init()
	{
		if (nameOnCard == null)
			nameOnCard = "";

//		if (firstNameOnCard == null)
//			firstNameOnCard = "";

		if (cardNumber == null)
			cardNumber = "";

		if (cvc == null)
			cvc = "";

		if (expiryYear < 0)
			expiryYear = Calendar.getInstance().get(Calendar.YEAR);

		if (expiryMonth < 0)
			expiryMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
	}
}
