package org.nightlabs.jfire.reporting.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jfire.reporting.parameter.config.ValueAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ValueProviderConfig;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderCategoryID;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;

import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

/**
 * ValueProviders are used to declare the process of acquiring
 * report parameters from the user. They are registered on the
 * server side referenced in {@link ValueProviderConfig}s and
 * {@link ValueAcquisitionSetup}s.
 * <p>
 * This object is only for declaring the value provider.
 * Objects for concrete user interaction should be registered
 * to the appropriate {@link #valueProviderID} on the
 * client side.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.parameter.id.ValueProviderID"
 *		detachable = "true"
 *		table="JFireReporting_ValueProvider"
 *
 * @jdo.create-objectid-class field-order="organisationID, valueProviderCategoryID, valueProviderID"
 * 
 * @jdo.inheritance strategy = "new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 * 
 * @jdo.fetch-group name="ValueProvider.name" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="ValueProvider.description" fetch-groups="default" fields="description"
 * @jdo.fetch-group name="ValueProvider.defaultMessage" fetch-groups="default" fields="defaultMessage"
 * @jdo.fetch-group name="ValueProvider.category" fetch-groups="default" fields="category"
 * @jdo.fetch-group name="ValueProvider.inputParameters" fetch-groups="default" fields="inputParameters"
 * @jdo.fetch-group name="ValueProvider.this" fetch-groups="default" fields="name, description, category, inputParameters"
 *
 *  @jdo.query
 *		name="getValueProviderIDsForParent"
 *		query="SELECT JDOHelper.getObjectId(this)
 *			WHERE this.category == :parentCategory
 *			"
 *
 */@PersistenceCapable(
	objectIdClass=ValueProviderID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireReporting_ValueProvider")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=ValueProvider.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ValueProvider.FETCH_GROUP_DESCRIPTION,
		members=@Persistent(name="description")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ValueProvider.FETCH_GROUP_DEFAULT_MESSAGE,
		members=@Persistent(name="defaultMessage")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ValueProvider.FETCH_GROUP_CATEGORY,
		members=@Persistent(name="category")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ValueProvider.FETCH_GROUP_INPUT_PARAMETERS,
		members=@Persistent(name="inputParameters")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ValueProvider.FETCH_GROUP_THIS_VALUE_PROVIDER,
		members={@Persistent(name="name"), @Persistent(name="description"), @Persistent(name="category"), @Persistent(name="inputParameters")})
})
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Queries(
	@javax.jdo.annotations.Query(
		name="getValueProviderIDsForParent",
		value="SELECT JDOHelper.getObjectId(this) WHERE this.category == :parentCategory ")
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class ValueProvider implements Serializable, DetachCallback {

	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_NAME = "ValueProvider.name";
	public static final String FETCH_GROUP_DESCRIPTION = "ValueProvider.description";
	public static final String FETCH_GROUP_DEFAULT_MESSAGE = "ValueProvider.defaultMessage";
	public static final String FETCH_GROUP_CATEGORY = "ValueProvider.category";
	public static final String FETCH_GROUP_CATEGORY_ID = "ValueProvider.categoryID";
	public static final String FETCH_GROUP_INPUT_PARAMETERS = "ValueProvider.inputParameters";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_VALUE_PROVIDER = "ValueProvider.this";
			 
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String valueProviderCategoryID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String valueProviderID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private String outputType;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private ValueProviderCategory category;
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.reporting.parameter.ValueProviderInputParameter"
	 *		mapped-by="valueProvider"
	 *		dependent-element="true"
	 */	@Persistent(
		dependentElement="true",
		mappedBy="valueProvider",
		persistenceModifier=PersistenceModifier.PERSISTENT)

	private List<ValueProviderInputParameter> inputParameters;
	
	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		mapped-by="valueProvider"
	 */	@Persistent(
		mappedBy="valueProvider",
		persistenceModifier=PersistenceModifier.PERSISTENT)

	private ValueProviderName name;

	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		mapped-by="valueProvider"
	 */	@Persistent(
		mappedBy="valueProvider",
		persistenceModifier=PersistenceModifier.PERSISTENT)

	private ValueProviderDescription description;
	
	/**
	 * @jdo.field
	 * 		persistence-modifier="persistent"
	 * 		mapped-by="valueProvider"
	 */	@Persistent(
		mappedBy="valueProvider",
		persistenceModifier=PersistenceModifier.PERSISTENT)

	private ValueProviderDefaultMessage defaultMessage;
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */	@Persistent(persistenceModifier=PersistenceModifier.NONE)

	private ValueProviderCategoryID categoryID;
	
	protected ValueProvider() {
	}
	
	public ValueProvider(ValueProviderCategory category, String valueProviderID, String outputType) {
		this.organisationID = category.getOrganisationID();
		this.valueProviderCategoryID = category.getValueProviderCategoryID();
		this.category = category;
		this.valueProviderID = valueProviderID;
		this.outputType = outputType;
		this.name = new ValueProviderName(this);
		this.description = new ValueProviderDescription(this);
		this.defaultMessage = new ValueProviderDefaultMessage(this);
	}
	
	/**
	 * @return the outputType
	 */
	public String getOutputType() {
		return outputType;
	}

	/**
	 * @param outputType the outputType to set
	 */
	public void setOutputType(String outputType) {
		this.outputType = outputType;
	}

	/**
	 * @return the inputParameters
	 */
	public List<ValueProviderInputParameter> getInputParameters() {
		return inputParameters;
	}
	
	public void addInputParameter(ValueProviderInputParameter inputParameter) {
		inputParameter.setValueProvider(this);
		if (inputParameters == null)
			inputParameters = new ArrayList<ValueProviderInputParameter>();
		inputParameters.add(inputParameter);
	}
	
	/**
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return the valueProviderID
	 */
	public String getValueProviderID() {
		return valueProviderID;
	}

	/**
	 * @return the name
	 */
	public ValueProviderName getName() {
		return name;
	}

	/**
	 * 
	 * @return the description
	 */
	public ValueProviderDescription getDescription() {
		return description;
	}

	/**
	 * 
	 * @return the default message shown when using this value provider
	 */
	public ValueProviderDefaultMessage getDefaultMessage() {
		return defaultMessage;
	}
	
	/**
	 * @return The valueProviderCategoryID
	 */
	public String getValueProviderCategoryID() {
		return valueProviderCategoryID;
	}

	/**
	 * @return the category
	 */
	public ValueProviderCategory getCategory() {
		return category;
	}
	
	/**
	 * @return the category
	 */
	void setCategory(ValueProviderCategory category) {
		this.category = category;
	}
	
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("No PersistenceManager assigned!");
		
		return pm;
	}
	
	public ValueProviderCategoryID getCategoryID() {
		if (categoryID == null)
			categoryID = (ValueProviderCategoryID) JDOHelper.getObjectId(category);
		return categoryID;
	}
	
	public void jdoPostDetach(Object obj) {
		ValueProvider attached = (ValueProvider) obj;
		ValueProvider detached = this;
		if (attached.getPersistenceManager().getFetchPlan().getGroups().contains(FETCH_GROUP_CATEGORY_ID))
			detached.categoryID = attached.getCategoryID();
	}
	
	public void jdoPreDetach() {
	}
	
	
	@SuppressWarnings("unchecked")
	public static Collection<ValueProviderID> getValueProviderIDsForParent(PersistenceManager pm, ValueProviderCategory category) {
		Query q = pm.newNamedQuery(ValueProvider.class, "getValueProviderIDsForParent");
		return (Collection<ValueProviderID>) q.execute(category);
	}

}
