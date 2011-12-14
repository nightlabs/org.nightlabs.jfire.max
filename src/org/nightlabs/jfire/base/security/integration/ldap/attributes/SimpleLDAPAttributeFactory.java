package org.nightlabs.jfire.base.security.integration.ldap.attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Implemetation of {@link LDAPAttributeFactory} which is used by default in {@link LDAPAttributeSet} for creating {@link LDAPAttribute}s.</p>
 * 
 * <p>This factory creates attributes with values of type {@link String} or byte[] for binary ones. Therefore it expects 
 * incoming attributeValues parameters to be one of these types, otherwise {@link IllegalArgumentException} is thrown.</p>
 * 
 * <p>If <code>null</code> value or empty array is passed then a {@link SimpleStringLDAPAttribute} instance will be created with an empty {@link String} as a value
 * and a warning will be written to log.</p>
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class SimpleLDAPAttributeFactory implements LDAPAttributeFactory{
	
	private static final Logger logger = LoggerFactory.getLogger(SimpleLDAPAttributeFactory.class);

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public LDAPAttribute<?> createAttribute(String attributeName, Object... attributeValues) {
		
		if (attributeValues == null){
			logger.warn(
					String.format(
							"NULL was specified as value for the attribute \"%s\" so SimpleStringLDAPAttribute will be created with empty String as a value.", attributeName));
			attributeValues = new String[]{""};
		}else if (attributeValues.length == 0){
			logger.warn(
					String.format(
							"Empty array was specified as value for the attribute \"%s\" so SimpleStringLDAPAttribute will be created with empty String as a value.", attributeName));
			attributeValues = new String[]{""};
		}
		
		if (attributeValues.length == 1){	// create single valued attribute
			Object value = attributeValues[0];
			if (value == null){
				logger.warn("NULL was specified as value for the attribute so SimpleStringLDAPAttribute will be created with empty String as a value.");
				value = "";
			}
			if (value instanceof String){
				return new SimpleStringLDAPAttribute(attributeName, (String) value);
			}else if (value instanceof byte[]){
				return new SimpleBinaryLDAPAttribute(attributeName, (byte[]) value);
			}else{
				throw new IllegalArgumentException("Unknown attribute value type!");
			}
		}else if (attributeValues.length > 1){	// create multivalued attribute
			
			if (attributeValues[0] == null){
				logger.warn(
						String.format(
								"NULL was specified as value for the attribute \"%s\" so SimpleStringLDAPAttribute will be created with empty String as a value.", attributeName));
				attributeValues[0] = "";
			}
			
			if (attributeValues[0] instanceof String){
				SimpleStringLDAPAttribute attribute = new SimpleStringLDAPAttribute(attributeName);
				for (Object value : attributeValues){
					attribute.addValue((String) value);
				}
				
				return attribute;
				
			}else if (attributeValues[0] instanceof byte[]){
				SimpleBinaryLDAPAttribute attribute = new SimpleBinaryLDAPAttribute(attributeName);
				for (Object value : attributeValues){
					attribute.addValue((byte[]) value);
				}
				
				return attribute;
				
			}else{
				throw new IllegalArgumentException("Unknown attribute value type for attribute " + attributeName);
			}
			
		}else{
			throw new IllegalArgumentException(
					String.format("Can't create attribute \"%s\" when no values are given. Please specify an empty String if you want to have an attribute with no real value.", attributeName));
		}
	}

}
