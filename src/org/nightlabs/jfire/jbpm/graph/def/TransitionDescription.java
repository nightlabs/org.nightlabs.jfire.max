package org.nightlabs.jfire.jbpm.graph.def;

import java.util.HashMap;
import java.util.Map;

import org.nightlabs.i18n.I18nText;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.jbpm.graph.def.id.TransitionDescriptionID;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Fitas Amine - fitas at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.jbpm.graph.def.id.TransitionDescriptionID"
 *		detachable="true"
 *		table="JFireJbpm_TransitionDescription"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="
 *				processDefinitionOrganisationID, processDefinitionID,
 *				stateDefinitionOrganisationID, stateDefinitionID,
 *				transitionOrganisationID, transitionID"
 *
 * @jdo.fetch-group name="Transition.description" fields="transition, descriptions"
 */@PersistenceCapable(
	objectIdClass=TransitionDescriptionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireJbpm_TransitionDescription")
@FetchGroups(
	@FetchGroup(
		name="Transition.description",
		members={@Persistent(name="transition"), @Persistent(name="descriptions")})
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)

public class TransitionDescription extends I18nText
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String processDefinitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */	@PrimaryKey
	@Column(length=50)

	private String processDefinitionID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String stateDefinitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */	@PrimaryKey
	@Column(length=50)

	private String stateDefinitionID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */	@PrimaryKey
	@Column(length=100)

	private String transitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */	@PrimaryKey
	@Column(length=50)

	private String transitionID;

	/**
	 * key: String languageID<br/>
	 * value: String name
	 * 
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="java.lang.String"
	 *		default-fetch-group="true"
	 *		table="JFireJbpm_TransitionDescription_descriptions"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireJbpm_TransitionDescription_descriptions",
		defaultFetchGroup="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)

	private Map<String, String> descriptions = new HashMap<String, String>();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)

	private Transition transition;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected TransitionDescription()
	{
	}

	public TransitionDescription(Transition transition)
	{
		this.processDefinitionOrganisationID = transition.getProcessDefinitionOrganisationID();
		this.processDefinitionID = transition.getProcessDefinitionID();
		this.stateDefinitionOrganisationID = transition.getStateDefinitionOrganisationID();
		this.stateDefinitionID = transition.getStateDefinitionID();
		this.transitionOrganisationID = transition.getTransitionOrganisationID();
		this.transitionID = transition.getTransitionID();
		this.transition = transition;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getI18nMap()
	 */
	@Override
	protected Map<String, String> getI18nMap()
	{
		return descriptions;
	}

	/**
	 * @see org.nightlabs.i18n.I18nText#getFallBackValue(java.lang.String)
	 */
	@Override
	protected String getFallBackValue(String languageID)
	{
		return transitionOrganisationID + ':' + transitionID;
	}

	public Transition getTransition()
	{
		return transition;
	}
	public String getProcessDefinitionOrganisationID()
	{
		return processDefinitionOrganisationID;
	}
	public String getProcessDefinitionID()
	{
		return processDefinitionID;
	}
	public String getStateDefinitionOrganisationID()
	{
		return stateDefinitionOrganisationID;
	}
	public String getStateDefinitionID()
	{
		return stateDefinitionID;
	}
	public String getTransitionOrganisationID()
	{
		return transitionOrganisationID;
	}
	public String getTransitionID()
	{
		return transitionID;
	}
}