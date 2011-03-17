package org.nightlabs.jfire.base.security.integration.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.naming.InitialContext;
import javax.script.ScriptException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.AuthCallbackHandler;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPSyncEvent.LDAPSyncEventType;
import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnection;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionManager;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionWrapper.EntryModificationFlag;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.integration.Session;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.nightlabs.jfire.security.integration.UserManagementSystemType;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.util.CollectionUtil;
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
@FetchGroups({
		@FetchGroup(
				name=LDAPServer.FETCH_GROUP_LDAP_SCRIPT_SET,
				members=@Persistent(name="ldapScriptSet")
				)
})
@Queries(
		@javax.jdo.annotations.Query(
				name=LDAPServer.FIND_LDAP_SERVERS,
				value="SELECT where this.host == :host && this.port == :port && this.encryptionMethod == :encryptionMethod ORDER BY JDOHelper.getObjectId(this) ASCENDING"
					)
		)
public class LDAPServer extends UserManagementSystem implements ILDAPConnectionParamsProvider{

	public static final String FIND_LDAP_SERVERS = "LDAPServer.findLDAPServers";
	
	public static final String FETCH_GROUP_LDAP_SCRIPT_SET = "LDAPServer.ldapScriptSet";

	private static final Logger logger = LoggerFactory.getLogger(LDAPServer.class);

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Executes {@link javax.jdo.Query) to find persistent LDAPServer instances by host, port and encryptionMethod 
	 * 
	 * @param pm
	 * @param host
	 * @param port
	 * @param encryptionMethod
	 * @return
	 */
	public static Collection<LDAPServer> findLDAPServers(
			PersistenceManager pm, String host, int port, EncryptionMethod encryptionMethod
			) {
		
		javax.jdo.Query q = pm.newNamedQuery(
				LDAPServer.class, 
				LDAPServer.FIND_LDAP_SERVERS
				);
		@SuppressWarnings("unchecked")
		List<LDAPServer> foundServers = (List<LDAPServer>) q.execute(host, port, encryptionMethod);
		
		// We copy them into a new ArrayList in order to be able to already close the query (save resources).
		// That would only be a bad idea, if we had really a lot of them and we would not need to iterate all afterwards.
		// But as we need to iterate most of them anyway, we can fetch the whole result set already here.
		// Note that this has only a positive effect in long-running transactions (because the query will be closed at the end of the
		// transaction, anyway). However, it has no negative effect besides the one already mentioned and we don't know in
		// which contexts this method might be used => better close the query quickly.
		// Marco.
		foundServers = new ArrayList<LDAPServer>(foundServers);
		q.closeAll();
		return foundServers;
	}

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
	 * DN used for binding agains LDAP server during synchronization process
	 */
	@Persistent
	private String syncDN;

	/**
	 * Password for DN used for binding agains LDAP server during synchronization process
	 */
	@Persistent
	private String syncPassword;
	
	/**
	 * Set of scripts which perform specific synchronization tasks
	 */
	@Persistent
	private LDAPScriptSet ldapScriptSet;
	

	public LDAPServer(UserManagementSystemID userManagementSystemID, UserManagementSystemType<?> type){
		super(userManagementSystemID, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void logout(Session session) throws UserManagementSystemCommunicationException {

		LDAPConnection connection = null;
		try{

			if (logger.isDebugEnabled()){
	        	logger.debug("Loggin out from session, id: " + session.getSessionID());
	        }

			connection = createConnection(this);
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
	 */
	@Override
	public Session login(LoginData loginData) throws LoginException, UserManagementSystemCommunicationException{

        Session session = null;
        LDAPConnection connection = null;

        String loginDataOrganisationID = loginData.getOrganisationID();
		String loginDataUserID = loginData.getUserID();

        try{

			if (logger.isDebugEnabled()){
        		logger.debug(
        				"Try to recieve an LDAPConnection and login for " +
        				loginDataUserID + LoginData.USER_ORGANISATION_SEPARATOR + loginDataOrganisationID
        				);
        	}

			connection = createConnection(this);
			
    		PersistenceManager pm = JDOHelper.getPersistenceManager(this);

    		// getting LDAP DN for the loggin in user
    		User user = getUserById(pm, loginDataOrganisationID, loginDataUserID);
    		String userDN = null;
    		if (user != null){
    			userDN = getLDAPUserDN(user);
    			
    			if (logger.isDebugEnabled()){
		        	logger.debug(String.format("User %s@%s found in JFire, result DN is %s", loginDataUserID, loginDataOrganisationID, userDN));
		        }
    		}else if (user == null && shouldFetchUserFromLDAP(loginData)){
    			if (logger.isDebugEnabled()){
		        	logger.debug(String.format("User %s@%s was not found JFire, will fetch it from LDAP", loginDataUserID, loginDataOrganisationID));
		        }
        		
    			// create new fake user to get userDN
		        userDN = getLDAPUserDN(new User(loginData.getOrganisationID(), loginData.getUserID()));
        	}
        	
        	if (userDN == null){
        		ILDAPConnectionParamsProvider params = connection.getConnectionParamsProvider();
        		throw new LoginException(
        				String.format(
        						"Can't bind against LDAPServer at %s because userDN is null for loginData %s@%s!",
        						params.getHost(), loginDataUserID, loginDataOrganisationID
        						)
        				);
        	}
        	
	        connection.bind(
					userDN,
					loginData.getPassword()
					);

			// if no exception was thrown during bind operation we assume that login was successful
			session = new Session(loginData);
	        if (logger.isDebugEnabled()){
	        	logger.debug("Bind successful. Session id: " + session.getSessionID());
	        }

			if (user == null && shouldFetchUserFromLDAP(loginData)){
   				try {
   					if (logger.isDebugEnabled()){
   			        	logger.debug(String.format("User %s@%s was not found JFire, start fetching it from LDAP", loginDataUserID, loginDataOrganisationID));
   			        }
   	    			
					updateJFireData(CollectionUtil.createHashSet(userDN), loginDataOrganisationID);
				} catch (LDAPSyncException e) {
					logger.error(String.format("Exception while fetching user %s@%s from LDAP", loginDataUserID, loginDataOrganisationID), e);
					throw new LoginException(String.format("Can't fetch user %s@%s at login! See log for details.", loginDataUserID, loginDataOrganisationID));
				}
			}

			return session;

		}finally{
			unbindAndReleaseConnection(connection);
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
	 * Set server host
	 * @param host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPort() {
		return port;
	}
	
	/**
	 * Set server port
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EncryptionMethod getEncryptionMethod() {
		return encryptionMethod;
	}
	
	/**
	 * Set server encryption method
	 * @param encryptionMethod
	 */
	public void setEncryptionMethod(EncryptionMethod encryptionMethod) {
		this.encryptionMethod = encryptionMethod;
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
	
	/**
	 * Get password for syncronization
	 * 
	 * @return
	 */
	public String getSyncPassword() {
		return syncPassword;
	}
	
	/**
	 * Set LDAPScriptSet to this LDAP Server
	 * 
	 * @param ldapScriptSet can't be null 
	 */
	public void setLdapScriptSet(LDAPScriptSet ldapScriptSet) {
		if (ldapScriptSet == null){
			return;
		}
		this.ldapScriptSet = ldapScriptSet;
	}
	
	/**
	 * 
	 * @return LDAPSCriptSet of this LDAPServer
	 */
	public LDAPScriptSet getLdapScriptSet() {
		return ldapScriptSet;
	}
	

	private User getUserById(PersistenceManager pm, String organisationId, String userId){
		if (pm == null){
			logger.error(
					String.format("Can't load for user %s@%s because PersistenceManager is null!", userId, organisationId)
					);
			return null;
		}
		try{
			return User.getUser(pm, organisationId, userId);
		}catch(JDOObjectNotFoundException e){
			// there's no such User in JFire
			if (logger.isDebugEnabled()){
				logger.debug(
						String.format("User not found: %s@%s", userId, organisationId)
						);
			}
		}
		return null;
	}
	
	/**
	 * Gets LDAP DN for given object usign {@link LDAPScripSet} of this LDAPServer instance
	 * @param jfireObject
	 * @return
	 * @throws LoginException
	 */
	private String getLDAPUserDN(Object jfireObject) throws LoginException{
		try {
			return ldapScriptSet.getLdapDN(jfireObject);
		} catch (ScriptException e) {
			logger.error("Exception executing LDAPScript", e);
			throw new LoginException("Exception in LDAPScriptSet, see log for details.");
		}
	}
	
	/**
	 * Gets {@link LDAPConnection} via {@link LDAPConnectionManager}
	 * 
	 * @param server
	 * @return
	 * @throws UserManagementSystemCommunicationException
	 */
	private LDAPConnection createConnection(LDAPServer server) throws UserManagementSystemCommunicationException{
    	LDAPConnection connection = LDAPConnectionManager.sharedInstance().getConnection(this);
    	if (logger.isDebugEnabled()){
    		ILDAPConnectionParamsProvider params = connection.getConnectionParamsProvider();
    		logger.debug(
    				"LDAPConnection recieved. Trying to bind against LDAPServer at " +
    				params.getHost() + ":" + params.getPort() +
    				" Encryption: " + params.getEncryptionMethod().stringValue() +
    				" Auth method: " + params.getAuthMethod().stringValue()
    				);
    	}

    	return connection;
	}
	
	private void unbindAndReleaseConnection(LDAPConnection connection) throws UserManagementSystemCommunicationException{
		
		if (connection == null){
			return;
		}
		
    	if (logger.isDebugEnabled()){
    		ILDAPConnectionParamsProvider params = connection.getConnectionParamsProvider();
    		logger.debug(
    				"Unbinding and releasing connection for LDAP server at" +
    				params.getHost() + ":" + params.getPort() +
    				" Encryption: " + params.getEncryptionMethod().stringValue() +
    				" Auth method: " + params.getAuthMethod().stringValue()
    				);
    	}
		
    	connection.unbind();
		LDAPConnectionManager.sharedInstance().releaseConnection(connection);
    	
		if (logger.isDebugEnabled()){
    		logger.debug("Connection released");
    	}
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
	 * And of course we could create a general SyncEvent in this case. 
	 * I think taht this option needs to be considered. Denis. 
	 * 
	 * @param syncEvent
	 * @throws UserManagementSystemCommunicationException 
	 * @throws LoginException 
	 * @throws LDAPSyncException
	 */
	public void synchronize(LDAPSyncEvent syncEvent) throws LDAPSyncException, LoginException, UserManagementSystemCommunicationException{
		
		if (LDAPSyncEventType.FETCH == syncEvent.getEventType()){
			
			updateJFireData(syncEvent.getLdapUsersIds(), syncEvent.getOrganisationID());
			
		}else if (LDAPSyncEventType.SEND == syncEvent.getEventType()){
			
			updateLDAPUsers(syncEvent.getJFireObjectsIds());
			
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

		return fetchUserFromLDAP;
	}
	
	/**
	 * Performs synchronization from LDAP directory to JFire
	 * 
	 * @param ldapEntriesDNs
	 * @param organisationID
	 * @throws UserManagementSystemCommunicationException
	 * @throws LoginException
	 * @throws LDAPSyncException
	 */
	private void updateJFireData(Collection<String> ldapEntriesDNs, String organisationID) throws UserManagementSystemCommunicationException, LoginException, LDAPSyncException{
		
		LDAPConnection connection = null;
		LoginContext loginContext = null;
		JFireServerManager jsm = null;
		try{

			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			
			// We need to be logged into JFire to be able to persist objects, inflate property sets, generate IDs ...
			try{
				// check if there's some User logged in
				GlobalSecurityReflector.sharedInstance().getUserDescriptor();
			}catch(NoUserException e){
				// login with _System_ user
				try{
					
					if (logger.isDebugEnabled()){
						logger.debug("No user logged in, trying to log in with _System_ user...");
					}
					
					InitialContext initialContext = new InitialContext();
					J2EEAdapter j2eeAdapter = (J2EEAdapter) initialContext.lookup(J2EEAdapter.JNDI_NAME);
					JFireServerManagerFactory jsmf = (JFireServerManagerFactory) initialContext.lookup(JFireServerManagerFactory.JNDI_NAME);
					jsm = jsmf.getJFireServerManager();
					loginContext = j2eeAdapter.createLoginContext(
							LoginData.DEFAULT_SECURITY_PROTOCOL, 
							createAuthCallbackHandler(
									jsm, User.getUser(pm, organisationID, User.USER_ID_SYSTEM)
									)
							);
					loginContext.login();

					if (logger.isDebugEnabled()){
						logger.debug("_System_ user logged in");
					}
					
				}catch(Exception ne){
					throw new LDAPSyncException("Can't sync from LDAP to JFire because there's no logged in User.", ne);
				}
			}

			connection = createConnection(this);

			// We need to be logged in against LDAPServer for modification calls.
			bindForSync(connection, pm);
			
			for (String ldapEntryDN : ldapEntriesDNs){
				try{
					
					if (logger.isDebugEnabled()){
						logger.debug("Trying synchronization for DN: " + ldapEntryDN);
					}
					
					ldapScriptSet.syncLDAPDataToJFireObjects(
								connection.getAttributesForEntry(ldapEntryDN), organisationID
							);

					if (logger.isDebugEnabled()){
						logger.debug("Data synchronized for DN: " + ldapEntryDN);
					}

				}catch(Exception e){
					throw new LDAPSyncException("Exception occured while synchronizing entry with DN " + ldapEntryDN, e);
				}
			}
			
		}finally{
			if (loginContext != null && loginContext.getSubject() != null){
				if (logger.isDebugEnabled()){
					logger.debug("_System_ user was logged in, loggin it out...");
				}
				loginContext.logout();
			}
			if (jsm != null){
				jsm.close();
			}
			unbindAndReleaseConnection(connection);
		}
		
	}
	
	/**
	 * Performs synchronization from JFire to LDAP directory
	 * 
	 * @param jfireObjectsIds
	 * @throws UserManagementSystemCommunicationException
	 * @throws LoginException
	 * @throws LDAPSyncException
	 */
	private void updateLDAPUsers(Collection<Object> jfireObjectsIds) throws UserManagementSystemCommunicationException, LoginException, LDAPSyncException{
		
		LDAPConnection connection = null;
		try{

			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			connection = createConnection(this);

			// We need to be logged in against LDAPServer for modification calls.
			bindForSync(connection, pm);

			for (Object jfireObjectId : jfireObjectsIds){
				try{
					
					if (logger.isDebugEnabled()){
						logger.debug("Trying synchronization for object " + jfireObjectId.toString());
					}
				
					Object jfireObject = pm.getObjectById(jfireObjectId);
					String userDN = getLDAPUserDN(jfireObject);

					if (logger.isDebugEnabled()){
						logger.debug("Preparing attributes for modifying entry with DN: " + userDN);
					}
					Map<String, Object[]> modifyAttributes = ldapScriptSet.getAttributesMapForLDAP(jfireObject);

					Map<String, Object[]> foundAttributes = connection.getAttributesForEntry(userDN);
					if (foundAttributes != null && foundAttributes.size() > 0){	// user exists in LDAP

						if (logger.isDebugEnabled()){
							logger.debug("Entry exists, modifying attributes. DN: " + userDN);
						}

						if(modifyAttributes != null && modifyAttributes.size() > 0){
							connection.modifyEntry(userDN, modifyAttributes, EntryModificationFlag.MODIFY);
						}
					}else{	// user doesn't exist, create new entry 

						if (logger.isDebugEnabled()){
							logger.debug("Entry doesn't exist, creting new entry with DN: " + userDN);
						}

						connection.createEntry(userDN, modifyAttributes);
					}

					if (logger.isDebugEnabled()){
						logger.debug("Data synchronized for object " + jfireObjectId.toString());
					}
				}catch(Exception e){
					throw new LDAPSyncException("Exception occured while synchronizing object with id " + jfireObjectId.toString(), e);
				}
				
			}
			
		}finally{
			unbindAndReleaseConnection(connection);
		}
	}
	
	private void bindForSync(LDAPConnection  connection, PersistenceManager pm) throws LoginException, LDAPSyncException, UserManagementSystemCommunicationException{
		
		String syncDN = this.syncDN;
		String syncPassword = this.syncPassword;
		if (syncDN == null || "".equals(syncDN)){	// try to bind with currently logged in user
	    	
			User user = GlobalSecurityReflector.sharedInstance().getUserDescriptor().getUser(pm);
			syncDN = getLDAPUserDN(user);
        	
        	if (syncDN == null){
        		ILDAPConnectionParamsProvider params = connection.getConnectionParamsProvider();
        		throw new LDAPSyncException(
        				String.format(
        						"Can't bind against LDAPServer at %s for synchronization because userDN is null for current logged in user %s@%s!",
        						params.getHost(), user!=null?user.getUserID():"null", user!=null?user.getOrganisationID():"null"
        						)
        				);
        	}
			
    		// TODO: Where should I get a password? what to do if passwords are not stored in JFire anymore? Denis.
    		syncPassword = "root"; 
        	
		}

		connection.bind(syncDN, syncPassword);
		
	}

	private AuthCallbackHandler createAuthCallbackHandler(JFireServerManager ism, User user) throws Exception {
		return new AuthCallbackHandler(ism,
				user.getOrganisationID(),
				user.getUserID(),
				ObjectIDUtil.makeValidIDString(null, true));
	}

	/***********************************
	 * Synchronization section END *
	 ***********************************/
	
}
