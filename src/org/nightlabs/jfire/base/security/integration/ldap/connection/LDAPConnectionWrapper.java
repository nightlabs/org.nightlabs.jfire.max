package org.nightlabs.jfire.base.security.integration.ldap.connection;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;

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
    public void connect() throws CommunicationException;


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
    public void bind(String bindPrincipal, String password) throws CommunicationException, AuthenticationException;


    /**
     * Unbinds from the directory server.
     * 
     * @throws CommunicationException 
     */
    public void unbind() throws CommunicationException;


    /**
     * Checks if is connected.
     * 
     * @return true, if is connected
     */
    public boolean isConnected();

}
