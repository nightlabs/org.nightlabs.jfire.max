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
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.parameter.config.id.ValueAcquisitionSetupID"
 *		detachable = "true"
 *		table="JFireReporting_AcquisistionParameterConfig"
 *
 * @jdo.create-objectid-class field-order="organisationID, valueAcquisitionSetupID"
 * 
 * @jdo.fetch-group name="ValueAcquisitionSetup.parameterConfigs" fetch-groups="default" fields="parameterConfigs"
 * @jdo.fetch-group name="ValueAcquisitionSetup.valueProviderConfigs" fetch-groups="default" fields="valueProviderConfigs"
 * @jdo.fetch-group name="ValueAcquisitionSetup.valueConsumerBindings" fetch-groups="default" fields="valueConsumerBindings"
 * @jdo.fetch-group name="ValueAcquisitionSetup.this" fetch-groups="default" fields="parameterConfigs, valueProviderConfigs, valueConsumerBindings"
 */
public class ValueAcquisitionSetup {
	
	public static final String FETCH_GROUP_PARAMETER_CONFIGS = "ValueAcquisitionSetup.parameterConfigs";
	public static final String FETCH_GROUP_VALUE_PROVIDER_CONFIGS = "ValueAcquisitionSetup.valueProviderConfigs";
	public static final String FETCH_GROUP_VALUE_CONSUMER_BINDINGS = "ValueAcquisitionSetup.valueConsumerBindings";
	public static final String FETCH_GROUP_THIS_VALUE_ACQUISITION_SETUP = "ValueAcquisitionSetup.this";
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long valueAcquisitionSetupID;
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.reporting.parameter.config.AcquisitionParameterConfig"
	 *		mapped-by="setup"
	 *		dependent-element="true"
	 */
	private List<AcquisitionParameterConfig> parameterConfigs;
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.reporting.parameter.config.ValueProviderConfig"
	 *		mapped-by="setup"
	 *		dependent-element="true"
	 */
	private Set<ValueProviderConfig> valueProviderConfigs;
	
	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.reporting.parameter.config.ValueConsumerBinding"
	 *		mapped-by="setup"
	 *		dependent-element="true"
	 */
	private Set<ValueConsumerBinding> valueConsumerBindings;

	/**
	 * Used internally to provide bindings.
	 * 
	 * @jdo.field persistence-modifier="none"
	 **/
	private transient Map<String, ValueConsumerBinding> consumer2Binding = null;
	
	/**
	 * @deprecated Only for JDO
	 */
	protected ValueAcquisitionSetup() {	}

	/**
	 * Create a new ValueAcquisitionSetup
	 * 
	 * @param organisationID The organisationID.
	 * @param valueAcquisitionSetupID The setup ID.
	 */
	public ValueAcquisitionSetup(String organisationID, long valueAcquisitionSetupID) {
		this.organisationID = organisationID;
		this.valueAcquisitionSetupID = valueAcquisitionSetupID;		
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
