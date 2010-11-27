package org.nightlabs.jfire.base.security.integration.ldap;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;

import org.jboss.annotation.security.SecurityDomain;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.UserManagementSystemType;

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
			Collection<? extends UserManagementSystem> activeServers = UserManagementSystem.getActiveUserManagementSystems(pm);
			if (activeServers.size() == 0){
				InetOrgPersonLDAPServerType umsType = UserManagementSystemType.getInstance(InetOrgPersonLDAPServerType.class);
				LDAPServer server1 = umsType.createUserManagementSystem();
				server1.setActive(true);
				LDAPServer server2 = umsType.createUserManagementSystem();
				server1 = pm.makePersistent(server1);
				server2 = pm.makePersistent(server2);
			}
			
		}finally{
			pm.close();
		}
		
	}

}
