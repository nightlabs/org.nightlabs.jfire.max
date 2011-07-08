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

import org.apache.log4j.Logger;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.asyncinvoke.ErrorCallback;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.asyncinvoke.SuccessCallback;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPManagerBean;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttributeSet;
import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPPasswordWrapper;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnection;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionManager;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionWrapper.EntryModificationFlag;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.jfire.security.listener.SecurityChangeEvent_UserLocal_passwordChanged;
import org.nightlabs.jfire.security.listener.SecurityChangeListener;
import org.nightlabs.jfire.security.listener.id.SecurityChangeListenerID;
import org.nightlabs.util.Util;

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
	
	private static final Logger logger = Logger.getLogger(SecurityChangeListenerJFirePasswordChanged.class);

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

		if (logger.isDebugEnabled()){
			logger.debug("Getting all persistent LDAPServers via extent");
		}
		
		PersistenceManager pm = getPersistenceManager();
		List<LDAPServer> ldapServers = new ArrayList<LDAPServer>();
		for (Iterator<LDAPServer> it = pm.getExtent(LDAPServer.class, true).iterator(); it.hasNext(); ){
			ldapServers.add(it.next());
		}
		if (logger.isDebugEnabled()){
			logger.debug(String.format("Got %s LDAPServers, will try to sync password change on every LDAPServer which has corresponding entry", ldapServers.size()));
		}
		
		// create password wrapper for calculating hashed password and transmitting it to LDAP
		LDAPPasswordWrapper newPasswordWrapper = new LDAPPasswordWrapper(Util.HASH_ALGORITHM_SHA, event.getNewPassword());
		
		boolean exceptionOccured = false;
		for (LDAPServer ldapServer : ldapServers){
			try{
				boolean passwordChanged = modifyPassword(ldapServer, user, newPasswordWrapper.toString());
				if (!passwordChanged && !ldapServer.isLeading()){
					// We assume that if entry does not exist yet in LDAP directory it could mean that new User is being created in JFire
					// and its password is set BEFORE the User is made persistent. So this listener is triggered BEFORE actual entry
					// is created in LDAP directory. That's why we leave the possiblity (for non-leading LDAPServers only) to set
					// user password in LDAP after JFire User is stored and actual LDAP entry is created by synchronization.
					if (logger.isDebugEnabled()){
						logger.debug(String.format("Password was not changed yet. However we'll wait until entry is possibly created in LDAP. Adding push notification listener to LDAPServer with ID %s@%s.", ldapServer.getUserManagementSystemID(), ldapServer.getOrganisationID()));
					}
					PushNotificationsConfigurator.sharedInstance().addPushNotificationListener(
							ldapServer, new LDAPEntryAddedListener(user, newPasswordWrapper.toString(), ldapServer.getLdapScriptSet().getLdapDN(user)));
				}
			}catch(Exception e){
				exceptionOccured = true;
				logger.error(
						String.format(
								"Exception occured while trying to sync password to LDAPServer at %s:%s! Will contibue with next LDAPServer.", ldapServer.getHost(), ldapServer.getPort()), e);
			}
		}
		if (exceptionOccured){
			throw new RuntimeException("Exception(s) occured during password change in LDAP! Please see server log for details.");
		}
	}
	
	private boolean modifyPassword(LDAPServer ldapServer, User user, String newPasswordHashed) throws UserManagementSystemCommunicationException, LoginException, ScriptException{
		LDAPConnection connection = null;
		try{
			if (logger.isDebugEnabled()){
				logger.debug(
						String.format(
								"Trying to sync password to LDAPServer at %s:%s, object ID: %s@%s", ldapServer.getHost(), ldapServer.getPort(), ldapServer.getUserManagementSystemID(), ldapServer.getOrganisationID()));
			}
			
			connection = LDAPConnectionManager.sharedInstance().getConnection(ldapServer);
			ldapServer.bindForSynchronization(connection);

			String entryDN = ldapServer.getLdapScriptSet().getLdapDN(user);
			if (logger.isDebugEnabled()){
				logger.debug("Check if LDAP entry exists with name " + entryDN);
			}
			boolean entryExists = false;
			try{
				connection.getAttributesForEntry(entryDN);
				entryExists = true;
			}catch(UserManagementSystemCommunicationException e){
				entryExists = false;
			}
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
			if (connection != null){
				connection.unbind();
			}
			LDAPConnectionManager.sharedInstance().releaseConnection(connection);
		}
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
		
		public LDAPEntryAddedListener(User user, String newPassword, String ldapEntryName) {
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
				
				LDAPServer ldapServer = (LDAPServer) pm.getObjectById(ldapServerID);
				if (logger.isDebugEnabled()){
					logger.debug(String.format("Got LDAPServer, will check if User %s@%s matches added LDAP entry %s", user.getUserID(), user.getOrganisationID(), addedEntryName));
				}
				
				if (addedEntryName.equals(ldapServer.getLdapScriptSet().getLdapDN(user))){
					
					if (logger.isDebugEnabled()){
						logger.debug(String.format("User and entry name are correct, modifying LDAP entry with new password for user %s@%s", user.getUserID(), user.getOrganisationID()));
					}
					modifyPassword(ldapServer, user, newPasswordHashed);
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