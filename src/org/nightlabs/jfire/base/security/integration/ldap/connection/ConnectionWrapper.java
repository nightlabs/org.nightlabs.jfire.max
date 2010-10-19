package org.nightlabs.jfire.base.security.integration.ldap.connection;

import javax.naming.NamingException;

/**
 * A ConnectionWrapper is a wrapper for a real directory connection implementation.
 *
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 */
public interface ConnectionWrapper
{

    /**
     * Connects to the directory server.
     * 
     * @throws NamingException 
     */
    public void connect() throws NamingException;


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
     * @throws NamingException 
     */
    public void bind(String bindPrincipal, String password) throws NamingException;


    /**
     * Unbinds from the directory server.
     */
    public void unbind();


    /**
     * Checks if is connected.
     * 
     * @return true, if is connected
     */
    public boolean isConnected();

}
