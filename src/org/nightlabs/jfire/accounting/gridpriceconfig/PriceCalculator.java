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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.TariffMapper;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.id.PriceFragmentTypeID;
import org.nightlabs.jfire.accounting.id.TariffID;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.CustomerGroup;
import org.nightlabs.jfire.trade.CustomerGroupMapper;
import org.nightlabs.jfire.trade.id.CustomerGroupID;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class PriceCalculator
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(PriceCalculator.class);

	private ProductType packageProductType;
	private IResultPriceConfig packagePriceConfig;

	/**
	 * key: String innerProductTypePK
	 * value: NestedProductTypeLocal
	 */
	protected Map<String, NestedProductTypeLocal> virtualPackagedProductTypes = new HashMap<String, NestedProductTypeLocal>();

	private CustomerGroupMapper customerGroupMapper;

	public CustomerGroupMapper getCustomerGroupMapper()
	{
		return customerGroupMapper;
	}

	private TariffMapper tariffMapper;

	public TariffMapper getTariffMapper()
	{
		return tariffMapper;
	}

	PersistenceManager pm; // TODO JPOX WORKAROUND only needed for some workarounds! Will be removed again, soon.

	/**
	 * @param packageProductType The <tt>ProductType</tt> which encloses all the other <tt>ProductType</tt>s and
	 * which has an {@link IResultPriceConfig} assigned as {@link ProductType#packagePriceConfig} or <code>null</code>
	 * (= no <code>packagePriceConfig</code> e.g. for fully dynamic prices). If the packagePriceConfig is null, this
	 * <code>PriceCalculator</code> will create a temporary one via {@link #createResultPriceConfig()}. This might be changed
	 * though for optimization later.
	 *
	 * @param customerGroupMapper TODO
	 * @param tariffMapper TODO
	 */
	public PriceCalculator(ProductType packageProductType, CustomerGroupMapper customerGroupMapper, TariffMapper tariffMapper)
	{
		if (packageProductType == null)
			throw new IllegalArgumentException("packageProductType must not be null!");

		if (customerGroupMapper == null)
			throw new IllegalArgumentException("customerGroupMapper must not be null!");

		if (tariffMapper == null)
			throw new IllegalArgumentException("tariffMapper must not be null!");

		// If the JDO cache (1st-leve) has been evicted, the call packageProductType.getProductTypeLocal().getProductType()
		// will return a different instance. Therefore, we do not use packageProductType directly, but go over the ProductTypeLocal.
		// This is necessary anyway and therefore always detached.
		this.packageProductType = packageProductType.getProductTypeLocal().getProductType();
		this.customerGroupMapper = customerGroupMapper;
		this.tariffMapper = tariffMapper;
		if (packageProductType.isPackageInner())
			throw new IllegalArgumentException("packageProductType.isPackageInner() is true! Cannot calculate prices if the carrier ProductType is not a package!");

		IPriceConfig packagePriceConfig = packageProductType.getPackagePriceConfig();
		if (packagePriceConfig != null && !(packagePriceConfig instanceof IResultPriceConfig))
			throw new IllegalArgumentException("packageProductType.priceConfig is not an instance of " + IResultPriceConfig.class.getName() + " but " + (packagePriceConfig == null ? "null" : packagePriceConfig.getClass().getName()));

		this.packagePriceConfig = (IResultPriceConfig) packagePriceConfig;
		if (this.packagePriceConfig == null) {
			this.packagePriceConfig = createResultPriceConfig(); // we use a dummy price config, if the packageProductType does not have one.
			IInnerPriceConfig innerPC = packageProductType.getInnerPriceConfig();
			if (innerPC == null)
				throw new IllegalArgumentException("packageProductType does neither have a packagePriceConfig nor an innerPriceConfig! " + packageProductType.getPrimaryKey());
			this.packagePriceConfig.adoptParameters(innerPC);
		}

		this.pm = JDOHelper.getPersistenceManager(packageProductType);
	}

	/**
	 * @return Returns the packageProductType.
	 */
	public ProductType getPackageProductType()
	{
		return packageProductType;
	}

	/**
	 * This <p>Map</p> has every <tt>productTypePK</tt> of all anchestors (recursive) of all
	 * packaged <tt>ProductInfo</tt> s registered as key and the packaged <tt>ProductInfo</tt>'s
	 * <tt>productPK</tt> as value. Additionally, it maps all packaged <tt>ProductInfo</tt>'s
	 * <tt>productPK</tt> to itself (for easier coordinate resolve).
	 * <p>
	 * key: String searchedProductPK<br/>
	 * value: List of String packagedProductPK
	 *
	 * @!jdo.field persistence-modifier="none"
	 */
	protected Map<String, List<String>> resolvableProductTypes = null;

	public void preparePriceCalculation()
	{
		preparePriceCalculation_createResolvableProductTypesMap(); // must be first to create virtualPackagedProductTypes
		preparePriceCalculation_createPackagedResultPriceConfigs();
//		preparePriceCalculation_adoptPackagePriceConfigParams();
	}

//	/**
//	 * This method iterates all inner products (including the virtual one) and
//	 */
//	public void preparePriceCalculation_adoptPackagePriceConfigParams()
//	{
//		for (Iterator it = virtualPackagedProductTypes.iterator(); it.hasNext(); ) {
//			ProductType productType = (ProductType) it.next();
//			if (!productType.isPackage() || productType == packageProductType) {
//				FormulaPriceConfig formulaPriceConfig = (FormulaPriceConfig) productType.getInnerPriceConfig();
//				formulaPriceConfig.adoptParameters(packagePriceConfig);
//			}
//		}
//	}
	
	protected IResultPriceConfig createResultPriceConfig()
	{
		return new StablePriceConfig(
				IDGenerator.getOrganisationID(),
				PriceConfig.createPriceConfigID());
	}

	/**
	 * This method creates an instance of IResultPriceConfig
	 * by calling {@link #createResultPriceConfig()} for
	 * each internal FormulaPriceConfig to persistently store
	 * the results of the formulas in the current context, if it does
	 * not yet exist. Additionally, it adopts the parameters.
	 *
	 * @throws ModuleException
	 */
	public void preparePriceCalculation_createPackagedResultPriceConfigs()
	{
		// Create an instance of StablePriceConfig for each FormulaPriceConfig
		// (if not yet existing) to store the results of the FormulaPriceConfig.
		for (NestedProductTypeLocal nestedProductTypeLocal : virtualPackagedProductTypes.values()) {
			ProductType innerProductType = nestedProductTypeLocal.getInnerProductTypeLocal().getProductType();
			IPriceConfig priceConfig;

			if (innerProductType.isPackageOuter() && innerProductType != packageProductType)
				priceConfig = innerProductType.getPackagePriceConfig();
			else {
				priceConfig = innerProductType.getInnerPriceConfig();

				if (priceConfig == null)
					logger.info("preparePriceCalculation_createPackagedResultPriceConfigs: innerProductType \"" + innerProductType.getPrimaryKey() + "\" (nested within \"" + packageProductType.getPrimaryKey() + "\") does not have an inner price-config assigned!");
				else {
					IFormulaPriceConfig fpc = (IFormulaPriceConfig)priceConfig;

					fpc.adoptParameters(packagePriceConfig, true);

					IResultPriceConfig resultPriceConfig = (IResultPriceConfig) fpc.getPackagingResultPriceConfig(
							innerProductType.getPrimaryKey(), packageProductType.getPrimaryKey(), false);

					if (resultPriceConfig != null) {
						resultPriceConfig.resetPriceFragmentCalculationStati();
					}
					else {
						resultPriceConfig = createResultPriceConfig();

						// force the new price config to be written to the DB - otherwise we run into a foreign key violation
						// see http://www.jpox.org/servlet/forum/viewthread?thread=4871&offset=0#27443 error (2)
						if (pm != null)
							resultPriceConfig = pm.makePersistent(resultPriceConfig);

						fpc.setPackagingResultPriceConfig(
								innerProductType.getPrimaryKey(), packageProductType.getPrimaryKey(),
								resultPriceConfig);
					}

					resultPriceConfig.adoptParameters(packagePriceConfig);
				}
			}

			if (priceConfig != null) {
				// We need to merge the PriceFragmentTypes of all packaged PriceConfigs into
				// the package PriceConfig. To speed it all up, we do it here instead of a second
				// iteration.
				for (PriceFragmentType pft : priceConfig.getPriceFragmentTypes()) {
					if (!packagePriceConfig.containsPriceFragmentType(pft))
						packagePriceConfig.addPriceFragmentType(pft);
				}
			}
		}
	}

	/**
	 * This method creates the <tt>Map resolvableProductTypes</tt>. It must be called
	 * before <tt>calculatePrices()</tt> at least once and every time when a <tt>ProductType</tt>
	 * is added to or removed from the package. This method must be called, after the packageProductType
	 * has been detached!
	 */
	public void preparePriceCalculation_createResolvableProductTypesMap()
	{
//		IPriceConfig packagePriceConfig = packageProductType.getPackagePriceConfig();

		// Populate the Map resolvableProductTypes to allow referencing of anchestors
		// of packaged products in the formulas.
		this.resolvableProductTypes = new HashMap<String, List<String>>();

		// I use a LinkedList, because it is often iterated and I don't think I
		// don't need to access it differently.
//		virtualPackagedProductTypes = new LinkedList(packageProductType.getPackagedProductTypes());
		virtualPackagedProductTypes.clear();
		for (NestedProductTypeLocal npt : packageProductType.getProductTypeLocal().getNestedProductTypeLocals(true)) {
			if (npt.getInnerProductTypeLocal().getProductType().equals(packageProductType) && npt.getInnerProductTypeLocal().getProductType() != packageProductType)
				throw new IllegalStateException("Why the hell are there two instances of this JDO object?!??!");

			virtualPackagedProductTypes.put(npt.getInnerProductTypePrimaryKey(), npt);
		}

//		// register packageProductType because of virtual self-packaging - already done
//		if (packageProductType.getInnerPriceConfig() != null)
//			virtualPackagedProductTypes.put(
//					packageProductType.getPrimaryKey(),
//					new NestedProductTypeLocal(packageProductType, packageProductType));

		for (NestedProductTypeLocal packagedProductType : virtualPackagedProductTypes.values()) {
			_resolvableProductTypes_registerWithAnchestors(packagedProductType.getInnerProductTypeLocal().getProductType());
		} // for (Iterator it = getPackagedProductInfos().iterator(); it.hasNext(); ) {
	}
	
	protected void _resolvableProductTypes_registerWithAnchestors(ProductType packagedProductType)
	{
		// We map the productTypePK to itself, so we have it easier when resolving
		// (*all* searchable productTypePKs registered here).
		ProductType extendedProductType = packagedProductType;

		while (extendedProductType != null) {
			List<String> targetList = resolvableProductTypes.get(extendedProductType.getPrimaryKey());
			if (targetList == null) {
				targetList = new LinkedList<String>();
				resolvableProductTypes.put(extendedProductType.getPrimaryKey(), targetList);
			}

			targetList.add(packagedProductType.getPrimaryKey());
			extendedProductType = extendedProductType.getExtendedProductType();
		}
	}
	
	
	/**
	 * This method calculates the prices and populates the <tt>StablePriceConfig</tt>
	 * which is assigned to this <tt>AssemblyPackageProductInfo</tt>. This method can
	 * be executed in the client. Note, that you must call
	 * <tt>prepareResolvableProductInfos(PersistenceManager)</tt> before, for this
	 * method to work!
	 * <p>
	 * Note that price calculation is currently not thread safe! You should
	 * always detach before calculating prices! Detaching creates a copy and
	 * solves the problem.
	 *
	 * @see #prepareResolvableProductTypes()
	 */
	public void calculatePrices()
	throws PriceCalculationException
	{
		if (resolvableProductTypes == null)
			throw new IllegalStateException("The method prepareResolvableProductTypes(..) has not been called!");

//		StablePriceConfig packagePriceConfig = (StablePriceConfig) packagegetPriceConfig();
		
		// set all PriceCells to the status dirty
		// TODO do we really want to always recalculate all cells if sth. changed?
		// To keep track over dependencies is too complicated...
		packagePriceConfig.resetPriceFragmentCalculationStati();
		for (NestedProductTypeLocal nestedProductTypeLocal : virtualPackagedProductTypes.values()) {
			ProductType innerProductType = nestedProductTypeLocal.getInnerProductTypeLocal().getProductType();
			IPriceConfig priceConfig = innerProductType.getPriceConfigInPackage(
					packageProductType.getPrimaryKey());

//			if (innerProductType.isPackage() && innerProductType != packageProductType)
//				priceConfig = innerProductType.getPackagePriceConfig();
//			else {
//				priceConfig = innerProductType.getInnerPriceConfig();
			if (priceConfig instanceof IFormulaPriceConfig) {
				IFormulaPriceConfig fpc = (IFormulaPriceConfig)priceConfig;
				IResultPriceConfig resultPriceConfig = (IResultPriceConfig) fpc.getPackagingResultPriceConfig(
						innerProductType.getPrimaryKey(), packageProductType.getPrimaryKey(), true);

				resultPriceConfig.resetPriceFragmentCalculationStati();
			}
		}
		
//		GridPriceConfig aipipc = (GridPriceConfig) packageProductType.getInnerProductType().getPriceConfig();
//
//		// We need to assign all the parameters from the inner PriceConfig to our
//		// packagePriceConfig, because it is the inner PConfig that defines which
//		// cells we have (which possible parameters exist).
//		// The PriceFragmentType s are NOT touched by this! Hence we merged it above.
//		packagePriceConfig.adoptParameters(aipipc);
//		// Now our packagePriceConfig should have exactly the same parameters like the
//		// FormulaPriceConfig of the AssemblyInnerProductInfo.
//		// The necessary cells (formula and price) are automatically created if missing and
//		// unnecessary cells removed.

//		// Create an instance of StablePriceConfig for each FormulaPriceConfig
//		// (if not yet existing) to store the results of the FormulaPriceConfig.
//		for (Iterator it = virtualPackagedProductTypes.iterator(); it.hasNext(); ) {
//			ProductType innerProductType = (ProductType)it.next();
//			IPriceConfig priceConfig;
//
//			if (innerProductType.isPackage() && innerProductType != packageProductType)
//				priceConfig = innerProductType.getPackagePriceConfig();
//			else {
//				priceConfig = innerProductType.getInnerPriceConfig();
//
////			if (priceConfig instanceof FormulaPriceConfig) { // should now always be the case
//				FormulaPriceConfig fpc = (FormulaPriceConfig)priceConfig;
//				StablePriceConfig resultPriceConfig = fpc.getPackagingResultPriceConfig(
//						innerProductType.getPrimaryKey(), packageProductType.getPrimaryKey(), false);
//
//				if (resultPriceConfig != null) {
//					resultPriceConfig.resetPriceFragmentCalculationStati();
//				}
//				else {
//					resultPriceConfig = new StablePriceConfig(
//							priceConfigIDProvider.getOrganisationID(),
//							priceConfigIDProvider.createPriceConfigID());
////							this.getOrganisationID(),
////							this.getProductID()
////							+ '-'
////							+ innerProductInfo.getOrganisationID()
////							+ '-'
////							+ innerProductInfo.getProductID());
//
//					fpc.setPackagingResultPriceConfig(
//							innerProductType.getPrimaryKey(), packageProductType.getPrimaryKey(),
//							resultPriceConfig);
//				}
//
//				resultPriceConfig.adoptParameters(packagePriceConfig);
//			}
//
//			// We need to merge the PriceFragmentTypes of all packaged PriceConfigs into
//			// the package PriceConfig. To speed it all up, we do it here instead of a second
//			// iteration.
//			for (Iterator itpft = innerProductType.getInnerPriceConfig().getPriceFragmentTypes().iterator(); itpft.hasNext(); ) {
//				PriceFragmentType pft = (PriceFragmentType) itpft.next();
//				if (!packagePriceConfig.containsPriceFragmentType(pft))
//					packagePriceConfig.addPriceFragmentType(pft);
//			}
//		}

		// Now, all preparation is done and we can start calculation:
		for (PriceCell outerPriceCell : packagePriceConfig.getPriceCells()) {
			PriceCoordinate priceCoordinate = outerPriceCell.getPriceCoordinate();
			priceCoordinate.assertAllDimensionValuesAssigned();

			for (PriceFragmentType priceFragmentType : packagePriceConfig.getPriceFragmentTypes()) {
				long outerPriceCellAmount = 0;

				for (NestedProductTypeLocal nestedProductTypeLocal : virtualPackagedProductTypes.values()) {
//					ProductType innerProductType = nestedProductTypeLocal.getInnerProductType();

//					if (innerProductType.isPackageInner() || innerProductType == packageProductType) {
//						IPriceConfig innerPriceConfig = innerProductType.getInnerPriceConfig();
						PriceCell innerPriceCell = calculatePriceCell(nestedProductTypeLocal, priceFragmentType, priceCoordinate);
	
						if (innerPriceCell != null) { // it might be null, because the packaging is quite free and cells might be missing
							long amount = innerPriceCell.getPrice().getAmount(priceFragmentType);
							amount *= nestedProductTypeLocal.getQuantity();
							outerPriceCellAmount += amount;
						}
//					}
				} // for (Iterator itPackagedPIs = getPackagedProductInfos().iterator(); itPackagedPIs.hasNext(); ) {

				outerPriceCell.getPrice().setAmount(priceFragmentType, outerPriceCellAmount);
			}
		}
	}

	public IPriceCoordinate createMappedLocalPriceCoordinate(
			NestedProductTypeLocal nestedProductTypeLocal, PriceFragmentType priceFragmentType, IPriceCoordinate localPriceCoordinate)
	{
		if (nestedProductTypeLocal.getPackageProductTypeOrganisationID().equals(nestedProductTypeLocal.getInnerProductTypeOrganisationID())) // TODO or better check the organisationIDs of the price-configs?
			return localPriceCoordinate;

		CustomerGroupID orgCustomerGroupID = CustomerGroupID.create(localPriceCoordinate.getCustomerGroupPK());
		CustomerGroupID newCustomerGroupID = getCustomerGroupMapper().getPartnerCustomerGroupID(orgCustomerGroupID, nestedProductTypeLocal.getInnerProductTypeOrganisationID(), true);

		TariffID orgTariffID = TariffID.create(localPriceCoordinate.getTariffPK());
		TariffID newTariffID = getTariffMapper().getPartnerTariffID(orgTariffID, nestedProductTypeLocal.getInnerProductTypeOrganisationID(), true);

		IPriceCoordinate res = Util.cloneSerializable(localPriceCoordinate);
		res.setTariffPK(Tariff.getPrimaryKey(newTariffID.organisationID, newTariffID.tariffID));
		res.setCustomerGroupPK(CustomerGroup.getPrimaryKey(newCustomerGroupID.organisationID, newCustomerGroupID.customerGroupID));
		return res;
	}

	/**
	 * @return Returns a PriceCell which is calculated or <tt>null</tt>.
	 */
	public PriceCell calculatePriceCell(
			NestedProductTypeLocal packagedProductType, PriceFragmentType priceFragmentType,
			IPriceCoordinate localPriceCoordinate)
	throws PriceCalculationException
	{
		ProductType innerProductType = packagedProductType.getInnerProductTypeLocal().getProductType();

		localPriceCoordinate = createMappedLocalPriceCoordinate(packagedProductType, priceFragmentType, localPriceCoordinate);

		IPriceConfig innerPriceConfig;
		if (innerProductType.isPackageOuter() && innerProductType != packageProductType)
			innerPriceConfig = innerProductType.getPackagePriceConfig();
		else {
			innerPriceConfig = innerProductType.getInnerPriceConfig();

			if (innerPriceConfig != null) {
				IFormulaPriceConfig innerFPC = (IFormulaPriceConfig)innerPriceConfig;
				IResultPriceConfig stablePriceConfig = (IResultPriceConfig) innerFPC.getPackagingResultPriceConfig(
						innerProductType.getPrimaryKey(), packageProductType.getPrimaryKey(), true);

				PriceCell innerPriceCell = stablePriceConfig.createPriceCell(localPriceCoordinate);

				FormulaCell innerFormulaCell = innerFPC.getFormulaCell(localPriceCoordinate, false);
				if (innerFormulaCell != null && innerFormulaCell.getFormula(priceFragmentType) == null)
					innerFormulaCell = null;

				if (innerFormulaCell == null)
					innerFormulaCell = innerFPC.getFallbackFormulaCell(false);

				calculatePriceCell(
						innerFormulaCell, innerPriceCell, packagedProductType, priceFragmentType);
				return innerPriceCell;
			}
		}

		if (innerPriceConfig == null)
			return null;

		if (innerPriceConfig instanceof IResultPriceConfig) {
			IResultPriceConfig innerTPC = (IResultPriceConfig)innerPriceConfig;
			PriceCell innerPriceCell = innerTPC.getPriceCell(localPriceCoordinate, false);
			return innerPriceCell;
		}

		// Maybe support some other basic priceconfigs later - e.g. a StaticPriceConfig
		// that provides only one price for all situations or a TariffDependentPriceConfig...
		throw new UnsupportedOperationException("Unsupported IPriceConfig in packaged ProductType \""+innerProductType.getPrimaryKey()+"\"!");
	}

	protected IAbsolutePriceCoordinate createAbsolutePriceCoordinate(
			IPriceCoordinate priceCoordinate,
			ProductType productType, PriceFragmentType priceFragmentType)
	{
		return new AbsolutePriceCoordinate(
				priceCoordinate, productType, priceFragmentType);
	}
	
	protected CellReflector createCellReflector(
			IAbsolutePriceCoordinate absolutePriceCoordinate,
			NestedProductTypeLocal nestedProductTypeLocal)
	{
		return new CellReflector(
				this, packageProductType,
				absolutePriceCoordinate, nestedProductTypeLocal
				);
	}

	/**
	 * <code>Important:</code> You should not override this method! Override
	 * {@link #populateImportPackages(Set)} instead!
	 * <p>
	 * This method calls {@link #populateImportPackages(Set)} and returns
	 * a correctly formatted <code>String</code>.
	 * </p>
	 *
	 * @return Return either <tt>null</tt> (if you don't want to import any package) or
	 *		a comma separated list of package names. Note, that all packages need to be
	 *		prefixed with "Packages.". For the "java" package, this prefix is not
	 *		necessary (but doesn't hurt either). An example for a valid return
	 *		value could be: "java.io, Packages.org.nightlabs.test, Packages.java.util"
	 *
	 * @see #getImportClasses()
	 */
	protected String getImportPackages()
	{
		Set<Package> packages = new HashSet<Package>();
		populateImportPackages(packages);
		if (packages.isEmpty())
			return null;

		StringBuffer sb = new StringBuffer();
		for (Package pakkage : packages) {
			if (sb.length() != 0)
				sb.append(", ");

			sb.append("Packages.");
			sb.append(pakkage.getName());
		}
		return sb.toString();
	//	return "Packages.org.nightlabs.jfire.accounting.tariffpriceconfig";
	}

	protected void populateImportPackages(Set<Package> packages)
	{
		// we don't need to import any packages - we import individual classes
	}

	/**
	 * <code>Important:</code> You should not override this method! Override
	 * {@link #populateImportClasses(Set)} instead!
	 * <p>
	 * This method calls {@link #populateImportClasses(Set)} and returns
	 * a correctly formatted <code>String</code>.
	 * </p>
	 *
	 * @return either <tt>null</tt> (if no classes should be imported) or
	 *		a comma separated list of fully qualified class names. Note, that all packages
	 *		need to be
	 *		prefixed with "Packages.". For the "java" package, this prefix is not
	 *		necessary (but doesn't hurt either). An example for a valid return
	 *		value could be: "java.io.InputStream, Packages.org.nightlabs.test.MyClass, Packages.java.util.List"
	 *
	 * @see #getImportPackages()
	 */
	protected String getImportClasses()
	{
		Set<Class<?>> classes = new HashSet<Class<?>>();
		populateImportClasses(classes);
		if (classes.isEmpty())
			return null;

		StringBuffer sb = new StringBuffer();
		for (Iterator<Class<?>> it = classes.iterator(); it.hasNext();) {
			Class<?> clazz = it.next();
			if (sb.length() != 0)
				sb.append(", ");

			sb.append("Packages.");
			sb.append(clazz.getName());
		}
		return sb.toString();

//		return
//			"Packages." + AbsolutePriceCoordinate.class.getName()
//			+ ", " +
//			"Packages." + CustomerGroupID.class.getName()
//			+ ", " +
//			"Packages." + TariffID.class.getName()
//			+ ", " +
//			"Packages." + CurrencyID.class.getName();
	}

	protected void populateImportClasses(Set<Class<?>> classes)
	{
		classes.add(AbsolutePriceCoordinate.class);

		// types passed to the constructor of PriceCoordinate in JavaScript formulas (which actually instantiate AbsolutePriceCoordinate s)
		classes.add(CustomerGroupID.class);
		classes.add(TariffID.class);
		classes.add(CurrencyID.class);

		// types passed to the constructor of AbsolutePriceCoordinate (additionally) in JavaScript formulas
		classes.add(ProductTypeID.class);
		classes.add(PriceFragmentTypeID.class);
	}

	/**
	 * @param formulaCell The cell from which to read the formula. Might be <tt>null</tt>. If so, the result of the formula is 0.
	 * @param priceCell Must <b>not</b> be <tt>null</tt>.
	 * @param productInfo The productInfo to which the cell belongs.
	 * @param priceFragmentType The current PriceFragmentType for which to calculate a price.
	 * @throws PriceCalculationException
	 *
	 * @throws ModuleException
	 */
	protected void calculatePriceCell(
			FormulaCell formulaCell, PriceCell priceCell,
			NestedProductTypeLocal nestedProductTypeLocal, PriceFragmentType priceFragmentType)
	throws PriceCalculationException
//		throws ModuleException
	{
		// formulaCell can be null!
		if (priceCell == null)
			throw new NullPointerException("priceCell");
		if (nestedProductTypeLocal == null)
			throw new NullPointerException("nestedProductTypeLocal");
		if (priceFragmentType == null)
			throw new NullPointerException("priceFragmentType");
		
		String formula = null;

		ProductType productType = nestedProductTypeLocal.getInnerProductTypeLocal().getProductType();

		IAbsolutePriceCoordinate absolutePriceCoordinate = createAbsolutePriceCoordinate(
				priceCell.getPriceCoordinate(),
				productType, priceFragmentType);
		try {
			String status = priceCell.getPriceFragmentCalculationStatus(priceFragmentType.getPrimaryKey());

			if (logger.isDebugEnabled())
				logger.debug("calculatePriceCell (" + absolutePriceCoordinate + "): priceFragmentType=" + priceFragmentType.getPrimaryKey() + " status=" + status);

			if (PriceCell.CALCULATIONSTATUS_CLEAN.equals(status)) {
				if (logger.isDebugEnabled())
					logger.debug("calculatePriceCell (" + absolutePriceCoordinate + "): priceFragmentType=" + priceFragmentType.getPrimaryKey() + " result="+priceCell.getPrice().getAmount(priceFragmentType)+" readingFromPriceCell: " + priceCell);

				return; // nothing to do
			}

			if (PriceCell.CALCULATIONSTATUS_INPROCESS.equals(status))
				throw new CircularReferenceException(
						absolutePriceCoordinate,
						"PriceCell \""+priceCell.getPriceCoordinate()+"\" has a circular reference in priceFragmentType \""+priceFragmentType.getPrimaryKey()+"\" in productType \""+productType.getPrimaryKey()+"\"!");

			if (PriceCell.CALCULATIONSTATUS_DIRTY.equals(status)) {
				priceCell.setPriceFragmentCalculationStatus(
						priceFragmentType.getPrimaryKey(), PriceCell.CALCULATIONSTATUS_INPROCESS);

				if (formulaCell != null)
					formula = formulaCell.getFormula(priceFragmentType);

				if (formula == null) {
					priceCell.getPrice().setAmount(priceFragmentType, 0);
				}
				else {
					Context context = Context.enter();
					try {
						// Scriptable scope = context.initStandardObjects();
						Scriptable scope = new ImporterTopLevel(context);

						String importPackages = getImportPackages();
						if (importPackages != null)
							formula = "importPackage(" + importPackages + ");\n" + formula;

						String importClasses = getImportClasses();
						if (importClasses != null)
							formula = "importClass(" + importClasses + ");\n" + formula;

						if (logger.isDebugEnabled()) {
							Object oldCell = ScriptableObject.getProperty(scope, "cell");
							if (oldCell == null)
								logger.debug("calculatePriceCell (" + absolutePriceCoordinate + "): oldCell is null");
							else
								logger.debug("calculatePriceCell (" + absolutePriceCoordinate + "): oldCell: " + oldCell);
						} // if (logger.isDebugEnabled()) {

						CellReflector cell = createCellReflector(
//								this, packageProductType,
								absolutePriceCoordinate, nestedProductTypeLocal
								);
						Object js_cell = Context.javaToJS(cell, scope);
						ScriptableObject.putProperty(scope, "cell", js_cell);
						Object result = context.evaluateString(scope, formula, absolutePriceCoordinate.toString(), 1, null);
						long res = 0;
						if (result instanceof Integer)
							res = ((Integer)result).longValue();
						else if (result instanceof Long)
							res = ((Long)result).longValue();
						else if (result instanceof Number)
							res = Math.round(((Number)result).doubleValue());
						else if (Undefined.instance == result)
							throw new MissingResultException(absolutePriceCoordinate, "The formula does not return a result! To return a result, simply write it in the last line of the formula!");
						else
							throw new InvalidResultException(absolutePriceCoordinate, "The formula returns a result which is not a number! The last line in the formula is the result. Check, whether this is what you want to return.");

// Note: To multiply here doesn't work, because the quantity would then be multiplicated multiple times.
// Thus, within the scope of one ProductType, the prices are always for ONE item - even if there are multiple
// in the package. Marco.
//						res = packagedProductType.getQuantity() * res;

						if (logger.isDebugEnabled())
							logger.debug("calculatePriceCell (" + absolutePriceCoordinate + "): result=" + res + " writingToPriceCell: " + priceCell);

						priceCell.getPrice().setAmount(priceFragmentType, res);
					} finally {
						Context.exit();
					}
				}

				priceCell.setPriceFragmentCalculationStatus(
						priceFragmentType.getPrimaryKey(), PriceCell.CALCULATIONSTATUS_CLEAN);

				return;
			} // status is invalid => perform calculation

			throw new IllegalStateException("PriceCell has invalid status!");
		} catch (PriceCalculationException x) {
			logger.error("PriceCalculationException at original " + x.getAbsolutePriceCoordinate());
			logger.error("PriceCalculationException at now " + absolutePriceCoordinate);
			logger.error("Formula:\n------\n" + formula + "\n------");
			logger.error("Exception", x);
			throw x;
		} catch (Exception x) {
			logger.error("PriceCalculationException at " + absolutePriceCoordinate);
			logger.error("Formula:\n------\n" + formula + "\n------");
			logger.error("Exception", x);
			throw new PriceCalculationException(absolutePriceCoordinate, x);
		}
	}

}
