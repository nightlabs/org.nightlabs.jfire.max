package org.nightlabs.jfire.jbpm.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.jbpm.JbpmManagerRemote;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.id.StateID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

public class StateDAO
extends BaseJDOObjectDAO<StateID, State>
{
	private static StateDAO sharedInstance = null;

	public static StateDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (StateDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new StateDAO();
			}
		}
		return sharedInstance;
	}

	protected StateDAO() { }

	@Override
	protected Collection<State> retrieveJDOObjects(Set<StateID> stateIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		JbpmManagerRemote m = JFireEjb3Factory.getRemoteBean(JbpmManagerRemote.class, SecurityReflector.getInitialContextProperties());
		return m.getStates(stateIDs, fetchGroups, maxFetchDepth);
	}

	public State getState(StateID stateID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, stateID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<State> getStates(Set<StateID> stateIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, stateIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public List<State> getStates(ObjectID statableID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		Set<StateID> stateIDs;
		try {
			JbpmManagerRemote m = JFireEjb3Factory.getRemoteBean(JbpmManagerRemote.class, SecurityReflector.getInitialContextProperties());
			stateIDs = m.getStateIDs(statableID);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
		return getJDOObjects(null, stateIDs, fetchGroups, maxFetchDepth, monitor);
	}
}
