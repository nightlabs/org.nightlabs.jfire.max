/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.trade;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.trade.id.OfferRequirementID;

/**
 * One instance of OfferRequirement exists for each Offer on the side of the vendor.
 * The OfferRequirement bundles all other offers that the vendor needs to create to
 * fulfill its own offer.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.OfferRequirementID"
 *		detachable="true"
 *		table="JFireTrade_OfferRequirement"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, offerIDPrefix, offerID"
 */
public class OfferRequirement
	implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * This method returns a previously existing {@link OfferRequirement} or creates and persists
	 * a new instance if not existent.
	 */
	public static OfferRequirement getOfferRequirement(PersistenceManager pm, Offer offer)
	{
		OfferRequirementID offerRequirementID = OfferRequirementID.create(offer.getOrganisationID(), offer.getOfferIDPrefix(), offer.getOfferID());
		pm.getExtent(OfferRequirement.class);
		try {
			OfferRequirement res = (OfferRequirement) pm.getObjectById(offerRequirementID);
			res.getOffer();
			return res;
		} catch (JDOObjectNotFoundException x) {
			return (OfferRequirement) pm.makePersistent(new OfferRequirement(offer));
		}
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="50"
	 */
	private String offerIDPrefix;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long offerID;

	private Offer offer;

	/**
	 * key: LegalEntity vendor<br/>
	 * value: Offer offer
	 * <br/><br/>
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="LegalEntity"
	 *		value-type="Offer"
	 *
	 * @jdo.join
	 */
	private Map vendor2offer = new HashMap();

	public OfferRequirement() { }

	public OfferRequirement(Offer offer)
	{
		if (offer == null)
			throw new NullPointerException("offer");

		this.offer = offer;
		this.organisationID = offer.getOrganisationID();
		this.offerIDPrefix = offer.getOfferIDPrefix();
		this.offerID = offer.getOfferID();
	}
	
	public void addPartnerOffer(Offer offer) {
		OrganisationLegalEntity vendor = offer.getOrder().getVendor();
		Offer other = (Offer) vendor2offer.get(vendor);
		if (offer.equals(other))
			return; // already registered - ignore

		if (other != null)
			throw new IllegalStateException("Vendor-Offer cannot be added, because another Offer is already assigned for this vendor! offer.primaryKey=" + offer.getPrimaryKey() + " otherOffer.primaryKey=" + other.getPrimaryKey());

		vendor2offer.put(vendor, offer);
	}
	
	/**
	 * Returns the Offer for this vendor or null. 
	 * @param vendor
	 */
	public Offer getPartnerOffer(LegalEntity vendor) {
		return (Offer)vendor2offer.get(vendor);
	}
	
	/**
	 * 
	 * @return The associated Offer.
	 */
	public Offer getOffer() {
		return offer;
	}

}
