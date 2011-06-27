package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.io.Serializable;
import java.util.Collection;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.LDAPSyncEventType;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This invocation is used for synchronizing data from JFire to LDAP directory
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class SyncToLDAPServerInvocation extends Invocation{

	private static final Logger logger = LoggerFactory.getLogger(SyncToLDAPServerInvocation.class);

	private static final long serialVersionUID = 1L;
	
	private Object objectId;
	
	private LDAPSyncEventType eventType;
	
	public SyncToLDAPServerInvocation(Object objectId, LDAPSyncEventType eventType){
		this.objectId = objectId;
		this.eventType = eventType;
	}

	@Override
	public Serializable invoke() throws Exception {
		
		PersistenceManager pm = createPersistenceManager();
		try{
			
			Collection<LDAPServer> ldapServers = UserManagementSystem.getUserManagementSystemsByLeading(
					pm, false, LDAPServer.class
					);
			
			boolean exceptionOccured = false;
			for (LDAPServer ldapServer : ldapServers) {
				
				LDAPSyncEvent syncEvent = new LDAPSyncEvent(eventType);
				syncEvent.setOrganisationID(getOrganisationID());
				syncEvent.setJFireObjectsIds(CollectionUtil.createHashSet(objectId));
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
				throw new LDAPSyncException("Exception(s) occured during synchronizing User data to LDAP server(s). Please see log for details.");
			}

			return null;
			
		}finally{
			pm.close();
		}
	}
}

