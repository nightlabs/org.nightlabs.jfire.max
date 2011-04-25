package org.nightlabs.jfire.base.security.integration.ldap.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InsufficientResourcesException;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import javax.naming.event.EventContext;
import javax.naming.event.EventDirContext;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.UnsolicitedNotificationEvent;
import javax.naming.ldap.UnsolicitedNotificationListener;
import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttribute;
import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttributeSet;
import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider.AuthenticationMethod;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection wrapper that uses JNDI.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 */
public class JNDIConnectionWrapper implements LDAPConnectionWrapper{

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

		context = null;

		String host = connection.getConnectionParamsProvider().getHost();
		int port = connection.getConnectionParamsProvider().getPort();

		try{
			Hashtable<String, String> environment = new Hashtable<String, String>();
			synchronized (environment) {
				environment.put(JAVA_NAMING_LDAP_VERSION, "3"); //$NON-NLS-1$
				environment.put(COM_SUN_JNDI_LDAP_CONNECT_TIMEOUT, "10000"); //$NON-NLS-1$
				environment.put(COM_SUN_JNDI_DNS_TIMEOUT_INITIAL, "2000"); //$NON-NLS-1$
				environment.put(COM_SUN_JNDI_DNS_TIMEOUT_RETRIES, "3"); //$NON-NLS-1$
				environment.put(Context.PROVIDER_URL, LDAP_SCHEME + host + ':' + port);
				environment.put(Context.INITIAL_CONTEXT_FACTORY, getDefaultLdapContextFactory());

				if (logger.isDebugEnabled()){
					logger.debug("Connecting to LDAP server with params: " + environment.toString());
				}
				
				context = new InitialLdapContext(environment, null);
				isConnected = true;
				
				configureConnectionProblemsListener();
			}
			
		}catch(NamingException e){
			logger.error(String.format("Can't connect to LDAP server at %s:%s", host, port), e);
			
			disconnect();
			
			throw new UserManagementSystemCommunicationException(
					String.format(
							"Can't connect to LDAP server at %s:%s, see log for details. Cause: %s", host, port, e.getMessage()
							));
		}
		
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
				synchronized (context) {
					context.removeFromEnvironment(Context.SECURITY_AUTHENTICATION);
					context.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
					context.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
		
					context.addToEnvironment(Context.SECURITY_AUTHENTICATION, authMethod);
		
					context.addToEnvironment(Context.SECURITY_PRINCIPAL, bindPrincipal);
					context.addToEnvironment(Context.SECURITY_CREDENTIALS, bindCredentials);
		
					context.reconnect(context.getConnectControls());
				}
			}catch(NamingException e){
				logger.error(String.format("Failed to bind against LDAP server at %s:%s", connection.getConnectionParamsProvider().getHost(), connection.getConnectionParamsProvider().getPort()), e);
				
				unbind();
				
				throw new LoginException(
						"LDAP login failed, see log for details. Cause: " + e.getMessage()
						);
			}
		}else{
			throw new UserManagementSystemCommunicationException(
					String.format("No connection to LDAP server at %s:%s", connection.getConnectionParamsProvider().getHost(), connection.getConnectionParamsProvider().getPort())
					);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unbind() {
		if (context != null && isConnected) {
			try{
				synchronized(context){
					context.removeFromEnvironment(Context.SECURITY_AUTHENTICATION);
					context.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
					context.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
		
					context = new InitialLdapContext(context.getEnvironment(), context.getConnectControls());
				}
			}catch(NamingException e){
				logger.error(
						String.format("Failed to unbind on LDAP server at %s:%s", connection.getConnectionParamsProvider().getHost(), connection.getConnectionParamsProvider().getPort()), e
						);
				disconnect(); 
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public void createEntry(String dn, LDAPAttributeSet attributes) throws UserManagementSystemCommunicationException{
        try {
        	synchronized (context) {
                context.createSubcontext(getSaveJndiName(dn), getJNDIAttributes(attributes));
			}
        }catch(NamingException e){
			throw new UserManagementSystemCommunicationException("Failed to create an entry! Entry DN: " + dn, e);
        }
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
    public void deleteEntry(String dn) throws UserManagementSystemCommunicationException{
        try {
        	synchronized (context) {
                context.destroySubcontext(getSaveJndiName(dn));
			}
        }catch(NamingException e){
			throw new UserManagementSystemCommunicationException("Failed to delete an entry! Entry DN: " + dn, e);
        }
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyEntry(
			String dn, LDAPAttributeSet attributes, EntryModificationFlag modificationFlag
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
	        
	        synchronized (context) {
		        // prepare mododifcation items
		        Attributes translatedAttributes = getJNDIAttributes(attributes);
		        ModificationItem[] modificationItems = new ModificationItem[translatedAttributes.size()];
		        int i = 0;
		        for (Enumeration<? extends Attribute> attributesEnum = translatedAttributes.getAll(); attributesEnum.hasMoreElements();) {
		        	modificationItems[i] = new ModificationItem(operationType, attributesEnum.nextElement());
		        	i++;
				}
		        // perform modification
		        context.modifyAttributes(getSaveJndiName(dn), modificationItems);
			}
		}catch(NamingException e){
			throw new UserManagementSystemCommunicationException("Entry modification failed! Entry: " + dn, e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, LDAPAttributeSet> search(
			String dn, LDAPAttributeSet searchAttributes, String[] returnAttributes
			) throws UserManagementSystemCommunicationException {
		try{
			synchronized (context){
				NamingEnumeration<SearchResult> result = context.search(getSaveJndiName(dn), getJNDIAttributes(searchAttributes), returnAttributes);
				try{
					Map<String, LDAPAttributeSet> returnMap = new HashMap<String, LDAPAttributeSet>();
					while (result.hasMoreElements()) {
						SearchResult searchResult = result.nextElement();
						returnMap.put(searchResult.getNameInNamespace(), getLDAPAttributeSet(searchResult.getAttributes()));
					}
					return returnMap;
				}finally{
					result.close();
				}
			}
		}catch(NamingException e){
			throw new UserManagementSystemCommunicationException("Search failed!" , e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public LDAPAttributeSet getAttribbutesForEntry(String dn) throws UserManagementSystemCommunicationException {
		try {
			synchronized (context) {
				return getLDAPAttributeSet(context.getAttributes(getSaveJndiName(dn)));
			}
		} catch (NamingException e) {
			throw new UserManagementSystemCommunicationException("Failed to get attributes from entry with DN: " + dn, e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<String> getChildEntries(String parentName) throws UserManagementSystemCommunicationException{
		try {
			synchronized (context) {
				NamingEnumeration<NameClassPair> childEntries = context.list(getSaveJndiName(parentName));
				try{
					Collection<String> childNames = new ArrayList<String>();
					while (childEntries.hasMoreElements()) {
						NameClassPair pair = childEntries.nextElement();
						childNames.add(pair.getNameInNamespace());
					}
					return childNames;
				}finally{
					childEntries.close();
				}
			}
		} catch (NamingException e) {
			throw new UserManagementSystemCommunicationException("Failed to get child entries for parent with DN: " + parentName, e);
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
	 * Performs disconnection and releases all resources
	 */
	private void disconnect() {
		if (context != null) {
			try {
				context.close();
			} catch (NamingException e) {
				logger.debug(e.getMessage(), e);
			}
			context = null;
		}
		isConnected = false;
		System.gc();
	}

	private static final int MAX_TRIES = 3;
	/**
	 * LDAP servers often have an idle timeout period after which they will close connections no longer being used.
	 * That's why we need this listener to catch connection exceptions from LDAP server. If such exception is caught
	 * it will be logged and we'll try (MAX_TRIES times) to establish a new connection immidiately.
	 * 
	 * @throws NamingException
	 */
	private void configureConnectionProblemsListener() throws NamingException{
		
		// we can't add a listener in InitialLdapContext since it does not implemet EventContext interface
		Object eventContext = context.lookup("");
		
		if (eventContext instanceof EventContext){
			
			UnsolicitedNotificationListener ldapPushNotificationsListener = new UnsolicitedNotificationListener() {
				
				@Override
				public void namingExceptionThrown(NamingExceptionEvent event) {
					if (event.getException() instanceof CommunicationException
							|| event.getException() instanceof ServiceUnavailableException
							|| event.getException() instanceof InsufficientResourcesException){
						logger.warn("Recieved communication exception from LDAP server, trying to reconnect.");
						int tries = 0;
						while (true){
							try {
								disconnect();
								connect();
								break;
							} catch (Exception e) {
								tries++;
								if (tries < MAX_TRIES){
									logger.warn("Faield to reconnect to LDAP server, retrying... Tries left: " + (MAX_TRIES - tries), e);
									continue;
								}else{
									logger.error("Faield to reconnect to LDAP server! Number of tries: " + MAX_TRIES, e);
									break;
								}
							}
						}
					}else{
						logger.warn("Recieved communication exception from LDAP server", event.getException());
					}
				}
				
				@Override
				public void notificationReceived(
						UnsolicitedNotificationEvent unsolicitednotificationevent
						) {
					// we are not inersted in any notifications, so do nothig
				}
			};

			// Name and scope parameters are not used when adding UnsolicitedNotificationListener,
			// so we just specify a blank string and OBJECT_SCOPE to avoid possible NullPointerExceptions.
			((EventContext) eventContext).addNamingListener(
					"", EventDirContext.OBJECT_SCOPE, ldapPushNotificationsListener
					);
		}
		
	}
	
	/**
	 * Translates {@link Attributes} to {@link LDAPAttributeSet} 
	 * 
	 * @param attributes if <code>null</code> is passed it returns and empty {@link HashMap}
	 * @return
	 * @throws NamingException 
	 */
	private static LDAPAttributeSet getLDAPAttributeSet(Attributes attributes) throws NamingException{
		
		LDAPAttributeSet attributeSet = new LDAPAttributeSet();
		
		if (attributes == null){
			return attributeSet;
		}
		
		for (Enumeration<? extends Attribute> attsEnum = attributes.getAll(); attsEnum.hasMoreElements();){
			Attribute attr = attsEnum.nextElement();
			attributeSet.createAttribute(attr.getID(), getAttributeValues(attr));
		}
		
		return attributeSet;
	}
	
	private static Collection<Object> getAttributeValues(Attribute attr) throws NamingException{
		
		if (attr == null){
			return null;
		}
		
		NamingEnumeration<?> allValues = attr.getAll();
		List<Object> valuesList = new ArrayList<Object>();
		for (Enumeration<?> valuesEnum = allValues; valuesEnum.hasMoreElements();) {
			valuesList.add(valuesEnum.nextElement());
		}
		return valuesList;
	}

	/**
	 * Translates {@link LDAPAttributeSet} into JNDI {@link Attributes}
	 * 
	 * @param attributeSet if <code>null</code> is passed it returs an empty {@link Attributes} object 
	 * @return
	 */
	private static Attributes getJNDIAttributes(LDAPAttributeSet attributeSet){
		Attributes atts = new BasicAttributes(true);	// "true" indicates that we consider attribute names case-insensitive as they are in LDAP

		if (attributeSet == null){
			return atts;
		}
		
		for (LDAPAttribute<Object> ldapAttribute : attributeSet){
			
        	Attribute attribute = new BasicAttribute(ldapAttribute.getName());
			if (ldapAttribute.hasSingleValue()){
        		attribute.add(ldapAttribute.getValue());
			}else{
				for (Object value : ldapAttribute.getValues()){
					attribute.add(value);
				}
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
