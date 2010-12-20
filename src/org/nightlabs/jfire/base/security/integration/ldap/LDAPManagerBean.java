package org.nightlabs.jfire.base.security.integration.ldap;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.Extent;
import javax.jdo.JDOUserCallbackException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreLifecycleListener;

import org.jboss.annotation.security.SecurityDomain;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPSyncEvent.LDAPSyncEventType;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.UserManagementSystemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@Stateless(mappedName = "LDAPManager")
@SecurityDomain(value="jfire")
public class LDAPManagerBean extends BaseSessionBeanImpl implements LDAPManagerRemote {
	
	private static final Logger logger = LoggerFactory.getLogger(LDAPManagerBean.class);
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() {
		
		PersistenceManager pm = createPersistenceManager();

		try{
			// creating/loading InetOrgPersonLDAPServerType single instance and regisering it
			// within UserManagementSystemType instances map
			InetOrgPersonLDAPServerType.createSingleInstance(pm, "InetOrgPersonLDAPServerType");
			
			Collection<LDAPServer> activeServers = 
				UserManagementSystem.getActiveUserManagementSystems(pm, LDAPServer.class);
			
			// TODO: temporary server creation, remove it later
//			if (activeServers.size() == 0){
//				InetOrgPersonLDAPServerType umsType = UserManagementSystemType.getInstance(InetOrgPersonLDAPServerType.class);
//				LDAPServer server1 = umsType.createUserManagementSystem();
//				server1.setActive(true);
//				LDAPServer server2 = umsType.createUserManagementSystem();
//				server1 = pm.makePersistent(server1);
//				server2 = pm.makePersistent(server2);
//			}
			
			// run sync configuration only once at startup because it's unlikely 
			// that leading system scenario will be changed at server runtime
			configureSynchronization(pm);
			
		}finally{
			pm.close();
		}
		
	}
	
	private void configureSynchronization(PersistenceManager pm){

		// determine who is the leading system, LDAP or JFire
		boolean jfireAsLeadingSystem = true;	// FIXME: read property value from configuration
												// Where could be the most appropriate place in JFire 
												// for holding this kind of configuration? Denis. 
		
		if (jfireAsLeadingSystem){
			
			// TODO: add a Person class
			pm.getPersistenceManagerFactory().addInstanceLifecycleListener(
					syncStoreLifecycleListener, new Class[]{User.class}
					);
			
		}else{
			
			// TODO: LDAP as a leading system scenario is still to be done
			// 1. using timer
			// 2. using push notifications from LDAP server
			
		}
	}

	private static StoreLifecycleListener syncStoreLifecycleListener = new SyncStoreLifecycleListener();
	
	static class SyncStoreLifecycleListener implements StoreLifecycleListener{

		@Override
		public void postStore(final InstanceLifecycleEvent event) {
			
			try {
				Object persistentInstance = event.getPersistentInstance();
				if (persistentInstance instanceof User){
					AsyncInvoke.exec(
							new SyncToLDAPServersInvocation(((User) persistentInstance).getCompleteUserID()), true
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
		
	}
	
	public static class SyncToLDAPServersInvocation extends Invocation{
		
		private static final long serialVersionUID = 1L;
		
		private String completeUserId;
		
		public SyncToLDAPServersInvocation(String completeUserId){
			this.completeUserId = completeUserId;
		}

		@Override
		public Serializable invoke() throws Exception {
			
			PersistenceManager pm = createPersistenceManager();
			try{
				
				// call to getExtent with second parameter set to true just in case some classes
				// will appear extending an LDAPServer
				Extent<LDAPServer> ldapServersExtent = pm.getExtent(LDAPServer.class, true);
				
				boolean exceptionOccured = false;
				for (Iterator<LDAPServer> iterator = ldapServersExtent.iterator(); iterator.hasNext();) {
					
					LDAPServer ldapServer = iterator.next();
					
					LDAPSyncEvent syncEvent = new LDAPSyncEvent(LDAPSyncEventType.SEND, completeUserId);
					try{
						ldapServer.synchronize(syncEvent);
					}catch(Exception e){
						// catch all exceptions here without immidiate rethrowing or other reaction
						// because we need to execute synchronization for all other servers despite  
						// to the exception at particular server synchronization
						exceptionOccured = true;
						logger.error(e.getMessage(), e);
					}
					
					
				}
				
				if(exceptionOccured){
					// FIXME: not sure how exceptions should be handled in Async Invocations 
//					throw new RuntimeException("Exception(s) occured during synchronizing User data to LDAP server(s). Please see log for details.");
				}

				return null;
				
			}finally{
				pm.close();
			}
		}
	}

}
