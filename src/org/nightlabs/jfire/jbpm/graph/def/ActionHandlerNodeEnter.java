package org.nightlabs.jfire.jbpm.graph.def;


import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.security.User;

/**
 * {@link ActionHandlerNodeEnter} registers the node entered in the {@link Statable}
 * of the running process. It resolves the {@link Statable} by a variable set in the
 * process execution context with the key {@link AbstractActionHandler#VARIABLE_NAME_STATABLE_ID}
 * where the StatableID is stored.
 *
 * @deprecated Not necessary anymore as it has been replaced by AOP. This whole class will be removed soon!
 * The method {@link AbstractActionHandler#getLastNodeEnterTransitionName()} has moved to the super-class and
 * you should access it statically via <code>AbstractActionHandler</code>.
 */
@Deprecated
public class ActionHandlerNodeEnter
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ActionHandlerNodeEnter.class);

	/**
	 * @deprecated
	 */
	@Deprecated
	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
//		Action action = new Action(new Delegation(ActionHandlerNodeEnter.class.getName()));
//		action.setName(ActionHandlerNodeEnter.class.getName());
//
//		Event event = new Event("node-enter");
//		event.addAction(action);
//
//		jbpmProcessDefinition.addEvent(event);
	}
//

	/**
	 * @deprecated We've moved this method into {@link ProcessDefinition}. This method will be removed soon!!!!
	 */
	@Deprecated
	public static State createStartState(PersistenceManager pm, User user, Statable statable,
			org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		return ProcessDefinition.createStartState(pm, user, statable, jbpmProcessDefinition);
	}

	@Override
	protected void doExecute(ExecutionContext executionContext)
			throws Exception
	{
		/*if (executionContext == null)
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

		stateDefinition.createState(user, statable);*/
	}
}
