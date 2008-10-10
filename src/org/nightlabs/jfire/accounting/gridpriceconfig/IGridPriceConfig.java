package org.nightlabs.jfire.accounting.gridpriceconfig;

import java.util.Collection;

import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.id.CustomerGroupID;

public interface IGridPriceConfig extends IPriceConfig
{

//	boolean isDependentOnOffer();

	/**
	 * @return Returns the customerGroups.
	 */
	Collection<CustomerGroup> getCustomerGroups();

	boolean addCustomerGroup(CustomerGroup customerGroup);

	CustomerGroup getCustomerGroup(CustomerGroupID customerGroupID,
			boolean throwExceptionIfNotExistent);

	CustomerGroup getCustomerGroup(String organisationID,
			String customerGroupID, boolean throwExceptionIfNotExistent);

	boolean containsCustomerGroup(CustomerGroup customerGroup);

	CustomerGroup removeCustomerGroup(String organisationID,
			String customerGroupID);

	/**
	 * @return Returns the tariffs.
	 */
	Collection<Tariff> getTariffs();

	boolean addTariff(Tariff tariff);

	Tariff getTariff(TariffID tariffID, boolean throwExceptionIfNotExistent);

	Tariff getTariff(String organisationID, String tariffID,
			boolean throwExceptionIfNotExistent);

	boolean containsTariff(Tariff tariff);

	Tariff removeTariff(String organisationID, String tariffID);

	void removeTariff(Tariff tariff);

	void clearTariffs();

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

}