package org.nightlabs.jfire.jbpm.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.jbpm.JbpmManager;
import org.nightlabs.jfire.jbpm.JbpmManagerUtil;
import org.nightlabs.jfire.jbpm.graph.def.Transition;
import org.nightlabs.jfire.jbpm.graph.def.id.StateDefinitionID;
import org.nightlabs.jfire.jbpm.graph.def.id.StateID;
import org.nightlabs.jfire.jbpm.graph.def.id.TransitionID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

public class TransitionDAO
		extends BaseJDOObjectDAO<TransitionID, Transition>
{
	private static TransitionDAO sharedInstance = null;
	public static synchronized TransitionDAO sharedInstance()
	{
		if (sharedInstance == null)
			sharedInstance = new TransitionDAO();

		return sharedInstance;
	}

	protected TransitionDAO() { }

	@Override
	protected Collection<Transition> retrieveJDOObjects(
			Set<TransitionID> transitionIDs, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
			throws Exception
	{
		JbpmManager m = JbpmManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		return m.getTransitions(transitionIDs, fetchGroups, maxFetchDepth);
	}

	public List<Transition> getTransitions(StateDefinitionID stateDefinitionID, Boolean userExecutable,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		Set<TransitionID> transitionIDs;
		try {
			JbpmManager m = JbpmManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			transitionIDs = m.getTransitionIDs(stateDefinitionID, userExecutable);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return getJDOObjects(null, transitionIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public List<Transition> getTransitions(StateID stateID, Boolean userExecutable,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		Set<TransitionID> transitionIDs;
		try {
			JbpmManager m = JbpmManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			transitionIDs = m.getTransitionIDs(stateID, userExecutable);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return getJDOObjects(null, transitionIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public List<Transition> getTransitions(Set<TransitionID> transitionIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, transitionIDs, fetchGroups, maxFetchDepth, monitor);
	}
}
