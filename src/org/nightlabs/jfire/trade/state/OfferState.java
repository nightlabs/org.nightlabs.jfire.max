package org.nightlabs.jfire.trade.state;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Offer;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.state.id.OfferStateID"
 *		detachable="true"
 *		table="JFireTrade_OfferState"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, offerStateID"
 *
 * @jdo.fetch-group name="OfferState.user" fields="user"
 * @jdo.fetch-group name="OfferState.offer" fields="offer"
 * @jdo.fetch-group name="OfferState.offerStateDefinition" fields="offerStateDefinition"
 */
public class OfferState
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
	private long offerStateID;


	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private User user;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private Offer offer;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private OfferStateDefinition offerStateDefinition;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	private Date createDT;

	/**
	 * @deprecated Only for JDO!
	 */
	protected OfferState() { }

	public OfferState(String organisationID, long offerStateID, User user, Offer offer, OfferStateDefinition offerStateDefinition)
	{
		this.organisationID = organisationID;
		this.offerStateID = offerStateID;
		this.user = user;
		this.offer = offer;
		this.offerStateDefinition = offerStateDefinition;
		this.createDT = new Date();
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public long getOfferStateID()
	{
		return offerStateID;
	}
	public User getUser()
	{
		return user;
	}
	public Offer getOffer()
	{
		return offer;
	}
	public OfferStateDefinition getOfferStateDefinition()
	{
		return offerStateDefinition;
	}
	public Date getCreateDT()
	{
		return createDT;
	}

}
