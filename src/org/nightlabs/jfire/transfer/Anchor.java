/*
 * Created on 20.10.2004
 */
package org.nightlabs.jfire.transfer;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.id.AnchorID;

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
 * @jdo.fetch-group name="Anchor.transfers" fields="transfers"
 * @jdo.fetch-group name="Anchor.this" fetch-groups="default" fields="transfers"
 */
public abstract class Anchor
	implements Serializable
{
	public static final String FETCH_GROUP_TRANSFERS = "Anchor.transfers";
	public static final String FETCH_GROUP_THIS_ANCHOR = "Anchor.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String anchorTypeID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
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
		this.organisationID = organisationID;
		this.anchorTypeID = anchorTypeID;
		this.anchorID = anchorID;
		this.primaryKey = getPrimaryKey(organisationID, anchorTypeID, anchorID);
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
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	protected String primaryKey;
	
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
	 * Do NOT call this method directly! Use {@link Transfer#bookTransfer(User, Map)}
	 * instead!
	 */
	public void rollbackTransfer(User user, Transfer transfer, Map involvedAnchors)
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

		involvedAnchors.put(getPrimaryKey(), this);

		internalRollbackTransfer(transfer, user, involvedAnchors);

//		transfers.remove(pk);
		if (transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM)
			transfer.setBookedFrom(false);
		else
			transfer.setBookedTo(false);
	}

	protected abstract void internalRollbackTransfer(Transfer transfer, User user, Map involvedAnchors);

	/**
	 * Do NOT call this method directly! Use {@link Transfer#bookTransfer(User, Map)}
	 * instead!
	 * <p>
	 * This method does some checks andcalls <tt>internalBookTransfer</tt>. It is not
	 * recommended to overwrite this method. It's
	 * better to overwrite {@link #internalBookTransfer(Transfer, User, Map)} instead!
	 *
	 * @param transfer The transfer to be booked.
	 * @param involvedAnchors Every <tt>Anchor</tt> must register itself in this <tt>Map</tt> with the <tt>Anchor.primaryKey</tt> as key and the <tt>Anchor</tt> as value.
	 *
	 * @throws DuplicateTransferException If the given transfer has already been booked.
	 */
	public void bookTransfer(User user, Transfer transfer, Map involvedAnchors)
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

		String pk = getPrimaryKey();
		if (!involvedAnchors.containsKey(pk))
			involvedAnchors.put(pk, this);

		internalBookTransfer(transfer, user, involvedAnchors);

		if (transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM)
			transfer.setBookedFrom(true);
		else
			transfer.setBookedTo(true);
//		transfers.put(pk,transfer);
	}

	protected abstract void internalBookTransfer(Transfer transfer, User user, Map involvedAnchors);

//	protected void addTransfer(Transfer transfer)
//	{
//		if (transfer.getAnchorType(this) == Transfer.ANCHORTYPE_UNKNOWN)
//			throw new AnchorMismatchException("This Anchor \""+this.getPrimaryKey()+"\" is not a side of the transfer \""+transfer.getPrimaryKey()+"\"!");
//
//		transfers.put(transfer.getPrimaryKey(), transfer);
//	}

	public static void checkIntegrity(Collection containers, Map involvedAnchors)
	{
		for (Iterator it = involvedAnchors.values().iterator(); it.hasNext(); ) {
			Anchor anchor = (Anchor) it.next();
			anchor.checkIntegrity(containers);
		}
	}

	public static void resetIntegrity(Collection containers, Map involvedAnchors)
	{
		for (Iterator it = involvedAnchors.values().iterator(); it.hasNext(); ) {
			Anchor anchor = (Anchor) it.next();
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
	public abstract void checkIntegrity(Collection containers);

	/**
	 * This method is called on all involved anchors after {@link #checkIntegrity(Collection)} failed on one
	 * of them. You must reset your integrity checking data (e.g. set balance = 0) in this method if your
	 * integrity checking uses anything (e.g. {@link ThreadLocal} - which is recommended) that does not loose
	 * its data after the transaction is closed.
	 *
	 * @param containers
	 */
	public abstract void resetIntegrity(Collection containers);

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Anchor ("+getPrimaryKey()+") is currently not persistent or not attached. Cannot obtain PersistenceManager!");

		return pm;
	}
}
