package org.nightlabs.jfire.base.security.integration.ldap.connection;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

import org.apache.directory.shared.ldap.util.LdapURL;
import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.security.integration.ldap.connection.ILDAPConnectionParamsProvider.AuthenticationMethod;

/**
 * A connection wrapper that uses JNDI.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 */
public class JNDIConnectionWrapper implements LDAPConnectionWrapper {

	private static final Logger logger = Logger.getLogger(JNDIConnectionWrapper.class);

	private static final String JAVA_NAMING_SECURITY_SASL_REALM = "java.naming.security.sasl.realm"; //$NON-NLS-1$
	private static final String JAVA_NAMING_LDAP_VERSION = "java.naming.ldap.version"; //$NON-NLS-1$

	private static final String COM_SUN_JNDI_DNS_TIMEOUT_RETRIES = "com.sun.jndi.dns.timeout.retries"; //$NON-NLS-1$
	private static final String COM_SUN_JNDI_DNS_TIMEOUT_INITIAL = "com.sun.jndi.dns.timeout.initial"; //$NON-NLS-1$
	private static final String COM_SUN_JNDI_LDAP_CONNECT_TIMEOUT = "com.sun.jndi.ldap.connect.timeout"; //$NON-NLS-1$


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
	public void connect() throws CommunicationException {

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
			
			throw new CommunicationException(
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
			) throws CommunicationException, AuthenticationException {

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
				
				throw new AuthenticationException(
						"LDAP login failed, see log for details. Cause " + e.getMessage()
						);
				
			}
				
		}else{
			String msg = "No connection to LDAP server at " + host + ":" + port;
			logger.error(msg);
			throw new CommunicationException(msg);
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

}
