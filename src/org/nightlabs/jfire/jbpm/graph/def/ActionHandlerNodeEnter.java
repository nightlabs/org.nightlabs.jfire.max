package org.nightlabs.jfire.jbpm.graph.def;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.GraphElement;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.instantiation.Delegation;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.state.id.StateDefinitionID;

public class ActionHandlerNodeEnter
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerNodeEnter.class.getName()));
		action.setName(ActionHandlerNodeEnter.class.getName());

		Event event = new Event("node-enter");
		event.addAction(action);

		jbpmProcessDefinition.addEvent(event);
	}

	/**
	 * This variable name references the toString()-representation of the {@link ObjectID} which
	 * references the instance of {@link Statable} for which the {@link ProcessInstance} has been
	 * created.
	 */
	public static final String VARIABLE_NAME_STATABLE_ID = "statableID";
//	/**
//	 * This variable name references the fully qualified name of the class extending {@link StateDefinition}.
//	 * It is used in the {@link ContextInstance}. Usually, this will be the name of one of the following classes:
//	 * {@link OfferStateDefinition},
//	 * {@link org.nightlabs.jfire.accounting.state.InvoiceStateDefinition},
//	 * {@link org.nightlabs.jfire.store.state.DeliveryNoteStateDefinition}.
//	 */
//	public static final String VARIABLE_NAME_STATE_DEFINITION_CLASS = "stateDefinitionClass";

	public static State createStartState(PersistenceManager pm, User user, Statable statable,
			Class stateDefinitionClass, org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		StateDefinition stateDefinition = (StateDefinition) pm.getObjectById(StateDefinition.getStateDefinitionID(jbpmProcessDefinition.getStartState()));
		return (State) pm.makePersistent(
				new State(
						IDGenerator.getOrganisationID(), IDGenerator.nextID(State.class),
						user, statable, stateDefinition));
//		return stateDefinition.createState(user, statable);
	}

	@Implement
	protected void doExecute(ExecutionContext executionContext)
			throws Exception
	{
		GraphElement graphElement = executionContext.getEventSource();
		if (!(graphElement instanceof org.jbpm.graph.node.State))
			return;

		org.jbpm.graph.node.State jbpmState = (org.jbpm.graph.node.State) graphElement;

		PersistenceManager pm = getPersistenceManager();
		Object statableID = ObjectIDUtil.createObjectID((String) executionContext.getVariable(VARIABLE_NAME_STATABLE_ID));
		Statable statable = (Statable) pm.getObjectById(statableID);

		User user = SecurityReflector.getUserDescriptor().getUser(pm);

//		StateDefinition stateDefinition = (StateDefinition) pm.getObjectById(getStateDefinitionID(
//				(String) executionContext.getVariable(VARIABLE_NAME_STATE_DEFINITION_CLASS), jbpmState));
//		stateDefinition.createState(user, statable);
		StateDefinition stateDefinition = (StateDefinition) pm.getObjectById(StateDefinition.getStateDefinitionID(jbpmState));
		pm.makePersistent(new State(IDGenerator.getOrganisationID(), IDGenerator.nextID(State.class), user, statable, stateDefinition));
	}
}
