package org.nightlabs.jfire.store.search;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jfire.transfer.id.AnchorID;

public abstract class VendorDependentQuery
	extends AbstractJDOQuery
{
	private AnchorID vendorID = null;

	public static final class FieldName
	{
		public static final String vendorID = "vendorID";
	}

	/**
	 * @return Returns the VendorID.
	 */
	public AnchorID getVendorID() {
		return vendorID;
	}

	/**
	 * @param anchorTypeID The VendorID to set.
	 */
	public void setVendorID(AnchorID vendorID) {
		final AnchorID oldVendorID = this.vendorID;
		this.vendorID = vendorID;
		notifyListeners(FieldName.vendorID, oldVendorID, vendorID);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.query.AbstractJDOQuery#prepareQuery(javax.jdo.Query)
	 */
	@Override
	protected void prepareQuery(Query q)
	{
		PersistenceManager pm = getPersistenceManager();
		StringBuilder filter = getFilter();
		filter.append("true");

		if (vendorID != null)
		{
			filter.append("\n && JDOHelper.getObjectId(this.vendor) == :vendorID");
//			// TODO: WORKAROUND: JPOX
//			filter.append("\n && (" +
//					"this.vendor.organisationID == \""+vendorID.organisationID+"\" && " +
//					"this.vendor.anchorTypeID == \""+vendorID.anchorTypeID+"\" && " +
//					"this.vendor.anchorID == \""+vendorID.anchorID+"\"" +
//							")");
		}

		q.setFilter(filter.toString());
	}
}