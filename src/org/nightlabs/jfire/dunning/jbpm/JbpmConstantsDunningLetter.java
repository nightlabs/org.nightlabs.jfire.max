package org.nightlabs.jfire.dunning.jbpm;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;

import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class JbpmConstantsDunningLetter
{
	/**
	 * This constant represents a node that used for assigning <code>CREATED</code> state.
	 */
	public static final String NODE_NAME_CREATED = Organisation.DEV_ORGANISATION_ID + ":created";

	/**
	 * This constant represents a node that used for assigning <code>FINALIZED</code> state.
	 */
	public static final String NODE_NAME_FINALIZED = Organisation.DEV_ORGANISATION_ID + ":finalized";

	/**
	 * This constant represents a node that used for assigning <code>BOOKED_IMPLICITELY</code> state.
	 */
	public static final String NODE_NAME_BOOKED_IMPLICITELY = Organisation.DEV_ORGANISATION_ID + ":bookedImplicitely";

	/**
	 * This constant represents a node that used for assigning <code>BOOKED</code> state.
	 */
	public static final String NODE_NAME_BOOKED = Organisation.DEV_ORGANISATION_ID + ":booked";
	
	/**
	 * This constant represents a node that used for assigning <code>ABORTED</code> state.
	 */
	public static final String NODE_NAME_ABORTED = Organisation.DEV_ORGANISATION_ID + ":aborted";
	
	/**
	 * This constant represents a node that used for assigning <code>SENT</code> state.
	 */
	public static final String NODE_NAME_SENT = Organisation.DEV_ORGANISATION_ID + ":sent";
	
	/**
	 * This constant represents a node that used for assigning <code>PAID</code> state.
	 */
	public static final String NODE_NAME_PAID = Organisation.DEV_ORGANISATION_ID + ":paid";
	

	public static final String TRANSITION_NAME_BOOK_IMPLICITELY = Organisation.DEV_ORGANISATION_ID + ":bookImplicitely";
	
	public static final String TRANSITION_NAME_BOOK = Organisation.DEV_ORGANISATION_ID + ":book";

	public static final String TRANSITION_NAME_SEND = Organisation.DEV_ORGANISATION_ID + ":send";
	
	public static final String TRANSITION_NAME_PAY = Organisation.DEV_ORGANISATION_ID + ":pay";

	/**
	 * Sets the State properties for the standard Dunning workflow.
	 * @param processDefinition The {@link ProcessDefinition} to use.
	 */
	public static void initStandardProcessDefinition(ProcessDefinition processDefinition) {
		setStateDefinitionProperties(processDefinition, NODE_NAME_CREATED,
				"Created", "DunningLetter has been created", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_FINALIZED,
				"Finalized", "DunningLetter has been finalized", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_BOOKED_IMPLICITELY,
				"Booked implicitely", "DunningLetter has been booked implicitely", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_BOOKED,
				"Booked", "DunningLetter has been booked", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_ABORTED,
				"Aborted", "DunningLetter has been aborted", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_SENT,
				"Sent", "DunningLetter has been sent", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_PAID,
				"Paid", "DunningLetter has been paid", true);
	}

	private static void setStateDefinitionProperties(
			ProcessDefinition processDefinition, String jbpmNodeName,
			String name, String description, boolean publicState)
	{
		StateDefinition stateDefinition;
		try {
			stateDefinition = StateDefinition.getStateDefinition(processDefinition, jbpmNodeName);
		} catch (JDOObjectNotFoundException x) {
			return;
		}
		stateDefinition.getName().setText(Locale.ENGLISH.getLanguage(), name);
		stateDefinition.getDescription().setText(Locale.ENGLISH.getLanguage(), description);
		stateDefinition.setPublicState(publicState);
	}
}