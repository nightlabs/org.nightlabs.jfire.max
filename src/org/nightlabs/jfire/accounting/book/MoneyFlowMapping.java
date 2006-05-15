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

package org.nightlabs.jfire.accounting.book;

import java.io.Serializable;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.ArticlePrice;

/**
 * Abstract mapping to an Account. Subclasses can add conditions on which money
 * should be transfered to the Account of this Mapping.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.book.id.MoneyFlowMappingID"
 *		detachable="true"
 *		table="JFireTrade_MoneyFlowMapping"
 *
 * @jdo.inheritance strategy="new-table"
 * 
 * @jdo.create-objectid-class
 *		field-order="organisationID, moneyFlowMappingID"
 *
 * @jdo.fetch-group name="MoneyFlowMapping.localAccountantDelegate" fields="localAccountantDelegate"
 * @jdo.fetch-group name="MoneyFlowMapping.productType" fields="productType"
 * @jdo.fetch-group name="MoneyFlowMapping.currency" fields="currency"
 * @jdo.fetch-group name="MoneyFlowMapping.account" fields="account"
 */
public abstract class MoneyFlowMapping implements Serializable {

	public static final String PACKAGE_TYPE_PACKAGE = "package-outer";
	public static final String PACKAGE_TYPE_INNER = "package-inner";

	public static final String FETCH_GROUP_LOCAL_ACCOUNTANT_DELEGATE = "MoneyFlowMapping.localAccountantDelegate";
	public static final String FETCH_GROUP_PRODUCT_TYPE = "MoneyFlowMapping.productType";
	public static final String FETCH_GROUP_CURRENCY = "MoneyFlowMapping.currency";
	public static final String FETCH_GROUP_ACCOUNT = "MoneyFlowMapping.account";
			 
	public static interface Registry {
		public String getOrganisationID();
		public int createMoneyFlowMappingID(); // String organisationID);
	}
	
	/**
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field persistence-modifier="persistent" primary-key="true"
	 */
	private int moneyFlowMappingID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String productTypePK;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductType productType;
	
	/**
	 * Defines if this mapping is for the ProductType as packaged product
	 * within another product {@link #PACKAGE_TYPE_INNER} or weather it
	 * is for the productType as saleable product itself {@link #PACKAGE_TYPE_PACKAGE}.
	 * 
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String packageType;	
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String currencyID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Currency currency;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String accountPK;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Account account;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LocalAccountantDelegate localAccountantDelegate;
	
	/**
	 * 
	 */
	protected MoneyFlowMapping() {
		super();
	}
	
	protected MoneyFlowMapping(String organisationID, int moneyFlowMappingID) {
		super();
		this.organisationID = organisationID;
		this.moneyFlowMappingID = moneyFlowMappingID;
	}
	
	/**
	 * @return Returns the productType.
	 */
	public ProductType getProductType() {
		return productType;
	}
	

	/**
	 * @param productType The productType to set.
	 */
	public void setProductType(ProductType productType) {
		this.productType = productType;
		setProductTypePK(productType.getPrimaryKey());
	}
	

	/**
	 * @return Returns the productTypePK.
	 */
	public String getProductTypePK() {
		return productTypePK;
	}
	

	/**
	 * @param productTypePK The productTypePK to set.
	 */
	public void setProductTypePK(String productTypePK) {
		this.productTypePK = productTypePK;
	}
	

	/**
	 * 
	 * @return The packageType
	 */
	public String getPackageType() {
		return packageType;
	}

	/**
	 * Sets the packageType 
	 * @param packageType
	 */
	public void setPackageType(String packageType) {
		this.packageType = packageType;
	}
	
	public Currency getCurrency() {
		return currency;
		
	}
	
	public String getCurrencyID() {
		return currencyID;
	}

	public void setCurrencyID(String currencyID) {
		this.currencyID = currencyID;
	}
		
	public void setCurrency(Currency currency) {
		this.currency = currency;
		setCurrencyID(currency.getCurrencyID());
	}
	
	
	/**
	 * @return Returns the account.
	 */
	public Account getAccount() {
		return account;
	}	

	/**
	 * @param account The account to set.
	 */
	public void setAccount(Account account) {
		this.account = account;
		accountPK = (account == null) ? null : account.getPrimaryKey();
			
	}

	/**
	 * @return Returns the accountPK.
	 */
	public String getAccountPK() {
		return accountPK;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public int getMoneyFlowMappingID() {
		return moneyFlowMappingID;
	}
	
	/**
	 * @param accountPK The Account primary key to set
	 */
	public void setAccountPK(String accountPK) {
		this.accountPK = accountPK;
	}
	
	public LocalAccountantDelegate getLocalAccountantDelegate() {
		return localAccountantDelegate;
	}
	
	public void setLocalAccountantDelegate(
			LocalAccountantDelegate localAccountantDelegate) {
		this.localAccountantDelegate = localAccountantDelegate;
	}
	
	/**
	 * @param productType TODO
	 * @return
	 */
	public String getMappingKey(ProductType productType) {
		return getMappingKey(productTypePK, packageType, currencyID)+"/"+getMappingConditionKey(productType);
	}
	
	public String simulateMappingKeyForProductType(ProductType productType) {
		return getMappingKey(productType.getPrimaryKey(), packageType, currencyID)+"/"+getMappingConditionKey(productType);
	}
	
	protected String simulateMappingKeyPartForProductType(ProductType productType) {
		return getMappingKey(productType.getPrimaryKey(), packageType, currencyID)+"/";		
	}
	
	/**
	 * Should return a 
	 * @return
	 */
	public abstract String getMappingConditionKey(ProductType productType);
	
	public void populateResolvedMap() {
		
	}
	
	public abstract void addMappingsToMap(ProductType productType, Map<String, MoneyFlowMapping> resolvedMappings);
	
	
	public static String getMappingKey(
			String productTypePK, 
			String packageType, 
			String currencyID
		) 
	{
		return productTypePK+"/"+packageType+"/"+currencyID;
	}
	
	// TODO: Must check for taking from multiple amounts
	public abstract long getArticlePriceDimensionAmount(Map dimensionValues, ArticlePrice articlePrice);
	
	public void validate() {
		setProductType((ProductType)getPersistenceManager().getObjectById(ProductType.primaryKeyToProductTypeID(getProductTypePK())));
		setAccount((Account)getPersistenceManager().getObjectById(Account.primaryKeyToAnchorID(getAccountPK())));
		setCurrency((Currency)getPersistenceManager().getObjectById(CurrencyID.create(getCurrencyID())));
		validateMapping();
	}
	
	public abstract void validateMapping();
	
	protected PersistenceManager getPersistenceManager() {
		return JDOHelper.getPersistenceManager(this);
	}
	
	public abstract boolean matches(ProductType productType, String packageType);

}
