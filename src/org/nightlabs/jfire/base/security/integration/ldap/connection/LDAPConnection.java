package org.nightlabs.jfire.base.security.integration.ldap.connection;

import java.util.Enumeration;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionWrapper.EntryModificationFlag;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;

/**
 * Class representing connection to an actual LDAP server. Connection parameters are passed by
 * {@link ILDAPConnectionParamsProvider}, actual implementation is done inside {@link LDAPConnectionWrapper}
 * subclasses, so see it for code documentation.
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

    public void connect() throws UserManagementSystemCommunicationException {
    	this.connectionWrapper.connect();
    }
    
    public void bind(
    		String bindPrincipal, String password
    		) throws LoginException, UserManagementSystemCommunicationException {
    	
    	this.connectionWrapper.bind(bindPrincipal, password);
    }

    public void createEntry(String entryDN, Map<String, Object[]> attributes) throws UserManagementSystemCommunicationException{
    	this.connectionWrapper.createEntry(entryDN, attributes);
    }

    public void modifyEntry(String entryDN, Map<String, Object[]> attributes, EntryModificationFlag modificationFlag) throws UserManagementSystemCommunicationException{
    	this.connectionWrapper.modifyEntry(entryDN, attributes, modificationFlag);
    }
    
    public Enumeration<?> search(String dn, Map<String, Object[]> searchAttributes, String[] returnAttributes) throws UserManagementSystemCommunicationException{
    	return this.connectionWrapper.search(dn, searchAttributes, returnAttributes);
    }
    
	public Map<String, Object[]> getAttributesForEntry(String dn) throws UserManagementSystemCommunicationException {
		return this.connectionWrapper.getAttribbutesForEntry(dn);
	}

    public void disconnect() {
    	this.connectionWrapper.disconnect();
    }

    public void unbind() throws UserManagementSystemCommunicationException {
    	this.connectionWrapper.unbind();
    }

    public boolean isConnected() {
    	return this.connectionWrapper.isConnected();
    }

}
