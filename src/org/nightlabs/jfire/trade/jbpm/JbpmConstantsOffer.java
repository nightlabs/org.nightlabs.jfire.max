package org.nightlabs.jfire.trade.jbpm;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.state.id.StateDefinitionID;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JbpmConstantsOffer
{
	public static class Vendor {
		private static final String PROCESS_DEFINITION_ID = "Offer.Vendor";

		public static final StateDefinitionID STATE_DEFINITION_ID_CREATED = StateDefinitionID.create(
				Organisation.DEVIL_ORGANISATION_ID, PROCESS_DEFINITION_ID, Organisation.DEVIL_ORGANISATION_ID, "created");
		public static final StateDefinitionID STATE_DEFINITION_ID_FINALIZED = StateDefinitionID.create(
				Organisation.DEVIL_ORGANISATION_ID, PROCESS_DEFINITION_ID, Organisation.DEVIL_ORGANISATION_ID, "finalized");
		public static final StateDefinitionID STATE_DEFINITION_ID_ABORTED = StateDefinitionID.create(
				Organisation.DEVIL_ORGANISATION_ID, PROCESS_DEFINITION_ID, Organisation.DEVIL_ORGANISATION_ID, "aborted");
		public static final StateDefinitionID STATE_DEFINITION_ID_ACCEPTED = StateDefinitionID.create(
				Organisation.DEVIL_ORGANISATION_ID, PROCESS_DEFINITION_ID, Organisation.DEVIL_ORGANISATION_ID, "accepted");
		public static final StateDefinitionID STATE_DEFINITION_ID_EXPIRED = StateDefinitionID.create(
				Organisation.DEVIL_ORGANISATION_ID, PROCESS_DEFINITION_ID, Organisation.DEVIL_ORGANISATION_ID, "expired");
		public static final StateDefinitionID STATE_DEFINITION_ID_REJECTED = StateDefinitionID.create(
				Organisation.DEVIL_ORGANISATION_ID, PROCESS_DEFINITION_ID, Organisation.DEVIL_ORGANISATION_ID, "rejected");
		public static final StateDefinitionID STATE_DEFINITION_ID_REVOKED = StateDefinitionID.create(
				Organisation.DEVIL_ORGANISATION_ID, PROCESS_DEFINITION_ID, Organisation.DEVIL_ORGANISATION_ID, "revoked");

	}
	public static class Customer {
		private static final String PROCESS_DEFINITION_ID = "Offer.Customer";
		
	}
	
	// TODO this class should have constants for all StateDefinitionIDs and TransitionIDs applicable to this context and known to JFireTrade
	
//	public static final StateDefinitionID STATE_DEFINITION_ID_CREATED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Vendor", Organisation.DEVIL_ORGANISATION_ID, "created");
//	public static final StateDefinitionID STATE_DEFINITION_ID_FINALIZED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Vendor", Organisation.DEVIL_ORGANISATION_ID, "finalized");
//	public static final StateDefinitionID STATE_DEFINITION_ID_CANCELLED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Vendor", Organisation.DEVIL_ORGANISATION_ID, "cancelled");
//	public static final StateDefinitionID STATE_DEFINITION_ID_ACCEPTED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Vendor", Organisation.DEVIL_ORGANISATION_ID, "accepted");
//	public static final StateDefinitionID STATE_DEFINITION_ID_REJECTED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Vendor", Organisation.DEVIL_ORGANISATION_ID, "rejected");
//	public static final StateDefinitionID STATE_DEFINITION_ID_EXPIRED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Vendor", Organisation.DEVIL_ORGANISATION_ID, "expired");
//	public static final StateDefinitionID STATE_DEFINITION_ID_ABORTED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Vendor", Organisation.DEVIL_ORGANISATION_ID, "aborted");
//	public static final StateDefinitionID STATE_DEFINITION_ID_REVOKED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, "Offer.Vendor", Organisation.DEVIL_ORGANISATION_ID, "revoked");

	private JbpmConstantsOffer() { }
}
