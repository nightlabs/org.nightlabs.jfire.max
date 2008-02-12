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
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Offer offer;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected OfferPrice() { }

	/**
	 * @param organisationID
	 * @param priceID
	 * @param currency
	 * 
	 * @deprecated test
	 */
	@Deprecated
	public OfferPrice(Offer offer, String organisationID, String priceConfigID, long priceID)
	{
		super(organisationID, priceConfigID, priceID, offer.getCurrency());
		this.offer = offer;

		calculatePrice();
	}

	protected void calculatePrice()
	{
		setAmount(0);
		clearFragments();

		for (Article article : offer.getArticles()) {
			ArticlePrice articlePrice = article.getPrice();
			sumPrice(articlePrice);
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
