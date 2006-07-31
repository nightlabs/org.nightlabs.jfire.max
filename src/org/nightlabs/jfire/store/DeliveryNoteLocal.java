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

package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.jfire.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.DeliveryNoteLocalID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryNoteLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, deliveryNoteIDPrefix, deliveryNoteID"
 *
 * @jdo.fetch-group name="DeliveryNote.deliveryNoteLocal" fields="deliveryNote"
 * @jdo.fetch-group name="DeliveryNoteLocal.deliveryNote" fields="deliveryNote"
 * @jdo.fetch-group name="DeliveryNoteLocal.bookUser" fields="bookUser"
 * @jdo.fetch-group name="DeliveryNoteLocal.this" fields="deliveryNote, bookUser"
 */
public class DeliveryNoteLocal
implements Serializable
{
	public static final String FETCH_GROUP_DELIVERY_NOTE = "DeliveryNoteLocal.deliveryNote";
	public static final String FETCH_GROUP_BOOK_USER = "DeliveryNoteLocal.bookUser";
	public static final String FETCH_GROUP_THIS_DELIVERY_NOTE_LOCAL = "DeliveryNoteLocal.this";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String deliveryNoteIDPrefix;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long deliveryNoteID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private DeliveryNote deliveryNote;

	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private boolean delivered = false;

	/**
	 * This member stores the user who booked this DeliveryNote.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User bookUser = null;
	
	/**
	 * This member stores when this DeliveryNote was booked.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date bookDT  = null;

	/**
	 * @deprecated Only for JDO!
	 */
	protected DeliveryNoteLocal() { }

	public DeliveryNoteLocal(DeliveryNote deliveryNote)
	{
		this.organisationID = deliveryNote.getOrganisationID();
		this.deliveryNoteIDPrefix = deliveryNote.getDeliveryNoteIDPrefix();
		this.deliveryNoteID = deliveryNote.getDeliveryNoteID();
		this.deliveryNote = deliveryNote;

		deliveryNote.setDeliveryNoteLocal(this);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getDeliveryNoteIDPrefix()
	{
		return deliveryNoteIDPrefix;
	}
	public long getDeliveryNoteID()
	{
		return deliveryNoteID;
	}
	public DeliveryNote getDeliveryNote()
	{
		return deliveryNote;
	}

	protected void setBooked(User bookUser) {
		this.bookUser = bookUser;
		this.bookDT = new Date();
	}
	public User getBookUser() {
		return bookUser;
	}
	public Date getBookDT() {
		return bookDT;
	}
	public boolean isBooked() {
		return bookDT != null;
	}

	public boolean isDelivered() {
		return delivered;
	}
	public void setDelivered(boolean delivered) {
		this.delivered = delivered;
	}
}
