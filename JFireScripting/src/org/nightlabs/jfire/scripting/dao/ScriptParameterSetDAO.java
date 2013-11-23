/**
 *
 */
package org.nightlabs.jfire.scripting.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.scripting.ScriptManagerRemote;
import org.nightlabs.jfire.scripting.ScriptParameterSet;
import org.nightlabs.jfire.scripting.id.ScriptParameterSetID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Accessor for ScriptParameterSets.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[ÐOT]de>
 * @author Daniel Mazurek <daniel[AT]nightlabs[ÐOT]de>
 */
public class ScriptParameterSetDAO
extends BaseJDOObjectDAO<ScriptParameterSetID, ScriptParameterSet>
{
	private static ScriptParameterSetDAO sharedInstance;

	public static ScriptParameterSetDAO sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new ScriptParameterSetDAO();
		return sharedInstance;
	}

	private ScriptParameterSetDAO() {}

	/**
	 * Returns the collection of all {@link ScriptParameterSet}s for the given organisationID
	 */
	public Collection<ScriptParameterSet> getScriptParameterSets(String organisationID, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		try {
			ScriptManagerRemote sm = getEjbProvider().getRemoteBean(ScriptManagerRemote.class);
			Set<ScriptParameterSetID> ids = sm.getAllScriptParameterSetIDs(organisationID);
			return getJDOObjects(null, ids, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ScriptParameterSet getScriptParameterSet(ScriptParameterSetID scriptParameterSetID, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor)
	{
		return getJDOObject(null, scriptParameterSetID, fetchGroups, maxFetchDepth, monitor);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<ScriptParameterSet> retrieveJDOObjects(
			Set<ScriptParameterSetID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		monitor.beginTask("Loading ScriptParameterSets", 100);
		try {
			ScriptManagerRemote sm = getEjbProvider().getRemoteBean(ScriptManagerRemote.class);
			monitor.worked(50);
			Collection<ScriptParameterSet> result = sm.getScriptParameterSets(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(50);
			return result;
		}
		finally {
			monitor.done();
		}
	}

}
