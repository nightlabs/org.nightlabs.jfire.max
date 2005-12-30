/*
 * Created on Jul 12, 2005
 */
package org.nightlabs.jfire.accounting.tariffpriceconfig;

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
}