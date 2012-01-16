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
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
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
	 * Free LDAP connections
	 */
	private ConnectionsHolder availableConnections;
	
	/**
	 * Map of private (authenticated) LDAP connections which may be used by the same Users with only.
	 * The {@link LDAPConnectionManager} DOES NOT create connections directly for this map neither it adds them here. 
	 * These operations are performed externally. Typical usage cycle would be: 
	 * 	1) {@link User} gained a connection from {@link LDAPConnectionManager#getConnection(ILDAPConnectionParamsProvider)}
	 *  2) performed a "bind" opeartion on it so it became an authenticated one
	 *  3) put it back by {@link LDAPConnectionManager#preservePrivateLDAPConnection(LDAPConnection)} in order to preserve it for later usage
	 * 	4) got it somewhere by {@link LDAPConnectionManager#getPrivateLDAPConnection()} when authenticated conection is needed
	 *  	but there's no possibility to get one (i.e. in Async invocaions), performed all operations needed
	 *  5) "unbind" this {@link LDAPConnection} and release it back to {@link LDAPConnectionManager}  
	 * 
	 * One of the use cases where this kind of connections are used is described here: 
	 * https://www.jfire.org/modules/bugs/view.php?id=1974
	 */
	private Map<UserID, ConnectionsHolder> privateConnections;
	

	private LDAPConnectionManager(){
		availableConnections = new ConnectionsHolder();
		privateConnections = new HashMap<UserID, ConnectionsHolder>();
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

		LDAPConnection conn = availableConnections.getConnection(paramsProvider);
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
		
		// if released connection is still a preserved one we remove it from private connections map
		for (UserID key : privateConnections.keySet()){
			ConnectionsHolder holder = privateConnections.get(key);
			if (holder != null){
				holder.removePrivateConnection(connection);
				break;
			}
		}

		availableConnections.putConnection(connection.getConnectionParamsProvider(), connection);
	}

	/**
	 * Preserve some {@link LDAPConnection} for the later usage by the same {@link User}.
	 * If another {@link LDAPConnection} was already preserved by this {@link User} it will
	 * be unbinded and released back to the {@link LDAPConnectionManager}.
	 * 
	 * Be careful when using this API and try to make sure that preserved connection will be
	 * released by your code at some point even if some unexpected situations will occur. 
	 * Of course it is not always possible so usage of this API should be strongly considered.
	 * It was introduced to cover the use case described here (https://www.jfire.org/modules/bugs/view.php?id=1974)
	 * so please use it for similar use cases. 
	 *
	 * @param paramsProvider Params provider of a connection
	 * @param key Key to preserve given connection under
	 * @param connection The {@link LDAPConnection} to be preserved
	 * @throws NoUserException If no {@link User} is logged in 
	 */
	public synchronized void preservePrivateLDAPConnection(ILDAPConnectionParamsProvider paramsProvider, String key, LDAPConnection connection) throws NoUserException{
		UserID userID = GlobalSecurityReflector.sharedInstance().getUserDescriptor().getUserObjectID();
		
		ConnectionsHolder connectionsHolder = privateConnections.get(userID);
		if (connectionsHolder == null){
			connectionsHolder = new ConnectionsHolder();
			privateConnections.put(userID, connectionsHolder);
		}
		
		LDAPConnection prevConnection = connectionsHolder.getPrivateConnection(paramsProvider, key);
		if (prevConnection != null){
			try {
				prevConnection.unbind();
			} catch (UserManagementSystemCommunicationException e) {
				// connection is not alive, but we do not need to do anything about it
				// because it will be connected when asked again from the pool
			}
			releaseConnection(prevConnection);
		}
		
		connectionsHolder.putPrivateConnection(paramsProvider, key, connection);
	}

	/**
	 * Preserve some {@link LDAPConnection} for the later usage by the same {@link User}.
	 * If another {@link LDAPConnection} was already preserved by this {@link User} it will
	 * be unbinded and released back to the {@link LDAPConnectionManager}.
	 * As private {@link LDAPConnection}s are keeped under certain keys, default one will be used by this method.
	 * 
	 * Be careful when using this API and try to make sure that preserved connection will be
	 * released by your code at some point even if some unexpected situations will occur. 
	 * Of course it is not always possible so usage of this API should be strongly considered.
	 * It was introduced to cover the use case described here (https://www.jfire.org/modules/bugs/view.php?id=1974)
	 * so please use it for similar use cases. 
	 *
	 * @param paramsProvider Params provider of a connection
	 * @param connection The {@link LDAPConnection} to be preserved
	 * @throws NoUserException If no {@link User} is logged in 
	 */
	public synchronized void preservePrivateLDAPConnection(ILDAPConnectionParamsProvider paramsProvider, LDAPConnection connection) throws NoUserException{
		preservePrivateLDAPConnection(paramsProvider, ConnectionsHolder.DEFAULT_KEY, connection);
	}

	/**
	 * Get {@link LDAPConnection} which was preserved by this {@link User} earlier. 
	 * Returned {@link LDAPConnection} might be not alive and this method will NOT try to reconnect it 
	 * in a way that {@link LDAPConnectionManager#getConnection(ILDAPConnectionParamsProvider)} does.
	 * 
	 * @param paramsProvider Params provider or a preserved connection
	 * @param key Key to retrieve preserved {@link LDAPConnection}
	 * @return {@link LDAPConnection} that was preserved by this {@link User} earlier
	 * @throws NoUserException If no {@link User} is logged in 
	 */
	public LDAPConnection getPrivateLDAPConnection(ILDAPConnectionParamsProvider paramsProvider, String key) throws NoUserException{
		UserID userID = GlobalSecurityReflector.sharedInstance().getUserDescriptor().getUserObjectID();
		ConnectionsHolder connectionsHolder = privateConnections.get(userID);
		if (connectionsHolder != null){
			return connectionsHolder.getPrivateConnection(paramsProvider, key);
		}
		return null;
	}

	/**
	 * Get {@link LDAPConnection} which was preserved by this {@link User} earlier. 
	 * Returned {@link LDAPConnection} might be not alive and this method will NOT try to reconnect it 
	 * in a way that {@link LDAPConnectionManager#getConnection(ILDAPConnectionParamsProvider)} does.
	 * As private {@link LDAPConnection}s are keeped under certain keys, default one will be used by this method.
	 * 
	 * @param paramsProvider params provider or a preserved connection
	 * @return {@link LDAPConnection} that was preserved by this {@link User} earlier
	 * @throws NoUserException If no {@link User} is logged in 
	 */
	public LDAPConnection getPrivateLDAPConnection(ILDAPConnectionParamsProvider paramsProvider) throws NoUserException{
		return getPrivateLDAPConnection(paramsProvider, ConnectionsHolder.DEFAULT_KEY);
	}
	
	class ConnectionsHolder{
		private static final String DEFAULT_KEY = "default";
		private Map<ILDAPConnectionParamsProvider, Map<String, LDAPConnection>> privateConnectionsMap;
		private Map<ILDAPConnectionParamsProvider, List<LDAPConnection>> connectionsMap;
		public ConnectionsHolder(){
			connectionsMap = new HashMap<ILDAPConnectionParamsProvider, List<LDAPConnection>>();
			privateConnectionsMap = new HashMap<ILDAPConnectionParamsProvider, Map<String,LDAPConnection>>();
		}
		public void putConnection(ILDAPConnectionParamsProvider paramsProvider, LDAPConnection connection) {
			List<LDAPConnection> connections = connectionsMap.get(paramsProvider);
			if (connections == null){
				connections = new LinkedList<LDAPConnection>();
				connectionsMap.put(paramsProvider, connections);
			}
			connections.add(connection);

			// We use a while loop AFTER adding a connection so that we allow configuration changes
			// during runtime. Even if we don't need this, it's more robust to implement it this way.
			// Marco.
			while (connections.size() > maxConnectionsPerServer){
				connections.remove(connections.size() - 1);
			}
		}
		public void putPrivateConnection(ILDAPConnectionParamsProvider paramsProvider, String key, LDAPConnection connection){
			Map<String, LDAPConnection> connections = privateConnectionsMap.get(paramsProvider);
			if (connections == null){
				connections = new HashMap<String, LDAPConnection>();
				privateConnectionsMap.put(paramsProvider, connections);
			}
			connections.put(key, connection);
		}
		public LDAPConnection getConnection(ILDAPConnectionParamsProvider paramsProvider) {
			List<LDAPConnection> connections = connectionsMap.get(paramsProvider);
			if (connections != null && !connections.isEmpty()){
				return connections.remove(0);
			}
			return null;
		}
		public LDAPConnection getPrivateConnection(ILDAPConnectionParamsProvider paramsProvider, String key){
			Map<String, LDAPConnection> connections = privateConnectionsMap.get(paramsProvider);
			if (connections != null && connections.containsKey(key)){
				return connections.remove(key);
			}
			return null;
		}
		public void removePrivateConnection(LDAPConnection connection){
			if (connection == null){
				return;
			}
			Map<String, LDAPConnection> connections = privateConnectionsMap.get(connection.getConnectionParamsProvider());
			if (connections != null){
				String keyToRemove = null;
				for (String key : connections.keySet()){
					if (connection.equals(connections.get(key))){
						keyToRemove = key;
						break;
					}
				}
				if (keyToRemove != null){
					connections.remove(keyToRemove);
				}
			}
		}
	}
}
