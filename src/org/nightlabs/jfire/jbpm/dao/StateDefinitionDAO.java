package org.nightlabs.jfire.jbpm.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.jbpm.JbpmManager;
import org.nightlabs.jfire.jbpm.JbpmManagerUtil;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.jbpm.graph.def.id.StateDefinitionID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class StateDefinitionDAO
extends BaseJDOObjectDAO<StateDefinitionID, StateDefinition>
{
	private static StateDefinitionDAO sharedInstance;
	public static StateDefinitionDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (StateDefinitionDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new StateDefinitionDAO();
			}
		}
		return sharedInstance;
	}
	
	protected StateDefinitionDAO() {
		super();
	}

	@Override
	protected Collection<StateDefinition> retrieveJDOObjects(
			Set<StateDefinitionID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	throws Exception
	{
		monitor.beginTask("Loading StateDefintions", 2);
		monitor.worked(1);
		JbpmManager jbpmManager = JbpmManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		Collection<StateDefinition> stateDefintions = jbpmManager.getStateDefinitions(objectIDs, fetchGroups, maxFetchDepth);
		monitor.worked(1);
		return stateDefintions;
	}
	
	public Collection<StateDefinition> getStateDefintions(
			Set<StateDefinitionID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	throws Exception
	{
		return retrieveJDOObjects(objectIDs, fetchGroups, maxFetchDepth, monitor);
	}
}
