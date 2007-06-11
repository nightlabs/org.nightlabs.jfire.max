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

package org.nightlabs.jfire.trade;

import org.nightlabs.jfire.editlock.EditLockType;
import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.store.ProductType;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class JFireTradeEAR {
	
	public static final String MODULE_NAME = "JFireTrade";

	/**
	 * {@link EditLockType} for {@link ProductType}s. Used in the TradeAdminPrespective.
	 */
	public final static EditLockTypeID EDIT_LOCK_TYPE_ID_PRODUCTTYPE = EditLockTypeID.create(
			Organisation.DEVIL_ORGANISATION_ID, "EditLockTypeProductType"); 
}
