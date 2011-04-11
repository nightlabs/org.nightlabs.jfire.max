package org.nightlabs.jfire.base.security.integration.ldap.attributes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>Represents a manageable set of {@link LDAPAttribute}s and provides API for attribute creation  when client could know nothing 
 * about particular {@link LDAPAttribute} implmentation. But does not restrict clients to create {@link LDAPAttribute}s on their own 
 * and add them to {@link LDAPAttributeSet} manually.</p>
 * 
 * <p>Specific {@link LDAPAttribute} implementations are created by {@link LDAPAttributeFactory}. By default {@link SimpleLDAPAttributeFactory}
 * is used. Do not forget to call {@link #setAttributeFactory(LDAPAttributeFactory)} before creating attributes if you want to provide
 * different {@link LDAPAttribute}s implementations.</p>
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPAttributeSet implements Iterable<LDAPAttribute<Object>>{
	
	/**
	 * {@link Map} holding attributes instances
	 */
	private Map<String, LDAPAttribute<Object>> attributes = new HashMap<String, LDAPAttribute<Object>>();
	
	/**
	 * Factory used for {@link LDAPAttribute}s creation
	 */
	private LDAPAttributeFactory attributeFactory = new SimpleLDAPAttributeFactory();

	/**
	 * Get all attributes in the set
	 * 
	 * @return unmodifiable collection with {@link LDAPAttribute}s
	 */
	public Collection<LDAPAttribute<Object>> getAttributes() {
		return Collections.unmodifiableCollection(attributes.values());
	}

	/**
	 * Get {@link LDAPAttribute} by name
	 * 
	 * @param attributeName can't be <code>null</code>, {@link IllegalArgumentException} will be thrown otherwise
	 * @return {@link LDAPAttribute} or <code>null</code> if there's no attribute with such attributeName
	 */
	public LDAPAttribute<Object> getAttribute(String attributeName) {
		if (attributeName == null){
			throw new IllegalArgumentException("Attribute's name can't be null!");
		}
		return attributes.get(attributeName);
	}

	/**
	 * Adds new {@link LDAPAttribute} to the set.
	 * 
	 * If attribute with the same name already exists in the set than all values of attribute parameter will be added to existing one and
	 * NO new attribute will be added. 
	 * 
	 * @param attribute can't be <code>null</code>, {@link IllegalArgumentException} will be thrown otherwise
	 */
	public void addAttribute(LDAPAttribute<Object> attribute) {
		if (attribute == null){
			throw new IllegalArgumentException("LDAPAttribute can't be null!");
		}
		
		if (attributes.containsKey(attribute.getName())){	// add new value to existing attribute
			attributes.get(attribute.getName()).addValues(attribute.getValues());
		}else{
			attributes.put(attribute.getName(), attribute);
		}
	}

	/**
	 * Adds several attributes to the set by calling {@link #addAttribute(LDAPAttribute)} on each of them. 
	 * So the same behaviour will be applied.
	 * 
	 * @param attributes
	 */
	public void addAttributes(Iterable<LDAPAttribute<Object>> attributes) {
		for (LDAPAttribute<Object> attribute : attributes){
			addAttribute(attribute);
		}
	}

	/**
	 * Removes {@link LDAPAttribute} from the set.
	 * 
	 * @param attribute
	 * @return removed {@link LDAPAttribute} or <code>null</code> if this set did not contain it
	 */
	public LDAPAttribute<Object> removeAttribute(LDAPAttribute<Object> attribute) {
		return attributes.remove(attribute.getName());
	}

	/**
	 * Removes several {@link LDAPAttribute}s from the set by calling {@link #removeAttribute(LDAPAttribute)} on each of them
	 * so the same behaviour will be applied.
	 * 
	 * @param attributes
	 */
	public void removeAttributes(Iterable<LDAPAttribute<Object>> attributes) {
		for (LDAPAttribute<Object> attribute : attributes){
			removeAttribute(attribute);
		}
	}

	/**
	 * Get amount of {@link LDAPAttribute}s in the set
	 * 
	 * @return integer with amount of attributes in the set
	 */
	public int size() {
		return attributes.size();
	}
	
	/**
	 * Set {@link LDAPAttributeFactory} which creates {@link LDAPAttribute} instances. Remember to set it before attribute's creation,
	 * default {@link SimpleLDAPAttributeFactory} will be used otherwise.
	 * 
	 * @param attributeFactory can't be <code>null</code>, {@link IllegalArgumentException} will be thrown otherwise
	 */
	public void setAttributeFactory(LDAPAttributeFactory attributeFactory) {
		if (attributeFactory == null){
			throw new IllegalArgumentException("LDAPAttributeFactory can't be null!");
		}
		this.attributeFactory = attributeFactory;
	}

	/**
	 * Create {@link LDAPAttribute} instance using {@link #attributeFactory} and adds it to the set.
	 * 
	 * @param attributeName
	 * @param attributeValues
	 * @return created {@link LDAPAttribute} instance
	 */
	public LDAPAttribute<Object> createAttribute(String attributeName, Object... attributeValues) {
		LDAPAttribute<Object> attribute = attributeFactory.createAttribute(attributeName, attributeValues);
		addAttribute(attribute);
		return attribute;
	}

	/**
	 * Create {@link LDAPAttribute} instance using {@link #attributeFactory} and adds it to the set.
	 * 
	 * @param attributeName
	 * @param attributeValues
	 * @return created {@link LDAPAttribute} instance
	 */
	public LDAPAttribute<Object> createAttribute(String attributeName, Collection<Object> attributeValues) {
		if (attributeValues == null){
			throw new IllegalArgumentException("Can't create attribute with null value, such attribute will not be created in LDAP anyway. Please specify an empty String if you want to have an attribute with no real value.");
		}
		
		if (attributeValues.isEmpty()){	// for creating attribute with blank value
			attributeValues.add("");
		}

		Iterator<Object> valuesIterator = attributeValues.iterator();
		LDAPAttribute<Object> attribute = attributeFactory.createAttribute(attributeName, valuesIterator.next());
		while (valuesIterator.hasNext()){
			attribute.addValue(valuesIterator.next());
		}
		addAttribute(attribute);
		return attribute;
	}

	/**
	 * Get attribute value by attribute name.
	 * 
	 * @param attributeName
	 * @return attribute value or <code>null</code> if there's no {@link LDAPAttribute} with such name in the set
	 */
	public Object getAttributeValue(String attributeName) {
		if (attributes.containsKey(attributeName)){
			return attributes.get(attributeName).getValue();
		}
		return null;
	}

	/**
	 * Implemet {@link Iterator} for iterating over {@link LDAPAttributeSet} as a simple {@link Iterable}.
	 */
	@Override
	public Iterator<LDAPAttribute<Object>> iterator() {
		return attributes.values().iterator();
	}

}
