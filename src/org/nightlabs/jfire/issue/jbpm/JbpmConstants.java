package org.nightlabs.jfire.issue.jbpm;

import java.util.Locale;

import javax.jdo.JDOObjectNotFoundException;

import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * Contains constants for nodes and transitions.
 * <p>
 * This class is never instantiated.
 * </p>
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class JbpmConstants
{
	private JbpmConstants() { }

	// TODO a this class should only hold constants for those nodes + transitions that are really required to be known
	// by the system internally. I guess, at the moment, most of the nodes + transitions here are only used to define
	// a name and a description. This should be done by some descriptor file - see https://www.jfire.org/modules/bugs/view.php?id=386
	// As soon as issue 386 is resolved, we should remove here what's really needed in order to provide maximum flexibility when
	// designing process definitions.

	/**
	 * This constant represents a node that used for assigning <code>NEW</code> state. 
	 */
	public static final String NODE_NAME_NEW = Organisation.DEV_ORGANISATION_ID + ":new";
	
	/**
	 * This constant represents a node that used for assigning <code>OPEN</code> state. 
	 */
	public static final String NODE_NAME_OPEN = Organisation.DEV_ORGANISATION_ID + ":open";
	
	/**
	 * This constant represents a node that used for assigning <code>ACKNOWLEDGED</code> state. 
	 */
	public static final String NODE_NAME_ACKNOWLEDGED = Organisation.DEV_ORGANISATION_ID + ":acknowledged";
	
	/**
	 * This constant represents a node that used for assigning <code>ACKNOWLEDGED IMPLICITELY</code> state. 
	 */
	public static final String NODE_NAME_ACKNOWLEDGED_IMPLICITELY = Organisation.DEV_ORGANISATION_ID + ":acknowledgedImplicitely";
	
	/**
	 * This constant represents a node that used for assigning <code>CONFIRMED</code> state. 
	 */
	public static final String NODE_NAME_CONFIRMED = Organisation.DEV_ORGANISATION_ID + ":confirmed";
	
	/**
	 * This constant represents a node that used for assigning <code>COMFIRMED IMPLICITELY</code> state. 
	 */
	public static final String NODE_NAME_CONFIRMED_IMPLICITELY = Organisation.DEV_ORGANISATION_ID + ":confirmedImplicitely";
	
	/**
	 * This constant represents a node that used for assigning <code>ASSIGNED</code> state. 
	 */
	public static final String NODE_NAME_ASSIGNED = Organisation.DEV_ORGANISATION_ID + ":assigned";
	
	/**
	 * This constant represents a node that used for assigning <code>RESOLVED</code> state. 
	 */
	public static final String NODE_NAME_RESOLVED = Organisation.DEV_ORGANISATION_ID + ":resolved";
	
	/**
	 * This constant represents a node that used for assigning <code>RESOLVED IMPLICITELY</code> state. 
	 */
	public static final String NODE_NAME_RESOLVED_IMPLICITELY = Organisation.DEV_ORGANISATION_ID + ":resolvedImplicitely";
	
	/**
	 * This constant represents a node that used for assigning <code>CLOSED</code> state. 
	 */
	public static final String NODE_NAME_CLOSED = Organisation.DEV_ORGANISATION_ID + ":closed";
	
	/**
	 * This constant represents a node that used for assigning <code>REOPENED</code> state. 
	 */
	public static final String NODE_NAME_REOPENED = Organisation.DEV_ORGANISATION_ID + ":reopened";
	
	/**
	 * This constant represents a node that used for assigning <code>REJECTED</code> state. 
	 */
	public static final String NODE_NAME_REJECTED = Organisation.DEV_ORGANISATION_ID + ":rejected";

//	public static final String TRANSITION_NAME_NEW = "new";
//	public static final String TRANSITION_NAME_OPEN = "open";
//	public static final String TRANSITION_NAME_ACKNOWLEDGE = "acknowledge";
//	public static final String TRANSITION_NAME_CONFIRM = "confirm";
//	public static final String TRANSITION_NAME_ASSIGN = "assign";
//	public static final String TRANSITION_NAME_RESOLVE = "resolve";
//	public static final String TRANSITION_NAME_CLOSE = "close";
//	public static final String TRANSITION_NAME_REOPEN = "reopen";
//	public static final String TRANSITION_NAME_REJECT = "reject";

	/**
	 * This transition is used to automatically move to the state "assigned" when a user is assigned to
	 * an issue. If this transition is not available at the time this un-assignment
	 * is done, no transition will be automatically performed (and no exception will be thrown!).
	 */
	public static final String TRANSITION_NAME_ASSIGN = Organisation.DEV_ORGANISATION_ID + ":assign";

	/**
	 * This transition is used to automatically move to a non-assigned state (usually "open") when
	 * the assignee of an issue is cleared. If this transition is not available at the time this un-assignment
	 * is done, no transition will be automatically performed (and no exception will be thrown!).
	 */
	public static final String TRANSITION_NAME_UNASSIGN = Organisation.DEV_ORGANISATION_ID + ":unassign";

	/**
	 * Sets the State properties for the standard IssueTracking workflow.
	 * @param processDefinition The {@link ProcessDefinition} to use.
	 */
	public static void initStandardProcessDefinition(ProcessDefinition processDefinition) {
		setStateDefinitionProperties(processDefinition, NODE_NAME_NEW, 
				"New", "Issue has been created", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_OPEN, 
				"Open", "Issue has been opened", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_ACKNOWLEDGED, 
				"Acknowledged", "Issue has been acknowledged", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_ACKNOWLEDGED_IMPLICITELY, 
				"Acknowledged implicitely", "Issue has been acknowledged while setting a differen state.", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_CONFIRMED, 
				"Confirmed", "Issue has been confirmed", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_CONFIRMED_IMPLICITELY, 
				"Confirmed implicitely", "Issue has been confirmed while setting a different state", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_ASSIGNED, 
				"Assigned", "Issue has been assigned", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_RESOLVED, 
				"Resolved", "Issue has been resolved", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_RESOLVED_IMPLICITELY, 
				"Resolved implicitely", "Issue has been resolved while closing", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_CLOSED, 
				"Closed", "Issue has been closed", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_REOPENED, 
				"Reopened", "Issue has been reopened", true);
		setStateDefinitionProperties(processDefinition, NODE_NAME_REJECTED, 
				"Rejected", "Issue has been rejected", true);
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
