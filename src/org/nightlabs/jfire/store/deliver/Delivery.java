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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;
import org.nightlabs.jfire.store.deliver.id.ModeOfDeliveryFlavourID;
import org.nightlabs.jfire.store.deliver.id.ServerDeliveryProcessorID;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticleLocal;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.math.Base62Coder;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.deliver.id.DeliveryID"
 *		detachable="true"
 *		table="JFireTrade_Delivery"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, deliveryID"
 *		include-body="id/DeliveryID.body.inc"
 */
public class Delivery
implements Serializable, StoreCallback
{
	public static final String DELIVERY_DIRECTION_INCOMING = "incoming";

	public static final String DELIVERY_DIRECTION_OUTGOING = "outgoing";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String deliveryID;

	/**
	 * This is used for postponed <tt>Delivery</tt>s. To follow up a previously
	 * postponed delivery, you have to set this (or the {@link #precursorID})
	 * to the previous delivery (which has not been completed, but postponed).
	 * This will cause {@link #followUp} of the previous <tt>Delivery</tt> to point
	 * to the new one while <tt>precursor</tt> in the new <tt>Delivery</tt> points
	 * back to the old one.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Delivery precursor = null;

	/**
	 * @see #precursor
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	private DeliveryID precursorID = null;
	
	/**
	 * This is used for postponed <tt>Delivery</tt>s. If this <tt>Delivery</tt> has been
	 * postponed and later followed up by a new one, this field will point to the
	 * new <tt>Delivery</tt>. In case, the new <tt>Delivery</tt> failed and a second
	 * follow-up-<tt>Delivery</tt> is done, this will point to the newest follow-up
	 * <tt>Delivery</tt> (and therefore at the end always to one that did not fail).
	 *
	 * @see #precursor
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Delivery followUp = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User user = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String clientDeliveryProcessorFactoryID;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient ServerDeliveryProcessorID serverDeliveryProcessorID = null;

	/**
	 * @return Returns the serverDeliveryProcessorID. Might be <tt>null</tt> if client
	 *		doesn't choose one.
	 */
	public ServerDeliveryProcessorID getServerDeliveryProcessorID()
	{
		if (serverDeliveryProcessorID == null) {
			if (serverDeliveryProcessorIDStrKey != null) {
				try {
					serverDeliveryProcessorID = new ServerDeliveryProcessorID(serverDeliveryProcessorIDStrKey);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return serverDeliveryProcessorID;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String serverDeliveryProcessorIDStrKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String deliveryDirection = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date beginDT;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private DeliveryResult deliverBeginClientResult = null;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private DeliveryResult deliverBeginServerResult = null;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private DeliveryResult deliverDoWorkClientResult = null;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private DeliveryResult deliverDoWorkServerResult = null;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private DeliveryResult deliverEndClientResult = null;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private DeliveryResult deliverEndServerResult = null;

	/**
	 * A delivery can be postponed by a {@link ServerDeliveryProcessor} setting
	 * {@link DeliveryResult#CODE_POSTPONED} as <tt>DeliveryResult</tt> for the
	 * current delivery phase. If this happens, there are two possibilities:
	 * <ul>
	 *  <li>
	 *   The <tt>Delivery</tt> is postponed by
	 *   {@link ServerDeliveryProcessor#deliverBegin(DeliverParams)} or even already before
	 *   that by the client. In this case, your <tt>ServerDeliveryProcessor</tt>
	 *   doesn't even need to create a <tt>DeliverMoneyTransfer</tt> because it will NOT
	 *   be stored anyway.    
	 *  </li>
	 *  <li>
	 *   The <tt>Delivery</tt> can still be postponed during
	 *   {@link ServerDeliveryProcessor#deliverEnd(DeliverParams)}. This is not recommended!
	 *   It causes the delivery process to be rollbacked. The creation and booking
	 *   of a <tt>DeliverMoneyTransfer</tt> and the rollback of it are very expensive in
	 *   comparison to postponing in <tt>deliverBegin(...)</tt>. Whenever possible, please
	 *   postpone already in <tt>deliverBegin(...)</tt>!!!
	 *  </li>
	 * </ul>
	 * <tt>postponed</tt> becomes true automatically by setting a <tt>DeliveryResult</tt>
	 * to this <tt>Delivery</tt> which has the code {@link DeliveryResult#CODE_POSTPONED}.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean postponed = false;

	/**
	 * Is set <tt>false</tt>, after all has been done. As long as it is <tt>true</tt>,
	 * the delivery process is pending and still needs to be completed by a server-
	 * side <tt>deliverEnd(..)</tt>. If an error occurs, <tt>failed</tt> is set to <tt>true</tt>
	 * and after the rollback has been done, <tt>pending</tt> is set to <tt>false</tt>.
	 * If the delivery ends successful, <tt>pending</tt> is set to <tt>false</tt>, too. So
	 * <tt>pending == false && failed == false</tt> indicates a success.
	 * <p>
	 * When <tt>pending</tt> is set <tt>false</tt>, {@link #endDT} is set to the current
	 * timestamp.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean pending = true;

	/**
	 * Is set <tt>true</tt>, as soon as the external delivery is approved. It is
	 * set automatically, when {@link #externalDeliveryDone} is set <tt>true</tt>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean externalDeliveryApproved = false;

	/**
	 * Is set <tt>true</tt>, as soon as the external delivery is complete.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean externalDeliveryDone = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean failed = false;

	/**
	 * This is set automatically when pending is set to <tt>false</tt>
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date endDT = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
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
	 * A rollback has been done, but there was no {@link org.nightlabs.jfire.store.deliver.DeliverMoneyTransfer}
	 * which could have been undone. Hence the rollback didn't do anything except changing
	 * this flag.
	 */
	public static final byte ROLLBACK_STATUS_DONE_WITHOUT_ACTION = 1;
	/**
	 * A rollback has been done and the {@link org.nightlabs.jfire.store.deliver.DeliverMoneyTransfer}
	 * including all dependent {@link org.nightlabs.jfire.store.MoneyTransfer}s have
	 * been deleted. All involved anchors got previously informed by
	 * {@link org.nightlabs.jfire.transfer.Anchor#rollbackTransfer(User, Transfer, Map)}.
	 * <p>
	 * Note: If the <tt>DeliverMoneyTransfer</tt> has not been booked (but only created),
	 * it will of course not be rolled back on the anchors, but the status will be
	 * this one, too (and the transfer deleted from datastore).
	 */
	public static final byte ROLLBACK_STATUS_DONE_NORMAL = 2;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private byte rollbackStatus = ROLLBACK_STATUS_NOT_DONE;

	protected static Base62Coder base62Coder = null;
	protected static Base62Coder getBase62Coder()
	{
		if (base62Coder == null)
			base62Coder = new Base62Coder();

		return base62Coder;
	}

	public Delivery(String organisationID, DeliveryID precursorID)
	{
		this(organisationID);
		this.precursorID = precursorID;
	}

	/**
	 * This constructor will autogenerate the <tt>deliveryID</tt> with timestamp
	 * + '-' + random. The numbers are base62encoded.
	 */
	public Delivery(String organisationID)
	{
		this(organisationID,
				getBase62Coder().encode(System.currentTimeMillis(), 1)
				+ '-' +
				getBase62Coder().encode((long)(Math.random() * Integer.MAX_VALUE), 1));
	}

	public Delivery(String organisationID, String deliveryID)
	{
		if (organisationID == null)
			throw new NullPointerException("organisationID");

		if (deliveryID == null)
			throw new NullPointerException("deliveryID");

		this.organisationID = organisationID;
		this.deliveryID = deliveryID;

		this.beginDT = new Date();
//		this.deliveryDirection = DELIVERY_DIRECTION_INCOMING;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected Delivery() { }

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the deliveryID.
	 */
	public String getDeliveryID()
	{
		return deliveryID;
	}

	public static String getPrimaryKey(String organisationID, String deliveryID)
	{
		return organisationID + '/' + deliveryID;
	}

	public String getPrimaryKey()
	{
		return getPrimaryKey(organisationID, deliveryID);
	}

	/**
	 * @return Returns the deliverBeginClientResult.
	 */
	public DeliveryResult getDeliverBeginClientResult()
	{
		return deliverBeginClientResult;
	}

	protected void updateStatus(DeliveryResult deliveryResult)
	{
		if (DeliveryResult.CODE_APPROVED_WITH_EXTERNAL.equals(
				deliveryResult.getCode()))
		{
			externalDeliveryApproved = true;
		}

		if (DeliveryResult.CODE_DELIVERED_WITH_EXTERNAL.equals(
				deliveryResult.getCode()))
		{
			externalDeliveryApproved = true;
			externalDeliveryDone = true;
		}

		if (deliveryResult.isFailed())
		{
			failed = true;
		}

		if (DeliveryResult.CODE_POSTPONED.equals(
				deliveryResult.getCode()))
		{
			postponed = true;
		}
	}

	/**
	 * @param deliverBeginClientResult The deliverBeginClientResult to set.
	 */
	public void setDeliverBeginClientResult(DeliveryResult deliverBeginClientResult)
	{
		this.deliverBeginClientResult = deliverBeginClientResult;
		updateStatus(deliverBeginClientResult);
	}
	/**
	 * @return Returns the deliverBeginServerResult.
	 */
	public DeliveryResult getDeliverBeginServerResult()
	{
		return deliverBeginServerResult;
	}

	/**
	 * @param deliverBeginServerResult The deliverBeginServerResult to set.
	 */
	public void setDeliverBeginServerResult(DeliveryResult deliverBeginServerResult)
	{
		this.deliverBeginServerResult = deliverBeginServerResult;
		updateStatus(deliverBeginServerResult);
	}
	/**
	 * @return Returns the deliverEndClientResult.
	 */
	public DeliveryResult getDeliverEndClientResult()
	{
		return deliverEndClientResult;
	}
	/**
	 * @return Returns the deliverDoWorkClientResult.
	 */
	public DeliveryResult getDeliverDoWorkClientResult()
	{
		return deliverDoWorkClientResult;
	}
	/**
	 * @param deliverDoWorkClientResult The deliverDoWorkClientResult to set.
	 */
	public void setDeliverDoWorkClientResult(
			DeliveryResult deliverDoWorkClientResult)
	{
		this.deliverDoWorkClientResult = deliverDoWorkClientResult;
		updateStatus(deliverDoWorkClientResult);
	}
	/**
	 * @return Returns the deliverDoWorkServerResult.
	 */
	public DeliveryResult getDeliverDoWorkServerResult()
	{
		return deliverDoWorkServerResult;
	}
	/**
	 * @param deliverDoWorkServerResult The deliverDoWorkServerResult to set.
	 */
	public void setDeliverDoWorkServerResult(
			DeliveryResult deliverDoWorkServerResult)
	{
		this.deliverDoWorkServerResult = deliverDoWorkServerResult;
		updateStatus(deliverDoWorkServerResult);
	}
	/**
	 * Step 3, performed first in client in a copy of Delivery.
	 * Then, in the server, after the client has uploaded its
	 * DeliveryResult, before the server tries local commit (hence, this method
	 * is called in a separate transaction ("RequiresNew").
	 *
	 * @param deliverEndClientResult The deliverEndClientResult to set.
	 */
	public void setDeliverEndClientResult(DeliveryResult deliverEndClientResult)
	{
		this.deliverEndClientResult = deliverEndClientResult;
		updateStatus(deliverEndClientResult);
	}
	/**
	 * @return Returns the deliverEndServerResult.
	 */
	public DeliveryResult getDeliverEndServerResult()
	{
		return deliverEndServerResult;
	}
	/**
	 * Step 4, performed
	 *
	 * @param deliverEndServerResult The deliverEndServerResult to set.
	 */
	public void setDeliverEndServerResult(DeliveryResult deliverEndServerResult)
	{
		this.deliverEndServerResult = deliverEndServerResult;
		updateStatus(deliverEndServerResult);

		if (DeliveryResult.CODE_COMMITTED_WITH_EXTERNAL.equals(deliverEndServerResult.getCode()) ||
				DeliveryResult.CODE_COMMITTED_NO_EXTERNAL.equals(deliverEndServerResult.getCode()))
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
	 * @return Returns the beginDT.
	 */
	public Date getBeginDT()
	{
		return beginDT;
	}

	/**
	 * @return Returns the user. Will be <tt>null</tt> until this <tt>Delivery</tt>
	 *		has been stored to the database the first time.
	 *
	 * @see #initUser(User)
	 */
	public User getUser()
	{
		return user;
	}
	/**
	 * This method is called when the Delivery is stored to the database
	 * by {@link DeliveryHelperBean#deliverBegin_storeDeliveryData(DeliveryData)}.
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
	 * @return Returns the serverDeliveryProcessorIDStrKey.
	 */
	public String getServerDeliveryProcessorIDStrKey()
	{
		return serverDeliveryProcessorIDStrKey;
	}
	/**
	 * @param serverDeliveryProcessorID The serverDeliveryProcessorID to set.
	 */
	public void setServerDeliveryProcessorID(
			ServerDeliveryProcessorID serverDeliveryProcessorID)
	{
		this.serverDeliveryProcessorID = serverDeliveryProcessorID;

		if (serverDeliveryProcessorID == null)
			this.serverDeliveryProcessorIDStrKey = null;
		else
			this.serverDeliveryProcessorIDStrKey = serverDeliveryProcessorID.toString();
	}

	/**
	 * @return Returns the deliveryDirection.
	 */
	public String getDeliveryDirection()
	{
		return deliveryDirection;
	}
	/**
	 * @param deliveryDirection The deliveryDirection to set.
	 */
	public void setDeliveryDirection(String deliveryDirection)
	{
		if (!DELIVERY_DIRECTION_INCOMING.equals(deliveryDirection) &&
				!DELIVERY_DIRECTION_OUTGOING.equals(deliveryDirection))
			throw new IllegalArgumentException("deliveryDirection \""+deliveryDirection+"\" is invalid!");

		this.deliveryDirection = deliveryDirection;
	}
	/**
	 * @return Returns the externalDeliveryApproved.
	 */
	public boolean isExternalDeliveryApproved()
	{
		return externalDeliveryApproved;
	}
	/**
	 * @return Returns the externalDeliveryDone.
	 */
	public boolean isExternalDeliveryDone()
	{
		return externalDeliveryDone;
	}

	/**
	 * If this method returns <tt>true</tt>, it means that the delivery is complete
	 * and no further action will happen. If the <tt>Delivery</tt> was postponed, you
	 * need to find out the follow-up.
	 *
	 * @return <tt>true</tt> if the <tt>Delivery</tt> is neither pending nor
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
	 * @return Returns whether the <tt>Delivery</tt> has been rolled back. This equals
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
			throw new IllegalStateException("This delivery ("+getPrimaryKey()+") is not pending!");

		this.rollbackStatus = rollbackStatus;
		clearPending();
	}

	/**
	 * @return Returns <tt>null</tt> if there was no failure. Otherwise the
	 *		first <tt>DeliveryResult</tt> with {@link DeliveryResult#CODE_FAILED} is
	 *		returned. The order is (1) begin client, (2) begin server, (3) end client, (4) end server). 
	 */
	public DeliveryResult getFailureDeliveryResult()
	{
		DeliveryResult res = getDeliverBeginClientResult();
		if (res != null && !res.isFailed())
			res = null;

		if (res == null)
			res = getDeliverBeginServerResult();

		if (res != null && !res.isFailed())
			res = null;

		if (res == null)
			res = getDeliverEndClientResult();

		if (res != null && !res.isFailed())
			res = null;

		if (res == null)
			res = getDeliverEndServerResult();

		if (res != null && !res.isFailed())
			res = null;

		return res;
	}

	/**
	 * @return Returns true if this delivery is postponed.
	 * 
	 * @see #postponed
	 */
	public boolean isPostponed()
	{
		return postponed;
	}
	/**
	 * @return Returns the endDT.
	 */
	public Date getEndDT()
	{
		return endDT;
	}

//	/**
//	 * If no <tt>DeliveryNote</tt>s are defined, a reasonForDelivery must be declared.
//	 *
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private String reasonForDelivery = null;

// The following fields are not transferred to the server - only their IDs are. They're linked in jdoPreStore
//	/**
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="collection"
//	 *		element-type="org.nightlabs.jfire.store.Product"
//	 *		table="JFireTrade_Delivery_products"
//	 *
//	 * @jdo.join
//	 */
//	private Collection products = null;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.trade.ArticleLocal"
	 *		mapped-by="delivery"
	 */	
	private Collection<ArticleLocal> articleLocals = null;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.store.DeliveryNote"
	 *		table="JFireTrade_Delivery_deliveryNotes"
	 * 
	 * @jdo.join
	 */
	private Set<DeliveryNote> deliveryNotes = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ModeOfDeliveryFlavour modeOfDeliveryFlavour = null;

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private Currency currency = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LegalEntity partner = null;

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private Account partnerAccount = null;

// The above fields are not transferred to the server - only their IDs are.
	
//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private long amount = 0;
	
// IDs for the transfer to the server - the following fields are not stored in the DB

//	/**
//	 * @jdo.field persistence-modifier="transactional"
//	 */
//	private Collection productIDs = null;
	/**
	 * @jdo.field persistence-modifier="transactional"
	 */
	private Collection<ArticleID> articleIDs = null;

	/**
	 * @jdo.field persistence-modifier="transactional"
	 */
	private transient Collection<Article> _articles = null;

//	/**
//	 * @jdo.field persistence-modifier="transactional"
//	 */
//	private Set deliveryNoteIDs = null;

	/**
	 * @jdo.field persistence-modifier="transactional"
	 */
	private ModeOfDeliveryFlavourID modeOfDeliveryFlavourID = null;

//	/**
//	 * @jdo.field persistence-modifier="transactional"
//	 */
//	private CurrencyID currencyID = null;

	/**
	 * @jdo.field persistence-modifier="transactional"
	 */
	private AnchorID partnerID = null;

//	/**
//	 * @jdo.field persistence-modifier="transactional"
//	 */
//	private AnchorID partnerAccountID = null;

	/**
	 * @return Returns the deliveryNotes.
	 */
	public Collection<DeliveryNote> getDeliveryNotes()
	{
		return deliveryNotes;
	}

	/**
	 * @jdo.field persistence-modifier="transactional"
	 */
	private Set<DeliveryNoteID> deliveryNoteIDs = null;

	public Set<DeliveryNoteID> getDeliveryNoteIDs()
	{
		if (deliveryNoteIDs == null) {
			Set dnids = new HashSet();
			for (Iterator it = deliveryNotes.iterator(); it.hasNext(); ) {
				DeliveryNote deliveryNote = (DeliveryNote) it.next();
				dnids.add((DeliveryNoteID) JDOHelper.getObjectId(deliveryNote));
			}
			deliveryNoteIDs = dnids;
		}
		return deliveryNoteIDs;
	}

	/**
	 * @return Returns the modeOfDeliveryFlavour.
	 */
	public ModeOfDeliveryFlavour getModeOfDeliveryFlavour()
	{
		return modeOfDeliveryFlavour;
	}
	
	/**
	 * @return Returns the modeOfDeliveryFlavourID.
	 */
	public ModeOfDeliveryFlavourID getModeOfDeliveryFlavourID()
	{
		if (modeOfDeliveryFlavour != null)
			return (ModeOfDeliveryFlavourID) JDOHelper.getObjectId(modeOfDeliveryFlavour);

		return modeOfDeliveryFlavourID;
	}

//	/**
//	 * @return Returns the currency.
//	 */
//	public Currency getCurrency()
//	{
//		return currency;
//	}
//	/**
//	 * @return Returns the amount.
//	 */
//	public long getAmount()
//	{
//		return amount;
//	}

	/**
	 * @return Returns instances of {@link org.nightlabs.jfire.trade.id.ArticleID}.
	 */
	public Collection<ArticleID> getArticleIDs()
	{
		if (articleIDs == null) {
			HashSet s = new HashSet();
			for (Article article : getArticles()) {
				s.add((ArticleID) JDOHelper.getObjectId(article));
			}
			articleIDs = s;
		}

		return articleIDs;
	}

	/**
	 * @return Returns instances of {@link ArticleLocal}.
	 */
	public Collection getArticleLocals()
	{
		return articleLocals;
	}

	/**
	 * @return Returns instances of {@link org.nightlabs.jfire.trade.Article}.
	 */
	public Collection<Article> getArticles()
	{
		if (_articles == null) {
			Set res = new HashSet();
			for (Iterator it = articleLocals.iterator(); it.hasNext();) {
				ArticleLocal articleLocal = (ArticleLocal) it.next();
				res.add(articleLocal.getArticle());
			}
			_articles = res;
		}
		return _articles;
	}
	/**
	 * @param articleIDs Instances of {@link org.nightlabs.jfire.trade.id.ArticleID}
	 */
	public void setArticleIDs(Collection articleIDs)
	{
		if (articleIDs == null)
			throw new IllegalArgumentException("articleIDs must not be null!");

		if (articleLocals != null)
			throw new IllegalStateException("Articles cannot be changed afterwards! They have already been set!");
//			articles = null;

		// check types in collection
		for (Iterator it = articleIDs.iterator(); it.hasNext(); ) {
			ArticleID articleID = (ArticleID) it.next();
		}
		this.articleIDs = articleIDs;
		this.deliveryNoteIDs = null; // hmmm... I think this is not necessary, but it doesn't hurt either
	}

//	/**
//	 * @param articles Instances of {@link org.nightlabs.jfire.trade.Article}.
//	 */
//	public void setArticles(Collection articles)
//	{
//		if (articles == null)
//			throw new IllegalArgumentException("articles must not be null!");
//		
//		if (articleIDs == null)
//			articleIDs = new HashSet();
//		else
//			articleIDs.clear();
//
//		for (Iterator it = articles.iterator(); it.hasNext(); ) {
//			Article article = (Article) it.next();
//			articleIDs.add(JDOHelper.getObjectId(article));
//		}
//		this.articles = articles;
//	}

//	/**
//	 * @return Returns instances of {@link ProductID}.
//	 */
//	public Collection getProductIDs()
//	{
//		return productIDs;
//	}
//
//	/**
//	 * @return Returns instances of {@link Product}.
//	 */
//	public Collection getProducts()
//	{
//		return products;
//	}
//
//	/**
//	 * @param productIDs Instances of {@link ProductID}
//	 */
//	public void setProductIDs(Collection productIDs)
//	{
//		if (productIDs == null)
//			throw new NullPointerException("productIDs must not be null!");
//
//		if (products != null)
//			products = null;
//
//		// check types in collection
//		for (Iterator it = productIDs.iterator(); it.hasNext(); ) {
//			ProductID productID = (ProductID) it.next();
//		}
//		this.productIDs = productIDs;
//	}
//
//	/**
//	 * @param products The products to set.
//	 */
//	public void setProducts(Collection products)
//	{
//		if (products == null)
//			throw new NullPointerException("products must not be null!");
//		
//		if (productIDs == null)
//			productIDs = new HashSet();
//		else
//			productIDs.clear();
//
//		for (Iterator it = products.iterator(); it.hasNext(); ) {
//			Product product = (Product) it.next();
//			productIDs.add(JDOHelper.getObjectId(product));
//		}
//		this.products = products;
//	}

//	/**
//	 * @param deliveryNoteIDs Instances of {@link DeliveryNoteID}.
//	 */
//	public void setDeliveryNoteIDs(Set deliveryNoteIDs)
//	{
//		if (deliveryNoteIDs == null)
//			throw new NullPointerException("deliveryNoteIDs must not be null!");
//
//		if (deliveryNotes != null)
//			deliveryNotes = null;
//
//		// check types in collection
//		for (Iterator it = deliveryNoteIDs.iterator(); it.hasNext(); ) {
//			DeliveryNoteID deliveryNoteID = (DeliveryNoteID) it.next();
//		}
//		this.deliveryNoteIDs = deliveryNoteIDs;
//	}


//	/**
//	 * @param deliveryNotes The deliveryNotes to set.
//	 */
//	public void setDeliveryNotes(Collection deliveryNotes)
//	{
//		if (deliveryNotes == null)
//			throw new NullPointerException("deliveryNotes must not be null!");
//
//		if (deliveryNoteIDs == null)
//			deliveryNoteIDs = new HashSet();
//		else
//			deliveryNoteIDs.clear();
//
//		for (Iterator it = deliveryNotes.iterator(); it.hasNext(); ) {
//			DeliveryNote deliveryNote = (DeliveryNote) it.next();
//			deliveryNoteIDs.add(JDOHelper.getObjectId(deliveryNote));
//		}
//		this.deliveryNotes = deliveryNotes;
//	}

//	/**
//	 * @return Returns the deliveryNoteIDs.
//	 */
//	public Set getDeliveryNoteIDs()
//	{
//		if (deliveryNoteIDs == null && deliveryNotes != null) {
//			deliveryNoteIDs = new HashSet(deliveryNotes.size());
//
//			for (Iterator it = deliveryNotes.iterator(); it.hasNext(); ) {
//				DeliveryNote deliveryNote = (DeliveryNote) it.next();
//				deliveryNoteIDs.add(JDOHelper.getObjectId(deliveryNote));
//			}
//		}
//		return deliveryNoteIDs;
//	}
	
	/**
	 * @return Returns the partnerID.
	 */
	public AnchorID getPartnerID()
	{
		if (partner != null)
			return (AnchorID) JDOHelper.getObjectId(partner);

		return partnerID;
	}

//	/**
//	 * @param currency The currency to set.
//	 */
//	public void setCurrency(Currency currency)
//	{
//		this.currency = currency;
//		this.currencyID = (CurrencyID) JDOHelper.getObjectId(currency);
//	}
//
//	/**
//	 * @return Returns the currencyID.
//	 */
//	public CurrencyID getCurrencyID()
//	{
//		if (currency != null)
//			return (CurrencyID) JDOHelper.getObjectId(currency);
//
//		return currencyID;
//	}
	
//	/**
//	 * @return Returns the partnerAccount.
//	 */
//	public Account getPartnerAccount()
//	{
//		return partnerAccount;
//	}
//
//	/**
//	 * @param partnerAccount The partnerAccount to set.
//	 */
//	public void setPartnerAccount(Account partnerAccount)
//	{
//		this.partnerAccount = partnerAccount;
//		this.partnerAccountID = (AnchorID) JDOHelper.getObjectId(partnerAccount);
//	}

//	/**
//	 * @param partnerAccountID The partnerAccountID to set.
//	 */
//	public void setPartnerAccountID(AnchorID partnerAccountID)
//	{
//		this.partnerAccountID = partnerAccountID;
//		this.partnerAccount = null;
//	}
//
//	/**
//	 * @return Returns the partnerAccountID.
//	 */
//	public AnchorID getPartnerAccountID()
//	{
//		if (partnerAccount != null)
//			return (AnchorID) JDOHelper.getObjectId(partnerAccount);
//
//		return partnerAccountID;
//	}

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
	 * @param modeOfDeliveryFlavour The modeOfDeliveryFlavour to set.
	 */
	public void setModeOfDeliveryFlavour(ModeOfDeliveryFlavour modeOfDeliveryFlavour)
	{
		this.modeOfDeliveryFlavour = modeOfDeliveryFlavour;
		this.modeOfDeliveryFlavourID = (ModeOfDeliveryFlavourID) JDOHelper.getObjectId(modeOfDeliveryFlavour);
	}

//	/**
//	 * @param currencyID The currencyID to set.
//	 */
//	public void setCurrencyID(CurrencyID currencyID)
//	{
//		this.currencyID = currencyID;
//		this.currency = null;
//	}
	/**
	 * @param modeOfDeliveryFlavourID The modeOfDeliveryFlavourID to set.
	 */
	public void setModeOfDeliveryFlavourID(
			ModeOfDeliveryFlavourID modeOfDeliveryFlavourID)
	{
		this.modeOfDeliveryFlavourID = modeOfDeliveryFlavourID;
	}
//	/**
//	 * @param amount The amount to set.
//	 */
//	public void setAmount(long amount)
//	{
//		this.amount = amount;
//	}

//	/**
//	 * @return Returns the reasonForDelivery.
//	 */
//	public String getReasonForDelivery()
//	{
//		return reasonForDelivery;
//	}
//	/**
//	 * @param reasonForDelivery The reasonForDelivery to set.
//	 */
//	public void setReasonForDelivery(String reasonForDelivery)
//	{
//		this.reasonForDelivery = reasonForDelivery;
//	}

	/**
	 * @return Returns the clientDeliveryProcessorFactoryID.
	 */
	public String getClientDeliveryProcessorFactoryID()
	{
		return clientDeliveryProcessorFactoryID;
	}
	/**
	 * @param clientDeliveryProcessorFactoryID The clientDeliveryProcessorFactoryID to set.
	 */
	public void setClientDeliveryProcessorFactoryID(String clientDeliveryProcessorID)
	{
		this.clientDeliveryProcessorFactoryID = clientDeliveryProcessorID;
	}

	/**
	 * @return Returns the precursor.
	 */
	public Delivery getPrecursor()
	{
		return precursor;
	}
	/**
	 * @param precursor The precursor to set.
	 */
	public void setPrecursor(Delivery precursor)
	{
		this.precursor = precursor;
		this.precursorID = (DeliveryID) JDOHelper.getObjectId(precursor);
	}
	/**
	 * @param precursorID The precursorID to set.
	 */
	public void setPrecursorID(DeliveryID precursorID)
	{
		this.precursorID = precursorID;
		this.precursor = null;
	}
	/**
	 * @return Returns the followUp.
	 */
	public Delivery getFollowUp()
	{
		return followUp;
	}
	/**
	 * @return Returns the precursorID.
	 */
	public DeliveryID getPrecursorID()
	{
		if (precursor != null)
			return (DeliveryID) JDOHelper.getObjectId(precursor);

		return precursorID;
	}

	/**
	 * This method is called before a newly created <tt>Delivery</tt> is sent to the
	 * server.
	 *
	 * @return Returns a new instance of <tt>Delivery</tt> with {@link #modeOfDeliveryFlavour},
	 *		{@link #currency} and {@link #deliveryNotes} being <tt>null</tt> in order to minimize
	 *		traffic when uploading.
	 *
	 * @see #jdoPreStore()
	 */
	protected Delivery cloneForUpload()
	{
		Delivery n = new Delivery(organisationID, deliveryID);
//		n.amount = amount;
		n.beginDT = beginDT;
		n.clientDeliveryProcessorFactoryID = clientDeliveryProcessorFactoryID;
//		n.currencyID = getCurrencyID();
		n.endDT = endDT;
		n.externalDeliveryApproved = externalDeliveryApproved;
		n.externalDeliveryDone = externalDeliveryDone;
		n.failed = failed;
//		n.deliveryNoteIDs = getDeliveryNoteIDs();
//		n.followUp = followUp;
		n.precursorID = getPrecursorID();
		n.modeOfDeliveryFlavourID = getModeOfDeliveryFlavourID();
		n.partnerID = getPartnerID();
		n.articleIDs = articleIDs;
//		n.partnerAccountID = getPartnerAccountID();
		n.deliverBeginClientResult = deliverBeginClientResult;
		n.deliverBeginServerResult = deliverBeginServerResult;
		n.deliverEndClientResult = deliverEndClientResult;
		n.deliverEndServerResult = deliverEndServerResult;
		n.deliveryDirection = deliveryDirection;
		n.pending = pending;
		n.postponed = postponed;
//		n.reasonForDelivery = reasonForDelivery;
		n.rollbackStatus = rollbackStatus;
		n.serverDeliveryProcessorIDStrKey = serverDeliveryProcessorIDStrKey;
		return n;
	}

	/**
	 * This method resolves {@link #modeOfDeliveryFlavour},
	 * {@link #currency} and {@link #deliveryNotes} by using the IDs before
	 * the object is written to datastore.
	 *
	 * @see javax.jdo.listener.StoreCallback#jdoPreStore()
	 */
	public void jdoPreStore()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Delivery is currently not persistent! Cannot obtain PersistenceManager!");

		if (deliveryDirection == null)
			throw new IllegalStateException("deliveryDirection is not set!");

		if (partnerID != null && partner == null) 
			partner = (LegalEntity) pm.getObjectById(partnerID);

//		if (partnerAccountID != null && partnerAccount == null) 
//			partnerAccount = (Account) pm.getObjectById(partnerAccountID);

		if (modeOfDeliveryFlavourID != null && modeOfDeliveryFlavour == null)
			modeOfDeliveryFlavour = (ModeOfDeliveryFlavour) pm.getObjectById(modeOfDeliveryFlavourID);

//		if (currencyID != null && currency == null)
//			currency = (Currency) pm.getObjectById(currencyID);

		if (articleIDs != null) {
			if (articleLocals == null || articleLocals.size() != articleIDs.size()) {
				if (articleLocals == null)
					articleLocals = new HashSet();

				articleLocals.clear();

				for (Iterator it = articleIDs.iterator(); it.hasNext(); ) {
					ArticleID articleID = (ArticleID) it.next();
					Article article = (Article) pm.getObjectById(articleID);
					ArticleLocal articleLocal = article.getArticleLocal();

					if (articleLocal.isDelivered() && !getPrimaryKey().equals(articleLocal.getDelivery().getPrimaryKey()))
						throw new IllegalStateException("The Article \""+article.getPrimaryKey()+"\" has already been delivered by another delivery!");

					articleLocal.setDelivery(this);
					articleLocals.add(articleLocal);
				}
			}
		}

//		if (deliveryNoteIDs != null) {
//			if (deliveryNotes == null || deliveryNotes.size() != deliveryNoteIDs.size()) {
//				if (deliveryNotes == null)
//					deliveryNotes = new ArrayList();
//
//				deliveryNotes.clear();
//
//				for (Iterator it = deliveryNoteIDs.iterator(); it.hasNext(); ) {
//					DeliveryNoteID deliveryNoteID = (DeliveryNoteID) it.next();
//					DeliveryNote deliveryNote = (DeliveryNote) pm.getObjectById(deliveryNoteID);
//					deliveryNotes.add(deliveryNote);
//				}
//			} // if (deliveryNotes == null || deliveryNotes.size() != deliveryNoteIDs.size()) {
//		} // if (deliveryNoteIDs != null) {

		if (JDOHelper.isNew(this)) {
			HashSet dnIDs = new HashSet();
			if (deliveryNotes == null)
				deliveryNotes = new HashSet();
	
			deliveryNotes.clear();
			for (Iterator it = articleLocals.iterator(); it.hasNext(); ) {
				ArticleLocal articleLocal = (ArticleLocal) it.next();
				DeliveryNote deliveryNote = articleLocal.getArticle().getDeliveryNote();
				DeliveryNoteID deliveryNoteID = (DeliveryNoteID) JDOHelper.getObjectId(deliveryNote);
				if (!dnIDs.contains(deliveryNoteID)) {
					deliveryNotes.add(deliveryNote);
					dnIDs.add(deliveryNoteID);
				}
			}
		}

		if (precursorID != null && precursor == null) {
			precursor = (Delivery) pm.getObjectById(precursorID);
			precursor.followUp = this;
		}

	}
}
