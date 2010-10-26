package org.nightlabs.jfire.base.security.integration.ldap.connection;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;

/**
 * Class representing connection to an actual LDAP server. Connection parameters are passed by
 * {@link ILDAPConnectionParamsProvider}, actual implementation is done inside {@link LDAPConnectionWrapper}
 * subclasses.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPConnection {

    private ILDAPConnectionParamsProvider connectionParamsProvider;

    private LDAPConnectionWrapper connectionWrapper;

    /**
     * Creates a new instance of Connection.
     *
     * @param connectionParameter
     */
    public LDAPConnection(ILDAPConnectionParamsProvider connectionParameter){
        this.connectionParamsProvider = connectionParameter;
        this.connectionWrapper = new JNDIConnectionWrapper(this);
    }
    
    public void setConnectionWrapper(LDAPConnectionWrapper connectionWrapper) {
		this.connectionWrapper = connectionWrapper;
	}    

     /**
     * Gets the connection parameter.
     * 
     * @return the connection parameter
     */
    public ILDAPConnectionParamsProvider getConnectionParamsProvider() {
        return connectionParamsProvider;
    }

    public void connect() throws CommunicationException {
    	this.connectionWrapper.connect();
    }
    
    public void bind(
    		String bindPrincipal, String password
    		) throws AuthenticationException, CommunicationException {
    	
    	this.connectionWrapper.bind(bindPrincipal, password);
    }

    public void disconnect() {
    	this.connectionWrapper.disconnect();
    }

    public void unbind() throws CommunicationException {
    	this.connectionWrapper.unbind();
    }

    public boolean isConnected() {
    	return this.connectionWrapper.isConnected();
    }

}
