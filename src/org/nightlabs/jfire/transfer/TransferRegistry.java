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

package org.nightlabs.jfire.transfer;


/**
 * This interface defines the registry which is used to manage Transfers of a certain type.
 * One registry might manage transfers of different types as well. It is responsible for
 * generating unique transferIDs within the context of the local organisation and dependent
 * of the transferTypeID.
 * 
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface TransferRegistry
{
	/**
	 * This method should return the local organisationID. 
	 *
	 * @return Returns the organisationID of the organisation which owns the datastore.
	 */
	public String getOrganisationID();

	/**
	 * This method adds an instance of Transfer. In most of the cases, you don't need to call this
	 * method directly, because every Transfer does a self-registration, if it has been created
	 * by this organisation.
	 *
	 * @param transfer
	 */
	public void addTransfer(Transfer transfer);

	/**
	 * @param transferTypeID
	 * @return
	 */
	public long createTransferID(String transferTypeID);
}
