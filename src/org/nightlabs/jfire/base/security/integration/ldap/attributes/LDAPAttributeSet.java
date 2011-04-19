package org.nightlabs.jfire.base.security.integration.ldap.attributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(LDAPAttributeSet.class);
	
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
	 * TODO: some attributes could have "" as a value, some could not. Need to decide if we restrict attribute values and which rules for 
	 * restrictions should be used. Denis.
	 * 
	 * @param attribute can't be <code>null</code>, {@link IllegalArgumentException} will be thrown otherwise
	 * @return <code>true</code> if either new attribute was added or it's values were added to existing one, <code>false</code> if nothing at all was added
	 */
	public boolean addAttribute(LDAPAttribute<Object> attribute) {
		if (attribute == null){
			throw new IllegalArgumentException("LDAPAttribute can't be null!");
		}
		
		if (attribute.hasSingleValue()){
			if (attribute.getValue() == null || "".equals(attribute.getValue())){
				logger.warn(String.format("Attribute %s has no valid value, it will not be added to the set!", attribute.getName()));
				return false;
			}
		}else{
			Collection<Object> valuesToDelete = new ArrayList<Object>();
			for (Object value : attribute.getValues()){
				if (value == null || "".equals(value)){
					valuesToDelete.add(value);
				}
			}
			attribute.removeValues(valuesToDelete);
			if (attribute.valuesCount() == 0){
				logger.warn(String.format("Attribute %s has no valid values, it will not be added to the set!", attribute.getName()));
				return false;
			}
		}
		
		if (attributes.containsKey(attribute.getName())){	// add new value to existing attribute
			attributes.get(attribute.getName()).addValues(attribute.getValues());
		}else{
			attributes.put(attribute.getName(), attribute);
		}
		return true;
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
	 * @param attributeValue - if value is <code>null</code> or empty string then resulting attribute WILL NOT be added to the set
	 * @return created {@link LDAPAttribute} instance
	 */
	@SuppressWarnings("unchecked")
	public LDAPAttribute<Object> createAttribute(String attributeName, Object attributeValue) {
		LDAPAttribute<Object> attribute = null;
		if (attributeValue instanceof Iterable){
			Iterator<Object> valuesIterator = ((Iterable<Object>) attributeValue).iterator();

			Object value = null;
			if (valuesIterator.hasNext()){
				value = valuesIterator.next();
			}else{
				value = "";	// for creating attribute with no values
			}
			attribute = attributeFactory.createAttribute(attributeName, value);
			while (valuesIterator.hasNext()){
				attribute.addValue(valuesIterator.next());
			}
		}else{
			attribute = attributeFactory.createAttribute(attributeName, attributeValue);
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
	
	@Override
	public String toString() {
		StringBuffer toStringValue = new StringBuffer();
		for (LDAPAttribute<Object> attribute : attributes.values()){
			toStringValue.append(attribute.toString());
			toStringValue.append("\n");
		}
		return toStringValue.toString();
	}

}
