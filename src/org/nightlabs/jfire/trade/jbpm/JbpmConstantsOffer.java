package org.nightlabs.jfire.trade.jbpm;

import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.jbpm.graph.def.id.TransitionID;
import org.nightlabs.jfire.organisation.Organisation;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JbpmConstantsOffer
{
	public static class Vendor {
		public static final ProcessDefinitionID PROCESS_DEFINITION_ID = ProcessDefinitionID.create(
				Organisation.DEVIL_ORGANISATION_ID, "Offer.Vendor");

		public static final String STATE_DEFINITION_JBPM_NODE_NAME_CREATED   = Organisation.DEVIL_ORGANISATION_ID + ":created";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_FINALIZED = Organisation.DEVIL_ORGANISATION_ID + ":finalized";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_SENT      = Organisation.DEVIL_ORGANISATION_ID + ":sent";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_ABORTED   = Organisation.DEVIL_ORGANISATION_ID + ":aborted";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_ACCEPTED  = Organisation.DEVIL_ORGANISATION_ID + ":accepted";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_EXPIRED   = Organisation.DEVIL_ORGANISATION_ID + ":expired";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_REJECTED  = Organisation.DEVIL_ORGANISATION_ID + ":rejected";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_REVOKED   = Organisation.DEVIL_ORGANISATION_ID + ":revoked";

//		public static final StateDefinitionID STATE_DEFINITION_ID_CREATED = StateDefinitionID.create(
//				PROCESS_DEFINITION_ID.organisationID, PROCESS_DEFINITION_ID.processDefinitionID,
//				Organisation.DEVIL_ORGANISATION_ID, "created");
//
//		public static final StateDefinitionID STATE_DEFINITION_ID_FINALIZED = StateDefinitionID.create(
//				PROCESS_DEFINITION_ID.organisationID, PROCESS_DEFINITION_ID.processDefinitionID,
//				Organisation.DEVIL_ORGANISATION_ID, "finalized");
//
//		public static final StateDefinitionID STATE_DEFINITION_ID_ABORTED = StateDefinitionID.create(
//				PROCESS_DEFINITION_ID.organisationID, PROCESS_DEFINITION_ID.processDefinitionID,
//				Organisation.DEVIL_ORGANISATION_ID, "aborted");
//
//		public static final StateDefinitionID STATE_DEFINITION_ID_ACCEPTED = StateDefinitionID.create(
//				PROCESS_DEFINITION_ID.organisationID, PROCESS_DEFINITION_ID.processDefinitionID,
//				Organisation.DEVIL_ORGANISATION_ID, "accepted");
//
//		public static final StateDefinitionID STATE_DEFINITION_ID_EXPIRED = StateDefinitionID.create(
//				PROCESS_DEFINITION_ID.organisationID, PROCESS_DEFINITION_ID.processDefinitionID,
//				Organisation.DEVIL_ORGANISATION_ID, "expired");
//
//		public static final StateDefinitionID STATE_DEFINITION_ID_REJECTED = StateDefinitionID.create(
//				PROCESS_DEFINITION_ID.organisationID, PROCESS_DEFINITION_ID.processDefinitionID,
//				Organisation.DEVIL_ORGANISATION_ID, "rejected");
//
//		public static final StateDefinitionID STATE_DEFINITION_ID_REVOKED = StateDefinitionID.create(
//				PROCESS_DEFINITION_ID.organisationID, PROCESS_DEFINITION_ID.processDefinitionID,
//				Organisation.DEVIL_ORGANISATION_ID, "revoked");

		public static TransitionID getTransitionID_created_2_accept(ProcessDefinitionID processDefinitionID) {
			return TransitionID.create(
					processDefinitionID.organisationID, processDefinitionID.processDefinitionID,
					Organisation.DEVIL_ORGANISATION_ID, "created",
					Organisation.DEVIL_ORGANISATION_ID, "accept");
		}

		public static TransitionID getTransitionID_finalized_2_send(ProcessDefinitionID processDefinitionID) {
			return TransitionID.create(
					processDefinitionID.organisationID, processDefinitionID.processDefinitionID,
					Organisation.DEVIL_ORGANISATION_ID, "finalized",
					Organisation.DEVIL_ORGANISATION_ID, "send");
		}

		public static TransitionID getTransitionID_finalized_2_customerAccepted(ProcessDefinitionID processDefinitionID) {
			return TransitionID.create(
					processDefinitionID.organisationID, processDefinitionID.processDefinitionID,
					Organisation.DEVIL_ORGANISATION_ID, "finalized",
					Organisation.DEVIL_ORGANISATION_ID, "customerAccepted");
		}

		public static TransitionID getTransitionID_finalized_2_customerRejected(ProcessDefinitionID processDefinitionID) {
			return TransitionID.create(
					processDefinitionID.organisationID, processDefinitionID.processDefinitionID,
					Organisation.DEVIL_ORGANISATION_ID, "finalized",
					Organisation.DEVIL_ORGANISATION_ID, "customerRejected");
		}

//		public static final TransitionID TRANSITION_ID_CREATED_2_ACCEPT = TransitionID.create(
//				PROCESS_DEFINITION_ID.organisationID, PROCESS_DEFINITION_ID.processDefinitionID,
//				Organisation.DEVIL_ORGANISATION_ID, "created",
//				Organisation.DEVIL_ORGANISATION_ID, "accept");
//
//		public static final TransitionID TRANSITION_ID_FINALIZED_2_CUSTOMER_ACCEPTED = TransitionID.create(
//				PROCESS_DEFINITION_ID.organisationID, PROCESS_DEFINITION_ID.processDefinitionID,
//				Organisation.DEVIL_ORGANISATION_ID, "finalized",
//				Organisation.DEVIL_ORGANISATION_ID, "customerAccepted");
//
//		public static final TransitionID TRANSITION_ID_FINALIZED_2_CUSTOMER_REJECTED = TransitionID.create(
//				PROCESS_DEFINITION_ID.organisationID, PROCESS_DEFINITION_ID.processDefinitionID,
//				Organisation.DEVIL_ORGANISATION_ID, "finalized",
//				Organisation.DEVIL_ORGANISATION_ID, "customerRejected");
	}

	public static class Customer {
		public static final ProcessDefinitionID PROCESS_DEFINITION_ID = ProcessDefinitionID.create(
				Organisation.DEVIL_ORGANISATION_ID, "Offer.Customer");

		public static final String STATE_DEFINITION_JBPM_NODE_NAME_SENT              = Organisation.DEVIL_ORGANISATION_ID + ":sent";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_REVOKED           = Organisation.DEVIL_ORGANISATION_ID + ":revoked";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_EXPIRED           = Organisation.DEVIL_ORGANISATION_ID + ":expired";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_CUSTOMER_ACCEPTED = Organisation.DEVIL_ORGANISATION_ID + ":customerAccepted";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_CUSTOMER_REJECTED = Organisation.DEVIL_ORGANISATION_ID + ":customerRejected";

		public static TransitionID getTransitionID_sent_2_revoked(ProcessDefinitionID processDefinitionID) {
			return TransitionID.create(
					processDefinitionID.organisationID, processDefinitionID.processDefinitionID,
					Organisation.DEVIL_ORGANISATION_ID, "sent",
					Organisation.DEVIL_ORGANISATION_ID, "revoked");
		}

		public static TransitionID getTransitionID_sent_2_expired(ProcessDefinitionID processDefinitionID) {
			return TransitionID.create(
					processDefinitionID.organisationID, processDefinitionID.processDefinitionID,
					Organisation.DEVIL_ORGANISATION_ID, "sent",
					Organisation.DEVIL_ORGANISATION_ID, "expired");
		}

//		public static final StateDefinitionID STATE_DEFINITION_ID_FINALIZED = StateDefinitionID.create(
//				PROCESS_DEFINITION_ID.organisationID, PROCESS_DEFINITION_ID.processDefinitionID,
//				Organisation.DEVIL_ORGANISATION_ID, "finalized");
//
//		public static final StateDefinitionID STATE_DEFINITION_ID_CUSTOMER_ACCEPTED = StateDefinitionID.create(
//				PROCESS_DEFINITION_ID.organisationID, PROCESS_DEFINITION_ID.processDefinitionID,
//				Organisation.DEVIL_ORGANISATION_ID, "customerAccepted");
//
//		public static final StateDefinitionID STATE_DEFINITION_ID_CUSTOMER_REJECTED = StateDefinitionID.create(
//				PROCESS_DEFINITION_ID.organisationID, PROCESS_DEFINITION_ID.processDefinitionID,
//				Organisation.DEVIL_ORGANISATION_ID, "customerRejected");
	}

	private JbpmConstantsOffer() { }
}
