package org.nightlabs.jfire.trade.state;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.jfire.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.state.id.ArticleContainerStateID"
 *		detachable="true"
 *		table="JFireTrade_State"
 *
 * @jdo.inheritance strategy="subclass-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, articleContainerStateID"
 *
 * @jdo.fetch-group name="State.user" fields="user"
 * @jdo.fetch-group name="State.statable" fields="statable"
 * @jdo.fetch-group name="State.stateDefinition" fields="stateDefinition"
 */
public abstract class State
implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long articleContainerStateID;


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
	private ArticleContainerStateDefinition stateDefinition;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private Date createDT;

	/**
	 * @deprecated Only for JDO!
	 */
	protected State() { }

	public State(
			String organisationID, long articleContainerStateID,
			User user, Statable statable,
			ArticleContainerStateDefinition articleContainerStateDefinition)
	{
		this.organisationID = organisationID;
		this.articleContainerStateID = articleContainerStateID;
		this.user = user;
		this.statable = statable;
		this.stateDefinition = articleContainerStateDefinition;
		this.createDT = new Date();
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public long getArticleContainerStateID()
	{
		return articleContainerStateID;
	}
	public User getUser()
	{
		return user;
	}
	public Statable getStatable()
	{
		return statable;
	}
	public ArticleContainerStateDefinition getStateDefinition()
	{
		return stateDefinition;
	}
	public Date getCreateDT()
	{
		return createDT;
	}

}
