package org.nightlabs.jfire.base.security.integration.ldap.attributes;


/**
 * Represents attribute values as {@link String}.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class SimpleStringLDAPAttribute extends AbstractSimpleLDAPAttribute<String>{
	
	/**
	 * {@inheritDoc}
	 */
	public SimpleStringLDAPAttribute(String name){
		super(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public SimpleStringLDAPAttribute(String name, String value){
		super(name, value);
	}

}
