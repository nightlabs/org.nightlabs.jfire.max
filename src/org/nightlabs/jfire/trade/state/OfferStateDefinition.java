package org.nightlabs.jfire.trade.state;

import java.io.Serializable;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.OfferLocal;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.state.id.OfferStateDefinitionID"
 *		detachable="true"
 *		table="JFireTrade_OfferStateDefinition"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, offerStateDefinitionID"
 *
 * @jdo.fetch-group name="OfferStateDefinition.name" fields="name"
 */
public class OfferStateDefinition
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "OfferStateDefinition.name";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String offerStateDefinitionID;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="offerStateDefinition"
	 */
	private OfferStateDefinitionName name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean publicState = false;

	/**
	 * @deprecated Only for JDO!
	 */
	protected OfferStateDefinition() { }

	public OfferStateDefinition(String organisationID, String offerStateDefinitionID)
	{
		this.organisationID = organisationID;
		this.offerStateDefinitionID = offerStateDefinitionID;
		this.name = new OfferStateDefinitionName(this);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getOfferStateDefinitionID()
	{
		return offerStateDefinitionID;
	}

	public static String getPrimaryKey(String organisationID, String offerStateDefinitionID)
	{
		return organisationID + '/' + offerStateDefinitionID;
	}

	public OfferStateDefinitionName getName()
	{
		return name;
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

	/**
	 * This method creates a new {@link OfferState} and registers it in the {@link Offer} and {@link OfferLocal}.
	 * Note, that it won't be added to the {@link Offer} (but only to the {@link OfferLocal}), if {@link #isPublicState()}
	 * returns false.
	 * <p>
	 * This method calls {@link #_createOfferState(User, Offer)} in order to obtain the new instance. Override that method
	 * if you feel the need for subclassing {@link OfferState}.
	 * </p>
	 * 
	 * @param user The user who is responsible for the action.
	 * @param offer The offer that is transitioned to the new state.
	 * @return Returns the new newly created OfferState instance.
	 */
	public OfferState createOfferState(User user, Offer offer)
	{
		OfferState offerState = _createOfferState(user, offer);

		offer.getOfferLocal().setOfferState(offerState);

		if (isPublicState())
			offer.setOfferState(offerState);

		return offerState;
	}

	/**
	 * This method creates an instance of {@link OfferState}. It is called by {@link #createOfferState(User, Offer)}.
	 * This method does NOT register anything. You should override this method if you want to subclass {@link OfferState}.
	 */
	protected OfferState _createOfferState(User user, Offer offer)
	{
		return new OfferState(user.getOrganisationID(), IDGenerator.nextID(OfferState.class), user, offer, this);
	}
}
