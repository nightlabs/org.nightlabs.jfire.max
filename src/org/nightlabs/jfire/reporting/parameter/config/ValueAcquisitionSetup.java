/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

import java.util.List;
import java.util.Set;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ValueAcquisitionSetup {
	private String organisationID;
	
	private long valueAcquisitionSetupID;
	
	private List<AcquisitionParameterConfig> parameterConfigs;
	
	private Set<ValueProviderConfig> valueProviderConfigs;
	
	private Set<ValueConsumerBinding> valueConsumerBindings;

	protected ValueAcquisitionSetup() {
		
	}
	
	public ValueAcquisitionSetup(String organisationID, long valueAcquisitionID) {
		this.organisationID = organisationID;
		this.valueAcquisitionSetupID = valueAcquisitionID;		
	}
	
	/**
	 * @return the valueAcquisitionSetupID
	 */
	public long getValueAcquisitionSetupID() {
		return valueAcquisitionSetupID;
	}

	/**
	 * @return the parameterConfigs
	 */
	public List<AcquisitionParameterConfig> getParameterConfigs() {
		return parameterConfigs;
	}

	/**
	 * @param parameterConfigs the parameterConfigs to set
	 */
	public void setParameterConfigs(
			List<AcquisitionParameterConfig> parameterConfigs) {
		this.parameterConfigs = parameterConfigs;
	}

	/**
	 * @return the valueProviderConfigs
	 */
	public Set<ValueProviderConfig> getValueProviderConfigs() {
		return valueProviderConfigs;
	}

	/**
	 * @param valueProviderConfigs the valueProviderConfigs to set
	 */
	public void setValueProviderConfigs(
			Set<ValueProviderConfig> valueProviderConfigs) {
		this.valueProviderConfigs = valueProviderConfigs;
	}

	/**
	 * @return the valueConsumerBindings
	 */
	public Set<ValueConsumerBinding> getValueConsumerBindings() {
		return valueConsumerBindings;
	}

	/**
	 * @param valueConsumerBindings the valueConsumerBindings to set
	 */
	public void setValueConsumerBindings(
			Set<ValueConsumerBinding> valueConsumerBindings) {
		this.valueConsumerBindings = valueConsumerBindings;
	}

	/**
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	
}
