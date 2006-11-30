package org.nightlabs.jfire.trade.state;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.state.id.StateDefinitionID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class OfferStateDefinitionUtil
{
	public static final StateDefinitionID STATE_DEFINITION_ID_CREATED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Customer", Organisation.DEVIL_ORGANISATION_ID, "created");
	public static final StateDefinitionID STATE_DEFINITION_ID_FINALIZED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Customer", Organisation.DEVIL_ORGANISATION_ID, "finalized");
	public static final StateDefinitionID STATE_DEFINITION_ID_CANCELLED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Customer", Organisation.DEVIL_ORGANISATION_ID, "cancelled");
	public static final StateDefinitionID STATE_DEFINITION_ID_ACCEPTED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Customer", Organisation.DEVIL_ORGANISATION_ID, "accepted");
	public static final StateDefinitionID STATE_DEFINITION_ID_REJECTED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Customer", Organisation.DEVIL_ORGANISATION_ID, "rejected");
	public static final StateDefinitionID STATE_DEFINITION_ID_EXPIRED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Customer", Organisation.DEVIL_ORGANISATION_ID, "expired");
	public static final StateDefinitionID STATE_DEFINITION_ID_ABORTED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Customer", Organisation.DEVIL_ORGANISATION_ID, "aborted");
	public static final StateDefinitionID STATE_DEFINITION_ID_REVOKED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Customer", Organisation.DEVIL_ORGANISATION_ID, "revoked");

	private OfferStateDefinitionUtil() { }
}
