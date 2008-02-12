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

package org.nightlabs.jfire.accounting.gridpriceconfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfigUtil;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.jfire.trade.CustomerGroup;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfig"
 *		detachable="true"
 *		table="JFireTrade_StablePriceConfig"
 *
 * @jdo.inheritance strategy="superclass-table"
 *
 * @!jdo.query name="getPriceCellsForCustomerGroupAndCurrency" query="SELECT
 *		FROM org.nightlabs.jfire.accounting.gridpriceconfig.PriceCell 
 *		WHERE
 *			this.priceConfig == paramPriceConfig &&
 *			this.priceCoordinate.customerGroupPK == paramCustomerGroupPK &&
 *			this.priceCoordinate.currencyID == paramCurrencyID
 *		PARAMETERS StablePriceConfig paramPriceConfig, String paramCustomerGroupPK, String paramCurrencyID
 *		import java.lang.String; import org.nightlabs.jfire.accounting.gridpriceconfig.StablePriceConfig"
 *
 * @jdo.fetch-group name="StablePriceConfig.priceCells" fields="priceCells"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="priceCells"
 */
public class StablePriceConfig
extends GridPriceConfig
implements IPackagePriceConfig, IResultPriceConfig
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_PRICE_CELLS = "StablePriceConfig.priceCells";

	/**
	 * @see #getPriceCells(String, String)
	 */
	public Collection<PriceCell> getPriceCells(CustomerGroup customerGroup, Currency currency)
	{
		return getPriceCells(customerGroup.getPrimaryKey(), currency.getCurrencyID());
	}
	/**
	 * @return a <tt>Collection</tt> of {@link PriceCell}.
	 */
	@SuppressWarnings("unchecked")
	public Collection<PriceCell> getPriceCells(String customerGroupPK, String currencyID)
	{
		PersistenceManager pm = getPersistenceManager();
		//Query query = pm.newNamedQuery(StablePriceConfig.class, "getPriceCellsForCustomerGroupAndCurrency");
		Query query = pm.newNamedQuery(PriceCell.class, "getPriceCellsForCustomerGroupAndCurrency");
		return (Collection<PriceCell>) query.execute(this, customerGroupPK, currencyID);
	}

	/**
	 * key: PriceCoordinate priceCoordinate<br/>
	 * value: PriceCell priceCell
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="PriceCoordinate"
	 *		value-type="PriceCell"
	 *		mapped-by="priceConfig"
	 *		dependent-key="true"
	 *		dependent-value="true"
	 *
	 * @jdo.key mapped-by="priceCoordinate"
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="priceCoordinate"
	 */
	private Map<IPriceCoordinate, PriceCell> priceCells;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected StablePriceConfig() { }

	/**
	 * @param organisationID
	 * @param priceConfigID
	 */
	public StablePriceConfig(String organisationID, String priceConfigID)
	{
		super(organisationID, priceConfigID);
		priceCells = new HashMap<IPriceCoordinate, PriceCell>();
	}

	@Override
	@Implement
	public boolean requiresProductTypePackageInternal()
	{
		return false;
	}

	@Implement
	public Collection<PriceCell> getPriceCells()
	{
		return priceCells.values();
	}
	/**
	 * This method drops all calculation stati for all <tt>PriceFragment</tt> s
	 * in all <tt>PriceCell</tt> s which is equivalent to setting
	 * them to <tt>CALCULATIONSTATUS_DIRTY</tt>.
	 */
	@Implement
	public void resetPriceFragmentCalculationStati()
	{
		for (Iterator itPriceCells = this.getPriceCells().iterator(); itPriceCells.hasNext(); ) {
			PriceCell priceCell = (PriceCell)itPriceCells.next();
			priceCell.resetPriceFragmentCalculationStati();
		}
	}
	
	protected PriceCell createPriceCell(
			CustomerGroup customerGroup,
			Tariff tariff, Currency currency)
	{
		PriceCoordinate priceCoordinate = new PriceCoordinate(
				this, customerGroup,
				tariff, currency);
		return createPriceCell(priceCoordinate);
	}

	public PriceCell getPriceCell(
			CustomerGroup customerGroup,
			Tariff tariff, Currency currency,
			boolean throwExceptionIfNotExistent)
	{
		PriceCoordinate priceCoordinate = new PriceCoordinate(
				this, customerGroup,
				tariff, currency);
		return getPriceCell(priceCoordinate, throwExceptionIfNotExistent);
	}

	@Implement
	public PriceCell getPriceCell(IPriceCoordinate priceCoordinate, boolean throwExceptionIfNotExistent)
	{
		PriceCell priceCell = priceCells.get(priceCoordinate);

		// If the JDO implementation uses a shortcut (a direct JDOQL instead of loading the whole Map and then
		// search for the key), the cell might exist and not be found. Hence, we load the whole Map and try it again.
		if (priceCell == null) {
			for (Iterator it = priceCells.entrySet().iterator(); it.hasNext();) {
				Map.Entry me = (Map.Entry) it.next();
				if (me.getKey().equals(priceCoordinate)) {
					priceCell = (PriceCell) me.getValue();
					break;
				}
			}
		}

		if (throwExceptionIfNotExistent && priceCell == null)
			throw new IllegalArgumentException("No PriceCell found for "+priceCoordinate);
		return priceCell;
	}

	@Implement
	public PriceCell createPriceCell(IPriceCoordinate priceCoordinate)
	{
		if (priceCoordinate.getPriceConfig() == null ||
				!priceCoordinate.getPriceConfig().getPrimaryKey().equals(this.getPrimaryKey()))
			priceCoordinate = new PriceCoordinate(this, priceCoordinate);

		PriceCell priceCell = getPriceCell(priceCoordinate, false);
		if (priceCell == null) {
			priceCell = new PriceCell(priceCoordinate);
			priceCells.put(priceCoordinate, priceCell);
		}
		return priceCell;
	}

	protected void removePriceCell(
			CustomerGroup customerGroup,
			Tariff tariff, Currency currency)
	{
		PriceCoordinate priceCoordinate = new PriceCoordinate(
				this, customerGroup,
				tariff, currency);
		removePriceCell(priceCoordinate);
	}

	protected void removePriceCell(PriceCoordinate priceCoordinate)
	{
		priceCells.remove(priceCoordinate);
	}

	protected static class SingleObjectList extends ArrayList
	{
		private static final long serialVersionUID = 1L;

		public SingleObjectList(Object obj)
		{
			super(1);
			add(obj);
		}
	}

	/**
	 * This method adds all missing price cells into the multidimensional grid.
	 * To speed it up, every dimension can be defined (be not <tt>null</tt>). All
	 * defined dimensions will not be iterated.
	 * <p>
	 * <b>Important</b>: The current behaviour is that only exactly zero or one parameter
	 * can be defined, the others must be <tt>null</tt>. This might be changed later.
	 *
	 * @param customerGroup <tt>null</tt> or a specific CustomerGroup for which to add the missing cells in the other dimensions. 
	 * @param tariff <tt>null</tt> or a specific Tariff for which to add the missing cells in the other dimensions.
	 * @param currency <tt>null</tt> or a specific Currency for which to add the missing cells in the other dimensions.
	 */
	protected void createPriceCells(
			CustomerGroup _customerGroup, Tariff _tariff,
			Currency _currency)
	{
		int paramCount = 0;
		if (_customerGroup != null) ++paramCount;
		if (_tariff != null) ++paramCount;
		if (_currency != null) ++paramCount;
		if (paramCount > 1)
			throw new IllegalArgumentException("More than one parameter defined!");

		if (_customerGroup != null && !containsCustomerGroup(_customerGroup))
			throw new IllegalArgumentException("Given CustomerGroup is not a registered parameter!");
		if (_tariff != null && !containsTariff(_tariff))
			throw new IllegalArgumentException("Given Tariff is not a registered parameter!");
		if (_currency != null && !containsCurrency(_currency))
			throw new IllegalArgumentException("Given Currency is not a registered parameter!");

		Collection customerGroups = _customerGroup == null ? getCustomerGroups() : new SingleObjectList(_customerGroup);
		Collection tariffs = _tariff == null ? getTariffs() : new SingleObjectList(_tariff);
		Collection currencies = _currency == null ? getCurrencies() : new SingleObjectList(_currency);

		for (Iterator itCustomerGroups = customerGroups.iterator(); itCustomerGroups.hasNext(); ) {
			CustomerGroup customerGroup = (CustomerGroup)itCustomerGroups.next();

			for (Iterator itTariffs = tariffs.iterator(); itTariffs.hasNext(); ) {
				Tariff tariff = (Tariff)itTariffs.next();

				for (Iterator itCurrencies = currencies.iterator(); itCurrencies.hasNext(); ) {
					Currency currency = (Currency)itCurrencies.next();

					createPriceCell(customerGroup, tariff, currency);
				} // iterate Currency
			} // iterate Tariff
		} // iterate CustomerGroup
	}

	@Override
	public boolean addCustomerGroup(CustomerGroup customerGroup)
	{
		boolean res = super.addCustomerGroup(customerGroup);
		if (customerGroup != null)
			createPriceCells(customerGroup, null, null);
		return res;
	}

	@Override
	public boolean addTariff(Tariff tariff)
	{
		boolean res = super.addTariff(tariff);
		if (tariff != null)
			createPriceCells(null, tariff, null);
		return res;
	}

	@Override
	public boolean addCurrency(Currency currency)
	{
		boolean res = super.addCurrency(currency);
		if (currency != null)
			createPriceCells(null, null, currency);
		return res;
	}

	@Override
	public CustomerGroup removeCustomerGroup(String organisationID,
			String customerGroupID)
	{
		CustomerGroup cg = super.removeCustomerGroup(organisationID, customerGroupID);
		if (cg != null)
			removePriceCells(cg, null, null);
		return cg;
	}

	@Override
	public Tariff removeTariff(String organisationID, String tariffID)
	{
		Tariff t = super.removeTariff(organisationID, tariffID);
		if (t != null)
			removePriceCells(null, t, null);
		return t;
	}

	@Override
	public Currency removeCurrency(String currencyID)
	{
		Currency c = super.removeCurrency(currencyID);
		if (c != null)
			removePriceCells(null, null, c);
		return c;
	}

	/**
	 * If one of the parameters is not <tt>null</tt>, all
	 * <p>
	 * <b>Important</b>: The current behaviour is that only exactly zero or one parameter
	 * can be defined, the others must be <tt>null</tt>. This might be changed later. 
	 *
	 * @param _customerGroup
	 * @param _saleMode
	 * @param _tariff
	 * @param _categorySet
	 * @param _currency
	 */
	protected void removePriceCells(
			CustomerGroup _customerGroup, Tariff _tariff,
			Currency _currency)
	{
		int paramCount = 0;
		if (_customerGroup != null) ++paramCount;
		if (_tariff != null) ++paramCount;
		if (_currency != null) ++paramCount;
		if (paramCount != 1)
			throw new IllegalArgumentException("Less or more than one parameter defined!");

// methods like removeCurrency(String) remove first from the map and then call this method - therefore, containsCurrency(...) or similar methods return always false.
//		if (_customerGroup != null && !containsCustomerGroup(_customerGroup))
//			throw new IllegalArgumentException("Given CustomerGroup is not a registered parameter!");
//		if (_tariff != null && !containsTariff(_tariff))
//			throw new IllegalArgumentException("Given Tariff is not a registered parameter!");
//		if (_currency != null && !containsCurrency(_currency))
//			throw new IllegalArgumentException("Given Currency is not a registered parameter!");

		Collection customerGroups = _customerGroup == null ? getCustomerGroups() : new SingleObjectList(_customerGroup);
		Collection tariffs = _tariff == null ? getTariffs() : new SingleObjectList(_tariff);
		Collection currencies = _currency == null ? getCurrencies() : new SingleObjectList(_currency);

		for (Iterator itCustomerGroups = customerGroups.iterator(); itCustomerGroups.hasNext(); ) {
			CustomerGroup customerGroup = (CustomerGroup)itCustomerGroups.next();

			for (Iterator itTariffs = tariffs.iterator(); itTariffs.hasNext(); ) {
				Tariff tariff = (Tariff)itTariffs.next();
				
				for (Iterator itCurrencies = currencies.iterator(); itCurrencies.hasNext(); ) {
					Currency currency = (Currency)itCurrencies.next();
					
					removePriceCell(customerGroup, tariff, currency);
				} // iterate Currency
			} // iterate Tariff
		} // iterate CustomerGroup
	}

	@Override
	public ArticlePrice createArticlePrice(Article article)
	{
		CustomerGroup customerGroup = getCustomerGroup(article);
		Tariff tariff = getTariff(article);
		Currency currency = article.getCurrency();

		PriceCell priceCell = getPriceCell(customerGroup, tariff, currency, true);
		return PriceConfigUtil.createArticlePrice(this, article, priceCell.getPrice());
	}

	public void fillArticlePrice(Article article)
	{
		PriceConfigUtil.fillArticlePrice(this, article);
	}

	public ArticlePrice createNestedArticlePrice(
			IPackagePriceConfig packagePriceConfig, Article article,
			LinkedList<IPriceConfig> priceConfigStack, ArticlePrice topLevelArticlePrice,
			ArticlePrice nextLevelArticlePrice, LinkedList<ArticlePrice> articlePriceStack,
			NestedProductTypeLocal nestedProductTypeLocal, LinkedList<NestedProductTypeLocal> nestedProductTypeStack)
	{
//	 TODO implement
		throw new UnsupportedOperationException("NYI");
	}

	public ArticlePrice createNestedArticlePrice(
			IPackagePriceConfig packagePriceConfig, Article article,
			LinkedList<IPriceConfig> priceConfigStack, ArticlePrice topLevelArticlePrice,
			ArticlePrice nextLevelArticlePrice, LinkedList<ArticlePrice> articlePriceStack,
			NestedProductTypeLocal nestedProductTypeLocal, LinkedList<NestedProductTypeLocal> nestedProductTypeStack, Product nestedProduct, LinkedList<Product> productStack)
	{
		CustomerGroup customerGroup = getCustomerGroup(article);
		Tariff tariff = getTariff(article);
		Currency currency = article.getCurrency();

		PriceCell priceCell = getPriceCell(customerGroup, tariff, currency, true);

		return PriceConfigUtil.createNestedArticlePrice(
				packagePriceConfig,
				this,
				article, priceConfigStack,
				topLevelArticlePrice, nextLevelArticlePrice,
				articlePriceStack,
				nestedProductTypeLocal,
				nestedProductTypeStack,
				nestedProduct,
				productStack,
				priceCell.getPrice());
	}

}
