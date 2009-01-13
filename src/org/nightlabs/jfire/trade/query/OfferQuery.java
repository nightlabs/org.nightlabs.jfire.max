package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.trade.Offer;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 * @author marco schulze - marco at nightlabs dot de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class OfferQuery
	extends AbstractArticleContainerQuery
{
	private static final long serialVersionUID = 1L;

	private boolean reserved;

	public boolean isReserved() {
		return reserved;
	}
	public void setReserved(boolean reserved) {
		this.reserved = reserved;
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
	protected void checkVendorName(StringBuilder filter)
	{
		if (getVendorName() != null)
			filter.append("\n && (this.order.vendor.person.displayName.toLowerCase().indexOf(\""+getVendorName().toLowerCase()+"\") >= 0)");
	}

	@Override
	protected void checkCustomerName(StringBuilder filter)
	{
		if (getCustomerName() != null)
			filter.append("\n && (this.order.customer.person.displayName.toLowerCase().indexOf(\""+getCustomerName().toLowerCase()+"\") >= 0)");
	}

	@Override
	protected void checkCustomer(StringBuilder filter) {
		if (getCustomerID() != null) {
			filter.append("\n && JDOHelper.getObjectId(this.order.customer) == :customerID");
//		// WORKAROUND:
//		filter.append("\n && (" +
//				"this.order.customer.organisationID == \""+getCustomerID().organisationID+"\" && " +
//				"this.order.customer.anchorTypeID == \""+getCustomerID().anchorTypeID+"\" && " +
//				"this.order.customer.anchorID == \""+getCustomerID().anchorID+"\"" +
//						")");
		}
	}

	@Override
	protected void checkVendor(StringBuilder filter) {
		if (getVendorID() != null)
		{
			filter.append("\n && JDOHelper.getObjectId(this.vendor) == :vendorID");
//			// WORKAROUND:
//			filter.append("\n && (" +
//					"this.order.vendor.organisationID == \""+getVendorID().organisationID+"\" && " +
//					"this.order.vendor.anchorTypeID == \""+getVendorID().anchorTypeID+"\" && " +
//					"this.order.vendor.anchorID == \""+getVendorID().anchorID+"\"" +
//							")");
		}
	}

	@Override
	protected Class<Offer> initCandidateClass()
	{
		return Offer.class;
	}

	@Override
	protected void checkAdditionalFields(StringBuilder filter)
	{
		if (reserved) {
			// reserved means finalized but not accepted
			filter.append("\n && this.offerLocal.acceptDT == null");
			filter.append(" && this.offerLocal.rejectDT == null");
			filter.append(" && this.finalizeDT != null");
		}
	}
}
