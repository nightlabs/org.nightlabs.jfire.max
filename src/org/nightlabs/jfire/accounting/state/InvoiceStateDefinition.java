package org.nightlabs.jfire.accounting.state;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
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
 *		table="JFireTrade_InvoiceStateDefinition"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class InvoiceStateDefinition
extends StateDefinition
{
	private static final long serialVersionUID = 1L;

	public static final StateDefinitionID STATE_DEFINITION_ID_CREATED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, InvoiceStateDefinition.class.getName(), "created");
	public static final StateDefinitionID STATE_DEFINITION_ID_FINALIZED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, InvoiceStateDefinition.class.getName(), "finalized");
	public static final StateDefinitionID STATE_DEFINITION_ID_BOOKED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, InvoiceStateDefinition.class.getName(), "booked");
	public static final StateDefinitionID STATE_DEFINITION_ID_CANCELLED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, InvoiceStateDefinition.class.getName(), "cancelled");
	public static final StateDefinitionID STATE_DEFINITION_ID_PAID = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, InvoiceStateDefinition.class.getName(), "paid");
	public static final StateDefinitionID STATE_DEFINITION_ID_DOUBTFUL = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, InvoiceStateDefinition.class.getName(), "doubtful");
	public static final StateDefinitionID STATE_DEFINITION_ID_UNCOLLECTABLE = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, InvoiceStateDefinition.class.getName(), "uncollectable");

	/**
	 * @deprecated Only for JDO!
	 */
	protected InvoiceStateDefinition() { }

	public InvoiceStateDefinition(StateDefinitionID stateDefinitionID)
	{
		this(stateDefinitionID.organisationID, stateDefinitionID.stateDefinitionID);

		if (!InvoiceStateDefinition.class.getName().equals(stateDefinitionID.stateDefinitionClass))
			throw new IllegalArgumentException("stateDefinitionID.stateDefinitionClass must be " + InvoiceStateDefinition.class.getName() + " but is " + stateDefinitionID.stateDefinitionClass);
	}

	public InvoiceStateDefinition(String organisationID, String stateDefinitionID)
	{
		super(organisationID, InvoiceStateDefinition.class, stateDefinitionID);
	}

	@Override
	protected State _createState(User user, Statable statable)
	{
		return new InvoiceState(user.getOrganisationID(), IDGenerator.nextID(InvoiceState.class), user, (Invoice)statable, this);
	}
}
