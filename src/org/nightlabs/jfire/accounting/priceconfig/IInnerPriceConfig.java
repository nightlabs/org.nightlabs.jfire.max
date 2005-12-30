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

package org.nightlabs.jfire.accounting.priceconfig;



/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface IInnerPriceConfig extends IPriceConfig
{
	public void setPackagingResultPriceConfig(
			String innerProductTypePK, String packageProductTypePK,
			IPriceConfig resultPriceConfig);

	public IPriceConfig getPackagingResultPriceConfig(
			String innerProductTypePK, String packageProductTypePK,
			boolean throwExceptionIfNotExistent);


//	/**
//	 * There are implementations of <tt>PriceConfig</tt> that are useable only within
//	 * product packages, because their values are indefinit (formulas depending on the
//	 * siblings within the package). Therefore a <tt>ProductType</tt> is not saleable directly
//	 * if such a <tt>PriceConfig</tt> is assigned.
//	 *
//	 * @return An implementation of <tt>PriceConfig</tt> must return <tt>true</tt>, if
//	 * it's prices are dependent on the <tt>ProductType</tt>'s siblings within a package
//	 * (or the package itself) and therefore, the <tt>ProductType</tt> cannot be sold outside
//	 * of a package.
//	 */
//	boolean requiresProductTypePackageInternal();

}
