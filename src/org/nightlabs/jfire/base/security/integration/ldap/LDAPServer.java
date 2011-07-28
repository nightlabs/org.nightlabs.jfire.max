package org.nightlabs.jfire.base.security.integration.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.StoreCallback;
import javax.naming.InitialContext;
import javax.script.ScriptException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.nightlabs.j2ee.LoginData;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.base.AuthCallbackHandler;
import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttribute;
import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttributeSet;
import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnection;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionManager;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionWrapper.EntryModificationFlag;
import org.nightlabs.jfire.base.security.integration.ldap.sync.AttributeStructFieldSyncHelper;
import org.nightlabs.jfire.base.security.integration.ldap.sync.AttributeStructFieldSyncHelper.AttributeStructFieldDescriptor;
import org.nightlabs.jfire.base.security.integration.ldap.sync.AttributeStructFieldSyncHelper.LDAPAttributeSyncPolicy;
import org.nightlabs.jfire.base.security.integration.ldap.sync.IAttributeStructFieldDescriptorProvider;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.LDAPSyncEventType;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncException;
import org.nightlabs.jfire.base.security.integration.ldap.sync.SyncLifecycleListener;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserDescriptor;
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
 * Class representing LDAP-based UserManagementSystem. This is the most important class for the JFireLDAP module. 
 * It is in charge of logging in {@link User} and synchronization between LDAP directory and JFire. It also implements 
 * {@link ILDAPConnectionParamsProvider} for providing stored server parameters to a {@link LDAPConnection}.
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
@Queries({
		@javax.jdo.annotations.Query(
				name="LDAPServer.findLDAPServers",
				value="SELECT where this.host == :host && this.port == :port && this.encryptionMethod == :encryptionMethod ORDER BY JDOHelper.getObjectId(this) ASCENDING"
				),
		@javax.jdo.annotations.Query(
				name="LDAPServer.findLDAPServersByAttributeSyncPolicy",
				value="SELECT where this.type == :serverType && this.attributeSyncPolicy == :attributeSyncPolicy ORDER BY JDOHelper.getObjectId(this) ASCENDING"
				)
		})
public class LDAPServer extends UserManagementSystem implements ILDAPConnectionParamsProvider, AttachCallback, DeleteCallback, StoreCallback{

	public static final int LDAP_DEFAULT_PORT = 10389;
	public static final String LDAP_DEFAULT_HOST = "localhost";
	public static final String LDAP_DEFAULT_SERVER_NAME = LDAPServer.class.getSimpleName();
	public static final EncryptionMethod LDAP_DEFAULT_ENCRYPTION_METHOD = EncryptionMethod.NONE;
	public static final AuthenticationMethod LDAP_DEFAULT_AUTHENTICATION_METHOD = AuthenticationMethod.SIMPLE;
	public static final LDAPAttributeSyncPolicy LDAP_DEFAULT_ATTRIBUTE_SYNC_POLICY = LDAPAttributeSyncPolicy.MANDATORY_ONLY;

	public static final String SYNC_USER_DATA_FROM_LDAP_TASK_ID = "JFireLDAP-syncUserDataFromLDAP";
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
				"LDAPServer.findLDAPServers"
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
	 * Executes {@link javax.jdo.Query) to find persistent LDAPServer instances by attributeSyncPolicy 
	 * 
	 * @param pm
	 * @param attributeSyncPolicy
	 * @return
	 */
	public static Collection<LDAPServer> findLDAPServersByAttributeSyncPolicy(
			PersistenceManager pm, UserManagementSystemType<?> serverType, LDAPAttributeSyncPolicy attributeSyncPolicy
			) {
		
		javax.jdo.Query q = pm.newNamedQuery(
				LDAPServer.class, 
				"LDAPServer.findLDAPServersByAttributeSyncPolicy"
				);
		@SuppressWarnings("unchecked")
		List<LDAPServer> foundServers = (List<LDAPServer>) q.execute(serverType, attributeSyncPolicy);
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
	private EncryptionMethod encryptionMethod = LDAP_DEFAULT_ENCRYPTION_METHOD;
	
	/**
	 * Authentication method used when binding against LDAP server
	 * IMPORTANT! For now only SIMPLE method is supported or NONE for anonymous access.
	 */
	@Persistent(defaultFetchGroup="true")
	private AuthenticationMethod authenticationMethod = LDAP_DEFAULT_AUTHENTICATION_METHOD;
	
	/**
	 * DN used for binding agains LDAP server during synchronization process (see issue 1974)
	 */
	@Persistent
	private String syncDN;

	/**
	 * Password for DN used for binding agains LDAP server during synchronization process (see issue 1974)
	 */
	@Persistent
	private String syncPassword;
	
	/**
	 * Set of scripts which perform specific synchronization tasks
	 */
	@Persistent(dependent="true", mappedBy="ldapServer")
	private LDAPScriptSet ldapScriptSet;
	
	/**
	 * Policy for mapping LDAP entry attributes to {@link Person} datafields during synchronization.
	 * See {@link AttributeStructFieldSyncHelper} class for details.
	 */
	@Persistent(defaultFetchGroup="true")
	private LDAPAttributeSyncPolicy attributeSyncPolicy = LDAP_DEFAULT_ATTRIBUTE_SYNC_POLICY;
	
	
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
   	    			
					updateJFireData(CollectionUtil.createHashSet(userDN), loginDataOrganisationID, false);
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
	 * @param encryptionMethod, if <code>null</code> is given than the default value will be set
	 */
	public void setEncryptionMethod(EncryptionMethod encryptionMethod) {
		if (encryptionMethod == null){
			encryptionMethod = LDAP_DEFAULT_ENCRYPTION_METHOD;
		}
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
	
	/**
	 * Get {@link #attributeSyncPolicy}
	 * 
	 * @return {@link #attributeSyncPolicy} value
	 */
	public LDAPAttributeSyncPolicy getAttributeSyncPolicy() {
		return attributeSyncPolicy;
	}
	
	/**
	 * Set {@link #attributeSyncPolicy} to this LDAPServer
	 * 
	 * @param attributeSyncPolicy value to set, if <code>null</code> is given than the default value will be set
	 */
	public void setAttributeSyncPolicy(LDAPAttributeSyncPolicy attributeSyncPolicy) {
		if (attributeSyncPolicy == null){
			attributeSyncPolicy = LDAP_DEFAULT_ATTRIBUTE_SYNC_POLICY;
		}
		this.attributeSyncPolicy = attributeSyncPolicy;
	}
	
	/**
	 * Set LDAP entry name which will be used as base entry for generating LDAP distingueshed names by {@link LDAPScriptSet}.
	 * 
	 * @param entryName
	 */
	public void setBaseDN(String entryName) {
		if (entryName == null){
			return;
		}
		ldapScriptSet.setLdapDNScript(
				ldapScriptSet.getLdapDNScript().replaceAll(LDAPScriptSet.BASE_ENTRY_NAME_PLACEHOLDER, entryName));
		ldapScriptSet.setGenerateParentLdapEntriesScript(
				ldapScriptSet.getGenerateParentLdapEntriesScript().replaceAll(LDAPScriptSet.BASE_ENTRY_NAME_PLACEHOLDER, entryName));
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
			
			updateJFireData(syncEvent.getLdapUsersIds(), syncEvent.getOrganisationID(), false);
			
		}else if (LDAPSyncEventType.SEND == syncEvent.getEventType()){
			
			updateLDAPData(syncEvent.getJFireObjectsIds(), false);
			
		}else if (LDAPSyncEventType.DELETE == syncEvent.getEventType()){
			
			if (syncEvent.getJFireObjectsIds() != null){
				updateLDAPData(syncEvent.getJFireObjectsIds(), true);
			}
			if (syncEvent.getLdapUsersIds() != null){
				updateJFireData(syncEvent.getLdapUsersIds(), syncEvent.getOrganisationID(), true);
			}
			
		}else{
			throw new UnsupportedOperationException("Unknown LDAPSyncEventType!");
		}
	}

	/**
	 * Convinient method which binds given {@link LDAPConnection} before data synchronization to LDAP.
	 * First it tries to bind against LDAP using currently logged in {@link User} credentials. If that fails
	 * it tries to bind using {@link #syncDN} and {@link #syncPassword} fields if they are set.
	 * If non of that succeeds it just logs a warning that all further requests to LDAP directory will be 
	 * anonymous so it's up to LDAP directory itself whether it allows anonymous access or throws some kind
	 * of authentication exception (which of course will be propagated to JFire).
	 * 
	 * @param connection {@link LDAPConnection} to bind
	 * @throws LoginException
	 * @throws UserManagementSystemCommunicationException
	 */
	public void bindForSynchronization(LDAPConnection connection) throws LoginException, UserManagementSystemCommunicationException{
		boolean fallToGlobalSyncCredentials = false;
		try{
			UserDescriptor userDescriptor = GlobalSecurityReflector.sharedInstance().getUserDescriptor();

			if (User.USER_ID_SYSTEM.equals(userDescriptor.getUserID())){
				
				logger.warn(
						String.format(
								"Current user is a system user with ID %s, can't bind it against LDAP. Will try to bind with global syncDN/syncPassword if set.", userDescriptor.getCompleteUserID()));
				fallToGlobalSyncCredentials = true;
				
			}else{
			
				User user = null;
				PersistenceManager pm = JDOHelper.getPersistenceManager(this);
				if (pm != null){
					user = userDescriptor.getUser(pm);
				}else{	// create fake, not persisted new User just to get LDAP entry DN
					user = new User(userDescriptor.getOrganisationID(), userDescriptor.getUserID());
				}
				
				String bindDN = getLDAPUserDN(user);
				String bindPwd = null;
				
				Object credential = GlobalSecurityReflector.sharedInstance().getCredential();
				if (credential instanceof String){
					bindPwd = (String) credential;
				}else if (credential instanceof char[]){
					bindPwd = new String((char[])credential);
				}else{
					logger.warn("User credential type is neither String nor char[], can't use it for binding against LDAP. UserID: " + userDescriptor.getCompleteUserID());
				}
				
				if (canBind(bindDN, bindPwd)){
					// TODO: check if this user has enough permissions to read/modify LDAP entries, see issue 1971 (enhancement)
					try{
						connection.bind(bindDN, bindPwd);
					}catch(LoginException e){
						// if we can't log in with current User credentials, we fall to global syncDN/syncPassword
						logger.warn(
								String.format(
										"Can't bind with current User credentials, bind entry name: %s. LoginException occured.", bindDN), e);
						fallToGlobalSyncCredentials = true;
					}
				}else{
					logger.info(
							String.format(
									"Unable to bind against LDAP with current user (userID=%s) credentials, will try to bind with global syncDN/syncPassword if set.", userDescriptor.getCompleteUserID()));
					fallToGlobalSyncCredentials = true;
				}
			}
		}catch(NoUserException e){
			// There's no logged in User, so we'll try to bind with syncDN and syncPasswrod
			fallToGlobalSyncCredentials = true;
		}
		
		if (fallToGlobalSyncCredentials){
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
			connection = createConnection(this);

			bindForSynchronization(connection);
			
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
	 * @param removeJFireObjects
	 * @throws UserManagementSystemCommunicationException
	 * @throws LoginException
	 * @throws LDAPSyncException
	 */
	private void updateJFireData(Collection<String> ldapEntriesDNs, String organisationID, boolean removeJFireObjects) throws UserManagementSystemCommunicationException, LoginException, LDAPSyncException{
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
					User user = User.getUser(pm, organisationID, User.USER_ID_SYSTEM);
					loginContext = j2eeAdapter.createLoginContext(
							LoginData.DEFAULT_SECURITY_PROTOCOL,
							new AuthCallbackHandler(jsm,
									user.getOrganisationID(),
									user.getUserID(),
									ObjectIDUtil.makeValidIDString(null, true))
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
			bindForSynchronization(connection);
			
			// disable JDO lifecycle listener used for JFire2LDAP synchronization
			SyncLifecycleListener.setEnabled(false);
			// disable JDO lifecycle listener which forbids new User creation
			ForbidUserCreationLyfecycleListener.setEnabled(false);
			
			for (String ldapEntryDN : ldapEntriesDNs){
				try{
					
					if (logger.isDebugEnabled()){
						logger.debug("Trying synchronization for DN: " + ldapEntryDN);
					}
					
					LDAPAttributeSet attributes = null;
					if (!removeJFireObjects){
						attributes = connection.getAttributesForEntry(ldapEntryDN);
					}else{
						attributes = new LDAPAttributeSet();
						attributes.createAttributesFromString(ldapEntryDN);
					}
					Object returnObject = ldapScriptSet.syncLDAPDataToJFireObjects(
								attributes, organisationID, removeJFireObjects
							);
					
					// processing attributes synchronization depending on current LDAPAttributeSyncPolicy for this LDAPServer
					if (logger.isDebugEnabled()){
						logger.debug("LDAPAttributeSyncPolicy is " + attributeSyncPolicy.stringValue());
					}
					if (!LDAPAttributeSyncPolicy.NONE.equals(attributeSyncPolicy)
							&& returnObject instanceof Person
							&& getType() instanceof IAttributeStructFieldDescriptorProvider){
						
						IAttributeStructFieldDescriptorProvider descriptorProvider = (IAttributeStructFieldDescriptorProvider) getType();
						Collection<AttributeStructFieldDescriptor> attributeDescriptors = descriptorProvider.getAttributeStructFieldDescriptors(attributeSyncPolicy);
						
						if (logger.isDebugEnabled()){
							logger.debug(
									String.format(
											"Got %s attribute descriptors for sync. Server type is %s, attribute sync policy is %s.", attributeDescriptors.size(), getType().getClass().getName(), attributeSyncPolicy.stringValue()));
						}
						
						AttributeStructFieldSyncHelper.setPersonDataForAttributes(
								pm, (Person) returnObject, descriptorProvider.getAttributeStructBlockID(), attributes, attributeDescriptors);

						if (logger.isDebugEnabled()){
							logger.debug("Attribute sync is done.");
						}
					}

					if (logger.isDebugEnabled()){
						logger.debug("Data synchronized for DN: " + ldapEntryDN);
					}

				}catch(Exception e){
					throw new LDAPSyncException("Exception occured while synchronizing entry with DN " + ldapEntryDN, e);
				}
			}
		}finally{
			// need flush before enabling SyncLifecycleListener
			pm.flush();

			// enable JDO lifecycle listener used for JFire2LDAP synchronization
			SyncLifecycleListener.setEnabled(true);
			// enable JDO lifecycle listener which forbids new User creation
			ForbidUserCreationLyfecycleListener.setEnabled(true);
			
			if (loginContext != null && loginContext.getSubject() != null){
				if (logger.isDebugEnabled()){
					logger.debug("_System_ user was logged in, logging it out...");
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
	 * @param removeEntries
	 * @throws UserManagementSystemCommunicationException
	 * @throws LoginException
	 * @throws LDAPSyncException
	 */
	private void updateLDAPData(Collection<?> jfireObjectsIds, boolean removeEntries) throws UserManagementSystemCommunicationException, LoginException, LDAPSyncException{
		LDAPConnection connection = null;
		try{
			connection = createConnection(this);
			bindForSynchronization(connection);

			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
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

					if (removeEntries){
						
						if (!entryExists){
							logger.warn("Can't remove non-existent entry with DN: " + userDN);
							continue;
						}
						if (logger.isDebugEnabled()){
							logger.debug("Removing existing entry with DN: " + userDN);
						}
						connection.deleteEntry(userDN);
						
					}else{
	
						if (logger.isDebugEnabled()){
							logger.debug("Preparing attributes for modifying entry with DN: " + userDN);
						}
						LDAPAttributeSet modifyAttributes = ldapScriptSet.getLDAPAttributes(
								jfireObject, !entryExists
								);
						
						
						// add attributs based on attributeSyncPolicy set for this LDAPServer
						if (logger.isDebugEnabled()){
							logger.debug("LDAPAttributeSyncPolicy is " + attributeSyncPolicy.stringValue());
						}
						if (!LDAPAttributeSyncPolicy.NONE.equals(attributeSyncPolicy)
								&& getType() instanceof IAttributeStructFieldDescriptorProvider){
							IAttributeStructFieldDescriptorProvider ldapServerType = (IAttributeStructFieldDescriptorProvider) getType();
							LDAPAttributeSet syncAttributes = AttributeStructFieldSyncHelper.getAttributesForSync(
									jfireObject, ldapServerType.getAttributeStructFieldDescriptors(attributeSyncPolicy));

							if (logger.isDebugEnabled()){
								logger.debug(
										String.format(
												"Got %s LDAP attributes for sync. Server type is %s, attribute sync policy is %s.", syncAttributes.size(), getType().getClass().getName(), attributeSyncPolicy.stringValue()));
							}
							
							// removing attributes which might be set by a script earlier
							for (Iterator<LDAPAttribute<Object>> iterator = modifyAttributes.iterator(); iterator.hasNext();) {
								LDAPAttribute<Object> attribute = iterator.next();
								if (syncAttributes.containsAttribute(attribute.getName())){
									iterator.remove();
									logger.debug(
											String.format(
													"LDAP attribute %s was removed from generated by script attribute set because it is mapped by current attribute sync policy %s.", attribute.getName(), attributeSyncPolicy.stringValue()));
								}
							}
							modifyAttributes.addAttributes(syncAttributes);
						}else{
							logger.warn(
									"LDAPServer type is not an instance of ILDAPUserManagementSystemType! Cant' sync attributes based on current attribute sync policy.");
						}
						
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

	
	/**
	 * This boolean flag is used to determine if {@link #attributeSyncPolicy} value was changed on detached {@link LDAPServer} instance.
	 * The field is set for DETACHED instance during {@link #jdoPreAttach()} and is checked in {@link #jdoPostAttach(Object)}.
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean attributeSyncPolicyChanged = true;
	
	/**
	 * If {@link #attributeSyncPolicy} is changed on a ATTACHED {@link LDAPServer} instance you MUST call this method after
	 * {@link #setAttributeSyncPolicy(LDAPAttributeSyncPolicy)} was called to proceed with {@link Person} structure modification.
	 * {@link Person} structure is NOT modified at once every time {@link #setAttributeSyncPolicy(LDAPAttributeSyncPolicy)} is called
	 * because this operation might be time consuming. So do not forget to call {@link #commitAttributeSyncPolicyChange()} when changing
	 * {@link #attributeSyncPolicy} on attached {@link LDAPServer} instance.
	 */
	public void commitAttributeSyncPolicyChange(){
		if (JDOHelper.isDetached(this)){
			logger.warn("commitAttributeSyncPolicyChange can't be called on a detached instance! Returning, Person structure will not be modified.");
			return;
		}
		AttributeStructFieldSyncHelper.modifyPersonStructure(JDOHelper.getPersistenceManager(this), this);
	}

	/**
	 * Attach callback which calls for modification of {@link Person} {@link StructLocal} if {@link #attributeSyncPolicy}
	 * was changed on detached {@link LDAPServer} instance. See {@link AttributeStructFieldSyncHelper} for details.  
	 * @param obj
	 */
	@Override
	public void jdoPostAttach(Object detachedObj) {
		LDAPServer detachedServer = (LDAPServer) detachedObj;
		if (detachedServer.attributeSyncPolicyChanged){
			AttributeStructFieldSyncHelper.modifyPersonStructure(JDOHelper.getPersistenceManager(this), this);
		}
	}

	/**
	 * Attach callback which checks if {@link #attributeSyncPolicy} was changed on a detached {@link LDAPServer} instance
	 * and sets {@link #attributeSyncPolicyChanged} accordingly. See {@link #jdoPostAttach(Object)}.
	 */
	@Override
	public void jdoPreAttach() {
		PersistenceManager pm = NLJDOHelper.getThreadPersistenceManager();
		if (pm != null){
			LDAPServer attachedServer = null;
			try{
				attachedServer = (LDAPServer) pm.getObjectById(
						UserManagementSystemID.create(this.getOrganisationID(), this.getUserManagementSystemID()));
			}catch(JDOObjectNotFoundException e){
				// no object exist, so we assume that new LDAPServer is created and attributeSyncPolicyChanged is considered true
				return;
			}
			if (attachedServer != null){
				this.attributeSyncPolicyChanged = !this.getAttributeSyncPolicy().equals(attachedServer.getAttributeSyncPolicy());
			}
		}
	}

	/**
	 * Store callback is used for new {@link LDAPServer} instances only to call for {@link Person} structure modification
	 * when these instances are created. Further modifications are supposed to happen either in attach callback or by calling
	 * {@link #commitAttributeSyncPolicyChange()} manually on attached {@link LDAPServer} instance.
	 */
	@Override
	public void jdoPreStore() {
		if (JDOHelper.isNew(this)){
			AttributeStructFieldSyncHelper.modifyPersonStructure(JDOHelper.getPersistenceManager(this), this);
		}
	}
	
	/**
	 * Delete callback which calls for {@link Person} {@link StructLocal} modification when {@link LDAPServer} is deleted.
	 * It's done for the case when it is the last {@link LDAPServer} existed with {@link #attributeSyncPolicy} not NONE.
	 * However note that {@link #attributeSyncPolicy} is set to {@link LDAPAttributeSyncPolicy#NONE} before calling for 
	 * {@link Person} structure modifications. This is done in order to remove {@link Person} structure parts if they are not
	 * needed by any other {@link LDAPServer} anymore.
	 */
	@Override
	public void jdoPreDelete() {
		setAttributeSyncPolicy(LDAPAttributeSyncPolicy.NONE);
		AttributeStructFieldSyncHelper.modifyPersonStructure(JDOHelper.getPersistenceManager(this), this);
	}

	/***********************************
	 * Synchronization section END *
	 ***********************************/
	
}
