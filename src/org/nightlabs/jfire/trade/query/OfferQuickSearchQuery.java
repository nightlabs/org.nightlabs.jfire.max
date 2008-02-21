/**
 * 
 */
package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.trade.Offer;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author marco schulze - marco at nightlabs dot de
 */
public class OfferQuickSearchQuery
extends AbstractArticleContainerQuickSearchQuery
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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

	@Override
	protected void checkVendorName(StringBuffer filter)
	{
		if (getVendorName() != null)
			filter.append("\n && (this.order.vendor.person.displayName.toLowerCase().indexOf(\""+getVendorName().toLowerCase()+"\") >= 0)");
	}

	@Override
	protected void checkCustomerName(StringBuffer filter)
	{
		if (getCustomerName() != null)
			filter.append("\n && (this.order.customer.person.displayName.toLowerCase().indexOf(\""+getCustomerName().toLowerCase()+"\") >= 0)");
	}
}
