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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.AccountType;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentFlavourID;
import org.nightlabs.jfire.accounting.pay.id.ModeOfPaymentID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.pay.id.ServerPaymentProcessorID"
 *		detachable="true"
 *		table="JFireTrade_ServerPaymentProcessor"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, serverPaymentProcessorID"
 *
 * @jdo.fetch-group name="ServerPaymentProcessor.name" fields="name"
 * @jdo.fetch-group name="ServerPaymentProcessor.modeOfPayments" fields="modeOfPayments"
 * @jdo.fetch-group name="ServerPaymentProcessor.modeOfPaymentFlavours" fields="modeOfPaymentFlavours"
 * @jdo.fetch-group name="ServerPaymentProcessor.this" fetch-groups="default" fields="name, modeOfPayments, modeOfPaymentFlavours"
 *
 * @!jdo.query name="getServerPaymentProcessorsForOneModeOfPaymentFlavour"
 *            query="SELECT
 *            	WHERE
 *            		modeOfPaymentFlavour.organisationID == paramOrganisationID &&
 *            		modeOfPaymentFlavour.modeOfPaymentFlavourID == paramModeOfPaymentFlavourID &&
 *            		(
 *            			this.modeOfPaymentFlavours.containsValue(modeOfPaymentFlavour) ||
 *            			this.modeOfPayments.containsValue(modeOfPaymentFlavour.modeOfPayment)
 *            		)
 *            	VARIABLES ModeOfPaymentFlavour modeOfPaymentFlavour
 *            	PARAMETERS String paramOrganisationID, String paramModeOfPaymentFlavourID
 *            	import java.lang.String;
 *            	import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour"
 *
 * @!jdo.query name="getServerPaymentProcessorsForOneModeOfPaymentFlavour"
 *            query="SELECT
 *            	WHERE
 *            		(
 *            			modeOfPaymentFlavour1.organisationID == paramOrganisationID &&
 *            			modeOfPaymentFlavour1.modeOfPaymentFlavourID == paramModeOfPaymentFlavourID &&
 *            			this.modeOfPaymentFlavours.containsValue(modeOfPaymentFlavour1)
 *            		) ||
 *            		(
 *            			modeOfPaymentFlavour2.organisationID == paramOrganisationID &&
 *            			modeOfPaymentFlavour2.modeOfPaymentFlavourID == paramModeOfPaymentFlavourID &&
 *            			this.modeOfPayments.containsValue(modeOfPaymentFlavour2.modeOfPayment)
 *            		)
 *            	VARIABLES ModeOfPaymentFlavour modeOfPaymentFlavour1; ModeOfPaymentFlavour modeOfPaymentFlavour2
 *            	PARAMETERS String paramOrganisationID, String paramModeOfPaymentFlavourID
 *            	import java.lang.String;
 *            	import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour"
 *
 * @jdo.query name="getServerPaymentProcessorsForOneModeOfPaymentFlavour_WORKAROUND1"
 *            query="SELECT
 *            	WHERE
 *            		modeOfPaymentFlavour.organisationID == paramOrganisationID &&
 *            		modeOfPaymentFlavour.modeOfPaymentFlavourID == paramModeOfPaymentFlavourID &&
 *            		this.modeOfPaymentFlavours.containsValue(modeOfPaymentFlavour)
 *            	VARIABLES ModeOfPaymentFlavour modeOfPaymentFlavour
 *            	PARAMETERS String paramOrganisationID, String paramModeOfPaymentFlavourID
 *            	import java.lang.String;
 *            	import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour"
 *
 * FIXME Maybe we can put WORKAROUND 1+2 into one query now... NO! The JPOX bug with the combination of && and || seems to still exist! Marco. 2006-05-16
 * @jdo.query name="getServerPaymentProcessorsForOneModeOfPaymentFlavour_WORKAROUND2"
 *            query="SELECT
 *            	WHERE
 *            		modeOfPaymentFlavour.organisationID == paramOrganisationID &&
 *            		modeOfPaymentFlavour.modeOfPaymentFlavourID == paramModeOfPaymentFlavourID &&
 *            		this.modeOfPayments.containsValue(modeOfPaymentFlavour.modeOfPayment)
 *            	VARIABLES ModeOfPaymentFlavour modeOfPaymentFlavour
 *            	PARAMETERS String paramOrganisationID, String paramModeOfPaymentFlavourID
 *            	import java.lang.String;
 *            	import org.nightlabs.jfire.accounting.pay.ModeOfPaymentFlavour"
 */
public abstract class ServerPaymentProcessor
implements Serializable, DetachCallback
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "ServerPaymentProcessor.name";

	public static final String FETCH_GROUP_MODE_OF_PAYMENTS = "ServerPaymentProcessor.modeOfPayments";

	public static final String FETCH_GROUP_MODE_OF_PAYMENT_FLAVOURS = "ServerPaymentProcessor.modeOfPaymentFlavours";

	public static final String FETCH_GROUP_THIS_SERVER_PAYMENT_PROCESSOR = "ServerPaymentProcessor.this";

	/**
	 * @return Returns a <tt>Collection</tt> with instances of type
	 *         {@link ServerPaymentProcessor}.
	 */
	public static Collection<ServerPaymentProcessor> getServerPaymentProcessorsForOneModeOfPaymentFlavour(
			PersistenceManager pm, ModeOfPaymentFlavour modeOfPaymentFlavour)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm must not be null!");
		if (modeOfPaymentFlavour == null)
			throw new IllegalArgumentException("modeOfPaymentFlavour must not be null!");

		return getServerPaymentProcessorsForOneModeOfPaymentFlavour(
				pm,
				modeOfPaymentFlavour.getOrganisationID(),
				modeOfPaymentFlavour.getModeOfPaymentFlavourID());
	}

	/**
	 * @return Returns a <tt>Collection</tt> with instances of type
	 *         {@link ServerPaymentProcessor}.
	 */
	public static Collection<ServerPaymentProcessor> getServerPaymentProcessorsForOneModeOfPaymentFlavour(
			PersistenceManager pm, ModeOfPaymentFlavourID modeOfPaymentFlavourID)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm must not be null!");
		if (modeOfPaymentFlavourID == null)
			throw new IllegalArgumentException("modeOfPaymentFlavourID must not be null!");

		return getServerPaymentProcessorsForOneModeOfPaymentFlavour(
				pm,
				modeOfPaymentFlavourID.organisationID,
				modeOfPaymentFlavourID.modeOfPaymentFlavourID);
	}

	/**
	 * @return Returns a <tt>Collection</tt> with instances of type
	 *         {@link ServerPaymentProcessor}.
	 */
	public static Collection<ServerPaymentProcessor> getServerPaymentProcessorsForOneModeOfPaymentFlavour(
			PersistenceManager pm, String organisationID,
			String modeOfPaymentFlavourID)
	{
		Set<ServerPaymentProcessor> res = new HashSet<ServerPaymentProcessor>();

		Query query;

//		query = pm.newNamedQuery(ServerPaymentProcessor.class,
//				"getServerPaymentProcessorsForOneModeOfPaymentFlavour");
//		return (Collection) query.execute(organisationID, modeOfPaymentFlavourID);

		query = pm.newNamedQuery(ServerPaymentProcessor.class, "getServerPaymentProcessorsForOneModeOfPaymentFlavour_WORKAROUND2");
		for (Iterator<?> it = ((Collection<?>) query.execute(organisationID, modeOfPaymentFlavourID)).iterator(); it.hasNext();) {
			ServerPaymentProcessor p = (ServerPaymentProcessor) it.next();
			res.add(p);
		}

		query = pm.newNamedQuery(ServerPaymentProcessor.class, "getServerPaymentProcessorsForOneModeOfPaymentFlavour_WORKAROUND1");
		for (Iterator<?> it = ((Collection<?>) query.execute(organisationID, modeOfPaymentFlavourID)).iterator(); it.hasNext();) {
			ServerPaymentProcessor p = (ServerPaymentProcessor) it.next();
			res.add(p);
		}

		return res;
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String serverPaymentProcessorID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * This <tt>Map</tt> stores all {@link ModeOfPayment}s which supported by
	 * this <tt>ServerPaymentProcessor</tt>. This means that all their flavours
	 * are included. If only some specific {@link ModeOfPaymentFlavour}s are
	 * supported, they must be put into {@link #modeOfPaymentFlavours}.
	 * <p>
	 * key: String modeOfPaymentPK <br/>
	 * value: ModeOfPayment modeOfPayment
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ModeOfPayment"
	 *		table="JFireTrade_ServerPaymentProcessor_modeOfPayments"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Map<String, ModeOfPayment> modeOfPayments;

	/**
	 * Unlike {@link #modeOfPayments}, this <tt>Map</tt> allows
	 * <tt>ServerPaymentProcessor</tt> to declare the support of a subset of the
	 * {@link ModeOfPaymentFlavour}s if not all of a given {@link ModeOfPayment}
	 * are supported.
	 * <p>
	 * key: String modeOfPaymentFlavourPK <br/>
	 * value: ModeOfPaymentFlavour modeOfPaymentFlavour
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ModeOfPaymentFlavour"
	 *		table="JFireTrade_ServerPaymentProcessor_modeOfPaymentFlavours"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Map<String, ModeOfPaymentFlavour> modeOfPaymentFlavours;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="serverPaymentProcessor"
	 */
	private ServerPaymentProcessorName name;

	/**
	 * @deprecated This constructor exists only for JDO and should never be used
	 *             explicitely!
	 */
	@Deprecated
	protected ServerPaymentProcessor() { }

	public ServerPaymentProcessor(String organisationID, String serverPaymentProcessorID)
	{
		this.organisationID = organisationID;
		this.serverPaymentProcessorID = serverPaymentProcessorID;
		this.primaryKey = getPrimaryKey(organisationID, serverPaymentProcessorID);

		this.name = new ServerPaymentProcessorName(this);
		this.modeOfPayments = new HashMap<String, ModeOfPayment>();
		this.modeOfPaymentFlavours = new HashMap<String, ModeOfPaymentFlavour>();
	}

	public static String getPrimaryKey(String organisationID,
			String serverPaymentProcessorID)
	{
		return organisationID + '/' + serverPaymentProcessorID;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException(
					"This ServerPaymentProcessor ("
							+ this.getPrimaryKey()
							+ ") is currently not persistent! Cannot obtain a PersistenceManager!");
		return pm;
	}

	/**
	 * @return Returns the primaryKey.
	 */
	public String getPrimaryKey()
	{
		return primaryKey;
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	/**
	 * @return Returns the serverPaymentProcessorID.
	 */
	public String getServerPaymentProcessorID()
	{
		return serverPaymentProcessorID;
	}

//	public static class PayApproveResult
//	{
//		public PayMoneyTransfer payMoneyTransfer;
//
//		public PaymentResult serverPaymentResult;
//
//		public PayApproveResult(PayMoneyTransfer payMoneyTransfer,
//				PaymentResult serverPaymentResult)
//		{
//			this.payMoneyTransfer = payMoneyTransfer;
//			this.serverPaymentResult = serverPaymentResult;
//		}
//	}

	public static class PayParams
	{
		public Accounting accounting;

		public User user;

		public PaymentData paymentData;

		/**
		 * @param accounting
		 * @param user
		 * @param paymentData The <tt>PaymentData</tt> (including <tt>Payment</tt>) of the current process.
		 */
		public PayParams(Accounting accounting, User user,
				PaymentData paymentData)
		{
			this.accounting = accounting;
			this.user = user;
			this.paymentData = paymentData;
		}
	}

	public void addModeOfPayment(ModeOfPayment modeOfPayment)
	{
		modeOfPayments.put(modeOfPayment.getPrimaryKey(), modeOfPayment);
	}

	public void removeModeOfPayment(ModeOfPaymentID modeOfPaymentID)
	{
		modeOfPayments.remove(ModeOfPayment.getPrimaryKey(
				modeOfPaymentID.organisationID, modeOfPaymentID.modeOfPaymentID));
	}

	public void removeModeOfPayment(String modeOfPaymentPK)
	{
		modeOfPayments.remove(modeOfPaymentPK);
	}

	/**
	 * @return Returns the modeOfPayments.
	 */
	public Collection<ModeOfPayment> getModeOfPayments()
	{
		return modeOfPayments.values();
	}

	public void addModeOfPaymentFlavour(ModeOfPaymentFlavour modeOfPaymentFlavour)
	{
		modeOfPaymentFlavours.put(modeOfPaymentFlavour.getPrimaryKey(),
				modeOfPaymentFlavour);
	}

	public void removeModeOfPaymentFlavour(
			ModeOfPaymentFlavourID modeOfPaymentFlavourID)
	{
		modeOfPaymentFlavours.remove(ModeOfPaymentFlavour.getPrimaryKey(
				modeOfPaymentFlavourID.organisationID,
				modeOfPaymentFlavourID.modeOfPaymentFlavourID));
	}

	public void removeModeOfPaymentFlavour(String modeOfPaymentFlavourPK)
	{
		modeOfPaymentFlavours.remove(modeOfPaymentFlavourPK);
	}

	/**
	 * @return Returns the modeOfPaymentFlavours.
	 */
	public Collection<ModeOfPaymentFlavour> getModeOfPaymentFlavours()
	{
		return modeOfPaymentFlavours.values();
	}

	protected Account getAccountOutside(PayParams payParams, String accountIDPrefix)
	{
		return getAccountOutside(payParams, accountIDPrefix, false);
	}

	/**
	 * Get an account for a payment process. This is a convenience method making
	 * implementation of an <code>ServerPaymentProcessor</code> easier.
	 *
	 * @param payParams the current payment situation
	 * @param accountIDPrefix a prefix for the account id.
	 * @param individualAccount If <code>true</code>, the business partner's ID will be
	 *		part of the account-id and thus one account per business partner will be used. If <code>false</code>,
	 *		the payment processor will use the same account for all business partners.
	 * @return the account to be used.
	 */
	protected Account getAccountOutside(PayParams payParams, String accountIDPrefix, boolean individualAccount)
	{
		PersistenceManager pm = getPersistenceManager();

		String organisationID = payParams.accounting.getOrganisationID();
		Payment payment = payParams.paymentData.getPayment();
		LegalEntity partner = payment.getPartner();
		Currency currency = payment.getCurrency();

		String accountID = AccountType.ANCHOR_TYPE_ID_PREFIX_OUTSIDE + accountIDPrefix + '#' + currency.getCurrencyID();
		if (individualAccount)
			accountID = accountID + '#' + partner.getOrganisationID() + '#' + partner.getAnchorTypeID() + '#' + partner.getAnchorID();

		Account account;
		try {
			pm.getExtent(Account.class);
			account = (Account) pm.getObjectById(AnchorID.create(organisationID,
					Account.ANCHOR_TYPE_ID_ACCOUNT,
					accountID));
		} catch (JDOObjectNotFoundException x) {
//			OrganisationLegalEntity organisationLegalEntity = OrganisationLegalEntity
//					.getOrganisationLegalEntity(pm, organisationID, Account.ANCHOR_TYPE_ID_OUTSIDE, true);
			AccountType accountType = (AccountType) pm.getObjectById(AccountType.ACCOUNT_TYPE_ID_OUTSIDE);

			account = new Account(
					organisationID,
					accountID,
					accountType,
					partner,
					currency);
			pm.makePersistent(account);
		}
		return account;
	}

	/**
	 * This method is called by the default implementation of
	 * {@link #payBegin(PayParams)} to get the "outside"
	 * <tt>Anchor</tt> for a payment. This might be a different account for each
	 * processor to provide easy checking possibility with an external agency
	 * (e.g. credit card company or bank). Additionally, it might be a good idea
	 * to use one Anchor per Partner - so we see quickly, which partner has
	 * used which PaymentProcessor(s).
	 * <p>
	 * Even if you overwrite the pay(...) method, you should use this method to
	 * obtain your "outside world anchor" to keep the API consistent.
	 * <p>
	 * It is a good idea to call {@link #getAccountOutside(PayParams, String)}.
	 * 
	 * @return Returns the <tt>Anchor</tt> from which or to which the money is
	 *         flowing outside the organisation.
	 */
	public abstract Anchor getAnchorOutside(PayParams payParams);

	/**
	 * Overwrite this method to implement the first phase of your server sided
	 * payment, if overwriting {@link #externalPayBegin(PayParams)} is not sufficient.
	 * <p>
	 * Note, that you MUST either throw a <tt>PaymentException</tt> or call sth. like
	 * <tt>payParams.paymentData.getPayment().setPayBeginServerResult(payBeginServerResult);</tt>.
	 *
	 * @return The base implementation of this method returns a new instance of <tt>PayMoneyTransfer</tt>
	 *		if no error occured and {@link Payment#isPostponed()}<tt> == false</tt>. If
	 *		it has been postponed, this method returns <tt>null</tt>. If the payment failed,
	 *		a <tt>PaymentException</tt> is thrown (hence, no result).
	 *
	 * @throws PaymentException
	 *           If sth. goes wrong you can either manually create a
	 *           PayApproveResult indicating failure or throw a PaymentException.
	 */
public PayMoneyTransfer payBegin(PayParams payParams)
	throws PaymentException
	{
		// find out from and to (one is the virtual treasury and one the partner's LegalEntity)
		Anchor from = null;
		Anchor to = null;

		if (Payment.PAYMENT_DIRECTION_INCOMING.equals(
				payParams.paymentData.getPayment().getPaymentDirection())) {
			from = getAnchorOutside(payParams);
			to = payParams.paymentData.getPayment().getPartner();
		}
		else {
			from = payParams.paymentData.getPayment().getPartner();
			to = getAnchorOutside(payParams);
		}

		PaymentResult payBeginServerResult;
		payBeginServerResult = externalPayBegin(payParams);

		if (payBeginServerResult == null) {
			payBeginServerResult = new PaymentResult(
					PaymentResult.CODE_APPROVED_NO_EXTERNAL,
					(String)null,
					(Throwable)null
					);
		}
		else if (payBeginServerResult.isFailed()) // in case of external failure, we don't even create a payMoneyTransfer
			throw new PaymentException(payBeginServerResult); // It would be OK to create it, as the rollback would remove it anyway. And it would - though created - not be booked by the anchors.

		payParams.paymentData.getPayment().setPayBeginServerResult(payBeginServerResult);

		if (payParams.paymentData.getPayment().isPostponed())
			return null;

		PayMoneyTransfer payMoneyTransfer = new PayMoneyTransfer(
				null,
				payParams.user,
				from,
				to,
				payParams.paymentData.getPayment()
			);
		payMoneyTransfer = getPersistenceManager().makePersistent(payMoneyTransfer);

		return payMoneyTransfer;
	}

	/**
	 * Implement this method to perform an external approval or similar initialization
	 * of the payment. At least the data should be checked for integrity!
	 * <p>
	 * If your
	 * external payment interface does not support two-phase payments (approve+pay),
	 * you should return <tt>null</tt> (if all was successful) and perform your
	 * one-step-payment in {@link #externalPayEnd(PayParams)}.
	 *
	 * @return Return simply <tt>null</tt> if you have no external work to do.
	 *         Otherwise, you must return a meaningful instance of
	 *         <tt>PaymentResult</tt>.
	 * @throws PaymentException
	 *           Throw a PaymentException, if your operation fails.
	 */
	protected abstract PaymentResult externalPayBegin(PayParams payParams)
			throws PaymentException;

	/**
	 * This is the second phase of the payment process, executed after
	 * {@link #payBegin(PayParams)} and before {@link #payEnd(PayParams)}. It
	 * delegates to {@link #externalPayDoWork(PayParams)}.
	 */
	public void payDoWork(PayParams payParams)
	throws PaymentException
	{
		Payment payment = payParams.paymentData.getPayment();
		PaymentResult payDoWorkServerResult = externalPayDoWork(payParams); 
	
		if (payDoWorkServerResult == null) {
			if (payment.isPostponed()) {
				payDoWorkServerResult = new PaymentResult(
						PaymentResult.CODE_POSTPONED,
						(String)null,
						(Throwable)null);
			}
			else {
				payDoWorkServerResult = new PaymentResult(
						PaymentResult.CODE_PAID_NO_EXTERNAL,
						(String)null,
						(Throwable)null);
			}
		}

		payParams.paymentData.getPayment().setPayDoWorkServerResult(payDoWorkServerResult);
	}

	/**
	 * This is the external part of the second phase of the payment process.
	 * <p>
	 * In your implementation of this method, you must do the actual payment in
	 * the external system if - and only if - it can still be rolled back afterwards,
	 * in case another payment/delivery (executed together) fails. If your external
	 * payment process supports only two phases (approve & commit/rollback), you
	 * should simply return <tt>null</tt>.
	 * <p> 
	 * This method is called after
	 * {@link #externalPayBegin(PayParams)} and before
	 * {@link #externalPayCommit(PayParams)}/{@link #externalPayRollback(PayParams)}.
	 *
	 * @return Return simply <tt>null</tt> if you have no external work to do.
	 *         Otherwise, you must return a meaningful instance of
	 *         <tt>PaymentResult</tt>.
	 *         Usually with {@link PaymentResult#CODE_PAID_WITH_EXTERNAL}.
	 * @throws PaymentException
	 *           Throw a PaymentException, if your operation fails.
	 */
	protected abstract PaymentResult externalPayDoWork(PayParams payParams)
	throws PaymentException;

	/**
	 * This is the third phase of the server payment.
	 * <p>
	 * Note, that you MUST either throw a <tt>PaymentException</tt> or call sth. like
	 * <tt>payParams.paymentData.getPayment().setPayEndServerResult(payEndServerResult);</tt>.
	 * It's much wiser not to override this method but just implement {@link #externalPayCommit(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams)}
	 * and {@link #externalPayRollback(org.nightlabs.jfire.accounting.pay.ServerPaymentProcessor.PayParams).
	 *
	 * @param payParams
	 *
	 * @throws PaymentException
	 *           If sth. goes wrong you can either manually create a
	 *           PayApproveResult indicating failure or throw a PaymentException.
	 */
	public void payEnd(PayParams payParams)
	throws PaymentException
	{
		Payment payment = payParams.paymentData.getPayment();
		PaymentResult payEndServerResult;
		if (payment.isForceRollback() || payment.isFailed()) {
			payEndServerResult = externalPayRollback(payParams); 
	
			if (payEndServerResult == null) {
				payEndServerResult = new PaymentResult(
						PaymentResult.CODE_ROLLED_BACK_NO_EXTERNAL,
						(String)null,
						(Throwable)null);
			}
		}
		else {
			payEndServerResult = externalPayCommit(payParams); 
			
			if (payEndServerResult == null) {
				if (payment.isPostponed()) {
					payEndServerResult = new PaymentResult(
							PaymentResult.CODE_POSTPONED,
							(String)null,
							(Throwable)null);					
				}
				else {
					payEndServerResult = new PaymentResult(
							PaymentResult.CODE_COMMITTED_NO_EXTERNAL,
							(String)null,
							(Throwable)null);
				}
			}
		}

		payParams.paymentData.getPayment().setPayEndServerResult(payEndServerResult);
	}

	/**
	 * In your implementation of this method, you must commit the payment in
	 * the external system.
	 *
	 * @return Return simply <tt>null</tt> if you have no external work to do.
	 *         Otherwise, you must return a meaningful instance of
	 *         <tt>PaymentResult</tt>.
	 *         Usually with {@link PaymentResult#CODE_COMMITTED_WITH_EXTERNAL}.
	 * @throws PaymentException
	 *           Throw a PaymentException, if your operation fails.
	 */
	protected abstract PaymentResult externalPayCommit(PayParams payParams)
	throws PaymentException;
	
	/**
	 * In your implementation of this method, you must rollback the payment in
	 * the external system. 
	 *
	 * @return Return simply <tt>null</tt> if you have no external work to do.
	 *         Otherwise, you must return a meaningful instance of
	 *         <tt>PaymentResult</tt>.
	 *         Usually with {@link PaymentResult#CODE_ROLLED_BACK_WITH_EXTERNAL}.
	 * @throws PaymentException
	 *           Throw a PaymentException, if your operation fails.
	 */
	protected abstract PaymentResult externalPayRollback(PayParams payParams)
	throws PaymentException;

	/**
	 * @return Returns the name.
	 */
	public ServerPaymentProcessorName getName()
	{
		return name;
	}

	/**
	 * The default implementation of this method returns <tt>null</tt>, meaning that
	 * all client payment processors (which are registered on the same
	 * {@link ModeOfPayment}s/{@link ModeOfPaymentFlavour}s are allowed. If you
	 * want to reduce it to a certain subset, you can return here a <tt>Set</tt>
	 * containing all IDs (as specified in the client's <tt>plugin.xml</tt>).
	 * <p>
	 * Note, that this method may be called in both, client and server.
	 *
	 * @return Returns either <tt>null</tt> or a <tt>Set</tt> of <tt>String</tt>,
	 *		where each one represents an extension id (for the extension-point
	 *		"org.nightlabs.jfire.trade.clientPaymentProcessorFactory") as specified
	 *		in the client's <tt>plugin.xml</tt>.
	 *
	 * @see #getExcludedClientPaymentProcessorFactoryIDs()
	 */
	public Set<String> getIncludedClientPaymentProcessorFactoryIDs()
	{
		return null;
	}

	/**
	 * Unlike {@link #getIncludedClientPaymentProcessorFactoryIDs()}, this
	 * method rather explicitely excludes client payment processors. You can only
	 * overwrite one of the methods, because this one is ignored if
	 * {@link #getIncludedClientPaymentProcessorFactoryIDs()}
	 * returned a result <tt>!= null</tt>. Either include or exclude.
	 * <p>
	 * The default implementation of this method returns <tt>null</tt>.
	 * <p>
	 * Note, that this method may be called in both, client and server.
	 *
	 * @return Returns either <tt>null</tt> or a <tt>Set</tt> of <tt>String</tt>,
	 *		where each one represents an extension id (for the extension-point
	 *		"org.nightlabs.jfire.trade.clientPaymentProcessorFactory") as specified
	 *		in the client's <tt>plugin.xml</tt>.
	 *
	 * @see #getIncludedClientPaymentProcessorFactoryIDs()
	 */
	public Set<String> getExcludedClientPaymentProcessorFactoryIDs()
	{
		return null;
	}
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private String requirementCheckKey;
	
	/**
	 * This method returns null if all requirements are met and a descriptive string
	 * otherwise.
	 * 
	 * @return null if all requirements are met and a descriptive string otherwise.
	 */
	public String getRequirementCheckKey() {
		return requirementCheckKey;
	}

//	public void setRequirementCheckKey(String reqMsg) {
//		this.requirementCheckKey = reqMsg;
//	}

	/**
	 * This method is not supposed to be called from outside.
	 * Extendors should implement {@link #_checkRequirements(CheckRequirementsEnvironment)} instead of this method.
	 * @param checkRequirementsEnvironment TODO
	 */
	public void checkRequirements(CheckRequirementsEnvironment checkRequirementsEnvironment) {
		requirementCheckKey = _checkRequirements(checkRequirementsEnvironment);
	}

	/**
	 * Extendors should override this method if their {@link ServerPaymentProcessor} if
	 * it needs to ensure some requirements before it can be used. If everything is ok, this
	 * method should return null. Otherwise a string describing the failure should be returned.
	 * @param checkRequirementsEnvironment TODO
	 * 
	 * @return null if everything is ok, a descriptive string otherwise.
	 */
	protected String _checkRequirements(CheckRequirementsEnvironment checkRequirementsEnvironment) {
		return null;
	}

	public void jdoPreDetach()
	{
		// nothing to do
	}
	public void jdoPostDetach(Object o)
	{
		ServerPaymentProcessor detached = this;
		ServerPaymentProcessor attached = (ServerPaymentProcessor) o;

		detached.requirementCheckKey = attached.requirementCheckKey;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof ServerPaymentProcessor)) return false;
		ServerPaymentProcessor o = (ServerPaymentProcessor) obj;
		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.serverPaymentProcessorID, o.serverPaymentProcessorID);
	}
	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) + Util.hashCode(serverPaymentProcessorID);
	}
}
