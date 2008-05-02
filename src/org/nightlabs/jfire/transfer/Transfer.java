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

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DetachCallback;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;

/**
 * A {@link Transfer} is used to describe the transfer of something (money or products)
 * form one {@link Anchor} to another.
 * 
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.transfer.id.TransferID"
 *		detachable="true"
 *		table="JFireTrade_Transfer"
 *
 * @jdo.create-objectid-class field-order="organisationID, transferTypeID, transferID"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.fetch-group name="Transfer.container" fields="container"
 * @jdo.fetch-group name="Transfer.from" fields="from"
 * @jdo.fetch-group name="Transfer.to" fields="to"
 * @jdo.fetch-group name="Transfer.initiator" fields="initiator"
 * @jdo.fetch-group name="Transfer.this" fields="container, from, to, initiator"
 */
public abstract class Transfer
implements Serializable, DetachCallback
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(Transfer.class);

	public static final String FETCH_GROUP_CONTAINER = "Transfer.container";
	public static final String FETCH_GROUP_FROM = "Transfer.from";
	public static final String FETCH_GROUP_TO = "Transfer.to";
	public static final String FETCH_GROUP_INITIATOR = "Transfer.initiator";
	public static final String FETCH_GROUP_THIS_TRANSFER = "Transfer.this";
	
	public static final String FETCH_GROUP_DESCRIPTION = "Transfer.description";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String transferTypeID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long transferID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Transfer container;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Anchor from;

	/**
	 * This is false before {@link Anchor#bookTransfer(User, Transfer, Map)} and after
	 * {@link Anchor#rollbackTransfer(User, Transfer, Map)}.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean bookedFrom = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Anchor to;

	/**
	 * This is false before {@link Anchor#bookTransfer(User, Transfer, Map)} and after
	 * {@link Anchor#rollbackTransfer(User, Transfer, Map)}.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean bookedTo = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User initiator;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date timestamp;

	protected Transfer() { }

	/**
	 * This constructor creates an instance of Transfer and automatically registers it in the TransferRegistry.
	 *
	 * @param container Can be null. If not null, container defines the enclosing Transfer of which this Transfer is a part.
	 * @param initiator The user which is responsible for this transfer. Must not be null.
	 * @param from The source of this transfer. Must not be null.
	 * @param to The destination of this transfer. Must not be null.
	 */
	public Transfer(String transferTypeID, Transfer container, User initiator, Anchor from, Anchor to)
	{
		this.organisationID = IDGenerator.getOrganisationID();
		this.transferTypeID = transferTypeID;
		this.transferID = IDGenerator.nextID(Transfer.class, transferTypeID);
		this.container = container;
		if (initiator == null)
			throw new NullPointerException("initiator must not be null! Someone must be responsible!");
		this.initiator = initiator;
		if (from == null)
			throw new NullPointerException("from must not be null! Even nirvana must be known as Anchor!");
		this.from = from;
		if (to == null)
			throw new NullPointerException("to must not be null! Even nirvana must be known as Anchor!");
		this.to = to;
		this.timestamp = new Date();

		if (logger.isDebugEnabled()) {
			if (logger.isTraceEnabled())
				logger.trace(this.getClass().getName() + ".<init>: pk=\"" + getPrimaryKey() + "\" from.pk=\"" + (from == null ? null : from.getPrimaryKey()) + "\"" + " to.pk=\"" + (to == null ? null : to.getPrimaryKey()) + "\"", new Exception("STACKTRACE"));
			else
				logger.debug(this.getClass().getName() + ".<init>: pk=\"" + getPrimaryKey() + "\" from.pk=\"" + (from == null ? null : from.getPrimaryKey()) + "\"" + " to.pk=\"" + (to == null ? null : to.getPrimaryKey()) + "\"");
		}
	}

	public static String getPrimaryKey(String organisationID, String transferTypeID, long transferID)
	{
		return organisationID + '/' + transferTypeID + '/' + ObjectIDUtil.longObjectIDFieldToString(transferID);
	}

	public String getPrimaryKey()
	{
		return getPrimaryKey(organisationID, transferTypeID, transferID);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the transferTypeID.
	 */
	public String getTransferTypeID()
	{
		return transferTypeID;
	}
	/**
	 * @return Returns the transferID.
	 */
	public long getTransferID()
	{
		return transferID;
	}

	/**
	 * One Transfer can be part of an enclosing Transfer. E.g. if a product needs to be shipped, it may first
	 * need to be checked out of a certain storage hall, brought to a test center (to assure the product is in
	 * good order) and finally leaves the source and reaches the customer. Thus, in this example, we have the
	 * enclosing transfer: Organisation A transfers object to Organisation B. This transfer contains two transfers:
	 * First the one from the storage hall to the test center and second from test center to customer.
	 *
	 * @return Returns the container, which may be null.
	 */
	public Transfer getContainer()
	{
		return container;
	}

	/**
	 * The initiator is the user who is responsible for this transfer.
	 * 
	 * @return Returns the initiator, which is never null.
	 */
	public User getInitiator()
	{
		return initiator;
	}
	/**
	 * @return Returns the from. Never null.
	 */
	public Anchor getFrom()
	{
		return from;
	}
	/**
	 * @return Returns the to. Never null.
	 */
	public Anchor getTo()
	{
		return to;
	}

	public static final int ANCHORTYPE_UNKNOWN = 0;
	public static final int ANCHORTYPE_FROM = 1;
	public static final int ANCHORTYPE_TO = 2;
	public int getAnchorType(Anchor anchor)
	{
		if (anchor == null)
			throw new NullPointerException("anchor must not be null!");

		if (from.getPrimaryKey().equals(anchor.getPrimaryKey()))
			return ANCHORTYPE_FROM;
		
		if (to.getPrimaryKey().equals(anchor.getPrimaryKey()))
			return ANCHORTYPE_TO;
		
		return ANCHORTYPE_UNKNOWN;
	}
	
	/**
	 * When did the transfer happen.
	 *
	 * @return Returns the timestamp. Never null.
	 */
	public Date getTimestamp()
	{
		return timestamp;
	}

	/**
	 * @return Returns the bookedFrom.
	 */
	public boolean isBookedFrom()
	{
		return bookedFrom;
	}
	/**
	 * This method is called by the {@link Anchor}.
	 *
	 * @param bookedFrom The bookedFrom to set.
	 */
	public void setBookedFrom(boolean bookedFrom)
	{
		this.bookedFrom = bookedFrom;
	}
	/**
	 * @return Returns the bookedTo.
	 */
	public boolean isBookedTo()
	{
		return bookedTo;
	}
	/**
	 * This method is called by the {@link Anchor}.
	 *
	 * @param bookedTo The bookedTo to set.
	 */
	public void setBookedTo(boolean bookedTo)
	{
		this.bookedTo = bookedTo;
	}

	/**
	 * Returns whether this Transfer was already booked. Note, that it will also return
	 * <code>false</code> after {@link #rollbackTransfer(User, Set)} was invoked. 
	 * <p>
	 * Note, that this method will throw and {@link IllegalStateException}
	 * if only one of the Anchors (to or from) registered the booking of
	 * the Transfer by setting {@link #setBookedFrom(boolean)} or {@link #setBookedFrom(boolean)}.
	 * </p>
	 * @return Whether this Transfer was already booked.
	 */
	public boolean isBooked()
	{
		if (bookedFrom && !bookedTo)
			throw new IllegalStateException("The Transfer \""+getPrimaryKey()+"\" is booked on the 'from' side, but not on the 'to' side. This should not happen!");

		if (!bookedFrom && bookedTo)
			throw new IllegalStateException("The Transfer \""+getPrimaryKey()+"\" is booked on the 'to' side, but not on the 'from' side. This should not happen!");

		return bookedFrom;
	}

	/**
	 * This method is called when a new Transfer is created an it should
	 * be registered in the system.
	 * <p>
	 * It calls the bookTransfer method of the to-anchor
	 * and the from-anchor. The anchors are then themselves responsible
	 * to perform necessary actions reflecting the new transfer.
	 * For example a LegalEntity will delegate the work to its Accountant.
	 * </p>
	 * <p>
	 * Note, that the Anchors of the Transfer must call either {@link #setBookedFrom(boolean)}
	 * or {@link #setBookedTo(boolean)} in order tell the Transfer that
	 * it was booked.
	 * </p>
	 * <p>
	 * Some sub-classes of Transfer might do additional stuff, too.
	 * For example an InvoiceMoneyTransfer registers the amount paid
	 * in the Invoice.
	 * </p>
	 * @param user The user that initiated the booking (creation) of this transfer
	 * @param involvedAnchors All {@link Anchor}s involved in the booking process.
	 */
	public void bookTransfer(User user, Set<Anchor> involvedAnchors)
	{
		if (logger.isInfoEnabled()) {
			logger.info("bookTransfer: pk=\"" + getPrimaryKey() + "\" from.pk=\"" + (from == null ? null : from.getPrimaryKey()) + "\"" + " to.pk=\"" + (to == null ? null : to.getPrimaryKey()) + "\"");
		}

		getPersistenceManager(); // ensure we're attached

		from.bookTransfer(user, this, involvedAnchors);
		to.bookTransfer(user, this, involvedAnchors);
	}

	/**
	 * This method is called when an existing and already booked
	 * Transfer should be rolled back, i.e. the actions upon booking
	 * should be undone.
	 * <p>
	 * This method calls the rollbackTransfer method of the to-anchor
	 * and the from-anchor. The anchors are then themselves responsible
	 * to perform necessary actions reflecting the rolled back transfer.
	 * </p>
	 * <p>
	 * Some sub-classes of Transfer might do additional stuff, too.
	 * For example an InvoiceMoneyTransfer registers the rolled back amount
	 * in the Invoice.
	 * </p> 
	 * @param user The user that initiated the transfer rollback. 
	 * @param involvedAnchors All {@link Anchor}s involved in the rollback proccss.
	 */
	public void rollbackTransfer(User user, Set<Anchor> involvedAnchors)
	{
		if (logger.isInfoEnabled()) {
			logger.info("rollbackTransfer: pk=\"" + getPrimaryKey() + "\" from.pk=\"" + (from == null ? null : from.getPrimaryKey()) + "\"" + " to.pk=\"" + (to == null ? null : to.getPrimaryKey()) + "\"");
		}

		getPersistenceManager(); // ensure we're attached

		from.rollbackTransfer(user, this, involvedAnchors);
		to.rollbackTransfer(user, this, involvedAnchors);
	}
	
	/**
	 * Returns the {@link PersistenceManager} this Transfer 
	 * is associated with. This method will fail if the
	 * Transfer is not attached, an {@link IllegalStateException}
	 * will then be thrown.
	 * 
	 * @return The {@link PersistenceManager} this Transfer is associated with.
	 */
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This Transfer has not yet been persisted or it has been detached! Cannot obtain PersistenceManager!");

		return pm;
	}
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private String description = null;
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean description_detached = false;
	
	/**
	 * Returns a human readable description for this Transfer.
	 * 
	 * @return returns a human readable description for this Transfer
	 */
	public String getDescription() {
		if (description == null && !description_detached)
			description = internalGetDescription();
		
		return description;
	}
	
	/**
	 * This should return a human readable description of the transfer.
	 * <p>
	 * Note that this method can assume that it is called on attached instances of Transfer.
	 * </p>
	 * @return A human readable description of the transfer.
	 */
	protected abstract String internalGetDescription();
	
	/**
	 * Checks for the {@link #FETCH_GROUP_DESCRIPTION} and
	 * set the non-persitent member for the description.
	 */
	@Override
	public void jdoPostDetach(Object _attached) {
		Transfer attached = (Transfer) _attached;
		Transfer detached = this;
		PersistenceManager pm = attached.getPersistenceManager();
		if (pm.getFetchPlan().getGroups().contains(FETCH_GROUP_DESCRIPTION)) {
			detached.description_detached = true;
			detached.description = attached.getDescription();
		}
	}
	
	@Override
	public void jdoPreDetach() {
	}
}
