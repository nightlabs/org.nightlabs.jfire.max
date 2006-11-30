package org.nightlabs.jfire.jbpm.graph.def;

import java.io.Serializable;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.graph.def.id.TransitionID;
import org.nightlabs.jfire.trade.state.id.StateDefinitionID;

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
 */
public class Transition
		implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "Transition.name";

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
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		dependent="true"
	 *		mapped-by="transition"
	 *		null-value="exception"
	 */
	private TransitionName name;

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
	public StateDefinition getFromStateDefinition()
	{
		return fromStateDefinition;
	}
	public String getJbpmTransitionName()
	{
		return jbpmTransitionName;
	}
}
