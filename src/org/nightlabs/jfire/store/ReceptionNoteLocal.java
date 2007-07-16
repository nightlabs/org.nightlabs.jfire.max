package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.nightlabs.jfire.jbpm.graph.def.ActionHandlerNodeEnter;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ReceptionNoteLocalID"
 *		detachable="true"
 *		table="JFireTrade_ReceptionNoteLocal"
 *
 * @jdo.implements name="org.nightlabs.jfire.jbpm.graph.def.StatableLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, deliveryNoteIDPrefix, deliveryNoteID, receptionNoteID"
 */
public class ReceptionNoteLocal
implements
		Serializable,
		StatableLocal
{
	private static final long serialVersionUID = 1L;

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
	 * @jdo.field primary-key="true"
	 */
	private int receptionNoteID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReceptionNote receptionNote;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ReceptionNoteLocal() { }

	public ReceptionNoteLocal(ReceptionNote receptionNote)
	{
		this.receptionNote = receptionNote;
		this.organisationID = receptionNote.getOrganisationID();
		this.deliveryNoteIDPrefix = receptionNote.getDeliveryNoteIDPrefix();
		this.deliveryNoteID = receptionNote.getDeliveryNoteID();
		this.receptionNoteID = receptionNote.getReceptionNoteID();

		receptionNote.setReceptionNoteLocal(this);
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
	public int getReceptionNoteID()
	{
		return receptionNoteID;
	}

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
	 *		table="JFireTrade_ReceptionNoteLocal_states"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private List<State> states;

	public ReceptionNote getReceptionNote()
	{
		return receptionNote;
	}
	public Statable getStatable()
	{
		return receptionNote;
	}

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
}
