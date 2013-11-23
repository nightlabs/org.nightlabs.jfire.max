package org.nightlabs.jfire.trade;

import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import org.nightlabs.jfire.trade.id.OfferActionHandlerID;
import javax.jdo.annotations.Discriminator;

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
@PersistenceCapable(
	objectIdClass=OfferActionHandlerID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_OfferActionHandler")
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class OfferActionHandler
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String offerActionHandlerID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
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
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.offerActionHandlerID, o.offerActionHandlerID);
	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) ^ Util.hashCode(offerActionHandlerID);
	}

	public void onFinalizeOffer(User user, Offer offer)
	{
	}

	public void onAcceptOffer(User user, Offer offer)
	{
	}

	public void onArticlesTariffChanged(User user, Offer offer, Set<Article> articles)
	{

	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of OfferActionHandler has currently no PersistenceManager assigned!");

		return pm;
	}

	public void onRejectOffer(User user, Offer offer) {

	}
}
