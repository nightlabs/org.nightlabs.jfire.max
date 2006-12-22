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

package org.nightlabs.jfire.store.deliver;

import java.io.Serializable;

import org.nightlabs.math.Base62Coder;
import org.nightlabs.util.Utils;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.deliver.id.DeliveryResultID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryResult"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, deliveryResultID"
 */
public class DeliveryResult
implements Serializable
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String deliveryResultID;

	/**
	 * @deprecated Only for JDO!
	 */
	protected DeliveryResult()
	{
	}

	protected static Base62Coder base62Coder = null;
	protected static Base62Coder getBase62Coder()
	{
		if (base62Coder == null)
			base62Coder = new Base62Coder();

		return base62Coder;
	}

	public static String createDeliveryResultID()
	{
		return
				getBase62Coder().encode(System.currentTimeMillis(), 1)
				+ '-' +
				getBase62Coder().encode((long)(Math.random() * Integer.MAX_VALUE), 1);
	}

//	public DeliveryResult(String organisationID)
//	{
//		this(organisationID, createDeliveryResultID());
//	}
//
//	public DeliveryResult(String organisationID, String deliveryResultID)
//	{
//		this.organisationID = organisationID;
//		this.deliveryResultID = deliveryResultID;
//	}

	public DeliveryResult(String organisationID, String code, String text, Throwable error)
	{
		this(
				organisationID,
				createDeliveryResultID(),
				code, text, error);
	}

	public DeliveryResult(String organisationID, String deliveryResultID, String code, String text, Throwable error)
	{
		this.organisationID = organisationID;
		this.deliveryResultID = deliveryResultID;
		this.setCode(code);
		this.setText(text);
		this.setError(error);
	}

	public DeliveryResult(String organisationID, Throwable error)
	{
		this(
				organisationID,
				createDeliveryResultID(),
				error instanceof DeliveryException ? ((DeliveryException)error).getDeliveryResult().getCode() : CODE_FAILED,
				error instanceof DeliveryException ? ((DeliveryException)error).getDeliveryResult().getText() : error.getLocalizedMessage(),
				error instanceof DeliveryException ? ((DeliveryException)error).getDeliveryResult().getError() : error);
	}

	/**
	 * If the client does a two phase delivery, this status means that the first
	 * phase was successful and the delivery will very probably succeed in the second
	 * phase. If the second phase fails, the client will try to reimburse the delivery
	 * in the server.
	 * <p>
	 * If the client sends this status to the client, it MUST afterwards either
	 * confirm or reimburse the delivery in the server, otherwise the delivery will
	 * hang "pending" in the server and needs manual care. In this pending state,
	 * the money is already booked on the customer's account.
	 */
	public static String CODE_APPROVED_NO_EXTERNAL = "approvedNoExternal";

	/**
	 * 
	 */
	public static String CODE_APPROVED_WITH_EXTERNAL = "approvedWithExternal";

	/**
	 * The delivery has failed.  More information will be found in {@link #text}
	 * and {@link #error}.
	 */
	public static String CODE_FAILED = "failed";

	/**
	 * The delivery has been completed. No further access to external delivery systems
	 * is necessary.
	 */
	public static String CODE_DELIVERED_WITH_EXTERNAL = "deliveredWithExternal";

	/**
	 * The delivery has been done locally within JFire, but no external delivery has been
	 * performed (e.g. the server processor might return this, because the client is
	 * responsible for the delivery).
	 */
	public static String CODE_DELIVERED_NO_EXTERNAL = "deliveredNoExternal";

	/**
	 * The delivery has been rolled back externally.
	 */
	public static String CODE_ROLLED_BACK_WITH_EXTERNAL = "rolledBackWithExternal";

	/**
	 * Even though there has no external work been involved, this means the delivery
	 * has been rolled back in the not-really-existing external system.
	 */
	public static String CODE_ROLLED_BACK_NO_EXTERNAL = "rolledBackNoExternal";

	/**
	 * The delivery has been committed externally.
	 */
	public static String CODE_COMMITTED_WITH_EXTERNAL = "committedWithExternal";

	/**
	 * Even though there has no external work been involved, this means the delivery
	 * has been committed in the not-really-existing external system.
	 */
	public static String CODE_COMMITTED_NO_EXTERNAL = "committedNoExternal";

	/**
	 * The delivery has been postponed. This means, the delivery system has taken
	 * control of the delivery process and a real delivery will be done at a later time.
	 * This is the case when handling asynchronous external deliveries: The delivery
	 * should not be booked before the products have been transferred in the real world.
	 * <p>
	 * Alternatively, it can mean that no delivery at all has been initiated, because
	 * the delivery does not continue (with the mode of delivery "nonDelivery").
	 * <p>
	 * Important: When your delivery systems tries to perform a delivery later,
	 * the delivery may already have been done by another way (you can't prevent a
	 * customer to fetch the products himself even though they will be sent by mail
	 * later).
	 * Hence, you should handle this case in your GUI!
	 * <p>
	 * If this is the result of a {@link ServerDeliveryProcessor#deliverBegin(PayParams),
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
	 * This is a non-persistent field which can be used by a pair of delivery-processors
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
	 * @return Returns the deliveryResultID.
	 */
	public String getDeliveryResultID()
	{
		return deliveryResultID;
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
		if (!CODE_APPROVED_NO_EXTERNAL.equals(code) &&
				!CODE_APPROVED_WITH_EXTERNAL.equals(code) &&
				!CODE_FAILED.equals(code) &&
				!CODE_DELIVERED_NO_EXTERNAL.equals(code) &&
				!CODE_DELIVERED_WITH_EXTERNAL.equals(code) &&
				!CODE_ROLLED_BACK_NO_EXTERNAL.equals(code) &&
				!CODE_ROLLED_BACK_WITH_EXTERNAL.equals(code) &&
				!CODE_COMMITTED_NO_EXTERNAL.equals(code) &&
				!CODE_COMMITTED_WITH_EXTERNAL.equals(code) &&
				!CODE_POSTPONED.equals(code))
			throw new IllegalArgumentException("code \""+code+"\" is invalid!");

		this.code = code;
	}

	/**
	 * @return Returns <tt>true</tt> if {@link #isDelivered()} returns <tt>true</tt>.
	 *		Additionally, it returns <tt>true</tt>, if the
	 *		<tt>code</tt> is either {@link #CODE_APPROVED_NO_EXTERNAL} or
	 *		{@link #CODE_APPROVED_WITH_EXTERNAL}.
	 */
	public boolean isApproved()
	{
		if (isDelivered())
			return true;

		return
				CODE_APPROVED_NO_EXTERNAL.equals(code) ||
				CODE_APPROVED_WITH_EXTERNAL.equals(code);
	}

	/**
	 * @return Returns <tt>true</tt> if the <tt>code</tt> is either
	 * {@link #CODE_DELIVERED_NO_EXTERNAL} or
	 * {@link #CODE_DELIVERED_WITH_EXTERNAL} or
	 * {@link #CODE_COMMITTED_NO_EXTERNAL} or
	 * {@link #CODE_COMMITTED_WITH_EXTERNAL}.
	 */
	public boolean isDelivered()
	{
		return
				CODE_DELIVERED_NO_EXTERNAL.equals(code) ||
				CODE_DELIVERED_WITH_EXTERNAL.equals(code) ||
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
		this.errorStackTrace = error == null ? null : Utils.getStackTraceAsString(error);
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
	 * get/setObject access a non-persistent field which can be used by a pair of delivery-processors
	 * (client+server) to transfer additional data between them.
	 *
	 * @param object The object to set.
	 */
	public void setObject(Serializable object)
	{
		this.object = object;
	}
}
