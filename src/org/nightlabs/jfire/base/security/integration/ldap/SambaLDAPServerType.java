package org.nightlabs.jfire.base.security.integration.ldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.base.security.integration.ldap.scripts.ILDAPScriptProvider;
import org.nightlabs.jfire.base.security.integration.ldap.sync.IAttributeStructFieldDescriptorProvider;
import org.nightlabs.jfire.base.security.integration.ldap.sync.AttributeStructFieldSyncHelper.AttributeStructFieldDescriptor;
import org.nightlabs.jfire.base.security.integration.ldap.sync.AttributeStructFieldSyncHelper.LDAPAttributeSyncPolicy;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.structfield.NumberStructField;
import org.nightlabs.jfire.prop.structfield.TextStructField;
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
public class SambaLDAPServerType extends UserManagementSystemType<LDAPServer> implements IAttributeStructFieldDescriptorProvider, ILDAPScriptProvider{

	private static final StructBlockID LDAP_ATTRIBUTES = StructBlockID.create(Organisation.DEV_ORGANISATION_ID, "SambaLDAPAttributes"); //$NON-NLS-1$
	
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_GID_NUMBER = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "gidNumber"), "An integer uniquely identifying a group in an administrative domain", NumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_HOME_DIRECTORY = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "homeDirectory"), "The absolute path to the home directory", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_UID_NUMBER = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "uidNumber"), "An integer uniquely identifying a user in an administrative domain", NumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_SID = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaSID"), "Security ID", TextStructField.class); //$NON-NLS-1$

	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_GECOS = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "gecos"), "The GECOS field; the common name", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_LOGIN_SHELL = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "loginShell"), "The path to the login shell", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_ACCT_FLAGS = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaAcctFlags"), "Account Flags", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_BAD_PASSWORD_COUNT = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaBadPasswordCount"), "Bad password attempt count", NumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_BAD_PASSWORD_TIME = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaBadPasswordTime"), "Time of the last bad password attempts", NumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_DOMAIN_NAME = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaDomainName"), "Windows NT domain to which the user belongs", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_HOME_DRIVE = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaHomeDrive"), "Driver letter of home directory mapping", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_HOME_PATH = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaHomePath"), "Home directory UNC path", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_KICKOFF_TIME = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaKickoffTime"), "Timestamp of when the user will be logged off automatically", NumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_LM_PASSWORD = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaLMPassword"), "LanManager Password", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_LOGOFF_TIME = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaLogoffTime"), "Timestamp of last logoff", NumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_LOGON_HOURS = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaLogonHours"), "Logon Hours", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_LOGON_SCRIPT = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaLogonScript"), "Logon script path", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_LOGON_TIME = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaLogonTime"), "Timestamp of last logon", NumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_MUNGED_DIAL = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaMungedDial"), "Terminal server settings for Samba 3", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_NT_PASSWORD = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaNTPassword"), "MD4 hash of the unicode password", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_PASSWORD_HISTORY = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaPasswordHistory"), "Concatenated MD4 hashes of the unicode passwords used on this account", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_PRIMARY_GROUP_SID = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaPrimaryGroupSID"), "Primary Group Security ID", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_PROFILE_PATH = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaProfilePath"), "Roaming profile path", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_PWD_CAN_CHANGE = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaPwdCanChange"), "Timestamp of when the user is allowed to update the password", NumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_PWD_LAST_SET = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaPwdLastSet"), "Timestamp of the last password update", NumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_PWD_MUST_CHANGE = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaPwdMustChange"), "Timestamp of when the password will expire", NumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SAMBA_USER_WORKSTATIONS = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "sambaUserWorkstations"), "List of user workstations the user is allowed to logon to", TextStructField.class); //$NON-NLS-1$

	private static final Collection<AttributeStructFieldDescriptor> mandatoryAttributeStructFieldDescriptors = new ArrayList<AttributeStructFieldDescriptor>();
	static{
		mandatoryAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_GID_NUMBER);
		mandatoryAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_HOME_DIRECTORY);
		mandatoryAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_UID_NUMBER);
		mandatoryAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_SID);
	}

	private static final Collection<AttributeStructFieldDescriptor> allAttributeStructFieldDescriptors = new ArrayList<AttributeStructFieldDescriptor>();
	static{
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_GID_NUMBER);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_HOME_DIRECTORY);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_UID_NUMBER);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_SID);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_GECOS);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_LOGIN_SHELL);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_ACCT_FLAGS);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_BAD_PASSWORD_COUNT);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_BAD_PASSWORD_TIME);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_DOMAIN_NAME);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_HOME_DRIVE);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_HOME_PATH);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_KICKOFF_TIME);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_LM_PASSWORD);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_LOGOFF_TIME);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_LOGON_HOURS);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_LOGON_SCRIPT);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_LOGON_TIME);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_MUNGED_DIAL);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_NT_PASSWORD);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_PASSWORD_HISTORY);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_PRIMARY_GROUP_SID);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_PROFILE_PATH);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_PWD_CAN_CHANGE);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_PWD_LAST_SET);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_PWD_MUST_CHANGE);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SAMBA_USER_WORKSTATIONS);
	}

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StructBlockID getAttributeStructBlockID() {
		return LDAP_ATTRIBUTES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<AttributeStructFieldDescriptor> getAttributeStructFieldDescriptors(LDAPAttributeSyncPolicy attributeSyncPolicy) {
		if (LDAPAttributeSyncPolicy.ALL.equals(attributeSyncPolicy)){
			return allAttributeStructFieldDescriptors;
		}else if (LDAPAttributeSyncPolicy.MANDATORY_ONLY.equals(attributeSyncPolicy)){
			return mandatoryAttributeStructFieldDescriptors;
		}
		return new ArrayList<AttributeStructFieldDescriptor>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getInitialScriptContentByID(String scriptID) {
		Class<? extends SambaLDAPServerType> typeClass = this.getClass();
		try{
			if (ILDAPScriptProvider.BIND_VARIABLES_SCRIPT_ID.equals(scriptID)){
				return IOUtil.readTextFile(typeClass.getResourceAsStream(SAMBA_BIND_VARIABLES_SCRIPT));
			}else if (ILDAPScriptProvider.GET_ENTRY_NAME_SCRIPT_ID.equals(scriptID)){
				return IOUtil.readTextFile(typeClass.getResourceAsStream(SAMBA_GET_DN_SCRIPT));
			}else if (ILDAPScriptProvider.GET_ATTRIBUTE_SET_SCRIPT_ID.equals(scriptID)){
				return IOUtil.readTextFile(typeClass.getResourceAsStream(SAMBA_GET_ATTRIBUTES_FOR_LDAP_SCRIPT));
			}else if (ILDAPScriptProvider.GET_PARENT_ENTRIES_SCRIPT_ID.equals(scriptID)){
				return IOUtil.readTextFile(typeClass.getResourceAsStream(SAMBA_GET_PARENT_ENTRIES_SCRIPT));
			}else if (ILDAPScriptProvider.SYNC_TO_JFIRE_SCRIPT_ID.equals(scriptID)){
				return IOUtil.readTextFile(typeClass.getResourceAsStream(SAMBA_SYNC_TO_JFIRE_SCRIPT));
			}else{
				return null;
			}
		}catch(IOException e){
			return null;
		}
	}

}
