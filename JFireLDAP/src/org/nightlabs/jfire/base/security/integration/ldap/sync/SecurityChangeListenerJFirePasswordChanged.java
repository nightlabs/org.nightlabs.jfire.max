package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.script.ScriptException;
import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.asyncinvoke.ErrorCallback;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.asyncinvoke.SuccessCallback;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPManagerBean;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttributeSet;
import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPPasswordWrapper;
import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider.AuthenticationMethod;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnection;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionManager;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionWrapper.EntryModificationFlag;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserDescriptor;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.jfire.security.listener.SecurityChangeEvent_UserLocal_passwordChanged;
import org.nightlabs.jfire.security.listener.SecurityChangeListener;
import org.nightlabs.jfire.security.listener.id.SecurityChangeListenerID;
import org.nightlabs.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link SecurityChangeListener} implementation which listens for password change in JFire represented by {@link SecurityChangeEvent_UserLocal_passwordChanged}.
 * This listener is registered (persisted) by {@link LDAPManagerBean} at organisation-init so every {@link LDAPServer} could be notified
 * of password change in JFire and synchronize its entries data accordingly.
 * 
 * Password is synchronized immidiately (synchroniously) if LDAP entry exists at this exact moment or is postponed for the cases when
 * new {@link User} is created and has password set BEFORE storing (and synchronizing to LDAP). When password synchronization is postoponed
 * it makes use of push notifications via {@link PushNotificationsConfigurator} and listens to entry being added in LDAP directory.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class SecurityChangeListenerJFirePasswordChanged extends SecurityChangeListener{
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(SecurityChangeListenerJFirePasswordChanged.class);

	public static void register(PersistenceManager pm) {
		pm.getExtent(SecurityChangeListenerJFirePasswordChanged.class);
		SecurityChangeListenerID id = SecurityChangeListenerID.create(Organisation.DEV_ORGANISATION_ID, SecurityChangeListenerJFirePasswordChanged.class.getName());
		try {
			pm.getObjectById(id);
		} catch (JDOObjectNotFoundException x) {
			pm.makePersistent(new SecurityChangeListenerJFirePasswordChanged(id.organisationID, id.securityChangeListenerID));
		}
	}

	public static void unregister(PersistenceManager pm) {
		pm.getExtent(SecurityChangeListenerJFirePasswordChanged.class);
		SecurityChangeListenerID id = SecurityChangeListenerID.create(Organisation.DEV_ORGANISATION_ID, SecurityChangeListenerJFirePasswordChanged.class.getName());
		try {
			SecurityChangeListener listener = (SecurityChangeListener) pm.getObjectById(id);
			pm.deletePersistent(listener);
		} catch (JDOObjectNotFoundException x) {
			// do nothing because already unregistered
		}
	}
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SecurityChangeListenerJFirePasswordChanged() { }

	protected SecurityChangeListenerJFirePasswordChanged(String organisationID, String securityChangeListenerID) {
		super(organisationID, securityChangeListenerID);
	}

	@Override
	public void on_UserLocal_passwordChanged(SecurityChangeEvent_UserLocal_passwordChanged event) {
		User user = event.getUser();
		if (user == null){
			logger.warn("Can't proceed with password change to LDAP because User is not specified.");
			return;
		}
		if (event.getNewPassword() == null
				|| event.getNewPassword().isEmpty()){
			logger.warn("Can't proceed with password change to LDAP because new password is not specified or empty.");
			return;
		}

		PersistenceManager pm = getPersistenceManager();
		List<LDAPServer> ldapServers = new ArrayList<LDAPServer>();
		for (Iterator<LDAPServer> it = pm.getExtent(LDAPServer.class, true).iterator(); it.hasNext(); ){
			ldapServers.add(it.next());
		}
		if (logger.isDebugEnabled()){
			logger.debug(String.format("Got %s LDAPServers, will try to sync password change on every LDAPServer which has corresponding entry", ldapServers.size()));
		}
		
		Throwable lastThrowable = null;
		for (LDAPServer ldapServer : ldapServers){
			try{
				// create password wrapper for calculating hashed password and transmitting it to LDAP
				String hashAlgorithm = Util.HASH_ALGORITHM_SHA;
				if (AuthenticationMethod.SASL_CRAM_MD5.equals(ldapServer.getAuthenticationMethod())
						|| AuthenticationMethod.SASL_DIGEST_MD5.equals(ldapServer.getAuthenticationMethod())){
					hashAlgorithm = null; // CRAM_MD5 and DIGEST-MD5 support only plain text passwords
				}
				LDAPPasswordWrapper newPasswordWrapper = new LDAPPasswordWrapper(event.getNewPassword(), hashAlgorithm);
				boolean passwordChanged = modifyPassword(ldapServer, false, user, newPasswordWrapper.toString());
				if (!passwordChanged && !ldapServer.isLeading()){
					// We assume that if entry does not exist yet in LDAP directory it could mean that new User is being created in JFire
					// and its password is set BEFORE the User is made persistent. So this listener is triggered BEFORE actual entry
					// is created in LDAP directory. That's why we leave the possiblity (for non-leading LDAPServers only) to set
					// user password in LDAP after JFire User is stored and actual LDAP entry is created by synchronization.
					if (logger.isDebugEnabled()){
						logger.debug(String.format("Password was not changed yet. However we'll wait until entry is possibly created in LDAP. Adding push notification listener to LDAPServer with ID %s@%s.", ldapServer.getUserManagementSystemID(), ldapServer.getOrganisationID()));
					}

					LDAPConnection preservedConnection = null;
					if (!AuthenticationMethod.NONE.equals(ldapServer.getAuthenticationMethod())){
						// preserve LDAPConnection
						preservedConnection = preserveConnection(pm, ldapServer, PRESERVED_CONNECTION_KEY + user.getCompleteUserID());
						if (preservedConnection == null){
							logger.warn("LDAPConnection was not preserved for later usage when changing user password in LDAP!");
						}
					}
					
					try{
						PushNotificationsConfigurator.sharedInstance().addPushNotificationListener(
								ldapServer, 
								new LDAPEntryAddedListener(
										user, newPasswordWrapper.toString(), ldapServer.getLdapScriptSet().getLdapDN(user)),
								ldapServer.getLdapScriptSet().getUserParentEntriesForSync());
					}catch(Exception e){
						LDAPConnectionManager.sharedInstance().releaseConnection(preservedConnection);
						throw e;
					}
				}
			}catch(Exception e){
				lastThrowable = e;
				logger.error(
						String.format(
								"Exception occured while trying to sync password to LDAPServer at %s:%s! Will continue with next LDAPServer.", ldapServer.getHost(), ldapServer.getPort()), e);
			}
		}
		if (lastThrowable != null){
			throw new RuntimeException(
					"Exception(s) occured during password change in LDAP! Please see server log for details. Last one was " + lastThrowable.getMessage(), lastThrowable);
		}
		super.on_UserLocal_passwordChanged(event);
	}
	
	private static boolean modifyPassword(LDAPServer ldapServer, boolean usePreservedConnection, User user, String newPasswordHashed) throws UserManagementSystemCommunicationException, LoginException, ScriptException, NoSuchMethodException{
		LDAPConnection connection = null;
		try{
			if (logger.isDebugEnabled()){
				logger.debug(
						String.format(
								"Trying to sync password to LDAPServer at %s:%s, object ID: %s@%s", ldapServer.getHost(), ldapServer.getPort(), ldapServer.getUserManagementSystemID(), ldapServer.getOrganisationID()));
			}
			
			if (usePreservedConnection){
				connection = LDAPConnectionManager.sharedInstance().getPrivateLDAPConnection(ldapServer, PRESERVED_CONNECTION_KEY + user.getCompleteUserID());
			}else{
				connection = ldapServer.getConnectionForSync();
			}

			String entryDN = ldapServer.getLdapScriptSet().getLdapDN(user);
			if (logger.isDebugEnabled()){
				logger.debug("Check if LDAP entry exists with name " + entryDN);
			}

			boolean entryExists = connection.entryExists(entryDN);
			if (logger.isDebugEnabled()){
				logger.debug(String.format("Entry %s does%s exist.", entryDN, entryExists ? "" : " not"));
			}

			if (entryExists){
				String pwdAttributeName = ldapServer.getLdapScriptSet().getUserPasswordAttributeName();
				
				LDAPAttributeSet attributes = new LDAPAttributeSet();
				attributes.createAttribute(pwdAttributeName, newPasswordHashed);
				if (logger.isDebugEnabled()){
					logger.debug(String.format("Modifying entry %s with a new password", entryDN));
				}
				connection.modifyEntry(entryDN, attributes, EntryModificationFlag.MODIFY);
				if (logger.isDebugEnabled()){
					logger.debug(String.format("New password successfully set to entry %s", entryDN));
				}
				return true;
			}
			if (logger.isDebugEnabled()){
				logger.debug(String.format("New password was NOT set to entry %s because it does not exist.", entryDN));
			}
			return false;
		}finally{
			LDAPConnectionManager.sharedInstance().releaseConnection(connection);
		}
	}
	
	private static LDAPConnection preserveConnection(PersistenceManager pm, LDAPServer ldapServer, String key){
		UserDescriptor userDescriptor = null;
		LDAPConnection connection = null;
		
		try{
			userDescriptor = GlobalSecurityReflector.sharedInstance().getUserDescriptor();
		}catch(NoUserException e){
			logger.error("No User logged in while preserving LDAPConnection!");
			return null;
		}
		
		boolean exceptionOccured = false;
		try {
			connection = LDAPConnectionManager.sharedInstance().getConnection(ldapServer);
			
			User user = null;
			if (pm != null){
				user = userDescriptor.getUser(pm);
			}else{	// create fake, not persisted new User just to get LDAP entry DN
				user = new User(userDescriptor.getOrganisationID(), userDescriptor.getUserID());
			}
			ldapServer.bindConnection(connection, user, LDAPServer.getLDAPPasswordForCurrentUser());
			
			LDAPConnectionManager.sharedInstance().preservePrivateLDAPConnection(key, connection);
			
			return connection;
			
		} catch (UserManagementSystemCommunicationException e) {
			exceptionOccured = true;
			logger.error("Exception while trying to preserve a connection! No connection will be available inside sync invocation!", e);
		} catch (LoginException e) {
			exceptionOccured = true;
			logger.error("LDAPConnection could not be authenticated! No connection will be available inside sync invocation!", e);
		} catch (Exception e){
			exceptionOccured = true;
			throw new RuntimeException(e);
		} finally {
			if (exceptionOccured){
				LDAPConnectionManager.sharedInstance().releaseConnection(connection);
			}
		}
		return null;
	}
	
	/**
	 * This class is intended to be used by unit-tests only!
	 * 
	 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
	 *
	 */
	public static class TestAsyncInvokeCallbacksTranslator{
		
		private static TestAsyncInvokeCallbacksTranslator sharedInstance;

		private TestAsyncInvokeCallbacksTranslator() {}
		
		public static TestAsyncInvokeCallbacksTranslator sharedInstance(){
			if (sharedInstance == null) {
				synchronized (TestAsyncInvokeCallbacksTranslator.class) {
					if (sharedInstance == null)
						sharedInstance = new TestAsyncInvokeCallbacksTranslator();
				}
			}
			return sharedInstance;
		}

		private SuccessCallback successCallback;
		private ErrorCallback errorCallback;
		
		public void setSuccessCallback(SuccessCallback successCallback) {
			this.successCallback = successCallback;
		}
		
		public void setErrorCallback(ErrorCallback errorCallback) {
			this.errorCallback = errorCallback;
		}
		
		public SuccessCallback getSuccessCallback() {
			return successCallback;
		}
		
		public ErrorCallback getErrorCallback() {
			return errorCallback;
		}
	}
	
	class LDAPEntryAddedListener implements NamespaceChangeListener{
		
		private User user;
		private String newPasswordHashed;
		private String ldapEntryName;
		
		public LDAPEntryAddedListener(
				User user, String newPassword, String ldapEntryName) {
			this.user = user;
			this.newPasswordHashed = newPassword;
			this.ldapEntryName = ldapEntryName;
		}

		@Override
		public void objectAdded(NamingEvent event) {
			if (event.getEventContext() == null){
				logger.error("Can't proceed with password change because source EventContext is null!");
				return;
			}
			if (event.getNewBinding() == null 
					|| event.getNewBinding().getNameInNamespace() == null 
					|| event.getNewBinding().getNameInNamespace().isEmpty()
					|| ldapEntryName == null
					|| ldapEntryName.isEmpty()){
				logger.error("Can't proceed with password change because entry name is not specified!");
				return;
			}
			if (!ldapEntryName.equals(event.getNewBinding().getNameInNamespace())){
				logger.info("Can't proceed with password change because added entry name does not match User entry name.");
				return;
			}
			
			UserManagementSystemID ldapServerID = PushNotificationsConfigurator.sharedInstance().getLDAPServerIDByEventContext(event.getEventContext());
			if (ldapServerID == null){
				logger.error("Can't proceed with password change because corresponding LDAPServer is not found!");
				return;
			}
			
			if (logger.isDebugEnabled()){
				logger.debug(String.format("Entry was added in LDAP. Trying launch async invocation for password change in LDAP for user %s@%s.", user.getUserID(), user.getOrganisationID()));
			}
			
			PushNotificationsConfigurator.sharedInstance().removePushNotificationListener(ldapServerID, this);
			
			try {
				AsyncInvoke.exec(
						new LDAPChangePasswordInvocation(ldapServerID, user, newPasswordHashed, event.getNewBinding().getNameInNamespace()),
						TestAsyncInvokeCallbacksTranslator.sharedInstance().getSuccessCallback(),
						TestAsyncInvokeCallbacksTranslator.sharedInstance().getErrorCallback(),
						null, false);
			} catch (AsyncInvokeEnqueueException e) {
				throw new RuntimeException(
						String.format(
								"Could not enqueque Async Invocation for changing password in LDAP! LDAP password for user %s@%s is not changed, please change it manually.", user.getUserID(), user.getOrganisationID()), e);
			}
		}
	
		@Override
		public void namingExceptionThrown(NamingExceptionEvent event) {
			String errorMessage = "Error recieved from LDAPServer while waiting for password change! Password will not be changed, so please do it manually.";
			if (event.getEventContext() != null){
				UserManagementSystemID ldapServerID = PushNotificationsConfigurator.sharedInstance().getLDAPServerIDByEventContext(event.getEventContext());
				if (ldapServerID != null){
					errorMessage += " LDAPServer ID: " + ldapServerID.toString();
				}
			}
			throw new RuntimeException(errorMessage, event.getException());
		}

		@Override
		public void objectRemoved(NamingEvent event) {
			// do nothing
		}

		@Override
		public void objectRenamed(NamingEvent event) {
			// do nothing
		}
		
	}
	
	private static final String PRESERVED_CONNECTION_KEY = "passwordChangedFor_";

	class LDAPChangePasswordInvocation extends Invocation{

		private static final long serialVersionUID = 1L;

		private UserManagementSystemID ldapServerID;
		private User user;
		private String newPasswordHashed;
		private String addedEntryName;

		public LDAPChangePasswordInvocation(UserManagementSystemID ldapServerID, User user, String newPassword, String addedEntryName) {
			this.ldapServerID = ldapServerID;
			this.user = user;
			this.newPasswordHashed = newPassword;
			this.addedEntryName = addedEntryName;
		}

		@Override
		public Serializable invoke() throws Exception {
			
			if (logger.isDebugEnabled()){
				logger.debug(String.format("Invoking password change in LDAP for user %s@%s.", user.getUserID(), user.getOrganisationID()));
			}
			PersistenceManager pm = createPersistenceManager();
			try{
				
				LDAPServer ldapServer = null;
				try {
					ldapServer = (LDAPServer) pm.getObjectById(ldapServerID);
				}catch(JDOObjectNotFoundException e){
					logger.error(String.format("LDAPServer with ID %s not found! Cannot proceed with password change!", ldapServerID.toString()));
					return null;
				}
				
				if (logger.isDebugEnabled()){
					logger.debug(String.format("Got LDAPServer, will check if User %s@%s matches added LDAP entry %s", user.getUserID(), user.getOrganisationID(), addedEntryName));
				}
				
				if (addedEntryName.equals(ldapServer.getLdapScriptSet().getLdapDN(user))){
					
					if (logger.isDebugEnabled()){
						logger.debug(String.format("User and entry name are correct, modifying LDAP entry with new password for user %s@%s", user.getUserID(), user.getOrganisationID()));
					}
					modifyPassword(ldapServer, true, user, newPasswordHashed);
					return null;
				}
				
				throw new RuntimeException("Password was NOT changed! Please do it manually. Entry name does not match to User name.");
				
			}catch(Exception e){
				throw new RuntimeException(
						String.format(
								"Can't change LDAP password for user %s@%s! Please change it manually.", user.getUserID(), user.getOrganisationID()), e);
			}finally{
				pm.close();
			}
		}
		
	}
}