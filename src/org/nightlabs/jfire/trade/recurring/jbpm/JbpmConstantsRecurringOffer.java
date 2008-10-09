/**
 * 
 */
package org.nightlabs.jfire.trade.recurring.jbpm;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.trade.jbpm.JbpmConstantsOffer;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class JbpmConstantsRecurringOffer {
	
	public static class Vendor extends JbpmConstantsOffer.Vendor {
		public static final String NODE_NAME_RECURRENCE_STARTED =
			Organisation.DEV_ORGANISATION_ID + ":recurrenceStarted";
	
		public static final String NODE_NAME_RECURRENCE_STOPED =
			Organisation.DEV_ORGANISATION_ID + ":recurrenceStoped";
		
		public static final String TRANSITION_NAME_START_RECURRENCE =
			Organisation.DEV_ORGANISATION_ID + ":startRecurrence";
		public static final String TRANSITION_NAME_PAUSE_RECURRENCE =
			Organisation.DEV_ORGANISATION_ID + ":pauseRecurrence";
		public static final String TRANSITION_NAME_STOP_RECURRENCE =
			Organisation.DEV_ORGANISATION_ID + ":stopRecurrence";
	}
}
