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
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.ProductType;

/**
 * An instance of this class is published as the variable "cell" into
 * the JavaScript for formula evaluation.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class CellReflector
{
	private static final Logger logger = Logger.getLogger(CellReflector.class);

	public static class ResolvedPriceCell {
		public ResolvedPriceCell(NestedProductTypeLocal nestedProductTypeLocal, PriceCell priceCell) {
			this.nestedProductTypeLocal = nestedProductTypeLocal;
			this.priceCell = priceCell;
		}
		public NestedProductTypeLocal nestedProductTypeLocal;
		public PriceCell priceCell;
	}

	private PriceCalculator priceCalculator;
	private ProductType packageProductType;

	public CellReflector(
			PriceCalculator priceCalculator,
			ProductType packageProductType,
			IAbsolutePriceCoordinate coordinate,
			NestedProductTypeLocal nestedProductTypeLocal)
	{
		if (priceCalculator == null)
			throw new NullPointerException("priceCalculator");

		if (packageProductType == null)
			throw new NullPointerException("packageProductType");

		if (coordinate == null)
			throw new NullPointerException("coordinate");

		if (nestedProductTypeLocal == null)
			throw new NullPointerException("nestedProductTypeLocal");

		this.priceCalculator = priceCalculator;
		this.packageProductType = packageProductType;
		this.coordinate = coordinate;
		this.coordinate.assertAllDimensionValuesAssigned();
		this.nestedProductTypeLocal = nestedProductTypeLocal;
		this.productType = nestedProductTypeLocal.getInnerProductTypeLocal().getProductType();
	}

	/**
	 * The absolute coordinate of the cell.
	 */
	private IAbsolutePriceCoordinate coordinate;

	/**
	 * The NestedProductTypeLocal in which we are. <tt>nestedProductTypeLocal.innerProductType</tt>
	 * is the same as {@link #productType}
	 */
	private NestedProductTypeLocal nestedProductTypeLocal;
	/**
	 * The ProductType in which we are. This is the same as <tt>nestedProductTypeLocal.innerProductType</tt>.
	 * @see #nestedProductTypeLocal;
	 */
	private ProductType productType;

	/**
	 * @return Returns the priceCalculator.
	 */
	public PriceCalculator getPriceCalculator()
	{
		return priceCalculator;
	}
	/**
	 * @return Returns the packageProductType.
	 */
	public ProductType getPackageProductType()
	{
		return packageProductType;
	}
	/**
	 * @return Returns the coordinate.
	 */
	public IAbsolutePriceCoordinate getCoordinate()
	{
		return coordinate;
	}
	/**
	 * @return Returns the nestedProductTypeLocal in which this cell is located.
	 * @see #getProductType();
	 * @see #productType
	 */
	public NestedProductTypeLocal getNestedProductType()
	{
		return nestedProductTypeLocal;
	}
	/**
	 * The result of this method is the same as <tt>getNestedProductType().getInnerProductType()</tt>
	 *
	 * @return Returns the productType in which this cell is located.
	 * @see #getNestedProductType()
	 * @see #nestedProductTypeLocal
	 */
	public ProductType getProductType()
	{
		return productType;
	}

	/**
	 * This is a convenience method.
	 *
	 * @return The same as <tt>getNestedProductType().getQuantity()</tt>
	 * @see #getNestedProductType()
	 */
	public int getQuantity()
	{
		return getNestedProductType().getQuantity();
	}

	protected IAbsolutePriceCoordinate createAbsolutePriceCoordinate(Object ... dimensionValues)
	{
		return new AbsolutePriceCoordinate(dimensionValues);
	}

	/**
	 * This method calls <tt>resolvePriceCells(..)</tt> and sums all values.
	 *
	 * @param dimensionValues either instances of various JDO-object-IDs referencing some parameters or
	 *		an instance of {@link IAbsolutePriceCoordinate}.
	 * @return the sum of all price cells referenced by the given <code>dimensionValues</code>.
	 * @throws PriceCalculationException if an exception occurs during price calculation.
	 */
	public long resolvePriceCellsAmount(Object ... dimensionValues)
	throws PriceCalculationException
	{
		// To workaround the bug https://www.jfire.org/modules/bugs/view.php?id=1062 I renamed the
		// overloaded method resolvePriceCellsAmount(IAbsolutePriceCoordinate) to
		// internal_resolvePriceCellsAmount(IAbsolutePriceCoordinate) and added the following
		// case differentiations. Marco.
		if (dimensionValues == null)
			return 0;

		if (dimensionValues.length == 1 && (dimensionValues[0] instanceof IAbsolutePriceCoordinate))
			return internal_resolvePriceCellsAmount((IAbsolutePriceCoordinate) dimensionValues[0]);

		return internal_resolvePriceCellsAmount(createAbsolutePriceCoordinate(dimensionValues));
	}

	/**
	 * This method calls <tt>resolvePriceCells(..)</tt> and sums all values.
	 *
	 * @param address This is a relative address. All fields of this
	 *		<tt>IAbsolutePriceCoordinate</tt> which are <tt>null</tt> are
	 *		extended to be the current cell's coordinate.
	 *
	 * @see #resolvePriceCells(IAbsolutePriceCoordinate)
	 */
	private long internal_resolvePriceCellsAmount(IAbsolutePriceCoordinate address)
	throws PriceCalculationException
	{
		if (logger.isDebugEnabled())
			logger.debug("internal_resolvePriceCellsAmount (" + address + "): enter");

		Collection<ResolvedPriceCell> resolvedPriceCells = resolvePriceCells(address);
//				_customerGroupPK,
//				_tariffPK,
//				_currencyID,
//				_productTypePK,
//				_priceFragmentTypePK);

		String priceFragmentTypePK = address.getPriceFragmentTypePK() != null ?
				address.getPriceFragmentTypePK() : coordinate.getPriceFragmentTypePK();

		if (resolvedPriceCells.isEmpty())
			return 0;

		long sumAmount = 0;
		for (ResolvedPriceCell resolvedPriceCell : resolvedPriceCells) {
			long amount = resolvedPriceCell.priceCell.getPrice().getAmount(priceFragmentTypePK);
//If we reference a cell within the same ProductType, we take the simple result.
//We only multiply the amount with the quantity, if the cell is in a different
//ProductType.
			if (!this.coordinate.getProductTypePK().equals(resolvedPriceCell.nestedProductTypeLocal.getInnerProductTypePrimaryKey())) {
				amount *= resolvedPriceCell.nestedProductTypeLocal.getQuantity();
			}
			sumAmount += amount;
		}

		if (logger.isDebugEnabled())
			logger.debug("internal_resolvePriceCellsAmount (" + address + "): sumAmount=" + sumAmount);

		return sumAmount;
	}

//	/**
//	 * This method finds all <tt>PriceCell</tt> s that match the given criteria.
//	 * Because the <tt>productPK</tt> does not need to point to a <tt>ProductInfo</tt>
//	 * in the package but can point to an achestor, it might happen that multiple
//	 * <tt>ProductInfo</tt> s in the package inherit the defined <tt>ProductInfo</tt>.
//	 * Thus, this method returns a <tt>Collection</tt> of <tt>PriceCell</tt>.
//	 * <p>
//	 * The <tt>PriceCell</tt> s are calculated and valid in case they come from a
//	 * <tt>FormulaPriceConfig</tt> (in this case they come out of the associated
//	 * result-<tt>StablePriceConfig</tt>). Note, that only the <tt>PriceFragment</tt>s are
//	 * calculated which match the given <tt>priceFragmentTypePK</tt>! Other <tt>PriceFragment</tt>s
//	 * are probably wrong and you should not access them!
//	 * <p>
//	 * Every parameter can be <tt>null</tt>. If a parameter is <tt>null</tt>,
//	 * it is set to the value in <tt>CellReflector.coordinate</tt> - means the current
//	 * cell itself.
//	 * <p>
//	 * If this method references the current <tt>PriceCell</tt> itself <b>directly</b>
//	 * with the current <tt>priceFragmentTypePK</tt>,
//	 * this cell is filtered out - means <b>no</b> <tt>CircularReferenceException</tt>
//	 * is thrown. This allows a cell to reference e.g. the absolute root product and
//	 * therefore make itself dependent on all other products in the same package.
//	 * <p>
//	 * If the cell is <b>indirectly<b> referencing itself (means a cell on which this cell
//	 * is dependent, depends on this cell), a <tt>CircularReferenceException</tt>
//	 * is thrown.
//	 *
//	 * @param customerGroupPK
//	 * @param saleModePK
//	 * @param tariffPK
//	 * @param categoryPK
//	 * @param currencyID
//	 * @param productPK
//	 * @param priceFragmentTypePK
//	 *
//	 * @return Returns a <tt>Collection</tt> of {@link ResolvedPriceCell}. Never <tt>null</tt>, but the <tt>Collection</tt> may be empty.
//	 * @throws ModuleException
//	 */
//	public Collection resolvePriceCells(
//			String _customerGroupPK,
//			String _tariffPK,
//			String _currencyID,
//			String _productTypePK,
//			String _priceFragmentTypePK)
//	throws ModuleException
//	{
//		// Create a relative price coordinate to search
//		// inside all matching ProductTypes
//		PriceCoordinate searchedCoordinate = new PriceCoordinate(
//				coordinate,
//				_customerGroupPK,
//				_tariffPK,
//				_currencyID);
//
//		return resolvePriceCells(searchedCoordinate, _productTypePK, _priceFragmentTypePK);
//	}

	protected IAbsolutePriceCoordinate createAbsolutePriceCoordinate(IAbsolutePriceCoordinate address)
	{
		return new AbsolutePriceCoordinate(coordinate, address);
	}

	protected IPriceCoordinate createLocalPriceCoordinate(IPriceCoordinate address)
	{
		return new PriceCoordinate(coordinate, address);
	}

	/**
	 * This method finds all <tt>PriceCell</tt> s that match the given criteria.
	 * Because the <tt>address.productTypePK</tt> does not need to point to a
	 * <tt>ProductType</tt>
	 * in the package but can point to an anchestor, it might happen that multiple
	 * <tt>ProductType</tt> s in the package inherit the defined <tt>ProductType</tt>.
	 * Thus, this method returns a <tt>Collection</tt> of <tt>PriceCell</tt>.
	 * <p>
	 * The <tt>PriceCell</tt> s are calculated and valid in case they come from a
	 * <tt>FormulaPriceConfig</tt> (in this case they come out of the associated
	 * result-<tt>StablePriceConfig</tt>). Note, that only the <tt>PriceFragment</tt>s are
	 * calculated which match the given <tt>priceFragmentTypePK</tt>! Other <tt>PriceFragment</tt>s
	 * are probably wrong and you should not access them!
	 * <p>
	 * Every parameter can be <tt>null</tt>. If a parameter is <tt>null</tt>,
	 * it is set to the value in <tt>CellReflector.coordinate</tt> - means the current
	 * cell itself.
	 * <p>
	 * If this method references the current <tt>PriceCell</tt> itself <b>directly</b>
	 * with the current <tt>priceFragmentTypePK</tt>,
	 * this cell is filtered out - means <b>no</b> <tt>CircularReferenceException</tt>
	 * is thrown. This allows a cell to reference e.g. the absolute root product and
	 * therefore make itself dependent on all other products in the same package (but
	 * itself silently excluded).
	 * <p>
	 * If the cell is <b>indirectly<b> referencing itself (means a cell on which this cell
	 * is dependent, depends back on this cell), a <tt>CircularReferenceException</tt>
	 * is thrown.
	 *
	 * @param searchedCoordinate
	 *
	 * @return Returns a <tt>Collection</tt> of {@link ResolvedPriceCell}. Never <tt>null</tt>, but the <tt>Collection</tt> may be empty.
	 * @throws ModuleException
	 */
	public Collection<ResolvedPriceCell> resolvePriceCells(
			IAbsolutePriceCoordinate address)
	throws PriceCalculationException
	{
		if (logger.isDebugEnabled())
			logger.debug("resolvePriceCells (" + address + "): enter");

		priceCalculator.getStopwatch().start(StopwatchConstants.cellReflector_resolvePriceCells);
		try {
			Collection<ResolvedPriceCell> priceCells = new LinkedList<ResolvedPriceCell>();

			IAbsolutePriceCoordinate absoluteCoordinate = createAbsolutePriceCoordinate(address);
			absoluteCoordinate.assertAllDimensionValuesAssigned();

			IPriceCoordinate localPriceCoordinate = createLocalPriceCoordinate(address);
			localPriceCoordinate.assertAllDimensionValuesAssigned();

			String productTypePK = absoluteCoordinate.getProductTypePK();
			String priceFragmentTypePK = absoluteCoordinate.getPriceFragmentTypePK();
	//		String productTypePK = _productTypePK != null ? _productTypePK : coordinate.getProductTypePK();
	//		String priceFragmentTypePK = _priceFragmentTypePK != null ? _priceFragmentTypePK : coordinate.getPriceFragmentTypePK();

			// Find all productPKs that inherit the searched one (or are it) and are packaged
			// in the current package.
			List<String> foundProductTypePKs = priceCalculator.resolvableProductTypes.get(productTypePK);
			if (foundProductTypePKs == null)
				return priceCells;

			if (logger.isDebugEnabled())
				logger.debug("resolvePriceCells (" + address + "): foundProductTypePKs.size=" + foundProductTypePKs.size());

			PriceFragmentType priceFragmentType = packageProductType.getInnerPriceConfig().getPriceFragmentType(priceFragmentTypePK, true);

			for (String foundProductTypePK : foundProductTypePKs) {
				NestedProductTypeLocal packagedProductType = priceCalculator.virtualPackagedProductTypes.get(foundProductTypePK);

				PriceCell priceCell = null;
				// filter the cell itself out if everything is identical
				if (!foundProductTypePK.equals(this.getCoordinate().getProductTypePK()) ||
						!localPriceCoordinate.equals(this.getCoordinate()) ||
						!priceFragmentTypePK.equals(this.getCoordinate().getPriceFragmentTypePK()))
				{

	//				String innerProductTypeOrganisationID = packagedProductType.getInnerProductTypeOrganisationID();
	////				String packageProductTypeOrganisationID = packagedProductType.getPackageProductTypeOrganisationID();
	//
	//				IPriceCoordinate realLocalPriceCoordinate = localPriceCoordinate;
	////				if (!packageProductTypeOrganisationID.equals(innerProductTypeOrganisationID))
	//				if (!innerProductTypeOrganisationID.equals(localPriceCoordinate.getTariffOrganisationID()))
	//					realLocalPriceCoordinate = createMappedLocalPriceCoordinateForDifferentOrganisation(localPriceCoordinate, nestedProductTypeLocal);

	//				priceCell = priceCalculator.calculatePriceCell(
	//						packagedProductType, priceFragmentType, realLocalPriceCoordinate);
					priceCell = priceCalculator.calculatePriceCell(
							packagedProductType, priceFragmentType, localPriceCoordinate);
				}

				if (priceCell != null)
					priceCells.add(new ResolvedPriceCell(packagedProductType, priceCell));
			}

	//		if (logger.isDebugEnabled()) {
	//			logger.debug("resolvePriceCells (" + address + "): priceCells.size=" + priceCells.size());
	//			for (ResolvedPriceCell cell : priceCells) {
	//				logger.debug("resolvePriceCells (" + address + "):    cell: nestedInnerPTPK=" + cell.nestedProductTypeLocal.getInnerProductTypePrimaryKey() + " coordinate=" + cell.priceCell.getPriceCoordinate() + " priceFragmentTypePK="+priceFragmentTypePK+" amount=" + cell.priceCell.getPrice().getAmount(priceFragmentTypePK));
	//			}
	//		}

			return priceCells;
		} finally {
			priceCalculator.getStopwatch().stop(StopwatchConstants.cellReflector_resolvePriceCells);
		}
	}

//	protected IPriceCoordinate createMappedLocalPriceCoordinateForDifferentOrganisation(IPriceCoordinate priceCoordinate, NestedProductTypeLocal nestedProductTypeLocal)
//	{
//		TariffID orgTariffID = TariffID.create(priceCoordinate.getTariffPK());
//		TariffID newTariffID = priceCalculator.getTariffMapper().getTariffIDForProductType(orgTariffID, nestedProductTypeLocal.getInnerProductTypeOrganisationID(), true);
//		IPriceCoordinate res = createLocalPriceCoordinate(priceCoordinate);
//		res.setTariffPK(Tariff.getPrimaryKey(newTariffID.organisationID, newTariffID.tariffID));
//		return res;
//	}
}
