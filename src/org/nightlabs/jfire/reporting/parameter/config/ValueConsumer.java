package org.nightlabs.jfire.reporting.parameter.config;

/**
 * Interface used in a {@link ValueAcquisitionSetup} to bind
 * a {@link ValueProviderConfig} either to an {@link AcquisitionParameterConfig} or to a {@link ValueProviderConfig}.
 * 
 * @see ValueConsumerBinding
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public interface ValueConsumer {
	
	/**
	 * Returns a key of this consumer, that is unique withing an {@link ValueAcquisitionSetup}.
	 * This includes the the parameterID for {@link AcquisitionParameterConfig}s or the providerID for {@link ValueProviderConfig}s.  
	 *  
	 * @return A key of this consumer, that is unique withing an {@link ValueAcquisitionSetup}.
	 */
	String getConsumerKey();

}
