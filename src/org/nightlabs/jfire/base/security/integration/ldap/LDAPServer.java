package org.nightlabs.jfire.base.security.integration.ldap;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.security.integration.ldap.connection.Connection;
import org.nightlabs.jfire.base.security.integration.ldap.connection.IConnectionParamsProvider;
import org.nightlabs.jfire.security.integration.Session;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.UserManagementSystemType;

/**
 * Class representing LDAP-based UserManagementSystem. 
 * It also implements {@link IConnectionParamsProvider} for providing stored server parameters
 * to a {@link Connection}
 * 
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireLDAP_LDAPServer")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Queries(
		@Query(
				name=LDAPServer.GET_ACTIVE_LDAP_SERVERS_IDS, 
				value="SELECT JDOHelper.getObjectId(this) WHERE this.isActive == true"
					)
		)
public class LDAPServer extends UserManagementSystem implements IConnectionParamsProvider{

	private static final Logger logger = Logger.getLogger(LDAPServer.class);

	public static final String GET_ACTIVE_LDAP_SERVERS_IDS = "getActiveLDAPServersIDs";

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	

	/**
	 * LDAP server host
	 */
	@Persistent
	private String host;
	
	/**
	 * LDAP server port (for example default value for simple authentication is 10389)
	 */
	@Persistent
	private int port;
	
	/**
	 * Encryption method used in communication eith LDAP server
	 */
	@Persistent(defaultFetchGroup="true")
	private EncryptionMethod encryptionMethod;
	
	/**
	 * Indicates whether this LDAP server should be used for authentication
	 */
	@Persistent
	private boolean isActive = false;
	
	/**
	 * Connection to actual LDAP server
	 */
	@NotPersistent
	private Connection connection;

	
	/**
	 * {@inheritDoc}
	 * 
	 * @param name
	 * @param type
	 * @param host
	 * @param port
	 * @param encryptionMethod
	 */
	public LDAPServer(
			String name, UserManagementSystemType<LDAPServer> type, 
			String host, int port, EncryptionMethod encryptionMethod
			) {
		super(name, type);
		this.host = host;
		this.port = port;
		this.encryptionMethod = encryptionMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void logout(Session session) {
		
		try{
	        if (connection != null && connection.isConnected()){
	        	connection.unbind();
	        }
		}catch(Exception e){
			// TODO:
			e.printStackTrace();
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Session login(LoginData loginData) {

        Session session = null;

		try{
			
			if (connection == null){
				connection = new Connection(this);
			}
			if (!connection.isConnected()){
				connection.connect();
			}
	        
	        if (connection.isConnected()){
	        	try{
	        		
	        		connection.bind(
	        				"cn=ddudnik,ou=staff,ou=people,dc=nightlabs,dc=de",
	        				loginData.getPassword()
	        				);
	        		
	        		// if no exception was thrown during bind operation we assume that login was successful
	        		session = new Session(loginData);
	        		
	        	}catch(NamingException e){
	        		
	        		// not authenticated
	        		// TODO:
	        		logger.info("Authentication failed!");
	        	}
	        }
		}catch(Exception e){
			// TODO: 
			e.printStackTrace();
		}
		
		return session;
	}
	
	/**
	 * Set this <code>LDAPServer</code> active which means it will be used for authentication
	 * 
	 * @param isActive
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	/**
	 * 
	 * @return whether this <code>LDAPServer</code> is active
	 */
	public boolean isActive() {
		return isActive;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getHost() {
		return host;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPort() {
		return port;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public EncryptionMethod getEncryptionMethod() {
		return encryptionMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthenticationMethod getAuthMethod() {
		return AuthenticationMethod.SIMPLE;
	}

}
