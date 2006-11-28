package org.nightlabs.jfire.trade.state;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.jfire.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.state.id.StateID"
 *		detachable="true"
 *		table="JFireTrade_State"
 *
 * @jdo.inheritance strategy="subclass-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, stateID"
 *
 * @jdo.fetch-group name="State.user" fields="user"
 * @jdo.fetch-group name="State.statable" fields="statable"
 * @jdo.fetch-group name="State.stateDefinition" fields="stateDefinition"
 */
public abstract class State
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_USER = "State.user";
	public static final String FETCH_GROUP_STATABLE = "State.statable";
	public static final String FETCH_GROUP_STATE_DEFINITION = "State.stateDefinition";

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
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private User user;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private Statable statable;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
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

	public State(
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
