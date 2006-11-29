package org.nightlabs.jfire.trade.state;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.state.id.StateDefinitionID;

public class ActionHandlerNodeEnter
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	public static final String VARIABLE_NAME_STATABLE_ID = "statableID";
	public static final String VARIABLE_NAME_STATE_DEFINITION_CLASS = "stateDefinitionClass";

	@Implement
	protected void doExecute(ExecutionContext executionContext)
			throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		Object statableID = executionContext.getVariable(VARIABLE_NAME_STATABLE_ID);
		Statable statable = (Statable) pm.getObjectById(statableID);

		org.jbpm.graph.node.State jbpmState = (org.jbpm.graph.node.State) executionContext.getEventSource();
		String stateName = jbpmState.getName();
		String _organisationID;
		String _stateDefinitionClass = (String) executionContext.getVariable(VARIABLE_NAME_STATE_DEFINITION_CLASS);
		String _stateDefinitionID;
		if (stateName.indexOf(':') < 0) {
			_organisationID = IDGenerator.getOrganisationID(); // TODO is it safe to allow local names (without organisationID)? do we really never share process definitions across organisations?
			_stateDefinitionID = stateName;
		}
		else {
			String[] parts = stateName.split(":");
			if (parts.length != 2)
				throw new IllegalStateException("state.name does not contain exactly one or two parts: " + stateName);
			_organisationID = parts[0];
			_stateDefinitionID = parts[1];
		}

		User user = SecurityReflector.getUserDescriptor().getUser(pm);

		StateDefinition stateDefinition = (StateDefinition) pm.getObjectById(StateDefinitionID.create(_organisationID, _stateDefinitionClass, _stateDefinitionID));
		stateDefinition.createState(user, statable);
	}
}
