package org.nightlabs.jfire.base.security.integration.ldap.connection;

import java.util.ArrayList;
import java.util.List;

import javax.naming.CommunicationException;

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
	 */
	private List<LDAPConnection> freeConnections;
	
	
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
			) throws CommunicationException {
		
		LDAPConnection conn = null;
		
		if (!freeConnections.isEmpty()){
			
			for (LDAPConnection c : freeConnections){
				if (paramsAreEqual(c.getConnectionParamsProvider(), paramsProvider)){
					conn = c;
					freeConnections.remove(c);
					break;
				}
			}
			
		}else{
			conn = new LDAPConnection(paramsProvider);
		}
		
		if (!conn.isConnected()){
			conn.connect();
		}
		
		return conn;
	}
	
	/**
	 * Should be called in finally block after getting an {@link LDAPConnection} from pool
	 * 
	 * @param {@link LDAPConnection} to release
	 */
	public synchronized void releaseConnection(LDAPConnection connection){
		if (connection != null 
				&& connectionsSize(connection.getConnectionParamsProvider()) < maxConnectionsPerServer){
			freeConnections.add(connection);
		}
	}
	
	
	private int connectionsSize(ILDAPConnectionParamsProvider paramsProvider){
		int size = 0;
		for (LDAPConnection c : freeConnections){
			if (paramsAreEqual(c.getConnectionParamsProvider(), paramsProvider)){
				size++;
			}
		}
		return size;
	}
	
	private static boolean paramsAreEqual(
			ILDAPConnectionParamsProvider p1, ILDAPConnectionParamsProvider p2
			){
		
		if (p1 != null && p2 != null
				&& p1.getHost().equals(p2.getHost())
				&& p1.getEncryptionMethod().equals(p2.getEncryptionMethod())
				&& p1.getAuthMethod().equals(p2.getAuthMethod())
				&& p1.getPort() == p2.getPort()){
			return true;
		}
		
		return false;
	}
	
}
