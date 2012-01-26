package org.nightlabs.jfire.base.security.integration.ldap.connection;

import java.util.Collection;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttributeSet;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionWrapper.EntryModificationFlag;
import org.nightlabs.jfire.base.security.integration.ldap.connection.LDAPConnectionWrapper.SearchScope;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;

/**
 * Class representing connection to an actual LDAP server. Connection parameters are passed by
 * {@link ILDAPConnectionParamsProvider}, actual implementation is done inside {@link LDAPConnectionWrapper}
 * subclasses, so see it for code documentation.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPConnection{

    private ILDAPConnectionParamsProvider connectionParamsProvider;

    private LDAPConnectionWrapper connectionWrapper;

    /**
     * Creates a new instance of Connection.
     *
     * @param connectionParameter
     */
    public LDAPConnection(ILDAPConnectionParamsProvider connectionParamsProvider){
        this.connectionParamsProvider = connectionParamsProvider;
        this.connectionWrapper = new JNDIConnectionWrapper(connectionParamsProvider);
    }
    
    /**
     * Set an implementation of {@link LDAPConnectionWrapper} which will handle actual connection-related staff.
     * 
     * @param connectionWrapper implementation of {@link LDAPConnectionWrapper}
     */
    public void setConnectionWrapper(LDAPConnectionWrapper connectionWrapper) {
		this.connectionWrapper = connectionWrapper;
	}
    
    /**
     * Get current {@link LDAPConnectionWrapper} which performs all the connection-related staff for this {@link LDAPConnection}
     * 
     * @return implementation of {@link LDAPConnectionWrapper}
     */
    public LDAPConnectionWrapper getConnectionWrapper() {
		return connectionWrapper;
	}

    /**
     * Gets the connection parameter.
     * 
     * @return the connection parameter
     */
    public ILDAPConnectionParamsProvider getConnectionParamsProvider() {
        return connectionParamsProvider;
    }
    
    /**
     * Set {@link ILDAPConnectionParamsProvider} to this connection, can't be <code>null</code> 
     * or {@link IllegalArgumentException} will be thrown.
     * 
     * @param connectionParamsProvider, not <code>null</code>
     */
    public void setConnectionParamsProvider(ILDAPConnectionParamsProvider connectionParamsProvider) {
    	if (connectionParamsProvider == null){
    		throw new IllegalArgumentException("ILDAPConnectionParamsProvider can't be null!");
    	}
		this.connectionParamsProvider = connectionParamsProvider;
	}

    public void connect() throws UserManagementSystemCommunicationException {
    	this.connectionWrapper.connect();
    }
    
    public void disconnect(){
    	this.connectionWrapper.disconnect();
    }
    
    public void bind(
    		String bindPrincipal, String password
    		) throws LoginException, UserManagementSystemCommunicationException {
    	
    	this.connectionWrapper.bind(bindPrincipal, password);
    }

    public void createEntry(String entryDN, LDAPAttributeSet attributes) throws UserManagementSystemCommunicationException, LoginException{
    	this.connectionWrapper.createEntry(entryDN, attributes);
    }

    public void deleteEntry(String entryDN) throws UserManagementSystemCommunicationException, LoginException{
    	this.connectionWrapper.deleteEntry(entryDN);
    }

    public String modifyEntry(String entryDN, LDAPAttributeSet attributes, EntryModificationFlag modificationFlag) throws UserManagementSystemCommunicationException, LoginException{
    	return this.connectionWrapper.modifyEntry(entryDN, attributes, modificationFlag);
    }
    
    public Map<String, LDAPAttributeSet> search(String dn, LDAPAttributeSet searchAttributes, String[] returnAttributes) throws UserManagementSystemCommunicationException, LoginException{
    	return this.connectionWrapper.search(dn, searchAttributes, returnAttributes);
    }
    
    public Map<String, LDAPAttributeSet> search(String dn, String filterExpr, Object[] filterArgs, SearchScope scope) throws UserManagementSystemCommunicationException, LoginException{
    	return this.connectionWrapper.search(dn, filterExpr, filterArgs, scope);
    }
    
	public LDAPAttributeSet getAttributesForEntry(String dn) throws UserManagementSystemCommunicationException, LoginException {
		return this.connectionWrapper.getAttributesForEntry(dn);
	}

	public LDAPAttributeSet getAttributesForEntry(String dn, String[] attributeNames) throws UserManagementSystemCommunicationException, LoginException {
		return this.connectionWrapper.getAttributesForEntry(dn, attributeNames);
	}

	public Collection<String> getChildEntries(String parentName) throws UserManagementSystemCommunicationException, LoginException{
		return this.connectionWrapper.getChildEntries(parentName);
	}

    public void unbind() throws UserManagementSystemCommunicationException {
    	this.connectionWrapper.unbind();
    }

    public boolean isConnected() {
    	return this.connectionWrapper.isConnected();
    }
    
    public boolean entryExists(String entryName) throws LoginException{
    	return this.connectionWrapper.entryExists(entryName);
    }

}
