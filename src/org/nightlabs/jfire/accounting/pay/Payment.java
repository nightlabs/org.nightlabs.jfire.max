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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.Util;

/**
 * {@link Payment}s are created when payableObjects or parts of payableObjects are paid. Therefore
 * a {@link Payment} is linked to one or more payableObjects ({@link Payment#getPayableObjects()}).
 * <p>
 * Additionally, every <tt>MoneyTransfer</tt> can occur without being linked to any
 * <tt>PayableObject</tt> (e.g. if the partner decides later to do a withdrawal of some
 * "old" money kept after reimbourse).
 * </p>
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.pay.id.PaymentID"
 *		detachable="true"
 *		table="JFireTrade_Payment"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, paymentID"
 *		include-body="id/PaymentID.body.inc"
 *
 * @jdo.fetch-group name="Payment.precursor" fields="precursor"
 * @jdo.fetch-group name="Payment.followUp" fields="followUp"
 * @jdo.fetch-group name="Payment.user" fields="user"
 * @jdo.fetch-group name="Payment.payBeginClientResult" fields="payBeginClientResult"
 * @jdo.fetch-group name="Payment.payBeginServerResult" fields="payBeginServerResult"
 * @jdo.fetch-group name="Payment.payDoWorkClientResult" fields="payDoWorkClientResult"
 * @jdo.fetch-group name="Payment.payDoWorkServerResult" fields="payDoWorkServerResult"
 * @jdo.fetch-group name="Payment.payEndClientResult" fields="payEndClientResult"
 * @jdo.fetch-group name="Payment.payEndServerResult" fields="payEndServerResult"
 * @jdo.fetch-group name="Payment.currency" fields="currency"
 * @jdo.fetch-group name="Payment.payableObjects" fields="payableObjects"
 * @jdo.fetch-group name="Payment.partner" fields="partner"
 * @jdo.fetch-group name="Payment.partnerAccount" fields="partnerAccount"
 * @jdo.fetch-group name="Payment.modeOfPaymentFlavour" fields="modeOfPaymentFlavour"
 *
 * @jdo.query
 * 	name="getPaymentsForPayableObject"
 * 	query="SELECT
 *			WHERE
 *				this.payableObjects.contains(:payableObject)"
 */
@PersistenceCapable(
	objectIdClass=PaymentID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_Payment")
@FetchGroups({
	@FetchGroup(
		name=Payment.FETCH_GROUP_PRECURSOR,
		members=@Persistent(name="precursor")),
	@FetchGroup(
		name=Payment.FETCH_GROUP_FOLLOW_UP,
		members=@Persistent(name="followUp")),
	@FetchGroup(
		name=Payment.FETCH_GROUP_USER,
		members=@Persistent(name="user")),
	@FetchGroup(
		name=Payment.FETCH_GROUP_PAY_BEGIN_CLIENT_RESULT,
		members=@Persistent(name="payBeginClientResult")),
	@FetchGroup(
		name=Payment.FETCH_GROUP_PAY_BEGIN_SERVER_RESULT,
		members=@Persistent(name="payBeginServerResult")),
	@FetchGroup(
		name=Payment.FETCH_GROUP_PAY_DO_WORK_CLIENT_RESULT,
		members=@Persistent(name="payDoWorkClientResult")),
	@FetchGroup(
		name=Payment.FETCH_GROUP_PAY_DO_WORK_SERVER_RESULT,
		members=@Persistent(name="payDoWorkServerResult")),
	@FetchGroup(
		name=Payment.FETCH_GROUP_PAY_END_CLIENT_RESULT,
		members=@Persistent(name="payEndClientResult")),
	@FetchGroup(
		name=Payment.FETCH_GROUP_PAY_END_SERVER_RESULT,
		members=@Persistent(name="payEndServerResult")),
	@FetchGroup(
		name=Payment.FETCH_GROUP_CURRENCY,
		members=@Persistent(name="currency")),
	@FetchGroup(
		name=Payment.FETCH_GROUP_PAYABLEOBJECTS,
		members=@Persistent(name="payableObjects")),
	@FetchGroup(
		name=Payment.FETCH_GROUP_PARTNER,
		members=@Persistent(name="partner")),
	@FetchGroup(
		name=Payment.FETCH_GROUP_PARTNER_ACCOUNT,
		members=@Persistent(name="partnerAccount")),
	@FetchGroup(
		name=Payment.FETCH_GROUP_MODE_OF_PAYMENT_FLAVOUR,
		members=@Persistent(name="modeOfPaymentFlavour"))
})
@Queries(
	@javax.jdo.annotations.Query(
		name="getPaymentsForPayableObject",
		value="SELECT WHERE this.payableObjects.contains(:payableObject)")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Payment
implements Serializable, StoreCallback
{
	private static final long serialVersionUID = 1L;

	public static final String PAYMENT_DIRECTION_INCOMING = "incoming";

	public static final String PAYMENT_DIRECTION_OUTGOING = "outgoing";

	public static final String FETCH_GROUP_PRECURSOR = "Payment.precursor";
	public static final String FETCH_GROUP_FOLLOW_UP = "Payment.followUp";
	public static final String FETCH_GROUP_USER = "Payment.user";
	public static final String FETCH_GROUP_PAY_BEGIN_CLIENT_RESULT = "Payment.payBeginClientResult";
	public static final String FETCH_GROUP_PAY_BEGIN_SERVER_RESULT = "Payment.payBeginServerResult";
	public static final String FETCH_GROUP_PAY_DO_WORK_CLIENT_RESULT = "Payment.payDoWorkClientResult";
	public static final String FETCH_GROUP_PAY_DO_WORK_SERVER_RESULT = "Payment.payDoWorkServerResult";
	public static final String FETCH_GROUP_PAY_END_CLIENT_RESULT = "Payment.payEndClientResult";
	public static final String FETCH_GROUP_PAY_END_SERVER_RESULT = "Payment.payEndServerResult";
	public static final String FETCH_GROUP_CURRENCY = "Payment.currency";
	public static final String FETCH_GROUP_PAYABLEOBJECTS = "Payment.payableObjects";
	public static final String FETCH_GROUP_PARTNER = "Payment.partner";
	public static final String FETCH_GROUP_PARTNER_ACCOUNT = "Payment.partnerAccount";
	public static final String FETCH_GROUP_MODE_OF_PAYMENT_FLAVOUR = "Payment.modeOfPaymentFlavour";

	@SuppressWarnings("unchecked")
	public static Collection<Payment> getPaymentsForPayableObject(PersistenceManager pm, PayableObject payableObject) {
		Query q = pm.newNamedQuery(Payment.class, "getPaymentsForPayableObject");
		return (Collection<Payment>) q.execute(payableObject);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long paymentID;

	/**
	 * This is used for postponed <tt>Payment</tt>s. To follow up a previously
	 * postponed payment, you have to set this (or the {@link #precursorID})
	 * to the previous payment (which has not been completed, but postponed).
	 * This will cause {@link #followUp} of the previous <tt>Payment</tt> to point
	 * to the new one while <tt>precursor</tt> in the new <tt>Payment</tt> points
	 * back to the old one.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Payment precursor = null;

	/**
	 * @see #precursor
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private PaymentID precursorID = null;

	/**
	 * This is used for postponed <tt>Payment</tt>s. If this <tt>Payment</tt> has been
	 * postponed and later followed up by a new one, this field will point to the
	 * new <tt>Payment</tt>. In case, the new <tt>Payment</tt> failed and a second
	 * follow-up-<tt>Payment</tt> is done, this will point to the newest follow-up
	 * <tt>Payment</tt> (and therefore at the end always to one that did not fail).
	 *
	 * @see #precursor
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Payment followUp = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private User user = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String clientPaymentProcessorFactoryID;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="payment"
	 */
	@Persistent(
		mappedBy="payment",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private PaymentLocal paymentLocal;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient ServerPaymentProcessorID serverPaymentProcessorID = null;

	/**
	 * @return Returns the serverPaymentProcessorID. Might be <tt>null</tt> if client
	 *		doesn't choose one.
	 */
	public ServerPaymentProcessorID getServerPaymentProcessorID()
	{
		if (serverPaymentProcessorID == null) {
			if (serverPaymentProcessorIDStrKey != null) {
				try {
					serverPaymentProcessorID = new ServerPaymentProcessorID(serverPaymentProcessorIDStrKey);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return serverPaymentProcessorID;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String serverPaymentProcessorIDStrKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String paymentDirection = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date beginDT;


	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date paymentDT;
	

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private PaymentResult payBeginClientResult = null;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private PaymentResult payBeginServerResult = null;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private PaymentResult payDoWorkClientResult = null;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private PaymentResult payDoWorkServerResult = null;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private PaymentResult payEndClientResult = null;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private PaymentResult payEndServerResult = null;

	/**
	 * A payment can be postponed by a {@link ServerPaymentProcessor} setting
	 * {@link PaymentResult#CODE_POSTPONED} as <tt>PaymentResult</tt> for the
	 * current payment phase. If this happens, there are two possibilities:
	 * <ul>
	 *  <li>
	 *   The <tt>Payment</tt> is postponed by
	 *   {@link ServerPaymentProcessor#payBegin(PayParams)} or even already before
	 *   that by the client. In this case, your <tt>ServerPaymentProcessor</tt>
	 *   doesn't even need to create a <tt>PayMoneyTransfer</tt> because it will NOT
	 *   be stored anyway.
	 *  </li>
	 *  <li>
	 *   The <tt>Payment</tt> can still be postponed during
	 *   {@link ServerPaymentProcessor#payEnd(PayParams)}. This is not recommended!
	 *   It causes the payment process to be rollbacked. The creation and booking
	 *   of a <tt>PayMoneyTransfer</tt> and the rollback of it are very expensive in
	 *   comparison to postponing in <tt>payBegin(...)</tt>. Whenever possible, please
	 *   postpone already in <tt>payBegin(...)</tt>!!!
	 *  </li>
	 * </ul>
	 * <tt>postponed</tt> becomes true automatically by setting a <tt>PaymentResult</tt>
	 * to this <tt>Payment</tt> which has the code {@link PaymentResult#CODE_POSTPONED}.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean postponed = false;

	/**
	 * Is set <tt>false</tt>, after all has been done. As long as it is <tt>true</tt>,
	 * the payment process is pending and still needs to be completed by a server-
	 * side <tt>payEnd(..)</tt>. If an error occurs, <tt>failed</tt> is set to <tt>true</tt>
	 * and after the rollback has been done, <tt>pending</tt> is set to <tt>false</tt>.
	 * If the payment ends successful, <tt>pending</tt> is set to <tt>false</tt>, too. So
	 * <tt>pending == false && failed == false</tt> indicates a success.
	 * <p>
	 * When <tt>pending</tt> is set <tt>false</tt>, {@link #endDT} is set to the current
	 * timestamp.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean pending = true;

	/**
	 * Is set <tt>true</tt>, as soon as the external payment is approved. It is
	 * set automatically, when {@link #externalPaymentDone} is set <tt>true</tt>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean externalPaymentApproved = false;

	/**
	 * Is set <tt>true</tt>, as soon as the external payment is complete.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean externalPaymentDone = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean failed = false;

	/**
	 * This is set automatically when pending is set to <tt>false</tt>
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date endDT = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean forceRollback = false;

	/**
	 * @param forceRollback The forceRollback to set.
	 */
	public void setForceRollback()
	{
		this.forceRollback = true;
	}
	/**
	 * @return Returns the forceRollback.
	 */
	public boolean isForceRollback()
	{
		return forceRollback;
	}

	/**
	 * A rollback has not (yet) been done. If <tt>failed == true</tt> a rollback
	 * should still occur.
	 */
	public static final byte ROLLBACK_STATUS_NOT_DONE = 0;
	/**
	 * A rollback has been done, but there was no {@link org.nightlabs.jfire.accounting.pay.PayMoneyTransfer}
	 * which could have been undone. Hence the rollback didn't do anything except changing
	 * this flag.
	 */
	public static final byte ROLLBACK_STATUS_DONE_WITHOUT_ACTION = 1;
	/**
	 * A rollback has been done and the {@link org.nightlabs.jfire.accounting.pay.PayMoneyTransfer}
	 * including all dependent {@link org.nightlabs.jfire.accounting.MoneyTransfer}s have
	 * been deleted. All involved anchors got previously informed by
	 * {@link org.nightlabs.jfire.transfer.Anchor#rollbackTransfer(User, Transfer, Set)}.
	 * <p>
	 * Note: If the <tt>PayMoneyTransfer</tt> has not been booked (but only created),
	 * it will of course not be rolled back on the anchors, but the status will be
	 * this one, too (and the transfer deleted from datastore).
	 */
	public static final byte ROLLBACK_STATUS_DONE_NORMAL = 2;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private byte rollbackStatus = ROLLBACK_STATUS_NOT_DONE;

	public Payment(String organisationID, long paymentID, PaymentID precursorID)
	{
		this(organisationID, paymentID);
		this.precursorID = precursorID;
	}

	public Payment(String organisationID, long paymentID)
	{
		if (organisationID == null)
			throw new IllegalArgumentException("organisationID is null");

		if (paymentID < 0)
			throw new IllegalArgumentException("paymentID < 0");
//		if (paymentID == null)
//			throw new NullPointerException("paymentID");

		this.organisationID = organisationID;
		this.paymentID = paymentID;

		this.beginDT = new Date();
		this.paymentDT = new Date();
//		this.paymentDirection = PAYMENT_DIRECTION_INCOMING;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected Payment() { }

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the paymentID.
	 */
	public long getPaymentID()
	{
		return paymentID;
	}

	public static String getPrimaryKey(String organisationID, long paymentID)
	{
		return organisationID + '/' + ObjectIDUtil.longObjectIDFieldToString(paymentID);
	}

	public String getPrimaryKey()
	{
		return getPrimaryKey(organisationID, paymentID);
	}

	/**
	 * @return Returns the payBeginClientResult.
	 */
	public PaymentResult getPayBeginClientResult()
	{
		return payBeginClientResult;
	}

	protected void updateStatus(PaymentResult paymentResult)
	{
		if (PaymentResult.CODE_APPROVED_WITH_EXTERNAL.equals(
				paymentResult.getCode()))
		{
			externalPaymentApproved = true;
		}

		if (PaymentResult.CODE_PAID_WITH_EXTERNAL.equals(
				paymentResult.getCode()))
		{
			externalPaymentApproved = true;
			externalPaymentDone = true;
		}

		if (paymentResult.isFailed())
		{
			failed = true;
		}

		if (PaymentResult.CODE_POSTPONED.equals(
				paymentResult.getCode()))
		{
			postponed = true;
		}
	}

	/**
	 * @param payBeginClientResult The payBeginClientResult to set.
	 */
	public void setPayBeginClientResult(PaymentResult payBeginClientResult)
	{
		this.payBeginClientResult = payBeginClientResult;
		updateStatus(payBeginClientResult);
	}
	/**
	 * @return Returns the payBeginServerResult.
	 */
	public PaymentResult getPayBeginServerResult()
	{
		return payBeginServerResult;
	}

	/**
	 * @param payBeginServerResult The payBeginServerResult to set.
	 */
	public void setPayBeginServerResult(PaymentResult payBeginServerResult)
	{
		this.payBeginServerResult = payBeginServerResult;
		updateStatus(payBeginServerResult);
	}

	/**
	 * @return Returns the payDoWorkClientResult.
	 */
	public PaymentResult getPayDoWorkClientResult()
	{
		return payDoWorkClientResult;
	}
	/**
	 * @param payDoWorkClientResult The payDoWorkClientResult to set.
	 */
	public void setPayDoWorkClientResult(PaymentResult payDoWorkClientResult)
	{
		this.payDoWorkClientResult = payDoWorkClientResult;
		updateStatus(payDoWorkClientResult);
	}

	/**
	 * @return Returns the payDoWorkServerResult.
	 */
	public PaymentResult getPayDoWorkServerResult()
	{
		return payDoWorkServerResult;
	}
	/**
	 * @param payDoWorkServerResult The payDoWorkServerResult to set.
	 */
	public void setPayDoWorkServerResult(PaymentResult payDoWorkServerResult)
	{
		this.payDoWorkServerResult = payDoWorkServerResult;
		updateStatus(payDoWorkServerResult);
	}

	/**
	 * @return Returns the payEndClientResult.
	 */
	public PaymentResult getPayEndClientResult()
	{
		return payEndClientResult;
	}
	/**
	 * Step 3, performed first in client in a copy of Payment.
	 * Then, in the server, after the client has uploaded its
	 * PaymentResult, before the server tries local commit (hence, this method
	 * is called in a separate transaction ("RequiresNew").
	 *
	 * @param payEndClientResult The payEndClientResult to set.
	 */
	public void setPayEndClientResult(PaymentResult payEndClientResult)
	{
		this.payEndClientResult = payEndClientResult;
		updateStatus(payEndClientResult);
	}
	/**
	 * @return Returns the payEndServerResult.
	 */
	public PaymentResult getPayEndServerResult()
	{
		return payEndServerResult;
	}
	/**
	 * @param payEndServerResult The payEndServerResult to set.
	 */
	public void setPayEndServerResult(PaymentResult payEndServerResult)
	{
		this.payEndServerResult = payEndServerResult;
		updateStatus(payEndServerResult);

		if (PaymentResult.CODE_COMMITTED_WITH_EXTERNAL.equals(payEndServerResult.getCode()) ||
				PaymentResult.CODE_COMMITTED_NO_EXTERNAL.equals(payEndServerResult.getCode()))
		{
			// TODO should we throw an exception here or somewhere else, if status failed or postponed is set?
			// I mean, because the external system should not commit if it has been failed
			// or postponed but rollback, too.

			if (!failed && !postponed) // if failed or postponed, pending is reset on rollback
				clearPending();
		}
	}
	/**
	 * This method is called to set {@link #pending} to <tt>false</tt>. It sets
	 * {@link #endDT}, too. If it is not pending, the method silently returns without
	 * any action.
	 */
	public void clearPending()
	{
		if (!this.pending)
			return;

		this.pending = false;
		this.endDT = new Date();
	}
	
	/**
	 * when new Payment is created and the payment process begins BeginDT is sets to the current Timestamp.
	 * 
	 * @return Returns the beginDT.
	 */
	public Date getBeginDT()
	{
		return beginDT;
	}

	/**
	 * the end Payment Date is sets to the current TimeStamp when a Payment has finished processing (when pending is sets to False).
	 * @return Returns the endDT.
	 */
	public Date getEndDT()
	{
		return endDT;
	}
	
	
	/**
	 * @return Returns the user defined payment date
	 */
	public Date getPaymentDT() {
		return paymentDT;
	}

	/**
	 * Sets the Payment date of a <tt>Payment</tt> this date is defind by the User, 
	 * usually in the UI in the payment wizard dialog, so this is a User defined actuall payment date.
	 *
	 * @param paymentDT The User defined Payment Date.
	 */
	
	public void setPaymentDT(Date paymentDT) {
		this.paymentDT = paymentDT;
	}
	
	/**
	 * @return Returns the user. Will be <tt>null</tt> until this <tt>Payment</tt>
	 *		has been stored to the database the first time.
	 *
	 * @see #initUser(User)
	 */
	public User getUser()
	{
		return user;
	}
	/**
	 * This method is called when the Payment is stored to the database
	 * by {@link PaymentHelperBean#payBegin_storePaymentData(PaymentData)}.
	 * It can only be called once to initially set the user.
	 *
	 * @param user The user to set.
	 */
	protected void initUser(User user)
	{
		if (this.user != null)
			throw new IllegalStateException("user has already been initialized");

		this.user = user;
	}

	/**
	 * @return Returns the serverPaymentProcessorIDStrKey.
	 */
	public String getServerPaymentProcessorIDStrKey()
	{
		return serverPaymentProcessorIDStrKey;
	}
	/**
	 * @param serverPaymentProcessorID The serverPaymentProcessorID to set.
	 */
	public void setServerPaymentProcessorID(
			ServerPaymentProcessorID serverPaymentProcessorID)
	{
		this.serverPaymentProcessorID = serverPaymentProcessorID;

		if (serverPaymentProcessorID == null)
			this.serverPaymentProcessorIDStrKey = null;
		else
			this.serverPaymentProcessorIDStrKey = serverPaymentProcessorID.toString();
	}

	/**
	 * @return Returns the paymentDirection.
	 */
	public String getPaymentDirection()
	{
		return paymentDirection;
	}
	/**
	 * @param paymentDirection The paymentDirection to set.
	 */
	public void setPaymentDirection(String paymentDirection)
	{
		if (!PAYMENT_DIRECTION_INCOMING.equals(paymentDirection) &&
				!PAYMENT_DIRECTION_OUTGOING.equals(paymentDirection))
			throw new IllegalArgumentException("paymentDirection \""+paymentDirection+"\" is invalid!");

		this.paymentDirection = paymentDirection;
	}
	/**
	 * @return Returns the externalPaymentApproved.
	 */
	public boolean isExternalPaymentApproved()
	{
		return externalPaymentApproved;
	}
	/**
	 * @return Returns the externalPaymentDone.
	 */
	public boolean isExternalPaymentDone()
	{
		return externalPaymentDone;
	}

	/**
	 * If this method returns <tt>true</tt>, it means that the payment is complete
	 * and no further action will happen. If the <tt>Payment</tt> was postponed, you
	 * need to find out the follow-up.
	 *
	 * @return <tt>true</tt> if the <tt>Payment</tt> is neither pending nor
	 *		failed nor postponed. Otherwise <tt>false</tt>
	 */
	public boolean isSuccessfulAndComplete()
	{
		return !isFailed() && !isPending() && !isPostponed();
	}

	/**
	 * @return Returns the failed.
	 */
	public boolean isFailed()
	{
		return failed;
	}
	/**
	 * @return Returns the pending.
	 */
	public boolean isPending()
	{
		return pending;
	}
	/**
	 * @return Returns whether the <tt>Payment</tt> has been rolled back. This equals
	 *		{@link #ROLLBACK_STATUS_NOT_DONE}<tt> != rollbackStatus</tt>.
	 */
	public boolean isRolledBack()
	{
		return ROLLBACK_STATUS_NOT_DONE != rollbackStatus;
	}
	/**
	 * @return Returns the rollbackStatus.
	 */
	public byte getRollbackStatus()
	{
		return rollbackStatus;
	}
	/**
	 * @param rollbackStatus The rollbackStatus to set.
	 */
	public void setRollbackStatus(byte rollbackStatus)
	{
		if (ROLLBACK_STATUS_NOT_DONE == rollbackStatus)
			throw new IllegalArgumentException("Cannot undo a rollback and therefore cannot set ROLLBACK_STATUS_NOT_DONE!");

		if (ROLLBACK_STATUS_DONE_NORMAL != rollbackStatus &&
				ROLLBACK_STATUS_DONE_WITHOUT_ACTION != rollbackStatus)
			throw new IllegalArgumentException("rollbackStatus must be one of ROLLBACK_STATUS_DONE_NORMAL, ROLLBACK_STATUS_DONE_WITHOUT_ACTION!");

		if (!pending)
			throw new IllegalStateException("This payment ("+getPrimaryKey()+") is not pending!");

		this.rollbackStatus = rollbackStatus;
		clearPending();
	}

	/**
	 * @return Returns <tt>null</tt> if there was no failure. Otherwise the
	 *		first <tt>PaymentResult</tt> with {@link PaymentResult#CODE_FAILED} is
	 *		returned. The order is (1) begin client, (2) begin server, (3) end client, (4) end server).
	 */
	public PaymentResult getFailurePaymentResult()
	{
		PaymentResult res = getPayBeginClientResult();
		if (res != null && !res.isFailed())
			res = null;

		if (res == null)
			res = getPayBeginServerResult();

		if (res != null && !res.isFailed())
			res = null;

		if (res == null)
			res = getPayEndClientResult();

		if (res != null && !res.isFailed())
			res = null;

		if (res == null)
			res = getPayEndServerResult();

		if (res != null && !res.isFailed())
			res = null;

		return res;
	}

	/**
	 * @return Returns true if this payment is postponed.
	 *
	 * @see #postponed
	 */
	public boolean isPostponed()
	{
		return postponed;
	}

	/**
	 * If no <tt>PayableObject</tt>s are defined, a reasonForPayment must be declared.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String reasonForPayment = null;

// The following fields are not transferred to the server - only their IDs are.
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.accounting.pay.PayableObject"
	 *		table="JFireTrade_Payment_payableObjects"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
	nullValue=NullValue.EXCEPTION,
	table="JFireTrade_Payment_payableObjects",
	persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<PayableObject> payableObjects = new HashSet<PayableObject>();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ModeOfPaymentFlavour modeOfPaymentFlavour = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Currency currency = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private LegalEntity partner = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Account partnerAccount = null;
// The above fields are not transferred to the server - only their IDs are.

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long amount = 0;

// IDs for the transfer to the server - the following fields are not stored in the DB

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private Set<ObjectID> payableObjectIDs = null;

	/**
	 * @jdo.field persistence-modifier="transactional"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.TRANSACTIONAL)
	private ModeOfPaymentFlavourID modeOfPaymentFlavourID = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private CurrencyID currencyID = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private AnchorID partnerID = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private AnchorID partnerAccountID = null;

	/**
	 * @return Returns the payableObjects.
	 */
	public Set<PayableObject> getPayableObjects()
	{
		return payableObjects;
	}
	/**
	 * @return Returns the modeOfPaymentFlavour.
	 */
	public ModeOfPaymentFlavour getModeOfPaymentFlavour()
	{
		return modeOfPaymentFlavour;
	}

	/**
	 * @return Returns the modeOfPaymentFlavourID.
	 */
	public ModeOfPaymentFlavourID getModeOfPaymentFlavourID()
	{
		if (modeOfPaymentFlavour != null)
			return (ModeOfPaymentFlavourID) JDOHelper.getObjectId(modeOfPaymentFlavour);

		return modeOfPaymentFlavourID;
	}

	/**
	 * @return Returns the currency.
	 */
	public Currency getCurrency()
	{
		return currency;
	}
	/**
	 * @return Returns the amount.
	 */
	public long getAmount()
	{
		return amount;
	}

	/**
	 * Sets the {@link ObjectID}s.
	 * IMPORTANT: May only be called before the Payment is persisted, otherwise use {@link #setPayableObjects(Set)} instead.
	 *
	 * @param payableObjectIDs The objectIDs to set.
	 */
	public void setPayableObjectIDs(Set<ObjectID> payableObjectIDs)
	{
		if (payableObjectIDs == null)
			throw new NullPointerException("payableObjectIDs must not be null!");

		// check whether this object has not been persistet yet, only then setPayableObjectIDs is legal
		if (JDOHelper.getObjectId(this) != null) {
			throw new IllegalStateException("setPayableObjectIDs can only be called on NOT yet persisted instances of Payment, use setPayableObjects() instead!");
		}

		for (Iterator<ObjectID> it = payableObjectIDs.iterator(); it.hasNext(); ) {
			ObjectID objectID = it.next();
			if (objectID == null)
				throw new IllegalArgumentException("payableObjectIDs must not contain null entries!");
		}
		this.payableObjectIDs = payableObjectIDs;
	}


	/**
	 * Sets the {@link PayableObject}s.
	 * IMPORTANT: May only be called after the Paymnet is persisted, otherwise use {@link #setPayableObjectIDs(Set)} instead.
	 *
	 * @param payableObjects The PayableObject to set.
	 */
	@SuppressWarnings("unchecked")
	public void setPayableObjects(Set<? extends PayableObject> payableObjects)
	{
		if (payableObjects == null)
			throw new NullPointerException("payableObjects must not be null!");

		// check wether this object has been already persisted, only then setPayableObjects is legal
		if (JDOHelper.getObjectId(this) == null) {
			throw new IllegalStateException("setPayableObjects can only be called on already persisted instances of Payment, use setPayableObjectIDs() instead!");
		}

		if (payableObjectIDs == null)
			payableObjectIDs = new HashSet<ObjectID>();
		else
			payableObjectIDs.clear();

		for (Iterator<? extends PayableObject> it = payableObjects.iterator(); it.hasNext(); ) {
			PayableObject payableObject = it.next();
			payableObjectIDs.add((ObjectID) JDOHelper.getObjectId(payableObject));
		}
		this.payableObjects = (Set<PayableObject>) payableObjects;
	}

	/**
	 * @return Returns the objectIDs.
	 */
	public Set<ObjectID> getPayableObjectIDs()
	{
		if (payableObjectIDs == null && payableObjects != null) {
			Set<ObjectID> s = new HashSet<ObjectID>(payableObjects.size());

			for (Iterator<PayableObject> it = payableObjects.iterator(); it.hasNext(); ) {
				PayableObject payableObject = it.next();
				s.add((ObjectID) JDOHelper.getObjectId(payableObject));
			}

			payableObjectIDs = s;
		}
		return payableObjectIDs;
	}

	/**
	 * @return Returns the partnerID.
	 */
	public AnchorID getPartnerID()
	{
		if (partner != null)
			return (AnchorID) JDOHelper.getObjectId(partner);

		return partnerID;
	}

	/**
	 * @param currency The currency to set.
	 */
	public void setCurrency(Currency currency)
	{
		this.currency = currency;
		this.currencyID = (CurrencyID) JDOHelper.getObjectId(currency);
	}

	/**
	 * @return Returns the currencyID.
	 */
	public CurrencyID getCurrencyID()
	{
		if (currency != null)
			return (CurrencyID) JDOHelper.getObjectId(currency);

		return currencyID;
	}

	/**
	 * @return Returns the partnerAccount.
	 */
	public Account getPartnerAccount()
	{
		return partnerAccount;
	}

	/**
	 * @param partnerAccount The partnerAccount to set.
	 */
	public void setPartnerAccount(Account partnerAccount)
	{
		this.partnerAccount = partnerAccount;
		this.partnerAccountID = (AnchorID) JDOHelper.getObjectId(partnerAccount);
	}

	/**
	 * @param partnerAccountID The partnerAccountID to set.
	 */
	public void setPartnerAccountID(AnchorID partnerAccountID)
	{
		this.partnerAccountID = partnerAccountID;
		this.partnerAccount = null;
	}

	/**
	 * @return Returns the partnerAccountID.
	 */
	public AnchorID getPartnerAccountID()
	{
		if (partnerAccount != null)
			return (AnchorID) JDOHelper.getObjectId(partnerAccount);

		return partnerAccountID;
	}

	/**
	 * @param partner The partner to set.
	 */
	public void setPartner(LegalEntity partner)
	{
		this.partner = partner;
		this.partnerID = (AnchorID) JDOHelper.getObjectId(partner);
	}

	/**
	 * @return Returns the partner.
	 */
	public LegalEntity getPartner()
	{
		return partner;
	}

	/**
	 * @param partnerID The partnerID to set.
	 */
	public void setPartnerID(AnchorID partnerID)
	{
		this.partnerID = partnerID;
		this.partner = null;
	}

	/**
	 * @param modeOfPaymentFlavour The modeOfPaymentFlavour to set.
	 */
	public void setModeOfPaymentFlavour(ModeOfPaymentFlavour modeOfPaymentFlavour)
	{
		this.modeOfPaymentFlavour = modeOfPaymentFlavour;
		if (modeOfPaymentFlavour == null)
			this.modeOfPaymentFlavourID = null;
		else
			this.modeOfPaymentFlavourID = (ModeOfPaymentFlavourID) JDOHelper.getObjectId(modeOfPaymentFlavour);
	}

	/**
	 * @param currencyID The currencyID to set.
	 */
	public void setCurrencyID(CurrencyID currencyID)
	{
		this.currencyID = currencyID;
		this.currency = null;
	}
	/**
	 * @param modeOfPaymentFlavourID The modeOfPaymentFlavourID to set.
	 */
	public void setModeOfPaymentFlavourID(
			ModeOfPaymentFlavourID modeOfPaymentFlavourID)
	{
		this.modeOfPaymentFlavourID = modeOfPaymentFlavourID;
	}
	/**
	 * @param amount The amount to set.
	 */
	public void setAmount(long amount)
	{
		this.amount = amount;
	}

	/**
	 * @return Returns the reasonForPayment.
	 */
	public String getReasonForPayment()
	{
		return reasonForPayment;
	}
	/**
	 * @param reasonForPayment The reasonForPayment to set.
	 */
	public void setReasonForPayment(String reasonForPayment)
	{
		this.reasonForPayment = reasonForPayment;
	}

	/**
	 * @return Returns the clientPaymentProcessorFactoryID.
	 */
	public String getClientPaymentProcessorFactoryID()
	{
		return clientPaymentProcessorFactoryID;
	}
	/**
	 * @param clientPaymentProcessorFactoryID The clientPaymentProcessorFactoryID to set.
	 */
	public void setClientPaymentProcessorFactoryID(String clientPaymentProcessorFactoryID)
	{
		this.clientPaymentProcessorFactoryID = clientPaymentProcessorFactoryID;
	}

	/**
	 * @return Returns the precursor.
	 */
	public Payment getPrecursor()
	{
		return precursor;
	}
	/**
	 * @param precursor The precursor to set.
	 */
	public void setPrecursor(Payment precursor)
	{
		this.precursor = precursor;
		this.precursorID = (PaymentID) JDOHelper.getObjectId(precursor);
	}
	/**
	 * @param precursorID The precursorID to set.
	 */
	public void setPrecursorID(PaymentID precursorID)
	{
		this.precursorID = precursorID;
		this.precursor = null;
	}
	/**
	 * @return Returns the followUp.
	 */
	public Payment getFollowUp()
	{
		return followUp;
	}
	/**
	 * @return Returns the precursorID.
	 */
	public PaymentID getPrecursorID()
	{
		if (precursor != null)
			return (PaymentID) JDOHelper.getObjectId(precursor);

		return precursorID;
	}

	/**
	 * Returns the associated {@link PaymentLocal}.
	 * @return The associated {@link PaymentLocal}.
	 */
	public PaymentLocal getPaymentLocal() {
		return paymentLocal;
	}

	/**
	 * Sets the associated {@link PaymentLocal}.
	 * @param paymentLocal The {@link PaymentLocal} to be associated with this instance.
	 */
	public void setPaymentLocal(PaymentLocal paymentLocal) {
		this.paymentLocal = paymentLocal;
	}

	/**
	 * This method is called before a newly created <tt>Payment</tt> is sent to the
	 * server.
	 *
	 * @return Returns a new instance of <tt>Payment</tt> with {@link #modeOfPaymentFlavour},
	 *		{@link #currency} and {@link #payableObjects} being <tt>null</tt> in order to minimize
	 *		traffic when uploading.
	 *
	 * @see #jdoPreStore()
	 */
	protected Payment cloneForUpload()
	{
		Payment n = new Payment(organisationID, paymentID);
		n.amount = amount;
		n.beginDT = beginDT;
		n.paymentDT = paymentDT;
		n.clientPaymentProcessorFactoryID = clientPaymentProcessorFactoryID;
		n.currencyID = getCurrencyID();
		n.endDT = endDT;
		n.externalPaymentApproved = externalPaymentApproved;
		n.externalPaymentDone = externalPaymentDone;
		n.failed = failed;
		n.payableObjectIDs = getPayableObjectIDs();
//		n.followUp = followUp;
		n.precursorID = getPrecursorID();
		n.modeOfPaymentFlavourID = getModeOfPaymentFlavourID();
		n.partnerID = getPartnerID();
		n.partnerAccountID = getPartnerAccountID();
		n.payBeginClientResult = payBeginClientResult;
		n.payBeginServerResult = payBeginServerResult;
		n.payEndClientResult = payEndClientResult;
		n.payEndServerResult = payEndServerResult;
		n.paymentDirection = paymentDirection;
		n.pending = pending;
		n.postponed = postponed;
		n.reasonForPayment = reasonForPayment;
		n.rollbackStatus = rollbackStatus;
		n.serverPaymentProcessorIDStrKey = serverPaymentProcessorIDStrKey;
		return n;
	}

	/**
	 * This method resolves {@link #modeOfPaymentFlavour},
	 * {@link #currency} and {@link #payableObjects} by using the IDs before
	 * the object is written to datastore.
	 *
	 * @see javax.jdo.listener.StoreCallback#jdoPreStore()
	 */
	public void jdoPreStore()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Payment is currently not persistent! Cannot obtain PersistenceManager!");

		if (paymentDirection == null)
			throw new IllegalStateException("paymentDirection is not set!");

		if (partnerID != null && partner == null)
			partner = (LegalEntity) pm.getObjectById(partnerID);

		if (partnerAccountID != null && partnerAccount == null)
			partnerAccount = (Account) pm.getObjectById(partnerAccountID);

		if (modeOfPaymentFlavourID != null && modeOfPaymentFlavour == null)
			modeOfPaymentFlavour = (ModeOfPaymentFlavour) pm.getObjectById(modeOfPaymentFlavourID);

		if (currencyID != null && currency == null)
			currency = (Currency) pm.getObjectById(currencyID);

		if (payableObjectIDs != null) {
			if (payableObjects == null || payableObjects.size() != payableObjectIDs.size()) {
				payableObjects.clear();

				for (Iterator<ObjectID> it = payableObjectIDs.iterator(); it.hasNext(); ) {
					ObjectID objectID = it.next();
					PayableObject payableObject = (PayableObject) pm.getObjectById(objectID);
					payableObjects.add(payableObject);
				}
			} // if (payableObjects == null || payableObjects.size() != payableObjectIDs.size()) {
		} // if (payableObjectIDs != null) {

		if (precursorID != null && precursor != null) {
			precursor = (Payment) pm.getObjectById(precursorID);
			precursor.followUp = this;
		}

	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (paymentID ^ (paymentID >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		final Payment other = (Payment) obj;
		return (
				Util.equals(this.organisationID, other.organisationID) &&
				Util.equals(this.paymentID, other.paymentID)
		);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + ObjectIDUtil.longObjectIDFieldToString(paymentID) + ']';
	}
}
