package org.nightlabs.jfire.trade.query;

import org.nightlabs.jfire.store.ReceptionNote;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public class ReceptionNoteQuery
	extends AbstractArticleContainerQuery
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getArticleContainerIDMemberName() {
		return "receptionNoteID";
	}

	@Override
	public String getArticleContainerIDPrefixMemberName() {
		return "receptionNoteIDPrefix";
	}

	@Override
	protected Class<ReceptionNote> initCandidateClass()
	{
		return ReceptionNote.class;
	}

	@Override
	protected void checkAdditionalFields(StringBuilder filter)
	{
	}

	@Override
	protected void checkCustomer(StringBuilder filter)
	{
		final AnchorID customerID = getCustomerID();
		if (customerID != null)
		{
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//			filter.append("\n && JDOHelper.getObjectId(this.customer) == :customerID");
			// WORKAROUND:
			filter.append("\n && (" +
				"this.deliveryNote.customer.organisationID == \""+customerID.organisationID+"\" && " +
				"this.deliveryNote.customer.anchorTypeID == \""+customerID.anchorTypeID+"\" && " +
				"this.deliveryNote.customer.anchorID == \""+customerID.anchorID+"\"" +
			")");
		}
	}

	@Override
	protected void checkCustomerName(StringBuilder filter)
	{
		if (getCustomerName() != null)
		{
			filter.append("\n && (this.deliveryNote.customer.person.displayName.toLowerCase().indexOf(\""
				+getCustomerName().toLowerCase()+"\") >= 0)");
		}
	}

	@Override
	protected void checkVendor(StringBuilder filter)
	{
		final AnchorID vendorID = getVendorID();
		if (vendorID != null)
		{
			// FIXME: JPOX Bug JDOHelper.getObjectId(this.*) does not seem to work (java.lang.IndexOutOfBoundsException: Index: 3, Size: 3)
//			filter.append("\n && JDOHelper.getObjectId(this.vendor) == :vendorID");
			// WORKAROUND:
			filter.append("\n && (" +
				"this.deliveryNote.vendor.organisationID == \""+vendorID.organisationID+"\" && " +
				"this.deliveryNote.vendor.anchorTypeID == \""+vendorID.anchorTypeID+"\" && " +
				"this.deliveryNote.vendor.anchorID == \""+vendorID.anchorID+"\"" +
			")");
		}
	}

	@Override
	protected void checkVendorName(StringBuilder filter)
	{
		if (getVendorName() != null)
		{
			filter.append("\n && (this.deliveryNote.vendor.person.displayName.toLowerCase().indexOf(\"" +
				getVendorName().toLowerCase()+"\") >= 0)");
		}
	}
}
