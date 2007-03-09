package org.nightlabs.jfire.reporting.parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ValueProvider {

	private String organisationID;
	
	private String valueProviderID;
	
	private String outputType;
	
	private List<ValueProviderInputParameter> inputParameters;

	protected ValueProvider() {
		
	}
	
	public ValueProvider(String organisationID, String valueProviderID, String outputType) {
		this.organisationID = organisationID;
		this.valueProviderID = valueProviderID;
		this.outputType = outputType;
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
	
	
	
}
