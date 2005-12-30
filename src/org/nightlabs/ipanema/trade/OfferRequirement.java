/*
 * Created on 07.11.2004
 */
package org.nightlabs.ipanema.trade;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * One instance of OfferRequirement exists for each Offer on the side of the vendor.
 * The OfferRequirement bundles all other offers that the vendor needs to create to
 * fulfill its own offer.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.ipanema.trade.id.OfferRequirementID"
 *		detachable="true"
 *		table="JFireTrade_OfferRequirement"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, offerID"
 */
public class OfferRequirement
	implements Serializable
{
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long offerID;

	private Trader trader;

	private Offer offer;

	/**
	 * key: String anchorPK (of the vendor LegalEntity)<br/>
	 * value: Offer offer
	 * <br/><br/>
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="Offer"
	 *
	 * @jdo.join
	 */
	private Map offersByVendor = new HashMap();

	public OfferRequirement() { }

	public OfferRequirement(Trader trader, Offer offer)
	{
		if (trader == null)
			throw new NullPointerException("trader");
		
		if (offer == null)
			throw new NullPointerException("offer");

		this.trader = trader;
		this.offer = offer;
		this.organisationID = offer.getOrganisationID();
		this.offerID = offer.getOfferID();
	}
	
	public void addOffer(Offer offer) {
		OrganisationLegalEntity vendor = offer.getOrder().getVendor();
		offersByVendor.put(vendor.getPrimaryKey(), vendor);
	}
	
	/**
	 * Returns the Offer for this vendor or null. 
	 * @param vendor
	 */
	public Offer getOfferByVendor(OrganisationLegalEntity vendor) {
		return (Offer)offersByVendor.get(vendor.getPrimaryKey());
	}
	
	/**
	 * 
	 * @return The associated Offer.
	 */
	public Offer getOffer() {
		return offer;
	}
	

}
