package org.nightlabs.jfire.store.state;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.trade.state.State;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.state.State"
 *		detachable="true"
 *		table="JFireTrade_DeliveryNoteState"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class DeliveryNoteState
extends State
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	protected DeliveryNoteState() { }

	public DeliveryNoteState(String organisationID, long stateID, User user, DeliveryNote deliveryNote, DeliveryNoteStateDefinition deliveryNoteStateDefinition)
	{
		super(organisationID, stateID, user, deliveryNote, deliveryNoteStateDefinition);
	}

	/**
	 * This is a convenience method calling the super method {@link State#getStatable()}.
	 * @return the result of {@link State#getStatable()}
	 */
	public DeliveryNote getDeliveryNote()
	{
		return (DeliveryNote) getStatable();
	}
	/**
	 * This is a convenience method calling the super method {@link State#getStateDefinition()}.
	 * @return the result of {@link State#getStateDefinition()}
	 */
	public DeliveryNoteStateDefinition getDeliveryNoteStateDefinition()
	{
		return (DeliveryNoteStateDefinition) getStateDefinition();
	}
}
