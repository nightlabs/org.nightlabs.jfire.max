package org.nightlabs.jfire.store.search;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.transfer.id.AnchorID;

public abstract class VendorDependentQuery
//extends SearchFilter
extends AbstractJDOQuery
{
	private static final String PROPERTY_PREFIX = "VendorDependentQuery.";
	public static final String PROPERTY_VENDOR_ID = PROPERTY_PREFIX + "vendorID";
	private AnchorID vendorID = null;
	
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
		notifyListeners(PROPERTY_VENDOR_ID, oldVendorID, vendorID);
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.query.AbstractJDOQuery#prepareQuery(javax.jdo.Query)
	 */
	@Override
	protected void prepareQuery(Query q) 
	{
		PersistenceManager pm = getPersistenceManager();
//		StringBuffer filter = new StringBuffer();
		StringBuffer filter = getFilter();
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
		
//		checkAdditionalFields(filter);
		
		q.setFilter(filter.toString());
	}
	
	@Override
	public List<FieldChangeCarrier> getChangedFields(String propertyName)
	{
		List<FieldChangeCarrier> changedFields = super.getChangedFields(propertyName);
		boolean allFields = AbstractSearchQuery.PROPERTY_WHOLE_QUERY.equals(propertyName);
		if (allFields || PROPERTY_VENDOR_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_VENDOR_ID, vendorID) );
		}
		
		return changedFields;		
	}
	
//	/**
//	 * Subclasses can override this method to enhance the query (filter)
//	 * @param filter the StringBuffer containing the filter for the query
//	 */
//	protected void checkAdditionalFields(StringBuffer filter) {
//		// By default empty but subclasses can enhance the query by overwriting this method
//	}
}
