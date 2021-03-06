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

import org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.transfer.Anchor;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor"
 *		detachable="true"
 *		table="JFireTrade_ServerPaymentProcessorDebitNoteGermany"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_ServerPaymentProcessorDebitNoteGermany")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class ServerPaymentProcessorDebitNoteGermany extends ServerPaymentProcessor
{
	private static final long serialVersionUID = 1L;

	public static ServerPaymentProcessorDebitNoteGermany getServerPaymentProcessorDebitNoteGermany(PersistenceManager pm)
	{
		ServerPaymentProcessorDebitNoteGermany serverPaymentProcessorDebitNote;
		try {
			pm.getExtent(ServerPaymentProcessorDebitNoteGermany.class);
			serverPaymentProcessorDebitNote = (ServerPaymentProcessorDebitNoteGermany) pm.getObjectById(
					ServerPaymentProcessorID.create(Organisation.DEV_ORGANISATION_ID, ServerPaymentProcessorDebitNoteGermany.class.getName()));
		} catch (JDOObjectNotFoundException e) {
			serverPaymentProcessorDebitNote = new ServerPaymentProcessorDebitNoteGermany(Organisation.DEV_ORGANISATION_ID, ServerPaymentProcessorDebitNoteGermany.class.getName());
			serverPaymentProcessorDebitNote = pm.makePersistent(serverPaymentProcessorDebitNote);
		}

		return serverPaymentProcessorDebitNote;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ServerPaymentProcessorDebitNoteGermany()
	{
	}

	/**
	 * @param organisationID
	 * @param serverPaymentProcessorID
	 */
	public ServerPaymentProcessorDebitNoteGermany(String organisationID,
			String serverPaymentProcessorID)
	{
		super(organisationID, serverPaymentProcessorID);
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#getAnchorOutside(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	@Override
	public Anchor getAnchorOutside(PayParams payParams)
	{
		return getAccountOutside(payParams, "debitNote");
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#externalPayBegin(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	@Override
	protected PaymentResult externalPayBegin(PayParams payParams)
			throws PaymentException
	{
		// If we have no precursor, we have to postpone the payment.
		if (payParams.paymentData.getPayment().getPrecursor() == null)
			return new PaymentResult(
					PaymentResult.CODE_POSTPONED,
					(String)null, (Throwable)null);

		// We have a precursor, hence this is the second call after the data
		// has been transferred to a DTA file. This is probably done outside the
		// payment core.
		return null;
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#externalPayDoWork(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	@Override
	protected PaymentResult externalPayDoWork(PayParams payParams)
			throws PaymentException
	{
		// If we have no precursor, we have to postpone the payment.
		if (payParams.paymentData.getPayment().getPrecursor() == null)
			return new PaymentResult(
					PaymentResult.CODE_POSTPONED,
					(String)null, (Throwable)null);

		// We have a precursor, hence this is the second call after the data
		// has been transferred to a DTA file. This is probably done outside the
		// payment core.
		return null;
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#externalPayCommit(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)
	 */
	@Override
	protected PaymentResult externalPayCommit(PayParams payParams)
			throws PaymentException
	{
		// If we have no precursor, we have to postpone the payment.
		if (payParams.paymentData.getPayment().getPrecursor() == null)
			return new PaymentResult(
					PaymentResult.CODE_POSTPONED,
					(String)null, (Throwable)null);

		// We have a precursor, hence this is the second call after the data
		// has been transferred to a DTA file. This is probably done outside the
		// payment core.
		return null;
	}

	/**
	 * @see org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor#externalPayRollback(PayParams)
	 */
	@Override
	protected PaymentResult externalPayRollback(PayParams payParams)
			throws PaymentException
	{
		return null;
	}

}
