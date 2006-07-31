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

package org.nightlabs.jfire.trade;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.nightlabs.jfire.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.OfferLocalID"
 *		detachable="true"
 *		table="JFireTrade_OfferLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, offerIDPrefix, offerID"
 *
 * @jdo.fetch-group name="Offer.offerLocal" fields="offer"
 * @jdo.fetch-group name="OfferLocal.offer" fields="offer"
 * @jdo.fetch-group name="OfferLocal.acceptUser" fields="acceptUser"
 * @jdo.fetch-group name="OfferLocal.rejectUser" fields="rejectUser"
 * @jdo.fetch-group name="OfferLocal.confirmUser" fields="confirmUser"
 * @jdo.fetch-group name="OfferLocal.this" fields="offer, acceptUser, rejectUser, confirmUser"
 */
public class OfferLocal
implements Serializable
{
	public static final String FETCH_GROUP_OFFER = "OfferLocal.offer";
	public static final String FETCH_GROUP_ACCEPT_USER = "OfferLocal.acceptUser";
	public static final String FETCH_GROUP_REJECT_USER = "OfferLocal.rejectUser";
	public static final String FETCH_GROUP_CONFIRM_USER = "OfferLocal.confirmUser";
	public static final String FETCH_GROUP_THIS_OFFER_LOCAL = "OfferLocal.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String offerIDPrefix;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long offerID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Offer offer;

	/**
	 * An <tt>Offer</tt> is accepted, once the customer has agreed on all conditions.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date acceptDT = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User acceptUser = null;

	/**
	 * Instead of accepting, a customer may decide to reject an <tt>Offer</tt>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date rejectDT = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User rejectUser = null;

	/**
	 * When an <tt>Offer</tt> has been accepted by the customer, it still needs to be
	 * confirmed by the saler.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date confirmDT = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User confirmUser = null;

	/**
	 * @deprecated Only for JDO!
	 */
	protected OfferLocal() { }

	public OfferLocal(Offer offer)
	{
		this.organisationID = offer.getOrganisationID();
		this.offerIDPrefix = offer.getOfferIDPrefix();
		this.offerID = offer.getOfferID();
		this.offer = offer;
		this.offerActionHandlers = new HashSet<OfferActionHandler>();

		offer.setOfferLocal(this);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getOfferIDPrefix()
	{
		return offerIDPrefix;
	}
	public long getOfferID()
	{
		return offerID;
	}
	public Offer getOffer()
	{
		return offer;
	}

	/**
	 * An <tt>Offer</tt> is accepted, once the customer has agreed on all conditions.
	 *
	 * @return Returns whether it's accepted.
	 */
	public boolean isAccepted()
	{
		return acceptDT != null;
	}
	/**
	 * Instead of accepting, a customer may decide to reject an <tt>Offer</tt>.
	 *
	 * @return Returns whether it's rejected.
	 */
	public boolean isRejected()
	{
		return rejectDT != null;
	}
	/**
	 * When an <tt>Offer</tt> has been accepted by the customer, it still needs to be
	 * confirmed by the saler.
	 *
	 * @return Returns whether it's confirmed.
	 */
	public boolean isConfirmed()
	{
		return confirmDT != null;
	}

	/**
	 * Accepts the <tt>Offer</tt>. This happens after finalization. It means, the customer
	 * has accepted the offer and confirms this acceptance to the saler.
	 * <p>
	 * This method is called by {@link Trader#acceptOffer(User, OfferLocal)}.
	 * </p>
	 */
	protected void accept(User user)
	{
		if (isAccepted())
			return;

		if (!offer.isFinalized())
			throw new IllegalStateException("This Offer ("+offer.getPrimaryKey()+") is not finalized! Call setFinalized() first!");

		if (isRejected())
			throw new IllegalStateException("This Offer ("+offer.getPrimaryKey()+") is not already rejected! Cannot accept! Create a new Offer based on this one.");

		acceptDT = new Date();
		acceptUser = user;
	}
	/**
	 * Rejects the <tt>Offer</tt>. This may happen instead of accepting after finalization.
	 * It means, the customer doesn't want the offer.
	 * <p>
	 * This method is called by {@link Trader#rejectOffer(User, OfferLocal)}.
	 * </p>
	 */
	protected void reject(User user)
	{
		if (isRejected())
			return;

		if (!offer.isFinalized())
			throw new IllegalStateException("This Offer ("+offer.getPrimaryKey()+") is not finalized! Call setFinalized() first!");

		if (isAccepted())
			throw new IllegalStateException("This Offer ("+offer.getPrimaryKey()+") is already accepted! Too late to reject!");

		rejectDT = new Date();
		rejectUser = user;
	}
	/**
	 * Confirms this offer. Confirmation happens after the customer has accepted. This
	 * basically means a notification is sent to the customer that work/delivery will begin.
	 * <p>
	 * This method is called by {@link Trader#confirmOffer(User, OfferLocal)}.
	 * </p>
	 */
	protected void confirm(User user)
	{
		if (isConfirmed())
			return;

		if (!isAccepted())
			throw new IllegalStateException("This Offer ("+offer.getPrimaryKey()+") is not accepted! Call accept() first!");

		confirmDT = new Date();
		acceptUser = user;
	}

	public Date getAcceptDT()
	{
		return acceptDT;
	}
	public User getAcceptUser()
	{
		return acceptUser;
	}

	public Date getRejectDT()
	{
		return rejectDT;
	}
	public User getRejectUser()
	{
		return rejectUser;
	}

	public Date getConfirmDT()
	{
		return confirmDT;
	}
	public User getConfirmUser()
	{
		return confirmUser;
	}

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.trade.OfferActionHandler"
	 *		table="JFireTrade_OfferLocal_offerActionHandlers"
	 *
	 * @jdo.join
	 */
	private Set<OfferActionHandler> offerActionHandlers;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<OfferActionHandler> _offerActionHandlers = null;

	/**
	 * @return Instances of {@link OfferActionHandler}.
	 */
	public Set<OfferActionHandler> getOfferActionHandlers()
	{
		if (_offerActionHandlers == null)
			_offerActionHandlers = Collections.unmodifiableSet(offerActionHandlers);

		return _offerActionHandlers;
	}

	public void addOfferActionHandler(OfferActionHandler offerActionHandler)
	{
		if (!offerActionHandlers.contains(offerActionHandler))
			offerActionHandlers.add(offerActionHandler);
	}

	public boolean removeOfferActionHandler(OfferActionHandler offerActionHandler)
	{
		return offerActionHandlers.remove(offerActionHandler);
	}
}
