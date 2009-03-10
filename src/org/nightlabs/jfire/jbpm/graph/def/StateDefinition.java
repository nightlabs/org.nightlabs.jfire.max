package org.nightlabs.jfire.jbpm.graph.def;

import java.io.Serializable;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.jbpm.graph.node.EndState;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.jbpm.graph.def.id.ProcessDefinitionID;
import org.nightlabs.jfire.jbpm.graph.def.id.StateDefinitionID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.jbpm.graph.def.id.StateDefinitionID"
 *		detachable="true"
 *		table="JFireJbpm_StateDefinition"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class
 *		field-order="processDefinitionOrganisationID, processDefinitionID, stateDefinitionOrganisationID, stateDefinitionID"
 *
 * @jdo.fetch-group name="StateDefinition.name" fields="name"
 * @jdo.fetch-group name="StateDefinition.description" fields="description"
 *
 * @jdo.query name="getStateDefinitionByProcessDefinitionAndJbpmNodeName" query="SELECT UNIQUE
 *		WHERE
 *			this.processDefinition == :processDefinition &&
 *			this.jbpmNodeName == :jbpmNodeName"
 *
 * @jdo.query name="getStateDefinitionsForProcessDefinition"
 *		query="SELECT WHERE this.processDefinition == :processDefinition"
 */
public class StateDefinition
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "StateDefinition.name";
	public static final String FETCH_GROUP_DESCRIPTION = "StateDefinition.description";

	public static List<StateDefinition> getStateDefinitions(PersistenceManager pm, ProcessDefinition processDefinition)
	{
		Query q = pm.newNamedQuery(StateDefinition.class, "getStateDefinitionsForProcessDefinition");
		return (List<StateDefinition>) q.execute(processDefinition);
	}

	public static StateDefinition getStateDefinition(
			ProcessDefinition processDefinition, String jbpmNodeName)
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(processDefinition);
		if (pm == null)
			throw new IllegalArgumentException("processDefinition is currently not persistent! Cannot obtain PersistenceManager!");

		Query q = pm.newNamedQuery(StateDefinition.class, "getStateDefinitionByProcessDefinitionAndJbpmNodeName");
		return (StateDefinition) q.execute(processDefinition, jbpmNodeName);
	}

	public static StateDefinitionID getStateDefinitionID(org.jbpm.graph.def.Node jbpmNode)
	{
		ProcessDefinitionID processDefinitionID = ProcessDefinition.getProcessDefinitionID(jbpmNode.getProcessDefinition());

		String stateName = jbpmNode.getName();
		String organisationID;
		String stateDefinitionID;
		if (stateName.indexOf(':') < 0) {
			organisationID = IDGenerator.getOrganisationID(); // TODO is it safe to allow local names (without organisationID)? do we really never share process definitions across organisations?
			stateDefinitionID = stateName;
		}
		else {
			String[] parts = stateName.split(":");
			if (parts.length != 2)
				throw new IllegalStateException("state.name does not contain exactly one or two parts: " + stateName);
			organisationID = parts[0];
			stateDefinitionID = parts[1];
		}
		return StateDefinitionID.create(
				processDefinitionID.organisationID, processDefinitionID.processDefinitionID,
				organisationID, stateDefinitionID);
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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProcessDefinition processDefinition;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"  dependent="true"
	 */
	private String jbpmNodeName;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="stateDefinition"  dependent="true"
	 */
	private StateDefinitionName name;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="stateDefinition"
	 */
	private StateDefinitionDescription description;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean publicState = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean endState = false;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected StateDefinition()
	{
	}

	public StateDefinition(
			ProcessDefinition processDefinition,
//			String processDefinitionOrganisationID, String processDefinitionID,
			org.jbpm.graph.def.Node jbpmNode)
//			String stateDefinitionOrganisationID, String stateDefinitionID)
	{
		this.processDefinition = processDefinition;
		this.processDefinitionOrganisationID = processDefinition.getOrganisationID();
		this.processDefinitionID = processDefinition.getProcessDefinitionID();
//		this.processDefinitionOrganisationID = processDefinitionOrganisationID;
//		this.processDefinitionID = processDefinitionID;
		this.jbpmNodeName = jbpmNode.getName();
		this.endState = jbpmNode instanceof EndState;

		if (jbpmNodeName.indexOf(':') < 0) {
			this.stateDefinitionOrganisationID = IDGenerator.getOrganisationID(); // TODO is it safe to allow local names (without organisationID)? do we really never share process definitions across organisations?
			this.stateDefinitionID = jbpmNodeName;
		}
		else {
			String[] parts = jbpmNodeName.split(":");
			if (parts.length != 2)
				throw new IllegalStateException("jbpmNodeName does not contain exactly one or two parts: " + jbpmNodeName);
			this.stateDefinitionOrganisationID = parts[0];
			this.stateDefinitionID = parts[1];
		}
		
//		this.stateDefinitionOrganisationID = stateDefinitionOrganisationID;
//		this.stateDefinitionID = stateDefinitionID;
		this.name = new StateDefinitionName(this);
		this.description = new StateDefinitionDescription(this);
	}

	public static String getPrimaryKey(String processDefinitionOrganisationID, String processDefinitionID, String stateDefinitionOrganisationID, String stateDefinitionID)
	{
		return processDefinitionOrganisationID + '/' + processDefinitionID + '/' + stateDefinitionOrganisationID + '/' + stateDefinitionID;
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

	public ProcessDefinition getProcessDefinition()
	{
		return processDefinition;
	}

	/**
	 * If a state definition is marked as <code>publicState</code>, it will be exposed to other organisations
	 * by storing it in both the {@link OfferLocal} and the {@link Offer} instance. If it is not public,
	 * it is only stored in the {@link OfferLocal}.
	 *
	 * @return true, if it shall be registered in the non-local instance and therefore published to business partners.
	 */
	public boolean isPublicState()
	{
		return publicState;
	}

	public void setPublicState(boolean publicState)
	{
		this.publicState = publicState;
	}

	public boolean isEndState()
	{
		return endState;
	}

//	/**
//	 * This method creates a new {@link State} and registers it in the {@link Statable} and {@link StatableLocal}.
//	 * Note, that it won't be added to the {@link Statable} (but only to the {@link StatableLocal}), if {@link #isPublicState()}
//	 * returns false.
//	 * <p>
//	 * This method calls {@link #_createState(User, Statable)} in order to obtain the new instance. Override that method
//	 * in order to subclass {@link State}.
//	 * </p>
//	 *
//	 * @param user The user who is responsible for the action.
//	 * @param statable The {@link Statable} that is transitioned to the new state.
//	 * @return Returns the new newly created State instance.
//	 */
//	public State createState(User user, Statable statable)
//	{
//		State state = _createState(user, statable);
//
//		statable.getStatableLocal().setState(state);
//
//		if (isPublicState())
//			statable.setState(state);
//
//		return state;
//	}
//
//	/**
//	 * This method creates an instance of {@link State}. It is called by {@link #createState(User, Statable)}.
//	 * This method does NOT register anything. You should override this method if you want to subclass {@link State}.
//	 */
//	protected abstract State _createState(User user, Statable statable);

	public StateDefinitionName getName()
	{
		return name;
	}
	public StateDefinitionDescription getDescription()
	{
		return description;
	}

	public String getJbpmNodeName()
	{
		return jbpmNodeName;
	}

	/**
	 * This method creates a new instance of {@link State} and registers it in the {@link StatableLocal} (and
	 * if <code>{@link #isPublicState()} == true</code>, additionally in {@link Statable}).
	 *
	 * @param user The user who is responsible for the creation of the new <code>State</code>.
	 * @param statable The <code>Statable</code> instance for which the <code>State</code> is created.
	 * @return the new <code>State</code> instance.
	 */
	public State createState(User user, Statable statable)
	{
		State state = new State(IDGenerator.getOrganisationID(), IDGenerator.nextID(State.class), user, statable, this);

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm != null)
			state = pm.makePersistent(state);

		statable.getStatableLocal().setState(state);
		if (this.isPublicState())
				statable.setState(state);

		return state;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		
		if (!(obj instanceof StateDefinition))
			return false;
		
		StateDefinition stateDefinition = (StateDefinition) obj;

		return
			Util.equals(stateDefinition.processDefinitionID, this.processDefinitionID) &&
			Util.equals(stateDefinition.processDefinitionOrganisationID, this.processDefinitionOrganisationID) &&
			Util.equals(stateDefinition.stateDefinitionOrganisationID, this.stateDefinitionOrganisationID) &&
			Util.equals(stateDefinition.stateDefinitionID, this.stateDefinitionID);
	}
		
	@Override
	public int hashCode()
	{
		return
				Util.hashCode(this.processDefinitionID) ^
				Util.hashCode(this.processDefinitionOrganisationID) ^
				Util.hashCode(this.stateDefinitionOrganisationID) ^
				Util.hashCode(this.stateDefinitionID);
	}
}
