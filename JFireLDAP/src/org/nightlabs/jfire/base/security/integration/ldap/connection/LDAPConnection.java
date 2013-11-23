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
 * implementations. Default is {@link JNDIConnectionWrapper}.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPConnection{

    private ILDAPConnectionParamsProvider connectionParamsProvider;

    private LDAPConnectionWrapper connectionWrapper;

    /**
     * Creates a new instance of {@link LDAPConnection} with given {@link ILDAPConnectionParamsProvider} 
     * using default {@link JNDIConnectionWrapper}.
     *
     * @param connectionParamsProvider
     */
    public LDAPConnection(ILDAPConnectionParamsProvider connectionParamsProvider){
    	this(connectionParamsProvider, new JNDIConnectionWrapper(connectionParamsProvider));
    }

    /**
     * Creates a new instance of {@link LDAPConnection} with given {@link ILDAPConnectionParamsProvider} 
     * and {@link LDAPConnectionWrapper} implementation.
     *
     * @param connectionParamsProvider
     * @param connectionWrapper can't be <code>null</code>, {@link IllegalArgumentException} will be thrown otherwise
     */
    public LDAPConnection(ILDAPConnectionParamsProvider connectionParamsProvider, LDAPConnectionWrapper connectionWrapper){
    	if (connectionWrapper == null){
    		throw new IllegalArgumentException("LDAPConnectionWrapper cannot be null!");
    	}
        this.connectionParamsProvider = connectionParamsProvider;
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

    /**
     * @see LDAPConnectionWrapper
     * @throws UserManagementSystemCommunicationException
     */
    public void connect() throws UserManagementSystemCommunicationException {
    	this.connectionWrapper.connect();
    }
    
    /**
     * @see LDAPConnectionWrapper
     */
    public void disconnect(){
    	this.connectionWrapper.disconnect();
    }
    
    /**
     * @see LDAPConnectionWrapper
     * 
     * @param bindPrincipal
     * @param password
     * @throws LoginException
     * @throws UserManagementSystemCommunicationException
     */
    public void bind(
    		String bindPrincipal, String password
    		) throws LoginException, UserManagementSystemCommunicationException {
    	
    	this.connectionWrapper.bind(bindPrincipal, password);
    }

    /**
     * @see LDAPConnectionWrapper
     * 
     * @param entryDN
     * @param attributes
     * @throws UserManagementSystemCommunicationException
     * @throws LoginException
     */
    public void createEntry(String entryDN, LDAPAttributeSet attributes) throws UserManagementSystemCommunicationException, LoginException{
    	this.connectionWrapper.createEntry(entryDN, attributes);
    }

    /**
     * @see LDAPConnectionWrapper
     * 
     * @param entryDN
     * @throws UserManagementSystemCommunicationException
     * @throws LoginException
     */
    public void deleteEntry(String entryDN) throws UserManagementSystemCommunicationException, LoginException{
    	this.connectionWrapper.deleteEntry(entryDN);
    }

    public String modifyEntry(String entryDN, LDAPAttributeSet attributes, EntryModificationFlag modificationFlag) throws UserManagementSystemCommunicationException, LoginException{
    	return this.connectionWrapper.modifyEntry(entryDN, attributes, modificationFlag);
    }
    
    /**
     * @see LDAPConnectionWrapper
     * 
     * @param dn
     * @param searchAttributes
     * @param returnAttributes
     * @return
     * @throws UserManagementSystemCommunicationException
     * @throws LoginException
     */
    public Map<String, LDAPAttributeSet> search(String dn, LDAPAttributeSet searchAttributes, String[] returnAttributes) throws UserManagementSystemCommunicationException, LoginException{
    	return this.connectionWrapper.search(dn, searchAttributes, returnAttributes);
    }
    
    /**
     * @see LDAPConnectionWrapper
     * 
     * @param dn
     * @param filterExpr
     * @param filterArgs
     * @param scope
     * @return
     * @throws UserManagementSystemCommunicationException
     * @throws LoginException
     */
    public Map<String, LDAPAttributeSet> search(String dn, String filterExpr, Object[] filterArgs, SearchScope scope) throws UserManagementSystemCommunicationException, LoginException{
    	return this.connectionWrapper.search(dn, filterExpr, filterArgs, scope);
    }
    
    /**
     * @see LDAPConnectionWrapper
     * 
     * @param dn
     * @return
     * @throws UserManagementSystemCommunicationException
     * @throws LoginException
     */
	public LDAPAttributeSet getAttributesForEntry(String dn) throws UserManagementSystemCommunicationException, LoginException {
		return this.connectionWrapper.getAttributesForEntry(dn);
	}

	/**
     * @see LDAPConnectionWrapper
	 * 
	 * @param dn
	 * @param attributeNames
	 * @return
	 * @throws UserManagementSystemCommunicationException
	 * @throws LoginException
	 */
	public LDAPAttributeSet getAttributesForEntry(String dn, String[] attributeNames) throws UserManagementSystemCommunicationException, LoginException {
		return this.connectionWrapper.getAttributesForEntry(dn, attributeNames);
	}

	/**
     * @see LDAPConnectionWrapper
	 * 
	 * @param parentName
	 * @return
	 * @throws UserManagementSystemCommunicationException
	 * @throws LoginException
	 */
	public Collection<String> getChildEntries(String parentName) throws UserManagementSystemCommunicationException, LoginException{
		return this.connectionWrapper.getChildEntries(parentName);
	}

	/**
     * @see LDAPConnectionWrapper
	 * 
	 * @throws UserManagementSystemCommunicationException
	 */
    public void unbind() throws UserManagementSystemCommunicationException {
    	this.connectionWrapper.unbind();
    }

    /**
     * @see LDAPConnectionWrapper
     * 
     * @return
     */
    public boolean isConnected() {
    	return this.connectionWrapper.isConnected();
    }
    
    /**
     * @see LDAPConnectionWrapper
     * 
     * @param entryName
     * @return
     * @throws LoginException
     */
    public boolean entryExists(String entryName) throws LoginException{
    	return this.connectionWrapper.entryExists(entryName);
    }

}
