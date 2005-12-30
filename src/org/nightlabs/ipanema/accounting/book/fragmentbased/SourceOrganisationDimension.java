/*
 * Created 	on Oct 7, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.accounting.book.fragmentbased;

import javax.jdo.JDOHelper;

import org.nightlabs.ipanema.accounting.book.MoneyFlowDimension;
import org.nightlabs.ipanema.store.ProductType;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.accounting.book.MoneyFlowDimension"
 *		detachable="true"
 *		table="JFireTrade_SourceOrganisationDimension"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class SourceOrganisationDimension extends MoneyFlowDimension {

	public static final String MONEY_FLOW_DIMENSION_ID = SourceOrganisationDimension.class.getName();

	/**
	 * 
	 */
	public SourceOrganisationDimension() {
		super();
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.book.MoneyFlowMapping#getMappingConditionKey()
	 */
	public String getMappingConditionKey() {
		return MONEY_FLOW_DIMENSION_ID;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.book.MoneyFlowDimension#getMoneyFlowDimensionID()
	 */
	public String getMoneyFlowDimensionID() {
		return MONEY_FLOW_DIMENSION_ID;
	}

	/**
	 * @see org.nightlabs.ipanema.accounting.book.MoneyFlowDimension#getValues(org.nightlabs.ipanema.store.ProductType)
	 */
	public String[] getValues(ProductType productType) {
		if (!(JDOHelper.isPersistent(productType) && !JDOHelper.isDetached(productType)))
			throw new IllegalStateException("OwnerDimension can only return values for attached ProductTypes.");
		return new String[] { productType.getOrganisationID() };
	}

}
