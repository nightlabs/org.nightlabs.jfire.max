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

import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface IFormulaPriceConfig extends IInnerPriceConfig
{
	/**
	 * Calls {@link #adoptParameters(GridPriceConfig, boolean)} with <tt>onlyAdd=false</tt>.
	 */
	void adoptParameters(IPriceConfig other);
	
	/**
	 * This method adjusts the own parameter config according to the given other
	 * TicketingPriceConfig. After this method has been called, this instance
	 * has the same <tt>CustomerGroup</tt> s, <tt>SaleMode</tt> s, <tt>Tariff</tt> s,
	 * <tt>CategorySet</tt> s and <tt>Currency</tt> s as the other. While adopting it,
	 * the two descendants <tt>FormulaPriceConfig</tt> and <tt>StablePriceConfig</tt>
	 * create missing formula/price cells and remove cells that are not needed anymore.
	 * <p>
	 * Note, that this method leaves the <tt>PriceFragmentType</tt> s untouched!
	 * <tt>PriceFragmentType</tt> s are different, because they do not define cells, but
	 * are fragments within a cell. Additionally, we need to handover all fragments and
	 * cannot ignore any. All the other parameters are filtered by whatever the guiding
	 * inner price config defines, but <tt>PriceFragmentType</tt> s are merged (i.e. one
	 * occurence anywhere in the package forces the packagePriceConfig to know it).
	 *
	 * @param other The other GridPriceConfig from which to take over the parameter config.
	 * @param onlyAdd If this is true, no parameter will be removed and only missing params added.
	 */
	void adoptParameters(IPriceConfig _other, boolean onlyAdd);

	/**
	 * @param throwExceptionIfNotExistent If <tt>true</tt> this method throws a
	 *		<tt>NullPointerException</tt> with a nice message or, if <tt>false</tt>,
	 *		it returns <tt>null</tt>, in case, no <tt>FormulaCell</tt> is existing as
	 *		fallback.
	 *
	 * @return Returns a <tt>FormulaCell</tt> that serves as fallback if no specific one
	 * for a certain coordinate exists or <tt>null</tt> if the fallbackFormulaCell is not
	 * defined. If <tt>throwExceptionIfNotExistent==true</tt>, this method never returns
	 * <tt>null</tt>. 
	 */
	FormulaCell getFallbackFormulaCell(boolean throwExceptionIfNotExistent);

	FormulaCell createFallbackFormulaCell();

	void setFallbackFormula(PriceFragmentType priceFragmentType, String formula);

	FormulaCell getFormulaCell(IPriceCoordinate priceCoordinate, boolean throwExceptionIfNotExistent);

	void setFormula(IPriceCoordinate priceCoordinate, PriceFragmentType priceFragmentType, String formula);
}
