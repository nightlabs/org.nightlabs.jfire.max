package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.jbpm.JbpmConstantsOffer;

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

	@Override
	protected Class<Offer> initCandidateClass()
	{
		return Offer.class;
	}

	@Override
	protected void checkAdditionalFields(StringBuffer filter)
	{
		if (reserved) {
//			filter.append("\n && ((this.states.contains(state)");
//			filter.append(" && state.stateDefinition.jbpmNodeName == \"" + JbpmConstantsOffer.Vendor.NODE_NAME_ACCEPTED + "\")");
//			filter.append("\n || (this.offerLocal.states.contains(stateLocal)");
//			filter.append(" && stateLocal.stateDefinition.jbpmNodeName == \"" + JbpmConstantsOffer.Vendor.NODE_NAME_ACCEPTED + "\")");
//			filter.append(")");

			// The accepted node is defined by dev.jfire.org and it is known that it is in the Statable (not only the StatableLocal).
			// Therefore, we only need to query this.states
			filter.append("\n && this.states.contains(state)");
			filter.append(" && state.stateDefinition.jbpmNodeName == \"" + JbpmConstantsOffer.Vendor.NODE_NAME_ACCEPTED + "\"");
		}
	}
}
