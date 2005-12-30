/*
 * Created on Jan 15, 2005
 */
package org.nightlabs.jfire.accounting.tariffpriceconfig;

import org.nightlabs.ModuleException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class PriceCalculationException extends ModuleException
{
	private IAbsolutePriceCoordinate absolutePriceCoordinate;

	public PriceCalculationException(IAbsolutePriceCoordinate absolutePriceCoordinate)
	{
		this.absolutePriceCoordinate = absolutePriceCoordinate;
	}

	/**
	 * @param message
	 */
	public PriceCalculationException(IAbsolutePriceCoordinate absolutePriceCoordinate, String message)
	{
		super(message);
		this.absolutePriceCoordinate = absolutePriceCoordinate;
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PriceCalculationException(IAbsolutePriceCoordinate absolutePriceCoordinate, String message, Throwable cause)
	{
		super(message, cause);
		this.absolutePriceCoordinate = absolutePriceCoordinate;
	}

	/**
	 * @param cause
	 */
	public PriceCalculationException(IAbsolutePriceCoordinate absolutePriceCoordinate, Throwable cause)
	{
		super(cause);
		this.absolutePriceCoordinate = absolutePriceCoordinate;
	}

	/**
	 * @return Returns the absolutePriceCoordinate.
	 */
	public IAbsolutePriceCoordinate getAbsolutePriceCoordinate()
	{
		return absolutePriceCoordinate;
	}
}
