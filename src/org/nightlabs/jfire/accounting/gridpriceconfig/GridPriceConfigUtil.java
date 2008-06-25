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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.FetchPlanBackup;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.priceconfig.AffectedProductType;
import org.nightlabs.jfire.accounting.priceconfig.FetchGroupsPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfigUtil;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.util.Util;

public class GridPriceConfigUtil
{
	private static final Logger logger = Logger.getLogger(GridPriceConfigUtil.class);

	protected GridPriceConfigUtil() { }

	/**
	 * This method is a workaround-method as we cannot tag certain fields as not-null-fields, because
	 * this causes errors during replication to other datastores. Therefore, we call this method after
	 * persisting one or more price-configs before the transaction is committed.
	 *
	 * @param pm The door to the datastore ;-)
	 */
	public static void assertConsistency(PersistenceManager pm, Set<PriceConfigID> priceConfigIDs)
	{
		long start = System.currentTimeMillis();
		{
			Query q = pm.newQuery(PriceCoordinate.class);
//			q.setResult("count(this.priceCoordinateID)");
			q.setResult("count(this)");
			q.setFilter("this.priceConfig == null");
			Long count = (Long) q.execute();
			if (count.intValue() != 0)
				throw new IllegalStateException("Datastore is inconsistent! Found " + count + " PriceCoordinate instances with PriceCoordinate.priceConfig == null!");
		}


		{
			Query q = pm.newQuery(PriceCell.class);
			q.setResult("count(this)");
			q.setFilter("this.price == null");
			Long count = (Long) q.execute();
			if (count.intValue() != 0)
				throw new IllegalStateException("Datastore is inconsistent! Found " + count + " PriceCell instances with PriceCell.price == null!");
		}
		{
			Query q = pm.newQuery(PriceCell.class);
			q.setResult("count(this)");
			q.setFilter("this.priceConfig == null");
			Long count = (Long) q.execute();
			if (count.intValue() != 0)
				throw new IllegalStateException("Datastore is inconsistent! Found " + count + " PriceCell instances with PriceCell.priceConfig == null!");
		}
		{
			Query q = pm.newQuery(PriceCell.class);
			q.setResult("count(this)");
			q.setFilter("this.priceCoordinate == null");
			Long count = (Long) q.execute();
			if (count.intValue() != 0)
				throw new IllegalStateException("Datastore is inconsistent! Found " + count + " PriceCell instances with PriceCell.priceCoordinate == null!");
		}


		{
			Query q = pm.newQuery(FormulaCell.class);
			q.setResult("count(this)");
			q.setFilter("this.priceConfig == null");
			Long count = (Long) q.execute();
			if (count.intValue() != 0)
				throw new IllegalStateException("Datastore is inconsistent! Found " + count + " FormulaCell instances with FormulaCell.priceConfig == null!");
		}
		{
			Query q = pm.newQuery(FormulaCell.class);
			q.setFilter("this.priceCoordinate == null");
			for (Iterator<?> it = ((Collection<?>)q.execute()).iterator(); it.hasNext(); ) {
				FormulaCell formulaCell = (FormulaCell) it.next();
				IPriceConfig pc = formulaCell.getPriceConfig();
				if (!(pc instanceof IFormulaPriceConfig))
					throw new IllegalStateException("Datastore is inconsistent! Found a FormulaCell where FormulaCell.priceConfig is not an instance of IFormulaPriceConfig, but " + (pc == null ? null : pc.getClass().getName()) + "! " + formulaCell);

				IFormulaPriceConfig fpc = (IFormulaPriceConfig) pc;

				if (!formulaCell.equals(fpc.getFallbackFormulaCell(false)))
					throw new IllegalStateException("Datastore is inconsistent! Found a FormulaCell where thisFormulaCell.priceCoordinate is null, but thisFormulaCell.priceConfig.fallbackFormulaCell != thisFormulaCell! " + formulaCell);
			}
		}

		long duration = System.currentTimeMillis() - start;
		if (duration > 1000)
			Logger.getLogger(GridPriceConfig.class).warn("Consistency check took very long: " + duration + " msec", new Exception());
	}

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
	 * @param assignInnerPriceConfigCommand If not <code>null</code>, the specified ProductType will get an <code>innerPriceConfig</code> assigned and the inheritance-meta-data adjusted.
	 */
	public static <T extends GridPriceConfig> Collection<T> storePriceConfigs(
			PersistenceManager pm, Collection<T> _priceConfigs,
			PriceCalculatorFactory priceCalculatorFactory,
			boolean get,
			AssignInnerPriceConfigCommand assignInnerPriceConfigCommand)
	throws PriceCalculationException
	{
		if (logger.isDebugEnabled()) {
			for (GridPriceConfig priceConfig : _priceConfigs) {
				logger.debug("storePriceConfigs: PriceConfig BEFORE attach:");
				logGridPriceConfig(priceConfig);
			}
		}

		// prevent writing a partner-PriceConfig
		String localOrganisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();

		// store all price configs and put the living objects into priceConfigs
		Set<T> priceConfigs = new HashSet<T>();
		List<AffectedProductType> affectedProductTypes = null;
		for (T priceConfig : _priceConfigs) {
			if (logger.isInfoEnabled())
				logger.info("storePriceConfigs: storing GridPriceConfig: " + priceConfig.getPrimaryKey());

			if (!localOrganisationID.equals(priceConfig.getOrganisationID()))
				throw new IllegalArgumentException("Cannot store a partner's price config: " + priceConfig.getPrimaryKey());

			priceConfig = pm.makePersistent(priceConfig);
			priceConfigs.add(priceConfig);
		}

		if (assignInnerPriceConfigCommand != null) {
			ProductType pt = (ProductType) pm.getObjectById(assignInnerPriceConfigCommand.getProductTypeID());
			IInnerPriceConfig pc = assignInnerPriceConfigCommand.getInnerPriceConfigID() == null ? null : (IInnerPriceConfig) pm.getObjectById(assignInnerPriceConfigCommand.getInnerPriceConfigID());
			boolean applyInheritance = false;
			if (pt.getFieldMetaData(ProductType.FieldName.innerPriceConfig).isValueInherited() != assignInnerPriceConfigCommand.isInnerPriceConfigInherited()) {
				pt.getFieldMetaData(ProductType.FieldName.innerPriceConfig).setValueInherited(assignInnerPriceConfigCommand.isInnerPriceConfigInherited());
				applyInheritance = true;
			}
			if (!Util.equals(pc, pt.getInnerPriceConfig())) {
				pt.setInnerPriceConfig(pc);
				applyInheritance = true;
			}
			if (applyInheritance)
				pt.applyInheritance();
		}

		for (GridPriceConfig priceConfig : _priceConfigs) { // _priceConfigs is empty, if there is null assigned as inner price config => affectedProductTypes = null !!!
			if (affectedProductTypes == null)
				affectedProductTypes = PriceConfigUtil.getAffectedProductTypes(pm, priceConfig);
			else
				affectedProductTypes.addAll(PriceConfigUtil.getAffectedProductTypes(pm, priceConfig));
		}

		if (affectedProductTypes == null)
			affectedProductTypes = new ArrayList<AffectedProductType>();

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

			if (
					productType.getPackageNature() == ProductType.PACKAGE_NATURE_OUTER &&
					productType.getPackagePriceConfig() != null &&
					productType.getInnerPriceConfig() != null)
			{
				++recalculatedCounter;

				((IResultPriceConfig)productType.getPackagePriceConfig()).adoptParameters(productType.getInnerPriceConfig());

				PriceCalculator priceCalculator = priceCalculatorFactory.createPriceCalculator(productType);
				priceCalculator.preparePriceCalculation();
				priceCalculator.calculatePrices();

				if (logger.isDebugEnabled())
					logger.debug("storePriceConfig: Recalculating prices for ProductType " + productType.getPrimaryKey() + " (" + productType.getName().getText() + ")");
			}
			else {
				if (logger.isDebugEnabled())
					logger.debug("storePriceConfig: Will NOT recalculate prices for ProductType " + productType.getPrimaryKey() + " (" + productType.getName().getText() + ") because it is not PACKAGE_NATURE_OUTER or has not both (inner + outer) price-configs assigned!");
			}
		}

		logger.info("storePriceConfig: Recalculated prices for " + recalculatedCounter + " ProductTypes in " + (System.currentTimeMillis() - startDT) + " msec. // affectedProductTypes.size()=" + affectedProductTypes.size() + " // skippedBecauseAlreadyProcessed=" + skippedBecauseAlreadyProcessed);

		Set<PriceConfigID> priceConfigIDs = NLJDOHelper.getObjectIDSet(priceConfigs);
		assertConsistency(pm, priceConfigIDs);

		if (!get)
			return null;

		pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
		pm.getFetchPlan().setGroups(new String[] {
				FetchPlan.DEFAULT,
				FetchGroupsPriceConfig.FETCH_GROUP_EDIT});

		return pm.detachCopyAll(priceConfigs);
	}

// FIXME WORKAROUND for JPOX - begin
	private static void resolveExtendedProductTypes(PersistenceManager pm, ProductType attachedPT, ProductType detachedPT)
	{
		while (attachedPT != null) {
			attachedPT = attachedPT.getExtendedProductType();
			System.out.println(attachedPT == null ? null : attachedPT.getPrimaryKey());
			ProductType extPT = null;
			if (attachedPT != null)
				extPT = pm.detachCopy(attachedPT);
			try {
				Method method = ProductType.class.getDeclaredMethod("setExtendedProductType", new Class[] {ProductType.class});
				method.setAccessible(true);
				method.invoke(detachedPT, extPT);
			} catch (Exception e) {
				e.printStackTrace();
			}
			detachedPT = extPT;
		}
	}
//FIXME WORKAROUND for JPOX - end

	public static ProductType detachProductTypeForPriceConfigEditing(PersistenceManager pm, ProductTypeID productTypeID)
	{
		FetchPlan fetchPlan = pm.getFetchPlan();
		FetchPlanBackup fetchPlanBackup = NLJDOHelper.backupFetchPlan(fetchPlan);
		try {
			fetchPlan.setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS);
			fetchPlan.setMaxFetchDepth(-1);
			fetchPlan.setGroups(new String[] {
					FetchPlan.DEFAULT,
					FetchGroupsPriceConfig.FETCH_GROUP_EDIT});

			pm.getExtent(ProductType.class);
			ProductType res = (ProductType) pm.getObjectById(productTypeID);
			res.getName().getTexts();
			res.getFieldMetaData(ProductType.FieldName.innerPriceConfig, false);
			res.getFieldMetaData(ProductType.FieldName.packagePriceConfig, false);
			res.getProductTypeLocal().getProductType();

			// load main price configs
			res.getPackagePriceConfig();
			res.getInnerPriceConfig();

			// load all extended ProductType-s
			ProductType pt = res;
			while (pt != null) {
				pt = pt.getExtendedProductType();
				System.out.println(pt == null ? null : pt.getPrimaryKey());
			}

			// load all nested ProductType-s
			for (NestedProductTypeLocal npt : res.getProductTypeLocal().getNestedProductTypeLocals()) {
				npt.getPackageProductTypeLocal();
				ProductType ipt = npt.getInnerProductTypeLocal().getProductType();
				ipt.getProductTypeLocal().getProductType();
				ipt.getName().getTexts();
				ipt.getPackagePriceConfig();
				ipt.getInnerPriceConfig();
				pt = ipt;
				while (pt != null) {
					pt = pt.getExtendedProductType();
					System.out.println(pt == null ? null : pt.getPrimaryKey());
				}
			}

			ProductType detachedRes = pm.detachCopy(res);

			// FIXME WORKAROUND for JPOX - begin
			resolveExtendedProductTypes(pm, res, detachedRes);

			for (NestedProductTypeLocal attached_npt : res.getProductTypeLocal().getNestedProductTypeLocals()) {
				NestedProductTypeLocal detached_npt = detachedRes.getProductTypeLocal().getNestedProductTypeLocal(attached_npt.getInnerProductTypePrimaryKey(), true);

				resolveExtendedProductTypes(pm,
						attached_npt.getInnerProductTypeLocal().getProductType(),
						detached_npt.getInnerProductTypeLocal().getProductType());
			}
			// FIXME WORKAROUND for JPOX - end

			if (logger.isDebugEnabled()) {
				LinkedList<ProductType> productTypes = new LinkedList<ProductType>();
				productTypes.add(detachedRes);

				for (NestedProductTypeLocal npt : detachedRes.getProductTypeLocal().getNestedProductTypeLocals())
					productTypes.add(npt.getInnerProductTypeLocal().getProductType());

				for (ProductType productType : productTypes) {
					logger.debug("getProductTypeForPriceConfigEditing: productType="+productType.getPrimaryKey());
					if (productType.getInnerPriceConfig() instanceof GridPriceConfig) {
						logger.debug("innerPriceConfig:");
						GridPriceConfigUtil.logGridPriceConfig((GridPriceConfig)productType.getInnerPriceConfig());
					}
					if (productType.getPackagePriceConfig() instanceof GridPriceConfig) {
						logger.debug("packagePriceConfig:");
						GridPriceConfigUtil.logGridPriceConfig((GridPriceConfig)productType.getPackagePriceConfig());
					}
				}
			}

			return detachedRes;
		} finally {
			NLJDOHelper.restoreFetchPlan(fetchPlan, fetchPlanBackup);
		}
	}
}
