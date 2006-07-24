package org.nightlabs.jfire.trade;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Utils;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.OfferActionHandlerID"
 *		detachable="true"
 *		table="JFireTrade_OfferActionHandler"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, offerActionHandlerID"
 */
public class OfferActionHandler
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String offerActionHandlerID;

	/**
	 * @deprecated Only for JDO!
	 */
	protected OfferActionHandler() { }

	public OfferActionHandler(String organisationID, String offerActionHandlerID)
	{
		this.organisationID = organisationID;
		this.offerActionHandlerID = offerActionHandlerID;
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getOfferActionHandlerID()
	{
		return offerActionHandlerID;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof OfferActionHandler))
			return false;

		OfferActionHandler o = (OfferActionHandler) obj;

		return
				Utils.equals(this.organisationID, o.organisationID) &&
				Utils.equals(this.offerActionHandlerID, o.offerActionHandlerID);
	}

	@Override
	public int hashCode()
	{
		return Utils.hashCode(organisationID) ^ Utils.hashCode(offerActionHandlerID);
	}

	public void onFinalizeOffer(User user, Offer offer)
	{
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of OfferActionHandler has currently no PersistenceManager assigned!");

		return pm;
	}
}
