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

import java.util.Collection;

import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface IResultPriceConfig extends IPackagePriceConfig
{
	void resetPriceFragmentCalculationStati();
	void adoptParameters(IPriceConfig priceConfig);
	PriceCell createPriceCell(IPriceCoordinate priceCoordinate);
	PriceCell getPriceCell(IPriceCoordinate priceCoordinate, boolean throwExceptionIfNotExistent);
	Collection getPriceCells();

//	Collection getCustomerGroups();
//	void addCustomerGroup(CustomerGroup customerGroup);
//	CustomerGroup getCustomerGroup(String organisationID, String customerGroupID, boolean throwExceptionIfNotExistent);
//	boolean containsCustomerGroup(CustomerGroup customerGroup);
//	CustomerGroup removeCustomerGroup(String organisationID, String customerGroupID);
//
//	Collection getTariffs();
//	void addTariff(Tariff tariff);
//	Tariff getTariff(String organisationID, long tariffID, boolean throwExceptionIfNotExistent);
//	boolean containsTariff(Tariff tariff);
//	Tariff removeTariff(String organisationID, long tariffID);

}
