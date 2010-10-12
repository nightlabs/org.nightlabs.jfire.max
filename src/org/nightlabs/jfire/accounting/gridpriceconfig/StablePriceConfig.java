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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfigUtil;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
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
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_StablePriceConfig")
@FetchGroups({
	@FetchGroup(
		name=StablePriceConfig.FETCH_GROUP_PRICE_CELLS,
		members=@Persistent(name="priceCells")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsPriceConfig.edit",
		members=@Persistent(name="priceCells"))
})
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class StablePriceConfig
extends GridPriceConfig
implements IPackagePriceConfig, IResultPriceConfig
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(StablePriceConfig.class);

	public static final String FETCH_GROUP_PRICE_CELLS = "StablePriceConfig.priceCells";

	/**
	 * @see #getPriceCells(String, String)
	 */
	public Collection<PriceCell> getPriceCells(final CustomerGroup customerGroup, final Currency currency)
	{
		return getPriceCells(customerGroup.getPrimaryKey(), currency.getCurrencyID());
	}
	/**
	 * @return a <tt>Collection</tt> of {@link PriceCell}.
	 */
	@SuppressWarnings("unchecked")
	public Collection<PriceCell> getPriceCells(final String customerGroupPK, final String currencyID)
	{
		final PersistenceManager pm = getPersistenceManager();
		//Query query = pm.newNamedQuery(StablePriceConfig.class, "getPriceCellsForCustomerGroupAndCurrency");
		final Query query = pm.newNamedQuery(PriceCell.class, "getPriceCellsForCustomerGroupAndCurrency");
		return (Collection<PriceCell>) query.execute(this, customerGroupPK, currencyID);
	}

//	/**
//	 * key: PriceCoordinate priceCoordinate<br/>
//	 * value: PriceCell priceCell
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="PriceCoordinate"
//	 *		value-type="PriceCell"
//	 *		mapped-by="priceConfig"
//	 *		dependent-key="true"
//	 *		dependent-value="true"
//	 *
//	 * @jdo.key mapped-by="priceCoordinate"
//	 */
//	private Map<IPriceCoordinate, PriceCell> priceCells;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="PriceCell"
	 *		mapped-by="priceConfig"
	 *		dependent-element="true"
	 */
	@Persistent(
			dependentElement="true",
			mappedBy="priceConfig",
			persistenceModifier=PersistenceModifier.PERSISTENT
	)
	private Set<PriceCell> priceCells;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Map<IPriceCoordinate, PriceCell> priceCoordinate2priceCell;

	protected Map<IPriceCoordinate, PriceCell> getPriceCoordinate2priceCell()
	{
		if (priceCoordinate2priceCell == null) {
			final Map<IPriceCoordinate, PriceCell> m = new HashMap<IPriceCoordinate, PriceCell>();
			for (final PriceCell priceCell : priceCells)
				m.put(priceCell.getPriceCoordinate(), priceCell);

			priceCoordinate2priceCell = m;
		}
		return priceCoordinate2priceCell;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected StablePriceConfig() { }

	/**
	 * @param organisationID
	 * @param priceConfigID
	 */
	public StablePriceConfig(final PriceConfigID priceConfigID)
	{
		super(priceConfigID);
		priceCells = new HashSet<PriceCell>();
	}

	@Override
	public boolean requiresProductTypePackageInternal()
	{
		return false;
	}

	@Override
	public Collection<PriceCell> getPriceCells()
	{
		return Collections.unmodifiableCollection(priceCells);
	}
	/**
	 * This method drops all calculation status for all <tt>PriceFragment</tt> s
	 * in all <tt>PriceCell</tt> s which is equivalent to setting
	 * them to <tt>CALCULATIONSTATUS_DIRTY</tt>.
	 */
	@Override
	public void resetPriceFragmentCalculationStatus()
	{
//		for (Map.Entry<IPriceCoordinate, PriceCell> me : new ArrayList<Map.Entry<IPriceCoordinate, PriceCell>>(priceCells.entrySet())) { // new ArrayList, because we might call putPriceCell(...)
		for (final PriceCell priceCell : new ArrayList<PriceCell>(priceCells)) { // new ArrayList, because we might call putPriceCell(...)
//			PriceCell priceCell = me.getValue();
//			if (priceCell == null) {
//				IPriceCoordinate priceCoordinate = me.getKey();
//				if (priceCoordinate != null) {
//					// TODO DataNucleus WORKAROUND!!! It should never happen that a null value comes into this map in the first place.
//					// We try to find it.
//					logger.warn("resetPriceFragmentCalculationStatus: found entry in priceCells with a key but without a value! key=" + me.getKey() +" this=" + this);
//
//					PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//					if (pm == null)
//						logger.warn("resetPriceFragmentCalculationStatus: this StablePriceConfig is currently not attached to a datastore! Cannot obtain PersistenceManager to find lost object! this=" + this);
//					else {
//						Query q = pm.newQuery(PriceCell.class);
//						q.setFilter("this.priceConfig == :priceConfig");
//						Collection<PriceCell> c = CollectionUtil.castCollection((Collection<?>)q.execute(this));
//						for (PriceCell pc : c) {
//							if (priceCoordinate.equals(pc.getPriceCoordinate())) {
//								priceCell = pc;
//								break;
//							}
//						}
//
//						if (priceCell == null)
//							logger.warn("resetPriceFragmentCalculationStatus: could not find lost PriceCell via JDO query! this=" + this + " priceCoordinate=" + priceCoordinate);
//						else {
//							putPriceCell(priceCoordinate, priceCell);
//						}
//					}
//				}
//				// DataNucleus WORKAROUND - END
//
//				if (priceCell == null)
//					throw new IllegalStateException("priceCells contains null value for key=" + me.getKey() +" this=" + this);
//			}

			priceCell.resetPriceFragmentCalculationStatus();
		}
	}

	protected PriceCell createPriceCell(
			final CustomerGroup customerGroup,
			final Tariff tariff, final Currency currency)
	{
		final PriceCoordinate priceCoordinate = new PriceCoordinate(
				this, customerGroup,
				tariff, currency);
		return createPriceCell(priceCoordinate);
	}

	public PriceCell getPriceCell(
			final CustomerGroup customerGroup,
			final Tariff tariff, final Currency currency,
			final boolean throwExceptionIfNotExistent)
	{
		final PriceCoordinate priceCoordinate = new PriceCoordinate(
				this, customerGroup,
				tariff, currency);
		return getPriceCell(priceCoordinate, throwExceptionIfNotExistent);
	}

	@Override
	public PriceCell getPriceCell(final IPriceCoordinate priceCoordinate, final boolean throwExceptionIfNotExistent)
	{
		final PriceCell priceCell = getPriceCoordinate2priceCell().get(priceCoordinate);

//		// If the JDO implementation uses a shortcut (a direct JDOQL instead of loading the whole Map and then
//		// search for the key), the cell might exist and not be found. Hence, we load the whole Map and try it again.
//		if (priceCell == null) {
//			for (Iterator<Map.Entry<IPriceCoordinate, PriceCell>> it = priceCells.entrySet().iterator(); it.hasNext();) {
//				Map.Entry<IPriceCoordinate, PriceCell> me = it.next();
//				if (me.getKey().equals(priceCoordinate)) {
//					priceCell = (PriceCell) me.getValue();
//					break;
//				}
//			}
//		}

		if (throwExceptionIfNotExistent && priceCell == null)
			throw new IllegalArgumentException("No PriceCell found for "+priceCoordinate);
		return priceCell;
	}

	@Override
	public PriceCell createPriceCell(IPriceCoordinate priceCoordinate)
	{
		if (priceCoordinate.getPriceConfig() == null ||
				!priceCoordinate.getPriceConfig().getPrimaryKey().equals(this.getPrimaryKey()))
			priceCoordinate = new PriceCoordinate(this, priceCoordinate);

		PriceCell priceCell = getPriceCell(priceCoordinate, false);
		if (priceCell == null) {
			priceCell = new PriceCell(priceCoordinate);
//			priceCells.put(priceCoordinate, priceCell);
			putPriceCell(priceCell);
		}
		return priceCell;
	}

	protected void putPriceCell(final PriceCell priceCell)
	{
		final IPriceCoordinate priceCoordinate = priceCell.getPriceCoordinate();
		priceCoordinate.assertAllDimensionValuesAssigned();
		if (priceCell == null)
			throw new IllegalArgumentException("priceCell must not be null");

		if (logger.isDebugEnabled())
			logger.debug("putPriceCell: priceCoordinate=" + priceCoordinate + " priceCell=" + priceCell);

		final PriceCell oldPriceCell = getPriceCoordinate2priceCell().get(priceCoordinate);
		if (oldPriceCell != null && !oldPriceCell.equals(priceCell))
			priceCells.remove(oldPriceCell);

//		priceCells.put(priceCoordinate, priceCell);
		priceCells.add(priceCell);
		priceCoordinate2priceCell.put(priceCoordinate, priceCell);
	}

	protected void removePriceCell(
			final CustomerGroup customerGroup,
			final Tariff tariff, final Currency currency)
	{
		final PriceCoordinate priceCoordinate = new PriceCoordinate(
				this, customerGroup,
				tariff, currency);
		removePriceCell(priceCoordinate);
	}

	protected void removePriceCell(final PriceCoordinate priceCoordinate)
	{
		if (logger.isDebugEnabled())
			logger.debug("removePriceCell: " + priceCoordinate);

//		priceCells.remove(priceCoordinate);
		final PriceCell oldPriceCell = getPriceCoordinate2priceCell().remove(priceCoordinate);
		if (oldPriceCell != null)
			priceCells.remove(oldPriceCell);
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
			final CustomerGroup _customerGroup, final Tariff _tariff,
			final Currency _currency)
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

		final Collection<CustomerGroup> customerGroups = _customerGroup == null ? getCustomerGroups() : Collections.singleton(_customerGroup);
		final Collection<Tariff> tariffs = _tariff == null ? getTariffs() : Collections.singleton(_tariff);
		final Collection<Currency> currencies = _currency == null ? getCurrencies() : Collections.singleton(_currency);

		for (final Iterator<CustomerGroup> itCustomerGroups = customerGroups.iterator(); itCustomerGroups.hasNext(); ) {
			final CustomerGroup customerGroup = itCustomerGroups.next();

			for (final Iterator<Tariff> itTariffs = tariffs.iterator(); itTariffs.hasNext(); ) {
				final Tariff tariff = itTariffs.next();

				for (final Iterator<Currency> itCurrencies = currencies.iterator(); itCurrencies.hasNext(); ) {
					final Currency currency = itCurrencies.next();

					createPriceCell(customerGroup, tariff, currency);
				} // iterate Currency
			} // iterate Tariff
		} // iterate CustomerGroup
	}

	@Override
	public boolean addCustomerGroup(final CustomerGroup customerGroup)
	{
		final boolean res = super.addCustomerGroup(customerGroup);
		if (res)
			createPriceCells(customerGroup, null, null);
		return res;
	}

	@Override
	public boolean addTariff(final Tariff tariff)
	{
		final boolean res = super.addTariff(tariff);
		if (res)
			createPriceCells(null, tariff, null);
		return res;
	}

	@Override
	public boolean addCurrency(final Currency currency)
	{
		final boolean res = super.addCurrency(currency);
		if (res)
			createPriceCells(null, null, currency);
		return res;
	}

	@Override
	public CustomerGroup removeCustomerGroup(final String organisationID,
			final String customerGroupID)
	{
		final CustomerGroup cg = super.removeCustomerGroup(organisationID, customerGroupID);
		if (cg != null)
			removePriceCells(cg, null, null);
		return cg;
	}

	@Override
	public Tariff removeTariff(final String organisationID, final String tariffID)
	{
		final Tariff t = super.removeTariff(organisationID, tariffID);
		if (t != null)
			removePriceCells(null, t, null);
		return t;
	}

	@Override
	public Currency removeCurrency(final String currencyID)
	{
		final Currency c = super.removeCurrency(currencyID);
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
			final CustomerGroup _customerGroup, final Tariff _tariff,
			final Currency _currency)
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

		final Collection<CustomerGroup> customerGroups = _customerGroup == null ? getCustomerGroups() : Collections.singleton(_customerGroup);
		final Collection<Tariff> tariffs = _tariff == null ? getTariffs() : Collections.singleton(_tariff);
		final Collection<Currency> currencies = _currency == null ? getCurrencies() : Collections.singleton(_currency);

		for (final Iterator<CustomerGroup> itCustomerGroups = customerGroups.iterator(); itCustomerGroups.hasNext(); ) {
			final CustomerGroup customerGroup = itCustomerGroups.next();

			for (final Iterator<Tariff> itTariffs = tariffs.iterator(); itTariffs.hasNext(); ) {
				final Tariff tariff = itTariffs.next();

				for (final Iterator<Currency> itCurrencies = currencies.iterator(); itCurrencies.hasNext(); ) {
					final Currency currency = itCurrencies.next();

					removePriceCell(customerGroup, tariff, currency);
				} // iterate Currency
			} // iterate Tariff
		} // iterate CustomerGroup
	}

	@Override
	public ArticlePrice createArticlePrice(final Article article)
	{
		final CustomerGroup customerGroup = getCustomerGroup(article);
		final Tariff tariff = getTariff(article);
		final Currency currency = article.getCurrency();

		final PriceCell priceCell = getPriceCell(customerGroup, tariff, currency, true);
		return PriceConfigUtil.createArticlePrice(this, article, priceCell.getPrice());
	}

	public void fillArticlePrice(final Article article)
	{
		PriceConfigUtil.fillArticlePrice(this, article);
	}

	public ArticlePrice createNestedArticlePrice(
			final IPackagePriceConfig packagePriceConfig, final Article article,
			final LinkedList<IPriceConfig> priceConfigStack, final ArticlePrice topLevelArticlePrice,
			final ArticlePrice nextLevelArticlePrice, final LinkedList<ArticlePrice> articlePriceStack,
			final NestedProductTypeLocal nestedProductTypeLocal, final LinkedList<NestedProductTypeLocal> nestedProductTypeStack)
	{
		final CustomerGroup customerGroup = getCustomerGroup(article);
		final Tariff tariff = getTariff(article);
		final Currency currency = article.getCurrency();

		final PriceCell priceCell = getPriceCell(customerGroup, tariff, currency, true);

		return PriceConfigUtil.createNestedArticlePrice(
				packagePriceConfig,
				this,
				article, priceConfigStack,
				topLevelArticlePrice, nextLevelArticlePrice,
				articlePriceStack,
				nestedProductTypeLocal,
				nestedProductTypeStack,
				priceCell.getPrice());
	}

	public ArticlePrice createNestedArticlePrice(
			final IPackagePriceConfig packagePriceConfig, final Article article,
			final LinkedList<IPriceConfig> priceConfigStack, final ArticlePrice topLevelArticlePrice,
			final ArticlePrice nextLevelArticlePrice, final LinkedList<ArticlePrice> articlePriceStack,
			final NestedProductTypeLocal nestedProductTypeLocal, final LinkedList<NestedProductTypeLocal> nestedProductTypeStack, final Product nestedProduct, final LinkedList<Product> productStack)
	{
		final CustomerGroup customerGroup = getCustomerGroup(article);
		final Tariff tariff = getTariff(article);
		final Currency currency = article.getCurrency();

		final PriceCell priceCell = getPriceCell(customerGroup, tariff, currency, true);

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
