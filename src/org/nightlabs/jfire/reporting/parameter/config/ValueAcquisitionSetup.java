/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private transient Map<String, ValueConsumerBinding> consumer2Binding = null;
	
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
	
	public ValueConsumerBinding getValueConsumerBinding(ValueConsumer consumer, String parameterID) {
		String key = consumer.getConsumerKey() + "/" + parameterID;
		if (consumer2Binding == null) {
			consumer2Binding = new HashMap<String, ValueConsumerBinding>();			
			for (ValueConsumerBinding binding : valueConsumerBindings) {
				String bindingKey = binding.getConsumer().getConsumerKey() + "/" + binding.getParameterID();
				consumer2Binding.put(bindingKey, binding);
			}
		}
		return consumer2Binding.get(key);
	}
	
}
