package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.util.Collection;

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
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
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
	
	@Override
	public void postStore(InstanceLifecycleEvent event) {
		execSyncInvocation(event.getPersistentInstance(), LDAPSyncEventType.SEND);
	}

	@Override
	public void postDelete(InstanceLifecycleEvent event) {
		execSyncInvocation(event.getPersistentInstance(), LDAPSyncEventType.DELETE);
	}
	
	private void execSyncInvocation(Object persistentInstance, LDAPSyncEventType eventType) {

		if (!isEnabled()){
			return;
		}
		
		try{
			
			PersistenceManager pm = JDOHelper.getPersistenceManager(persistentInstance);
			
			// Determine if JFire is a leading system for at least one existent LDAPServer, 
			// therefore we query all NON leading LDAPServers.
			Collection<LDAPServer> nonLeadingSystems = UserManagementSystem.getUserManagementSystemsByLeading(
					pm, false, LDAPServer.class
					);
			if (!nonLeadingSystems.isEmpty()){
	
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
				
				AsyncInvoke.exec(
						new SyncToLDAPServerInvocation(JDOHelper.getObjectId(persistentInstance), eventType), true
						);
				
			}
		} catch (AsyncInvokeEnqueueException e) {
			throw new JDOUserCallbackException("Unable to synhronize User data to LDAP server(s)!", e);
		}
	}

	@Override
	public void preStore(InstanceLifecycleEvent event) {
		// do nothing
	}

	@Override
	public void preDelete(InstanceLifecycleEvent event) {
		// do nothing
	}
	
}