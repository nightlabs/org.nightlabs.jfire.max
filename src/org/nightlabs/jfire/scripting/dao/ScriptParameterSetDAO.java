/**
 * 
 */
package org.nightlabs.jfire.scripting.dao;

import java.util.Collection;
import java.util.Set;

import javax.jdo.FetchPlan;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.scripting.ScriptManager;
import org.nightlabs.jfire.scripting.ScriptManagerUtil;
import org.nightlabs.jfire.scripting.ScriptParameterSet;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ScriptParameterSetDAO extends BaseJDOObjectDAO<ScriptRegistryItemID, ScriptParameterSet> {

	public static final String[] DEFAULT_FETCH_GROUPS = new String[] {
		FetchPlan.DEFAULT, 
		ScriptParameterSet.FETCH_GROUP_PARAMETERS,
		ScriptParameterSet.FETCH_GROUP_NAME
	};
	
	/**
	 * 
	 */
	public ScriptParameterSetDAO() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Collection<ScriptParameterSet> retrieveJDOObjects(
			Set<ScriptRegistryItemID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		ScriptManager sm = ScriptManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		return sm.getScriptParameterSets(objectIDs, fetchGroups, maxFetchDepth);
	}
	
	
	public ScriptParameterSet getScriptParameterSet(ScriptRegistryItemID scriptRegistryItemID, String[] fetchGroups, ProgressMonitor monitor) {
		return getJDOObject(null, scriptRegistryItemID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	/**
	 * A static instance of ScriptParameterSetDAO.
	 */
	private static ScriptParameterSetDAO sharedInstance;

	/**
	 * Returns a static instance of ScriptParameterSetDAO.
	 * It will be layzily created on demand.
	 *
	 * @return A static instance of ScriptParameterSetDAO.
	 */
	public static ScriptParameterSetDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ScriptParameterSetDAO.class) {
				if (sharedInstance == null) {
					sharedInstance = new ScriptParameterSetDAO();
				}
			}
		}
		return sharedInstance;
	}
}
