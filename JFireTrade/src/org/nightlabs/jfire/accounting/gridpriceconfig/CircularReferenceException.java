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
public class CircularReferenceException extends PriceCalculationException
{
	private static final long serialVersionUID = 1L;
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
