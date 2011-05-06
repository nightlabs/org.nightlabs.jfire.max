package org.nightlabs.jfire.base.security.integration.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.nightlabs.jfire.base.security.integration.ldap.LDAPManagerBean.SyncStoreLifecycleListener;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPSyncEvent.LDAPSyncEventType;
import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttributeSet;
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
import org.nightlabs.jfire.security.integration.UserManagementSystemManagerBean.ForbidUserCreationLyfecycleListener;
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

	public static final int LDAP_DEFAULT_PORT = 10389;
	public static final String LDAP_DEFAULT_HOST = "localhost";
	public static final EncryptionMethod LDAP_DEFAULT_ENCRYPTION_METHOD = EncryptionMethod.NONE;

	public static final String SYNC_USER_DATA_FROM_LDAP_TASK_ID = "JFireLDAP-syncUserDataFromLDAP";
	public static final String FETCH_GROUP_LDAP_SCRIPT_SET = "LDAPServer.ldapScriptSet";

	private static final String FIND_LDAP_SERVERS = "LDAPServer.findLDAPServers";
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
	 * Encryption method used in communication with LDAP server
	 */
	@Persistent(defaultFetchGroup="true")
	private EncryptionMethod encryptionMethod;
	
	/**
	 * Authentication method used when binding against LDAP server
	 * IMPORTANT! For now only SIMPLE method is supported or NONE for anonymous access.
	 */
	@Persistent(defaultFetchGroup="true")
	private AuthenticationMethod authenticationMethod = AuthenticationMethod.SIMPLE;
	
	/**
	 * DN used for binding agains LDAP server during synchronization process
	 */
	@Persistent
	private String syncDN;

	/**
	 * Password for DN used for binding agains LDAP server during synchronization process.
	 * REV: (Alex) It should be possible to sync only when a user authenticates and thus leave this field empty (make it optional, as it is only essentially required for timed synchronisations).
	 * 
	 * REV: (Denis) If I understood correctly sync scenario for JFire as leading system (not involving any timers) should look like this:
	 * 	- some user was authenticated in JFire (and was bind against LDAP during login)
	 *	- we assume that no additional binding against LDAP needed for synchronization
	 *	- run sync process under this authenticated user
	 *
	 * In this case there are couple of problems:
	 * 1) Current user could not have enough permissions for modifying LDAP directory. It should be checked and if this happens we need to bind against LDAP with syncDN/syncPassword so they must not be empty.
	 * 2) Even if first problem is not encountered there's another issue. Every time we obtain a LDAPConnection from LDAPCOnnectionManager we get a clean connection which doesn't contain any credentials.
	 *    It happens because we always unbind LDAPConnection (which means removing any auth related data) before releasing it back to LDAPConnectionManager in order not to have security violation if someone would
	 *    obtain this released connection somewhere else. 
	 *    So the point is that we always perform bind operation after getting new LDAPConnection if we want to perform some 'write' operations on LDAP directory.
	 *    Of course we need a password for that and there's no possibility to get plain password of currently logged in user.
	 *    
	 * Possible workarounds/solutions would be:
	 * 1) Alwas use syncDN/syncPassword for synchronization purposes, so they can't be empty (that's why it was already implemented in previous revision, solves both problems)
	 * 2) Modify LDAPConnectionManager and manage two kinds of LDAPConnections: anonymous and logged in (bind was called on them). And have an API to recieve authenticated LDAPConnection providing Session object for example.
	 *    It solves problem 2.
	 * 3) Something else :)
	 *    
	 */
	@Persistent
	private String syncPassword;
	
	/**
	 * Set of scripts which perform specific synchronization tasks
	 */
	// REV: @Persistent is not necessary. It is the default value. You can safely remove it. Marco.
	// REV: Yes, I knew that. Im my opinion code looks more readable and clear when specifying this explicitly. 
	// Of course I can remove this annotation if it fits better into JFire code style. Denis.
	@Persistent(dependent="true", mappedBy="ldapServer")
	private LDAPScriptSet ldapScriptSet;
	
	
	/**
	 * @deprecated For JDO only!
	 */
	@Deprecated
	public LDAPServer(){}

	
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
			
			// currently this method does nothing, probably it will be used if Session object is used somehow by LDAPServer

			if (logger.isDebugEnabled()){
	        	logger.debug("Logged out from session, id: " + session.getSessionID());
	        }

		}finally{
			unbindAndReleaseConnection(connection);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Session login(LoginData loginData) throws LoginException, UserManagementSystemCommunicationException{

		if (!canBind(loginData.getUserID(), loginData.getPassword())){
			return null;
		}

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
    		}else if (user == null && shouldFetchUserFromLDAP()){
    			if (logger.isDebugEnabled()){
		        	logger.debug(String.format("User %s@%s was not found JFire, will fetch it from LDAP", loginDataUserID, loginDataOrganisationID));
		        }
        		
    			// create new fake user to get userDN
		        userDN = getLDAPUserDN(new User(loginData.getOrganisationID(), loginData.getUserID()));
		        
        	}else if (user == null && !isLeading()){
        		// if there's no such user in JFire and if it's not supposed to fetch it from LDAP
        		// and if JFire is a leading system than login will fail
        		
        		throw new LoginException(
        				String.format(
        						"Can't proceed with login! There's no user in JFire with specified userID %s and JFire being a leading system is NOT configured to fetch it from LDAP. Either use LDAP as leading system or add a %s=true system property.", 
        						loginDataUserID, UserManagementSystem.SHOULD_FETCH_USER_DATA
        						));        		
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

			if (user == null && shouldFetchUserFromLDAP()){
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
	public AuthenticationMethod getAuthenticationMethod() {
		return authenticationMethod;
	}
	
	/**
	 * Set Authentication method for this LDAPServer.
	 * IMPORTANT! For now only SIMPLE method is supported or NONE for anonymous access.
	 * 
	 * @param authenticationMethod
	 */
	public void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
		if (AuthenticationMethod.NONE.equals(authenticationMethod)){
			logger.info("AuthenticationMethod was set to NONE, which means anonymous access only.");
		}else if (!AuthenticationMethod.SIMPLE.equals(authenticationMethod)){
			logger.warn("For now only SIMPLE method is supported or NONE for anonymous access, setting it to SIMPLE");
			authenticationMethod = AuthenticationMethod.SIMPLE;
		}
		this.authenticationMethod = authenticationMethod;
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
    				" Auth method: " + params.getAuthenticationMethod().stringValue()
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
    				" Auth method: " + params.getAuthenticationMethod().stringValue()
    				);
    	}
		
    	connection.unbind();
		LDAPConnectionManager.sharedInstance().releaseConnection(connection);
    	
		if (logger.isDebugEnabled()){
    		logger.debug("Connection released");
    	}
	}
	
	private boolean canBind(String userName, String password){
		
		if (userName == null || "".equals(userName)){
			return false;
		}
		
		if (AuthenticationMethod.SIMPLE.equals(getAuthenticationMethod())
				&& (password == null || "".equals(password))){
			// For simple auth method LDAP doesn't support authentication without password.
			// If password is not provided than it's supposed by LDAP that access is anonymous.
			// So we just log the warning here and silently return, because otherwise log will be 
			// polluted with LoginExceptions. When JFire client starts - it already tries to log in
			// with blank password until login/password dialog appears - it will cause exceptions. Denis.
			logger.warn("Password was not specified for simple LDAP authentication! Bind will no be executed, returning null.");
			return false;
		}
		return true;
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
			
			updateLDAPData(syncEvent.getJFireObjectsIds());
			
		}else{
			throw new UnsupportedOperationException("Unknown LDAPSyncEventType!");
		}
		
	}
	
	/**
	 * Retrieves all entries' names from LDAP which should be synchronized with JFire objects.
	 * 
	 * @return all LDAP entries which should be synchronized into JFire
	 * @throws UserManagementSystemCommunicationException 
	 * @throws LDAPSyncException 
	 * @throws LoginException 
	 */
	public Collection<String> getAllEntriesForSync() throws UserManagementSystemCommunicationException, LoginException, LDAPSyncException{
		LDAPConnection connection = null;
		Collection<String> entriesForSync = new ArrayList<String>();
		try{

			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			connection = createConnection(this);

			bindForSync(connection, pm);
			
			Collection<String> parentEntriesNames = new ArrayList<String>();
			try {
				parentEntriesNames.addAll(ldapScriptSet.getParentEntriesForSync());
			} catch (ScriptException e) {
				logger.error("Can't get initial parent entries from LDAPScripSet!", e);
			}
			
			for (String parentEntryName : parentEntriesNames) {
				entriesForSync.addAll(
						connection.getChildEntries(parentEntryName)
						);
			}
			
		}finally{
			unbindAndReleaseConnection(connection);
		}
		return entriesForSync;
	}
	
	private boolean shouldFetchUserFromLDAP(){

		// we can fetch user data from LDAP in both scenarios:
		// - when JFire is a leading system we're doing it because it might help in certain situations 
		//   (e.g. when you want to use JFire as leading system, but initially have some users in the 
		//   LDAP which you want to import)
		// - when LDAP is a leading system it's done when user still does not exist in JFire
		boolean fetchUserFromLDAP;
		if (isLeading()){
			fetchUserFromLDAP = true;
		}else{
			String fetchPropertyValue = System.getProperty(UserManagementSystem.SHOULD_FETCH_USER_DATA);
			if (fetchPropertyValue != null && !fetchPropertyValue.isEmpty()){
				fetchUserFromLDAP = Boolean.parseBoolean(
						System.getProperty(UserManagementSystem.SHOULD_FETCH_USER_DATA)
						);
			}else{
				fetchUserFromLDAP = true;
			}
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

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		
		try{
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

			bindForSync(connection, pm);
			
			// disable JDO lifecycle listener used for JFire2LDAP synchronization
			SyncStoreLifecycleListener.setEnabled(false);
			// disable JDO lifecycle listener which forbids new User creation
			ForbidUserCreationLyfecycleListener.setEnabled(false);
			
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
			
			// need flush before enabling SyncStoreLifecycleListener
			pm.flush();

			// enable JDO lifecycle listener used for JFire2LDAP synchronization
			SyncStoreLifecycleListener.setEnabled(true);
			// enable JDO lifecycle listener which forbids new User creation
			ForbidUserCreationLyfecycleListener.setEnabled(true);
			
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
	private void updateLDAPData(Collection<?> jfireObjectsIds) throws UserManagementSystemCommunicationException, LoginException, LDAPSyncException{
		
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
					
					if (userDN == null || "".equals(userDN)){
						logger.warn("DN is empty, can't process synchronization for JFire object " + jfireObjectId.toString());
						continue;
					}

					// check if entry exists, probabaly should be done with another API
					boolean entryExists = false;
					try{
						connection.getAttributesForEntry(userDN);
						entryExists = true;
					}catch(UserManagementSystemCommunicationException e){
						entryExists = false;
					}

					if (logger.isDebugEnabled()){
						logger.debug("Preparing attributes for modifying entry with DN: " + userDN);
					}
					LDAPAttributeSet modifyAttributes = ldapScriptSet.getAttributesMapForLDAP(
							jfireObject, !entryExists
							);
					
					if (entryExists){
						if (logger.isDebugEnabled()){
							logger.debug("Modifying entry with DN: " + userDN);
						}
						
						if(modifyAttributes != null && modifyAttributes.size() > 0){
							connection.modifyEntry(userDN, modifyAttributes, EntryModificationFlag.MODIFY);
						}
						
					}else{	// create new entry 
						if (logger.isDebugEnabled()){
							logger.debug("Creating new entry with DN: " + userDN);
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
	
	private void bindForSync(LDAPConnection  connection, PersistenceManager pm) throws LoginException, UserManagementSystemCommunicationException{
		
		try{
			// if some User is logged in than we assume that bind operation has been already performed
			// TODO: check if this user has enough permissions to modify LDAP entries
			GlobalSecurityReflector.sharedInstance().getUserDescriptor();
			
			// TODO: temp
			connection.bind(syncDN, syncPassword);
			
		}catch(NoUserException e){
			// There's no logged in User, so we'll try to bind with syncDN and syncPasswrod
			if (canBind(syncDN, syncPassword)){
				
				connection.bind(syncDN, syncPassword);
				
			}else{
				logger.warn(
						"Can't bind against LDAP because either syncDN or password are not specified. " +
						"There's also no logged in User so all further requests to LDAP will be performed under anonymous user."
						);
			}
		}
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
