package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.io.Serializable;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;

/**
 * This invocation is used for synchronizing between JFire and {@link LDAPServer}. Synchronization is configured by given {@link LDAPSyncEvent}.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPSyncInvocation extends Invocation{

	private static final long serialVersionUID = 1L;
	
	private UserManagementSystemID ldapServerID;
	private LDAPSyncEvent ldapSyncEvent;
	
	/**
	 * Construct a {@link LDAPSyncInvocation} which will run synchronization on {@link LDAPServer} with given {@link #ldapServerID} 
	 * configured with {@link #ldapSyncEvent}.
	 * 
	 * @param ldapServerID
	 * @param ldapSyncEvent
	 */
	public LDAPSyncInvocation(UserManagementSystemID ldapServerID, LDAPSyncEvent ldapSyncEvent){
		this.ldapServerID = ldapServerID;
		this.ldapSyncEvent = ldapSyncEvent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable invoke() throws Exception {
		
		if (ldapServerID == null || ldapSyncEvent == null){
			return null;
		}
		
		PersistenceManager pm = createPersistenceManager();
		try{
			
			LDAPServer ldapServer = (LDAPServer) pm.getObjectById(ldapServerID);
			if (ldapSyncEvent.getOrganisationID() == null
					|| ldapSyncEvent.getOrganisationID().isEmpty()){
				ldapSyncEvent.setOrganisationID(getOrganisationID());
			}
			ldapServer.synchronize(ldapSyncEvent);
			
			return null;
			
		}finally{
			pm.close();
		}
	}
}

