/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

import java.util.Map;

import org.nightlabs.jfire.reporting.parameter.ValueProvider;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class AcquisitionTest {

	public class ProviderProvider implements ValueProviderProvider {
		Map<String, ValueProvider> providers;

		public ValueProvider getValueProvider(ValueProviderConfig valueProviderConfig) {
			return providers.get(valueProviderConfig.getValueProviderID());
		}
		
		public void addValueProvider(ValueProvider valueProvider) {
			providers.put(valueProvider.getValueProviderID(), valueProvider);
		}
	}
	
	private static String organsiationID = "chezfrancois.jfire.org";
	
	public ValueAcquisitionSetup createSetup(ProviderProvider providerProvider) {
		ValueAcquisitionSetup setup = new ValueAcquisitionSetup(organsiationID, 0);
		
		return setup;
	}
	
	/**
	 * 
	 */
	public AcquisitionTest() {
	}

}
