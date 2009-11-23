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

import java.util.StringTokenizer;



/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class PriceCalculationException extends Exception
{
	private static final long serialVersionUID = 1L;

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

//	private static final String MOZILLA_EXCEPTION_CLASS = "org.mozilla.javascript"; // mozilla javascript engine


	/**
	 * This method trims down the exception error string message to return a meaningfull Error message.
	 * the Mozilla Java script engine throws the message in the format of MozzilaClassNameException:TitleError:ErrorMessage(PluginClassName)
	 * this method tries to extract only the ErrorMessage part and return it.
	 *
	 * @return Returns the ErrorMessage.
	 */
	public String getShortenedErrorMessage() {
		String msg = getMessage();
		StringTokenizer strTok = new StringTokenizer(msg, ":");
		while (strTok.hasMoreTokens()) {
			String shortMsg = strTok.nextToken();
			if (shortMsg.contains("(org.nightlabs.jfire.accounting.")) {  //$NON-NLS-1$
				msg = shortMsg.substring(0, shortMsg.indexOf('(')).trim();
				if (msg.charAt(msg.length()-1) != '.') msg += ".";

				break;
			}
		}

		return msg;
	}

//	public String getShortenedErrorMessage()
	// Exception
//	org.mozilla.javascript.WrappedException: Wrapped java.lang.IndexOutOfBoundsException: There is no Currency registered in this PriceConfig with the currencyID CHF (org.nightlabs.jfire.accounting.gridpriceconfig.AbsolutePriceCoordinate[chezfrancois.jfire.org/CustomerGroup-default,chezfrancois.jfire.org/_gold_card_,EUR,chezfrancois.
//	{
//		String  exceptionError = getMessage();
//		// detects if the exception error thrown is coming from the Mozilla javascript engine
//		int lastInd = 0;
//		int expInd = exceptionError.indexOf(MOZILLA_EXCEPTION_CLASS);
//		// get to the last occurance of the exception to remove the wrapped exceptions.
//		while(lastInd > -1){
//			lastInd = exceptionError.indexOf(MOZILLA_EXCEPTION_CLASS, expInd + MOZILLA_EXCEPTION_CLASS.length());
//			if(lastInd > -1)
//				expInd = lastInd;
//		}
//
//		if(expInd > -1)
//		{
//			// remove all the wrapped exceptions
//			String eclErr = exceptionError.substring(expInd, exceptionError.length());
//			String[] str = eclErr.split(":");
//			if(str.length >= 2)
//				if(str[str.length - 1].indexOf("(") > -1)
//					return  str[str.length - 1].substring(0, str[str.length - 1].indexOf("("));
//				else
//					return  str[str.length - 1];
//			else
//				return  eclErr;
//		}
//		else
//			return getMessage();
//	}

}
