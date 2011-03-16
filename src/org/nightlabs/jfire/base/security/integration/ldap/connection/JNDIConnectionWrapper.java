package org.nightlabs.jfire.base.security.integration.ldap.connection;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapName;
import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider.AuthenticationMethod;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection wrapper that uses JNDI.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 */
public class JNDIConnectionWrapper implements LDAPConnectionWrapper {

	private static final Logger logger = LoggerFactory.getLogger(JNDIConnectionWrapper.class);

	private static final String JAVA_NAMING_LDAP_VERSION = "java.naming.ldap.version"; //$NON-NLS-1$
	private static final String COM_SUN_JNDI_DNS_TIMEOUT_RETRIES = "com.sun.jndi.dns.timeout.retries"; //$NON-NLS-1$
	private static final String COM_SUN_JNDI_DNS_TIMEOUT_INITIAL = "com.sun.jndi.dns.timeout.initial"; //$NON-NLS-1$
	private static final String COM_SUN_JNDI_LDAP_CONNECT_TIMEOUT = "com.sun.jndi.ldap.connect.timeout"; //$NON-NLS-1$
	private static final String LDAP_SCHEME = "ldap://";

	private LDAPConnection connection;

	private String authMethod;

	private InitialLdapContext context;

	private boolean isConnected;

	/**
	 * Creates a new instance of JNDIConnectionContext.
	 * 
	 * @param connection
	 *            the connection
	 */
	public JNDIConnectionWrapper(LDAPConnection connection) {
		this.connection = connection;
	}

	/**
	 * {@inheritDoc}
	 * @throws CommunicationException 
	 */
	@Override
	public void connect() throws UserManagementSystemCommunicationException {
		
		// TODO: consider hiding connect() from client and do it implicitly in each method if not connected

		context = null;
		isConnected = true;

		Hashtable<String, String> environment = new Hashtable<String, String>();

		environment.put(JAVA_NAMING_LDAP_VERSION, "3"); //$NON-NLS-1$
		environment.put(COM_SUN_JNDI_LDAP_CONNECT_TIMEOUT, "10000"); //$NON-NLS-1$
		environment.put(COM_SUN_JNDI_DNS_TIMEOUT_INITIAL, "2000"); //$NON-NLS-1$
		environment.put(COM_SUN_JNDI_DNS_TIMEOUT_RETRIES, "3"); //$NON-NLS-1$

		String host = connection.getConnectionParamsProvider().getHost();
		int port = connection.getConnectionParamsProvider().getPort();
		environment.put(Context.PROVIDER_URL, LDAP_SCHEME + host + ':' + port);

		try{
			
			if (logger.isDebugEnabled()){
				logger.debug("Connecting to LDAP server with params: " + environment.toString());
			}
			
			environment.put(Context.INITIAL_CONTEXT_FACTORY, getDefaultLdapContextFactory());
			context = new InitialLdapContext(environment, null);
			
		}catch(NamingException e){
			
			logger.error("Can't connect to LDAP server at " + host + ":" + port, e);
			
			disconnect();
			
			throw new UserManagementSystemCommunicationException(
					"Can't connect to LDAP server at " + host + ":" + port +
					", see log for details. Cause: " + e.getMessage()
					);
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 * 
	 * TODO: consider calling this method internally, probably it's called more than needed :)
	 */
	@Override
	public void disconnect() {
		if (context != null) {
			try {
				context.close();
			} catch (NamingException e) {
				// ignore
			}
			context = null;
		}
		isConnected = false;
		System.gc();
	}

	/**
	 * {@inheritDoc}
	 * @throws CommunicationException 
	 * @throws AuthenticationException 
	 * 
	 * @throws NamingException
	 */
	@Override
	public void bind(
			String bindPrincipal, String bindCredentials
			) throws UserManagementSystemCommunicationException, LoginException {

		if (context != null && isConnected) {
			
			authMethod = AuthenticationMethod.NONE.stringValue();
			if (AuthenticationMethod.SIMPLE.equals(connection.getConnectionParamsProvider().getAuthMethod())) {
				authMethod = AuthenticationMethod.SIMPLE.stringValue();
			}

			// setup credentials
			try{
				context.removeFromEnvironment(Context.SECURITY_AUTHENTICATION);
				context.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
				context.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
	
				context.addToEnvironment(Context.SECURITY_AUTHENTICATION, authMethod);
	
				context.addToEnvironment(Context.SECURITY_PRINCIPAL, bindPrincipal);
				context.addToEnvironment(Context.SECURITY_CREDENTIALS, bindCredentials);
	
				context.reconnect(context.getConnectControls());
				
			}catch(NamingException e){

				String host = connection.getConnectionParamsProvider().getHost();
				int port = connection.getConnectionParamsProvider().getPort();

				logger.error("Failed to bind against LDAP server at " + host + ":" + port, e);
				
				disconnect();
				
				throw new LoginException(
						"LDAP login failed, see log for details. Cause " + e.getMessage()
						);
				
			}
				
		}else{
			String host = connection.getConnectionParamsProvider().getHost();
			int port = connection.getConnectionParamsProvider().getPort();
			String msg = "No connection to LDAP server at " + host + ":" + port;
			logger.error(msg);
			throw new UserManagementSystemCommunicationException(msg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unbind() {
		if (context != null && isConnected) {
			
			try{
				
				context.removeFromEnvironment(Context.SECURITY_AUTHENTICATION);
				context.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
				context.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
	
				context = new InitialLdapContext(context.getEnvironment(), context.getConnectControls());
				
			}catch(NamingException e){
				String host = connection.getConnectionParamsProvider().getHost();
				int port = connection.getConnectionParamsProvider().getPort();
				logger.error("Failed to unbind on LDAP server at " + host + ":" + port, e);
				disconnect(); 
			}
				
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public void createEntry(String dn, Map<String, Object[]> attributes) throws UserManagementSystemCommunicationException{
    	
        try {
        	
            // create entry
            context.createSubcontext(getSaveJndiName(dn), getAttributesFromMap(attributes));
            
        }catch(NamingException e){
			throw new UserManagementSystemCommunicationException("Failed to create an entry! Entry DN: " + dn, e);
        }

    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyEntry(
			String dn, Map<String, Object[]> attributes, EntryModificationFlag modificationFlag
			) throws UserManagementSystemCommunicationException {
		
		try{

	        // determine operation type
	        int operationType = DirContext.REPLACE_ATTRIBUTE;
	        switch(modificationFlag){
	        case MODIFY:
	        	operationType = DirContext.REPLACE_ATTRIBUTE;
	        	break;
	        	
	        case REMOVE:
	        	operationType = DirContext.REMOVE_ATTRIBUTE;
	        	break;
	        	
	        default: 
		        operationType = DirContext.REPLACE_ATTRIBUTE;
	        }
	        
	        // prepare mododifcation items
	        Attributes translatedAttributes = getAttributesFromMap(attributes);
	        ModificationItem[] modificationItems = new ModificationItem[translatedAttributes.size()];
	        int i = 0;
	        for (Enumeration<? extends Attribute> attributesEnum = translatedAttributes.getAll(); attributesEnum.hasMoreElements();) {
	        	modificationItems[i] = new ModificationItem(operationType, attributesEnum.nextElement());
	        	i++;
			}
	        
	        // perform modification
	        context.modifyAttributes(getSaveJndiName(dn), modificationItems);
	        
		}catch(NamingException e){
			throw new UserManagementSystemCommunicationException("Entry modification failed! Entry: " + dn, e);
		}

	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Enumeration<?> search(
			String dn, Map<String, Object[]> searchAttributes, String[] returnAttributes
			) throws UserManagementSystemCommunicationException {

		try{
			
			return context.search(dn, getAttributesFromMap(searchAttributes), returnAttributes);
			
		}catch(NamingException e){
			throw new UserManagementSystemCommunicationException("Search failed!");
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @throws UserManagementSystemCommunicationException 
	 */
	@Override
	public HashMap<String, Object[]> getAttribbutesForEntry(String dn) throws UserManagementSystemCommunicationException {
		try {
			return getAttributesMap(context.getAttributes(dn));
		} catch (NamingException e) {
			throw new UserManagementSystemCommunicationException("Failed to get attributes from entry with DN: " + dn);
		}
	}
    
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isConnected() {
		// It's not a very troubleproof strategy to check some internal flag. But since there's no way
		// to ping a LDAPServer via JNDI (we can just recreate a JNDI context, perform a search or lookup)
		// we need to maintain these flags carefully so they will reflect the real connection state.
		return context != null && isConnected;
	}
	
	
	/**
	 * Translates {@link Attributes} to {@link HashMap} 
	 * 
	 * @param attributes if <code>null</code> is passed it returns and empty {@link HashMap}
	 * @return
	 * @throws NamingException 
	 */
	private static HashMap<String, Object[]> getAttributesMap(Attributes attributes) throws NamingException{
		
		HashMap<String, Object[]> attributesMap = new HashMap<String, Object[]>();
		
		if (attributes == null){
			return attributesMap;
		}
		
		for (Enumeration<? extends Attribute> attsEnum = attributes.getAll(); attsEnum.hasMoreElements();){
			Attribute attr = attsEnum.nextElement();
			attributesMap.put(attr.getID(), getAttributeValues(attr));
		}
		
		return attributesMap;
	}
	
	private static Object[] getAttributeValues(Attribute attr) throws NamingException{
		
		if (attr == null){
			return new Object[0];
		}
		
		NamingEnumeration<?> allValues = attr.getAll();
		List<Object> valuesList = new ArrayList<Object>();
		for (Enumeration<?> valuesEnum = allValues; valuesEnum.hasMoreElements();) {
			valuesList.add(valuesEnum.nextElement());
		}
		
		return valuesList.toArray();
	}

	/**
	 * Translates a {@link HashMap} of attributes into JNDI {@link Attributes}
	 * 
	 * @param attributes if <code>null</code> is passed it returs an empty {@link Attributes} object 
	 * @return
	 */
	private static Attributes getAttributesFromMap(Map<String, Object[]> attributes){
		Attributes atts = new BasicAttributes();

		if (attributes == null){
			return atts;
		}
		
		for (String attributeName : attributes.keySet()){
        	
        	Object attributeValue = attributes.get(attributeName);
        	
        	Attribute attribute = new BasicAttribute(attributeName);
        	if (attributeValue != null 
        			&& attributeValue.getClass().isArray()){
        		for (Object obj : (Object[]) attributeValue) {
					attribute.add(obj);
				}
        	}else if (attributeValue != null){
        		attribute.add(attributeValue);
        	}
        	
        	atts.put(attribute);
        }
        
        return atts;
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
     * @throws NamingException 
     */
    private static String getDefaultLdapContextFactory() throws NamingException {

        try{
            
        	String sun = "com.sun.jndi.ldap.LdapCtxFactory"; //$NON-NLS-1$
            Class.forName(sun);
            return sun;
            
        }catch (ClassNotFoundException e){
        	logger.warn("com.sun.jndi.ldap.LdapCtxFactory class not found!");
        }
        
        try {
            
        	String apache = "org.apache.harmony.jndi.provider.ldap.LdapContextFactory"; //$NON-NLS-1$
            Class.forName(apache);
            return apache;
            
        }catch(ClassNotFoundException e){
        	logger.warn("org.apache.harmony.jndi.provider.ldap.LdapContextFactory class not found!");
        }

        throw new NamingException("No LDAP ContextFactory found!");
        
    }
    
    /**
     * Gets a Name object that is save for JNDI operations.
     * <p>
     * In JNDI we have could use the following classes for names:
     * <ul>
     * <li>DN as String</li>
     * <li>javax.naming.CompositeName</li>
     * <li>javax.naming.ldap.LdapName (since Java5)</li>
     * <li>org.apache.directory.shared.ldap.name.LdapDN</li>
     * </ul>
     * <p>
     * There are some drawbacks when using this classes:
     * <ul>
     * <li>When passing DN as String, JNDI doesn't handle slashes '/' correctly.
     * So we must use a Name object here.</li>
     * <li>With CompositeName we have the same problem with slashes '/'.</li>
     * <li>When using LdapDN from shared-ldap, JNDI uses the toString() method
     * and LdapDN.toString() returns the normalized ATAV, but we need the
     * user provided ATAV.</li>
     * <li>When using LdapName for the empty DN (Root DSE) JNDI _sometimes_ throws
     * an Exception (java.lang.IndexOutOfBoundsException: Posn: -1, Size: 0
     * at javax.naming.ldap.LdapName.getPrefix(LdapName.java:240)).</li>
     * <li>Using LdapDN for the RootDSE doesn't work with Apache Harmony because
     * its JNDI provider only accepts intstances of CompositeName or LdapName.</li>
     * </ul>
     * <p>
     * So we use LdapName as default and the CompositeName for the empty DN.
     * 
     * @param name the DN
     * 
     * @return the save JNDI name
     * 
     * @throws InvalidNameException the invalid name exception
     */
    private static Name getSaveJndiName(String name) throws InvalidNameException{
        if (name == null || "".equals(name)){
            return new CompositeName();
        }else{
            return new LdapName(name);
        }
    }

}
