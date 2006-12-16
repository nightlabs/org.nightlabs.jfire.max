package org.nightlabs.jfire.accounting.jbpm;

import org.nightlabs.jfire.organisation.Organisation;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JbpmConstantsInvoice
{
	public static class Both {
		public static final String NODE_NAME_SENT   = Organisation.DEVIL_ORGANISATION_ID + ":sent";
		public static final String NODE_NAME_BOOKED = Organisation.DEVIL_ORGANISATION_ID + ":booked";
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
//				Organisation.DEVIL_ORGANISATION_ID, "Invoice.Vendor");

		public static final String NODE_NAME_CREATED       = Organisation.DEVIL_ORGANISATION_ID + ":created";
		public static final String NODE_NAME_FINALIZED     = Organisation.DEVIL_ORGANISATION_ID + ":finalized";
		public static final String NODE_NAME_ABORTED       = Organisation.DEVIL_ORGANISATION_ID + ":aborted";
		public static final String NODE_NAME_PAID          = Organisation.DEVIL_ORGANISATION_ID + ":paid";
		public static final String NODE_NAME_SENT_REMINDER = Organisation.DEVIL_ORGANISATION_ID + ":sentReminder";
		public static final String NODE_NAME_SENT_PRE_COLLECTION_LETTER = Organisation.DEVIL_ORGANISATION_ID + ":sentPreCollectionLetter";
		public static final String NODE_NAME_DOUBTFUL      = Organisation.DEVIL_ORGANISATION_ID + ":doubtful";
		public static final String NODE_NAME_BOOKED_UNRECEIVABLE = Organisation.DEVIL_ORGANISATION_ID + ":bookedUnreceivable";
		public static final String NODE_NAME_UNCOLLECTABLE = Organisation.DEVIL_ORGANISATION_ID + ":uncollectable";

		public static final String NODE_NAME_BOOKED_IMPLICITELY = Organisation.DEVIL_ORGANISATION_ID + ":bookedImplicitely";

		/**
		 * This transition is automatically triggered asynchronously as soon as the user finalized the document.
		 */
		public static final String TRANSITION_NAME_BOOK = Organisation.DEVIL_ORGANISATION_ID + ":book";

		/**
		 * This transition is automatically triggered asynchronously as soon as the document has been booked.
		 */
		public static final String TRANSITION_NAME_SEND = Organisation.DEVIL_ORGANISATION_ID + ":send";

		public static final String TRANSITION_NAME_BOOK_IMPLICITELY = Organisation.DEVIL_ORGANISATION_ID + ":bookImplicitely";

		/**
		 * This transition is automatically triggered synchronously as soon as the invoice
		 * has been completely paid. It is not triggered by a partial payment, if there's still
		 * a due amount outstanding after this payment. In other words, it is only triggered by a partial
		 * payment, if it was the last partial payment and there is no outstanding money left.
		 */
		public static final String TRANSITION_NAME_PAY = Organisation.DEVIL_ORGANISATION_ID + ":pay";
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
//				Organisation.DEVIL_ORGANISATION_ID, "Invoice.Customer");

		public static final String NODE_NAME_PAID = Organisation.DEVIL_ORGANISATION_ID + ":paid";

		public static final String TRANSITION_NAME_PAY = Organisation.DEVIL_ORGANISATION_ID + ":pay";
	}

	private JbpmConstantsInvoice() { }
}
