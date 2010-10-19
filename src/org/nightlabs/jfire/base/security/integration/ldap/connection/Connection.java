package org.nightlabs.jfire.base.security.integration.ldap.connection;

import javax.naming.NamingException;

/**
 * Class representing connection to an actual LDAP server. Connection parameters are passed by
 * {@link IConnectionParamsProvider}, actual implementation is done inside {@link ConnectionWrapper}
 * subclasses.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class Connection {

    private IConnectionParamsProvider connectionParamsProvider;

    private ConnectionWrapper connectionWrapper;

    /**
     * Creates a new instance of Connection.
     *
     * @param connectionParameter
     */
    public Connection(IConnectionParamsProvider connectionParameter){
        this.connectionParamsProvider = connectionParameter;
        this.connectionWrapper = new JNDIConnectionWrapper(this);
    }
    
    public void setConnectionWrapper(ConnectionWrapper connectionWrapper) {
		this.connectionWrapper = connectionWrapper;
	}    

     /**
     * Gets the connection parameter.
     * 
     * @return the connection parameter
     */
    public IConnectionParamsProvider getConnectionParamsProvider()
    {
        return connectionParamsProvider;
    }

    /**
     * Sets the connection parameter.
     * 
     * @param connectionParameter the connection parameter
     */
    public void setConnectionParamsProvider( IConnectionParamsProvider connectionParameter )
    {
        this.connectionParamsProvider = connectionParameter;
    }
    
    public void connect() throws NamingException{
    	this.connectionWrapper.connect();
    }
    
    public void bind(String bindPrincipal, String password) throws NamingException{
    	this.connectionWrapper.bind(bindPrincipal, password);
    }

    public void disconnect() {
    	this.connectionWrapper.disconnect();
    }

    public void unbind() {
    	this.connectionWrapper.unbind();
    }

    public boolean isConnected() {
    	return this.connectionWrapper.isConnected();
    }

    /**
     * Gets the default LDAP context factory.
     * 
     * Right now the following context factories are supported (by Apache DS):
     * <ul>
     * <li>com.sun.jndi.ldap.LdapCtxFactory</li>
     * <li>org.apache.harmony.jndi.provider.ldap.LdapContextFactory</li>
     * </ul>
     * 
     * @return the default LDAP context factory
     */
    public static String getDefaultLdapContextFactory()
    {
        String defaultLdapContextFactory = ""; //$NON-NLS-1$

        try
        {
            String sun = "com.sun.jndi.ldap.LdapCtxFactory"; //$NON-NLS-1$
            Class.forName( sun );
            defaultLdapContextFactory = sun;
        }
        catch ( ClassNotFoundException e )
        {
        }
        try
        {
            String apache = "org.apache.harmony.jndi.provider.ldap.LdapContextFactory"; //$NON-NLS-1$
            Class.forName( apache );
            defaultLdapContextFactory = apache;
        }
        catch ( ClassNotFoundException e )
        {
        }

        return defaultLdapContextFactory;
    }

}
