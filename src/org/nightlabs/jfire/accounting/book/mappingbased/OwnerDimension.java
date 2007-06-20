/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.accounting.book.mappingbased;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowDimension"
 *		detachable="true"
 *		table="JFireTrade_OwnerDimension"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class OwnerDimension extends MoneyFlowDimension {
	
	public static final String MONEY_FLOW_DIMENSION_ID = OwnerDimension.class.getName();
	
	/**
	 * 
	 */
	public OwnerDimension() {
		super();
	}

	/**
	 * @see org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowDimension#getMoneyFlowDimensionID()
	 */
	public String getMoneyFlowDimensionID() {
		return MONEY_FLOW_DIMENSION_ID;
	}

	/**
	 * @see org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowDimension#getValues(org.nightlabs.jfire.store.ProductType, Article)
	 */
	public String[] getValues(ProductType productType, Article bookArticle) {
		if (!(JDOHelper.isPersistent(productType) && !JDOHelper.isDetached(productType)))
			throw new IllegalStateException("OwnerDimension can only return values for attached ProductTypes.");
		return new String[] { productType.getOwner().getPrimaryKey() };
	}

}
