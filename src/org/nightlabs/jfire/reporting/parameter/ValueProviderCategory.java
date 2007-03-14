/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.parameter.id.ValueProviderCategoryID"
 *		detachable = "true"
 *		table="JFireReporting_ValueProviderCategory"
 *
 * @jdo.create-objectid-class field-order="organisationID, valueProviderCategoryID"
 * 
 * @jdo.inheritance strategy = "new-table" 
 * @jdo.inheritance-discriminator strategy="class-name"
 * 
 * @jdo.fetch-group name="ValueProviderCategory.valueProviders" fetch-groups="default" fields="valueProviders"
 * @jdo.fetch-group name="ValueProviderCategory.parent" fetch-groups="default" fields="parent"
 * @jdo.fetch-group name="ValueProviderCategory.childCategories" fetch-groups="default" fields="childCategories"
 * @jdo.fetch-group name="ValueProviderCategory.name" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="ValueProviderCategory.this" fetch-groups="default" fields="name, parent, childCategories"
 *
 */
public class ValueProviderCategory implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_VALUE_PROVIDERS = "ValueProviderCategory.valueProviders";
	public static final String FETCH_GROUP_PARENT = "ValueProviderCategory.parent";
	public static final String FETCH_GROUP_CHILD_CATEGORIES = "ValueProviderCategory.childCategories";
	public static final String FETCH_GROUP_NAME = "ValueProviderCategory.name";
	public static final String FETCH_GROUP_THIS_VALUE_PROVIDER_CATEGORY = "ValueProviderCategory.this";
			 
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private String valueProviderCategoryID;
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.reporting.parameter.ValueProvider"
	 *		mapped-by="category"
	 *		dependent-element="true"
	 */
	private Set<ValueProvider> valueProviders;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ValueProviderCategory parent;
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.reporting.parameter.ValueProviderCategory"
	 *		mapped-by="parent"
	 *		dependent-element="true"
	 */
	private Set<ValueProviderCategory> childCategories;
	
	/**
	 * @jdo.field 
	 * 		persistence-modifier="persistent"
	 * 		mapped-by="valueProviderCategory"
	 */
	private ValueProviderCategoryName name;
	
	/**
	 * @deprecated Only for JDO
	 */
	protected ValueProviderCategory() {
	}

	public ValueProviderCategory(
			ValueProviderCategory parent,
			String organisationID, String valueProviderCategoryID,
			boolean addToParent
		) 
	{
		this.organisationID = organisationID;
		this.valueProviderCategoryID = valueProviderCategoryID;
		this.valueProviders = new HashSet<ValueProvider>();		
		this.name = new ValueProviderCategoryName(this);
		this.parent = parent;
		if (parent != null && addToParent)
			parent.getChildCategories().add(this);
	}
	
	/**
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return the valueProviderCategoryID
	 */
	public String getValueProviderCategoryID() {
		return valueProviderCategoryID;
	}

	/**
	 * @return the name
	 */
	public ValueProviderCategoryName getName() {
		return name;
	}

	/**
	 * @return the valueProviders
	 */
	public Set<ValueProvider> getValueProviders() {
		return valueProviders;
	}
	
	/**
	 * Sets the valueProvider member of the given provider to this
	 * and adds the provider to the list of providers of this category.
	 * 
	 * @param valueProvider
	 */
	public void addValueProvider(ValueProvider valueProvider) {
		valueProvider.setCategory(this);
		valueProviders.add(valueProvider);
	}

	/**
	 * @return the childCategories
	 */
	public Set<ValueProviderCategory> getChildCategories() {
		return childCategories;
	}

	/**
	 * @param childCategories the childCategories to set
	 */
	public void setChildCategories(Set<ValueProviderCategory> childCategories) {
		this.childCategories = childCategories;
	}

	/**
	 * @return the parent
	 */
	public ValueProviderCategory getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(ValueProviderCategory parent) {
		this.parent = parent;
	}
}
