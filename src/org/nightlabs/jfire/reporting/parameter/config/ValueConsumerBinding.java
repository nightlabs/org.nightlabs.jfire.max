package org.nightlabs.jfire.reporting.parameter.config;


/**
 * {@link ValueConsumerBinding} define a binding from a {@link ValueProviderConfig}
 * to a {@link ValueConsumer} (that can be an {@link AcquisitionParameterConfig} or {@link ValueProviderConfig}).
 * The output of the bindings {@link ValueProviderConfig} will be the input for its 
 * {@link ValueConsumer} and the given {@link #parameterID}.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.parameter.config.id.ValueConsumerBindingID"
 *		detachable = "true"
 *		table="JFireReporting_ValueConsumerBinding"
 *
 * @jdo.create-objectid-class field-order="organisationID, valueConsumerBindingID"
 * 
 * @jdo.implements name="org.nightlabs.jfire.reporting.parameter.config.ValueConsumer"
 */
public class ValueConsumerBinding {
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long valueConsumerBindingID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ValueConsumer consumer;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String parameterID;	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ValueProviderConfig provider;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ValueAcquisitionSetup setup;
	
	/**
	 * @deprecated Only for JDO
	 */
	protected ValueConsumerBinding() {}

	/**
	 * Create a new ValueConsumerBinding.
	 * 
	 * @param organisationID The organisationID
	 * @param valueConsumerBindingID The binding ID.
	 */
	public ValueConsumerBinding(String organisationID, long valueConsumerBindingID, ValueAcquisitionSetup setup) {
		this.organisationID = organisationID;
		this.valueConsumerBindingID = valueConsumerBindingID;
		this.setup = setup;
	}	
	
	/**
	 * @return the consumer
	 */
	public ValueConsumer getConsumer() {
		return consumer;
	}
	/**
	 * @param consumer the consumer to set
	 */
	public void setConsumer(ValueConsumer consumer) {
		this.consumer = consumer;
	}
	/**
	 * @return the parameterID
	 */
	public String getParameterID() {
		return parameterID;
	}
	/**
	 * @param parameterID the parameterID to set
	 */
	public void setParameterID(String parameterID) {
		this.parameterID = parameterID;
	}
	/**
	 * @return the provider
	 */
	public ValueProviderConfig getProvider() {
		return provider;
	}
	/**
	 * @param provider the provider to set
	 */
	public void setProvider(ValueProviderConfig provider) {
		this.provider = provider;
	}
	/**
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	/**
	 * @return the valueConsumerBindingID
	 */
	public long getValueConsumerBindingID() {
		return valueConsumerBindingID;
	}

	/**
	 * @return the setup
	 */
	public ValueAcquisitionSetup getSetup() {
		return setup;
	}
	
}
