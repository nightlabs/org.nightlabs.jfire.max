package org.nightlabs.jfire.base.security.integration.ldap.connection;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.security.auth.login.LoginException;

import org.apache.directory.shared.ldap.util.LdapURL;
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

	private static final String JAVA_NAMING_SECURITY_SASL_REALM = "java.naming.security.sasl.realm"; //$NON-NLS-1$
	private static final String JAVA_NAMING_LDAP_VERSION = "java.naming.ldap.version"; //$NON-NLS-1$

	private static final String COM_SUN_JNDI_DNS_TIMEOUT_RETRIES = "com.sun.jndi.dns.timeout.retries"; //$NON-NLS-1$
	private static final String COM_SUN_JNDI_DNS_TIMEOUT_INITIAL = "com.sun.jndi.dns.timeout.initial"; //$NON-NLS-1$
	private static final String COM_SUN_JNDI_LDAP_CONNECT_TIMEOUT = "com.sun.jndi.ldap.connect.timeout"; //$NON-NLS-1$

    public static final String REFERRAL_IGNORE = "ignore"; //$NON-NLS-1$

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
		isConnected = true;

		Hashtable<String, String> environment = new Hashtable<String, String>();

		environment.put(JAVA_NAMING_LDAP_VERSION, "3"); //$NON-NLS-1$
		environment.put(COM_SUN_JNDI_LDAP_CONNECT_TIMEOUT, "10000"); //$NON-NLS-1$
		environment.put(COM_SUN_JNDI_DNS_TIMEOUT_INITIAL, "2000"); //$NON-NLS-1$
		environment.put(COM_SUN_JNDI_DNS_TIMEOUT_RETRIES, "3"); //$NON-NLS-1$

		String host = connection.getConnectionParamsProvider().getHost();
		int port = connection.getConnectionParamsProvider().getPort();
		environment.put(Context.PROVIDER_URL, LdapURL.LDAP_SCHEME + host + ':' + port);

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

		String host = connection.getConnectionParamsProvider().getHost();
		int port = connection.getConnectionParamsProvider().getPort();
		
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
				context.removeFromEnvironment(JAVA_NAMING_SECURITY_SASL_REALM);
	
				context.addToEnvironment(Context.SECURITY_AUTHENTICATION, authMethod);
	
				context.addToEnvironment(Context.SECURITY_PRINCIPAL, bindPrincipal);
				context.addToEnvironment(Context.SECURITY_CREDENTIALS, bindCredentials);
	
				context.reconnect(context.getConnectControls());
				
			}catch(NamingException e){
				
				logger.error("Failed to bind against LDAP server at " + host + ":" + port, e);
				
				disconnect();
				
				throw new LoginException(
						"LDAP login failed, see log for details. Cause " + e.getMessage()
						);
				
			}
				
		}else{
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
		disconnect();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public void createEntry(String dn, Attributes attributes, Control[] controls){
    	
        try {
        	
            LdapContext modCtx = context.newInstance(controls);

            // TODO: deal with referrals. We can leave it up to service provider or make it manually
            // like in Apache Studio
            modCtx.addToEnvironment(Context.REFERRAL, REFERRAL_IGNORE);

            // create entry
            modCtx.createSubcontext(getSaveJndiName(dn), attributes);
            
        }catch(NamingException ne){
        	// TODO
        }

    }

    
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isConnected() {
		return context != null && isConnected;
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
