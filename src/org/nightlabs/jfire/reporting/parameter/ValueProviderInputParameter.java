package org.nightlabs.jfire.reporting.parameter;

/**
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ValueProviderInputParameter {

	private String organisationID;
	
	private String valueProviderID;
	
	private String parameterID;
	
	private String parameterType;	

	private ValueProvider valueProvider;
	
	protected ValueProviderInputParameter() {
		
	}
	
	public ValueProviderInputParameter(String parameterID, String parameterType) {
		this.parameterID = parameterID;
		this.parameterType = parameterType;
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
