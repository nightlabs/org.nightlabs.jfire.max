package org.nightlabs.jfire.base.security.integration.ldap;

import java.util.HashMap;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.naming.CommunicationException;
import javax.security.auth.login.LoginException;

import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPSyncEvent.LDAPSyncEventType;
import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnection;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionManager;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionWrapper.EntryModificationFlag;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.integration.Session;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.nightlabs.jfire.security.integration.UserManagementSystemType;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing LDAP-based UserManagementSystem.
 * It also implements {@link ILDAPConnectionParamsProvider} for providing stored server parameters
 * to a {@link LDAPConnection}
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

	// TODO: test DN before there's no LDAPScriptSet, should be removed
	private static final String TEST_DN = "cn=ddudnik,ou=staff,ou=people,dc=nightlabs,dc=de";

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
	
	@Persistent
	private String syncDN = TEST_DN;

	@Persistent
	private String syncPassword = "1111";
	

	public LDAPServer(UserManagementSystemID userManagementSystemID, UserManagementSystemType<?> type){
		super(userManagementSystemID, type);
	}

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

			connection = LDAPConnectionManager.sharedInstance().getConnection(this);
        	connection.unbind();

			if (logger.isDebugEnabled()){
	        	logger.debug("Logged out from session, id: " + session.getSessionID());
	        }

		}finally{
			LDAPConnectionManager.sharedInstance().releaseConnection(connection);
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

        	connection = LDAPConnectionManager.sharedInstance().getConnection(this);

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
					TEST_DN,
					loginData.getPassword()
					);

			// if no exception was thrown during bind operation we assume that login was successful
			session = new Session(loginData);

	        if (logger.isDebugEnabled()){
	        	logger.debug("Bind successful. Session id: " + session.getSessionID());
	        }

	        // It could be not the best choice to keep this code here, but for now it seems as 
	        // an optimal decision for me. I think that having a login or bind listener somewhere 
	        // could be a better one. But I didn' want to plug in JFSM login() since JFSM
	        // is considered deprecated. Also didn't want to pollute LDAPServer (or UserManagementSystem)
	        // code with these listeners processing. But we can cosider it if we really need 
	        // to recieve login/bind events somewhere else. Denis.
	        if (shouldFetchUserFromLDAP(session.getLoginData())){
				fetchUserFromLDAP(session.getLoginData());
	        }

			return session;

		}finally{
			connection.unbind();
			LDAPConnectionManager.sharedInstance().releaseConnection(connection);
		}

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

	/**
	 * Set DN used for synchronization
	 * 
	 * @param syncDN
	 */
	public void setSyncDN(String syncDN) {
		this.syncDN = syncDN;
	}
	
	/**
	 * Set password used for synchronization
	 * @param syncPassword
	 */
	public void setSyncPassword(String syncPassword) {
		this.syncPassword = syncPassword;
	}
	
	/**
	 * Get DN used for synchronization
	 * 
	 * @return
	 */
	public String getSyncDN() {
		return syncDN;
	}
	
	/***********************************
	 * Synchronization section START *
	 ***********************************/
	
	/**
	 * Starts synchronization process. It's driven by {@link LDAPSyncEvent} objects
	 * which are telling what to do: send data to LDAP server or recieve it and store
	 * in JFire. These events also contain pointers for the data to be synchronized 
	 * (i.e. a complete userId for a User object).
	 * 
	 * TODO: This API could be generalized and be a part of {@link UserManagementSystem}.
	 * And we can create a general SyncEvent of course in this case. 
	 * This option needs to be considered. Denis. 
	 * 
	 * @param syncEvent
	 * @throws Exception
	 */
	public void synchronize(LDAPSyncEvent syncEvent) throws Exception{
		
		String completeUserId = syncEvent.getCompleteUserId();
		
		if (LDAPSyncEventType.FETCH == syncEvent.getType()){
			
			updateJFireUser(completeUserId);
			
		}else if (LDAPSyncEventType.SEND == syncEvent.getType()){
			
			updateLDAPUser(completeUserId);
			
		}else{
			throw new UnsupportedOperationException("Unknown LDAPSyncEventType!");
		}
		
	}
	
	private boolean shouldFetchUserFromLDAP(LoginData loginData){

		// determine who is the leading system, LDAP or JFire
		boolean ldapAsLeadingSystem = true;	// FIXME: read property value from configuration
											// Where could be the most appropriate place in JFire 
											// for holding this kind of configuration? Denis. 

		// we can fetch user data from LDAP in both scenarios:
		// - when JFire is a leading system we're doing it because it might help in certain situations 
		//   (e.g. when you want to use JFire as leading system, but initially have some users in the 
		//   LDAP which you want to import)
		// - when LDAP is a leading system it's done when user still does not exist in JFire
		boolean fetchUserFromLDAP;
		if (ldapAsLeadingSystem){
			fetchUserFromLDAP = true;
		}else{
			fetchUserFromLDAP = true;	// FIXME: get property value from some configuration
										// Where could be the most appropriate place in JFire 
										// for holding this kind of configuration? Denis.
		}

		if (fetchUserFromLDAP){
			
			// TODO: what if call it on detached instance and there would be no PersistenceManager? Denis.
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			if (pm != null){
				User user = null;
				try{
					user = User.getUser(pm, loginData.getOrganisationID(), loginData.getUserID());
				}catch(JDOObjectNotFoundException e){
					// there's no such User in JFire
				}
				if (user == null){
					return true;
				}
			}
			
		}
		
		return false;
	}
	
	private void fetchUserFromLDAP(LoginData loginData) throws UserManagementSystemCommunicationException{
		
		LDAPSyncEvent syncEvent = new LDAPSyncEvent(
				LDAPSyncEventType.FETCH, loginData.getUserID()
				);
		try {
			synchronize(syncEvent);
		} catch (Exception e) {
			throw new UserManagementSystemCommunicationException("Synchronization failed!", e);
		}

	}
	
	private void updateJFireUser(String userId){
		logger.info("Fetch user");
	}

	private void updateLDAPUser(String userId) throws UserManagementSystemCommunicationException, LoginException{
		
		LDAPConnection connection = null;
		try{

			connection = LDAPConnectionManager.sharedInstance().getConnection(this);
			
			// We need to be logged in against LDAPServer for modification calls.
			String syncDN = this.syncDN;
			String syncPassword = this.syncPassword;
			if (syncDN == null || "".equals(syncDN)){
				syncDN = TEST_DN;	// TODO: get current logged in user DN via LDAPScriptSet
			}
			if (syncPassword == null){
				syncPassword = "1111";	// TODO: get current logged in user's password
			}
			connection.bind(syncDN, syncPassword);
			
			
			// collect attributes to be stored in LDAP entry (new or existing one)
			// TODO: will be done via LDAPScriptSet
			HashMap<String, Object> modifyAttributes = new HashMap<String, Object>();
			modifyAttributes.put("sn", "Denis Dudnik changed his name!");
			modifyAttributes.put("mail", new String[]{"deniska.dudnik@gmail.com", "deniska@instinctools.ru"});
			
			
			// check if user alreadt exist in LDAP
			String userDN = TEST_DN; // TODO: will be done via LDAPScriptSet
			
			HashMap<String, Object> foundAttributes = connection.getAttributesForEntry(userDN);
			if (foundAttributes != null && foundAttributes.size() > 0){	// user exists
				
				if(modifyAttributes != null && modifyAttributes.size() > 0){
					connection.modifyEntry(userDN, modifyAttributes, EntryModificationFlag.MODIFY);
				}
				
			}else{	// user doesn't exist, create new entry 
				
				connection.createEntry(userDN, modifyAttributes);
				
			}

		}finally{
			connection.unbind();
			LDAPConnectionManager.sharedInstance().releaseConnection(connection);
		}
		
	}

	/***********************************
	 * Synchronization section END *
	 ***********************************/
	
}
