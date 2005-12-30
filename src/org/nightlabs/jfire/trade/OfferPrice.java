/*
 * Created on 04.11.2004
 */
package org.nightlabs.jfire.trade;

import java.util.Iterator;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.Price"
 *		detachable="true"
 *		table="JFireTrade_OfferPrice"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class OfferPrice extends org.nightlabs.jfire.accounting.Price
{
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Offer offer;

	public OfferPrice() { }

	/**
	 * @param organisationID
	 * @param priceID
	 * @param currency
	 * 
	 * @deprecated test
	 */
	public OfferPrice(Offer offer, String organisationID, long priceConfigID, long priceID)
	{
		super(organisationID, priceConfigID, priceID, offer.getCurrency());
		this.offer = offer;

		calculatePrice();
	}

	protected void calculatePrice()
	{
		setAmount(0);
		clearFragments();

		for (Iterator it = offer.getArticles().iterator(); it.hasNext(); ) {
			Article offerItem = (Article)it.next();
			ArticlePrice offerItemPrice = offerItem.getPrice();
			sumPrice(offerItemPrice);
		}
	}

	/**
	 * @return Returns the offer.
	 */
	protected Offer getOffer()
	{
		return offer;
	}
}
