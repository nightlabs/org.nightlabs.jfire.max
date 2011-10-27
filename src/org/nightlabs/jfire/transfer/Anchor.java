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
import java.util.Collection;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Anchor is an end-point for a Transfer. Every Transfer has exactly two Anchors -
 * a source and a destination. Anchor can be everything that is able to receive,
 * hold and give away something (e.g. money or products). Examples for Anchors may be:
 * a legal entity, a storage area, an account.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.transfer.id.AnchorID"
 *		detachable="true"
 *		table="JFireTrade_Anchor"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, anchorTypeID, anchorID"
 *
 * @!jdo.fetch-group name="Anchor.transfers" fields="transfers"
 * @jdo.fetch-group name="Anchor.this" fetch-groups="default"
 */
@PersistenceCapable(
	objectIdClass=AnchorID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_Anchor")
//@FetchGroups(
//	@FetchGroup(
//		fetchGroups={"default"},
//		name=Anchor.FETCH_GROUP_THIS_ANCHOR,
//		members={})
//)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class Anchor
	implements Serializable, DetachCallback, AttachCallback, StoreCallback
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(Anchor.class);

	//	public static final String FETCH_GROUP_TRANSFERS = "Anchor.transfers";
//	/**
//	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
//	 */
//	@Deprecated
//	public static final String FETCH_GROUP_THIS_ANCHOR = "Anchor.this";
	public static final String FETCH_GROUP_DESCRIPTION = "Anchor.description";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String anchorTypeID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String anchorID;


//	/**
//	 * key: String transferPK<br/>
//	 * value: Transfer transfer
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="Transfer"
//	 *		dependent="true"
//	 *
//	 * @jdo.join
//	 *
//	 * @jdo.map-vendor-extension vendor-name="jpox" key="key-length" value="max 255"
//	 */
//	private Map transfers = new HashMap();

	protected Anchor() { }

	public Anchor(String organisationID, String anchorTypeID, String anchorID)
	{
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(anchorTypeID, "anchorTypeID");
		ObjectIDUtil.assertValidIDString(anchorID, "anchorID");

		this.organisationID = organisationID;
		this.anchorTypeID = anchorTypeID;
		this.anchorID = anchorID;
//		this.primaryKey = getPrimaryKey(organisationID, anchorTypeID, anchorID);
	}

	public static String getPrimaryKey(String organisationID, String anchorTypeID, String anchorID)
	{
		return organisationID + '/' + anchorTypeID + "/" + anchorID;
	}

	public static AnchorID primaryKeyToAnchorID(String primaryKey)
	{
		String[] parts = primaryKey.split("/");
		if (parts.length < 3)
			throw new IllegalArgumentException("The given anchorPK "+primaryKey+" is illegal (not enough parts)");
		String anchorID = parts[2];
		if (parts.length > 3) {
			for (int i = 3; i < parts.length; i++) {
				anchorID = anchorID + "/" +parts[i];
			}
		}
		return AnchorID.create(parts[0], parts[1], anchorID);
	}

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	protected String primaryKey;

	public String getPrimaryKey()
	{
//		return primaryKey;
		return getPrimaryKey(organisationID, anchorTypeID, anchorID);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the anchorTypeID.
	 */
	public String getAnchorTypeID()
	{
		return anchorTypeID;
	}
	/**
	 * @return Returns the anchorID.
	 */
	public String getAnchorID()
	{
		return anchorID;
	}

	/**
	 * Do NOT call this method directly! Use {@link Transfer#bookTransfer(User, java.util.Set)}
	 * instead!
	 */
	public void rollbackTransfer(User user, Transfer transfer, Set<Anchor> involvedAnchors)
	{
		if (transfer == null)
			throw new NullPointerException("transfer must not be null!");

		if (!JDOHelper.isPersistent(this))
			throw new IllegalStateException("This instance of Anchor is not persistent. Non persistent instances can not rollback transfers!!");

//		String pk = transfer.getPrimaryKey();
//		if (!transfers.containsKey(pk))
//			throw new IllegalArgumentException("Transfer \""+pk+"\" is not known to this Anchor \""+this.getPrimaryKey()+"\"!");

		if (transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM) {
			if (!transfer.isBookedFrom())
				throw new IllegalStateException("This Anchor \""+this.getPrimaryKey()+"\" has already rolled back the transfer \""+transfer.getPrimaryKey()+"\" or it has not been booked!");
		}
		else if (transfer.getAnchorType(this) == Transfer.ANCHORTYPE_TO) {
			if (!transfer.isBookedTo())
				throw new IllegalStateException("This Anchor \""+this.getPrimaryKey()+"\" has already rolled back the transfer \""+transfer.getPrimaryKey()+"\" or it has not been booked!");
		}
		else
			throw new AnchorMismatchException("This Anchor \""+this.getPrimaryKey()+"\" is not a side of the transfer \""+transfer.getPrimaryKey()+"\"!");

		involvedAnchors.add(this);

		internalRollbackTransfer(transfer, user, involvedAnchors);

//		transfers.remove(pk);
		if (transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM)
			transfer.setBookedFrom(false);
		else
			transfer.setBookedTo(false);
	}

	protected abstract void internalRollbackTransfer(Transfer transfer, User user, Set<Anchor> involvedAnchors);

	/**
	 * Do NOT call this method directly! Use {@link Transfer#bookTransfer(User, java.util.Set)}
	 * instead!
	 * <p>
	 * This method does some checks and calls <tt>internalBookTransfer</tt>. It is not
	 * recommended to overwrite this method. It's
	 * better to overwrite {@link #internalBookTransfer(Transfer, User, Set)} instead!
	 *
	 * @param transfer The transfer to be booked.
	 * @param involvedAnchors Every <tt>Anchor</tt> must register itself in this <tt>Map</tt> with the <tt>Anchor.primaryKey</tt> as key and the <tt>Anchor</tt> as value.
	 *
	 * @throws DuplicateTransferException If the given transfer has already been booked.
	 */
	public void bookTransfer(User user, Transfer transfer, Set<Anchor> involvedAnchors)
	{
		if (transfer == null)
			throw new NullPointerException("transfer must not be null!");

		if (!JDOHelper.isPersistent(this))
			throw new IllegalStateException("This instance of Anchor is not persistent. Non persistent instances can not book transfers!!");

//		String pk = transfer.getPrimaryKey();
//		if (transfers.containsKey(pk))
//			throw new DuplicateTransferException("Transfer \""+pk+"\" has already been booked!");

		if (transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM) {
			if (transfer.isBookedFrom())
				throw new IllegalStateException("This Anchor \""+this.getPrimaryKey()+"\" has already booked the transfer \""+transfer.getPrimaryKey()+"\"!");
		}
		else if (transfer.getAnchorType(this) == Transfer.ANCHORTYPE_TO) {
			if (transfer.isBookedTo())
				throw new IllegalStateException("This Anchor \""+this.getPrimaryKey()+"\" has already booked the transfer \""+transfer.getPrimaryKey()+"\"!");
		}
		else
			throw new AnchorMismatchException("This Anchor \""+this.getPrimaryKey()+"\" is not a side of the transfer \""+transfer.getPrimaryKey()+"\"!");

		involvedAnchors.add(this);

		internalBookTransfer(transfer, user, involvedAnchors);

		if (transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM)
			transfer.setBookedFrom(true);
		else
			transfer.setBookedTo(true);
//		transfers.put(pk,transfer);
	}

	protected abstract void internalBookTransfer(Transfer transfer, User user, Set<Anchor> involvedAnchors);

//	protected void addTransfer(Transfer transfer)
//	{
//		if (transfer.getAnchorType(this) == Transfer.ANCHORTYPE_UNKNOWN)
//			throw new AnchorMismatchException("This Anchor \""+this.getPrimaryKey()+"\" is not a side of the transfer \""+transfer.getPrimaryKey()+"\"!");
//
//		transfers.put(transfer.getPrimaryKey(), transfer);
//	}

	public static void checkIntegrity(Collection<? extends Transfer> containers, Set<? extends Anchor> involvedAnchors)
	{
		for (Anchor anchor : involvedAnchors) {
			anchor.checkIntegrity(containers);
		}
	}

	public static void resetIntegrity(Collection<? extends Transfer> containers, Set<? extends Anchor> involvedAnchors)
	{
		for (Anchor anchor : involvedAnchors) {
			anchor.resetIntegrity(containers);
		}
	}

	/**
	 * This method is called after transfers have been booked. It must check whether
	 * all is OK and throw any <tt>RuntimeException</tt> if not. E.g. a LegalEntity must
	 * always have a balance = 0 after all transfers are complete.
	 * <p>
	 * Because one Anchor might be involved simultaneously in multiple Transfers (e.g. booking of
	 * Invoice, booking of DeliveryNote and a payment alltogether), the current container is
	 * passed here. Note, that you should work mainly with a {@link ThreadLocal} and use this
	 * <code>container</code> only as supplementary hint.
	 *
	 * @param containers These are all main (top-level) {@link Transfer}s of the current transaction.
	 *		They contain all the other transfers (them pointing to this
	 *		one with {@link Transfer#getContainer()}.
	 */
	public abstract void checkIntegrity(Collection<? extends Transfer> containers);

	/**
	 * This method is called on all involved anchors after {@link #checkIntegrity(Collection)} failed on one
	 * of them. You must reset your integrity checking data (e.g. set balance = 0) in this method if your
	 * integrity checking uses anything (e.g. {@link ThreadLocal} - which is recommended) that does not loose
	 * its data after the transaction is closed.
	 *
	 * @param containers
	 */
	public abstract void resetIntegrity(Collection<? extends Transfer> containers);

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Anchor ("+getPrimaryKey()+") is currently not persistent or not attached. Cannot obtain PersistenceManager!");

		return pm;
	}

	/**
	 * Checks whether this anchor is the from-anchor of the given transfer.
	 * @param transfer The transfer to check.
	 * @return Whether this anchor is the from-anchor of the given transfer.
	 */
	protected boolean isTransferFrom(Transfer transfer) {
		return (transfer.getFrom() != null && transfer.getFrom().equals(this));
	}

	/**
	 * Checks whether this anchor is the to-anchor of the given transfer.
	 * @param transfer The transfer to check.
	 * @return Whether this anchor is the to-anchor of the given transfer.
	 */
	protected boolean isTransferTo(Transfer transfer) {
		return (transfer.getTo() != null && transfer.getTo().equals(this));
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private String description = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean description_detached = false;

	/**
	 * Returns a human readable description for this <code>Anchor</code>
	 * for the default Locale of the current user.
	 *
	 * @return returns a human readable description for this <code>Anchor</code>.
	 */
	public String getDescription() {
		if (description == null && !description_detached)
			description = internalGetDescription();

		return description;
	}

	/**
	 * Checks for the {@link #FETCH_GROUP_DESCRIPTION} and
	 * set the non-persitent member for the description.
	 */
	@Override
	public void jdoPostDetach(Object _attached) {
		Anchor attached = (Anchor) _attached;
		Anchor detached = this;
		PersistenceManager pm = attached.getPersistenceManager();
		if (pm.getFetchPlan().getGroups().contains(FETCH_GROUP_DESCRIPTION)) {
			detached.description_detached = true;
			detached.description = attached.getDescription();
		}
	}

	@Override
	public void jdoPreDetach() {
	}

	@Override
	public void jdoPreAttach() {
		logger.info("jdoPreAttach: " + this.getClass().getName() + '[' + getPrimaryKey() + ']');
	}
	@Override
	public void jdoPostAttach(Object arg0) {
		logger.info("jdoPostAttach: " + this.getClass().getName() + '[' + getPrimaryKey() + ']');
	}

	@Override
	public void jdoPreStore() {
		logger.info("jdoPreStore: " + this.getClass().getName() + '[' + getPrimaryKey() + ']');
	}

	/**
	 * This should return a human readable description of this anchor.
	 * <p>
	 * Note that this method can assume that it is called on attached instances of Anchor.
	 * @return A description of this Anchor.
	 */
	protected abstract String internalGetDescription();

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;

		if (obj.getClass() != this.getClass())
			return false;

		Anchor o = (Anchor) obj;

		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.anchorTypeID, o.anchorTypeID) &&
				Util.equals(this.anchorID, o.anchorID);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Util.hashCode(organisationID);
		result = prime * result + Util.hashCode(anchorTypeID);
		result = prime * result + Util.hashCode(anchorID);
		return result;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + anchorTypeID + ',' + anchorID + ']';
	}
}
