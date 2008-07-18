package org.nightlabs.jfire.trade.history;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ProductHistoryItemComparator 
implements Comparator<ProductHistoryItem>, Serializable 
{
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ProductHistoryItem phi1, ProductHistoryItem phi2) {
		if (phi1.getCreateDT() != null && phi2.getCreateDT() != null) {
			return phi1.getCreateDT().compareTo(phi2.getCreateDT());
		}
		return 0;
	}

}
