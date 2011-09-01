package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.io.Serializable;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.LDAPSyncEventType;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.SendEventTypeDataUnit;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.util.CollectionUtil;

/**
 * This invocation is used for synchronizing data from JFire to LDAP directory
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class SyncToLDAPServerInvocation extends Invocation{

	private static final long serialVersionUID = 1L;
	
	private UserManagementSystemID ldapServerId;
	
	private SendEventTypeDataUnit syncDataUnit;
	
	private LDAPSyncEventType eventType;
	
	public SyncToLDAPServerInvocation(UserManagementSystemID ldapServerId, SendEventTypeDataUnit dataUnit, LDAPSyncEventType eventType){
		this.ldapServerId = ldapServerId;
		this.syncDataUnit = dataUnit;
		this.eventType = eventType;
	}

	@Override
	public Serializable invoke() throws Exception {
		PersistenceManager pm = createPersistenceManager();
		try{
			LDAPSyncEvent syncEvent = new LDAPSyncEvent(eventType);
			syncEvent.setOrganisationID(getOrganisationID());
			syncEvent.setSendEventTypeDataUnits(CollectionUtil.createHashSet(syncDataUnit));
			LDAPServer ldapServer = (LDAPServer) pm.getObjectById(ldapServerId);
			ldapServer.synchronize(syncEvent);
		}catch(Exception e){
			throw new LDAPSyncException(
					String.format(
							"Exception(s) while synchronizing data to LDAP server with id %s.", ldapServerId), e);
		}finally{
			pm.close();
		}
		return null;
	}
}

