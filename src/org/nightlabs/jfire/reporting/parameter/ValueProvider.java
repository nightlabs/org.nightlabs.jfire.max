package org.nightlabs.jfire.reporting.parameter;

import java.util.Map;

/**
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ValueProvider {

	private String organisationID;
	
	private String valueProviderID;
	
	private String outputType;
	
	private Map<String, ValueProviderInputParameter> inputParameters;

	protected ValueProvider() {
		
	}
	
	public ValueProvider(String organisationID, String valueProviderID) {
		this.organisationID = organisationID;
		this.valueProviderID = valueProviderID;
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
	public Map<String, ValueProviderInputParameter> getInputParameters() {
		return inputParameters;
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
	
	
	
}
