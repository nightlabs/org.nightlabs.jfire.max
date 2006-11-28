package org.nightlabs.jfire.trade.state;

import java.io.Serializable;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.OfferLocal;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.state.id.StateDefinitionID"
 *		detachable="true"
 *		table="JFireTrade_StateDefinition"
 *
 * @jdo.inheritance strategy="subclass-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, stateDefinitionClass, stateDefinitionID"
 *
 * @jdo.fetch-group name="StateDefinition.name" fields="name"
 * @jdo.fetch-group name="StateDefinition.description" fields="description"
 */
public abstract class StateDefinition
implements Serializable
{
	public static final String FETCH_GROUP_NAME = "StateDefinition.name";
	public static final String FETCH_GROUP_DESCRIPTION = "StateDefinition.description";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String stateDefinitionClass;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String stateDefinitionID;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="stateDefinition"
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
	 * @deprecated Only for JDO!
	 */
	protected StateDefinition()
	{
	}

	public StateDefinition(String organisationID, Class articleContainerStateDefinitionClass, String articleContainerStateDefinitionID)
	{
		this(organisationID, articleContainerStateDefinitionClass.getName(), articleContainerStateDefinitionID);
	}

	public StateDefinition(String organisationID, String articleContainerStateDefinitionClass, String articleContainerStateDefinitionID)
	{
		this.organisationID = organisationID;
		this.stateDefinitionClass = articleContainerStateDefinitionClass;
		this.stateDefinitionID = articleContainerStateDefinitionID;
		this.name = new StateDefinitionName(this);
		this.description = new StateDefinitionDescription(this);
	}

	public static String getPrimaryKey(String organisationID, String articleContainerStateDefinitionClass, String articleContainerStateDefinitionID)
	{
		return organisationID + '/' + articleContainerStateDefinitionClass + '/' + articleContainerStateDefinitionID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getStateDefinitionClass()
	{
		return stateDefinitionClass;
	}
	public String getStateDefinitionID()
	{
		return stateDefinitionID;
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

	/**
	 * This method creates a new {@link State} and registers it in the {@link Statable} and {@link StatableLocal}.
	 * Note, that it won't be added to the {@link Statable} (but only to the {@link StatableLocal}), if {@link #isPublicState()}
	 * returns false.
	 * <p>
	 * This method calls {@link #_createState(User, Statable)} in order to obtain the new instance. Override that method
	 * in order to subclass {@link State}.
	 * </p>
	 * 
	 * @param user The user who is responsible for the action.
	 * @param statable The {@link Statable} that is transitioned to the new state.
	 * @return Returns the new newly created State instance.
	 */
	public State createArticleContainerState(User user, Statable statable)
	{
		State state = _createState(user, statable);

		statable.getStatableLocal().setState(state);

		if (isPublicState())
			statable.setState(state);

		return state;
	}

	/**
	 * This method creates an instance of {@link State}. It is called by {@link #createArticleContainerState(User, Offer)}.
	 * This method does NOT register anything. You should override this method if you want to subclass {@link State}.
	 */
	protected abstract State _createState(User user, Statable statable);

	public StateDefinitionName getName()
	{
		return name;
	}
	public StateDefinitionDescription getDescription()
	{
		return description;
	}
}
