package org.nightlabs.jfire.base.security.integration.ldap.connection;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

import org.apache.directory.shared.ldap.util.LdapURL;

/**
 * A connection wrapper that uses JNDI.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 */
public class JNDIConnectionWrapper implements ConnectionWrapper {

	private static final String AUTHMETHOD_NONE = "none"; //$NON-NLS-1$

	private static final String AUTHMETHOD_SIMPLE = "simple"; //$NON-NLS-1$

	private static final String NO_CONNECTION = "No connection"; //$NON-NLS-1$

	private static final String JAVA_NAMING_SECURITY_SASL_REALM = "java.naming.security.sasl.realm"; //$NON-NLS-1$

	private static final String COM_SUN_JNDI_DNS_TIMEOUT_RETRIES = "com.sun.jndi.dns.timeout.retries"; //$NON-NLS-1$

	private static final String COM_SUN_JNDI_DNS_TIMEOUT_INITIAL = "com.sun.jndi.dns.timeout.initial"; //$NON-NLS-1$

	private static final String COM_SUN_JNDI_LDAP_CONNECT_TIMEOUT = "com.sun.jndi.ldap.connect.timeout"; //$NON-NLS-1$

	private static final String JAVA_NAMING_LDAP_VERSION = "java.naming.ldap.version"; //$NON-NLS-1$


	private Connection connection;

	private String authMethod;

	private InitialLdapContext context;

	private boolean isConnected;

	/**
	 * Creates a new instance of JNDIConnectionContext.
	 * 
	 * @param connection
	 *            the connection
	 */
	public JNDIConnectionWrapper(Connection connection) {
		this.connection = connection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect() throws NamingException {

		try {
			
			context = null;
			isConnected = true;

			// setup connection parameters
			String host = connection.getConnectionParamsProvider().getHost();
			int port = connection.getConnectionParamsProvider().getPort();

			Hashtable<String, String> environment = new Hashtable<String, String>();

			environment.put(Context.INITIAL_CONTEXT_FACTORY, Connection.getDefaultLdapContextFactory());
			environment.put(JAVA_NAMING_LDAP_VERSION, "3"); //$NON-NLS-1$

			environment.put(COM_SUN_JNDI_LDAP_CONNECT_TIMEOUT, "10000"); //$NON-NLS-1$
			environment.put(COM_SUN_JNDI_DNS_TIMEOUT_INITIAL, "2000"); //$NON-NLS-1$
			environment.put(COM_SUN_JNDI_DNS_TIMEOUT_RETRIES, "3"); //$NON-NLS-1$

			environment.put(Context.PROVIDER_URL, LdapURL.LDAP_SCHEME + host + ':' + port);

			context = new InitialLdapContext(environment, null);

		} catch (NamingException ne) {
			disconnect();
			throw ne;
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
	 * 
	 * @throws NamingException
	 */
	@Override
	public void bind(String bindPrincipal, String bindCredentials) throws NamingException {
		if (context != null && isConnected) {
			
			// setup authentication methdod
			authMethod = AUTHMETHOD_NONE;
			if (connection.getConnectionParamsProvider().getAuthMethod() == IConnectionParamsProvider.AuthenticationMethod.SIMPLE) {
				authMethod = AUTHMETHOD_SIMPLE;
			}

			// setup credentials
			try {
				
				context.removeFromEnvironment(Context.SECURITY_AUTHENTICATION);
				context.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
				context.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
				context.removeFromEnvironment(JAVA_NAMING_SECURITY_SASL_REALM);

				context.addToEnvironment(Context.SECURITY_AUTHENTICATION, authMethod);

				context.addToEnvironment(Context.SECURITY_PRINCIPAL, bindPrincipal);
				context.addToEnvironment(Context.SECURITY_CREDENTIALS, bindCredentials);

				context.reconnect(context.getConnectControls());
				
			} catch (NamingException ne) {
				
				disconnect();
				throw ne;
				
			}
			
		} else {
			throw new NamingException(NO_CONNECTION);
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

}
