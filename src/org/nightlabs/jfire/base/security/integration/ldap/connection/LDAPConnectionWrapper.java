package org.nightlabs.jfire.base.security.integration.ldap.connection;

import javax.naming.directory.Attributes;
import javax.naming.ldap.Control;
import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;

/**
 * A ConnectionWrapper is a wrapper for a real directory connection implementation.
 *
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 */
public interface LDAPConnectionWrapper
{

    /**
     * Connects to the directory server.
     * 
     * @throws CommunicationException 
     */
    public void connect() throws UserManagementSystemCommunicationException;


    /**
     * Disconnects from the directory server.
     */
    public void disconnect();


    /**
     * Binds to the directory server.
     * 
     * @param password 
     * @param bindPrincipal 
     * 
     * @throws CommunicationException, AuthenticationException 
     */
    public void bind(String bindPrincipal, String password) throws UserManagementSystemCommunicationException, LoginException;


    /**
     * Unbinds from the directory server.
     * 
     * @throws CommunicationException 
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
     * @param attributes the entry's attributes
     * @param controls the controls
     * @param monitor the progress monitor
     * @param referralsInfo the referrals info
     */
	public void createEntry(String dn, Attributes attributes, Control[] controls);

}
