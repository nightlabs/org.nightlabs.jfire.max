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

package org.nightlabs.jfire.accounting.pay;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.transfer.Anchor;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;


/**
 * This payment processor handles cash payments. This basically means, it doesn't
 * do anything as cash is flowing outside the computer and the payment is booked
 * in the accounting by the framework.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class ServerPaymentProcessorCash
extends ServerPaymentProcessor
{
	private static final long serialVersionUID = 1L;

	public static ServerPaymentProcessorCash getServerPaymentProcessorCash(PersistenceManager pm)
	{
		ServerPaymentProcessorCash serverPaymentProcessorCash;
		try {
			pm.getExtent(ServerPaymentProcessorCash.class);
			serverPaymentProcessorCash = (ServerPaymentProcessorCash) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEV_ORGANISATION_ID, ServerPaymentProcessorCash.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverPaymentProcessorCash = new ServerPaymentProcessorCash(Organisation.DEV_ORGANISATION_ID, ServerPaymentProcessorCash.class.getName());
			serverPaymentProcessorCash = pm.makePersistent(serverPaymentProcessorCash);
		}

		return serverPaymentProcessorCash;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ServerPaymentProcessorCash()
	{
	}

	public ServerPaymentProcessorCash(String organisationID,
			String serverPaymentProcessorID)
	{
		super(organisationID, serverPaymentProcessorID);
	}

	@Override
	public Anchor getAnchorOutside(PayParams payParams)
	{
		Account treasury = getAccountOutside(payParams, "cash");
		return treasury;
	}

	@Override
	protected PaymentResult externalPayBegin(PayParams payParams) throws PaymentException
	{
		return null;
	}

	@Override
	protected PaymentResult externalPayDoWork(PayParams payParams)
			throws PaymentException
	{
		return null;
	}

	@Override
	protected PaymentResult externalPayCommit(PayParams payParams) throws PaymentException
	{
		return null;
	}

	@Override
	protected PaymentResult externalPayRollback(PayParams payParams) throws PaymentException
	{
		return null;
	}

}
