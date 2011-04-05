package org.nightlabs.jfire.base.security.integration.ldap;

import java.io.IOException;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.security.integration.UserManagementSystemType;
import org.nightlabs.util.IOUtil;

/**
 * {@link UserManagementSystemType} for {@link LDAPServer} with Samba schema
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class SambaLDAPServerType extends UserManagementSystemType<LDAPServer> {

	private static final String SAMBA_SYNC_TO_JFIRE_SCRIPT = "scripts/SambaSyncToJFireScript.js";
	private static final String SAMBA_GET_ATTRIBUTES_FOR_LDAP_SCRIPT = "scripts/SambaGetAttributesForLDAPScript.js";
	private static final String SAMBA_GET_DN_SCRIPT = "scripts/SambaGetDNScript.js";
	private static final String SAMBA_BIND_VARIABLES_SCRIPT = "scripts/CommonBindVariablesScript.js";
	private static final String SAMBA_GET_PARENT_ENTRIES_SCRIPT = "scripts/SambaGetParentEntriesScript.js";
	
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated For JDO only!
	 */
	@Deprecated
	public SambaLDAPServerType(){}

	/**
	 * {@inheritDoc}
	 * @param name
	 */
	protected SambaLDAPServerType(String name){
		super(name);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public LDAPServer createUserManagementSystem() {
		
		LDAPServer server = new LDAPServer(null, this);

		try {
			LDAPScriptSet ldapScriptSet = new LDAPScriptSet(server);
			
			// actual scripts are stored in separate files to make their editing more comfortable
			Class<? extends SambaLDAPServerType> typeClass = this.getClass();
			ldapScriptSet.setBindVariablesScript(IOUtil.readTextFile(typeClass.getResourceAsStream(SAMBA_BIND_VARIABLES_SCRIPT)));
			ldapScriptSet.setLdapDNScript(IOUtil.readTextFile(typeClass.getResourceAsStream(SAMBA_GET_DN_SCRIPT)));
			ldapScriptSet.setGenerateJFireToLdapAttributesScript(IOUtil.readTextFile(typeClass.getResourceAsStream(SAMBA_GET_ATTRIBUTES_FOR_LDAP_SCRIPT)));
			ldapScriptSet.setSyncLdapToJFireScript(IOUtil.readTextFile(typeClass.getResourceAsStream(SAMBA_SYNC_TO_JFIRE_SCRIPT)));
			ldapScriptSet.setGenerateParentLdapEntriesScript(IOUtil.readTextFile(typeClass.getResourceAsStream(SAMBA_GET_PARENT_ENTRIES_SCRIPT)));

			server.setLdapScriptSet(ldapScriptSet);
			
		} catch (IOException e) {
			throw new RuntimeException("Can't create LDAPScriptSet!", e);
		}

		return server;
	}

}
