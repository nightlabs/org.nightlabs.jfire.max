package org.nightlabs.jfire.base.security.integration.ldap;

import java.util.ArrayList;
import java.util.Collection;

import javax.jdo.JDOUserCallbackException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Queries;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.UserSecurityGroupSyncConfig;
import org.nightlabs.jfire.security.integration.UserSecurityGroupSyncConfigContainer;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;

/**
 * Implementation of {@link UserSecurityGroupSyncConfig} for {@link LDAPServer} user management system.
 * The type which maps to a {@link UserSecurityGroup} is a simple {@link String} with full distingueshed
 * name of a corresponding entry in LDAP directory. Note that this entry MUST represent a group which means
 * that it must have either "groupOfNames" or "groupOfUniqueNames" object class.
 * 
 * For more details please see {@link UserSecurityGroupSyncConfig}.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION, 
		detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
@Queries({
	@javax.jdo.annotations.Query(
			name="LDAPUserSecurityGroupSyncConfig.getSyncConfigForLDAPGroupName",
			value="SELECT where JDOHelper.getObjectId(this.userManagementSystem) == :ldapServerId && this.ldapGroupName == :ldapGroupName ORDER BY JDOHelper.getObjectId(this) ASCENDING"
			)
	})
public class LDAPUserSecurityGroupSyncConfig extends UserSecurityGroupSyncConfig<LDAPServer, String> implements StoreCallback{

	/**
	 * The serial version UID of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Executes a named {@link Query} which returns {@link LDAPUserSecurityGroupSyncConfig} for given {@link LDAPServer} ID and LDAP group name.
	 * 
	 * @param pm {@link PersistenceManager} to be used for execution
	 * @param ldapServerId The ID of {@link LDAPServer}
	 * @param ldapGroupName Name of LDAP group
	 * @return found {@link LDAPUserSecurityGroupSyncConfig} or <code>null</code>
	 */
	public static LDAPUserSecurityGroupSyncConfig getSyncConfigForLDAPGroupName(
			PersistenceManager pm, UserManagementSystemID ldapServerId, String ldapGroupName
			) {
		javax.jdo.Query q = pm.newNamedQuery(
				LDAPUserSecurityGroupSyncConfig.class, 
				"LDAPUserSecurityGroupSyncConfig.getSyncConfigForLDAPGroupName"
				);
		@SuppressWarnings("unchecked")
		Collection<LDAPUserSecurityGroupSyncConfig> syncConfigs = (Collection<LDAPUserSecurityGroupSyncConfig>) q.execute(ldapServerId, ldapGroupName);
		syncConfigs = new ArrayList<LDAPUserSecurityGroupSyncConfig>(syncConfigs);
		q.closeAll();
		if (!syncConfigs.isEmpty()){
			return syncConfigs.iterator().next();
		}
		return null;
	}

	/**
	 * LDAP Distingueshed Name of a group in LDAP directory which will map to JFire {@link UserSecurityGroup}
	 */
	@Persistent
	private String ldapGroupName;
	
	
	/**
	 * @deprecated For JDO only!
	 */
	@Deprecated
	public LDAPUserSecurityGroupSyncConfig(){}

	/**
	 * Construct new synchronization config for given {@link LDAPServer} with empty LDAP group name.
	 * Note that this config cannot be stored before LDAP group name is not set via {@link #setLdapGroupName(String)},
	 * if {@link #ldapGroupName} is not correctly set {@link JDOUserCallbackException} will be thrown in {@link #jdoPreStore()}. 
	 * 
	 * @param container {@link UserSecurityGroupSyncConfigContainer} which holds this {@link LDAPUserSecurityGroupSyncConfig}, not <code>null</code>
	 * @param userManagementSystem {@link UserManagementSystem} to synchronize with, not <code>null</code>
	 */
	public LDAPUserSecurityGroupSyncConfig(UserSecurityGroupSyncConfigContainer container, LDAPServer userManagementSystem) {
		super(container, userManagementSystem);
	}

	/**
	 * Construct new synchronization config for given {@link LDAPServer}. 
	 * 
	 * @param container {@link UserSecurityGroupSyncConfigContainer} which holds this {@link LDAPUserSecurityGroupSyncConfig}, not <code>null</code>
	 * @param userManagementSystem {@link UserManagementSystem} to synchronize with, not <code>null</code>
	 * @param ldapGroupName Distingueshed (full) name of LDAP group to map to, not <code>null</code> and not empty
	 */
	public LDAPUserSecurityGroupSyncConfig(
			UserSecurityGroupSyncConfigContainer container, LDAPServer userManagementSystem, String ldapGroupName) {
		this(container, userManagementSystem);
		setLdapGroupName(ldapGroupName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getUserManagementSystemSecurityObject() {
		return ldapGroupName;
	}
	
	/**
	 * Set LDAP group name to map to underlying {@link UserSecurityGroup}.
	 * 
	 * @param ldapGroupName distingueshed name of LDAP group, not <code>null</code>, not empty
	 */
	public void setLdapGroupName(String ldapGroupName) {
		if (ldapGroupName == null || ldapGroupName.isEmpty()){
			throw new IllegalArgumentException("ldapGroupName must be not null an not empty!");
		}
		this.ldapGroupName = ldapGroupName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void jdoPreStore() {
		if (this.ldapGroupName == null || this.ldapGroupName.isEmpty()){
			throw new JDOUserCallbackException("Cannot store LDAPUserSecurityGroupSyncConfig with null or empty ldapGroupName!");
		}
	}

}
