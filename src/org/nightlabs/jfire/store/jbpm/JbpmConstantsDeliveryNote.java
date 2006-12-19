package org.nightlabs.jfire.store.jbpm;

import org.nightlabs.jfire.organisation.Organisation;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JbpmConstantsDeliveryNote
{
	public static class Both {
		public static final String NODE_NAME_SENT          = Organisation.DEVIL_ORGANISATION_ID + ":sent";
		public static final String NODE_NAME_BOOKED        = Organisation.DEVIL_ORGANISATION_ID + ":booked";
		public static final String NODE_NAME_DELIVERED     = Organisation.DEVIL_ORGANISATION_ID + ":delivered";

		/**
		 * On the vendor side, this transition is automatically triggered asynchronously as soon as the user
		 * finalized the document. On the customer side, the booking is done asynchronously after the delivery
		 * (i.e. after the {@link #NODE_NAME_DELIVERED} node has been reached).
		 */
		public static final String TRANSITION_NAME_BOOK = Organisation.DEVIL_ORGANISATION_ID + ":book";

		public static final String TRANSITION_NAME_DELIVER = Organisation.DEVIL_ORGANISATION_ID + ":deliver";
	}

	public static class Vendor {
//		public static final ProcessDefinitionID PROCESS_DEFINITION_ID = ProcessDefinitionID.create(
//				Organisation.DEVIL_ORGANISATION_ID, "DeliveryNote.Vendor");

		public static final String NODE_NAME_CREATED    = Organisation.DEVIL_ORGANISATION_ID + ":created";
		public static final String NODE_NAME_FINALIZED  = Organisation.DEVIL_ORGANISATION_ID + ":finalized";
		public static final String NODE_NAME_ABORTED    = Organisation.DEVIL_ORGANISATION_ID + ":aborted";
		
		public static final String NODE_NAME_BOOKED_IMPLICITELY = Organisation.DEVIL_ORGANISATION_ID + ":bookedImplicitely";

		public static final String TRANSITION_NAME_BOOK_IMPLICITELY = Organisation.DEVIL_ORGANISATION_ID + ":bookImplicitely";

		/**
		 * This transition is automatically triggered asynchronously as soon as the document has been booked.
		 */
		public static final String TRANSITION_NAME_SEND = Organisation.DEVIL_ORGANISATION_ID + ":send";
	}

	public static class Customer {
		public static final String NODE_NAME_CREATED_RECEPTION_NOTE  = Organisation.DEVIL_ORGANISATION_ID + ":createdReceptionNote";
	}

	private JbpmConstantsDeliveryNote() { }
}
