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

import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.base.jdo.notification.JDOLifecycleListener;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.LDAPSyncEventType;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.SendEventTypeDataUnit;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link JDOLifecycleListener} used for calling {@link SyncToLDAPServerInvocation} for synchronization from JFire to LDAP when 
 * JFire is a leading system.
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
		execSyncInvocation(event.getPersistentInstance(), LDAPSyncEventType.SEND);
	}

	@Override
	public void postDelete(InstanceLifecycleEvent event) {
		execSyncInvocation(event.getPersistentInstance(), LDAPSyncEventType.SEND_DELETE);
	}
	
	@Override
	public void preDelete(InstanceLifecycleEvent event) {
		// when JFire object is about to be deleted we should get corresponding LDAP entry name 
		// to be able to remove it from directory later when postDelete is called
		Object jfireObject = event.getPersistentInstance();
		Collection<LDAPServer> ldapServers = UserManagementSystem.getUserManagementSystemsByLeading(
				JDOHelper.getPersistenceManager(jfireObject), false, LDAPServer.class
				);
		Object objectId = JDOHelper.getObjectId(jfireObject);
		for (LDAPServer ldapServer : ldapServers) {
			try{
				UserManagementSystemID ldapServerId = ldapServer.getUserManagementSystemObjectID();
				synchronized (ldapServerId2sendEventDataUnits) {
					String ldapDN = ldapServer.getLdapScriptSet().getLdapDN(jfireObject);
					SendEventTypeDataUnit sendEventTypeDataUnit = new SendEventTypeDataUnit(objectId, ldapDN);
					if (ldapServerId2sendEventDataUnits.get(ldapServerId) != null){
						ldapServerId2sendEventDataUnits.get(ldapServerId).add(sendEventTypeDataUnit);
					}else{
						Collection<SendEventTypeDataUnit> dataUnits = new HashSet<SendEventTypeDataUnit>();
						dataUnits.add(sendEventTypeDataUnit);
						ldapServerId2sendEventDataUnits.put(ldapServerId, dataUnits);
					}
				}
			}catch(Exception e){
				logger.error(
						String.format("Can't calculate ldapDN for object with ID %s", objectId), e);
			}
		}
	}
	
	private void execSyncInvocation(Object persistentInstance, LDAPSyncEventType eventType) {

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
			
			boolean exceptionOccured = false;
			for (LDAPServer ldapServer : nonLeadingLdapServers){
				
				Object jfireObjectId = JDOHelper.getObjectId(persistentInstance);
				UserManagementSystemID ldapServerId = ldapServer.getUserManagementSystemObjectID();
				SendEventTypeDataUnit dataUnit = null;
				if (LDAPSyncEventType.SEND_DELETE == eventType
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
				
				try{
					AsyncInvoke.exec(
							new SyncToLDAPServerInvocation(ldapServerId, dataUnit, eventType), true
							);
				} catch (AsyncInvokeEnqueueException e) {
					exceptionOccured = true;
					logger.error(e.getMessage(), e);
				}
			}
			
			if (exceptionOccured){
				throw new JDOUserCallbackException("Unable to synhronize User data to some LDAP server(s)! Please see log for details.");
			}
		}
	}

	@Override
	public void preStore(InstanceLifecycleEvent event) {
		// do nothing
	}

}