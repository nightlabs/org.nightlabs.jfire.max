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

package org.nightlabs.jfire.accounting.gridpriceconfig;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class PriceCalculationException extends Exception
{
	private static final long serialVersionUID = 1L;
	private static final String MOZILLA_EXCEPTION_TAG = "org.mozilla.javascript.EcmaError";
	
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
	
	/**
	 * This method trims down the exception error string message
	 * the Mozilla Java script engine throws the message in the format of Classname:TitleError:ErrorMessage(PluginClassName)
	 * this method tries to extract only the ErrorMessage part and return it.
	 *
	 * @return Returns the ErrorMessage.
	 */
	public String getShortenedErrorMessage()
	{
		String  exceptionError = getMessage();
		if(exceptionError.indexOf(MOZILLA_EXCEPTION_TAG) > -1)
		{
			String eclErr = exceptionError.substring(exceptionError.indexOf(
					MOZILLA_EXCEPTION_TAG), 
					exceptionError.length());			
			String[] str = eclErr.split(":");
			if(str.length == 3)
				return  str[2].substring(0, str[2].indexOf("("));
			else
				return  eclErr; 
		}
		else
			return getMessage();
	}

	/**
	 * @see #getShortenedErrorMessage()
	 *
	 * @return Returns the TitleErrorMessage.
	 */
	public String getTitleErrorMessage()
	{
		String  exceptionError = getMessage();
		if(exceptionError.indexOf(MOZILLA_EXCEPTION_TAG) > -1)
		{
			String eclErr = exceptionError.substring(exceptionError.indexOf(
					MOZILLA_EXCEPTION_TAG), exceptionError.length());			
			String[] str = eclErr.split(":");
			if(str.length == 3)
				return  str[1];
			else
				return  eclErr; 
		}
		else
			// if the format of the message is diffrent then just trim down to 15 charecters.
			if(getMessage().length() > 15)
				return getMessage().substring(0, 15);	
			else
				return getMessage();
	}	
	
}
