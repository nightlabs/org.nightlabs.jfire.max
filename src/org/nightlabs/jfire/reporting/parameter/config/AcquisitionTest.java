/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.reporting.parameter.ValueProvider;
import org.nightlabs.jfire.reporting.parameter.ValueProviderCategory;
import org.nightlabs.jfire.reporting.parameter.ValueProviderInputParameter;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class AcquisitionTest {

	private static String organsiationID = "chezfrancois.jfire.org";
	
	public static ValueAcquisitionSetup createSetup() {
		ValueAcquisitionSetup setup = new ValueAcquisitionSetup(organsiationID, 0, null, null);
		
		List<AcquisitionParameterConfig> parameterConfigs = new ArrayList<AcquisitionParameterConfig>();
		AcquisitionParameterConfig pc1 = new AcquisitionParameterConfig(setup);
		pc1.setParameterID("param1");
		pc1.setParameterType(String.class.getName());
		AcquisitionParameterConfig pc2 = new AcquisitionParameterConfig(setup);
		pc2.setParameterID("param2");
		pc2.setParameterType(Integer.class.getName());
		setup.getParameterConfigs().add(pc1);
		setup.getParameterConfigs().add(pc2);
		
		
		Set<ValueProviderConfig> providerConfigs = new HashSet<ValueProviderConfig>();
		ValueProviderConfig vpc1 = new ValueProviderConfig(setup, IDGenerator.nextID(ValueProviderConfig.class));
		vpc1.setValueProviderID("vp1");
		ValueProviderConfig vpc2 = new ValueProviderConfig(setup, IDGenerator.nextID(ValueProviderConfig.class));
		vpc2.setValueProviderID("vp2");
		ValueProviderConfig vpc3 = new ValueProviderConfig(setup, IDGenerator.nextID(ValueProviderConfig.class));
		vpc3.setValueProviderID("vp3");
		ValueProviderConfig vpc4 = new ValueProviderConfig(setup, IDGenerator.nextID(ValueProviderConfig.class));
		vpc4.setValueProviderID("vp4");
		ValueProviderConfig vpc5 = new ValueProviderConfig(setup, IDGenerator.nextID(ValueProviderConfig.class));
		vpc5.setValueProviderID("vp5");
		ValueProviderConfig vpc6 = new ValueProviderConfig(setup, IDGenerator.nextID(ValueProviderConfig.class));
		vpc6.setValueProviderID("vp6");
		setup.getValueProviderConfigs().add(vpc1);
		setup.getValueProviderConfigs().add(vpc2);
		setup.getValueProviderConfigs().add(vpc3);
		setup.getValueProviderConfigs().add(vpc4);
		setup.getValueProviderConfigs().add(vpc5);
		
		
		Set<ValueConsumerBinding> bindings = new HashSet<ValueConsumerBinding>();
		ValueConsumerBinding b1 = new ValueConsumerBinding(organsiationID, 0, setup);
		b1.setConsumer(pc1);
		b1.setParameterID("param1");
		b1.setProvider(vpc1);
		
		ValueConsumerBinding b2 = new ValueConsumerBinding(organsiationID, 1, setup);
		b2.setConsumer(vpc1);
		b2.setParameterID("param1");
		b2.setProvider(vpc2);
		
		ValueConsumerBinding b3 = new ValueConsumerBinding(organsiationID, 2, setup);
		b3.setConsumer(vpc1);
		b3.setParameterID("param2");
		b3.setProvider(vpc3);
		
		ValueConsumerBinding b4 = new ValueConsumerBinding(organsiationID, 3, setup);
		b4.setConsumer(vpc2);
		b4.setParameterID("param1");
		b4.setProvider(vpc4);
		
		ValueConsumerBinding b5 = new ValueConsumerBinding(organsiationID, 4, setup);
		b5.setConsumer(pc2);
		b5.setParameterID("param2");
		b5.setProvider(vpc5);
		
		ValueConsumerBinding b6 = new ValueConsumerBinding(organsiationID, 5, setup);
		b6.setConsumer(vpc5);
		b6.setParameterID("param1");
		b6.setProvider(vpc6);
		setup.getValueConsumerBindings().add(b1);
		setup.getValueConsumerBindings().add(b2);
		setup.getValueConsumerBindings().add(b3);
		setup.getValueConsumerBindings().add(b4);
		setup.getValueConsumerBindings().add(b5);
		setup.getValueConsumerBindings().add(b6);

//		setup.getValueProviderConfigs()
		return setup;
	}
	
	public static class DummyProviderProvider implements ValueProviderProvider {

		private Map<String, ValueProvider> providers = new HashMap<String, ValueProvider>();
		
		public DummyProviderProvider() {
			ValueProviderCategory cat = new ValueProviderCategory(null, organsiationID, "cat1", false);
			ValueProvider vp1 = new ValueProvider(cat, "vp1", String.class.getName());
			vp1.addInputParameter(new ValueProviderInputParameter(vp1, "param1", Integer.class.getName()));
			vp1.addInputParameter(new ValueProviderInputParameter(vp1, "param2", String.class.getName()));
			providers.put("vp1", vp1);
			
			ValueProvider vp2 = new ValueProvider(cat, "vp2", Integer.class.getName());
			vp2.addInputParameter(new ValueProviderInputParameter(vp2, "param1", Date.class.getName()));
			providers.put("vp2", vp2);

			ValueProvider vp3 = new ValueProvider(cat, "vp3", String.class.getName());
			providers.put("vp3", vp3);
			
			ValueProvider vp4 = new ValueProvider(cat, "vp4", Date.class.getName());
			providers.put("vp4", vp4);
			
			ValueProvider vp5 = new ValueProvider(cat, "vp5", Integer.class.getName());
			vp5.addInputParameter(new ValueProviderInputParameter(vp5, "param1", String.class.getName()));
			providers.put("vp5", vp5);

			ValueProvider vp6 = new ValueProvider(cat, "vp6", String.class.getName());
			providers.put("vp6", vp6);
		}
		
		public ValueProvider getValueProvider(ValueProviderConfig valueProviderConfig) {
			return providers.get(valueProviderConfig.getValueProviderID());
		}
		
	}
	
	
	public SortedMap<Integer, List<ValueProviderConfig>> createAcquisitionSequence(
			ValueAcquisitionSetup setup, ValueProviderProvider provider
		)
	{
		List<AcquisitionParameterConfig> parameterConfigs = setup.getParameterConfigs();
		int levelOffset = 0;
		SortedMap<Integer, List<ValueProviderConfig>> levels = new TreeMap<Integer, List<ValueProviderConfig>>();
		for (AcquisitionParameterConfig parameterConfig : parameterConfigs) {
			resolveProviderLevel(setup, provider, parameterConfig, levels, levelOffset);
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
				config.setPageRow(i++);
			}
			result.put(new Integer(inverseLevel--), entry.getValue());
		}
		
		return result;
	}
	
	public void resolveProviderLevel(
			ValueAcquisitionSetup setup, ValueProviderProvider provider,
			ValueConsumer consumer,
			Map<Integer, List<ValueProviderConfig>> levels, int level
		)
	{
		if (consumer instanceof AcquisitionParameterConfig) {
			AcquisitionParameterConfig parameterConfig = (AcquisitionParameterConfig) consumer;
			ValueConsumerBinding binding = setup.getValueConsumerBinding(parameterConfig, parameterConfig.getParameterID());
			if (binding == null)
				return;
			resolveProviderLevel(setup, provider, binding.getProvider(), levels, level);
		}
		else if (consumer instanceof ValueProviderConfig) {
			ValueProviderConfig providerConfig = (ValueProviderConfig) consumer;
			addToLevel(level, levels, providerConfig);
			ValueProvider valueProvider = provider.getValueProvider(providerConfig);
			if (valueProvider == null || valueProvider.getInputParameters() == null)
				return;
			for (ValueProviderInputParameter inputParameter : valueProvider.getInputParameters()) {
				ValueConsumerBinding binding = setup.getValueConsumerBinding(providerConfig, inputParameter.getParameterID());
				if (binding == null)
					continue;
				resolveProviderLevel(setup, provider, binding.getProvider(), levels, (level+1));
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
	
	

	/**
	 * 
	 */
	public AcquisitionTest() {
	}
	
	public static void main(String[] args) {
		DummyProviderProvider provider = new DummyProviderProvider();
		ValueAcquisitionSetup setup = createSetup();
		AcquisitionTest test = new AcquisitionTest();
		SortedMap<Integer, List<ValueProviderConfig>> result = test.createAcquisitionSequence(setup, provider);

		
		for (Entry<Integer, List<ValueProviderConfig>> entry : result.entrySet()) {
			System.out.println("*** Page "+entry.getKey()+" ***");
			for (ValueProviderConfig config : entry.getValue()) {
				System.out.println("    -> part "+config.getPageRow()+": "+config.getValueProviderID());
			}
		}
		
	}

}
