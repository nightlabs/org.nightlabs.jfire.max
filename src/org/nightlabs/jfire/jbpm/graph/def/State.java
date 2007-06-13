package org.nightlabs.jfire.jbpm.graph.def;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.state.id.StateID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.state.id.StateID"
 *		detachable="true"
 *		table="JFireJbpm_State"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, stateID"
 *
 * @jdo.fetch-group name="State.user" fields="user"
 * @jdo.fetch-group name="State.statable" fields="statable"
 * @jdo.fetch-group name="State.stateDefinition" fields="stateDefinition"
 *
 * @jdo.query name="getStateIDsForStatable" query="SELECT JDOHelper.getObjectId(this)
 *		WHERE this.statable == :statable"
 */
public class State
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_USER = "State.user";
	public static final String FETCH_GROUP_STATABLE = "State.statable";
	public static final String FETCH_GROUP_STATE_DEFINITION = "State.stateDefinition";

	public static Set<StateID> getStateIDsForStatableID(PersistenceManager pm, ObjectID statableID)
	{
// TODO WORKAROUND for JPOX: The following would be cleaner, but it does not work :-(
//		Statable statable = (Statable) pm.getObjectById(statableID);
//		Query q = pm.newNamedQuery(State.class, "getStateIDsForStatable");
//		return new HashSet<StateID>((Collection<? extends StateID>) q.execute(statable));

// WORKAROUND begin
		Set<StateID> res = new HashSet<StateID>();
		Statable statable = (Statable) pm.getObjectById(statableID);
		Set<StateID> stateIDs = NLJDOHelper.getObjectIDSet(statable.getStates());
		res.addAll(stateIDs);

		StatableLocal statableLocal = statable.getStatableLocal();
		stateIDs = NLJDOHelper.getObjectIDSet(statableLocal.getStates());
		res.addAll(stateIDs);
		return res;
// WORKAROUND end
	}

	public static boolean hasState(PersistenceManager pm, ObjectID statableID, String jbpmNodeName)
	{
		// TODO use a query once JPOX issues with interfaces are fixed
		Statable statable = (Statable) pm.getObjectById(statableID);
		for (State state : statable.getStates()) {
			if (jbpmNodeName.equals(state.getStateDefinition().getJbpmNodeName()))
				return true;
		}
		for (State state : statable.getStatableLocal().getStates()) {
			if (jbpmNodeName.equals(state.getStateDefinition().getJbpmNodeName()))
				return true;
		}
		return false;
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long stateID;


	/**
	 * TODO JPOX Bug WORKAROUND: null-value="exception" causes problems in replication to another datastore
	 * @!jdo.field persistence-modifier="persistent" null-value="exception"
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User user;

	/**
	 * TODO JPOX Bug WORKAROUND: null-value="exception" causes problems in replication to another datastore
	 * @!jdo.field persistence-modifier="persistent" null-value="exception"
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Statable statable;

	/**
	 * TODO JPOX Bug WORKAROUND: null-value="exception" causes problems in replication to another datastore
	 * @!jdo.field persistence-modifier="persistent" null-value="exception"
	 * @jdo.field persistence-modifier="persistent"
	 */
	private StateDefinition stateDefinition;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private Date createDT;

	/**
	 * @deprecated Only for JDO!
	 */
	protected State() { }

	/**
	 * Use {@link StateDefinition#createState(User, Statable)} instead!
	 */
	protected State(
			String organisationID, long stateID,
			User user, Statable statable,
			StateDefinition stateDefinition)
	{
		this.organisationID = organisationID;
		this.stateID = stateID;
		this.user = user;
		this.statable = statable;
		this.stateDefinition = stateDefinition;
		this.createDT = new Date();

		// auto-registration causes a duplicate key exception (JPOX bug?!) => we register in the StateDefinition#createState method
		// which is called by ActionHandlerNodeEnter#doExecute(...)
//		// autoregister this State in Statable and StatableLocal
//		statable.getStatableLocal().setState(this);
//
//		if (stateDefinition.isPublicState())
//			statable.setState(this);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public long getStateID()
	{
		return stateID;
	}
	public User getUser()
	{
		return user;
	}
	public Statable getStatable()
	{
		return statable;
	}
	public StateDefinition getStateDefinition()
	{
		return stateDefinition;
	}
	public Date getCreateDT()
	{
		return createDT;
	}

}
