package org.nightlabs.jfire.reporting.parameter.config;

import com.sun.jdi.Value;

public class ValueConsumerBinding {
	private String organisationID;
	private long valueConsumerBindingID;
	private ValueConsumer consumer;
	private String parameterID;	
	private ValueProviderConfig provider;
	
	protected ValueConsumerBinding() {
		
	}
	
	public ValueConsumerBinding(String organisationID, long valueConsumerBindingID) {
		this.organisationID = organisationID;
		this.valueConsumerBindingID = valueConsumerBindingID;
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
	
}
