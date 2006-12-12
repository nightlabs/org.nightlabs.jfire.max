package org.nightlabs.jfire.accounting.jbpm;

import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.jbpm.graph.def.id.TransitionID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.state.id.StateDefinitionID;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JbpmConstantsInvoice
{
	public static class Vendor {
		public static final ProcessDefinitionID PROCESS_DEFINITION_ID = ProcessDefinitionID.create(
				Organisation.DEVIL_ORGANISATION_ID, "Invoice.Vendor");

		public static final String STATE_DEFINITION_JBPM_NODE_NAME_CREATED       = Organisation.DEVIL_ORGANISATION_ID + ":created";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_FINALIZED     = Organisation.DEVIL_ORGANISATION_ID + ":finalized";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_SENT          = Organisation.DEVIL_ORGANISATION_ID + ":sent";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_ABORTED       = Organisation.DEVIL_ORGANISATION_ID + ":aborted";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_BOOKED        = Organisation.DEVIL_ORGANISATION_ID + ":booked";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_PAID          = Organisation.DEVIL_ORGANISATION_ID + ":paid";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_SENT_REMINDER = Organisation.DEVIL_ORGANISATION_ID + ":sentReminder";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_SENT_PRE_COLLECTION_LETTER = Organisation.DEVIL_ORGANISATION_ID + ":sentPreCollectionLetter";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_DOUBTFUL      = Organisation.DEVIL_ORGANISATION_ID + ":doubtful";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_BOOKED_UNRECEIVABLE = Organisation.DEVIL_ORGANISATION_ID + ":bookedUnreceivable";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_UNCOLLECTABLE = Organisation.DEVIL_ORGANISATION_ID + ":uncollectable";
	}

	public static class Customer {
		public static final ProcessDefinitionID PROCESS_DEFINITION_ID = ProcessDefinitionID.create(
				Organisation.DEVIL_ORGANISATION_ID, "Invoice.Customer");

		public static final String STATE_DEFINITION_JBPM_NODE_NAME_SENT          = Organisation.DEVIL_ORGANISATION_ID + ":sent";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_BOOKED        = Organisation.DEVIL_ORGANISATION_ID + ":booked";
		public static final String STATE_DEFINITION_JBPM_NODE_NAME_PAID          = Organisation.DEVIL_ORGANISATION_ID + ":paid";

		public static TransitionID getTransitionID_pay(StateDefinitionID stateDefinitionID) {
			return TransitionID.create(
					stateDefinitionID.processDefinitionOrganisationID, stateDefinitionID.processDefinitionID,
					stateDefinitionID.stateDefinitionOrganisationID, stateDefinitionID.stateDefinitionID,
					Organisation.DEVIL_ORGANISATION_ID, "pay");
		}
	}

	// TODO this class should have constants for all StateDefinitionIDs and TransitionIDs applicable to this context and known to JFireTrade
//	public static final StateDefinitionID STATE_DEFINITION_ID_CREATED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, JbpmConstantsInvoice.class.getName(), "created");
//	public static final StateDefinitionID STATE_DEFINITION_ID_FINALIZED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, JbpmConstantsInvoice.class.getName(), "finalized");
//	public static final StateDefinitionID STATE_DEFINITION_ID_BOOKED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, JbpmConstantsInvoice.class.getName(), "booked");
//	public static final StateDefinitionID STATE_DEFINITION_ID_CANCELLED = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, JbpmConstantsInvoice.class.getName(), "cancelled");
//	public static final StateDefinitionID STATE_DEFINITION_ID_PAID = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, JbpmConstantsInvoice.class.getName(), "paid");
//	public static final StateDefinitionID STATE_DEFINITION_ID_DOUBTFUL = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, JbpmConstantsInvoice.class.getName(), "doubtful");
//	public static final StateDefinitionID STATE_DEFINITION_ID_UNCOLLECTABLE = StateDefinitionID.create(Organisation.DEVIL_ORGANISATION_ID, JbpmConstantsInvoice.class.getName(), "uncollectable");

	private JbpmConstantsInvoice() { }
}
