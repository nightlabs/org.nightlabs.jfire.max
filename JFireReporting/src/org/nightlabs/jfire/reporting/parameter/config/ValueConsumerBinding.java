package org.nightlabs.jfire.reporting.parameter.config;

import java.io.Serializable;

import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;

import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import org.nightlabs.jfire.reporting.parameter.config.id.ValueConsumerBindingID;


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
 * @jdo.create-objectid-class
 * 		field-order="organisationID, valueConsumerBindingID"
 * 		include-imports="id/ValueConsumerBinding.imports.inc"
 *		include-body="id/ValueConsumerBinding.body.inc"
 * 
 * @jdo.implements name="org.nightlabs.jfire.reporting.parameter.config.ValueConsumer"
 * 
 * @jdo.fetch-group name="ValueConsumerBinding.consumer" fetch-groups="default" fields="consumer"
 * @jdo.fetch-group name="ValueConsumerBinding.provider" fetch-groups="default" fields="provider"
 * @jdo.fetch-group name="ValueConsumerBinding.this" fetch-groups="default" fields="consumer, provider"
 * 
 */@PersistenceCapable(
	objectIdClass=ValueConsumerBindingID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireReporting_ValueConsumerBinding")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=ValueConsumerBinding.FETCH_GROUP_CONSUMER,
		members=@Persistent(name="consumer")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ValueConsumerBinding.FETCH_GROUP_PROVIDER,
		members=@Persistent(name="provider")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ValueConsumerBinding.FETCH_GROUP_THIS_VALUE_CONSUMER_BINDING,
		members={@Persistent(name="consumer"), @Persistent(name="provider")})
})

public class ValueConsumerBinding implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_CONSUMER = "ValueConsumerBinding.consumer";
	public static final String FETCH_GROUP_PROVIDER = "ValueConsumerBinding.provider";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_VALUE_CONSUMER_BINDING = "ValueConsumerBinding.this";
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */	@PrimaryKey

	private long valueConsumerBindingID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private ValueConsumer consumer;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private String parameterID;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private ValueProviderConfig provider;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private ValueAcquisitionSetup setup;
	
	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
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
	
	public ValueProviderID getValueProviderID() {
		if (getProvider() == null)
			return null;
		return ValueProviderID.create(
				getProvider().getValueProviderOrganisationID(),
				getProvider().getValueProviderCategoryID(),
				getProvider().getValueProviderID()
			);
	}
	
}
