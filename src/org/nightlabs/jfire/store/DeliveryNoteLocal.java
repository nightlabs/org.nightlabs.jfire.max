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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.DeliveryNoteLocalID"
 *		detachable="true"
 *		table="JFireTrade_DeliveryNoteLocal"
 *
 * @jdo.implements name="org.nightlabs.jfire.jbpm.graph.def.StatableLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, deliveryNoteIDPrefix, deliveryNoteID"
 *		include-body="id/DeliveryNoteLocalID.body.inc"
 *
 * @jdo.fetch-group name="DeliveryNote.deliveryNoteLocal" fields="deliveryNote"
 * @jdo.fetch-group name="DeliveryNoteLocal.deliveryNote" fields="deliveryNote"
 * @jdo.fetch-group name="DeliveryNoteLocal.bookUser" fields="bookUser"
 * @jdo.fetch-group name="DeliveryNoteLocal.this" fields="deliveryNote, bookUser, state, states"
 *
 * @jdo.fetch-group name="StatableLocal.state" fields="state"
 * @jdo.fetch-group name="StatableLocal.states" fields="states"
 *
 */
public class DeliveryNoteLocal
implements Serializable, StatableLocal
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

//	/**
//	 * @jdo.field persistence-modifier="persistent"	 
//	 */
//	private boolean delivered = false;

	/**
	 * @jdo.field persistence-modifier="persistent"	 
	 */
	private int deliveredArticleCount = 0;

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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private State state;

	/**
	 * This is the history of <b>public</b> {@link State}s with the newest last and the oldest first.
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="State"
	 *		table="JFireTrade_DeliveryNoteLocal_states"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private List<State> states;

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

	/**
	 * @return the same as {@link #getStatable()}
	 */
	public DeliveryNote getDeliveryNote()
	{
		return deliveryNote;
	}
	public Statable getStatable()
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

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="DeliveryNoteActionHandler"
	 *		table="JFireTrade_DeliveryNoteLocal_deliveryNoteActionHandlers"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Set<DeliveryNoteActionHandler> deliveryNoteActionHandlers;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<DeliveryNoteActionHandler> _deliveryNoteActionHandlers = null;

	/**
	 * @return Instances of {@link DeliveryNoteActionHandler}.
	 */
	public Set<DeliveryNoteActionHandler> getDeliveryNoteActionHandlers()
	{
		if (_deliveryNoteActionHandlers == null)
			_deliveryNoteActionHandlers = Collections.unmodifiableSet(deliveryNoteActionHandlers);

		return _deliveryNoteActionHandlers;
	}

	public void addDeliveryNoteActionHandler(DeliveryNoteActionHandler deliveryNoteActionHandler)
	{
		if (!deliveryNoteActionHandlers.contains(deliveryNoteActionHandler))
			deliveryNoteActionHandlers.add(deliveryNoteActionHandler);
	}

	public boolean removeDeliveryNoteActionHandler(DeliveryNoteActionHandler deliveryNoteActionHandler)
	{
		return deliveryNoteActionHandlers.remove(deliveryNoteActionHandler);
	}

//	public boolean isDelivered() {
//		return delivered;
//	}
//	public void setDelivered(boolean delivered) {
//		this.delivered = delivered;
//	}


	/**
	 * This method is <b>not</b> intended to be called directly. It is called by
	 * {@link State#State(String, long, User, Statable, org.nightlabs.jfire.jbpm.graph.def.StateDefinition)}
	 * which is called automatically by {@link ActionHandlerNodeEnter}, if this <code>ActionHandler</code> is registered.
	 */
	public void setState(State currentState)
	{
		if (currentState == null)
			throw new IllegalArgumentException("state must not be null!");

		this.state = (State)currentState;
		this.states.add((State)currentState);
	}

	public State getState()
	{
		return state;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient List<State> _states = null;

	public List<State> getStates()
	{
		if (_states == null)
			_states = CollectionUtil.castList(Collections.unmodifiableList(states));

		return _states;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long jbpmProcessInstanceId = -1;

	public long getJbpmProcessInstanceId()
	{
		return jbpmProcessInstanceId;
	}

	public void setJbpmProcessInstanceId(long jbpmProcessInstanceId)
	{
		this.jbpmProcessInstanceId = jbpmProcessInstanceId;
	}

	public int getDeliveredArticleCount()
	{
		return deliveredArticleCount;
	}

	/**
	 * This method is called by {@link DeliveryNote#bookDeliverProductTransfer(org.nightlabs.jfire.store.deliver.DeliverProductTransfer, Map, boolean)}.
	 *
	 * @return the new value after incrementing it
	 */
	protected int incDeliveredArticleCount(int count)
	{
		if (count < 0)
			return decDeliveredArticleCount(-count);

		int newDeliveredArticleCount = deliveredArticleCount + count;

		if (newDeliveredArticleCount > deliveryNote.getArticleCount())
			throw new IllegalArgumentException("deliveredArticleCount + count > deliveryNote.getArticleCount() !!!");

		deliveredArticleCount = newDeliveredArticleCount;
		return deliveredArticleCount;
	}

	/**
	 * This method is called by {@link DeliveryNote#bookDeliverProductTransfer(org.nightlabs.jfire.store.deliver.DeliverProductTransfer, Map, boolean)}.
	 *
	 * @return the new value after decrementing it
	 */
	protected int decDeliveredArticleCount(int count)
	{
		if (count > deliveredArticleCount)
			throw new IllegalArgumentException("count > deliveredArticleCount !!!");

		deliveredArticleCount -= count;
		return deliveredArticleCount;
	}
}
