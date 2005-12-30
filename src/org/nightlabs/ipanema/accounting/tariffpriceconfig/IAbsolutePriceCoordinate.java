/*
 * Created on Jul 12, 2005
 */
package org.nightlabs.ipanema.accounting.tariffpriceconfig;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface IAbsolutePriceCoordinate
extends IPriceCoordinate
{
	/**
	 * @return Returns the priceFragmentTypePK.
	 */
	String getPriceFragmentTypePK();

	/**
	 * @return Returns the productTypePK.
	 */
	String getProductTypePK();
}