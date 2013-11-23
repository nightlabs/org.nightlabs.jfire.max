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

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.id.PriceFragmentTypeID;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class AbsolutePriceCoordinate extends PriceCoordinate implements IAbsolutePriceCoordinate
{
	private static final long serialVersionUID = 1L;
	private String productTypePK;
	private String priceFragmentTypePK;

	protected AbsolutePriceCoordinate()
	{
	}

	/**
	 * <strong>WARNING:</strong> When using this constructor in java code, one of the
	 * arguments <b>must</b> be the PriceConfig to which this coordinate belongs!
	 * Otherwise, it cannot be persisted into the database! Without the owning price config
	 * in the parameter list, it is only intended for usage as address in javascript formulas!
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
	public AbsolutePriceCoordinate(Object ... dimensionValues)
	{
		super(dimensionValues);

		ProductTypeID productTypeID = (ProductTypeID) getDimensionValue(dimensionValues, ProductTypeID.class, ProductType.class);
		if (productTypeID != null)
			this.productTypePK = productTypeID.getPrimaryKey();

		PriceFragmentTypeID priceFragmentTypeID = (PriceFragmentTypeID) getDimensionValue(dimensionValues, PriceFragmentTypeID.class, PriceFragmentType.class);
		if (priceFragmentTypeID != null)
			this.priceFragmentTypePK = priceFragmentTypeID.getPrimaryKey();
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

	@Override
	public String getPriceFragmentTypePK()
	{
		return priceFragmentTypePK;
	}

	@Override
	public String getProductTypePK()
	{
		return productTypePK;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName());
		sb.append('[');
		sb.append(this.getCustomerGroupPK());
		sb.append(',');
		sb.append(this.getTariffPK());
		sb.append(',');
		sb.append(this.getCurrencyID());
		sb.append(',');
		sb.append(this.productTypePK);
		sb.append(',');
		sb.append(this.priceFragmentTypePK);
		sb.append(']');
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return (super.hashCode() * 31 + Util.hashCode(productTypePK)) * 31 + Util.hashCode(priceFragmentTypePK);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof IAbsolutePriceCoordinate))
			return false;

		if (!super.equals(obj))
			return false;

		IAbsolutePriceCoordinate other = (IAbsolutePriceCoordinate)obj;
		return
				this.productTypePK.equals(other.getProductTypePK()) &&
				this.priceFragmentTypePK.equals(other.getPriceFragmentTypePK());
	}

	@Override
	public void assertAllDimensionValuesAssigned() {
		super.assertAllDimensionValuesAssigned();

		if (productTypePK == null)
			throw new IllegalStateException("productTypePK == null");

		if (priceFragmentTypePK == null)
			throw new IllegalStateException("priceFragmentTypePK == null");
	}

	@Override
	public IPriceCoordinate copyForPriceCalculation() {
		throw new UnsupportedOperationException("NYI"); // I think that's not necessary in absolute coordinates. Marco
	}
}
