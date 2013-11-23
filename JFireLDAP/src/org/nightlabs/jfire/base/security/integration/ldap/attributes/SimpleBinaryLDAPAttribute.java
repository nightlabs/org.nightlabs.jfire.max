package org.nightlabs.jfire.base.security.integration.ldap.attributes;


/**
 * Represents attribute values as byte[], which corresponds to binary LDAP attributes.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class SimpleBinaryLDAPAttribute extends AbstractSimpleLDAPAttribute<byte[]>{
	
	/**
	 * {@inheritDoc}
	 */
	public SimpleBinaryLDAPAttribute(String name, byte[] value){
		super(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public SimpleBinaryLDAPAttribute(String name){
		super(name);
	}

}
