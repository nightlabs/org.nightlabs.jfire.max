package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOUserCallbackException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.AttachLifecycleListener;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.security.auth.login.LoginException;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPUserSecurityGroupSyncConfig;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnection;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionManager;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.FetchEventTypeDataUnit;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.jfire.security.NoUserException;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserDescriptor;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.nightlabs.jfire.security.integration.UserManagementSystemSyncEvent.SyncEventGenericType;
import org.nightlabs.jfire.security.integration.id.UserSecurityGroupSyncConfigID;
import org.nightlabs.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDO lifecycle listener that is configured to listen for creation and attaching of {@link LDAPUserSecurityGroupSyncConfig}s.
 * It performs synchronization of authorization-related data whenever new {@link LDAPUserSecurityGroupSyncConfig} is created
 * or an existing one is attached back to datastore AND has its mapping (name of LDAP group) changed.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPUserSecurityGroupSyncConfigLifecycleListener implements AttachLifecycleListener, CreateLifecycleListener{
	
	private static final Logger logger = LoggerFactory.getLogger(LDAPUserSecurityGroupSyncConfigLifecycleListener.class);
	
	private static ThreadLocal<Boolean> isEnabledTL = new ThreadLocal<Boolean>(){
		protected Boolean initialValue() {
			return true;
		};
	};
	
	/**
	 * Holds IDs of {@link LDAPUserSecurityGroupSyncConfig}s which should be synchronized. 
	 * Added by {@link #preStore(InstanceLifecycleEvent)} and removed by {@link #postStore(InstanceLifecycleEvent)}.
	 */
	private Set<UserSecurityGroupSyncConfigID> syncConfigsForSync = new HashSet<UserSecurityGroupSyncConfigID>();

	/**
	 * Enable/disable this listener.
	 * 
	 * @param isEnabled
	 */
	public static void setEnabled(boolean isEnabled) {
		isEnabledTL.set(isEnabled);
	}
	
	/**
	 * 
	 * @return <code>true</code> if this listener is enabled
	 */
	public static boolean isEnabled(){
		return isEnabledTL.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postAttach(InstanceLifecycleEvent event) {
		if (!isEnabled()){
			return;
		}
		LDAPUserSecurityGroupSyncConfig syncConfig = (LDAPUserSecurityGroupSyncConfig) event.getPersistentInstance();
		if (!syncConfig.isSyncEnabled()){
			return;
		}
		UserSecurityGroupSyncConfigID syncConfigId = UserSecurityGroupSyncConfigID.create(
				syncConfig.getUserSecurityGroupSyncConfigID(), syncConfig.getOrganisationID());
		if (syncConfigsForSync.contains(syncConfigId)){
			try{
				execSyncInvocation(syncConfig);
			}finally{
				syncConfigsForSync.remove(syncConfigId);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preAttach(InstanceLifecycleEvent event) {
		if (!isEnabled()){
			return;
		}
		LDAPUserSecurityGroupSyncConfig detachedSyncConfig = (LDAPUserSecurityGroupSyncConfig) event.getDetachedInstance();
		if (detachedSyncConfig != null
				&& detachedSyncConfig.isSyncEnabled()){
			UserSecurityGroupSyncConfigID syncConfigId = UserSecurityGroupSyncConfigID.create(
					detachedSyncConfig.getUserSecurityGroupSyncConfigID(), detachedSyncConfig.getOrganisationID());
			PersistenceManager pm = NLJDOHelper.getThreadPersistenceManager();
			LDAPUserSecurityGroupSyncConfig attachedSyncConfig = null;
			try{
				attachedSyncConfig = (LDAPUserSecurityGroupSyncConfig) pm.getObjectById(syncConfigId);
			}catch(JDOObjectNotFoundException e){
				// no object exist, so we assume that new LDAPUserSecurityGroupSyncConfig is created and initial sync is necessary
				if (detachedSyncConfig.getUserManagementSystem().shouldFetchUserData()){	// assume that userManagementSystem field would be present in new detached instance
					syncConfigsForSync.add(syncConfigId);
				}
				return;
			}
			if (attachedSyncConfig != null
					&& !detachedSyncConfig.getUserManagementSystemSecurityObject().equals(attachedSyncConfig.getUserManagementSystemSecurityObject())
					&& attachedSyncConfig.getUserManagementSystem().shouldFetchUserData()){
				syncConfigsForSync.add(syncConfigId);
			}
		}
	}

	@Override
	public void postCreate(InstanceLifecycleEvent event) {
		if (!isEnabled()){
			return;
		}
		LDAPUserSecurityGroupSyncConfig syncConfig = (LDAPUserSecurityGroupSyncConfig) event.getPersistentInstance();
		if (!syncConfig.isSyncEnabled()){
			return;
		}
		execSyncInvocation(syncConfig);
	}

	private void execSyncInvocation(LDAPUserSecurityGroupSyncConfig syncConfig){
		String ldapEntryName = (String) syncConfig.getUserManagementSystemSecurityObject();
		LDAPServer ldapServer = syncConfig.getUserManagementSystem();
		PersistenceManager pm = JDOHelper.getPersistenceManager(syncConfig);
		try {
			if (entryExists(pm, ldapServer, ldapEntryName)){
				LDAPSyncEvent syncEvent = new LDAPSyncEvent(SyncEventGenericType.FETCH_AUTHORIZATION);
				syncEvent.setFetchEventTypeDataUnits(
						CollectionUtil.createArrayList(
								new FetchEventTypeDataUnit(ldapEntryName)));
				try {
					LDAPSyncInvocation.executeWithPreservedLDAPConnection(pm, syncEvent, ldapServer);
				} catch (AsyncInvokeEnqueueException e) {
					throw new RuntimeException(
							"Unable to enqueue Async invocation for initial sync of created LDAPUserSecurityGroupSyncConfig!", e);
				}
			}
		} catch (LoginException e) {
			throw new JDOUserCallbackException(
					String.format("Login exception occured while trying to check for %s entry existance! Can't proceed with sync because the same exception will occur anyway.", ldapEntryName), e);
		}
	}

	private boolean entryExists(PersistenceManager pm, LDAPServer ldapServer, String entryName) throws LoginException{
		UserDescriptor userDescriptor = null;
		LDAPConnection connection = null;
		try{
			userDescriptor = GlobalSecurityReflector.sharedInstance().getUserDescriptor();
		}catch(NoUserException e){
			throw new IllegalStateException("No User is logged in! Makes no sense as no Invocation could be executed with no User.");
		}
		try {
			connection = LDAPConnectionManager.sharedInstance().getConnection(ldapServer);
			User user = null;
			if (pm != null){
				user = userDescriptor.getUser(pm);
			}else{	// create fake, not persisted new User just to get LDAP entry DN
				user = new User(userDescriptor.getOrganisationID(), userDescriptor.getUserID());
			}
			ldapServer.bindConnection(connection, user, LDAPServer.getLDAPPasswordForCurrentUser());
			return connection.entryExists(entryName);
		} catch (UserManagementSystemCommunicationException e) {
			logger.error(e.getMessage(), e);
			return false;
		} catch (LoginException e) {
			throw e;
		} finally {
			LDAPConnectionManager.sharedInstance().releaseConnection(connection);
		}
	}
}
