package org.nightlabs.jfire.base.security.integration.ldap.connection;

/**
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public interface ILDAPConnectionParamsProvider{

    /**
     * Enum for the used authentication method.
     * 
     */
    public enum AuthenticationMethod {

        /** No authentication, anonymous bind. */
        NONE("none"),

        /** Simple authentication, simple bind. */
        SIMPLE("simple");
        
        private String stringValue;
        
        private AuthenticationMethod(String stringValue){
        	this.stringValue = stringValue;
        }
        
        public String stringValue(){
        	return stringValue;
        }
        
    }

    /**
     * Enum for the used encryption method.
     * 
     */
    public enum EncryptionMethod {

        /** No encryption. */
        NONE("None"),

        /** SSL encryption. */
        LDAPS("SSL (LDAPS)"),

        /** Encryption using Start TLS extension. */
        START_TLS("Start TLS");
        
        private String stringValue;
        
        private EncryptionMethod(String stringValue){
        	this.stringValue = stringValue;
        }
        
        public String stringValue(){
        	return stringValue;
        }
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
     * Gets the port.
     * 
     * @return the port
     */
    public int getPort();
    

}
