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
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
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
 */
public abstract class Transfer
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
	 * One Transfer can be part of an enclosing Tranfer. E.g. if a product needs to be shipped, it may first
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

	public boolean isBooked()
	{
		if (bookedFrom && !bookedTo)
			throw new IllegalStateException("The Transfer \""+getPrimaryKey()+"\" is booked on the 'from' side, but not on the 'to' side. This should not happen!");

		if (!bookedFrom && bookedTo)
			throw new IllegalStateException("The Transfer \""+getPrimaryKey()+"\" is booked on the 'to' side, but not on the 'from' side. This should not happen!");

		return bookedFrom;
	}

	/**
	 * This method calls <tt>from.bookTransfer(...)</tt> and
	 * <tt>to.bookTransfer(...)</tt>. Some special implementations
	 * might do other stuff, too.
	 */
	public void bookTransfer(User user, Set<Anchor> involvedAnchors)
	{
		if (JDOHelper.getPersistenceManager(this) == null)
			throw new IllegalStateException("This Transfer has not yet been persisted or it has been detached!");

		from.bookTransfer(user, this, involvedAnchors);
		to.bookTransfer(user, this, involvedAnchors);
	}

	public void rollbackTransfer(User user, Set<Anchor> involvedAnchors)
	{
		if (JDOHelper.getPersistenceManager(this) == null)
			throw new IllegalStateException("This Transfer has not yet been persisted or it has been detached!");

		from.rollbackTransfer(user, this, involvedAnchors);
		to.rollbackTransfer(user, this, involvedAnchors);
	}
}
