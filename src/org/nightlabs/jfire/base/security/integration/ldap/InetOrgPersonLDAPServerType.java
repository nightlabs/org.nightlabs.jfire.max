package org.nightlabs.jfire.base.security.integration.ldap;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.security.integration.UserManagementSystemType;
import org.nightlabs.util.IOUtil;

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

	private static final String INET_ORG_PERSON_SYNC_TO_JFIRE_SCRIPT = "scripts/InetOrgPersonSyncToJFireScript.js";
	private static final String INET_ORG_PERSON_GET_ATTRIBUTES_FOR_LDAP_SCRIPT = "scripts/InetOrgPersonGetAttributesForLDAPScript.js";
	private static final String INET_ORG_PERSON_GET_DN_SCRIPT = "scripts/InetOrgPersonGetDNScript.js";
	private static final String INET_ORG_PERSON_BIND_VARIABLES_SCRIPT = "scripts/InetOrgPersonBindVariablesScript.js";
	
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
		InetOrgPersonLDAPServerType singleInstance = loadSingleInstance(pm, InetOrgPersonLDAPServerType.class);
		if (singleInstance == null){
			singleInstance = new InetOrgPersonLDAPServerType(name);
			singleInstance = pm.makePersistent(singleInstance);
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
		
		LDAPServer server = new LDAPServer(null, this);

		try {
			LDAPScriptSet ldapScriptSet = new LDAPScriptSet(null);

			// actual scripts are stored in separate files to make their editing more comfortable
			Class<? extends InetOrgPersonLDAPServerType> typeClass = this.getClass();
			ldapScriptSet.setBindVariablesScript(IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_BIND_VARIABLES_SCRIPT)));
			ldapScriptSet.setLdapDNScript(IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_GET_DN_SCRIPT)));
			ldapScriptSet.setSyncJFireToLdapScript(IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_GET_ATTRIBUTES_FOR_LDAP_SCRIPT)));
			ldapScriptSet.setSyncLdapToJFireScript(IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_SYNC_TO_JFIRE_SCRIPT)));

			server.setLdapScriptSet(ldapScriptSet);
			
		} catch (IOException e) {
			throw new RuntimeException("Can't create LDAPScriptSet!", e);
		}

		return server;
	}

}
