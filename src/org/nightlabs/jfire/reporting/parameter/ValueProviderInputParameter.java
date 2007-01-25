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
	
	private int orderNumber;
	
	protected ValueProviderInputParameter() {
		
	}
	
	public ValueProviderInputParameter(ValueProvider valueProvider, String parameterID, String parameterType) {
		this.organisationID = valueProvider.getOrganisationID();
		this.valueProviderID = valueProvider.getValueProviderID();
		this.valueProvider = valueProvider;
		this.parameterID = parameterID;
		this.parameterType = parameterType;
		this.orderNumber = Integer.MAX_VALUE / 2;
	}

	/**
	 * @return the orderNumber
	 */
	public int getOrderNumber() {
		return orderNumber;
	}

	/**
	 * @param orderNumber the orderNumber to set
	 */
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
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
	
	
	
}
