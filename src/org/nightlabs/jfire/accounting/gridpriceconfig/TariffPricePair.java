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

package org.nightlabs.jfire.accounting.gridpriceconfig;

import java.io.Serializable;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.Tariff;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class TariffPricePair
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Tariff tariff;
	private Price price;

	public TariffPricePair()
	{
	}
	
	public TariffPricePair(Tariff tariff, Price price)
	{
		this.tariff = tariff;
		this.price = price;
	}

	/**
	 * @return Returns the price.
	 */
	public Price getPrice()
	{
		return price;
	}
//	/**
//	 * @param price The price to set.
//	 */
//	public void setPrice(Price price)
//	{
//		this.price = price;
//	}
	/**
	 * @return Returns the tariff.
	 */
	public Tariff getTariff()
	{
		return tariff;
	}
//	/**
//	 * @param tariff The tariff to set.
//	 */
//	public void setTariff(Tariff tariff)
//	{
//		this.tariff = tariff;
//	}
}
