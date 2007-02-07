/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

import org.nightlabs.jfire.reporting.parameter.ValueProvider;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface ValueProviderProvider {
	ValueProvider getValueProvider(ValueProviderConfig valueProviderConfig);
}
