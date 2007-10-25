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

import java.util.Date;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.pay.PaymentData"
 *		detachable="true"
 *		table="JFireTrade_PaymentDataBankTransferGermany"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class PaymentDataBankTransferGermany extends PaymentData
{
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date moneyInDT = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date moneyOutDT = null;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PaymentDataBankTransferGermany() { }

	/**
	 * @param payment
	 */
	public PaymentDataBankTransferGermany(Payment payment)
	{
		super(payment);
	}

	/**
	 * @return Returns the moneyInDT.
	 */
	public Date getMoneyInDT()
	{
		return moneyInDT;
	}
	/**
	 * @param moneyInDT The moneyInDT to set.
	 */
	public void setMoneyInDT(Date arrivalDT)
	{
		this.moneyInDT = arrivalDT;
	}
	/**
	 * @return Returns the moneyOutDT.
	 */
	public Date getMoneyOutDT()
	{
		return moneyOutDT;
	}
	/**
	 * @param moneyOutDT The moneyOutDT to set.
	 */
	public void setMoneyOutDT(Date moneyOutDT)
	{
		this.moneyOutDT = moneyOutDT;
	}
}
