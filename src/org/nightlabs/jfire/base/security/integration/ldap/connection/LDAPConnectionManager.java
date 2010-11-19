package org.nightlabs.jfire.base.security.integration.ldap.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.naming.CommunicationException;

import org.nightlabs.jdo.FetchPlanBackup;
import org.nightlabs.jdo.NLJDOHelper;

/**
 * Simple LDAPConnection pool
 *
 * @author deniska
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
	 * List of free LDAP connections
	 * @deprecated It's better to use a Map! Just keeping this, because I don't want to break the code. See {@link #getConnection(ILDAPConnectionParamsProvider)}
	 */
	@Deprecated
	private List<LDAPConnection> freeConnections;

	private Map<ILDAPConnectionParamsProvider, List<LDAPConnection>> availableConnections = new HashMap<ILDAPConnectionParamsProvider, List<LDAPConnection>>();

	private LDAPConnectionManager(){
		freeConnections = new ArrayList<LDAPConnection>();
	}

	public static synchronized LDAPConnectionManager getInstance(){
		if (_instance == null){
			_instance = new LDAPConnectionManager();
		}
		return _instance;
	}

	/**
	 * Recieves an LDAP connection from pool for specified {@link LDAPServer).
	 * Server is specified via ILDAPConnectionParamsProvider content.
	 *
	 * @param paramsProvider
	 * @return connection to the {@link LDAPServer)
	 * @throws CommunicationException
	 */
	public synchronized LDAPConnection getConnection(
			ILDAPConnectionParamsProvider paramsProvider
	)
	throws CommunicationException
	{
		LDAPConnection conn = null;

		// *** REV_marco ***
		// Iterating all connections is a possibility. But why not use a HashMap
		// with the ILDAPConnectionParamsProvider as key and List<LDAPConnection> as value?
		// Of course, you should document then in ILDAPConnectionParamsProvider, that it must
		// implement equals + hashcode. Of course, you have
		// to detach the paramsProvider when putting it into the map. But you can detach
		// it with minimal fetch-groups (or even no FG at all, since only the PK is used).
//		if (!freeConnections.isEmpty()){
//
//			for (LDAPConnection c : freeConnections){
//				if (paramsAreEqual(c.getConnectionParamsProvider(), paramsProvider)){
//					conn = c;
//					freeConnections.remove(c);
//					break;
//				}
//			}
//
//		}
//		else {

		List<LDAPConnection> connections = availableConnections.get(paramsProvider);
		if (connections != null && !connections.isEmpty())
			conn = connections.remove(0);

		if (conn == null) {
			// Since the paramsProvider is kept across transactions, we must detach it now, iff it is a
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

			if (detachedParamsProvider != null)
				conn = new LDAPConnection(detachedParamsProvider);
			else
				conn = new LDAPConnection(paramsProvider);
		}

		if (!conn.isConnected()){
			conn.connect();
		}

		return conn;
	}

	// *** REV_marco ***
	// You should always document in the javadoc what constraints apply to the arguments (e.g. null allowed or not,
	// ranges, behaviour depending on args - e.g. exception, silent no-op etc.). I know that this is not the case
	// in most other places in JFire, but we want to IMPROVE JFire (and not repeat the mistakes of the past).
	// Therefore, good javadoc is important.
	// I added the info about connection being null here (because I first thought I found a potential NPE in the
	// code calling this method here).
	// It can be discussed whether a null argument here should be allowed at all. I would probably do the null-check
	// outside or put the try-finally-block at a location where it cannot happen, but this is a question of taste
	// here.
	/**
	 * Should be called in finally block after getting an {@link LDAPConnection} from pool.
	 * If you pass <code>null</code>, this method will silently return without any action.
	 *
	 * @param {@link LDAPConnection} to release or <code>null</code>.
	 */
	public synchronized void releaseConnection(LDAPConnection connection){
//		if (connection != null
//				&& connectionsSize(connection.getConnectionParamsProvider()) < maxConnectionsPerServer){
//			freeConnections.add(connection);
//		}

		if (connection == null)
			return;

		ILDAPConnectionParamsProvider paramsProvider = connection.getConnectionParamsProvider();
		List<LDAPConnection> connections = availableConnections.get(paramsProvider);
		if (connections == null) {
			connections = new LinkedList<LDAPConnection>();
			availableConnections.put(paramsProvider, connections);
		}

		connections.add(connection);

		// Instead of an if, we use a while loop AFTER adding it so that we allow configuration changes
		// during runtime. Even if we don't need this, it's more robust to implement it this way.
		// Marco.
		while (connections.size() > maxConnectionsPerServer)
			connections.remove(connections.size() - 1);

		// *** REV_marco ***
		// One final thought coming into my mind now: Are you ensuring that no broken connection is released back
		// into the pool? Are you trying to reconnect a broken connection? Did you try it?
		// Marco.
	}


//	private int connectionsSize(ILDAPConnectionParamsProvider paramsProvider){
//		int size = 0;
//		for (LDAPConnection c : freeConnections){
//			if (paramsAreEqual(c.getConnectionParamsProvider(), paramsProvider)){
//				size++;
//			}
//		}
//		return size;
//	}

//	private static boolean paramsAreEqual(
//			ILDAPConnectionParamsProvider p1, ILDAPConnectionParamsProvider p2
//			){
//
//		if (p1 != null && p2 != null
//				&& p1.getHost().equals(p2.getHost())
//				&& p1.getEncryptionMethod().equals(p2.getEncryptionMethod())
//				&& p1.getAuthMethod().equals(p2.getAuthMethod())
//				&& p1.getPort() == p2.getPort()){
//			return true;
//		}
//
//		return false;
//	}

}
