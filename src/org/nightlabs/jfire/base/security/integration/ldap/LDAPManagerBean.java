package org.nightlabs.jfire.base.security.integration.ldap;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;

import org.jboss.annotation.security.SecurityDomain;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;

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
			
			// FIXME: temporary server creation
//			Collection<UserManagementSystem> activeServers = getActiveLDAPServers();
//			if (activeServers.size() == 0){
//				InetOrgPersonLDAPServerType umsType = UserManagementSystemType.getInstance(InetOrgPersonLDAPServerType.class);
//				LDAPServer server1 = umsType.createUserManagementSystem();
//				server1.setActive(true);
//				LDAPServer server2 = umsType.createUserManagementSystem();
//				server1 = pm.makePersistent(server1);
//				server2 = pm.makePersistent(server2);
//			}
			
		}finally{
			pm.close();
		}
		
	}

	// FIXME: temporary server creation
//	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
//	private Collection<UserManagementSystem> getActiveLDAPServers(){
//		
//		PersistenceManager pm = createPersistenceManager();
//		
//		try{
//			
//			javax.jdo.Query q = pm.newNamedQuery(UserManagementSystem.class, UserManagementSystem.GET_ACTIVE_USER_MANAGEMENT_SYSTEMS);
//			return (Collection<UserManagementSystem>) q.execute();
//			
//		}finally{
//			pm.close();
//		}
//		
//	}
	
}
