package org.nightlabs.jfire.base.security.integration.ldap.attributes;

/**
 * <p>Implemetation of {@link LDAPAttributeFactory} which is used by default in {@link LDAPAttributeSet} for creating {@link LDAPAttribute}s.</p>
 * 
 * <p>This factory creates attributes with values of type {@link String} or byte[] for binary ones. Therefore it expects 
 * incoming attributeValues parameters to be one of these types, otherwise {@link IllegalArgumentException} is thrown.</p>
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class SimpleLDAPAttributeFactory implements LDAPAttributeFactory{

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public LDAPAttribute<?> createAttribute(String attributeName, Object... attributeValues) {
		
		if (attributeValues == null){
			throw new IllegalArgumentException("Can't create attribute with null value, such attribute will not be created in LDAP anyway. Please specify an empty String if you want to have an attribute with no real value.");
		}
		
		if (attributeValues.length == 1){	// create single valued attribute
			Object value = attributeValues[0];
			if (value instanceof String){
				return new SimpleStringLDAPAttribute(attributeName, (String) value);
			}else if (value instanceof byte[]){
				return new SimpleBinaryLDAPAttribute(attributeName, (byte[]) value);
			}else{
				throw new IllegalArgumentException("Unknown attribute value type!");
			}
		}else{	// create multivalued attribute
			SimpleStringLDAPAttribute attribute = new SimpleStringLDAPAttribute(attributeName);
			for (Object value : attributeValues){
				if (value instanceof String){
					attribute.addValue((String) value);
				}else if (value instanceof byte[]){
					throw new IllegalArgumentException("Attribute with binary value is always single valued!");
				}else if (value != null){
					attribute.addValue(value.toString());
				}
			}
			return attribute;
		}
	}

}
