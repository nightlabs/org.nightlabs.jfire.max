package org.nightlabs.jfire.base.security.integration.ldap;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.jboss.annotation.security.SecurityDomain;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.jfire.servermanager.JFireServerManager;

/**
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@Stateless(mappedName = "LDAPManager")
@SecurityDomain(value="jfire")
public class LDAPManagerBean extends BaseSessionBeanImpl implements LDAPManagerRemote {

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() {
		
		PersistenceManager pm = createPersistenceManager();

		try{
			// creating/loading InetOrgPersonLDAPServerType single instance and regisering it
			// within UserManagementSystemType instances map
			InetOrgPersonLDAPServerType.createSingleInstance(pm, "InetOrgPersonLDAPServerType");
			
			
			JFireServerManager jFireServerManager = getJFireServerManager();
			Collection<LDAPServer> activeServers = getActiveLDAPServers();
			
			
//			// FIXME: temporary server creation
//			if (activeServers.size() == 0){
//				InetOrgPersonLDAPServerType umsType = UserManagementSystemType.getInstance(InetOrgPersonLDAPServerType.class);
//				LDAPServer server1 = umsType.createUserManagementSystem();
//				server1.setActive(true);
//				LDAPServer server2 = umsType.createUserManagementSystem();
//				server1 = pm.makePersistent(server1);
//				server2 = pm.makePersistent(server2);
//				
//				activeServers.add(server1);
//				activeServers.add(server2);
//			}
			
			
			for (LDAPServer ldapServer : activeServers) {
				
				jFireServerManager.registerActiveUserManagementSystem(ldapServer);
				
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			pm.close();
		}
		
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	private Collection<LDAPServer> getActiveLDAPServers(){
		
		PersistenceManager pm = createPersistenceManager();
		
		try{
			
			javax.jdo.Query q = pm.newNamedQuery(LDAPServer.class, LDAPServer.GET_ACTIVE_LDAP_SERVERS_IDS);
			Collection<UserManagementSystemID> ids = (Collection<UserManagementSystemID>) q.execute();

			pm.getFetchPlan().setMaxFetchDepth(-1);
			pm.getFetchPlan().setGroups(FetchPlan.DEFAULT, UserManagementSystem.FETCH_GROUP_NAME);

			return pm.detachCopyAll(pm.getObjectsById(ids));
			
		}finally{
			pm.close();
		}
		
	}
	
}
