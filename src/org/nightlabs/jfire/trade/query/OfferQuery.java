/**
 * 
 */
package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.trade.Offer;


/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author marco schulze - marco at nightlabs dot de
 */
public class OfferQuery
//extends ArticleContainerQuery<Set<Offer>>
extends ArticleContainerQuery
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OfferQuery() {
		super(Offer.class);
	}

	@Override
	protected void checkCustomer(StringBuffer filter) {
		if (getCustomerID() != null) {
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//		filter.append("\n && JDOHelper.getObjectId(this.order.customer) == :customerID");
		// WORKAROUND:
		filter.append("\n && (" +
				"this.order.customer.organisationID == \""+getCustomerID().organisationID+"\" && " +
				"this.order.customer.anchorTypeID == \""+getCustomerID().anchorTypeID+"\" && " +
				"this.order.customer.anchorID == \""+getCustomerID().anchorID+"\"" +
						")");
		}
	}

	@Override
	protected void checkVendor(StringBuffer filter) {
		if (getVendorID() != null)
		{
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//			filter.append("\n && JDOHelper.getObjectId(this.vendor) == :vendorID");
			// WORKAROUND:
			filter.append("\n && (" +
					"this.order.vendor.organisationID == \""+getVendorID().organisationID+"\" && " +
					"this.order.vendor.anchorTypeID == \""+getVendorID().anchorTypeID+"\" && " +
					"this.order.vendor.anchorID == \""+getVendorID().anchorID+"\"" +
							")");
		}	
	}
}
