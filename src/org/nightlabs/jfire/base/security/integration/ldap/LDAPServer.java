package org.nightlabs.jfire.base.security.integration.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionWrapper.SearchScope;
import org.nightlabs.jfire.base.security.integration.ldap.sync.AttributeStructFieldSyncHelper;
import org.nightlabs.jfire.base.security.integration.ldap.sync.AttributeStructFieldSyncHelper.AttributeStructFieldDescriptor;
import org.nightlabs.jfire.base.security.integration.ldap.sync.AttributeStructFieldSyncHelper.LDAPAttributeSyncPolicy;
import org.nightlabs.jfire.base.security.integration.ldap.sync.IAttributeStructFieldDescriptorProvider;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.FetchEventTypeDataUnit;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.SendEventTypeDataUnit;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPUserSecurityGroupSyncConfigLifecycleListener;
import org.nightlabs.jfire.base.security.integration.ldap.sync.SecurityChangeListenerUserSecurityGroupMembers;
import org.nightlabs.jfire.base.security.integration.ldap.sync.SyncLifecycleListener;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.jfire.security.ISecurityReflector;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserDescriptor;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.jfire.security.integration.Session;
import org.nightlabs.jfire.security.integration.SynchronizableUserManagementSystem;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.nightlabs.jfire.security.integration.UserManagementSystemManagerBean.ForbidUserCreationLyfecycleListener;
import org.nightlabs.jfire.security.integration.UserManagementSystemSyncEvent.SyncEventGenericType;
import org.nightlabs.jfire.security.integration.UserManagementSystemSyncEvent.SyncEventType;
import org.nightlabs.jfire.security.integration.UserManagementSystemSyncException;
import org.nightlabs.jfire.security.integration.UserManagementSystemType;
import org.nightlabs.jfire.security.integration.UserSecurityGroupSyncConfig;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.jfire.security.listener.SecurityChangeController;
import org.nightlabs.jfire.servermanager.JFireServerManager;
import org.nightlabs.jfire.servermanager.JFireServerManagerFactory;
import org.nightlabs.jfire.servermanager.j2ee.J2EEAdapter;
import org.nightlabs.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing LDAP-based {@link UserManagementSystem}. This is the most important class for the JFireLDAP module. 
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
public class LDAPServer extends UserManagementSystem 
implements ILDAPConnectionParamsProvider, SynchronizableUserManagementSystem<LDAPSyncEvent>, AttachCallback, DeleteCallback, StoreCallback{

	public static final int LDAP_DEFAULT_PORT = 10389;
	public static final String LDAP_DEFAULT_HOST = "localhost";
	public static final String LDAP_DEFAULT_SERVER_NAME = LDAPServer.class.getSimpleName();
	public static final EncryptionMethod LDAP_DEFAULT_ENCRYPTION_METHOD = EncryptionMethod.NONE;
	public static final AuthenticationMethod LDAP_DEFAULT_AUTHENTICATION_METHOD = AuthenticationMethod.SIMPLE;
	public static final LDAPAttributeSyncPolicy LDAP_DEFAULT_ATTRIBUTE_SYNC_POLICY = LDAPAttributeSyncPolicy.MANDATORY_ONLY;

	public static final String SYNC_USER_DATA_FROM_LDAP_TASK_ID = "JFireLDAP-syncUserDataFromLDAP";
	public static final String FETCH_GROUP_LDAP_SCRIPT_SET = "LDAPServer.ldapScriptSet";

	public static final String OBJECT_CLASS_ATTR_NAME = "objectClass";
	private static final String COMMON_NAME_ATTR_NAME = "commonName";
	private static final String UNIQUE_MEMBER_ATTR_NAME = "uniqueMember";
	private static final String MEMBER_ATTR_NAME = "member";
	private static final String GROUP_OF_NAMES_ATTR_VALUE = "groupOfNames";
	private static final String GROUP_OF_UNIQUE_NAMES_ATTR_VALUE = "groupOfUniqueNames";

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

//			connection = createConnection(this);
			
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
			connection = createConnection(this);
    		PersistenceManager pm = JDOHelper.getPersistenceManager(this);

    		// getting LDAP DN for the loggin in user
    		User user = getUserById(pm, loginDataOrganisationID, loginDataUserID);
    		String userDN = null;
    		if (user != null){
    			userDN = getLDAPUserDN(user);
    			
    			if (logger.isDebugEnabled()){
		        	logger.debug(
		        			String.format("User %s@%s found in JFire, result DN is %s", loginDataUserID, loginDataOrganisationID, userDN));
		        }
    		}else if (user == null && shouldFetchUserData()){
    			if (logger.isDebugEnabled()){
		        	logger.debug(
		        			String.format("User %s@%s was not found JFire, will fetch it from LDAP", loginDataUserID, loginDataOrganisationID));
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

			if (user == null && shouldFetchUserData()){
   				try {
   					if (logger.isDebugEnabled()){
   			        	logger.debug(
   			        			String.format("User %s@%s was not found JFire, start fetching it from LDAP", loginDataUserID, loginDataOrganisationID));
   			        }
   	    			
   					LDAPSyncEvent syncEvent = new LDAPSyncEvent(SyncEventGenericType.FETCH_USER);
   					syncEvent.setFetchEventTypeDataUnits(
   							CollectionUtil.createHashSet(
   									new FetchEventTypeDataUnit(userDN)));
					synchronize(syncEvent);
				} catch (UserManagementSystemSyncException e) {
					logger.error(
							String.format("Exception while fetching user %s@%s from LDAP", loginDataUserID, loginDataOrganisationID), e);
					throw new LoginException(
							String.format("Can't fetch user %s@%s at login! See log for details.", loginDataUserID, loginDataOrganisationID));
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
	public void setBaseDN(String propName, String entryName) {
		if (entryName == null){
			return;
		}
		if (LDAPScriptSet.BASE_USER_ENTRY_NAME_PLACEHOLDER.equals(propName)){
			ldapScriptSet.setLdapDNScript(
					ldapScriptSet.getLdapDNScript().replaceAll(LDAPScriptSet.BASE_USER_ENTRY_NAME_PLACEHOLDER, entryName));
		}
		ldapScriptSet.setGenerateParentLdapEntriesScript(
				ldapScriptSet.getGenerateParentLdapEntriesScript().replaceAll(propName, entryName));
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
	
	private static LDAPConnection createConnection(ILDAPConnectionParamsProvider paramsProvider) throws UserManagementSystemCommunicationException{
		LDAPConnection connection = LDAPConnectionManager.sharedInstance().getConnection(paramsProvider);
    	if (logger.isDebugEnabled()){
    		logger.debug(
    				String.format("LDAPConnection recieved. LDAPServer at %s:%s  Encryption: %s  Auth method: %s",
    								paramsProvider.getHost(), paramsProvider.getPort(), 
    								paramsProvider.getEncryptionMethod().stringValue(),
    								paramsProvider.getAuthenticationMethod().stringValue()));
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
    				String.format("Unbinding and releasing connection for LDAP server at %s:%s  Encryption: %s  Auth method: %s",
    								params.getHost(), params.getPort(),
    								params.getEncryptionMethod().stringValue(),
    								params.getAuthenticationMethod().stringValue()));
    	}
		
    	try{
    		connection.unbind();
    	}finally{
    		LDAPConnectionManager.sharedInstance().releaseConnection(connection);
    	}
    	
		if (logger.isDebugEnabled()){
    		logger.debug("Connection released");
    	}
	}
	
	/**
	 * Checks whether we can bind against {@link LDAPServer} using provided username and password.
	 * 
	 * @param userName
	 * @param password
	 * @return <code>true</code> if binding is possible, <code>false</code> otherwise
	 */
	public boolean canBind(String userName, String password){
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
	 * {@inheritDoc}
	 */
	@Override
	public void synchronize(LDAPSyncEvent syncEvent) throws UserManagementSystemSyncException, LoginException, UserManagementSystemCommunicationException{
		SyncEventType eventType = syncEvent.getEventType();
		if (SyncEventGenericType.FETCH_USER == eventType
				|| SyncEventGenericType.JFIRE_REMOVE_USER == eventType){
			
			updateJFireUserData(syncEvent);
			
		}else if (SyncEventGenericType.SEND_USER == eventType
				|| SyncEventGenericType.UMS_REMOVE_USER == eventType){
			
			updateLDAPUserData(syncEvent);
			
		}else if(SyncEventGenericType.FETCH_AUTHORIZATION == eventType
				|| SyncEventGenericType.JFIRE_REMOVE_AUTHORIZATION == eventType){

			updateJFireAuthorizationData(syncEvent);
			
		}else if(SyncEventGenericType.SEND_AUTHORIZATION == eventType
				|| SyncEventGenericType.UMS_REMOVE_AUTHORIZATION == eventType){
			
			updateLDAPAuthorizationData(syncEvent);
			
		}else{
			throw new UnsupportedOperationException("Unknown UserManagementSystemSyncEventType!");
		}
	}

	/**
	 * Convinient method which either creates and binds or gets a private authenticated {@link LDAPConnection} 
	 * before data synchronization to LDAP. When non-private {@link LDAPConnection} from {@link LDAPConnectionManager} pool
	 * is recieved first it tries to bind it using currently logged in {@link User} credentials. If that fails
	 * it tries to bind using {@link #syncDN} and {@link #syncPassword} fields if they are set.
	 * If non of that succeeds it just logs a warning that all further requests to LDAP directory will be 
	 * anonymous so it's up to LDAP directory itself whether it allows anonymous access or throws some kind
	 * of authentication exception (which of course will be propagated to JFire).
	 * 
	 * @throws LoginException
	 * @throws UserManagementSystemCommunicationException
	 */
	public LDAPConnection createAndBindConnectionForSync() throws UserManagementSystemCommunicationException, LoginException{
		LDAPConnection connection = null;
		try{	// try to get already authenticated connection for a current User
			logger.info("Getting already authenticated (binded) LDAPConnection for synchronization.");
			connection = LDAPConnectionManager.sharedInstance().getPrivateLDAPConnection(this);
		}catch(NoUserException e){
			logger.info("Failed getting authenticated LDAPConnection for synchronization, new connection from pool will be used.");
		}
		
		if (connection != null){
			if (logger.isDebugEnabled()){
	    		logger.debug(
	    				String.format("Private (authenticated) LDAPConnection recieved. LDAPServer at %s:%s  Encryption: %s  Auth method: %s",
	    								getHost(), getPort(),
	    								getEncryptionMethod().stringValue(),
	    								getAuthenticationMethod().stringValue()));
	    	}
		}else{
			connection = LDAPConnectionManager.sharedInstance().getConnection(this);
	    	if (logger.isDebugEnabled()){
	    		logger.debug(
	    				String.format("LDAPConnection recieved. LDAPServer at %s:%s  Encryption: %s  Auth method: %s",
	    								getHost(), getPort(),
	    								getEncryptionMethod().stringValue(),
	    								getAuthenticationMethod().stringValue()));
	    	}
			bindForSynchronization(connection);
		}
    	return connection;
	}

	/**
	 * Get credential of current logged in {@link User} from a {@link ISecurityReflector}
	 * and checks whether it is a {@link String} or a char array so it could be used for 
	 * binding against LDAP directory. Logs a warning if credential is neither String nor char[].
	 * 
	 * @return password as a {@link String} or <code>null</code> if password is not a {@link String} or char[]
	 * @throws NoUserException If no {@link User} is logged in
	 */
	public static String getLDAPPasswordForCurrentUser() throws NoUserException{
		Object credential = GlobalSecurityReflector.sharedInstance().getCredential();
		String pwd = null;
		if (credential instanceof String){
			pwd = (String) credential;
		}else if (credential instanceof char[]){
			pwd = new String((char[])credential);
		}else{
			logger.warn(
					"User credential type is neither String nor char[], can't use it for binding against LDAP. UserID: " +  GlobalSecurityReflector.sharedInstance().getUserDescriptor().getCompleteUserID());
		}
		return pwd;
	}
	
	/**
	 * Retrieves all entries' names from LDAP which should be synchronized with JFire objects.
	 * 
	 * @return all LDAP entries which should be synchronized into JFire
	 * @throws UserManagementSystemCommunicationException 
	 * @throws UserManagementSystemSyncException 
	 * @throws LoginException 
	 */
	public Collection<String> getAllUserEntriesForSync() throws UserManagementSystemCommunicationException, LoginException, UserManagementSystemSyncException{
		LDAPConnection connection = null;
		Collection<String> entriesForSync = new ArrayList<String>();
		try{
			connection = createAndBindConnectionForSync();
			
			Collection<String> parentEntriesNames = new ArrayList<String>();
			try {
				parentEntriesNames.addAll(ldapScriptSet.getUserParentEntriesForSync());
			} catch (ScriptException e) {
				logger.error("Can't get initial parent entries from LDAPScripSet!", e);
			} catch (NoSuchMethodException e) {
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

	private void bindForSynchronization(LDAPConnection connection) throws LoginException, UserManagementSystemCommunicationException{
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
				String bindPwd = getLDAPPasswordForCurrentUser();
				
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
	 * Performs synchronization of {@link User} and/or {@link Person} data from LDAP directory to JFire
	 * 
	 * @param ldapSyncEvent
	 * @param removeJFireObjects
	 * @throws UserManagementSystemCommunicationException
	 * @throws LoginException
	 * @throws UserManagementSystemSyncException
	 */
	private void updateJFireUserData(final LDAPSyncEvent ldapSyncEvent) throws UserManagementSystemCommunicationException, LoginException, UserManagementSystemSyncException{
		
		if (ldapSyncEvent.getFetchEventTypeDataUnits() == null
				|| ldapSyncEvent.getFetchEventTypeDataUnits().isEmpty()){
			throw new UserManagementSystemSyncException("Synchronization is not possible! LDAPSyncEvent don't have any FetchEventTypeDataUnits!");
		}
		
		final PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		
		SyncRunnable runnable = new SyncRunnable() {
			@Override
			public void run() throws LoginException, UserManagementSystemCommunicationException, UserManagementSystemSyncException {
				LDAPConnection connection = null;
				try{
					connection = createAndBindConnectionForSync();
					
					// disable JDO lifecycle listener used for JFire2LDAP synchronization
					SyncLifecycleListener.setEnabled(false);
					// disable JDO lifecycle listener which forbids new User creation
					ForbidUserCreationLyfecycleListener.setEnabled(false);
					
					Collection<String> ldapEntriesToSyncAuthorization = new HashSet<String>();
					Throwable lastSyncThrowable = null;
					boolean removeJFireObjects = ldapSyncEvent.getEventType() == SyncEventGenericType.JFIRE_REMOVE_USER;
					for (FetchEventTypeDataUnit dataUnit : ldapSyncEvent.getFetchEventTypeDataUnits()){
						String ldapEntryDN = dataUnit.getLdapEntryName();
						try{
							LDAPAttributeSet attributes = null;
							if (!removeJFireObjects ){
								attributes = connection.getAttributesForEntry(ldapEntryDN);
							}else{
								attributes = LDAPAttributeSet.createAttributesFromString(ldapEntryDN);
							}
							
							boolean userExists = false;
							try{
								userExists = getUserByLDAPEntryName(pm, attributes) != null;
							}catch(JDOObjectNotFoundException e){
								// no user exists, which means that current LDAP entry corresponds to either a new User or a (existing) Person
								userExists = false;
							}

							Object returnObject = ldapScriptSet.syncLDAPDataToJFireObjects(ldapEntryDN, attributes, removeJFireObjects);
							
							// processing attributes synchronization depending on current LDAPAttributeSyncPolicy for this LDAPServer
							Person person = null;
							User user = null;
							if (returnObject instanceof Person){
								person = (Person) returnObject;
							}else if (returnObject instanceof User){
								user = (User) returnObject;
								person = user.getPerson();
								if (!userExists){
									ldapEntriesToSyncAuthorization.add(ldapEntryDN);
								}
							}
							if (!LDAPAttributeSyncPolicy.NONE.equals(attributeSyncPolicy)
									&& person != null
									&& getType() instanceof IAttributeStructFieldDescriptorProvider){
								
								IAttributeStructFieldDescriptorProvider descriptorProvider = (IAttributeStructFieldDescriptorProvider) getType();
								Collection<AttributeStructFieldDescriptor> attributeDescriptors = descriptorProvider.getAttributeStructFieldDescriptors(attributeSyncPolicy);
								
								AttributeStructFieldSyncHelper.setPersonDataForAttributes(
										pm, person, descriptorProvider.getAttributeStructBlockID(), attributes, attributeDescriptors);

								pm.makePersistent(user != null ? user : person);
							}
						}catch(Exception e){
							logger.error("Exception occured while synchronizing entry with DN " + ldapEntryDN, e);
							lastSyncThrowable = e;
						}
					}
					
					// sync authorization data for newly created Users
					if (!ldapEntriesToSyncAuthorization.isEmpty()){
						try{
							syncAuthorizationData(connection, ldapEntriesToSyncAuthorization);
						}catch(Exception e){
							logger.error("Exception occured while synchronizing authorization data!", e);
							lastSyncThrowable = e;
						}
					}
					
					if (lastSyncThrowable != null){
						throw new UserManagementSystemSyncException(
								"Exception(s) occured during synchronization! See log for details. Last exception was " + lastSyncThrowable.getMessage(), lastSyncThrowable);
					}
				}finally{
					// need flush before enabling SyncLifecycleListener
					pm.flush();

					// enable JDO lifecycle listener used for JFire2LDAP synchronization
					SyncLifecycleListener.setEnabled(true);
					// enable JDO lifecycle listener which forbids new User creation
					ForbidUserCreationLyfecycleListener.setEnabled(true);
					
					unbindAndReleaseConnection(connection);
				}
			}

			private void syncAuthorizationData(LDAPConnection connection, Collection<String> ldapEntriesToSyncAuthorization) throws LoginException, UserManagementSystemSyncException, UserManagementSystemCommunicationException {
				StringBuilder filterString = new StringBuilder();
				filterString.append("(&");
				filterString.append("(|(objectClass=").append(GROUP_OF_NAMES_ATTR_VALUE);
				filterString.append(")(objectClass=").append(GROUP_OF_UNIQUE_NAMES_ATTR_VALUE).append("))(|");
				for (String ldapName : ldapEntriesToSyncAuthorization){
					filterString.append("(").append(MEMBER_ATTR_NAME).append("=").append(ldapName).append(")");
					filterString.append("(").append(UNIQUE_MEMBER_ATTR_NAME).append("=").append(ldapName).append(")");
				}
				filterString.append("))");
				try {
					Map<String, LDAPAttributeSet> searchResult = connection.search(
							"", filterString.toString(), null, SearchScope.SUBTREE);
					
					LDAPSyncEvent event = new LDAPSyncEvent(SyncEventGenericType.FETCH_AUTHORIZATION);
					Collection<FetchEventTypeDataUnit> fetchDataUnits = new ArrayList<FetchEventTypeDataUnit>();
					for (String ldapName : searchResult.keySet()){
						fetchDataUnits.add(new FetchEventTypeDataUnit(ldapName));
					}
					event.setFetchEventTypeDataUnits(fetchDataUnits);
					
					userSyncIsRunning.set(true);
					synchronize(event);
				} finally {
					userSyncIsRunning.set(false);
				}
			}
		};
		executeAsSystemUser(pm, runnable);
	}
	
	/**
	 * Performs synchronization of {@link User} and/or {@link Person} data from JFire to LDAP directory
	 * 
	 * @param ldapSyncEvent
	 * @throws UserManagementSystemCommunicationException
	 * @throws LoginException
	 * @throws UserManagementSystemSyncException
	 */
	private void updateLDAPUserData(LDAPSyncEvent ldapSyncEvent) throws UserManagementSystemCommunicationException, LoginException, UserManagementSystemSyncException{
		
		if (ldapSyncEvent.getSendEventTypeDataUnits() == null
				|| ldapSyncEvent.getSendEventTypeDataUnits().isEmpty()){
			throw new UserManagementSystemSyncException("Synchronization is not possible! LDAPSyncEvent don't have any SendEventTypeDataUnits!");
		}
		
		LDAPConnection connection = null;
		try{
			connection = createAndBindConnectionForSync();
			Throwable lastSyncThrowable = null;
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			for (SendEventTypeDataUnit dataUnit : ldapSyncEvent.getSendEventTypeDataUnits()){
				Object jfireObjectId = dataUnit.getJfireObjectId();
				try{
					String userDN = dataUnit.getLdapEntryId();
					if (ldapSyncEvent.getEventType() == SyncEventGenericType.SEND_USER
							&& (userDN == null || userDN.isEmpty())){
						Object jfireObject = pm.getObjectById(jfireObjectId);
						pm.refresh(jfireObject);
						userDN = getLDAPUserDN(jfireObject);
					}
					
					if (userDN == null || "".equals(userDN)){
						logger.warn("DN is empty, can't process synchronization for JFire object " + jfireObjectId.toString());
						continue;
					}

					boolean entryExists = connection.entryExists(userDN);
					if (ldapSyncEvent.getEventType() == SyncEventGenericType.UMS_REMOVE_USER){
						
						if (!entryExists){
							logger.warn("Can't remove non-existent entry with DN: " + userDN);
							continue;
						}
						connection.deleteEntry(userDN);
						
					}else{
						Object jfireObject = pm.getObjectById(jfireObjectId);
						LDAPAttributeSet modifyAttributes = ldapScriptSet.getLDAPAttributes(
								jfireObject, !entryExists
								);
						
						// add attributs based on attributeSyncPolicy set for this LDAPServer
						Person person = null;
						if (jfireObject instanceof Person){
							person = (Person) jfireObject;
						}else if (jfireObject instanceof User){
							person = ((User) jfireObject).getPerson();
						}
						if (!LDAPAttributeSyncPolicy.NONE.equals(attributeSyncPolicy)
								&& person != null
								&& getType() instanceof IAttributeStructFieldDescriptorProvider){
							IAttributeStructFieldDescriptorProvider ldapServerType = (IAttributeStructFieldDescriptorProvider) getType();
							LDAPAttributeSet syncAttributes = AttributeStructFieldSyncHelper.getAttributesForSync(
									person, ldapServerType.getAttributeStructFieldDescriptors(attributeSyncPolicy));

							// removing attributes which might be set by a script earlier
							for (Iterator<LDAPAttribute<Object>> iterator = modifyAttributes.iterator(); iterator.hasNext();) {
								LDAPAttribute<Object> attribute = iterator.next();
								if (syncAttributes.containsAttribute(attribute.getName())){
									iterator.remove();
								}
							}
							modifyAttributes.addAttributes(syncAttributes);
						}
						
						if(modifyAttributes != null && modifyAttributes.size() > 0){
							if (entryExists){
								connection.modifyEntry(userDN, modifyAttributes, EntryModificationFlag.MODIFY);
							}else{ 
								connection.createEntry(userDN, modifyAttributes);
							}
						}
					}
				}catch(Exception e){
					logger.error("Exception occured while synchronizing object with id " + jfireObjectId.toString(), e);
					lastSyncThrowable = e;
				}
			}	// for (SendEventTypeDataUnit dataUnit : ldapSyncEvent.getSendEventTypeDataUnits()){
			if (lastSyncThrowable != null){
				throw new UserManagementSystemSyncException(
						"Exception(s) occured during synchronization! Ses log for details. Last exception was " + lastSyncThrowable.getMessage(), lastSyncThrowable);
			}
		}finally{
			unbindAndReleaseConnection(connection);
		}
	}
	
	private User getUserByLDAPEntryName(PersistenceManager pm, LDAPAttributeSet attributes) throws ScriptException, NoSuchMethodException{
		UserID userID = getLdapScriptSet().getUserIDFromLDAPEntry(attributes);
		if (userID != null){
			User user = null;
			try{
				user = User.getUser(pm, userID.organisationID, userID.userID);
			}catch(JDOObjectNotFoundException e){
				throw e;
			}
			return user;
		}
		return null;
	}

	/**
	 * When sync for {@link User}s is already running we do not sync other Users while synchronizing authorization data for current Users.
	 */
	private static ThreadLocal<Boolean> userSyncIsRunning = new ThreadLocal<Boolean>(){
		protected Boolean initialValue() {
			return false;
		};
	};

	private void updateJFireAuthorizationData(final LDAPSyncEvent ldapSyncEvent) throws UserManagementSystemSyncException, LoginException, UserManagementSystemCommunicationException{
		final PersistenceManager pm = JDOHelper.getPersistenceManager(this);

		SyncRunnable runnable = new SyncRunnable() {
			@Override
			public void run() throws LoginException, UserManagementSystemCommunicationException, UserManagementSystemSyncException{
				LDAPConnection connection = null;
				try{
					connection = createAndBindConnectionForSync();
					
					// disable JDO lifecycle listeners used for JFire2LDAP synchronization
					SyncLifecycleListener.setEnabled(false);
					LDAPUserSecurityGroupSyncConfigLifecycleListener.setEnabled(false);
					SecurityChangeListenerUserSecurityGroupMembers.setEnabled(false);
					
					Throwable lastSyncThrowable = null;
					boolean removeJFireObjects = ldapSyncEvent.getEventType() == SyncEventGenericType.JFIRE_REMOVE_AUTHORIZATION;
					for (FetchEventTypeDataUnit dataUnit : ldapSyncEvent.getFetchEventTypeDataUnits()){
						String ldapGroupDN = dataUnit.getLdapEntryName();
						try{
							LDAPUserSecurityGroupSyncConfig syncConfig = LDAPUserSecurityGroupSyncConfig.getSyncConfigForLDAPGroupName(
									pm, LDAPServer.this.getUserManagementSystemObjectID(), ldapGroupDN);

							LDAPAttributeSet attributes = connection.getAttributesForEntry(
									ldapGroupDN, new String[]{OBJECT_CLASS_ATTR_NAME, MEMBER_ATTR_NAME, UNIQUE_MEMBER_ATTR_NAME, COMMON_NAME_ATTR_NAME});
							
							LDAPSecurityGroup ldapGroup = new LDAPSecurityGroup(attributes);

							if (syncConfig != null && removeJFireObjects){
								UserSecurityGroup userSecurityGroup = syncConfig.getUserSecurityGroup();
								
								// remove the sync config
								pm.deletePersistent(syncConfig);
								
								// remove UserSecurityGroup if it does not have other UserSecurityGroupSyncConfigs referencing it
								if (!UserSecurityGroupSyncConfig.syncConfigsExistForGroup(
										pm, UserSecurityGroupID.create(userSecurityGroup.getOrganisationID(), userSecurityGroup.getUserSecurityGroupID()))){
									boolean successful = false;
									SecurityChangeController.beginChanging();
									try {
										Set<AuthorizedObject> members = userSecurityGroup.getMembers();
										for (AuthorizedObject member : members) {
											userSecurityGroup.removeMember(member);
										}
									} finally {
										SecurityChangeController.endChanging(successful);
									}
									pm.deletePersistent(userSecurityGroup);
								}
							}else{
								String groupName = ldapGroup.getGroupName();
								if (groupName == null){
									groupName = ldapGroupDN;
								}
								if (syncConfig == null 
										&& shouldFetchUserData()){
									UserSecurityGroup newUserSecurityGroup = new UserSecurityGroup(getOrganisationID(), groupName);
									newUserSecurityGroup.setName(groupName);
									newUserSecurityGroup = pm.makePersistent(newUserSecurityGroup);
									
									syncConfig = new LDAPUserSecurityGroupSyncConfig(newUserSecurityGroup, LDAPServer.this, ldapGroupDN);
									syncConfig = pm.makePersistent(syncConfig);
								}
								
								if (syncConfig == null){
									logger.info(
											String.format("Neither LDAPUserSecurityGroupSyncConfig found for LDAP group %s nor system is configured to craete a new one, skipping synchronization", ldapGroupDN));
									return;
								}

								Iterable<Object> ldapGroupMembers = attributes.getAttributeValues(ldapGroup.getMemberAttr());
								Collection<User> users = new ArrayList<User>();
								Map<String, LDAPAttributeSet> ldapEntriesForSync = new HashMap<String, LDAPAttributeSet>();
								for (Object groupMember : ldapGroupMembers){
									if (groupMember instanceof String){
										String groupMemberName = (String) groupMember;
										LDAPAttributeSet attributesForEntry = connection.getAttributesForEntry(groupMemberName);
										User user = null;
										try{
											user = getUserByLDAPEntryName(pm, attributesForEntry);
										}catch(JDOObjectNotFoundException e){
											// no user, add for later synchronization
											if (shouldFetchUserData() && !userSyncIsRunning.get()){
												ldapEntriesForSync.put(groupMemberName, attributesForEntry);
											}
										}
										if (user != null){
											users.add(user);
										}
									}
								}
								
								if (!ldapEntriesForSync.isEmpty()){	// sync Users
									LDAPSyncEvent syncEvent = new LDAPSyncEvent(SyncEventGenericType.FETCH_USER);
									Collection<FetchEventTypeDataUnit> dataUnits = new ArrayList<FetchEventTypeDataUnit>();
									for (String ldapEntry : ldapEntriesForSync.keySet()){
										dataUnits.add(new FetchEventTypeDataUnit(ldapEntry));
									}
									syncEvent.setFetchEventTypeDataUnits(dataUnits);
									synchronize(syncEvent);
									
									for (String ldapEntry : ldapEntriesForSync.keySet()){
										User user = null;
										try{
											user = getUserByLDAPEntryName(pm, ldapEntriesForSync.get(ldapEntry));
										}catch(JDOObjectNotFoundException e){
											// still no user
											logger.warn(String.format("User for LDAP name %s still does not exist in JFire!", ldapEntry));
										}
										if (user != null){
											users.add(user);
										}
									}
								}
								ldapEntriesForSync.clear();

								UserSecurityGroup userSecurityGroup = syncConfig.getUserSecurityGroup();
								userSecurityGroup.setName(groupName);
								boolean successful = false;
								SecurityChangeController.beginChanging();
								try {
									Set<AuthorizedObject> jfireGroupMembers = userSecurityGroup.getMembers();
									for (AuthorizedObject authorizedObject : jfireGroupMembers) {
										if (authorizedObject instanceof UserLocal){
											UserLocal userLocal = (UserLocal) authorizedObject;
											if (!users.contains(userLocal.getUser())){	// remove User from group
												userSecurityGroup.removeMember(userLocal);
											}
										}
									}
									for (User user : users){
										userSecurityGroup.addMember(user.getUserLocal());	// add User to group
									}
								} finally {
									SecurityChangeController.endChanging(successful);
								}
								pm.makePersistent(userSecurityGroup);
							}
						}catch(Exception e){
							logger.error("Exception occured while synchronizing group with DN " + ldapGroupDN, e);
							lastSyncThrowable = e;
						}
					}	// for (FetchEventTypeDataUnit dataUnit : ldapSyncEvent.getFetchEventTypeDataUnits()){
					if (lastSyncThrowable != null){
						throw new UserManagementSystemSyncException(
								"Exception(s) occured during synchronization! See log for details. Last exception was " + lastSyncThrowable.getMessage(), lastSyncThrowable);
					}
				}finally{
					// need flush before enabling SyncLifecycleListener
					pm.flush();

					// enable JDO lifecycle listeners used for JFire2LDAP synchronization
					SyncLifecycleListener.setEnabled(true);
					LDAPUserSecurityGroupSyncConfigLifecycleListener.setEnabled(true);
					SecurityChangeListenerUserSecurityGroupMembers.setEnabled(true);
					
					unbindAndReleaseConnection(connection);
				}
			}
		};
		executeAsSystemUser(pm, runnable);
	}

	private void updateLDAPAuthorizationData(LDAPSyncEvent ldapSyncEvent) throws LoginException, UserManagementSystemCommunicationException, UserManagementSystemSyncException{
		LDAPConnection connection = null;
		try{
			connection = createAndBindConnectionForSync();
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);

			// disallowing changing UserSecurityGroups members
			SecurityChangeListenerUserSecurityGroupMembers.setChangeGroupMembersEnabled(false);
			
			Throwable lastSyncThrowable = null;
			for (SendEventTypeDataUnit dataUnit : ldapSyncEvent.getSendEventTypeDataUnits()){
				UserSecurityGroupID userSecurityGroupId = null;
				try{
					Object jfireObjectId = dataUnit.getJfireObjectId();
					if (!(jfireObjectId instanceof UserSecurityGroupID)){
						throw new UserManagementSystemSyncException("JFire object ID should be UserSecurityGroupID! Instead it is " + jfireObjectId.getClass());
					}
					userSecurityGroupId = (UserSecurityGroupID) jfireObjectId;
					
					LDAPUserSecurityGroupSyncConfig syncConfig = (LDAPUserSecurityGroupSyncConfig) UserSecurityGroupSyncConfig.getSyncConfigForGroup(
							pm, getUserManagementSystemObjectID(), userSecurityGroupId);

					String ldapGroupName = dataUnit.getLdapEntryId();
					if ((ldapGroupName == null || ldapGroupName.isEmpty()) 
							&& syncConfig != null){
						ldapGroupName = syncConfig.getUserManagementSystemSecurityObject();
						if (ldapGroupName == null || ldapGroupName.isEmpty()){
							throw new UserManagementSystemSyncException("LDAP group name is either null or empty for LDAPUserSecurityGroupSyncConfig with UserSecurityGroup: " + userSecurityGroupId.toString());
						}
					}
					
					boolean entryExists = false;
					LDAPAttributeSet existingEntryAttributes = null;
					try{
						existingEntryAttributes = connection.getAttributesForEntry(ldapGroupName, new String[]{OBJECT_CLASS_ATTR_NAME});
						entryExists = true;
					}catch(UserManagementSystemCommunicationException e){
						logger.info(
								String.format("Exception while getting attributes for LDAP entry %s. Assume it does not exist.", ldapGroupName), e);
					}
					if (ldapSyncEvent.getEventType() == SyncEventGenericType.UMS_REMOVE_AUTHORIZATION){
						
						if (!entryExists){
							logger.warn("Can't remove non-existent group entry with DN: " + ldapGroupName);
							continue;
						}
						connection.deleteEntry(ldapGroupName);
						
					}else{

						if (syncConfig == null){
							logger.info(
									String.format("No UserSecurityGroupSyncConfig exist for UserSecurityGroup with ID %s, skipping synchronization", userSecurityGroupId.userSecurityGroupID));
							continue;
						}

						String groupName = syncConfig.getUserSecurityGroup().getName();
						if (groupName == null || groupName.isEmpty()){
							groupName = syncConfig.getUserSecurityGroup().getUserSecurityGroupID();
						}
						LDAPSecurityGroup ldapGroup = getDefaultLDAPSecurityGroup(
								existingEntryAttributes, connection);
						ldapGroup.setGroupName(groupName);
						
						LDAPAttributeSet modifyAttributes = new LDAPAttributeSet();
						modifyAttributes.createAttribute(COMMON_NAME_ATTR_NAME, ldapGroup.getGroupName());
						Set<AuthorizedObject> members = syncConfig.getUserSecurityGroup().getMembers();
						for (AuthorizedObject authorizedObject : members) {
							if (authorizedObject instanceof UserLocal){
								User user = ((UserLocal) authorizedObject).getUser();
								String ldapUserDN = getLDAPUserDN(user);
								modifyAttributes.createAttribute(ldapGroup.getMemberAttr(), ldapUserDN);
							}
						}
						
						if(modifyAttributes != null && modifyAttributes.size() > 0){
							if (entryExists){
								String newEntryName = connection.modifyEntry(ldapGroupName, modifyAttributes, EntryModificationFlag.MODIFY);
								if (newEntryName != null){
									syncConfig.setLdapGroupName(newEntryName);
									pm.makePersistent(syncConfig);
								}
							}else{	// create new entry
								modifyAttributes.createAttribute(OBJECT_CLASS_ATTR_NAME, "top");
								modifyAttributes.createAttribute(OBJECT_CLASS_ATTR_NAME, ldapGroup.getObjectClassAttr());
								connection.createEntry(ldapGroupName, modifyAttributes);
							}
						}
					}
				}catch(Exception e){
					logger.error("Exception occured while synchronizing object with id " + userSecurityGroupId!=null?userSecurityGroupId.toString():null, e);
					lastSyncThrowable = e;
				}
			}	// for (SendEventTypeDataUnit dataUnit : ldapSyncEvent.getSendEventTypeDataUnits()){
			if (lastSyncThrowable != null){
				throw new UserManagementSystemSyncException(
						"Exception(s) occured during synchronization! See log for details. Last exception was " + lastSyncThrowable.getMessage(), lastSyncThrowable);
			}
		}finally{
			SecurityChangeListenerUserSecurityGroupMembers.setChangeGroupMembersEnabled(true);
			unbindAndReleaseConnection(connection);
		}
	}

	private void executeAsSystemUser(PersistenceManager pm, SyncRunnable syncRunnable) throws LoginException, UserManagementSystemCommunicationException, UserManagementSystemSyncException{
		LoginContext loginContext = null;
		JFireServerManager jsm = null;
		try{
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
					User user = User.getUser(pm, getOrganisationID(), User.USER_ID_SYSTEM);
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
					throw new RuntimeException("Failed to log in System user!", ne);
				}
			}
			
			syncRunnable.run();
			
		}finally{
			if (loginContext != null && loginContext.getSubject() != null){
				if (logger.isDebugEnabled()){
					logger.debug("_System_ user was logged in, logging it out...");
				}
				loginContext.logout();
			}
			if (jsm != null){
				jsm.close();
			}
		}
	}
	
	private LDAPSecurityGroup getDefaultLDAPSecurityGroup(LDAPAttributeSet existingEntryAttrs, LDAPConnection connection) 
			throws ScriptException, NoSuchMethodException, LoginException, UserManagementSystemCommunicationException{
		if (existingEntryAttrs != null){
			return new LDAPSecurityGroup(existingEntryAttrs);
		}else{
			Collection<String> groupParentEntries = ldapScriptSet.getGroupParentEntriesForSync();
			if (groupParentEntries.isEmpty()){
				throw new IllegalStateException("No parent entries for LDAP security groups found!");
			}
			Collection<String> childEntries = null;
			for (String parentEntry : groupParentEntries){
				childEntries = connection.getChildEntries(parentEntry);
				if (childEntries != null && !childEntries.isEmpty()){
					break;
				}
			}
			if (childEntries == null || childEntries.isEmpty()){
				logger.warn("Can't get any group entries from LDAP server, default values will be used for LDAPSecurityGroup!");
				return new LDAPSecurityGroup("", GROUP_OF_NAMES_ATTR_VALUE, MEMBER_ATTR_NAME);
			}else{
				return new LDAPSecurityGroup(
						connection.getAttributesForEntry(childEntries.iterator().next()));
			}
		}
	}
	
	abstract class SyncRunnable{
		public abstract void run() throws LoginException, UserManagementSystemCommunicationException, UserManagementSystemSyncException;
	}
	
	class LDAPSecurityGroup{

		private String groupName;
		private String objectClassAttr;
		private String memberAttr;
		
		public LDAPSecurityGroup(LDAPAttributeSet attributes) {
			Iterable<Object> attributeValues = attributes.getAttributeValues(OBJECT_CLASS_ATTR_NAME);
			for (Object value : attributeValues){
				if (GROUP_OF_NAMES_ATTR_VALUE.equals(value)){
					objectClassAttr = GROUP_OF_NAMES_ATTR_VALUE;
					memberAttr = MEMBER_ATTR_NAME;
					break;
				}else if (GROUP_OF_UNIQUE_NAMES_ATTR_VALUE.equals(value)){
					objectClassAttr = GROUP_OF_UNIQUE_NAMES_ATTR_VALUE;
					memberAttr = UNIQUE_MEMBER_ATTR_NAME;
					break;
				}
			}
			if (objectClassAttr == null || memberAttr == null){
				throw new IllegalArgumentException("Can't get objectClass and member attribute names from given attribute set!");
			}
			this.groupName = (String) attributes.getAttributeValue(COMMON_NAME_ATTR_NAME);
		}
		
		public LDAPSecurityGroup(String name, String objectClassAttr, String memberAttr) {
			this.groupName = name;
			this.objectClassAttr = objectClassAttr;
			this.memberAttr = memberAttr;
		}
		
		public String getGroupName() {
			return groupName;
		}
		
		public void setGroupName(String groupName) {
			this.groupName = groupName;
		}

		public String getMemberAttr() {
			return memberAttr;
		}
		
		public String getObjectClassAttr() {
			return objectClassAttr;
		}
	}
	
	/***********************************
	 * Attribute-StructFields synchronization section START *
	 ***********************************/

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
				attachedServer = (LDAPServer) pm.getObjectById(this.getUserManagementSystemObjectID());
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
