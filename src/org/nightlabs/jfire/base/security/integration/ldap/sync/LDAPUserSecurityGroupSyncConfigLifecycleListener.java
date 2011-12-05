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

public class LDAPUserSecurityGroupSyncConfigLifecycleListener implements CreateLifecycleListener, StoreLifecycleListener{
	
	private static ThreadLocal<Boolean> isEnabledTL = new ThreadLocal<Boolean>(){
		protected Boolean initialValue() {
			return true;
		};
	};
	
	private Set<UserSecurityGroupSyncConfigID> syncConfigsForInitialSync = new HashSet<UserSecurityGroupSyncConfigID>();

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

	@Override
	public void postCreate(InstanceLifecycleEvent event) {
		if (!isEnabled()){
			return;
		}
		LDAPUserSecurityGroupSyncConfig persistentInstance = (LDAPUserSecurityGroupSyncConfig) event.getPersistentInstance();
		if (persistentInstance.getUserManagementSystem().shouldFetchUserData()){
			execSyncInvocation(persistentInstance);
		}
	}
	
	@Override
	public void postStore(InstanceLifecycleEvent event) {
		if (!isEnabled()){
			return;
		}
		LDAPUserSecurityGroupSyncConfig syncConfig = (LDAPUserSecurityGroupSyncConfig) event.getPersistentInstance();
		UserSecurityGroupSyncConfigID syncConfigId = UserSecurityGroupSyncConfigID.create(
				syncConfig.getUserSecurityGroupSyncConfigID(), syncConfig.getOrganisationID());
		if (syncConfigsForInitialSync.contains(syncConfigId)){
			try{
				execSyncInvocation(syncConfig);
			}finally{
				syncConfigsForInitialSync.remove(syncConfigId);
			}
		}
	}

	@Override
	public void preStore(InstanceLifecycleEvent event) {
		if (!isEnabled()){
			return;
		}
		LDAPUserSecurityGroupSyncConfig syncConfig = (LDAPUserSecurityGroupSyncConfig) event.getDetachedInstance();
		PersistenceManager pm = JDOHelper.getPersistenceManager(event.getPersistentInstance());
		if (pm != null){
			LDAPUserSecurityGroupSyncConfig attachedSyncConfig = null;
			UserSecurityGroupSyncConfigID syncConfigId = UserSecurityGroupSyncConfigID.create(
					syncConfig.getUserSecurityGroupSyncConfigID(), syncConfig.getOrganisationID());
			try{
				attachedSyncConfig = (LDAPUserSecurityGroupSyncConfig) pm.getObjectById(syncConfigId);
			}catch(JDOObjectNotFoundException e){
				// no object exist, so we assume that new LDAPUserSecurityGroupSyncConfig is created and initial sync is necessary
				syncConfigsForInitialSync.add(syncConfigId);
				return;
			}
			if (attachedSyncConfig != null
					&& !syncConfig.getUserManagementSystemSecurityObject().equals(attachedSyncConfig.getUserManagementSystemSecurityObject())
					&& attachedSyncConfig.getUserManagementSystem().shouldFetchUserData()){
				syncConfigsForInitialSync.add(syncConfigId);
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
