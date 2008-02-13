package org.nightlabs.jfire.jbpm.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.jbpm.JbpmManager;
import org.nightlabs.jfire.jbpm.JbpmManagerUtil;
import org.nightlabs.jfire.jbpm.graph.def.ProcessDefinition;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ProcessDefinitionDAO
extends BaseJDOObjectDAO<ProcessDefinitionID, ProcessDefinition>
{
	private static ProcessDefinitionDAO sharedInstance;
	public static ProcessDefinitionDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ProcessDefinitionDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new ProcessDefinitionDAO();
			}
		}
		return sharedInstance;
	}
	
	protected ProcessDefinitionDAO() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Collection<ProcessDefinition> retrieveJDOObjects(
			Set<ProcessDefinitionID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	throws Exception
	{
		monitor.beginTask("Loading ProcessDefintions", 2);
		monitor.worked(1);
		JbpmManager jbpmManager = JbpmManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		Collection<ProcessDefinition> processDefintions = jbpmManager.getProcessDefinitions(objectIDs, fetchGroups, maxFetchDepth);
		monitor.worked(1);
		return processDefintions;
	}

	public Collection<ProcessDefinition> getProcessDefinitions(Set<ProcessDefinitionID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	throws Exception
	{
		return retrieveJDOObjects(objectIDs, fetchGroups, maxFetchDepth, monitor);
	}

}
