package org.nightlabs.jfire.reporting.parameter;

import java.io.Serializable;

import javax.jdo.annotations.Persistent;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderInputParameterID;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;

/**
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.parameter.id.ValueProviderInputParameterID"
 *		detachable = "true"
 *		table="JFireReporting_ValueProviderInputParameter"
 *
 * @jdo.create-objectid-class field-order="organisationID, valueProviderCategoryID, valueProviderID, parameterID"
 * 
 * @jdo.inheritance strategy = "new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 * 
 * @jdo.fetch-group name="ValueProviderInputParameter.valueProvider" fetch-groups="default" fields="valueProvider"
 * @jdo.fetch-group name="ValueProviderInputParameter.this" fetch-groups="default" fields="valueProvider"
 */@PersistenceCapable(
	objectIdClass=ValueProviderInputParameterID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireReporting_ValueProviderInputParameter")
@FetchGroups({
	@FetchGroup(
		fetchGroups={"default"},
		name=ValueProviderInputParameter.FETCH_GROUP_VALUE_PROVIDER,
		members=@Persistent(name="valueProvider")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ValueProviderInputParameter.FETCH_GROUP_THIS_VALUE_PROVIDER_INPUT_PARAMETER,
		members=@Persistent(name="valueProvider"))
})
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class ValueProviderInputParameter implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_VALUE_PROVIDER = "ValueProviderInputParameter.valueProvider";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_VALUE_PROVIDER_INPUT_PARAMETER = "ValueProviderInputParameter.this";
			 
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true" @jdo.column length="100"
	 */	@PrimaryKey

	private String valueProviderCategoryID;
	
	/**
	 * @jdo.field primary-key="true" @jdo.column length="100"
	 */	@PrimaryKey

	private String valueProviderID;
	
	/**
	 * @jdo.field primary-key="true"
	 */	@PrimaryKey

	private String parameterID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private String parameterType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private ValueProvider valueProvider;
	
	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
	protected ValueProviderInputParameter() {}
	
	public ValueProviderInputParameter(ValueProvider valueProvider, String parameterID, String parameterType) {
		this.parameterID = parameterID;
		this.parameterType = parameterType;
		if (valueProvider != null)
			setValueProvider(valueProvider);
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
	 * @return the parameterType
	 */
	public String getParameterType() {
		return parameterType;
	}

	/**
	 * @param parameterType the parameterType to set
	 */
	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	/**
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return the valueProvider
	 */
	public ValueProvider getValueProvider() {
		return valueProvider;
	}

	/**
	 * @return the valueProviderID
	 */
	public String getValueProviderID() {
		return valueProviderID;
	}

	/**
	 * Used when adding a parameter to a Value Provider.
	 * 
	 * @param provider
	 */
	protected void setValueProvider(ValueProvider provider) {
		this.organisationID = provider.getOrganisationID();
		this.valueProviderCategoryID = provider.getValueProviderCategoryID();
		this.valueProviderID = provider.getValueProviderID();
		this.valueProvider = provider;
	}

	/**
	 * @return the valueProviderCategoryID
	 */
	public String getValueProviderCategoryID() {
		return valueProviderCategoryID;
	}
}
