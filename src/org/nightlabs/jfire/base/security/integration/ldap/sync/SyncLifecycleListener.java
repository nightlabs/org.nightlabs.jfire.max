package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.JDOUserCallbackException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreLifecycleListener;
import javax.script.ScriptException;

import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPUserSecurityGroupSyncConfig;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.SendEventTypeDataUnit;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.UserManagementSystemSyncEvent.SyncEventGenericType;
import org.nightlabs.jfire.security.integration.UserManagementSystemSyncEvent.SyncEventType;
import org.nightlabs.jfire.security.integration.UserSecurityGroupSyncConfig;
import org.nightlabs.jfire.security.integration.UserSecurityGroupSyncConfigContainer;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link JDOLifecycleListener} used for calling {@link LDAPSyncInvocation} for synchronization from JFire to LDAP 
 * when JFire is a leading system.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class SyncLifecycleListener implements StoreLifecycleListener, DeleteLifecycleListener{

	private static final Logger logger = LoggerFactory.getLogger(SyncLifecycleListener.class);

	private static ThreadLocal<Boolean> isEnabledTL = new ThreadLocal<Boolean>(){
		protected Boolean initialValue() {
			return true;
		};
	};
	
	/**
	 * Enable/disable this listener. If it's disabled than it will not exec AsyncInvoke for synchronization.
	 * 
	 * IMPORTANT! When you want to disable listener and then persist your objects you SHOULD always call to
	 * pm.flush() BEFORE enabling listener back. Otherwise there's a big chance that pm will be flushed 
	 * somewhere else AFTER you enable listener and it will be triggered causing unexpected behaviour.  
	 * 
	 * @param isEnabled
	 */
	public static void setEnabled(boolean isEnabled) {
		isEnabledTL.set(isEnabled);
	}
	
	/**
	 * 
	 * @return if this listener is enabled and will exec AsyncInvokes
	 */
	public static boolean isEnabled(){
		return isEnabledTL.get();
	}
	
	Map<UserManagementSystemID, Collection<SendEventTypeDataUnit>> ldapServerId2sendEventDataUnits = new HashMap<UserManagementSystemID, Collection<SendEventTypeDataUnit>>();
	
	@Override
	public void postStore(InstanceLifecycleEvent event) {
		Object persistentInstance = event.getPersistentInstance();
		execSyncInvocation(persistentInstance, getSyncEventType(persistentInstance, false));
	}

	@Override
	public void postDelete(InstanceLifecycleEvent event) {
		Object persistentInstance = event.getPersistentInstance();
		execSyncInvocation(persistentInstance, getSyncEventType(persistentInstance, true));
	}
	
	@Override
	public void preDelete(InstanceLifecycleEvent event) {
		if (!isEnabled()){
			return;
		}
		// when JFire object is about to be deleted we should get corresponding LDAP entry name 
		// to be able to remove it from directory later when postDelete is called
		Object jfireObject = event.getPersistentInstance();
		Collection<LDAPServer> ldapServers = UserManagementSystem.getUserManagementSystemsByLeading(
				JDOHelper.getPersistenceManager(jfireObject), false, LDAPServer.class
				);
		Object objectId = JDOHelper.getObjectId(jfireObject);
		for (LDAPServer ldapServer : ldapServers) {
			UserManagementSystemID ldapServerId = ldapServer.getUserManagementSystemObjectID();
			synchronized (ldapServerId2sendEventDataUnits) {
				String ldapDN = getLDAPNameForPersistentInstance(ldapServer, jfireObject);
				if (ldapDN == null || ldapDN.isEmpty()){
					logger.info(
							String.format("No LDAP name on LDAPSerever %s for object being deleted %s. Skipping it.", ldapServerId, objectId));
					continue;
				}
				SendEventTypeDataUnit sendEventTypeDataUnit = new SendEventTypeDataUnit(objectId, ldapDN);
				if (ldapServerId2sendEventDataUnits.get(ldapServerId) != null){
					ldapServerId2sendEventDataUnits.get(ldapServerId).add(sendEventTypeDataUnit);
				}else{
					Collection<SendEventTypeDataUnit> dataUnits = new HashSet<SendEventTypeDataUnit>();
					dataUnits.add(sendEventTypeDataUnit);
					ldapServerId2sendEventDataUnits.put(ldapServerId, dataUnits);
				}
			}
		}
	}

	@Override
	public void preStore(InstanceLifecycleEvent event) {
		// do nothing
	}
	
	private void execSyncInvocation(Object persistentInstance, SyncEventType eventType) {

		if (!isEnabled()){
			return;
		}
		
		PersistenceManager pm = JDOHelper.getPersistenceManager(persistentInstance);
		
		// Determine if JFire is a leading system for at least one existent LDAPServer, 
		// therefore we query all NON leading LDAPServers.
		Collection<LDAPServer> nonLeadingLdapServers = UserManagementSystem.getUserManagementSystemsByLeading(
				pm, false, LDAPServer.class
				);
		if (!nonLeadingLdapServers.isEmpty()){

			// If object being stored/deleted is a Person than we proceed with synchronization 
			// ONLY if this Person is NOT related to any User object - in this case we consider
			// this Person to be a separate entry in LDAP. If Person is related to at least one User
			// we consider that all Person data will be synchronized to LDAP when storing/deleting corresponding
			// User object. Denis.
			if (persistentInstance instanceof Person){
				Person person = (Person) persistentInstance;
				javax.jdo.Query q = pm.newQuery(User.class);
				try{
					q.setResult("JDOHelper.getObjectId(this)");
					q.setFilter("this.person == :person");
					
					@SuppressWarnings("unchecked")
					Collection<UserID> userIds = (Collection<UserID>) q.execute(person);
					
					if (userIds != null 
							&& !userIds.isEmpty()){
						logger.info("Person being stored/deleted is related to at least one User and therefore this Person will NOT be synchonized to LDAP.");
						return;
					}
					
				}finally{
					q.closeAll();
				}
			}
			
			Throwable lastThrowable = null;
			for (LDAPServer ldapServer : nonLeadingLdapServers){
				
				Object jfireObjectId = JDOHelper.getObjectId(persistentInstance);
				UserManagementSystemID ldapServerId = ldapServer.getUserManagementSystemObjectID();
				SendEventTypeDataUnit dataUnit = null;
				if ((SyncEventGenericType.UMS_REMOVE_USER == eventType || SyncEventGenericType.UMS_REMOVE_AUTHORIZATION == eventType)
						&& ldapServerId2sendEventDataUnits.get(ldapServerId) != null){
					synchronized (ldapServerId2sendEventDataUnits) {
						for (SendEventTypeDataUnit unit : ldapServerId2sendEventDataUnits.get(ldapServerId)){
							if (jfireObjectId.equals(unit.getJfireObjectId())){
								dataUnit = unit;
								break;
							}
						}
						ldapServerId2sendEventDataUnits.get(ldapServerId).remove(dataUnit);
						if (ldapServerId2sendEventDataUnits.get(ldapServerId).isEmpty()){
							ldapServerId2sendEventDataUnits.remove(ldapServerId);
						}
					}
				}
				if (dataUnit == null){
					dataUnit = new SendEventTypeDataUnit(jfireObjectId);
				}
				
				LDAPSyncEvent syncEvent = new LDAPSyncEvent(eventType);
				syncEvent.setSendEventTypeDataUnits(CollectionUtil.createHashSet(dataUnit));
				try {
					LDAPSyncInvocation.executeWithPreservedLDAPConnection(
							pm, new LDAPSyncInvocation(ldapServerId, syncEvent), ldapServer);
				} catch (AsyncInvokeEnqueueException e) {
					lastThrowable = e;
					logger.error(e.getMessage(), e);
				}
			}
			
			if (lastThrowable != null){
				throw new JDOUserCallbackException(
						"Unable to synhronize to some LDAP server(s)! Please see log for details. Last exception was " + lastThrowable.getMessage(), lastThrowable);
			}
		}
	}
	
	private static String getLDAPNameForPersistentInstance(LDAPServer ldapServer, Object persistentInstance){
		if (persistentInstance instanceof User
				|| persistentInstance instanceof Person){
			try{
				return ldapServer.getLdapScriptSet().getLdapDN(persistentInstance);
			}catch(ScriptException e){
				logger.error(
						String.format("Can't calculate ldapDN for object with ID %s", JDOHelper.getObjectId(persistentInstance)), e);
				return null;
			}
		}else if (persistentInstance instanceof UserSecurityGroup){
			UserSecurityGroup userSecurityGroup = (UserSecurityGroup) persistentInstance;
			UserSecurityGroupID userSecurityGroupId = UserSecurityGroupID.create(
					userSecurityGroup.getOrganisationID(), userSecurityGroup.getUserSecurityGroupID());
			
			UserSecurityGroupSyncConfigContainer syncConfigContainer = UserSecurityGroupSyncConfigContainer.getSyncConfigContainerForGroup(JDOHelper.getPersistenceManager(userSecurityGroup), userSecurityGroupId);
			if (syncConfigContainer != null){
				UserSecurityGroupSyncConfig<?, ?> syncConfigForGroup = syncConfigContainer.getSyncConfigForUserManagementSystem(ldapServer.getUserManagementSystemObjectID());
				if (syncConfigForGroup instanceof LDAPUserSecurityGroupSyncConfig){
					return (String) syncConfigForGroup.getUserManagementSystemSecurityObject();
				}
			}
			return null;
		}else{
			logger.warn(
					"Can not get LDAP name for object cause it is not either User/Person or UserSecurityGroup! Instead it is " + persistentInstance.getClass().getName());
			return null;
		}
	}

	private static SyncEventType getSyncEventType(Object persistentInstance, boolean isRemove){
		if (persistentInstance instanceof User
				|| persistentInstance instanceof Person){
			return isRemove ? SyncEventGenericType.UMS_REMOVE_USER : SyncEventGenericType.SEND_USER;
		}else if (persistentInstance instanceof UserSecurityGroup){
			return isRemove ? SyncEventGenericType.UMS_REMOVE_AUTHORIZATION : SyncEventGenericType.SEND_AUTHORIZATION;
		}else{
			throw new UnsupportedOperationException(
					"Persistent instance should be either User/Person or UserSecurityGroup! Instead it is " + persistentInstance.getClass().getName());
		}
	}

}