package org.nightlabs.jfire.base.security.integration.ldap.connection;

import java.util.Collection;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttributeSet;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;

/**
 * A ConnectionWrapper is a wrapper for a real directory connection implementation.
 *
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 */
public interface LDAPConnectionWrapper{
	
	public enum EntryModificationFlag{
		REMOVE,
		MODIFY
	}
	
//	public enum SearchScope{
//		SUBTREE,	// Starts at the base entry; searches the base entry and everything below it
//		ONELEVEL,	// Searches only the entries below the base entry
//		OBJECT		// Searches only the base entry; useful if you need to get attributes/value pair of just one entry
//	}

    /**
     * Connects to the directory server.
     * 
     * @throws UserManagementSystemCommunicationException 
     */
    public void connect() throws UserManagementSystemCommunicationException;


    /**
     * Binds to the directory server.
     * 
     * @param password 
     * @param bindPrincipal 
     * 
     * @throws UserManagementSystemCommunicationException, LoginException 
     */
    public void bind(String bindPrincipal, String password) throws UserManagementSystemCommunicationException, LoginException;


    /**
     * Unbinds from the directory server.
     * 
     * @throws UserManagementSystemCommunicationException 
     */
    public void unbind() throws UserManagementSystemCommunicationException;


    /**
     * Checks if is connected.
     * 
     * @return true, if is connected
     */
    public boolean isConnected();

    /**
     * Creates an entry.
     * 
     * @param dn the entry's DN
     * @param attributes the entry's initial attributes. If <code>null</code> is passed entry is created with no attributes.
     * @throws UserManagementSystemCommunicationException 
     */
	public void createEntry(String dn, LDAPAttributeSet attributes) throws UserManagementSystemCommunicationException;

	/**
	 * Deletes an entry
	 * 
	 * @param dn the entry's DN
	 * @throws UserManagementSystemCommunicationException
	 */
	public void deleteEntry(String dn) throws UserManagementSystemCommunicationException;

    /**
     * Modifies attributes of an entry.
     * 
     * @param dn the DN
     * @param attributes to be replaced, added or removed. If <code>null</code> is passed, method will silently return.
     * @param modificationFlag indicates what is to be done: replacement, addition or removal
     * @throws UserManagementSystemCommunicationException 
     */
	public void modifyEntry(String dn, LDAPAttributeSet attributes, EntryModificationFlag modificationFlag) throws UserManagementSystemCommunicationException;

	/**
	 * Performs a search for entries by their attributes.
	 * 
	 * TODO: another search method could be created if needed for searching by String filter 
	 * (with wildcards), with the possibility to specify search result size and scope. Denis.
	 * 
	 * @param dn startig point for a search
	 * @param searchAttributes will not check attributes if set to <code>null</code> or empty {@link LDAPAttributeSet}
	 * @param returnAttributes all attributes will be returned if set to <code>null</code>, no attributes if set to an empty array
	 * @return Map with search results - {@link LDAPAttributeSet} per found entry.
	 * @throws UserManagementSystemCommunicationException 
	 */
	public Map<String, LDAPAttributeSet> search(String dn, LDAPAttributeSet searchAttributes, String[] returnAttributes) throws UserManagementSystemCommunicationException;
	
	/**
	 * Get {@link LDAPAttributeSet} with all attributes of an entry specified by DN
	 * 
	 * @param dn
	 * @return attributes of given entry
	 * @throws UserManagementSystemCommunicationException 
	 */
	public LDAPAttributeSet getAttributesForEntry(String dn) throws UserManagementSystemCommunicationException;

	/**
	 * Get {@link LDAPAttributeSet} with attributes specified by <code>attributeNames</code> of an entry specified by DN.
	 * Will return all attributes if <code>attributeNames</code> is <code>null</code>.
	 * 
	 * @param dn
	 * @param attributeNames
	 * @return attributes of given entry
	 * @throws UserManagementSystemCommunicationException 
	 */
	public LDAPAttributeSet getAttributesForEntry(String dn, String[] attributeNames) throws UserManagementSystemCommunicationException;

	/**
	 * Get collection of names of direct children of a given entry
	 * 
	 * @param parentName
	 * @return collection of child entries' names
	 * @throws UserManagementSystemCommunicationException
	 * @throws LoginException 
	 */
	public Collection<String> getChildEntries(String parentName) throws UserManagementSystemCommunicationException, LoginException;
	
}
