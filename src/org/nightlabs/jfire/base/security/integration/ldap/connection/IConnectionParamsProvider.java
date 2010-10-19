package org.nightlabs.jfire.base.security.integration.ldap.connection;

/**
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public interface IConnectionParamsProvider{

    /**
     * Enum for the used authentication method.
     * 
     */
    public enum AuthenticationMethod{

        /** No authentication, anonymous bind. */
        NONE,

        /** Simple authentication, simple bind. */
        SIMPLE
    }

    /**
     * Enum for the used encryption method.
     * 
     */
    public enum EncryptionMethod
    {

        /** No encryption. */
        NONE,

        /** SSL encryption. */
        LDAPS,

        /** Encryption using Start TLS extension. */
        START_TLS
    }


    /**
     * Gets the auth method.
     * 
     * @return the auth method
     */
    public AuthenticationMethod getAuthMethod();

    /**
     * Gets the encryption method.
     * 
     * @return the encryption method
     */
    public EncryptionMethod getEncryptionMethod();

    /**
     * Gets the host.
     * 
     * @return the host
     */
    public String getHost();

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName();

    /**
     * Gets the port.
     * 
     * @return the port
     */
    public int getPort();

}
