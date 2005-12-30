/*
 * Created on Jul 12, 2005
 */
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
