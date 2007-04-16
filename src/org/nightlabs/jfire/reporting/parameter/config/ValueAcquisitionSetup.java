/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.nightlabs.jfire.reporting.parameter.ValueProvider;
import org.nightlabs.jfire.reporting.parameter.ValueProviderInputParameter;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.parameter.config.id.ValueAcquisitionSetupID"
 *		detachable = "true"
 *		table="JFireReporting_ValueAcquisitionSetup"
 *
 * @jdo.create-objectid-class 
 * 		field-order="organisationID, valueAcquisitionSetupID"
 * 		include-imports="id/ValueAcquisitionSetup.imports.inc"
 *		include-body="id/ValueAcquisitionSetup.body.inc"	 
 * 
 * @jdo.fetch-group name="ValueAcquisitionSetup.parameterConfigs" fetch-groups="default" fields="parameterConfigs"
 * @jdo.fetch-group name="ValueAcquisitionSetup.valueProviderConfigs" fetch-groups="default" fields="valueProviderConfigs"
 * @jdo.fetch-group name="ValueAcquisitionSetup.valueConsumerBindings" fetch-groups="default" fields="valueConsumerBindings"
 * @jdo.fetch-group name="ValueAcquisitionSetup.this" fetch-groups="default" fields="parameterConfigs, valueProviderConfigs, valueConsumerBindings"
 */
public class ValueAcquisitionSetup implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportParameterAcquisitionSetup parameterAcquisitionSetup;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportParameterAcquisitionUseCase useCase;

	/**
	 * Used internally to provide bindings.
	 * 
	 * @jdo.field persistence-modifier="none"
	 **/
	private transient Map<String, Map<String, ValueConsumerBinding>> consumer2Binding = null;
	
	/**
	 * Used internally to provide bindings.
	 * 
	 * @jdo.field persistence-modifier="none"
	 **/
	private transient Map<String, ValueConsumerBinding> provider2Binding = null;
	
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
	public ValueAcquisitionSetup(String organisationID, long valueAcquisitionSetupID, ReportParameterAcquisitionSetup parameterAcquisitionSetup, ReportParameterAcquisitionUseCase useCase) {
		this.organisationID = organisationID;
		this.valueAcquisitionSetupID = valueAcquisitionSetupID;
		this.parameterAcquisitionSetup = parameterAcquisitionSetup;
		this.useCase = useCase;
		
//		consumer2Binding = new HashMap<String, Map<String,ValueConsumerBinding>>();
		parameterConfigs = new ArrayList<AcquisitionParameterConfig>();
//		provider2Binding = new HashMap<String, ValueConsumerBinding>();
		valueConsumerBindings = new HashSet<ValueConsumerBinding>();
		valueProviderConfigs = new HashSet<ValueProviderConfig>();
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
	
	/**
	 * @return the useCase
	 */
	public ReportParameterAcquisitionUseCase getUseCase() {
		return useCase;
	}

	/**
	 * @return the parameterAcquisitionSetup
	 */
	public ReportParameterAcquisitionSetup getParameterAcquisitionSetup() {
		return parameterAcquisitionSetup;
	}

	public SortedMap<Integer, SortedMap<Integer, ValueProviderConfig>> getSortedValueProviderConfigs() {
		SortedMap<Integer, SortedMap<Integer, ValueProviderConfig>> result = new TreeMap<Integer, SortedMap<Integer,ValueProviderConfig>>();
		for (ValueProviderConfig config : getValueProviderConfigs()) {
			SortedMap<Integer, ValueProviderConfig> configs = result.get(config.getPageIndex());
			if (configs == null) {
				configs = new TreeMap<Integer, ValueProviderConfig>();
				result.put(config.getPageIndex(), configs);
			}
			configs.put(config.getPageOrder(), config);
		}
		return result;
	}
	
	public SortedMap<Integer, List<ValueProviderConfig>> createAcquisitionSequence(ValueProviderProvider provider) 
	{
		List<AcquisitionParameterConfig> parameterConfigs = getParameterConfigs();
		int levelOffset = 0;
		SortedMap<Integer, List<ValueProviderConfig>> levels = new TreeMap<Integer, List<ValueProviderConfig>>();
		for (AcquisitionParameterConfig parameterConfig : parameterConfigs) {
			resolveProviderLevel(provider, parameterConfig, levels, levelOffset);
			for (Integer rLevel : levels.keySet()) {
				levelOffset = Math.max(levelOffset, rLevel);				
			}
			levelOffset ++;
		}
		
		int inverseLevel = Integer.MIN_VALUE;
		for (Entry<Integer, List<ValueProviderConfig>> entry : levels.entrySet()) {
			inverseLevel = Math.max(entry.getKey(), inverseLevel);
		}
		SortedMap<Integer, List<ValueProviderConfig>> result = new TreeMap<Integer, List<ValueProviderConfig>>();
		for (Entry<Integer, List<ValueProviderConfig>> entry : levels.entrySet()) {
			int i = 0;
			for (ValueProviderConfig config : entry.getValue()) {
				config.setPageIndex(inverseLevel);
				config.setPageOrder(i++);
			}
			result.put(new Integer(inverseLevel--), entry.getValue());
		}
		
		return result;
	}
	
	protected void resolveProviderLevel(
			ValueProviderProvider provider, 
			ValueConsumer consumer, 
			Map<Integer, List<ValueProviderConfig>> levels, int level
		) 
	{
		if (consumer instanceof AcquisitionParameterConfig) {
			AcquisitionParameterConfig parameterConfig = (AcquisitionParameterConfig) consumer;
			ValueConsumerBinding binding = getValueConsumerBinding(parameterConfig, parameterConfig.getParameterID());
			if (binding == null)
				return;
			resolveProviderLevel(provider, binding.getProvider(), levels, level);
		}
		else if (consumer instanceof ValueProviderConfig) {
			ValueProviderConfig providerConfig = (ValueProviderConfig) consumer;
			addToLevel(level, levels, providerConfig);
			ValueProvider valueProvider = provider.getValueProvider(providerConfig);
			if (valueProvider == null || valueProvider.getInputParameters() == null)
				return;
			for (ValueProviderInputParameter inputParameter : valueProvider.getInputParameters()) {
				ValueConsumerBinding binding = getValueConsumerBinding(providerConfig, inputParameter.getParameterID());
				if (binding == null)
					continue;
				resolveProviderLevel(provider, binding.getProvider(), levels, (level+1));
			}
		}
		else 
			throw new IllegalStateException("resolveProviderLevel called with unknown consumer type: "+consumer.getClass().getName());
	}
	
	private void addToLevel(int level, Map<Integer, List<ValueProviderConfig>> levels, ValueProviderConfig providerConfig) {
		Integer l = new Integer(level);
		List<ValueProviderConfig> configs = levels.get(l);
		if (configs == null) { 
			configs = new LinkedList<ValueProviderConfig>();
			levels.put(l, configs);
		}
		configs.add(providerConfig);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, ValueConsumerBinding> getValueConsumerBindings(ValueConsumer consumer) {
		// TODO: Rewrite with generics when bcel bug fixed
		if (consumer2Binding == null) {
			consumer2Binding = new HashMap<String, Map<String, ValueConsumerBinding>>();			
			for (ValueConsumerBinding binding : valueConsumerBindings) {
				if (binding != null && binding.getConsumer() != null) {
					String bindingKey = binding.getConsumer().getConsumerKey();
					Map bindings = consumer2Binding.get(bindingKey);
					if (bindings == null) {
						bindings = new HashMap();
						consumer2Binding.put(bindingKey, bindings);
					}
					bindings.put(binding.getParameterID(), binding);					
				}
			}
		}
		return consumer2Binding.get(consumer.getConsumerKey());
	}
	
	public ValueConsumerBinding getValueConsumerBinding(ValueConsumer consumer, String parameterID) {
		Map bindings = getValueConsumerBindings(consumer);
		if (bindings != null)
			return (ValueConsumerBinding) bindings.get(parameterID);
		return null;
	}
	
	public ValueConsumerBinding getValueProviderBinding(ValueProviderConfig provider) {
		String key = provider.getConsumerKey();
		if (provider2Binding == null) {
			provider2Binding = new HashMap<String, ValueConsumerBinding>();			
			for (ValueConsumerBinding binding : valueConsumerBindings) {
				if (binding != null && binding.getProvider() != null) {
					String bindingKey = binding.getProvider().getConsumerKey();
					provider2Binding.put(bindingKey, binding);					
				}
			}
		}
		return provider2Binding.get(key);
	}
	
	public void clearBindingIndexes() {
		provider2Binding = null;
		consumer2Binding = null;
	}
}
