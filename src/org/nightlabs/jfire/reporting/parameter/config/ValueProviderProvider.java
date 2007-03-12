/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

import org.nightlabs.jfire.reporting.parameter.ValueProvider;

/**
 * Interface used by the structuring process of a {@link ValueAcquisitionSetup}
 * to obtain {@link ValueProvider}s and their definition on the basis
 * of a {@link ValueProviderConfig}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface ValueProviderProvider {
	/**
	 * Returns the {@link ValueProvider} that is referenced by the given valueProviderConfig.
	 * 
	 * @param valueProviderConfig The {@link ValueProviderConfig} to search the {@link ValueProvider} for.	 * 
	 * @return The {@link ValueProvider} that is referenced by the given valueProviderConfig.
	 */
	ValueProvider getValueProvider(ValueProviderConfig valueProviderConfig);
}
