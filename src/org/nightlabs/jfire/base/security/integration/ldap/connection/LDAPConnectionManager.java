package org.nightlabs.jfire.base.security.integration.ldap.connection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.FetchPlanBackup;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;

/**
 * Simple LDAPConnection pool.
 *
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPConnectionManager{

	/**
	 * Number of possible connections in the pool per every LDAP server
	 */
	private static final int maxConnectionsPerServer = 5;

	/**
	 * Static singleton instance. If used in cluster environment I think there's no problem if
	 * every cluster will have it's own singleton instance and a bunch of connections.
	 */
	private static LDAPConnectionManager _instance;

	/**
	 * Map of free LDAP connections
	 */
	private Map<ILDAPConnectionParamsProvider, List<LDAPConnection>> availableConnections;

	private LDAPConnectionManager(){
		availableConnections = new HashMap<ILDAPConnectionParamsProvider, List<LDAPConnection>>();
	}

	public static synchronized LDAPConnectionManager sharedInstance(){
		if (_instance == null){
			_instance = new LDAPConnectionManager();
		}
		return _instance;
	}

	/**
	 * Receives an LDAP connection from pool for specified {@link LDAPServer).
	 * Server is specified via ILDAPConnectionParamsProvider content.
	 *
	 * @param paramsProvider
	 * @return connection to the {@link LDAPServer)
	 * @throws UserManagementSystemCommunicationException
	 */
	public synchronized LDAPConnection getConnection(
			ILDAPConnectionParamsProvider paramsProvider
	) throws UserManagementSystemCommunicationException {

		LDAPConnection conn = null;

		List<LDAPConnection> connections = availableConnections.get(paramsProvider);
		if (connections != null && !connections.isEmpty()){
			conn = connections.remove(0);
		}

		if (conn == null) {
			// Since the paramsProvider is kept across transactions, we must detach it now, if it is a
			// persistence-capable class and the instance is currently attached to a datastore.
			ILDAPConnectionParamsProvider detachedParamsProvider = null;
			PersistenceManager pm = JDOHelper.getPersistenceManager(paramsProvider);
			if (pm != null) {
				// To prevent a mess outside this method (we have no idea who calls this method
				// in which context), we backup and restore the previous fetch-plan.
				FetchPlanBackup fetchPlanBackup = NLJDOHelper.backupFetchPlan(pm.getFetchPlan());
				try {
					pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
					pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
					detachedParamsProvider = pm.detachCopy(paramsProvider);
				} finally {
					NLJDOHelper.restoreFetchPlan(pm.getFetchPlan(), fetchPlanBackup);
				}
			}

			if (detachedParamsProvider != null){
				conn = new LDAPConnection(detachedParamsProvider);
			}else{
				conn = new LDAPConnection(paramsProvider);
			}
		}

		if (!conn.isConnected()){
			conn.connect();
		}

		return conn;
	}

	/**
	 * Should be called in finally block after getting an {@link LDAPConnection} from pool.
	 * If you pass <code>null</code>, this method will silently return without any action.
	 *
	 * @param {@link LDAPConnection} to release or <code>null</code>.
	 */
	public synchronized void releaseConnection(LDAPConnection connection){

		if (connection == null){
			return;
		}

		ILDAPConnectionParamsProvider paramsProvider = connection.getConnectionParamsProvider();
		List<LDAPConnection> connections = availableConnections.get(paramsProvider);
		if (connections == null) {
			connections = new LinkedList<LDAPConnection>();
			availableConnections.put(paramsProvider, connections);
		}

		connections.add(connection);

		// We use a while loop AFTER adding a connection so that we allow configuration changes
		// during runtime. Even if we don't need this, it's more robust to implement it this way.
		// Marco.
		while (connections.size() > maxConnectionsPerServer){
			connections.remove(connections.size() - 1);
		}

	}

}
