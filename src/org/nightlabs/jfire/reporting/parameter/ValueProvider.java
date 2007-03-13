package org.nightlabs.jfire.reporting.parameter;

import java.util.ArrayList;
import java.util.List;

import org.nightlabs.jfire.reporting.parameter.config.ValueAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ValueProviderConfig;

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
 * @jdo.fetch-group name="ValueProvider.category" fetch-groups="default" fields="category"
 * @jdo.fetch-group name="ValueProvider.inputParameters" fetch-groups="default" fields="inputParameters"
 * @jdo.fetch-group name="ValueProvider.this" fetch-groups="default" fields="name, description, category, inputParameters"
 *
 */
public class ValueProvider {

	public static final String FETCH_GROUP_NAME = "ValueProvider.name";
	public static final String FETCH_GROUP_DESCRIPTION = "ValueProvider.description";
	public static final String FETCH_GROUP_CATEGORY = "ValueProvider.category";
	public static final String FETCH_GROUP_INPUT_PARAMETERS = "ValueProvider.inputParameters";
	public static final String FETCH_GROUP_THIS_VALUE_PROVIDER = "ValueProvider.this";
			 
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
	 * @jdo.field primary-key="true"
	 */
	private String valueProviderID;	
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String outputType;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ValueProviderCategory category;
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.reporting.parameter.ValueProviderInputParameter"
	 *		mapped-by="valueProvider"
	 *		dependent-element="true"
	 */
	private List<ValueProviderInputParameter> inputParameters;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ValueProviderName name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ValueProviderDescription description;
	
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
}
