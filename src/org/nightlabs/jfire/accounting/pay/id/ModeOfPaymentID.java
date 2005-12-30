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

package org.nightlabs.jfire.accounting.pay.id;

import org.nightlabs.jdo.BaseObjectID;
import org.nightlabs.jdo.ObjectIDException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ModeOfPaymentID extends BaseObjectID
{
	public String organisationID;
	public String modeOfPaymentID;

	public ModeOfPaymentID() { }

	/**
	 * @param keyStr
	 * @throws org.nightlabs.jdo.ObjectIDException
	 */
	public ModeOfPaymentID(String keyStr) throws ObjectIDException
	{
		super(keyStr);
	}

	public static ModeOfPaymentID create(String organisationID, String modeOfPaymentID)
	{
		ModeOfPaymentID n = new ModeOfPaymentID();
		n.organisationID = organisationID;
		n.modeOfPaymentID = modeOfPaymentID;
		return n;
	}
}
