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

import java.io.Serializable;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.accounting.Account;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.book.mappingbased.id.MoneyFlowMappingID;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.util.Util;

/**
 * Abstract mapping to an revenue and expense accounts. This class has the productType the package-type
 * and a currency as matching-condition. Subclasses may add conditions on which money
 * should be transfered to the accounts of this mapping.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.book.mappingbased.id.MoneyFlowMappingID"
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
 * @jdo.fetch-group name="MoneyFlowMapping.revenueAccount" fields="revenueAccount"
 * @jdo.fetch-group name="MoneyFlowMapping.expenseAccount" fields="expenseAccount"
 * @jdo.fetch-group name="MoneyFlowMapping.reverseRevenueAccount" fields="reverseRevenueAccount"
 * @jdo.fetch-group name="MoneyFlowMapping.reverseExpenseAccount" fields="reverseExpenseAccount"
 *
 * @jdo.fetch-group name="MoneyFlowMapping.allDimensions" fetch-groups="default" fields="productType, currency, revenueAccount, expenseAccount, reverseRevenueAccount, reverseExpenseAccount"
 */
@PersistenceCapable(
	objectIdClass=MoneyFlowMappingID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_MoneyFlowMapping")
@FetchGroups({
	@FetchGroup(
		name=MoneyFlowMapping.FETCH_GROUP_LOCAL_ACCOUNTANT_DELEGATE,
		members=@Persistent(name="localAccountantDelegate")),
	@FetchGroup(
		name=MoneyFlowMapping.FETCH_GROUP_PRODUCT_TYPE,
		members=@Persistent(name="productType")),
	@FetchGroup(
		name=MoneyFlowMapping.FETCH_GROUP_CURRENCY,
		members=@Persistent(name="currency")),
	@FetchGroup(
		name=MoneyFlowMapping.fETCH_GROUP_REVENUE_ACCOUNT,
		members=@Persistent(name="revenueAccount")),
	@FetchGroup(
		name=MoneyFlowMapping.FETCH_GROUP_EXPENSE_ACCOUNT,
		members=@Persistent(name="expenseAccount")),
	@FetchGroup(
		name=MoneyFlowMapping.FETCH_GROUP_REVERSE_REVENUE_ACCOUNT,
		members=@Persistent(name="reverseRevenueAccount")),
	@FetchGroup(
		name=MoneyFlowMapping.FETCH_GROUP_REVERSE_EXPENSE_ACCOUNT,
		members=@Persistent(name="reverseExpenseAccount")),
	@FetchGroup(
		fetchGroups={"default"},
		name=MoneyFlowMapping.FETCH_GROUP_ALL_DIMENSIONS,
		members={@Persistent(name="productType"), @Persistent(name="currency"), @Persistent(name="revenueAccount"), @Persistent(name="expenseAccount"), @Persistent(name="reverseRevenueAccount"), @Persistent(name="reverseExpenseAccount")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class MoneyFlowMapping implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String PACKAGE_TYPE_PACKAGE = "package-outer";
	public static final String PACKAGE_TYPE_INNER = "package-inner";

	public static final String FETCH_GROUP_LOCAL_ACCOUNTANT_DELEGATE = "MoneyFlowMapping.localAccountantDelegate";
	public static final String FETCH_GROUP_PRODUCT_TYPE = "MoneyFlowMapping.productType";
	public static final String FETCH_GROUP_CURRENCY = "MoneyFlowMapping.currency";
	public static final String fETCH_GROUP_REVENUE_ACCOUNT = "MoneyFlowMapping.revenueAccount";
	public static final String FETCH_GROUP_EXPENSE_ACCOUNT = "MoneyFlowMapping.expenseAccount";
	public static final String FETCH_GROUP_REVERSE_REVENUE_ACCOUNT = "MoneyFlowMapping.reverseRevenueAccount";
	public static final String FETCH_GROUP_REVERSE_EXPENSE_ACCOUNT = "MoneyFlowMapping.reverseExpenseAccount";

	public static final String FETCH_GROUP_ALL_DIMENSIONS = "MoneyFlowMapping.allDimensions";

//	public static interface Registry {
//		public String getOrganisationID();
//		public int createMoneyFlowMappingID(); // String organisationID);
//	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
@PrimaryKey
@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long moneyFlowMappingID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProductType productType;

	/**
	 * Defines if this mapping is for the ProductType as packaged product
	 * within another product {@link #PACKAGE_TYPE_INNER} or weather it
	 * is for the productType as saleable product itself {@link #PACKAGE_TYPE_PACKAGE}.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String packageType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Currency currency;

//	/**
//	 * @jdo.field persistence-modifier="persistent" null-value="exception"
//	 */
//	private Account account;

	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
@Persistent(
	nullValue=NullValue.EXCEPTION,
	persistenceModifier=PersistenceModifier.PERSISTENT)
	private Account revenueAccount;
	/**
	 * @jdo.field persistence-modifier="persistent" null-value="exception"
	 */
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Account expenseAccount;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Account reverseRevenueAccount;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Account reverseExpenseAccount;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private LocalAccountantDelegate localAccountantDelegate;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected MoneyFlowMapping() { }

	protected MoneyFlowMapping(String organisationID, long moneyFlowMappingID) {
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
	}


	/**
	 * @return Returns the productTypePK.
	 */
	public String getProductTypePK() {
		return productType.getPrimaryKey();
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
		return currency != null ? currency.getCurrencyID() : null;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}


//	/**
//	 * @return Returns the account.
//	 */
//	public Account getAccount() {
//		return account;
//	}
//
//	/**
//	 * @param account The account to set.
//	 */
//	public void setAccount(Account account) {
//		this.account = account;
//	}
//
//	/**
//	 * @return Returns the accountPK.
//	 */
//	public String getAccountPK() {
//		return (account != null) ? account.getPrimaryKey() : null;
//	}

	/**
	 * @return The Account money should be transfered to when the ProductType of this mapping is sold.
	 */
	public Account getRevenueAccount()
	{
		return revenueAccount;
	}
	public void setRevenueAccount(Account revenueAccount)
	{
		this.revenueAccount = revenueAccount;
	}
//	public String getRevenueAccountPK() {
//		return revenueAccount == null ? null : revenueAccount.getPrimaryKey();
//	}

	/**
	 * @return The Account money should be transfered from when the ProductType of this mapping is purchased.
	 */
	public Account getExpenseAccount()
	{
		return expenseAccount;
	}
	public void setExpenseAccount(Account expenseAccount)
	{
		this.expenseAccount = expenseAccount;
	}
//	public String getExpenseAccountPK() {
//		return expenseAccount == null ? null : expenseAccount.getPrimaryKey();
//	}
	/**
	 * Get the reverse-revenue-account or <code>null</code>. If this is <code>null</code>, the booking process
	 * uses the same account as returned by {@link #getRevenueAccount()}.
	 */
	public Account getReverseRevenueAccount()
	{
		return reverseRevenueAccount;
	}
	public void setReverseRevenueAccount(Account reverseRevenueAccount)
	{
		this.reverseRevenueAccount = reverseRevenueAccount;
	}
//	public String getReverseRevenueAccountPK() {
//		return reverseRevenueAccount == null ? null : reverseRevenueAccount.getPrimaryKey();
//	}
	/**
	 * Get the reverse-expense-account or <code>null</code>. If this is <code>null</code>, the booking process
	 * uses the same account as returned by {@link #getExpenseAccount()}.
	 */
	public Account getReverseExpenseAccount()
	{
		return reverseExpenseAccount;
	}
	public void setReverseExpenseAccount(Account reverseExpenseAccount)
	{
		this.reverseExpenseAccount = reverseExpenseAccount;
	}
//	/**
//	 * Get the primary key in short notation from the reverse-expense-account or <code>null</code> if none assigned.
//	 * @see #getReverseExpenseAccount()
//	 */
//	public String getReverseExpenseAccountPK() {
//		return reverseExpenseAccount == null ? null : reverseExpenseAccount.getPrimaryKey();
//	}

	public String getOrganisationID() {
		return organisationID;
	}

	public long getMoneyFlowMappingID() {
		return moneyFlowMappingID;
	}

	public LocalAccountantDelegate getLocalAccountantDelegate() {
		return localAccountantDelegate;
	}

	public void setLocalAccountantDelegate(
			LocalAccountantDelegate localAccountantDelegate) {
		this.localAccountantDelegate = localAccountantDelegate;
	}

	/**
	 * Get an unique key for identifying this mapping, but for the given
	 * ProductType. This can be used to simulate that a mapping was
	 * defined for a different ProductType.
	 * @param productType The ProductType to generate the key with.
	 * @return An unique key for identifying this mapping
	 */
	public String simulateMappingKeyForProductType(ProductType productType) {
		return getMappingKey(productType.getPrimaryKey(), packageType, getCurrencyID())+"/"+getMappingConditionKey(productType);
	}

	protected String simulateMappingKeyPartForProductType(ProductType productType) {
		return getMappingKey(productType.getPrimaryKey(), packageType, getCurrencyID())+"/";
	}

	/**
	 * Subclasses should return an unique key for the additional condition
	 * (beside product-type, package-type and currency) that the bring
	 * to the mapping. As an example, mappings based on price-fragments
	 * would return the price-fragments primary key here.
	 *
	 * @return An unique key for the additional condition
	 */
	public abstract String getMappingConditionKey(ProductType productType);

	/**
	 * Subclasses should add themselves to the given map but with a key
	 * simulating that the mapping was made for the given ProductType
	 * and not for the ProductType defined for the mapping. Subclasses
	 * may use {@link #simulateMappingKeyPartForProductType(ProductType)}
	 * for creating the simulated key.
	 * The rest of the key should be the values of the dimensions of
	 * the mapping for the given product-type. This is used later
	 * to lookup a mapping for a given set of dimension values.
	 *
	 * @param productType The productType to simulate the mapping for.
	 * @param resolvedMappings The map to add the mapping to.
	 */
	public abstract void addMappingsToMap(ProductType productType, Map<String, MoneyFlowMapping> resolvedMappings);

	/**
	 * Get an unique key identifying a mapping.
	 *
	 * @param productTypePK The productTypes primary key of the mapping.
	 * @param packageType The package-type set for the mapping
	 * @param currencyID The currencyID of the mapping.
	 * @return An unique key identifying a mapping.
	 */
	public static String getMappingKey(
			String productTypePK,
			String packageType,
			String currencyID
		)
	{
		return productTypePK+"/"+packageType+"/"+currencyID;
	}

	/**
	 * Return the amount of money this mapping takes from the given {@link ArticlePrice}
	 * according to the given dimension values.
	 * An example is for example a mapping based on price-fragments that will extract and return
	 * the amount of that price-fragment out of the {@link ArticlePrice}.
	 * @return The amount from the given {@link ArticlePrice} this mapping is responsible for.
	 */
	// TODO: Must check for taking from multiple amounts
	public abstract long getArticlePriceDimensionAmount(Map dimensionValues, ArticlePrice articlePrice);

//	public void validate() {
//		setProductType((ProductType)getPersistenceManager().getObjectById(ProductType.primaryKeyToProductTypeID(getProductTypePK())));
//		setAccount((Account)getPersistenceManager().getObjectById(Account.primaryKeyToAnchorID(getAccountPK())));
//		setCurrency((Currency)getPersistenceManager().getObjectById(CurrencyID.create(getCurrencyID())));
//		validateMapping();
//	}

	/**
	 * Check and validate this mappings state/properties.
	 */
	public abstract void validateMapping();

	protected PersistenceManager getPersistenceManager() {
		return JDOHelper.getPersistenceManager(this);
	}
	/**
	 * Check if this mapping was made for the given {@link ProductType} and
	 * the given package-type.
	 *
	 * @param productType The {@link ProductType} to check.
	 * @param packageType The package-type to check.
	 * @return Whether this mapping matches.
	 */
	public abstract boolean matches(ProductType productType, String packageType);

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) + Util.hashCode(moneyFlowMappingID);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;

		if (!(obj instanceof MoneyFlowMapping))
			return false;

		MoneyFlowMapping o = (MoneyFlowMapping) obj;

		return Util.equals(this.organisationID, o.organisationID) && Util.equals(this.moneyFlowMappingID, o.moneyFlowMappingID);
	}
}
