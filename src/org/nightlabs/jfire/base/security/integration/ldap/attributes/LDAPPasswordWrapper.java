package org.nightlabs.jfire.base.security.integration.ldap.attributes;


import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.nightlabs.util.IOUtil;
import org.nightlabs.util.Util;

/**
 * The {@link LDAPPasswordWrapper} is used to represent a hashed or plain text password. 
 * 
 * The following hash methods are supported: SHA, MD5 (as it's supported in {@link Util})
 *
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 * 
 */
public class LDAPPasswordWrapper{
	
    /**
     *  The hash method. 
     */
    private String hashMethod;

    /** 
     * The hashed password. 
     */
    private byte[] hashedPassword;

    
    /**
     * Creates a new instance of Password.
     *
     * @param password the password, either hashed or plain text
     */
    public LDAPPasswordWrapper(String password) {
        if (password == null || password.isEmpty()){
            throw new IllegalArgumentException("Password should be not NULL and not empty!");
        }
        
        if (password.indexOf('{') == 0 && password.indexOf('}') > 0){	// hashed password given
        	hashMethod = password.substring(password.indexOf('{') + 1, password.indexOf('}'));
            String rest = password.substring(hashMethod.length() + 2);

            if (Util.HASH_ALGORITHM_SHA.equalsIgnoreCase(hashMethod) 
            		|| Util.HASH_ALGORITHM_MD5.equalsIgnoreCase(hashMethod)){
                
            	hashedPassword = base64decodeToByteArray(rest);
                
            } else {
            	throw new IllegalArgumentException(
            			String.format(
            					"Unsupported hash method specifed! Supported methods: %s and Plaintext(null). Given method: %s", 
            					Util.HASH_ALGORITHM_SHA + ", " + Util.HASH_ALGORITHM_MD5, hashMethod));
            }
        } else {	// plain text password given
            hashMethod = null;
            hashedPassword = utf8encode(password);
        }
    }


    /**
     * Creates a new instance of Password and calculates the hashed password.
     *
     * @param passwordAsPlaintext the plain text password
     * @param hashMethod the hash method to use, could be <code>null</code> if no hash is needed - password is stored as plain text
     * 
     * @throws IllegalArgumentException if the given hash method is not
     *         supported of if the given password is null
     */
    public LDAPPasswordWrapper(String passwordAsPlaintext, String hashMethod) {
        if (!(hashMethod == null 
        		|| Util.HASH_ALGORITHM_SHA.equalsIgnoreCase(hashMethod) 
        		|| Util.HASH_ALGORITHM_MD5.equalsIgnoreCase(hashMethod))) {
            
        	throw new IllegalArgumentException(
        			String.format(
        					"Unsupported hash method specifed! Supported methods: %s and Plaintext(null). Given method: %s", 
        					Util.HASH_ALGORITHM_SHA + ", " + Util.HASH_ALGORITHM_MD5, hashMethod));
        }
        
        if (passwordAsPlaintext == null || passwordAsPlaintext.isEmpty()) {
            throw new IllegalArgumentException("Empty password is not allowed!");
        }

        this.hashMethod = hashMethod;

        // calculate hash
        if (Util.HASH_ALGORITHM_SHA.equalsIgnoreCase(hashMethod)) {
            this.hashedPassword = digest(Util.HASH_ALGORITHM_SHA, passwordAsPlaintext);
        } else if (Util.HASH_ALGORITHM_MD5.equalsIgnoreCase(hashMethod)) {
            this.hashedPassword = digest(Util.HASH_ALGORITHM_MD5, passwordAsPlaintext);
        } else if (hashMethod == null) {
            this.hashedPassword = utf8encode(passwordAsPlaintext);
        }
        
    }


    /**
     * Verifies if this password is equal to the given test password.
     * 
     * @param testPasswordAsPlaintext the test password as plaintext
     * 
     * @return true, if equal
     */
    public boolean verify(String testPasswordAsPlaintext) {
        if (testPasswordAsPlaintext == null){
            return false;
        }

        boolean verified = false;
        if (hashMethod == null) {
            verified = testPasswordAsPlaintext.equals(utf8decode(hashedPassword));
        } else if (Util.HASH_ALGORITHM_SHA.equalsIgnoreCase(hashMethod)) {
            byte[] hash = digest(Util.HASH_ALGORITHM_SHA, testPasswordAsPlaintext);
            verified = equals(hash, hashedPassword);
        } else if (Util.HASH_ALGORITHM_MD5.equalsIgnoreCase(hashMethod)) {
            byte[] hash = digest(Util.HASH_ALGORITHM_MD5, testPasswordAsPlaintext);
            verified = equals(hash, hashedPassword);
        }

        return verified;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        if (hashMethod != null) {
            sb.append('{').append(hashMethod).append('}');
            sb.append(base64encode(hashedPassword));
        } else {
            sb.append(utf8decode(hashedPassword));
        }

        return sb.toString();
    }


    private static boolean equals(byte[] data1, byte[] data2) {
        if (data1 == data2) {
            return true;
        }
        if (data1 == null || data2 == null) {
            return false;
        }
        if (data1.length != data2.length) {
            return false;
        }
        for (int i = 0; i < data1.length; i++) {
            if (data1[i] != data2[i]) {
                return false;
            }
        }
        return true;
    }

    private static byte[] digest(String hashMethod, String password) {
        try {
			return Util.hash(utf8encode(password), hashMethod);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
    }

    private static String utf8decode(byte[] b) {
        try {
            return new String(b, IOUtil.CHARSET_NAME_UTF_8);
        } catch (UnsupportedEncodingException e) {
            return new String(b);
        }
    }

    private static byte[] utf8encode(String s) {
        try {
            return s.getBytes(IOUtil.CHARSET_NAME_UTF_8);
        } catch (UnsupportedEncodingException e) {
            return s.getBytes();
        }
    }

    private static byte[] base64decodeToByteArray(String s) {
        return Base64.decodeBase64(utf8encode(s));
    }

    private static String base64encode(byte[] b) {
        return utf8decode(Base64.encodeBase64(b));
    }

}
