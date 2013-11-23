package org.nightlabs.jfire.accounting.jbpm;

import org.nightlabs.jfire.organisation.Organisation;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class JbpmConstantsInvoice
{
	public static class Both {
		public static final String NODE_NAME_SENT   = Organisation.DEV_ORGANISATION_ID + ":sent";
		public static final String NODE_NAME_BOOKED = Organisation.DEV_ORGANISATION_ID + ":booked";

		public static final String NODE_NAME_PAID          = Organisation.DEV_ORGANISATION_ID + ":paid";

		/**
		 * <p>
		 * This transition is automatically triggered synchronously as soon as the invoice
		 * has been completely paid. It is not triggered by a partial payment, if there's still
		 * an amount outstanding after this payment. In other words, it is only triggered by a partial
		 * payment, if it was the last partial payment and there is no outstanding money left.
		 * </p>
		 * <p>
		 * It is triggered by
		 * {@link org.nightlabs.jfire.accounting.Accounting#payEnd(org.nightlabs.jfire.security.User, org.nightlabs.jfire.accounting.pay.PaymentData)}.
		 * </p>
		 */
		public static final String TRANSITION_NAME_PAY = Organisation.DEV_ORGANISATION_ID + ":pay";
	}

	public static class Vendor {
		public static final String NODE_NAME_CREATED       = Organisation.DEV_ORGANISATION_ID + ":created";
		public static final String NODE_NAME_FINALIZED     = Organisation.DEV_ORGANISATION_ID + ":finalized";
		public static final String NODE_NAME_ABORTED       = Organisation.DEV_ORGANISATION_ID + ":aborted";
		public static final String NODE_NAME_SENT_REMINDER = Organisation.DEV_ORGANISATION_ID + ":sentReminder";
		public static final String NODE_NAME_SENT_PRE_COLLECTION_LETTER = Organisation.DEV_ORGANISATION_ID + ":sentPreCollectionLetter";
		public static final String NODE_NAME_DOUBTFUL      = Organisation.DEV_ORGANISATION_ID + ":doubtful";
		public static final String NODE_NAME_BOOKED_UNRECEIVABLE = Organisation.DEV_ORGANISATION_ID + ":bookedUnreceivable";
		public static final String NODE_NAME_UNCOLLECTABLE = Organisation.DEV_ORGANISATION_ID + ":uncollectable";

		public static final String NODE_NAME_BOOKED_IMPLICITELY = Organisation.DEV_ORGANISATION_ID + ":bookedImplicitely";

		/**
		 * This transition is automatically triggered asynchronously as soon as the user finalized the document.
		 */
		public static final String TRANSITION_NAME_BOOK = Organisation.DEV_ORGANISATION_ID + ":book";

		/**
		 * This transition is automatically triggered asynchronously as soon as the document has been booked.
		 */
		public static final String TRANSITION_NAME_SEND = Organisation.DEV_ORGANISATION_ID + ":send";

		public static final String TRANSITION_NAME_BOOK_IMPLICITELY = Organisation.DEV_ORGANISATION_ID + ":bookImplicitely";
	}

	public static class CustomerLocal {
		public static final String TRANSITION_NAME_BOOK_IMPLICITELY = Vendor.TRANSITION_NAME_BOOK_IMPLICITELY;
	}

	public static class CustomerCrossOrganisation {
		
	}

	private JbpmConstantsInvoice() { }
}
