/*
 * Created 	on Sep 20, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.accounting.book.fragmentbased;

import java.util.Collection;
import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.ipanema.accounting.PriceFragmentType;
import org.nightlabs.ipanema.accounting.book.MoneyFlowDimension;
import org.nightlabs.ipanema.store.ProductType;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.accounting.book.MoneyFlowDimension"
 *		detachable="true"
 *		table="JFireTrade_PriceFragmentDimension"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class PriceFragmentDimension extends MoneyFlowDimension {

	public static final String MONEY_FLOW_DIMENSION_ID = PriceFragmentDimension.class.getName(); 
	
	/**
	 */
	public PriceFragmentDimension() {
		super();
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.book.MoneyFlowDimension#getMoneyFlowDimensionID()
	 */
	public String getMoneyFlowDimensionID() {
		return MONEY_FLOW_DIMENSION_ID;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.book.MoneyFlowDimension#getValues()
	 */
	public String[] getValues(ProductType productType) {
		Query q = getPersistenceManager().newQuery(PriceFragmentType.class);
		Collection pfs = (Collection)q.execute();
		String[] result = new String[pfs.size()];
		int i = 0;
		for (Iterator iter = pfs.iterator(); iter.hasNext();) {
			PriceFragmentType pft = (PriceFragmentType) iter.next();
			result[i++] = pft.getPrimaryKey();
		}
		return result;		
	}
	
	public PersistenceManager getPersistenceManager() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of PriceFragmentDimension is not persistent. Can't get PersistenceManager");
		return pm;
	}

}
