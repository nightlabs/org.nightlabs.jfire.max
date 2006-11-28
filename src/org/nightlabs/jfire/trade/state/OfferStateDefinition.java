package org.nightlabs.jfire.trade.state;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.state.id.StateDefinitionID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.state.ArticleContainerStateDefinition"
 *		detachable="true"
 *		table="JFireTrade_OfferStateDefinition"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class OfferStateDefinition
extends ArticleContainerStateDefinition
{
	private static final long serialVersionUID = 1L;

	public static final StateDefinitionID STATE_DEFINITION_ID_CREATED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, OfferStateDefinition.class.getName(), "created");
	public static final StateDefinitionID STATE_DEFINITION_ID_FINALIZED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, OfferStateDefinition.class.getName(), "finalized");
	public static final StateDefinitionID STATE_DEFINITION_ID_CANCELLED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, OfferStateDefinition.class.getName(), "cancelled");
	public static final StateDefinitionID STATE_DEFINITION_ID_ACCEPTED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, OfferStateDefinition.class.getName(), "accepted");
	public static final StateDefinitionID STATE_DEFINITION_ID_REJECTED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, OfferStateDefinition.class.getName(), "rejected");
	public static final StateDefinitionID STATE_DEFINITION_ID_EXPIRED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, OfferStateDefinition.class.getName(), "expired");
	public static final StateDefinitionID STATE_DEFINITION_ID_ABORTED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, OfferStateDefinition.class.getName(), "aborted");
	public static final StateDefinitionID STATE_DEFINITION_ID_REVOKED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, OfferStateDefinition.class.getName(), "revoked");

	/**
	 * @deprecated Only for JDO!
	 */
	protected OfferStateDefinition() { }

	public OfferStateDefinition(StateDefinitionID stateDefinitionID)
	{
		this(stateDefinitionID.organisationID, stateDefinitionID.stateDefinitionID);

		if (!OfferStateDefinition.class.getName().equals(stateDefinitionID.stateDefinitionClass))
			throw new IllegalArgumentException("stateDefinitionID.stateDefinitionClass must be " + OfferStateDefinition.class.getName() + " but is " + stateDefinitionID.stateDefinitionClass);
	}

	public OfferStateDefinition(String organisationID, String articleContainerStateDefinitionID)
	{
		super(organisationID, OfferStateDefinition.class, articleContainerStateDefinitionID);
	}

	protected State _createState(User user, Statable statable)
	{
		return new OfferState(user.getOrganisationID(), IDGenerator.nextID(State.class), user, (Offer)statable, this);
	}
}
