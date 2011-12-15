package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreLifecycleListener;

import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPUserSecurityGroupSyncConfig;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.FetchEventTypeDataUnit;
import org.nightlabs.jfire.security.integration.UserManagementSystemSyncEvent.SyncEventGenericType;
import org.nightlabs.jfire.security.integration.id.UserSecurityGroupSyncConfigID;
import org.nightlabs.util.CollectionUtil;

/**
 * JDO lifecycle listener that is configured to listen to creation and storing of {@link LDAPUserSecurityGroupSyncConfig}s.
 * It performs synchronization of authorization-related data whenever new {@link LDAPUserSecurityGroupSyncConfig} is created
 * or an existing one is stored AND has its mapping (name of LDAP group) changed.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPUserSecurityGroupSyncConfigLifecycleListener implements CreateLifecycleListener, StoreLifecycleListener{
	
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
	public void postCreate(InstanceLifecycleEvent event) {
		if (!isEnabled()){
			return;
		}
		LDAPUserSecurityGroupSyncConfig syncConfig = (LDAPUserSecurityGroupSyncConfig) event.getPersistentInstance();
		if (syncConfig.isSyncEnabled() 
				&& syncConfig.getUserManagementSystem().shouldFetchUserData()){
			execSyncInvocation(syncConfig);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void postStore(InstanceLifecycleEvent event) {
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
	public void preStore(InstanceLifecycleEvent event) {
		if (!isEnabled()){
			return;
		}
		LDAPUserSecurityGroupSyncConfig detachedSyncConfig = (LDAPUserSecurityGroupSyncConfig) event.getDetachedInstance();
		PersistenceManager pm = JDOHelper.getPersistenceManager(event.getPersistentInstance());
		if (pm != null 
				&& detachedSyncConfig != null 
				&& detachedSyncConfig.isSyncEnabled()){
			LDAPUserSecurityGroupSyncConfig attachedSyncConfig = null;
			UserSecurityGroupSyncConfigID syncConfigId = UserSecurityGroupSyncConfigID.create(
					detachedSyncConfig.getUserSecurityGroupSyncConfigID(), detachedSyncConfig.getOrganisationID());
			try{
				attachedSyncConfig = (LDAPUserSecurityGroupSyncConfig) pm.getObjectById(syncConfigId);
			}catch(JDOObjectNotFoundException e){
				// no object exist, so we assume that new LDAPUserSecurityGroupSyncConfig is created and initial sync is necessary
				syncConfigsForSync.add(syncConfigId);
				return;
			}
			if (attachedSyncConfig != null
					&& !detachedSyncConfig.getUserManagementSystemSecurityObject().equals(attachedSyncConfig.getUserManagementSystemSecurityObject())
					&& attachedSyncConfig.getUserManagementSystem().shouldFetchUserData()){
				syncConfigsForSync.add(syncConfigId);
			}
		}
	}

	private void execSyncInvocation(LDAPUserSecurityGroupSyncConfig syncConfig){
		LDAPSyncEvent syncEvent = new LDAPSyncEvent(SyncEventGenericType.FETCH_AUTHORIZATION);
		syncEvent.setFetchEventTypeDataUnits(
				CollectionUtil.createArrayList(
						new FetchEventTypeDataUnit((String) syncConfig.getUserManagementSystemSecurityObject())));
		LDAPServer ldapServer = syncConfig.getUserManagementSystem();
		try {
			LDAPSyncInvocation.executeWithPreservedLDAPConnection(
					JDOHelper.getPersistenceManager(syncConfig), new LDAPSyncInvocation(ldapServer.getUserManagementSystemObjectID(), syncEvent), ldapServer);
		} catch (AsyncInvokeEnqueueException e) {
			throw new RuntimeException(
					"Unable to enqueue Async invocation for initial sync of created LDAPUserSecurityGroupSyncConfig!", e);
		}
	}
}
