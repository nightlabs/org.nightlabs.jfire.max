package org.nightlabs.jfire.base.security.integration.ldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.base.security.integration.ldap.scripts.ILDAPScriptProvider;
import org.nightlabs.jfire.base.security.integration.ldap.sync.AttributeStructFieldSyncHelper.AttributeStructFieldDescriptor;
import org.nightlabs.jfire.base.security.integration.ldap.sync.AttributeStructFieldSyncHelper.LDAPAttributeSyncPolicy;
import org.nightlabs.jfire.base.security.integration.ldap.sync.IAttributeStructFieldDescriptorProvider;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.prop.id.StructBlockID;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.structfield.NumberStructField;
import org.nightlabs.jfire.prop.structfield.PhoneNumberStructField;
import org.nightlabs.jfire.prop.structfield.TextStructField;
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
public class InetOrgPersonLDAPServerType extends UserManagementSystemType<LDAPServer> implements IAttributeStructFieldDescriptorProvider, ILDAPScriptProvider{
	
	private static final StructBlockID LDAP_ATTRIBUTES = StructBlockID.create(Organisation.DEV_ORGANISATION_ID, "InetOrgPersonLDAPAttributes"); //$NON-NLS-1$
	
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_AUDIO = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "audio"), "Audio (u-law)", TextStructField.class) ; //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_BUSINESS_CATEGORY = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "businessCategory"), "Business category", TextStructField.class);; //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_CAR_LICENSE = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "carLicense"), "Vehicle license or registration plate", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_DEPARTMENT_NUMBER = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "departmentNumber"), "Department number (identifies a department within an organization)", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_DESTINATION_INDICATOR = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "destinationIndicator"), "Destination indicator", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_EMPLOYEE_NUMBER = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "employeeNumber"), "Employee number (numerically identifies an employee within an organization)", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_EMPLOYEE_TYPE = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "employeeType"), "Type of employment for a person", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_HOME_PHONE_NUMBER = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "homeTelephoneNumber"), "Home telephone number", PhoneNumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_HOME_POSTAL_ADDRESS = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "homePostalAddress"), "Home postal address", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_INITIALS = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "initials"), "Initials of some or all of names, but not the surname(s)", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_INTERNATIONAL_ISDN_NUMBER = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "internationaliSDNNumber"), "International ISDN number", NumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_MANAGER = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "manager"), "DN of existent manager LDAP entry", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_MOBILE_TELEPHONE_NUMBER = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "mobileTelephoneNumber"), "Mobile telephone number", PhoneNumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_ORGANISATIONAL_UNIT_NAME = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "organizationalUnitName"), "Organizational unit this object belongs to", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_PAGER_TELEPHONE_NUMBER = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "pagerTelephoneNumber"), "Pager telephone number", PhoneNumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_PHYSICAL_DELIVERY_OFFICE_NAME = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "physicalDeliveryOfficeName"), "Physical Delivery Office Name", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_POST_OFFICE_BOX = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "postOfficeBox"), "Post Office Box", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_PREFERRED_DELIVERY_METHOD = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "preferredDeliveryMethod"), "Preferred delivery method", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_REGISTERED_ADDRESS = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "registeredAddress"), "Registered postal address", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_ROOM_NUMBER = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "roomNumber"), "Room number", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SECRETARY = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "secretary"), "DN of existent secretary LDAP entry", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_SEE_ALSO = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "seeAlso"), "DN of existent related object LDAP entry", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_TELETEX_TERMINAL_IDENTIFIER = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "teletexTerminalIdentifier"), "Teletex Terminal Identifier", TextStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_TELEX_NUMBER = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "telexNumber"), "Teletex Terminal Identifier", NumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_X121_ADDRESS = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "x121Address"), "X.121 numeric address", NumberStructField.class); //$NON-NLS-1$
	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_X500_UNIQUE_IDENTIFIER = new AttributeStructFieldDescriptor(
			StructFieldID.create(LDAP_ATTRIBUTES, "x500UniqueIdentifier"), "X.500 unique identifier", TextStructField.class); //$NON-NLS-1$
	// TODO: following attributes require simple BinaryStructFields for holding files binary data, NOT supported at the moment
//	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_USER_CERTIFICATE = new AttributeStructFieldDescriptor(
//			StructFieldID.create(LDAP_ATTRIBUTES, "userCertificate"), "", TextStructField.class); //$NON-NLS-1$
//	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_USER_PKCS12 = new AttributeStructFieldDescriptor(
//			StructFieldID.create(LDAP_ATTRIBUTES, "userPKCS12"), "", TextStructField.class); //$NON-NLS-1$
//	private static final AttributeStructFieldDescriptor LDAP_ATTRIBUTES_USER_SMIMME_SERTIFICATE = new AttributeStructFieldDescriptor(
//			StructFieldID.create(LDAP_ATTRIBUTES, "userSMIMECertificate"), "", TextStructField.class); //$NON-NLS-1$

	private static final Collection<AttributeStructFieldDescriptor> mandatoryAttributeStructFieldDescriptors = new ArrayList<AttributeStructFieldDescriptor>();

	private static final Collection<AttributeStructFieldDescriptor> allAttributeStructFieldDescriptors = new ArrayList<AttributeStructFieldDescriptor>();
	static{
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_AUDIO);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_BUSINESS_CATEGORY);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_CAR_LICENSE);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_DEPARTMENT_NUMBER);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_DESTINATION_INDICATOR);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_EMPLOYEE_NUMBER);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_EMPLOYEE_TYPE);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_HOME_PHONE_NUMBER);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_HOME_POSTAL_ADDRESS);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_INITIALS);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_INTERNATIONAL_ISDN_NUMBER);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_MANAGER);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_MOBILE_TELEPHONE_NUMBER);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_ORGANISATIONAL_UNIT_NAME);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_PAGER_TELEPHONE_NUMBER);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_PHYSICAL_DELIVERY_OFFICE_NAME);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_POST_OFFICE_BOX);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_PREFERRED_DELIVERY_METHOD);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_REGISTERED_ADDRESS);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_ROOM_NUMBER);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SECRETARY);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_SEE_ALSO);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_TELETEX_TERMINAL_IDENTIFIER);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_TELEX_NUMBER);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_X121_ADDRESS);
		allAttributeStructFieldDescriptors.add(LDAP_ATTRIBUTES_X500_UNIQUE_IDENTIFIER);
	}

	private static final String INET_ORG_PERSON_SYNC_TO_JFIRE_SCRIPT = "scripts/InetOrgPersonSyncToJFireScript.js";
	private static final String INET_ORG_PERSON_GET_ATTRIBUTES_FOR_LDAP_SCRIPT = "scripts/InetOrgPersonGetAttributesForLDAPScript.js";
	private static final String INET_ORG_PERSON_GET_DN_SCRIPT = "scripts/InetOrgPersonGetDNScript.js";
	private static final String INET_ORG_PERSON_BIND_VARIABLES_SCRIPT = "scripts/CommonBindVariablesScript.js";
	private static final String INET_ORG_PERSON_GET_PARENT_ENTRIES_SCRIPT = "scripts/InetOrgPersonGetParentEntriesScript.js";

	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @deprecated For JDO only!
	 */
	@Deprecated
	public InetOrgPersonLDAPServerType(){}
	
	/**
	 * {@inheritDoc}
	 * @param name
	 */
	protected InetOrgPersonLDAPServerType(String name){
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
			Class<? extends InetOrgPersonLDAPServerType> typeClass = this.getClass();
			ldapScriptSet.setBindVariablesScript(IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_BIND_VARIABLES_SCRIPT)));
			ldapScriptSet.setLdapDNScript(IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_GET_DN_SCRIPT)));
			ldapScriptSet.setGenerateJFireToLdapAttributesScript(IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_GET_ATTRIBUTES_FOR_LDAP_SCRIPT)));
			ldapScriptSet.setSyncLdapToJFireScript(IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_SYNC_TO_JFIRE_SCRIPT)));
			ldapScriptSet.setGenerateParentLdapEntriesScript(IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_GET_PARENT_ENTRIES_SCRIPT)));

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
		Class<? extends InetOrgPersonLDAPServerType> typeClass = this.getClass();
		try{
			if (ILDAPScriptProvider.BIND_VARIABLES_SCRIPT_ID.equals(scriptID)){
				return IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_BIND_VARIABLES_SCRIPT));
			}else if (ILDAPScriptProvider.GET_ENTRY_NAME_SCRIPT_ID.equals(scriptID)){
				return IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_GET_DN_SCRIPT));
			}else if (ILDAPScriptProvider.GET_ATTRIBUTE_SET_SCRIPT_ID.equals(scriptID)){
				return IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_GET_ATTRIBUTES_FOR_LDAP_SCRIPT));
			}else if (ILDAPScriptProvider.GET_PARENT_ENTRIES_SCRIPT_ID.equals(scriptID)){
				return IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_GET_PARENT_ENTRIES_SCRIPT));
			}else if (ILDAPScriptProvider.SYNC_TO_JFIRE_SCRIPT_ID.equals(scriptID)){
				return IOUtil.readTextFile(typeClass.getResourceAsStream(INET_ORG_PERSON_SYNC_TO_JFIRE_SCRIPT));
			}else{
				return null;
			}
		}catch(IOException e){
			return null;
		}
	}

}
