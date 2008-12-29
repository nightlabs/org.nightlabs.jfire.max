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

package org.nightlabs.jfire.simpletrade.dao;

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.gridpriceconfig.TariffPricePair;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.simpletrade.SimpleTradeManager;
import org.nightlabs.jfire.simpletrade.SimpleTradeManagerUtil;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class TariffPricePairDAO
{
//	public static final long EXPIRE_MSEC = 1000 * 60 * 15; // 15 min

	private static TariffPricePairDAO _sharedInstance = null;

	public static TariffPricePairDAO sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new TariffPricePairDAO();

		return _sharedInstance;
	}

	protected static class TariffPricePairsCarrier {
		public TariffPricePairsCarrier(
				ProductTypeID productTypeID, CustomerGroupID customerGroupID,
				CurrencyID currencyID, Collection<TariffPricePair> tariffPricePairs)
		{
			this.productTypeID = productTypeID;
			this.customerGroupID = customerGroupID;
//			this.priceConfigID = priceConfigID;
			this.currencyID = currencyID;
			this.tariffPricePairs = tariffPricePairs;
		}

		public ProductTypeID productTypeID;
		public CustomerGroupID customerGroupID;
//		public PriceConfigID priceConfigID;
		public CurrencyID currencyID;
		public Collection<TariffPricePair> tariffPricePairs;
		public long loadDT = System.currentTimeMillis();
	}

	public static final String[] FETCH_GROUPS_TARIFF = new String[]{
		FetchPlan.DEFAULT, Tariff.FETCH_GROUP_NAME
	};

	public static final String[] FETCH_GROUPS_PRICE = new String[]{
		FetchPlan.DEFAULT, Price.FETCH_GROUP_CURRENCY
	};

//	/**
//	 * key: String priceConfigID_currencyID<br/>
//	 * value: TariffPricePairsCarrier tariffPricePairsCarrier
//	 */
//	private Map tariffPricePairsCarriers = new HashMap();

	public synchronized Collection<TariffPricePair> getTariffPricePairs(
			ProductTypeID productTypeID,
			CustomerGroupID customerGroupID,
			CurrencyID currencyID,
			ProgressMonitor monitor)
	{
//		if (priceConfigID == null)
//			throw new IllegalArgumentException("priceConfigID must not be null!");
		if (productTypeID == null)
			throw new IllegalArgumentException("productTypeID must not be null!");

		if (customerGroupID == null)
			throw new IllegalArgumentException("customerGroupID must not be null!");

		if (currencyID == null)
			throw new IllegalArgumentException("currencyID must not be null!");

		String key = productTypeID.toString() + "::" + customerGroupID.toString() + "::" + currencyID.toString();

//		TariffPricePairsCarrier tppc = (TariffPricePairsCarrier) tariffPricePairsCarriers.get(key);

		TariffPricePairsCarrier tppc = (TariffPricePairsCarrier) Cache.sharedInstance().get(
				TariffPricePairDAO.class.getName(), key, (String[])null, -1);
		if (tppc == null) { // || System.currentTimeMillis() - tppc.loadDT > EXPIRE_MSEC) {
			try {
				SimpleTradeManager stm = SimpleTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
				Collection<TariffPricePair> tariffPricePairs = CollectionUtil.castCollection(stm.getTariffPricePairs(productTypeID,
						customerGroupID,
						currencyID,
						FETCH_GROUPS_TARIFF,
						FETCH_GROUPS_PRICE));

				tppc = new TariffPricePairsCarrier(
						productTypeID, customerGroupID, currencyID, tariffPricePairs);
//				tariffPricePairsCarriers.put(key, tppc);

				// TODO We have to somehow get notified when the appropriate TariffUserSet changes (and evict the record from the cache). Needs some more thoughts. Marco.
				Cache.sharedInstance().put(TariffPricePairDAO.class.getName(), key, tppc, (String[])null, -1);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return tppc.tariffPricePairs;
	}
}
