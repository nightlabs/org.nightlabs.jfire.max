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

import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface IPriceCoordinate
{
	/**
	 * @return Returns the currencyID.
	 */
	String getCurrencyID();

	/**
	 * @return Returns the customerGroupPK.
	 */
	String getCustomerGroupPK();

	/**
	 * @return Returns the priceConfig.
	 */
	PriceConfig getPriceConfig();

	/**
	 * @return Returns the tariffPK.
	 */
	String getTariffPK();

	/**
	 * @param currencyID The currencyID to set.
	 */
	void setCurrencyID(String currencyID);

	/**
	 * @param customerGroupPK The customerGroupPK to set.
	 */
	void setCustomerGroupPK(String customerGroupPK);

	/**
	 * @param tariffPK The tariffPK to set.
	 */
	void setTariffPK(String tariffPK);

	/**
	 * @return The organisationID-part of the tariffPK that is returned by {@link #getTariffPK()} or <code>null</code>, if no tariffPK set.
	 */
	String getTariffOrganisationID();

	/**
	 * Check all dimension's values and throw an {@link IllegalStateException} if one of them is <code>null</code>.
	 */
	void assertAllDimensionValuesAssigned();
}
