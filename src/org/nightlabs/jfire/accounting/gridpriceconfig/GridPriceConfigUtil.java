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

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.priceconfig.AffectedProductType;
import org.nightlabs.jfire.accounting.priceconfig.FetchGroupsPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfigUtil;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.CustomerGroup;

/**
 * @!ejb.bean name="jfire/ejb/JFireTrade/GridPriceConfigManager"	
 *           jndi-name="jfire/ejb/JFireTrade/GridPriceConfigManager"
 *           type="Stateless" 
 *           transaction-type="Container"
 *
 * @!ejb.util generate = "physical"
 */
public class GridPriceConfigUtil
//extends BaseSessionBeanImpl
//implements SessionBean
{
	private static final Logger logger = Logger.getLogger(GridPriceConfigUtil.class);

	protected GridPriceConfigUtil() { }

//	/**
//	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
//	 */
//	public void setSessionContext(SessionContext sessionContext)
//			throws EJBException, RemoteException
//	{
//		logger.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
//		super.setSessionContext(sessionContext);
//	}
//	/**
//	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
//	 */
//	public void unsetSessionContext() {
//		super.unsetSessionContext();
//	}
//	/**
//	 * @ejb.create-method  
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public void ejbCreate()
//	throws CreateException
//	{
//		logger.debug(this.getClass().getName() + ".ejbCreate()");
//	}
//	/**
//	 * @see javax.ejb.SessionBean#ejbRemove()
//	 * 
//	 * @ejb.permission unchecked="true"
//	 */
//	public void ejbRemove() throws EJBException, RemoteException
//	{
//		logger.debug(this.getClass().getName() + ".ejbRemove()");
//	}
//	
//	/**
//	 * @see javax.ejb.SessionBean#ejbActivate()
//	 */
//	public void ejbActivate() throws EJBException, RemoteException
//	{
//		logger.debug(this.getClass().getName() + ".ejbActivate()");
//	}
//	/**
//	 * @see javax.ejb.SessionBean#ejbPassivate()
//	 */
//	public void ejbPassivate() throws EJBException, RemoteException
//	{
//		logger.debug(this.getClass().getName() + ".ejbPassivate()");
//	}

	public static void logGridPriceConfig(GridPriceConfig priceConfig)
	{
		if (!logger.isDebugEnabled())
			return;

		try {
			logger.debug("priceConfig="+priceConfig.getPrimaryKey() + " priceConfig.class=" + priceConfig.getClass().getName());
			for (Currency currency : priceConfig.getCurrencies()) {
				logger.debug("  currency=" + currency.getCurrencyID() + " ("+currency.getCurrencySymbol()+")");
				for (CustomerGroup customerGroup : priceConfig.getCustomerGroups()) {
					logger.debug("    customerGroup=" + customerGroup.getPrimaryKey() + " ("+customerGroup.getName().getText(Locale.ENGLISH.getLanguage())+")");
					for (Tariff tariff : priceConfig.getTariffs()) {
						logger.debug("      tariff=" + tariff.getPrimaryKey() + " ("+tariff.getName().getText(Locale.ENGLISH.getLanguage())+")");
						PriceCoordinate priceCoordinate = new PriceCoordinate(customerGroup.getPrimaryKey(), tariff.getPrimaryKey(), currency.getCurrencyID());
						if (priceConfig instanceof IFormulaPriceConfig) {
							FormulaCell formulaCell = ((IFormulaPriceConfig)priceConfig).getFormulaCell(priceCoordinate, false);
							for (PriceFragmentType priceFragmentType : priceConfig.getPriceFragmentTypes()) {
								String formula = formulaCell == null ? null : formulaCell.getFormula(priceFragmentType);
								logger.debug("        priceFragmentType=" + priceFragmentType.getPrimaryKey() + " ("+priceFragmentType.getName().getText(Locale.ENGLISH.getLanguage())+") formula=" + formula);
							}
						}
						else if (priceConfig instanceof IResultPriceConfig) {
							PriceCell priceCell = ((IResultPriceConfig)priceConfig).getPriceCell(priceCoordinate, false);
							Price price = priceCell == null ? null : priceCell.getPrice();
							for (PriceFragmentType priceFragmentType : priceConfig.getPriceFragmentTypes()) {
								String amount = price == null ? null : String.valueOf(price.getAmount(priceFragmentType));
								logger.debug("        priceFragmentType=" + priceFragmentType.getPrimaryKey() + " ("+priceFragmentType.getName().getText(Locale.ENGLISH.getLanguage())+") amount=" + amount);
							}
						}
					}
				}
			}
			
			if (priceConfig instanceof IFormulaPriceConfig) {
				FormulaCell formulaCell = ((IFormulaPriceConfig)priceConfig).getFallbackFormulaCell(false);
				if (formulaCell == null)
					logger.debug("  fallbackFormulaCell is null");
				else {
					logger.debug("  fallbackFormulaCell:");
					for (PriceFragmentType priceFragmentType : priceConfig.getPriceFragmentTypes()) {
						String formula = formulaCell == null ? null : formulaCell.getFormula(priceFragmentType);
						logger.debug("    priceFragmentType=" + priceFragmentType.getPrimaryKey() + " ("+priceFragmentType.getName().getText(Locale.ENGLISH.getLanguage())+") formula=" + formula);
					}
				}
			}
		} catch (JDODetachedFieldAccessException x) {
			logger.warn("Could not traverse PriceConfig", x);
		}
	}

	/**
	 * @!ejb.interface-method
	 * @!ejb.transaction type="Required"
	 * @!ejb.permission role-name="_Guest_"
	 */
	public static Collection<GridPriceConfig> storePriceConfigs(
			PersistenceManager pm, Collection<GridPriceConfig> _priceConfigs,
			Class priceCalculatorClass,
			boolean get) // , String[] fetchGroups, int maxFetchDepth)
	throws PriceCalculationException
	{
		if (logger.isDebugEnabled()) {
			for (GridPriceConfig priceConfig : _priceConfigs) {
				logger.debug("storePriceConfig: PriceConfig BEFORE attach:");
				logGridPriceConfig(priceConfig);
			}
		}

		if (!PriceCalculator.class.isAssignableFrom(priceCalculatorClass))
			throw new IllegalArgumentException("priceCalculatorClass " + (priceCalculatorClass == null ? null : priceCalculatorClass.getName()) + " does not extend " + PriceCalculator.class.getName());

		Constructor priceCalculatorConstructor;
		try {
			priceCalculatorConstructor = priceCalculatorClass.getConstructor(new Class[] { ProductType.class });
		} catch (NoSuchMethodException x) {
			throw new IllegalArgumentException("priceCalculatorClass " + priceCalculatorClass.getName() + " does not have a constructor taking one parameter of type " + ProductType.class.getName(), x);
		}

		// store all price configs and put the living objects into priceConfigs
		Set<GridPriceConfig> priceConfigs = new HashSet<GridPriceConfig>();
		List<AffectedProductType> affectedProductTypes = null;
		for (GridPriceConfig priceConfig : _priceConfigs) {
			priceConfig = (GridPriceConfig) pm.makePersistent(priceConfig);
			priceConfigs.add(priceConfig);

			if (affectedProductTypes == null)
				affectedProductTypes = PriceConfigUtil.getAffectedProductTypes(pm, priceConfig);
			else
				affectedProductTypes.addAll(PriceConfigUtil.getAffectedProductTypes(pm, priceConfig));
		}

		// and recalculate the prices for all affected ProductTypes
		long startDT = System.currentTimeMillis();
		int recalculatedCounter = 0;
		int skippedBecauseAlreadyProcessed = 0;
		Set<ProductType> processedProductTypes = new HashSet<ProductType>();
		for (AffectedProductType affectedProductType : affectedProductTypes) {
			ProductType productType = (ProductType) pm.getObjectById(affectedProductType.getProductTypeID());
			if (processedProductTypes.contains(productType)) {
				++skippedBecauseAlreadyProcessed;
				continue;
			}
			processedProductTypes.add(productType);

			if (productType.getPackageNature() == ProductType.PACKAGE_NATURE_OUTER) {
				++recalculatedCounter;

				((IResultPriceConfig)productType.getPackagePriceConfig()).adoptParameters(
						productType.getInnerPriceConfig());

				PriceCalculator priceCalculator;
				try {
					priceCalculator = (PriceCalculator) priceCalculatorConstructor.newInstance(new Object[] { productType });
				} catch (Exception e) {
					throw new RuntimeException("priceCalculatorClass " + priceCalculatorClass.getName() + " could not be instantiated!", e);
				}
//				PriceCalculator priceCalculator = new PriceCalculator(productType);
				priceCalculator.preparePriceCalculation();
				priceCalculator.calculatePrices();

				if (logger.isDebugEnabled())
					logger.debug("storePriceConfig: Recalculating prices for ProductType " + productType.getPrimaryKey() + " (" + productType.getName().getText() + ")");
			}
			else {
				if (logger.isDebugEnabled())
					logger.debug("storePriceConfig: Will NOT recalculate prices for ProductType " + productType.getPrimaryKey() + " (" + productType.getName().getText() + ") because it is not PACKAGE_NATURE_OUTER!");
			}
		}

		logger.info("storePriceConfig: Recalculated prices for " + recalculatedCounter + " ProductTypes in " + (System.currentTimeMillis() - startDT) + " msec. // affectedProductTypes.size()=" + affectedProductTypes.size() + " // skippedBecauseAlreadyProcessed=" + skippedBecauseAlreadyProcessed);

		if (!get)
			return null;

		pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		pm.getFetchPlan().setGroups(new String[] {
				FetchPlan.DEFAULT, 
				FetchGroupsPriceConfig.FETCH_GROUP_EDIT});

//		pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
//		if (fetchGroups != null)
//			pm.getFetchPlan().setGroups(fetchGroups);

		return pm.detachCopyAll(priceConfigs);
	}

//	protected static Pattern tariffPKSplitPattern = null;
//
//	/**
//	 * @return a <tt>Collection</tt> of {@link TariffPricePair}
//	 *
//	 * @!ejb.interface-method
//	 * @!ejb.transaction type="Supports"
//	 * @!ejb.permission role-name="_Guest_"
//	 */
//	public static Collection<TariffPricePair> getTariffPricePairs(
//			PersistenceManager pm,
//			PriceConfigID priceConfigID, CustomerGroupID customerGroupID, CurrencyID currencyID,
//			String[] tariffFetchGroups, String[] priceFetchGroups)
//			{
//		if (tariffPKSplitPattern == null)
//			tariffPKSplitPattern = Pattern.compile("/");
//
//		// TODO use setResult and put all this logic into the JDO query!
//		StablePriceConfig priceConfig = (StablePriceConfig) pm.getObjectById(priceConfigID);
//		Collection priceCells = priceConfig.getPriceCells(
//				CustomerGroup.getPrimaryKey(customerGroupID.organisationID, customerGroupID.customerGroupID),
//				currencyID.currencyID);
//
//		Collection<TariffPricePair> res = new ArrayList<TariffPricePair>();
//
//		for (Iterator it = priceCells.iterator(); it.hasNext(); ) {
//			PriceCell priceCell = (PriceCell) it.next();
//			String tariffPK = priceCell.getPriceCoordinate().getTariffPK();
//			String[] tariffPKParts = tariffPKSplitPattern.split(tariffPK);
//			if (tariffPKParts.length != 2)
//				throw new IllegalStateException("How the hell can it happen that the tariffPK does not consist out of two parts?");
//
//			String tariffOrganisationID = tariffPKParts[0];
//			long tariffID = Long.parseLong(tariffPKParts[1]);
//
//			if (tariffFetchGroups != null)
//				pm.getFetchPlan().setGroups(tariffFetchGroups);
//
//			Tariff tariff = (Tariff) pm.getObjectById(TariffID.create(tariffOrganisationID, tariffID));
//			tariff = (Tariff) pm.detachCopy(tariff);
//
//			if (priceFetchGroups != null)
//				pm.getFetchPlan().setGroups(priceFetchGroups);
//
//			Price price = (Price) pm.detachCopy(priceCell.getPrice());
//
//			res.add(new TariffPricePair(tariff, price));
//		}
//
//		return res;
//			}

}
