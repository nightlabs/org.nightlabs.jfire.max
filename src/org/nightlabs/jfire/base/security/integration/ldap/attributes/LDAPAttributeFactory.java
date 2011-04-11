package org.nightlabs.jfire.base.security.integration.ldap.attributes;


/**
 * <p>Interface for factories used for creating different instances of {@link LDAPAttribute}s.</p> 
 * 
 * <p>For now we have only one default implementation {@link SimpleLDAPAttributeFactory}. Other implemetations could, for example, create
 * attributes with internationalized values (usign LDAP language modifier).</p>
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public interface LDAPAttributeFactory {

	/**
	 * Creates {@link LDAPAttribute} with given name based on provided attribute values.
	 * 
	 * @param attributeName
	 * @param attributeValues
	 * @return created attribute or should throw an exception if it can not be created, no <code>null</code> should be returned
	 */
	public <ValueType> LDAPAttribute<ValueType> createAttribute(String attributeName, ValueType... attributeValues);
	
}
