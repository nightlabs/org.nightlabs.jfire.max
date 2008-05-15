package org.nightlabs.jfire.store;

import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jfire.transfer.id.AnchorID;

public abstract class VendorDependentSearchFilter
extends SearchFilter
{
	private AnchorID vendorID = null;

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

	public VendorDependentSearchFilter(int conjunction) {
		super(conjunction);
	}
}
