package org.nightlabs.jfire.base.security.integration.ldap.attributes;

/**
 * Interface representing LDAP attributes which are held in {@link LDAPAttributeSet}. LDAP attributes could be either single or multi valued
 * so there's separate API for this cases in order not mix them up.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public interface LDAPAttribute<ValueType> {

	/**
	 * Set value to the attribute. Should be used when attribute is single valued.
	 *  
	 * @param value
	 */
	public void setValue(ValueType value);

	/**
	 * Get attribute's value. Should be used when attribute is single valued.
	 * 
	 * @return attribute value
	 */
	public ValueType getValue();
	
	/**
	 * Get all attribute's values. Should be used when attribute is multi valued.
	 * 
	 * @return all attribute's values
	 */
	public Iterable<ValueType> getValues();

	/**
	 * Adds value to attribute's values. Should be used when attribute is multi valued.
	 * 
	 * @param value
	 */
	public void addValue(ValueType value);
	
	/**
	 * Adds several values to attribute's current values. Should be used when attribute is multi valued.
	 * 
	 * @param values
	 */
	public void addValues(Iterable<ValueType> values);

	/**
	 * Get name of this attribute. Could be either canonical or alias (e.g. "commonName" or "cn") depending on what was specified when attribute was created.
	 * For example, JNDI tutorial recommeds using canonical names only to avoid possible LDAP directory providers incompatibility.
	 * 
	 * @return name of this attribute
	 */
	public String getName();
	
	/**
	 * Removes single attribute's value. Should be used when attribute is single valued.
	 */
	public void removeValue();
	
	/**
	 * Removes specified value from attribute's values. Should be used when attribute is multi valued.
	 * 
	 * @param value
	 */
	public void removeValue(ValueType value);
	
	/**
	 * Removes specified values from attribute's current values. Should be used when attribute is multi valued.
	 * 
	 * @param values
	 */
	public void removeValues(Iterable<ValueType> values);

	/**
	 * Check if atribute is single valued.
	 * 
	 * @return <code>true</code> if attribute has only one value and it's not a <code>null</code>. 
	 */
	public boolean hasSingleValue();
	
	/**
	 * Get amount of attribute's values. Should always return 1 for single valued attributes.
	 * 
	 * @return integer with amount of attribute's values
	 */
	public int valuesCount();

}
