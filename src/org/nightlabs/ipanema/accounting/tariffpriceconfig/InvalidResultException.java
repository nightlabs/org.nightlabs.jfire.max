/*
 * Created on Jan 16, 2005
 */
package org.nightlabs.ipanema.accounting.tariffpriceconfig;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class InvalidResultException extends PriceCalculationException
{

	/**
	 * @param absolutePriceCoordinate
	 */
	public InvalidResultException(IAbsolutePriceCoordinate absolutePriceCoordinate)
	{
		super(absolutePriceCoordinate);
	}

	/**
	 * @param absolutePriceCoordinate
	 * @param message
	 */
	public InvalidResultException(
			IAbsolutePriceCoordinate absolutePriceCoordinate, String message)
	{
		super(absolutePriceCoordinate, message);
	}

	/**
	 * @param absolutePriceCoordinate
	 * @param message
	 * @param cause
	 */
	public InvalidResultException(
			IAbsolutePriceCoordinate absolutePriceCoordinate, String message,
			Throwable cause)
	{
		super(absolutePriceCoordinate, message, cause);
	}

	/**
	 * @param absolutePriceCoordinate
	 * @param cause
	 */
	public InvalidResultException(
			IAbsolutePriceCoordinate absolutePriceCoordinate, Throwable cause)
	{
		super(absolutePriceCoordinate, cause);
	}

}
