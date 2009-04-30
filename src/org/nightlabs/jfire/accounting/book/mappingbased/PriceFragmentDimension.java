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

import java.util.Collection;
import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;

import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowDimension"
 *		detachable="true"
 *		table="JFireTrade_PriceFragmentDimension"
 *
 * @jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_PriceFragmentDimension")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PriceFragmentDimension extends MoneyFlowDimension {

	public static final String MONEY_FLOW_DIMENSION_ID = PriceFragmentDimension.class.getName();
	
	/**
	 */
	public PriceFragmentDimension() {
		super();
	}

	/**
	 * @see org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowDimension#getMoneyFlowDimensionID()
	 */
	@Override
	public String getMoneyFlowDimensionID() {
		return MONEY_FLOW_DIMENSION_ID;
	}

	/**
	 * @see org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowDimension#getValues()
	 */
	@Override
	public String[] getValues(ProductType productType, Article bookArticle) {
		ProductType rootType = bookArticle.getProductType();
		IPriceConfig priceConfig = productType.getPriceConfigInPackage(rootType.getPrimaryKey());
		Collection<PriceFragmentType> pfs = priceConfig.getPriceFragmentTypes();
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
