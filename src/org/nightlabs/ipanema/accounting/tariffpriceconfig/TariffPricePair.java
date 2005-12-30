/*
 * Created on Apr 22, 2005
 */
package org.nightlabs.ipanema.accounting.tariffpriceconfig;

import java.io.Serializable;

import org.nightlabs.ipanema.accounting.Price;
import org.nightlabs.ipanema.accounting.Tariff;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class TariffPricePair
implements Serializable
{
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
