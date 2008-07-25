package org.nightlabs.jfire.jbpm.graph.def;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.GraphElement;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.node.EndState;
import org.jbpm.instantiation.Delegation;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;

/**
 * {@link ActionHandlerNodeEnter} registers the node entered in the {@link Statable}
 * of the running process. It resolves the {@link Statable} by a variable set in the
 * process execution context with the key {@link AbstractActionHandler#VARIABLE_NAME_STATABLE_ID}
 * where the StatableID is stored.
 */
public class ActionHandlerNodeEnter
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ActionHandlerNodeEnter.class);

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerNodeEnter.class.getName()));
		action.setName(ActionHandlerNodeEnter.class.getName());

		Event event = new Event("node-enter");
		event.addAction(action);

		jbpmProcessDefinition.addEvent(event);
	}

	private static class LastNodeEnterTransitionCarrier
	{
		public String transitionName;
	}

	private static ThreadLocal<Map<String,LastNodeEnterTransitionCarrier>> organisationID2lastNodeEnterTransitionCarrier = new ThreadLocal<Map<String,LastNodeEnterTransitionCarrier>>() {
		@Override
		protected Map<String,LastNodeEnterTransitionCarrier> initialValue()
		{
			return new HashMap<String, LastNodeEnterTransitionCarrier>();
		}
	};

	private static LastNodeEnterTransitionCarrier getLastNodeEnterTransitionCarrier()
	{
		Map<String,LastNodeEnterTransitionCarrier> m = organisationID2lastNodeEnterTransitionCarrier.get();
		String currentOrganisationID = IDGenerator.getOrganisationID();
		LastNodeEnterTransitionCarrier lastNodeEnterTransitionCarrier = m.get(currentOrganisationID);
		if (lastNodeEnterTransitionCarrier == null) {
			lastNodeEnterTransitionCarrier = new LastNodeEnterTransitionCarrier();
			m.put(currentOrganisationID, lastNodeEnterTransitionCarrier);
		}
		return lastNodeEnterTransitionCarrier;
	}

	/**
	 * This method allows to find out which transition was used for the last node-enter event. It is managed
	 * on a per-Thread base. A typical use case is to leave a Node via a certain transition depending on the
	 * name of the transition that was used to enter it (e.g. simply the same name).
	 *
	 * @return Returns either <code>null</code>, if <code>ActionHandlerNodeEnter</code> was never triggered on
	 *		the current thread or the name of the last transition that was used to enter a node on the current thread.
	 */
	public static String getLastNodeEnterTransitionName()
	{
		return getLastNodeEnterTransitionCarrier().transitionName;
	}

	protected static void setLastNodeEnterTransitionName(String transitionName)
	{
		getLastNodeEnterTransitionCarrier().transitionName = transitionName;
	}

//	/**
//	 * This variable name references the fully qualified name of the class extending {@link StateDefinition}.
//	 * It is used in the {@link ContextInstance}. Usually, this will be the name of one of the following classes:
//	 * {@link OfferStateDefinition},
//	 * {@link org.nightlabs.jfire.accounting.state.InvoiceStateDefinition},
//	 * {@link org.nightlabs.jfire.store.state.DeliveryNoteStateDefinition}.
//	 */
//	public static final String VARIABLE_NAME_STATE_DEFINITION_CLASS = "stateDefinitionClass";

	public static State createStartState(PersistenceManager pm, User user, Statable statable,
			org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		if (logger.isDebugEnabled())
			logger.debug("createStartState: user=" + JDOHelper.getObjectId(user) + " statable=" + JDOHelper.getObjectId(statable) + " jbpmProcessDefinition=" + JDOHelper.getObjectId(jbpmProcessDefinition));

		StateDefinition stateDefinition = (StateDefinition) pm.getObjectById(StateDefinition.getStateDefinitionID(jbpmProcessDefinition.getStartState()));

		if (logger.isDebugEnabled())
			logger.debug("createStartState: stateDefinition=" + JDOHelper.getObjectId(stateDefinition));

		return stateDefinition.createState(user, statable);
//		return (State) pm.makePersistent(
//				new State(
//						IDGenerator.getOrganisationID(), IDGenerator.nextID(State.class),
//						user, statable, stateDefinition));
//		return stateDefinition.createState(user, statable);
	}

	@Override
	@Implement
	protected void doExecute(ExecutionContext executionContext)
			throws Exception
	{
		if (executionContext == null)
			throw new IllegalArgumentException("executionContext == null");

		Action action = executionContext.getAction();
		if (action == null)
			throw new IllegalArgumentException("executionContext.getAction() == null");

		if (executionContext.getToken() == null)
			throw new IllegalArgumentException("executionContext.getToken() == null");

		GraphElement graphElement = executionContext.getToken().getNode();
		if (graphElement == null)
			throw new IllegalArgumentException("executionContext.getToken().getNode() == null");

		if (executionContext.getTransition() == null) {
			// TODO JBPM WORKAROUND - this seems to be a jBPM bug - hence we don't throw an exception but only log it.
			logger.warn("graphElement \"" + graphElement.getName() + "\": executionContext.getTransition() == null (executionContext.getTransitionSource() = " + executionContext.getTransitionSource() + ")", new IllegalArgumentException("graphElement \"" + graphElement.getName() + "\": executionContext.getTransition() == null (executionContext.getTransitionSource() = " + executionContext.getTransitionSource() + ")"));
			setLastNodeEnterTransitionName(null);
		}
		else
			setLastNodeEnterTransitionName(executionContext.getTransition().getName());

		if (logger.isDebugEnabled())
			logger.debug("doExecute: graphElement.class=" + (graphElement == null ? null : graphElement.getClass().getName()) + " graphElement=" + graphElement);

		if (graphElement instanceof EndState) {
			getStatable().getStatableLocal().setProcessEnded();
		}

//		if (!(graphElement instanceof org.jbpm.graph.node.State || graphElement instanceof org.jbpm.graph.node.EndState)) {
		if (!(graphElement instanceof org.jbpm.graph.def.Node)) {
			if (logger.isDebugEnabled())
				logger.debug("doExecute: graphElement is not an instance of an interesting type => return without action!");

			return;
		}

		org.jbpm.graph.def.Node jbpmNode = (org.jbpm.graph.def.Node) graphElement;

		PersistenceManager pm = getPersistenceManager();
		Object statableID = ObjectIDUtil.createObjectID((String) executionContext.getVariable(VARIABLE_NAME_STATABLE_ID));
		Statable statable = (Statable) pm.getObjectById(statableID);

		User user = SecurityReflector.getUserDescriptor().getUser(pm);

//		StateDefinition stateDefinition = (StateDefinition) pm.getObjectById(getStateDefinitionID(
//				(String) executionContext.getVariable(VARIABLE_NAME_STATE_DEFINITION_CLASS), jbpmState));
//		stateDefinition.createState(user, statable);
		StateDefinition stateDefinition = (StateDefinition) pm.getObjectById(StateDefinition.getStateDefinitionID(jbpmNode));

		if (logger.isDebugEnabled())
			logger.debug("doExecute: statable=" + statableID + " user=" + JDOHelper.getObjectId(user) + " stateDefinition=" + JDOHelper.getObjectId(stateDefinition));

		stateDefinition.createState(user, statable);
	}
}
