/**
 * 
 */
package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.trade.Offer;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public class OfferQuickSearchQuery 
extends AbstractArticleContainerQuickSearchQuery 
{
	@Override
	public Class getArticleContainerClass() {
		return Offer.class;
	}

	@Override
	public String getArticleContainerIDMemberName() {
		return "offerID";
	}

	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "offerIDPrefix";
	}
}
