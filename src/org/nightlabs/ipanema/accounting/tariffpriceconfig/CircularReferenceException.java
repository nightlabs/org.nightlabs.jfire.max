/*
 * Created on Jan 14, 2005
 */
package org.nightlabs.ipanema.accounting.tariffpriceconfig;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CircularReferenceException extends PriceCalculationException
{
	
	/**
	 * @param absolutePriceCoordinate
	 */
	public CircularReferenceException(
			IAbsolutePriceCoordinate absolutePriceCoordinate)
	{
		super(absolutePriceCoordinate);
	}
	/**
	 * @param absolutePriceCoordinate
	 * @param message
	 */
	public CircularReferenceException(
			IAbsolutePriceCoordinate absolutePriceCoordinate, String message)
	{
		super(absolutePriceCoordinate, message);
	}
	/**
	 * @param absolutePriceCoordinate
	 * @param message
	 * @param cause
	 */
	public CircularReferenceException(
			IAbsolutePriceCoordinate absolutePriceCoordinate, String message,
			Throwable cause)
	{
		super(absolutePriceCoordinate, message, cause);
	}
	/**
	 * @param absolutePriceCoordinate
	 * @param cause
	 */
	public CircularReferenceException(
			IAbsolutePriceCoordinate absolutePriceCoordinate, Throwable cause)
	{
		super(absolutePriceCoordinate, cause);
	}
}
