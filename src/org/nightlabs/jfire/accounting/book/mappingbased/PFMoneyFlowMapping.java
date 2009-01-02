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

import java.util.Map;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.jfire.trade.LegalEntity;

/**
 * Defines a mapping of ProductType, PriceFragmentTypes
 * and a PackageType (package-product, inner-product) to an Account.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowMapping"
 *		detachable="true"
 *		table="JFireTrade_PFMoneyFlowMapping"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.fetch-group name="MoneyFlowMapping.allDimensions" fetch-groups="default" fields="owner, priceFragmentType, sourceOrganisationID"
 */
public class PFMoneyFlowMapping extends MoneyFlowMapping
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PFMoneyFlowMapping() { }
	
	public PFMoneyFlowMapping(String organisationID, long moneyFlowMappingID) {
		super(organisationID, moneyFlowMappingID);
	}

	public PFMoneyFlowMapping(String organisationID, long moneyFlowMappingID, ProductType productType, String packageType, PriceFragmentType priceFragmentType, Currency currency) {
		this(organisationID, moneyFlowMappingID);
		setPackageType(packageType);
		setProductType(productType);
		setPriceFragmentType(priceFragmentType);
		setCurrency(currency);
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceFragmentType priceFragmentType;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String sourceOrganisationID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LegalEntity owner;
	
	/**
	 * @return Returns the priceFragmentType.
	 */
	public PriceFragmentType getPriceFragmentType() {
		return priceFragmentType;
	}
	

	/**
	 * @param priceFragmentType The priceFragmentType to set.
	 */
	public void setPriceFragmentType(PriceFragmentType priceFragmentType) {
		this.priceFragmentType = priceFragmentType;
	}
	

	/**
	 * @return Returns the sourceOrganisationID.
	 */
	public String getSourceOrganisationID() {
		return sourceOrganisationID;
	}

	/**
	 * @param sourceOrganisationID The sourceOrganisationID to set.
	 */
	public void setSourceOrganisationID(String sourceOrganisationID) {
		this.sourceOrganisationID = sourceOrganisationID;
	}

	@Override
	public String getMappingConditionKey(ProductType productType) {
		return priceFragmentType.getPrimaryKey();
	}
	
	public LegalEntity getOwner() {
		return owner;
	}
	
	public void setOwner(LegalEntity owner) {
		this.owner = owner;
	}
	
	/**
	 * @return
	 */
	public static String getMappingKey(
			String productTypePK,
			String packageType,
			String currencyID,
			String ownerPK,
			String sourceOrganisationID,
			String priceFragmentTypePK
		)
	{
		return MoneyFlowMapping.getMappingKey(productTypePK, packageType, currencyID)+"/"+ownerPK+"/"+sourceOrganisationID+"/"+priceFragmentTypePK;
	}

	@Override
	public long getArticlePriceDimensionAmount(Map dimensionValues, ArticlePrice articlePrice) {
		return articlePrice.getPriceFragment(
				(String)dimensionValues.get(PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID),
				true
			).getAmount(); // .getAmountAbsoluteValue();
	}

	@Override
	public void validateMapping() {
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.nightlabs.jfire.accounting.book.mappingbased.MoneyFlowMapping#addMappingsToMap(org.nightlabs.jfire.store.ProductType, java.util.Map)
	 */
	@Override
	public void addMappingsToMap(ProductType productType, Map<String, MoneyFlowMapping> resolvedMappings) {
		// TODO: Add here instance multiple times for wildcards
		resolvedMappings.put(
				simulateMappingKeyPartForProductType(productType)+
				productType.getOwner().getPrimaryKey() + "/" + productType.getOrganisationID() + "/" + priceFragmentType.getPrimaryKey(),
				this
			);
	}
	
	@Override
	public boolean matches(ProductType productType, String packageType) {
		if (getProductTypePK().equals(productType.getPrimaryKey()) &&
				getPackageType().equals(packageType))
		{
			if (getOwner() == null)
				return (getSourceOrganisationID() == null)
					?
						true
					:
						productType.getOrganisationID().equals(getSourceOrganisationID());
			else
				return (getSourceOrganisationID() == null)
					?
						productType.getOwner().getPrimaryKey().equals(getOwner().getPrimaryKey())
					:
						productType.getOwner().getPrimaryKey().equals(getOwner().getPrimaryKey()) &&
						productType.getOrganisationID().equals(getSourceOrganisationID());
		}
		return false;
	}
	
}
