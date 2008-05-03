/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jfire.reporting.parameter.id.ValueProviderCategoryID;

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
 *  @jdo.query
 *		name="getValueProviderCategoriesForParent"
 *		query="SELECT
 *			WHERE this.parent == :parentCategory
 *			"
 *
 *  @jdo.query
 *		name="getValueProviderCategoryIDsForParent"
 *		query="SELECT JDOHelper.getObjectId(this)
 *			WHERE this.parent == :parentCategory
 *			"
 */
public class ValueProviderCategory implements Serializable, DetachCallback {

	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_VALUE_PROVIDERS = "ValueProviderCategory.valueProviders";
	public static final String FETCH_GROUP_PARENT = "ValueProviderCategory.parent";
	public static final String FETCH_GROUP_PARENT_ID = "ValueProviderCategory.parentID";
	public static final String FETCH_GROUP_CHILD_CATEGORIES = "ValueProviderCategory.childCategories";
	public static final String FETCH_GROUP_NAME = "ValueProviderCategory.name";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_VALUE_PROVIDER_CATEGORY = "ValueProviderCategory.this";
			 
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true" @jdo.column length="100"
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
	 * @jdo.field persistence-modifier="none"
	 * 		mapped-by="valueProviderCategory"
	 */
	private ValueProviderCategoryID parentID;
	
	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
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
	

	protected PersistenceManager getPersistenceManager()

	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("No PersistenceManager assigned!");
		
		return pm;
	}
	
	public ValueProviderCategoryID getParentID() {
		if (parentID == null)
			parentID = (ValueProviderCategoryID) JDOHelper.getObjectId(parent);
		return parentID;
	}
	
	public void jdoPostDetach(Object obj) {
		ValueProviderCategory attached = (ValueProviderCategory) obj;
		ValueProviderCategory detached = this;
		if (attached.getPersistenceManager().getFetchPlan().getGroups().contains(FETCH_GROUP_PARENT_ID))
			detached.parentID = attached.getParentID();
	}

	public void jdoPreDetach() {
	}
	
	
	@SuppressWarnings("unchecked")
	public static Collection<ValueProviderCategory> getValueProviderCategoriesForParent(PersistenceManager pm, ValueProviderCategory category) {
		Query q = pm.newNamedQuery(ValueProviderCategory.class, "getValueProviderCategoriesByParent");
		return (Collection<ValueProviderCategory>) q.execute(category);
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<ValueProviderCategoryID> getValueProviderCategoryIDsForParent(PersistenceManager pm, ValueProviderCategory category) {
		Query q = pm.newNamedQuery(ValueProviderCategory.class, "getValueProviderCategoryIDsForParent");
		return (Collection<ValueProviderCategoryID>) q.execute(category);
	}
}
