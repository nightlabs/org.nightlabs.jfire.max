package org.nightlabs.jfire.jbpm.graph.def;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.jbpm.graph.def.id.StateDefinitionID;
import org.nightlabs.jfire.jbpm.graph.def.id.TransitionID;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.jbpm.graph.def.id.TransitionID"
 *		detachable="true"
 *		table="JFireJbpm_Transition"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="
 *				processDefinitionOrganisationID, processDefinitionID,
 *				stateDefinitionOrganisationID, stateDefinitionID,
 *				transitionOrganisationID, transitionID"
 *
 * @jdo.fetch-group name="Transition.name" fields="name"
 *
 * @jdo.query name="getTransitionByStateDefinitionAndTransitionName" query="SELECT UNIQUE
 *		WHERE
 *			this.processDefinitionOrganisationID == :processDefinitionOrganisationID &&
 *			this.processDefinitionID == :processDefinitionID &&
 *			this.stateDefinitionOrganisationID == :stateDefinitionOrganisationID &&
 *			this.stateDefinitionID == :stateDefinitionID &&
 *			this.jbpmTransitionName == :jbpmTransitionName"
 *
 * @jdo.query name="getTransitionsByTransitionName" query="SELECT
 *		WHERE
 *			this.processDefinitionOrganisationID == :processDefinitionOrganisationID &&
 *			this.processDefinitionID == :processDefinitionID &&
 *			this.jbpmTransitionName == :jbpmTransitionName"
 */
@PersistenceCapable(
	objectIdClass=TransitionID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireJbpm_Transition")
@FetchGroups(
	@FetchGroup(
		name=Transition.FETCH_GROUP_NAME,
		members=@Persistent(name="name"))
)
@Queries({
	@javax.jdo.annotations.Query(
		name="getTransitionByStateDefinitionAndTransitionName",
		value="SELECT UNIQUE WHERE this.processDefinitionOrganisationID == :processDefinitionOrganisationID && this.processDefinitionID == :processDefinitionID && this.stateDefinitionOrganisationID == :stateDefinitionOrganisationID && this.stateDefinitionID == :stateDefinitionID && this.jbpmTransitionName == :jbpmTransitionName"),
	@javax.jdo.annotations.Query(
		name="getTransitionsByTransitionName",
		value="SELECT WHERE this.processDefinitionOrganisationID == :processDefinitionOrganisationID && this.processDefinitionID == :processDefinitionID && this.jbpmTransitionName == :jbpmTransitionName")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Transition
		implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "Transition.name";

	@SuppressWarnings("unchecked")
	public static Transition getTransition(PersistenceManager pm, StateDefinitionID stateDefinitionID, String jbpmTransitionName)
	{
		HashMap params = new HashMap(5);
		params.put("processDefinitionOrganisationID", stateDefinitionID.processDefinitionOrganisationID);
		params.put("processDefinitionID", stateDefinitionID.processDefinitionID);
		params.put("stateDefinitionOrganisationID", stateDefinitionID.stateDefinitionOrganisationID);
		params.put("stateDefinitionID", stateDefinitionID.stateDefinitionID);
		params.put("jbpmTransitionName", jbpmTransitionName);
		Query q = pm.newNamedQuery(Transition.class, "getTransitionByStateDefinitionAndTransitionName");
		return (Transition) q.executeWithMap(params);
	}

	@SuppressWarnings("unchecked")
	public static List<Transition> getTransitions(PersistenceManager pm, ProcessDefinitionID processDefinitionID, String jbpmTransitionName)
	{
		HashMap params = new HashMap(3);
		params.put("processDefinitionOrganisationID", processDefinitionID.organisationID);
		params.put("processDefinitionID", processDefinitionID.processDefinitionID);
		params.put("jbpmTransitionName", jbpmTransitionName);
		Query q = pm.newNamedQuery(Transition.class, "getTransitionsByTransitionName");
		return (List<Transition>) q.executeWithMap(params);
	}

	public static TransitionID getTransitionID(org.jbpm.graph.def.Transition jbpmTransition)
	{
		StateDefinitionID stateDefinitionID = StateDefinition.getStateDefinitionID(jbpmTransition.getFrom());

		String transitionName = jbpmTransition.getName();
		String organisationID;
		String transitionID;
		if (transitionName.indexOf(':') < 0) {
			organisationID = IDGenerator.getOrganisationID(); // TODO is it safe to allow local names (without organisationID)? do we really never share process definitions across organisations?
			transitionID = transitionName;
		}
		else {
			String[] parts = transitionName.split(":");
			if (parts.length != 2)
				throw new IllegalStateException("transition.name does not contain exactly one or two parts: " + transitionName);
			organisationID = parts[0];
			transitionID = parts[1];
		}
		return TransitionID.create(
				stateDefinitionID.processDefinitionOrganisationID, stateDefinitionID.processDefinitionID,
				stateDefinitionID.stateDefinitionOrganisationID, stateDefinitionID.stateDefinitionID,
				organisationID, transitionID);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String processDefinitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	@PrimaryKey
	@Column(length=50)
	private String processDefinitionID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String stateDefinitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	@PrimaryKey
	@Column(length=50)
	private String stateDefinitionID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String transitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	@PrimaryKey
	@Column(length=50)
	private String transitionID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private String jbpmTransitionName;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean userExecutable = true;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="transition" dependent="true"
	 */
	@Persistent(
		dependent="true",
		mappedBy="transition",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private TransitionName name;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="transition" dependent="true"
	 */
	@Persistent(
		dependent="true",
		mappedBy="transition",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private TransitionDescription description;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private StateDefinition fromStateDefinition;

	protected Transition() { }

	public Transition(
			StateDefinition fromStateDefinition,
			String jbpmTransitionName
	)
	{
		this.fromStateDefinition = fromStateDefinition;
		this.processDefinitionOrganisationID = fromStateDefinition.getProcessDefinitionOrganisationID();
		this.processDefinitionID = fromStateDefinition.getProcessDefinitionID();
		this.stateDefinitionOrganisationID = fromStateDefinition.getStateDefinitionOrganisationID();
		this.stateDefinitionID = fromStateDefinition.getStateDefinitionID();
		this.jbpmTransitionName = jbpmTransitionName;

		if (jbpmTransitionName.indexOf(':') < 0) {
			this.transitionOrganisationID = IDGenerator.getOrganisationID(); // TODO is it safe to allow local names (without organisationID)? do we really never share process definitions across organisations?
			this.transitionID = jbpmTransitionName;
		}
		else {
			String[] parts = jbpmTransitionName.split(":");
			if (parts.length != 2)
				throw new IllegalStateException("jbpmTransitionName does not contain exactly one or two parts: " + jbpmTransitionName);
			this.transitionOrganisationID = parts[0];
			this.transitionID = parts[1];
		}

		this.name = new TransitionName(this);
		this.description = new TransitionDescription(this);
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
	public TransitionName getName()
	{
		return name;
	}
	public TransitionDescription getDescription() {
		return description;
	}
	public StateDefinition getFromStateDefinition()
	{
		return fromStateDefinition;
	}
	public String getJbpmTransitionName()
	{
		return jbpmTransitionName;
	}

	public boolean isUserExecutable()
	{
		return userExecutable;
	}
	public void setUserExecutable(boolean userExecutable)
	{
		this.userExecutable = userExecutable;
	}
}
