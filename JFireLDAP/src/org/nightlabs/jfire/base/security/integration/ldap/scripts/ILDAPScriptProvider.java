package org.nightlabs.jfire.base.security.integration.ldap.scripts;

import org.nightlabs.jfire.base.security.integration.ldap.LDAPScriptSet;

/**
 * Interface to declare that specified class is a provider of scripts for {@link LDAPScriptSet}.
 * It holds a bunch of simple script IDs as constants to make possible more comfortable UI development.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public interface ILDAPScriptProvider {
	
	public static final String BIND_VARIABLES_SCRIPT_ID = "ILDAPScriptProvider.bindVariables";
	public static final String GET_ENTRY_NAME_SCRIPT_ID = "ILDAPScriptProvider.getEntryName";
	public static final String GET_ATTRIBUTE_SET_SCRIPT_ID = "ILDAPScriptProvider.getAttributeSet";
	public static final String GET_PARENT_ENTRIES_SCRIPT_ID = "ILDAPScriptProvider.getParentEntries";
	public static final String SYNC_TO_JFIRE_SCRIPT_ID = "ILDAPScriptProvider.syncToJFire";
	
	/**
	 * Get initial value of a script by given scriptID. Used for rolling back all user changes on a particular script.
	 * 
	 * @param scriptID ID of a script to load initial value for
	 * @return initial value of a script as a {@link String}
	 */
	String getInitialScriptContentByID(String scriptID);
	
}
