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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.jfire.trade.CustomerGroup;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Value;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfig"
 *		detachable="true"
 *		table="JFireTrade_FormulaPriceConfig"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.fetch-group name="FormulaPriceConfig.fallbackFormulaCell" fields="fallbackFormulaCell"
 * @jdo.fetch-group name="FormulaPriceConfig.formulaCells" fields="formulaCells"
 * @jdo.fetch-group name="FormulaPriceConfig.packagingResultPriceConfigs" fields="packagingResultPriceConfigs"
 * @jdo.fetch-group name="FormulaPriceConfig.productTypes" fields="productTypes"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="fallbackFormulaCell, formulaCells, packagingResultPriceConfigs, productTypes"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_FormulaPriceConfig")
@FetchGroups({
	@FetchGroup(
		name=FormulaPriceConfig.FETCH_GROUP_FALLBACK_FORMULA_CELL,
		members=@Persistent(name="fallbackFormulaCell")),
	@FetchGroup(
		name=FormulaPriceConfig.FETCH_GROUP_FORMULA_CELLS,
		members=@Persistent(name="formulaCells")),
	@FetchGroup(
		name=FormulaPriceConfig.FETCH_GROUP_PACKAGING_RESULT_PRICE_CONFIGS,
		members=@Persistent(name="packagingResultPriceConfigs")),
	@FetchGroup(
		name=FormulaPriceConfig.FETCH_GROUP_PRODUCT_TYPES,
		members=@Persistent(name="productTypes")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsPriceConfig.edit",
		members={@Persistent(name="fallbackFormulaCell"), @Persistent(name="formulaCells"), @Persistent(name="packagingResultPriceConfigs"), @Persistent(name="productTypes")})
})
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class FormulaPriceConfig
extends GridPriceConfig
implements IFormulaPriceConfig
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_FALLBACK_FORMULA_CELL = "FormulaPriceConfig.fallbackFormulaCell";
	public static final String FETCH_GROUP_FORMULA_CELLS = "FormulaPriceConfig.formulaCells";
	public static final String FETCH_GROUP_PACKAGING_RESULT_PRICE_CONFIGS = "FormulaPriceConfig.packagingResultPriceConfigs";
	public static final String FETCH_GROUP_PRODUCT_TYPES = "FormulaPriceConfig.productTypes";

	/**
	 * key: String innerProductPK_packageProductPK<br/>
	 * value: StablePriceConfig stablePriceConfig
	 * 
	 * @see #getPackagingResultPriceConfig(String, String, boolean)
	 * @see #setPackagingResultPriceConfig(String, String, IPriceConfig)
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="GridPriceConfig"
	 *		dependent-value="true"
	 *		table="JFireTrade_FormulaPriceConfig_packagingResultPriceConfigs"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_FormulaPriceConfig_packagingResultPriceConfigs",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Value(dependent="true")
	private Map<String, GridPriceConfig> packagingResultPriceConfigs = new HashMap<String, GridPriceConfig>();

	public void setPackagingResultPriceConfig(String innerProductTypePK, String packageProductTypePK, IPriceConfig resultPriceConfig)
	{
		if (!(resultPriceConfig instanceof GridPriceConfig))
			throw new IllegalArgumentException("This implementation of IInnerPriceConfig only supports instances of type " + GridPriceConfig.class.getName() + " as resultPriceConfig, but you passed an instance of " + (resultPriceConfig == null ? null : resultPriceConfig.getClass().getName()));

		packagingResultPriceConfigs.put(innerProductTypePK+'-'+packageProductTypePK, (GridPriceConfig)resultPriceConfig);
	}
	public IPriceConfig getPackagingResultPriceConfig(String innerProductTypePK, String packageProductTypePK, boolean throwExceptionIfNotExistent)
	{
		IPriceConfig res = packagingResultPriceConfigs.get(innerProductTypePK+'-'+packageProductTypePK);
		if (throwExceptionIfNotExistent && res == null)
			throw new IllegalArgumentException("There is no PriceConfig registered as the result of the combination of the innerProductType \""+innerProductTypePK+"\" packaged in the packageProductType \""+packageProductTypePK+"\"!");
		return res;
	}
	protected void clearPackagingResultPriceConfigs()
	{
		packagingResultPriceConfigs.clear();
	}

//	/**
//	 * key: PriceCoordinate priceCoordinate<br/>
//	 * value: FormulaCell formulaCell
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="PriceCoordinate"
//	 *		value-type="FormulaCell"
//	 *		dependent-key="true"
//	 *		dependent-value="true"
//	 *		mapped-by="mapOwner"
//	 *
//	 * @jdo.key mapped-by="priceCoordinate"
//	 */
//	private Map<IPriceCoordinate, FormulaCell> formulaCells;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="FormulaCell"
	 *		dependent-element="true"
	 *		mapped-by="mapOwner"
	 */
@Persistent(
	dependentElement="true",
	mappedBy="mapOwner",
	persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<FormulaCell> formulaCells;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Map<IPriceCoordinate, FormulaCell> priceCoordinate2formulaCell;

	protected Map<IPriceCoordinate, FormulaCell> getPriceCoordinate2formulaCell() {
		if (priceCoordinate2formulaCell == null) {
			Map<IPriceCoordinate, FormulaCell> m = new HashMap<IPriceCoordinate, FormulaCell>();
			for (FormulaCell formulaCell : formulaCells)
				m.put(formulaCell.getPriceCoordinate(), formulaCell);

			priceCoordinate2formulaCell = m;
		}

		return priceCoordinate2formulaCell;
	}

	/**
	 * In case, there is a cell missing for a certain coordinate, this
	 * fallbackFormulaCell is used - if it is assigned. It may be <tt>null</tt>.
	 * For some configurations - e.g. a system fee - where there is only one formula
	 * necessary for all cells, this is the only you'll need to define.
	 *
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	protected FormulaCell fallbackFormulaCell;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected FormulaPriceConfig()
	{
	}
	/**
	 * @param organisationID
	 * @param priceConfigID
	 */
	public FormulaPriceConfig(String organisationID, String priceConfigID)
	{
		super(organisationID, priceConfigID);
//		formulaCells = new HashMap<IPriceCoordinate, FormulaCell>();
		formulaCells = new HashSet<FormulaCell>();
	}

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private long nextFormulaID = 0;
//
//	/**
//	 * Creates a formulaID by incrementing the member nextFormulaID. The new ID is unique within
//	 * the context of this FormulaPriceConfig (organisationID & priceConfigID).
//	 */
//	public synchronized long createFormulaID()
//	{
//		long res = nextFormulaID;
//		nextFormulaID = res + 1;
//		return res;
//	}

	/**
	 * @see org.nightlabs.jfire.accounting.priceconfig.PriceConfig#requiresProductTypePackageInternal()
	 */
	@Override
	public boolean requiresProductTypePackageInternal()
	{
		return true;
	}
	/**
	 * @see org.nightlabs.jfire.accounting.priceconfig.PriceConfig#createArticlePrice(Article)
	 */
	@Override
	public ArticlePrice createArticlePrice(Article article)
	{
		throw new UnsupportedOperationException("This implementation of PriceConfig cannot deliver a price for an Article! It is only intended to be used internally within a package and not to be sold directly!");
	}
	
	/**
	 * Additionally to the parameters within a price config, we need - for our formulas -
	 * to be able to address other price configs which are assigned to other
	 * <tt>ProductType</tt> s (nature TYPE) from which we want to be dependent. Therefore, here
	 * are all other <tt>ProductType</tt> s registered that should be available within
	 * the formulas.
	 * <p>
	 * The lookup in the formula for an external cell works like that: All sibling
	 * <tt>ProductType</tt> s are checked whether they are or inherit the desired one.
	 * If they do, the cell's value is fetched (and before calculated if necessary). All
	 * matching values are summarized. This means if there are multiple matches, the
	 * returned value is the sum of all and if none is found, the result is 0.
	 * <p>
	 * key: String productTypePK<br/>
	 * value: ProductType productType
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="ProductType"
	 *		table="JFireTrade_FormulaPriceConfig_productTypes"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_FormulaPriceConfig_productTypes",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, ProductType> productTypes = new HashMap<String, ProductType>();

	public Collection<ProductType> getProductTypes()
	{
		return Collections.unmodifiableCollection(productTypes.values());
	}
	public void addProductType(ProductType productType)
	{
		productTypes.put(productType.getPrimaryKey(), productType);
	}
	public ProductType getProductType(String organisationID, String productTypeID, boolean throwExceptionIfNotExsistent)
	{
		ProductType product = productTypes.get(ProductType.getPrimaryKey(organisationID, productTypeID));
		if (product == null && throwExceptionIfNotExsistent)
			throw new IllegalArgumentException("No ProductType registered with organisationID=\""+organisationID+"\" productTypeID=\""+productTypeID+"\"!");
		return product;
	}

	public FormulaCell createFormulaCell(
			CustomerGroup customerGroup, Tariff tariff, Currency currency)
	{
		PriceCoordinate priceCoordinate = new PriceCoordinate(
				this, customerGroup, tariff, currency);
		return createFormulaCell(priceCoordinate);
	}

	public FormulaCell createFormulaCell(IPriceCoordinate priceCoordinate)
	{
		if (priceCoordinate.getPriceConfig() == null ||
				!priceCoordinate.getPriceConfig().getPrimaryKey().equals(this.getPrimaryKey()))
			priceCoordinate = new PriceCoordinate(this, priceCoordinate);

		FormulaCell formulaCell = getFormulaCell(priceCoordinate, false);
		if (formulaCell == null) {
			formulaCell = new FormulaCell((PriceCoordinate)priceCoordinate);
//			formulaCells.put(priceCoordinate, formulaCell);
			putFormulaCell(formulaCell);
		}
		return formulaCell;
	}

	protected void putFormulaCell(FormulaCell formulaCell)
	{
		IPriceCoordinate priceCoordinate = formulaCell.getPriceCoordinate();
		priceCoordinate.assertAllDimensionValuesAssigned();
		if (formulaCell == null)
			throw new IllegalArgumentException("formulaCell must not be null!");

		formulaCell.setMapOwner(this);
//		formulaCells.put(priceCoordinate, formulaCell);

		FormulaCell oldFormulaCell = getPriceCoordinate2formulaCell().get(priceCoordinate);
		if (oldFormulaCell != null && !oldFormulaCell.equals(formulaCell))
			formulaCells.remove(oldFormulaCell);

		formulaCells.add(formulaCell);
		priceCoordinate2formulaCell.put(priceCoordinate, formulaCell);
	}

	public FormulaCell getFormulaCell(
			CustomerGroup customerGroup,
			Tariff tariff, Currency currency,
			boolean throwExceptionIfNotExistent)
	{
		PriceCoordinate priceCoordinate = new PriceCoordinate(
				this, customerGroup,
				tariff, currency);
		return getFormulaCell(priceCoordinate, throwExceptionIfNotExistent);
	}
	@Override
	public FormulaCell getFormulaCell(IPriceCoordinate priceCoordinate, boolean throwExceptionIfNotExistent)
	{
		FormulaCell formulaCell = getPriceCoordinate2formulaCell().get(priceCoordinate);

//		// If the JDO implementation uses a shortcut (a direct JDOQL instead of loading the whole Map and then
//		// searching for the key), the cell might exist and not be found. Hence, we load the whole Map and try it again.
//		if (formulaCell == null) {
//			for (Map.Entry<IPriceCoordinate, FormulaCell> me : formulaCells.entrySet()) {
//				if (me.getKey().equals(priceCoordinate)) {
//					formulaCell = (FormulaCell) me.getValue();
//					break;
//				}
//			}
//		}

		if (throwExceptionIfNotExistent && formulaCell == null)
			throw new IllegalArgumentException("No FormulaCell found for "+priceCoordinate);
		return formulaCell;
	}

	protected void removeFormulaCell(
			CustomerGroup customerGroup,
			Tariff tariff, Currency currency)
	{
		PriceCoordinate priceCoordinate = new PriceCoordinate(
				this, customerGroup,
				tariff, currency);
		removeFormulaCell(priceCoordinate);
	}
	/**
	 * @see org.nightlabs.jfire.accounting.gridpriceconfig.IFormulaPriceConfig#getFallbackFormulaCell(boolean)
	 */
	@Override
	public FormulaCell getFallbackFormulaCell(boolean throwExceptionIfNotExistent)
	{
		if (throwExceptionIfNotExistent && fallbackFormulaCell == null)
			throw new NullPointerException("There is no fallbackFormulaCell defined!");
		return fallbackFormulaCell;
	}
	@Override
	public FormulaCell createFallbackFormulaCell()
	{
		if (fallbackFormulaCell == null) {
			fallbackFormulaCell = new FormulaCell(this);
		}
		return fallbackFormulaCell;
	}

	protected static boolean isWhiteSpace(char c)
	{
		return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == '\f';
	}

	protected static String prepareFormula(String formula)
	{
		if (formula != null) {
			// TODO can this be done with a regular expression?
			StringBuffer sb = new StringBuffer(formula);
			while (sb.length() > 0 && isWhiteSpace(sb.charAt(sb.length() - 1)))
				sb.deleteCharAt(sb.length() - 1);
			formula = sb.toString();
		}

		if ("".equals(formula))
			formula = null;

		return formula;
	}

	@Override
	public void setFallbackFormula(PriceFragmentType priceFragmentType, String formula)
	{
		formula = prepareFormula(formula);

		if (formula == null) {
			FormulaCell formulaCell = getFallbackFormulaCell(false);
			if (formulaCell != null) {
				formulaCell.setFormula(priceFragmentType, formula);
				if (formulaCell.isEmpty())
					removeFallbackFormulaCell();
			}
		} // if (formula == null) {
		else {
			FormulaCell formulaCell = createFallbackFormulaCell();
			formulaCell.setFormula(priceFragmentType, formula);
		}
	}

	/**
	 * Sets a formula for a certain <tt>PriceFragmentType</tt> within a <tt>FormulaCell</tt>.
	 * If the formula is empty, it gets deleted. If the <tt>FormulaCell</tt> becomes empty for all
	 * <tt>PriceFragmentType</tt> s, the whole cell gets deleted and in the calculation, the fallback
	 * cell will be used. If the addressed <tt>FormulaCell</tt> does not exist and the formula is
	 * not empty (<tt>null</tt> or empty string or only whitespaces), the cell is automatically
	 * created.
	 *
	 * @param priceCoordinate Must not be <tt>null</tt>.
	 * @param priceFragmentType Must not be <tt>null</tt>.
	 * @param formula Can be <tt>null</tt>.
	 */
	@Override
	public void setFormula(IPriceCoordinate priceCoordinate, PriceFragmentType priceFragmentType, String formula)
	{
		formula = prepareFormula(formula);

		if (formula == null) {
			FormulaCell formulaCell = getFormulaCell(priceCoordinate, false);
			if (formulaCell != null) {
				formulaCell.setFormula(priceFragmentType, formula);
				if (formulaCell.isEmpty())
					removeFormulaCell(priceCoordinate);
			}
		} // if (formula == null) {
		else {
			FormulaCell formulaCell = createFormulaCell(priceCoordinate);
			formulaCell.setFormula(priceFragmentType, formula);
		}
	}

	public void removeFallbackFormulaCell()
	{
		this.fallbackFormulaCell = null; // TODO check, whether the persistent instance really disappears.
	}

	protected void removeFormulaCell(IPriceCoordinate priceCoordinate)
	{
//		formulaCells.remove(priceCoordinate);
		FormulaCell oldFormulaCell = getPriceCoordinate2formulaCell().remove(priceCoordinate);
		if (oldFormulaCell != null)
			formulaCells.remove(oldFormulaCell);
	}

//	/**
//	 * @see org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfig#getPriceCell(org.nightlabs.jfire.accounting.gridpriceconfig.PriceCoordinate, boolean)
//	 */
//	public PriceCell getPriceCell(PriceCoordinate priceCoordinate, boolean throwExceptionIfNotExistent)
//	{
//		throw new UnsupportedOperationException("The FormulaPriceConfig does not have PriceCells!");
//	}

//	/**
//	 * @see org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfig#adoptParameters(org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfig)
//	 */
//	public void adoptParameters(GridPriceConfig other)
//	{
//		super.adoptParameters(other);
//		// TODO This is expensive! Is it really necessary? Can we
//		for (Iterator it = packagingResultPriceConfigs.values().iterator(); it.)
//	}
	
	/**
	 * @see org.nightlabs.jfire.accounting.priceconfig.IPriceConfig#createNestedArticlePrice(IPackagePriceConfig, org.nightlabs.jfire.trade.Article, LinkedList, org.nightlabs.jfire.trade.ArticlePrice, org.nightlabs.jfire.trade.ArticlePrice, LinkedList, org.nightlabs.jfire.store.NestedProductTypeLocal, LinkedList)
	 */
	@Override
	public ArticlePrice createNestedArticlePrice(
			IPackagePriceConfig packagePriceConfig, Article article,
			LinkedList<IPriceConfig> priceConfigStack, ArticlePrice topLevelArticlePrice,
			ArticlePrice nextLevelArticlePrice, LinkedList<ArticlePrice> articlePriceStack,
			NestedProductTypeLocal nestedProductTypeLocal, LinkedList<NestedProductTypeLocal> nestedProductTypeStack)
	{
		ProductType packageProductType = nestedProductTypeLocal.getPackageProductTypeLocal().getProductType();
		ProductType innerProductType = nestedProductTypeLocal.getInnerProductTypeLocal().getProductType();
		StablePriceConfig packagingResultPriceConfig = (StablePriceConfig) getPackagingResultPriceConfig(
				innerProductType.getPrimaryKey(),
				packageProductType.getPrimaryKey(), true);

		return packagingResultPriceConfig.createNestedArticlePrice(
				packagePriceConfig, article,
				priceConfigStack, topLevelArticlePrice,
				nextLevelArticlePrice, articlePriceStack,
				nestedProductTypeLocal, nestedProductTypeStack);
	}

	/**
	 * @see org.nightlabs.jfire.accounting.priceconfig.IPriceConfig#createNestedArticlePrice(IPackagePriceConfig, org.nightlabs.jfire.trade.Article, LinkedList, org.nightlabs.jfire.trade.ArticlePrice, org.nightlabs.jfire.trade.ArticlePrice, LinkedList, NestedProductTypeLocal, LinkedList, org.nightlabs.jfire.store.Product, LinkedList)
	 */
	@Override
	public ArticlePrice createNestedArticlePrice(
			IPackagePriceConfig packagePriceConfig, Article article,
			LinkedList<IPriceConfig> priceConfigStack, ArticlePrice topLevelArticlePrice,
			ArticlePrice nextLevelArticlePrice, LinkedList<ArticlePrice> articlePriceStack,
			NestedProductTypeLocal nestedProductTypeLocal, LinkedList<NestedProductTypeLocal> nestedProductTypeStack,
			Product nestedProduct, LinkedList<Product> productStack)
	{
		ProductType packageProductType = nestedProductTypeLocal.getPackageProductTypeLocal().getProductType();
		ProductType innerProductType = nestedProductTypeLocal.getInnerProductTypeLocal().getProductType();
		IPriceConfig packagingResultPriceConfig = getPackagingResultPriceConfig(
				innerProductType.getPrimaryKey(),
				packageProductType.getPrimaryKey(), true);

		return packagingResultPriceConfig.createNestedArticlePrice(
				packagePriceConfig, article,
				priceConfigStack, topLevelArticlePrice,
				nextLevelArticlePrice, articlePriceStack,
				nestedProductTypeLocal, nestedProductTypeStack,
				nestedProduct, productStack);

//		PersistenceManager pm = getPersistenceManager();
//
//		CustomerGroup customerGroup = getCustomerGroup(article);
//		Tariff tariff = article.getTariff();
//		Currency currency = article.getCurrency();
//
//		ProductType packageProductType = nestedProductTypeLocal.getPackageProductType();
//		ProductType innerProductType = nestedProductTypeLocal.getInnerProductType();
//		StablePriceConfig packagingResultPriceConfig = (StablePriceConfig) getPackagingResultPriceConfig(
//				innerProductType.getPrimaryKey(),
//				packageProductType.getPrimaryKey(), true);
//
//		PriceCell priceCell = packagingResultPriceConfig.getPriceCell(customerGroup, tariff, currency, true);
//
//		return PriceConfigUtil.createNestedArticlePrice(
//				packagePriceConfig,
//				packagingResultPriceConfig,
//				article, priceConfigStack,
//				topLevelArticlePrice, nextLevelArticlePrice,
//				articlePriceStack,
//				nestedProductTypeLocal,
//				nestedProductTypeStack,
//				nestedProduct,
//				productStack,
//				priceCell.getPrice());
	}

}
