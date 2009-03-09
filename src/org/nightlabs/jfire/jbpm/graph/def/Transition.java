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
	private String processDefinitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String processDefinitionID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String stateDefinitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String stateDefinitionID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String transitionOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String transitionID;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private String jbpmTransitionName;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean userExecutable = true;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="transition"
	 */
	private TransitionName name;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="transition"
	 */
	private TransitionDescription description;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private StateDefinition fromStateDefinition;

	protected Transition()
	{
	}

	public Transition(
//			String processDefinitionOrganisationID, String processDefinitionID,
//			String stateDefinitionOrganisationID, String stateDefinitionID,
			StateDefinition fromStateDefinition,
			String jbpmTransitionName)
//			String transitionOrganisationID, String transitionID)
	{
		this.fromStateDefinition = fromStateDefinition;
		this.processDefinitionOrganisationID = fromStateDefinition.getProcessDefinitionOrganisationID();
		this.processDefinitionID = fromStateDefinition.getProcessDefinitionID();
		this.stateDefinitionOrganisationID = fromStateDefinition.getStateDefinitionOrganisationID();
		this.stateDefinitionID = fromStateDefinition.getStateDefinitionID();
		this.jbpmTransitionName = jbpmTransitionName;

//		this.processDefinitionOrganisationID = processDefinitionOrganisationID;
//		this.processDefinitionID = processDefinitionID;
//		this.stateDefinitionOrganisationID = stateDefinitionOrganisationID;
//		this.stateDefinitionID = stateDefinitionID;

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
