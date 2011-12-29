package org.nightlabs.jfire.base.security.integration.ldap.connection;

/**
 * Implementations are intended to hold LDAP connection parameters used by {@link LDAPConnection} instances.
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
        SIMPLE("simple"),

        /** SASL authentication using DIGEST-MD5. */
        SASL_DIGEST_MD5("DIGEST-MD5"),

        /** SASL authentication using CRAM-MD5. */
        SASL_CRAM_MD5("CRAM-MD5");

        private String stringValue;
        
        private AuthenticationMethod(String stringValue){
        	this.stringValue = stringValue;
        }
        
        public String stringValue(){
        	return stringValue;
        }

		/**
		 * Get possible {@link AuthenticationMethod}s names.
		 * 
		 * @return names of {@link AuthenticationMethod}s as {@link String}
		 */
        public static String[] getPossibleAuthenticationMethods(){
    		AuthenticationMethod[] possibleValues = AuthenticationMethod.values();
    		String[] names = new String[possibleValues.length];
    		for (int i = 0; i < possibleValues.length; i++) {
    			names[i] = possibleValues[i].stringValue();
    		}
    		return names;
    	}

        /**
         * Get {@link Enum} element by {@link String} value.
         * 
         * @param stringValue
         * @return {@link AuthenticationMethod} element or throws {@link IllegalArgumentException} if not found
         */
    	public static AuthenticationMethod findAuthenticationMethodByStringValue(String stringValue){
    	    for(AuthenticationMethod v : AuthenticationMethod.values()){
    	        if (v.stringValue().equals(stringValue)){
    	            return v;
    	        }
    	    }
    	    throw new IllegalArgumentException("Unable to find enum constant by String value: " + stringValue);
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
        
    	/**
    	 * Get possible {@link EncryptionMethod}s names.
    	 * 
    	 * @return names of {@link EncryptionMethod}s as {@link String}
    	 */
    	public static String[] getPossibleEncryptionMethods(){
    		EncryptionMethod[] possibleValues = EncryptionMethod.values();
    		String[] names = new String[possibleValues.length];
    		for (int i = 0; i < possibleValues.length; i++) {
    			names[i] = possibleValues[i].stringValue();
    		}
    		return names;
    	}

        /**
         * Get {@link Enum} element by {@link String} value.
         * 
         * @param stringValue
         * @return EncryptionMethod element or throws {@link IllegalArgumentException} if not found
         */
    	public static EncryptionMethod findEncryptionMethodByStringValue(String stringValue){
    	    for(EncryptionMethod v : EncryptionMethod.values()){
    	        if (v.stringValue().equals(stringValue)){
    	            return v;
    	        }
    	    }
    	    throw new IllegalArgumentException("Unable to find enum constant by String value: " + stringValue);
    	}

    }


    /**
     * Gets the auth method.
     * 
     * @return the auth method
     */
    public AuthenticationMethod getAuthenticationMethod();

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
