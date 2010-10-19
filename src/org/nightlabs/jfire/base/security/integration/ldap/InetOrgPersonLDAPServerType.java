package org.nightlabs.jfire.base.security.integration.ldap;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.base.security.integration.ldap.connection.IConnectionParamsProvider.EncryptionMethod;
import org.nightlabs.jfire.security.integration.UserManagementSystemType;

/**
 * {@link UserManagementSystemType} for {@link LDAPServer} with InetOrgPerson schema
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class InetOrgPersonLDAPServerType extends UserManagementSystemType<LDAPServer> {

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates single instance of the class if it's not found in datastore and registers it
	 * within _instances map of superclass.
	 * 
	 * @param pm
	 * @param name
	 */
	public static synchronized void createSingleInstance(PersistenceManager pm, String name){
		String className = InetOrgPersonLDAPServerType.class.getName();
		if (_instances.get(className) == null && pm != null){
			InetOrgPersonLDAPServerType singleInstance = loadSingleInstance(pm, InetOrgPersonLDAPServerType.class);
			if (singleInstance == null){
				singleInstance = new InetOrgPersonLDAPServerType(name);
				singleInstance = pm.makePersistent(singleInstance);
				_instances.put(className, singleInstance);
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @param name
	 */
	private InetOrgPersonLDAPServerType(String name){
		super(name);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public LDAPServer createUserManagementSystem() {
		
		LDAPServer server = new LDAPServer(
				"local LDAP server", this, "localhost", 10389, EncryptionMethod.NONE
				);
		
		return server;
	}

}
