package org.nightlabs.jfire.trade.jbpm;

import org.nightlabs.jfire.organisation.Organisation;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JbpmConstantsOffer
{
	public static class Both {
		public static final String NODE_NAME_SENT      = Organisation.DEV_ORGANISATION_ID + ":sent";
		public static final String NODE_NAME_EXPIRED   = Organisation.DEV_ORGANISATION_ID + ":expired";
		public static final String NODE_NAME_REVOKED   = Organisation.DEV_ORGANISATION_ID + ":revoked";

		public static final String NODE_NAME_ACCEPTED_IMPLICITELY = Organisation.DEV_ORGANISATION_ID + ":acceptedImplicitely";
		public static final String NODE_NAME_ACCEPTED_FOR_CROSS_TRADE = "dev.jfire.org:acceptedForCrossTrade";
	}

	public static class Vendor {
//		/**
//		 * <p>
//		 * This is the default process definition id as provided by the developer team. If a user uploads and assigns
//		 * another process definition, the id will be different. A normal user cannot upload a process definition with
//		 * this id.
//		 * </p>
//		 * <p>
//		 * Which process definition will be used, is defined by {@link ProcessDefinitionAssignment} instances.
//		 * </p>
//		 */
//		public static final ProcessDefinitionID PROCESS_DEFINITION_ID = ProcessDefinitionID.create(
//				Organisation.DEV_ORGANISATION_ID, "Offer.Vendor");

		public static final String NODE_NAME_CREATED   = Organisation.DEV_ORGANISATION_ID + ":created";
		public static final String NODE_NAME_FINALIZED = Organisation.DEV_ORGANISATION_ID + ":finalized";
		public static final String NODE_NAME_ABORTED   = Organisation.DEV_ORGANISATION_ID + ":aborted";
		public static final String NODE_NAME_ACCEPTED  = Organisation.DEV_ORGANISATION_ID + ":accepted";
		public static final String NODE_NAME_REJECTED  = Organisation.DEV_ORGANISATION_ID + ":rejected";

//		public static final String NODE_NAME_PREPARED_FOR_CROSS_TRADE = "dev.jfire.org:preparedForCrossTrade";
		public static final String NODE_NAME_FINALIZED_FOR_CROSS_TRADE = "dev.jfire.org:finalizedForCrossTrade";
		public static final String NODE_NAME_SENT_FOR_CROSS_TRADE = "dev.jfire.org:sentForCrossTrade";

		/**
		 * This transition is automatically triggered asynchronously as soon as the document has been finalized.
		 */
		public static final String TRANSITION_NAME_SEND = Organisation.DEV_ORGANISATION_ID + ":send";

		/**
		 * This transiation will be passed when a Offer is finalized directly (not via
		 * {@link #TRANSITION_NAME_ACCEPT_IMPLICITELY} or
		 * {@link #TRANSITION_NAME_ACCEPT_FOR_CROSS_TRADE}).
		 */
		public static final String TRANSITION_NAME_FINALIZE = Organisation.DEV_ORGANISATION_ID + ":finalize";

		/**
		 * This transition is triggered when acceptance is required and implicitely accepting possible. For example,
		 * when working only in the Order editor, during a payment, this transition will be done implicitely, just as
		 * an invoice is implicitely created (and the offer itself was implicitely created).
		 */
		public static final String TRANSITION_NAME_ACCEPT_IMPLICITELY = Organisation.DEV_ORGANISATION_ID + ":acceptImplicitely";

		public static final String TRANSITION_NAME_EXPIRE = Organisation.DEV_ORGANISATION_ID + ":expire";

		public static final String TRANSITION_NAME_CUSTOMER_ACCEPTED = JbpmConstantsOffer.Customer.NODE_NAME_CUSTOMER_ACCEPTED;
		public static final String TRANSITION_NAME_CUSTOMER_REJECTED = JbpmConstantsOffer.Customer.NODE_NAME_CUSTOMER_REJECTED;

//		public static final String TRANSITION_NAME_PREPARE_FOR_CROSS_TRADE = "dev.jfire.org:prepareForCrossTrade";
		public static final String TRANSITION_NAME_FINALIZE_FOR_CROSS_TRADE = "dev.jfire.org:finalizeForCrossTrade";
		public static final String TRANSITION_NAME_ACCEPT_FOR_CROSS_TRADE = "dev.jfire.org:acceptForCrossTrade";

//		public static TransitionID getTransitionID_created_2_accept(ProcessDefinitionID processDefinitionID) {
//			return TransitionID.create(
//					processDefinitionID.organisationID, processDefinitionID.processDefinitionID,
//					Organisation.DEV_ORGANISATION_ID, "created",
//					Organisation.DEV_ORGANISATION_ID, "accept");
//		}
//
//		public static TransitionID getTransitionID_finalized_2_send(ProcessDefinitionID processDefinitionID) {
//			return TransitionID.create(
//					processDefinitionID.organisationID, processDefinitionID.processDefinitionID,
//					Organisation.DEV_ORGANISATION_ID, "finalized",
//					Organisation.DEV_ORGANISATION_ID, "send");
//		}
//
//		public static TransitionID getTransitionID_finalized_2_customerAccepted(ProcessDefinitionID processDefinitionID) {
//			return TransitionID.create(
//					processDefinitionID.organisationID, processDefinitionID.processDefinitionID,
//					Organisation.DEV_ORGANISATION_ID, "finalized",
//					Organisation.DEV_ORGANISATION_ID, "customerAccepted");
//		}
//
//		public static TransitionID getTransitionID_finalized_2_customerRejected(ProcessDefinitionID processDefinitionID) {
//			return TransitionID.create(
//					processDefinitionID.organisationID, processDefinitionID.processDefinitionID,
//					Organisation.DEV_ORGANISATION_ID, "finalized",
//					Organisation.DEV_ORGANISATION_ID, "customerRejected");
//		}
	}

	public static class Customer {
//		/**
//		 * <p>
//		 * This is the default process definition id as provided by the developer team. If a user uploads and assigns
//		 * another process definition, the id will be different. A normal user cannot upload a process definition with
//		 * this id.
//		 * </p>
//		 * <p>
//		 * Which process definition will be used, is defined by {@link ProcessDefinitionAssignment} instances.
//		 * </p>
//		 */
//		public static final ProcessDefinitionID PROCESS_DEFINITION_ID = ProcessDefinitionID.create(
//				Organisation.DEV_ORGANISATION_ID, "Offer.Customer");

		public static final String NODE_NAME_CUSTOMER_ACCEPTED = Organisation.DEV_ORGANISATION_ID + ":customerAccepted";
		public static final String NODE_NAME_CUSTOMER_REJECTED = Organisation.DEV_ORGANISATION_ID + ":customerRejected";

		public static final String TRANSITION_NAME_ACCEPTED_IMPLICITELY = JbpmConstantsOffer.Both.NODE_NAME_ACCEPTED_IMPLICITELY;
		public static final String TRANSITION_NAME_EXPIRED = JbpmConstantsOffer.Both.NODE_NAME_EXPIRED;
		public static final String TRANSITION_NAME_REVOKED = JbpmConstantsOffer.Both.NODE_NAME_REVOKED;

		public static final String TRANSITION_NAME_ACCEPTED_FOR_CROSS_TRADE = "dev.jfire.org:acceptedForCrossTrade";

//		public static TransitionID getTransitionID_sent_2_revoked(ProcessDefinitionID processDefinitionID) {
//			return TransitionID.create(
//					processDefinitionID.organisationID, processDefinitionID.processDefinitionID,
//					Organisation.DEV_ORGANISATION_ID, "sent",
//					Organisation.DEV_ORGANISATION_ID, "revoked");
//		}
//
//		public static TransitionID getTransitionID_sent_2_expired(ProcessDefinitionID processDefinitionID) {
//			return TransitionID.create(
//					processDefinitionID.organisationID, processDefinitionID.processDefinitionID,
//					Organisation.DEV_ORGANISATION_ID, "sent",
//					Organisation.DEV_ORGANISATION_ID, "expired");
//		}
	}

	private JbpmConstantsOffer() { }
}
