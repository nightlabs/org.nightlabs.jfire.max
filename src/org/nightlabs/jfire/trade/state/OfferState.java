package org.nightlabs.jfire.trade.state;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Offer;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.state.State"
 *		detachable="true"
 *		table="JFireTrade_OfferState"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class OfferState
extends State
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	protected OfferState() { }

	public OfferState(String organisationID, long articleContainerStateID, User user, Offer offer, OfferStateDefinition offerStateDefinition)
	{
		super(organisationID, articleContainerStateID, user, offer, offerStateDefinition);
	}

	/**
	 * This is a convenience method calling the super method {@link State#getStatable()}.
	 * @return the result of {@link State#getStatable()}
	 */
	public Offer getOffer()
	{
		return (Offer) getStatable();
	}
	/**
	 * This is a convenience method calling the super method {@link State#getStateDefinition()}.
	 * @return the result of {@link State#getStateDefinition()}
	 */
	public OfferStateDefinition getOfferStateDefinition()
	{
		return (OfferStateDefinition) getStateDefinition();
	}

}
