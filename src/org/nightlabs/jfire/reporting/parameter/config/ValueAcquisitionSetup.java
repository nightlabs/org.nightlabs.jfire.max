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
}
