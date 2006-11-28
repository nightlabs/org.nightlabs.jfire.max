package org.nightlabs.jfire.store.state;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.trade.state.Statable;
import org.nightlabs.jfire.trade.state.State;
import org.nightlabs.jfire.trade.state.StateDefinition;
import org.nightlabs.jfire.trade.state.id.StateDefinitionID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.state.StateDefinition"
 *		detachable="true"
 *		table="JFireTrade_DeliveryNoteStateDefinition"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class DeliveryNoteStateDefinition
extends StateDefinition
{
	private static final long serialVersionUID = 1L;

	public static final StateDefinitionID STATE_DEFINITION_ID_CREATED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, DeliveryNoteStateDefinition.class.getName(), "created");
	public static final StateDefinitionID STATE_DEFINITION_ID_FINALIZED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, DeliveryNoteStateDefinition.class.getName(), "finalized");
	public static final StateDefinitionID STATE_DEFINITION_ID_BOOKED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, DeliveryNoteStateDefinition.class.getName(), "booked");
	public static final StateDefinitionID STATE_DEFINITION_ID_CANCELLED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, DeliveryNoteStateDefinition.class.getName(), "cancelled");

	public static final StateDefinitionID STATE_DEFINITION_ID_DELIVERED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, DeliveryNoteStateDefinition.class.getName(), "delivered");
//	public static final DeliveryNoteStateDefinitionID STATE_DEFINITION_ID_UNDELIVERABLE = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, DeliveryNoteStateDefinition.class.getName(), "undeliverable");

	/**
	 * @deprecated Only for JDO!
	 */
	protected DeliveryNoteStateDefinition() { }

	public DeliveryNoteStateDefinition(StateDefinitionID stateDefinitionID)
	{
		this(stateDefinitionID.organisationID, stateDefinitionID.stateDefinitionID);

		if (!DeliveryNoteStateDefinition.class.getName().equals(stateDefinitionID.stateDefinitionClass))
			throw new IllegalArgumentException("stateDefinitionID.stateDefinitionClass must be " + DeliveryNoteStateDefinition.class.getName() + " but is " + stateDefinitionID.stateDefinitionClass);
	}

	public DeliveryNoteStateDefinition(String organisationID, String stateDefinitionID)
	{
		super(organisationID, DeliveryNoteStateDefinition.class, stateDefinitionID);
	}

	@Override
	protected State _createState(User user, Statable statable)
	{
		return new DeliveryNoteState(IDGenerator.getOrganisationID(), IDGenerator.nextID(DeliveryNoteState.class), user, (DeliveryNote)statable, this);
	}
}
