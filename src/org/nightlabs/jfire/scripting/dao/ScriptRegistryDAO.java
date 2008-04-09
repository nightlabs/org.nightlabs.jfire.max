package org.nightlabs.jfire.scripting.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.scripting.ScriptManager;
import org.nightlabs.jfire.scripting.ScriptManagerUtil;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.scripting.id.ScriptRegistryID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ScriptRegistryDAO extends BaseJDOObjectDAO<ScriptRegistryID, ScriptRegistry>
{
	/**
	 * A static instance of ScriptRegistryDAO.
	 */
	private static ScriptRegistryDAO sharedInstance;

	/**
	 * Returns a static instance of ScriptRegistryDAO.
	 * It will be lazily created on demand.
	 *
	 * @return A static instance of ScriptRegistryDAO.
	 */
	public static ScriptRegistryDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ScriptRegistryDAO.class) {
				if (sharedInstance == null) {
					sharedInstance = new ScriptRegistryDAO();
				}
			}
		}
		return sharedInstance;
	}

	private ScriptRegistryDAO() {}

	@Override
	protected Collection<ScriptRegistry> retrieveJDOObjects(Set<ScriptRegistryID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		ScriptManager scriptManager = ScriptManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		List<ScriptRegistry> l = new ArrayList<ScriptRegistry>(1);
		l.add(scriptManager.getScriptRegistry());
		return l;
	}

	public ScriptRegistry getScriptRegistry(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			ScriptManager scriptManager = ScriptManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return scriptManager.getScriptRegistry();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
