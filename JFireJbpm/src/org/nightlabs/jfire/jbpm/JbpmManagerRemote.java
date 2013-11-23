package org.nightlabs.jfire.jbpm;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.jbpm.graph.def.Transition;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.jbpm.graph.def.id.StateDefinitionID;
import org.nightlabs.jfire.jbpm.graph.def.id.StateID;
import org.nightlabs.jfire.jbpm.graph.def.id.TransitionID;
import org.nightlabs.jfire.jbpm.query.StatableQuery;

@Remote
public interface JbpmManagerRemote {

	String ping(String message);

	void initialise() throws Exception;

	List<State> getStates(Set<StateID> stateIDs, String[] fetchGroups,
			int maxFetchDepth);

	Set<StateID> getStateIDs(ObjectID statableID);

	/**
	 * @param userExecutable If <code>null</code>, it is ignored. If not <code>null</code>, the query filters only transitions where userExecutable has this value.
	 */
	Set<TransitionID> getTransitionIDs(StateID stateID, Boolean userExecutable);

	/**
	 * @param stateDefinitionID The StateDefinition from which the transitions leave.
	 * @param userExecutable If <code>null</code>, it is ignored. If not <code>null</code>, the query filters only transitions where userExecutable has this value.
	 */
	Set<TransitionID> getTransitionIDs(StateDefinitionID stateDefinitionID, Boolean userExecutable);

	List<Transition> getTransitions(Set<TransitionID> transitionIDs, String[] fetchGroups, int maxFetchDepth);

	Set<StateDefinitionID> getStateDefinitionIDs(ProcessDefinition processDefinition);

	Collection<StateDefinition> getStateDefinitions(Set<StateDefinitionID> objectIDs, String[] fetchGroups, int maxFetchDepth);

	Collection<ProcessDefinition> getProcessDefinitions(Set<ProcessDefinitionID> objectIDs, String[] fetchGroups, int maxFetchDepth);

	Set<Statable> getStatables(QueryCollection<? extends StatableQuery> statableQueries);

}