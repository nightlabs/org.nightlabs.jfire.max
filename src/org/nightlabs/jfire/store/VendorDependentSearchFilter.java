package org.nightlabs.jfire.store;

import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jfire.transfer.id.AnchorID;

//TODO: extend this class from AbstractJDOQuery
public abstract class VendorDependentSearchFilter
extends SearchFilter
//extends AbstractJDOQuery
{
	private AnchorID vendorID = null;

	public VendorDependentSearchFilter(int conjunction) {
		super(conjunction);
	}
	
	/**
	 * @return Returns the VendorID.
	 */
	public AnchorID  getVendorID() {
		return vendorID;
	}

	/**
	 * @param anchorTypeID The VendorID to set.
	 */
	public void setVendorID(AnchorID  vendorID) {
		this.vendorID = vendorID;
	}
	
}
