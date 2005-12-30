/*
 * Created on Jan 16, 2005
 */
package org.nightlabs.jfire.accounting.tariffpriceconfig;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class MissingResultException extends InvalidResultException
{

	/**
	 * @param absolutePriceCoordinate
	 */
	public MissingResultException(IAbsolutePriceCoordinate absolutePriceCoordinate)
	{
		super(absolutePriceCoordinate);
	}

	/**
	 * @param absolutePriceCoordinate
	 * @param message
	 */
	public MissingResultException(
			IAbsolutePriceCoordinate absolutePriceCoordinate, String message)
	{
		super(absolutePriceCoordinate, message);
	}

	/**
	 * @param absolutePriceCoordinate
	 * @param message
	 * @param cause
	 */
	public MissingResultException(
			IAbsolutePriceCoordinate absolutePriceCoordinate, String message,
			Throwable cause)
	{
		super(absolutePriceCoordinate, message, cause);
	}

	/**
	 * @param absolutePriceCoordinate
	 * @param cause
	 */
	public MissingResultException(
			IAbsolutePriceCoordinate absolutePriceCoordinate, Throwable cause)
	{
		super(absolutePriceCoordinate, cause);
	}

}
