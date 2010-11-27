package org.nightlabs.jfire.base.security.integration.ldap;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.naming.CommunicationException;
import javax.security.auth.login.LoginException;

import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnection;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionManager;
import org.nightlabs.jfire.security.integration.Session;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.nightlabs.jfire.security.integration.UserManagementSystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing LDAP-based UserManagementSystem. 
 * It also implements {@link ILDAPConnectionParamsProvider} for providing stored server parameters
 * to a {@link LDAPConnection}
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
public class LDAPServer extends UserManagementSystem implements ILDAPConnectionParamsProvider{

	private static final Logger logger = LoggerFactory.getLogger(LDAPServer.class);

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
	 * @throws CommunicationException 
	 */
	@Override
	public void logout(Session session) throws UserManagementSystemCommunicationException {
		
		LDAPConnection connection = null;
		try{

			if (logger.isDebugEnabled()){
	        	logger.debug("Loggin out from session, id: " + session.getSessionID());
	        }

			connection = LDAPConnectionManager.getInstance().getConnection(this);
        	connection.unbind();
        	
			if (logger.isDebugEnabled()){
	        	logger.debug("Logged out from session, id: " + session.getSessionID());
	        }
        	
		}finally{
			LDAPConnectionManager.getInstance().releaseConnection(connection);
		}
		
	}

	/**
	 * {@inheritDoc}
	 * @throws CommunicationException 
	 */
	@Override
	public Session login(LoginData loginData) throws LoginException, UserManagementSystemCommunicationException{

        Session session = null;
        LDAPConnection connection = null;

        try{

        	if (logger.isDebugEnabled()){
        		logger.debug(
        				"Try to recieve an LDAPConnection and login for " + 
        				loginData.getUserID() + LoginData.USER_ORGANISATION_SEPARATOR + loginData.getOrganisationID()
        				);
        	}
        	
        	connection = LDAPConnectionManager.getInstance().getConnection(this);

        	if (logger.isDebugEnabled()){
        		ILDAPConnectionParamsProvider params = connection.getConnectionParamsProvider();
        		logger.debug(
        				"LDAPConnection recieved. Trying to bind against LDAPServer at " + 
        				params.getHost() + ":" + params.getPort() + 
        				" Encryption: " + params.getEncryptionMethod().stringValue() +
        				" Auth method: " + params.getAuthMethod().stringValue()
        				);
        	}

	        connection.bind(
					"cn=ddudnik,ou=staff,ou=people,dc=nightlabs,dc=de",
					loginData.getPassword()
					);
			
			// if no exception was thrown during bind operation we assume that login was successful
			session = new Session(loginData);
	        
	        if (logger.isDebugEnabled()){
	        	logger.debug("Bind successful. Session id: " + session.getSessionID());
	        }
			
		}finally{
			LDAPConnectionManager.getInstance().releaseConnection(connection);
		}
		
		return session;
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
