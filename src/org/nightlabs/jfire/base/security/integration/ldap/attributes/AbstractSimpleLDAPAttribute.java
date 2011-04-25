package org.nightlabs.jfire.base.security.integration.ldap.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple implemetation of {@link LDAPAttribute} which holds attribute's values as a {@link List}. Subclasses of this class specify 
 * type for elements in this {@link List}. For single valued attribute ({@link #hasSingleValue()} returns true) the {@link List} has only one element. 
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public abstract class AbstractSimpleLDAPAttribute<ValueType> implements LDAPAttribute<ValueType>{
	
	/**
	 * Attribute's name
	 */
	private String name;
	
	/**
	 * Attribute's values
	 */
	private List<ValueType> values = new ArrayList<ValueType>();
	
	/**
	 * Creates new {@link AbstractSimpleLDAPAttribute} with specified name and no values.
	 * 
	 * @param name
	 */
	protected AbstractSimpleLDAPAttribute(String name){
		this.name = name;
	}

	/**
	 * Creates new {@link AbstractSimpleLDAPAttribute} with specified name and value.
	 * 
	 * @param name
	 * @param value
	 */
	protected AbstractSimpleLDAPAttribute(String name, ValueType value){
		this.name = name;
		this.values.add(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ValueType getValue() {
		if (hasSingleValue()){
			return values.get(0);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<ValueType> getValues() {
		return Collections.unmodifiableList(values);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addValue(ValueType value) {
		values.add(value);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setValue(ValueType value) {
		this.values.clear();
		this.values.add(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addValues(Iterable<ValueType> values) {
		for(ValueType value : values){
			addValue(value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeValue() {
		this.values.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeValue(ValueType value) {
		values.remove(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeValues(Iterable<ValueType> values) {
		for (ValueType value : values){
			removeValue(value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasSingleValue() {
		return 1 == values.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int valuesCount() {
		return values.size();
	}

	@Override
	public String toString() {
		StringBuffer toStringValue = new StringBuffer();
		toStringValue.append(name);
		toStringValue.append('=');
		if (hasSingleValue()){
			toStringValue.append(getValue());
		}else{
			for (ValueType value : getValues()){
				toStringValue.append(value);
				toStringValue.append(' ');
			}
		}
		return toStringValue.toString();
	}
}
