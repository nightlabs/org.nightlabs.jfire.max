/*
 * Created 	on Mar 5, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.accounting.book.fragmentbased;

import java.util.Map;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.book.MoneyFlowMapping;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.ArticlePrice;

/**
 * Defines a mapping of ProductType, PriceFragmentTypes 
 * and a PackageType (package-product, inner-product) to an Account. 
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.book.MoneyFlowMapping"
 *		detachable="true"
 *		table="JFireTrade_PFMoneyFlowMapping"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class PFMoneyFlowMapping extends MoneyFlowMapping {

	/**
	 * 
	 */
	protected PFMoneyFlowMapping() {
		super();
	}
	
	public PFMoneyFlowMapping(String organisationID, int moneyFlowMappingID) {
		super(organisationID, moneyFlowMappingID);
	}

	public PFMoneyFlowMapping(String organisationID, int moneyFlowMappingID, ProductType productType, String packageType, PriceFragmentType priceFragmentType, Currency currency) {
		this(organisationID, moneyFlowMappingID);
		setPackageType(packageType);
		setProductType(productType);
		setPriceFragmentType(priceFragmentType);
		setCurrency(currency);
	}

	public PFMoneyFlowMapping(String organisationID, int moneyFlowMappingID, String productTypePK, String packageType, String priceFragmentTypePK, String currencyID) {
		this(organisationID, moneyFlowMappingID);		
		setProductTypePK(productTypePK);
		setPackageType(packageType);
		setPriceFragmentTypePK(priceFragmentTypePK);
		setCurrencyID(currencyID);
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String priceFragmentTypePK;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceFragmentType priceFragmentType;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String ownerPK;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String sourceOrganisationID;
	
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
		setPriceFragmentTypePK(priceFragmentType.getPrimaryKey());
	}
	

	/**
	 * @return Returns the priceFragmentTypePK.
	 */
	public String getPriceFragmentTypePK() {
		return priceFragmentTypePK;
	}
	

	/**
	 * @param priceFragmentTypePK The priceFragmentTypePK to set.
	 */
	public void setPriceFragmentTypePK(String priceFragmentTypePK) {
		this.priceFragmentTypePK = priceFragmentTypePK;
	}

	/**
	 * @return Returns the ownerPK.
	 */
	public String getOwnerPK() {
		return ownerPK;
	}

	/**
	 * @param ownerPK The ownerPK to set.
	 */
	public void setOwnerPK(String ownerPK) {
		this.ownerPK = ownerPK;
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

	public String getMappingConditionKey(ProductType productType) {
		return priceFragmentTypePK; 
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

	public long getArticlePriceDimensionAmount(Map dimensionValues, ArticlePrice articlePrice) {
		return articlePrice.getPriceFragment(
				(String)dimensionValues.get(PriceFragmentDimension.MONEY_FLOW_DIMENSION_ID), 
				true
			).getAmountAbsoluteValue();
	}

	public void validateMapping() {
		setPriceFragmentType((PriceFragmentType)getPersistenceManager().getObjectById(PriceFragmentType.primaryKeyToPriceFragmentTypeID(getPriceFragmentTypePK())));
	}	
	
	public void addMappingsToMap(ProductType productType, Map resolvedMappings) {
		// TODO: Add here instance multiple times for wildcards
		getProductType();
		getAccount();
		validate();
		resolvedMappings.put(
				simulateMappingKeyPartForProductType(productType)+
				productType.getOwner().getPrimaryKey() + "/" + productType.getOrganisationID() + "/" + priceFragmentTypePK, 
				this
			);
	}
	
	public boolean matches(ProductType productType, String packageType) {
		if (getProductTypePK().equals(productType.getPrimaryKey()) && 
				getPackageType().equals(packageType))
		{
			if (getOwnerPK() == null)
				return (getSourceOrganisationID() == null) 
					? 
						true 
					: 
						productType.getOrganisationID().equals(getSourceOrganisationID());
			else
				return (getSourceOrganisationID() == null) 
					? 
						productType.getOwner().getPrimaryKey().equals(getOwnerPK())
					:
						productType.getOwner().getPrimaryKey().equals(getOwnerPK()) &&
						productType.getOrganisationID().equals(getSourceOrganisationID());
		}
		return false;
	}
	
}
