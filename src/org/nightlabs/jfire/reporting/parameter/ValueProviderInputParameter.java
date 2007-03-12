package org.nightlabs.jfire.reporting.parameter;

/**
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.parameter.id.ValueProviderInputParameterID"
 *		detachable = "true"
 *		table="JFireReporting_ValueProviderInputParameter"
 *
 * @jdo.create-objectid-class field-order="organisationID, valueProviderID, parameterID"
 * 
 * @jdo.inheritance strategy = "new-table" 
 * @jdo.inheritance-discriminator strategy="class-name"
 * 
 * @jdo.fetch-group name="ValueProviderInputProvider.this" fetch-groups="default" fields="name, description"
 */
public class ValueProviderInputParameter {

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private String valueProviderID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private String parameterID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String parameterType;	

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ValueProvider valueProvider;
	
	/**
	 * @deprecated Only for JDO
	 */
	protected ValueProviderInputParameter() {}
	
	public ValueProviderInputParameter(String parameterID, String parameterType) {
		this.parameterID = parameterID;
		this.parameterType = parameterType;
	}

	public ValueProviderInputParameter(String parameterID, String parameterType, ValueProvider provider) {
		this.parameterID = parameterID;
		this.parameterType = parameterType;
		if (provider != null)
			setValueProvider(provider);
	}
	
	/**
	 * @return the parameterID
	 */
	public String getParameterID() {
		return parameterID;
	}

	/**
	 * @param parameterID the parameterID to set
	 */
	public void setParameterID(String parameterID) {
		this.parameterID = parameterID;
	}

	/**
	 * @return the parameterType
	 */
	public String getParameterType() {
		return parameterType;
	}

	/**
	 * @param parameterType the parameterType to set
	 */
	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	/**
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return the valueProvider
	 */
	public ValueProvider getValueProvider() {
		return valueProvider;
	}

	/**
	 * @return the valueProviderID
	 */
	public String getValueProviderID() {
		return valueProviderID;
	}

	/**
	 * Used when adding a parameter to a Value Provider.
	 * 
	 * @param provider
	 */
	protected void setValueProvider(ValueProvider provider) {
		this.organisationID = provider.getOrganisationID();
		this.valueProviderID = provider.getValueProviderID();
		this.valueProvider = provider;
	}
}
