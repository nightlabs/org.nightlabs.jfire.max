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

import java.io.Serializable;

import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.pay.id.PaymentResultID"
 *		detachable="true"
 *		table="JFireTrade_PaymentResult"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, paymentResultID"
 */
public class PaymentResult
implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long paymentResultID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PaymentResult()
	{
	}

//	protected static Base62Coder base62Coder = null;
//	protected static Base62Coder getBase62Coder()
//	{
//		if (base62Coder == null)
//			base62Coder = new Base62Coder();
//
//		return base62Coder;
//	}
//
//	public static String createPaymentResultID()
//	{
//		return
//				getBase62Coder().encode(System.currentTimeMillis(), 1)
//				+ '-' +
//				getBase62Coder().encode((long)(Math.random() * Integer.MAX_VALUE), 1);
//	}
//
//	public PaymentResult(String organisationID)
//	{
//		this(organisationID, createPaymentResultID());
//	}
//
//	public PaymentResult(String organisationID, String paymentResultID)
//	{
//		this.organisationID = organisationID;
//		this.paymentResultID = paymentResultID;
//	}

	public PaymentResult(String code, String text, Throwable error)
	{
		this(
				IDGenerator.getOrganisationID(),
				IDGenerator.nextID(PaymentResult.class),
				code, text, error);
	}

	public PaymentResult(String organisationID, long paymentResultID, String code, String text, Throwable error)
	{
		this.organisationID = organisationID;
		this.paymentResultID = paymentResultID;
		this.setCode(code);
		this.setText(text);
		this.setError(error);
	}

	public PaymentResult(String organisationID, Throwable error)
	{
		this(
				IDGenerator.getOrganisationID(),
				IDGenerator.nextID(PaymentResult.class),
				error instanceof PaymentException ? ((PaymentException)error).getPaymentResult().getCode() : CODE_FAILED,
				error instanceof PaymentException ? ((PaymentException)error).getPaymentResult().getText() : error.getLocalizedMessage(),
				error instanceof PaymentException ? ((PaymentException)error).getPaymentResult().getError() : error);
	}

	/**
	 * If the client does a two phase payment, this status means that the first
	 * phase was successful and the payment will very probably succeed in the second
	 * phase. If the second phase fails, the client will try to reimburse the payment
	 * in the server.
	 * <p>
	 * If the client sends this status to the client, it MUST afterwards either
	 * confirm or reimburse the payment in the server, otherwise the payment will
	 * hang "pending" in the server and needs manual care. In this pending state,
	 * the money is already booked on the customer's account.
	 */
	public static String CODE_APPROVED_NO_EXTERNAL = "approvedNoExternal";

	/**
	 * This means the same as {@link #CODE_APPROVED_NO_EXTERNAL} except that it
	 * indicates that an external approval has been done.
	 */
	public static String CODE_APPROVED_WITH_EXTERNAL = "approvedWithExternal";

	/**
	 * The payment has failed.  More information will be found in {@link #text}
	 * and {@link #error}.
	 */
	public static String CODE_FAILED = "failed";

	/**
	 * The payment has been completed. No further access to external payment systems
	 * is necessary.
	 */
	public static String CODE_PAID_WITH_EXTERNAL = "paidWithExternal";

	/**
	 * The payment has been done locally within JFire, but no external payment has been
	 * performed (e.g. the server processor might return this, because the client is
	 * responsible for the payment).
	 */
	public static String CODE_PAID_NO_EXTERNAL = "paidNoExternal";

	/**
	 * The payment has been rolled back externally.
	 */
	public static String CODE_ROLLED_BACK_WITH_EXTERNAL = "rolledBackWithExternal";

	/**
	 * Even though there has no external work been involved, this means the payment
	 * has been rolled back in the not-really-existing external system.
	 */
	public static String CODE_ROLLED_BACK_NO_EXTERNAL = "rolledBackNoExternal";

	/**
	 * The payment has been committed externally.
	 */
	public static String CODE_COMMITTED_WITH_EXTERNAL = "committedWithExternal";

	/**
	 * Even though there has no external work been involved, this means the payment
	 * has been committed in the not-really-existing external system.
	 */
	public static String CODE_COMMITTED_NO_EXTERNAL = "committedNoExternal";

	/**
	 * The payment has been postponed. This means, the payment system has taken
	 * control of the payment process and a real payment will be done at a later time.
	 * This is the case when handling debit notes: The payment should not be booked
	 * before the money has been fetched from the bank.
	 * <p>
	 * Alternatively, it can mean that no payment at all has been initiated, because
	 * the payment does not continue (with the mode of payment "nonPayment").
	 * <p>
	 * Important: When your payment systems tries to perform a payment later,
	 * the payment may already have been done by another way (you can't prevent a
	 * customer to transfer the money himself even though a debit note is pending).
	 * Hence, you should handle this case in your GUI!
	 * <p>
	 * If this is the result of a {@link ServerPaymentProcessor#payBegin(PayParams)
	 * no transfer will be created. If this is the result at a later time, the transfer
	 * will be rolled back.
	 */
	public static String CODE_POSTPONED = "postponed";

	/**
	 * This is one of the <tt>CODE_</tt> constants.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="30"
	 */
	private String code = null;

	/**
	 * This is usually <tt>null</tt> or the exception message (no stacktrace)
	 *
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="255"
	 */
	private String text = null;

	/**
	 * This may be non-<tt>null</tt> if an exception occured. The exception will NOT be stored
	 * in the database.
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	private Throwable error = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column jdbc-type="LONGVARCHAR"
	 */
	private String errorStackTrace = null;

	/**
	 * This is a non-persistent field which can be used by a pair of payment-processors
	 * (client+server) to transfer additional data between them.
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	private Object object = null;

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the paymentResultID.
	 */
	public long getPaymentResultID()
	{
		return paymentResultID;
	}
	/**
	 * @return Returns the code.
	 */
	public String getCode()
	{
		return code;
	}
	/**
	 * @param code The code to set.
	 */
	private void setCode(String code)
	{
		if (// !PAYMENT_RESULT_CODE_NOT_YET_DONE.equals(code) &&
				!CODE_APPROVED_NO_EXTERNAL.equals(code) &&
				!CODE_APPROVED_WITH_EXTERNAL.equals(code) &&
				!CODE_FAILED.equals(code) &&
				!CODE_PAID_NO_EXTERNAL.equals(code) &&
				!CODE_PAID_WITH_EXTERNAL.equals(code) &&
				!CODE_ROLLED_BACK_NO_EXTERNAL.equals(code) &&
				!CODE_ROLLED_BACK_WITH_EXTERNAL.equals(code) &&
				!CODE_COMMITTED_NO_EXTERNAL.equals(code) &&
				!CODE_COMMITTED_WITH_EXTERNAL.equals(code) &&
				!CODE_POSTPONED.equals(code))
			throw new IllegalArgumentException("code \""+code+"\" is invalid!");

		this.code = code;
	}

	/**
	 * @return Returns <tt>true</tt> if {@link #isPaid()} returns <tt>true</tt>.
	 *		Additionally, it returns <tt>true</tt>, if the
	 *		<tt>code</tt> is either {@link #CODE_APPROVED_NO_EXTERNAL} or
	 *		{@link #CODE_APPROVED_WITH_EXTERNAL}.
	 */
	public boolean isApproved()
	{
		if (isPaid())
			return true;

		return
				CODE_APPROVED_NO_EXTERNAL.equals(code) ||
				CODE_APPROVED_WITH_EXTERNAL.equals(code);
	}

	/**
	 * @return Returns <tt>true</tt> if the <tt>code</tt> is either
	 * {@link #CODE_PAID_NO_EXTERNAL} or
	 * {@link #CODE_PAID_WITH_EXTERNAL} or
	 * {@link #CODE_COMMITTED_NO_EXTERNAL} or
	 * {@link #CODE_COMMITTED_WITH_EXTERNAL}.
	 */
	public boolean isPaid()
	{
		return
				CODE_PAID_NO_EXTERNAL.equals(code) ||
				CODE_PAID_WITH_EXTERNAL.equals(code) ||
				CODE_COMMITTED_NO_EXTERNAL.equals(code) ||
				CODE_COMMITTED_WITH_EXTERNAL.equals(code);
	}

	/**
	 * @return Returns <tt>true</tt> if the <tt>code</tt> is either
	 *		{@link #CODE_ROLLED_BACK_NO_EXTERNAL} or
	 *		{@link #CODE_ROLLED_BACK_WITH_EXTERNAL}.
	 */
	public boolean isRolledBack()
	{
		return
				CODE_ROLLED_BACK_NO_EXTERNAL.equals(code) ||
				CODE_ROLLED_BACK_WITH_EXTERNAL.equals(code);
	}

	/**
	 * @return Returns the error. Note, that this field is not stored to the database!
	 * @see #getErrorStackTrace()
	 */
	public Throwable getError()
	{
		return error;
	}

	public String getErrorStackTrace()
	{
		return errorStackTrace;
	}

	/**
	 * @param error The error to set.
	 */
	public void setError(Throwable error)
	{
		this.error = error;
		this.errorStackTrace = error == null ? null : Util.getStackTraceAsString(error);
	}
	/**
	 * @return Returns the text.
	 */
	public String getText()
	{
		return text;
	}
	/**
	 * @param text The text to set.
	 */
	public void setText(String text)
	{
		this.text = text;
	}

	/**
	 * @return Returns <tt>true</tt> if <tt>code</tt> is {@link #CODE_FAILED} or
	 *		{@link #isRolledBack()} returns <tt>true</tt>.
	 */
	public boolean isFailed()
	{
		return CODE_FAILED.equals(code) || isRolledBack();
	}

	/**
	 * @return Returns the object.
	 *
	 * @see #setObject(Object)
	 */
	public Object getObject()
	{
		return object;
	}

	/**
	 * get/setObject access a non-persistent field which can be used by a pair of payment-processors
	 * (client+server) to transfer additional data between them.
	 *
	 * @param object The object to set.
	 */
	public void setObject(Serializable object)
	{
		this.object = object;
	}
}
