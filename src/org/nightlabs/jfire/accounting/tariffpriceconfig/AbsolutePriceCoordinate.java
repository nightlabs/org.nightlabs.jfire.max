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

package org.nightlabs.jfire.accounting.tariffpriceconfig;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.CustomerGroup;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class AbsolutePriceCoordinate extends PriceCoordinate implements IAbsolutePriceCoordinate
{
	private String productTypePK;
	private String priceFragmentTypePK;

	protected AbsolutePriceCoordinate()
	{
	}

//	/**
//	 * Every parameter - except <tt>priceCoordinate</tt> can be <tt>null</tt>. If a parameter
//	 * is null, the value from <tt>priceCoordinate</tt> is taken - otherwise the given value
//	 * overrides.
//	 *
//	 * @param priceCoordinate
//	 * @param customerGroupPK
//	 * @param tariffPK
//	 * @param currencyID
//	 * @param productTypePK
//	 * @param priceFragmentTypeID
//	 * 
//	 * @deprecated
//	 */
//	public AbsolutePriceCoordinate(
//			IAbsolutePriceCoordinate priceCoordinate,
//			String customerGroupPK, String tariffPK,
//			String currencyID,
//			String productTypePK, String priceFragmentTypePK)
//	{
//		super(priceCoordinate, customerGroupPK, tariffPK,
//				currencyID);
//		this.productTypePK = productTypePK != null ? productTypePK : priceCoordinate.getProductTypePK();
//		this.priceFragmentTypePK = priceFragmentTypePK != null ? priceFragmentTypePK : priceCoordinate.getPriceFragmentTypePK();
//	}
	/**
	 * @deprecated This constructor should not be used in java code! An instance
	 * created by it, cannot be persisted into the database! It is
	 * only intended for usage as address in javascript formulas! Though it's
	 * marked as deprecated, it will NOT vanish. Deprecation is only set to make you
	 * aware of the special function of this constructor. 
	 *
	 * @param customerGroupPK Either <tt>null</tt> (which means the same <tt>CustomerGroup</tt>
	 *		as the current cell's location) or the PK of another cell's location
	 *		(see {@link CustomerGroup#getPrimaryKey()}).
	 * @param tariffPK Either <tt>null</tt> (which means the same <tt>Tariff</tt>
	 *		as the current cell's location) or the PK of another cell's location
	 *		(see {@link Tariff#getPrimaryKey()}).
	 * @param currencyID Either <tt>null</tt> (which means the same <tt>Currency</tt>
	 *		as the current cell's location) or the PK of another cell's location
	 *		(see {@link Currency#getCurrencyID()}).
	 *
	 * @param productTypePK Either <tt>null</tt> (which means the same <tt>ProductType</tt>
	 *		as the current cell's location) or the PK of another cell's location
	 *		(see {@link ProductType#getPrimaryKey()}).
	 * @param priceFragmentTypePK Either <tt>null</tt> (which means the same <tt>PriceFragmentType</tt>
	 *		as the current cell's location) or the PK of another cell's location
	 *		(see {@link PriceFragmentType#getPrimaryKey()}).
	 */
	public AbsolutePriceCoordinate(
			String customerGroupPK, String tariffPK, String currencyID,
			String productTypePK, String priceFragmentTypePK)
	{
		super(customerGroupPK, tariffPK, currencyID);
		this.productTypePK = productTypePK;
		this.priceFragmentTypePK = priceFragmentTypePK;
	}

	public AbsolutePriceCoordinate(
			IAbsolutePriceCoordinate currentCell, IAbsolutePriceCoordinate address)
	{
		super(currentCell, address);
		this.productTypePK =
			address.getProductTypePK() != null ?
					address.getProductTypePK() : currentCell.getProductTypePK();
	this.priceFragmentTypePK =
			address.getPriceFragmentTypePK() != null ?
					address.getPriceFragmentTypePK() : currentCell.getPriceFragmentTypePK();
	}

	public AbsolutePriceCoordinate(
			IPriceCoordinate priceCoordinate, ProductType productType,
			PriceFragmentType priceFragmentType)
	{
		super(priceCoordinate);
		this.productTypePK = productType.getPrimaryKey();
		this.priceFragmentTypePK = priceFragmentType.getPrimaryKey();
	}

	/**
	 * @param priceConfig
	 * @param customerGroup
	 * @param saleMode
	 * @param tariff
	 * @param category
	 * @param currency
	 * @param productInfo
	 * @param priceFragmentType
	 */
	public AbsolutePriceCoordinate(PriceConfig priceConfig,
			CustomerGroup customerGroup, Tariff tariff,
			Currency currency, ProductType productType,
			PriceFragmentType priceFragmentType)
	{
		super(priceConfig, customerGroup, tariff, currency);
		this.productTypePK = productType.getPrimaryKey();
		this.priceFragmentTypePK = priceFragmentType.getPrimaryKey();
	}

	/**
	 * @return Returns the priceFragmentTypePK.
	 */
	public String getPriceFragmentTypePK()
	{
		return priceFragmentTypePK;
	}
	/**
	 * @return Returns the productTypePK.
	 */
	public String getProductTypePK()
	{
		return productTypePK;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		if (thisString == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(this.getClass().getName());
			sb.append('{');
			sb.append(this.getCustomerGroupPK());
			sb.append(',');
			sb.append(this.getTariffPK());
			sb.append(',');
			sb.append(this.getCurrencyID());
			sb.append(',');
			sb.append(this.productTypePK);
			sb.append(',');
			sb.append(this.priceFragmentTypePK);
			sb.append('}');
			thisString = sb.toString();
		}
		return thisString;
	}

	/**
	 * @see org.nightlabs.jfire.ticketing.accounting.PriceCoordinate#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof IAbsolutePriceCoordinate))
			return false;

		if (!super.equals(obj))
			return false;

		AbsolutePriceCoordinate other = (AbsolutePriceCoordinate)obj;
		return
				this.productTypePK.equals(other.productTypePK) &&
				this.priceFragmentTypePK.equals(other.priceFragmentTypePK);
	}
}
