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

package org.nightlabs.jfire.accounting.priceconfig;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.id.CurrencyID;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;

/**
 * A <tt>PriceConfig</tt> is a complete set of prices which can be assigned to a product by setting
 * it's <tt>ProductInfo.priceConfig</tt>. This class may be inherited thus, the PriceConfig can be
 * specialized for special products.
 * <br/><br/>
 * Because the prices are not hardlinked to a product but indirectly assigned via a PriceConfig, it is
 * no problem for multiple products to share the same <tt>PriceConfig</tt>. The property <tt>PriceConfig</tt>
 * of <tt>ProductInfo</tt> is inherited.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID"
 *		detachable="true"
 *		table="JFireTrade_PriceConfig"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, priceConfigID"
 *
 * @jdo.fetch-group name="PriceConfig.currencies" fields="currencies"
 * @jdo.fetch-group name="PriceConfig.name" fields="name"
 * @jdo.fetch-group name="PriceConfig.priceFragmentTypes" fields="priceFragmentTypes"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fields="currencies, name, priceFragmentTypes"
 */
@PersistenceCapable(
	objectIdClass=PriceConfigID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_PriceConfig")
@FetchGroups({
	@FetchGroup(
		name=PriceConfig.FETCH_GROUP_CURRENCIES,
		members=@Persistent(name="currencies")),
	@FetchGroup(
		name=PriceConfig.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=PriceConfig.FETCH_GROUP_PRICE_FRAGMENT_TYPES,
		members=@Persistent(name="priceFragmentTypes")),
	@FetchGroup(
		name="FetchGroupsPriceConfig.edit",
		members={@Persistent(name="currencies"), @Persistent(name="name"), @Persistent(name="priceFragmentTypes")})
})
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class PriceConfig implements Serializable, StoreCallback, AttachCallback, IPriceConfig
{
	private static final long serialVersionUID = 1L;
	public static final String FETCH_GROUP_CURRENCIES = "PriceConfig.currencies";
	public static final String FETCH_GROUP_NAME = "PriceConfig.name";
	public static final String FETCH_GROUP_PRICE_FRAGMENT_TYPES = "PriceConfig.priceFragmentTypes";

//	public static long createPriceConfigID() {
//		return IDGenerator.nextID(PriceConfig.class);
//	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
@PrimaryKey
@Column(length=100)
	private String organisationID = null;

//	/**
//	 * @jdo.field primary-key="true"
//	 */
//	private long priceConfigID = -1;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
@PrimaryKey
@Column(length=100)
	private String priceConfigID = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="priceConfig"
	 */
	@Persistent(
		dependent="true",
		mappedBy="priceConfig",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private PriceConfigName name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String managedBy;

//	protected PriceConfig extendedPriceConfig = null;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PriceConfig() { }

	public PriceConfig(String organisationID, String priceConfigID)
	{
//		if (organisationID == null)
//			throw new IllegalArgumentException("organisationID must not be null!");
//		if (priceConfigID < 0)
//			throw new IllegalArgumentException("priceConfigID < 0!");

		ObjectIDUtil.assertValidIDString(organisationID, "organisationID");
		ObjectIDUtil.assertValidIDString(priceConfigID, "priceConfigID");

		this.organisationID = organisationID;
		this.priceConfigID = priceConfigID;
		this.primaryKey = getPrimaryKey(organisationID, priceConfigID);
		this.name = new PriceConfigName(this);
		this.managedBy = null;
		this.currencies = new HashMap<String, Currency>();
		this.priceFragmentTypes = new HashMap<String, PriceFragmentType>();
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the priceConfigID.
	 */
	public String getPriceConfigID()
	{
		return priceConfigID;
	}
	public static String getPrimaryKey(String organisationID, String priceConfigID)
	{
		if (organisationID == null)
			throw new IllegalArgumentException("organisationID must not be null!");
//		if (priceConfigID < 0)
//			throw new IllegalArgumentException("priceConfigID < 0!");
		if (priceConfigID == null)
			throw new IllegalArgumentException("priceConfigID must not be null!");

//		return organisationID + '/' + ObjectIDUtil.longObjectIDFieldToString(priceConfigID);
		return organisationID  + '/' + priceConfigID;
	}

	public String getPrimaryKey()
	{
		return primaryKey;
	}

	/**
	 * Not every PriceConfig needs to know all Currencies. The usual behaviour of a
	 * PriceConfig is that you first add all the possible parameters for which it
	 * should then be able to deliver prices.
	 *
	 * <br/>br/>
	 *
	 * key: String currencyID<br/>
	 * value: Currency currency
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="Currency"
	 *		table="JFireTrade_PriceConfig_currencies"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_PriceConfig_currencies",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, Currency> currencies;

	public Collection<Currency> getCurrencies()
	{
		return Collections.unmodifiableCollection(currencies.values());
	}

	/**
	 * @param currency The Currency to add.
	 *
	 * @see #beginAdjustParameters()
	 * @see #endAdjustParameters()
	 */
	public boolean addCurrency(Currency currency)
	{
		if (currencies.containsKey(currency.getCurrencyID()))
			return false;

		currencies.put(currency.getCurrencyID(), currency);
		return true;
	}
	public Currency getCurrency(CurrencyID currencyID, boolean throwExceptionIfNotRegistered)
	{
		return getCurrency(currencyID.currencyID, throwExceptionIfNotRegistered);
	}
	/**
	 * @return Returns the desired Currency if registered or <tt>null</tt> if the
	 * given currencyID is not known.
	 */
	public Currency getCurrency(String currencyID, boolean throwExceptionIfNotRegistered)
	{
		Currency res = currencies.get(currencyID);
		if (res == null && throwExceptionIfNotRegistered)
			throw new IndexOutOfBoundsException("There is no Currency registered in this PriceConfig with the currencyID "+currencyID);
		return res;
	}
	@Override
	public boolean containsCurrency(String currencyID)
	{
		return currencies.containsKey(currencyID);
	}
	@Override
	public boolean containsCurrency(Currency currency)
	{
		return currencies.containsKey(currency.getCurrencyID());
	}
	@Override
	public Currency removeCurrency(String currencyID)
	{
		return currencies.remove(currencyID);
	}

	/**
	 * key: String priceFragmentTypePK<br/>
	 * value: PriceFragmentType priceFragmentType
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="PriceFragmentType"
	 *		table="JFireTrade_PriceConfig_priceFragmentTypes"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_PriceConfig_priceFragmentTypes",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, PriceFragmentType> priceFragmentTypes;

	@Override
	public Collection<PriceFragmentType> getPriceFragmentTypes()
	{
		return Collections.unmodifiableCollection(priceFragmentTypes.values());
	}
	@Override
	public boolean addPriceFragmentType(PriceFragmentType priceFragmentType)
	{
		if (priceFragmentTypes.containsKey(priceFragmentType.getPrimaryKey()))
			return false;

		priceFragmentTypes.put(priceFragmentType.getPrimaryKey(), priceFragmentType);
		return true;
	}
	@Override
	public PriceFragmentType getPriceFragmentType(String organisationID, String priceFragmentTypeID, boolean throwExceptionIfNotExistent)
	{
		return getPriceFragmentType(
				PriceFragmentType.getPrimaryKey(organisationID, priceFragmentTypeID),
				throwExceptionIfNotExistent);
	}
	@Override
	public PriceFragmentType getPriceFragmentType(String priceFragmentTypePK, boolean throwExceptionIfNotExistent)
	{
		PriceFragmentType res = priceFragmentTypes.get(priceFragmentTypePK);
		if (throwExceptionIfNotExistent && res == null)
			throw new IllegalArgumentException("No PriceFragmentType registered with \""+priceFragmentTypePK+"\"!");
		return res;
	}
	@Override
	public boolean containsPriceFragmentType(PriceFragmentType priceFragmentType)
	{
		return priceFragmentTypes.containsKey(priceFragmentType.getPrimaryKey());
	}
	@Override
	public boolean containsPriceFragmentType(String priceFragmentTypePK)
	{
		return priceFragmentTypes.containsKey(priceFragmentTypePK);
	}
	@Override
	public boolean containsPriceFragmentType(String organisationID, String priceFragmentTypeID)
	{
		return priceFragmentTypes.containsKey(
				PriceFragmentType.getPrimaryKey(organisationID, priceFragmentTypeID));
	}
	/**
	 * This method calls removePriceFragmentType(String priceFragmentTypePK), hence
	 * you don't need to overwrite this method to react on a remove.
	 *
	 * {@inheritDoc}
	 *
	 * @see #removePriceFragmentType(String)
	 * @see PriceFragmentType#getPrimaryKey(String, String)
	 */
	@Override
	public PriceFragmentType removePriceFragmentType(String organisationID, String priceFragmentTypeID)
	{
		return removePriceFragmentType(
				PriceFragmentType.getPrimaryKey(organisationID, priceFragmentTypeID));
	}
	/**
	 * {@inheritDoc}
	 *
	 * @param priceFragmentTypePK The composite primary key of the PriceFragmentType to remove.
	 * @return Returns the PriceFragmentType that has been removed or <tt>null</tt> if none was registered with the given key.
	 *
	 * @see #removePriceFragmentType(String, String)
	 */
	@Override
	public PriceFragmentType removePriceFragmentType(String priceFragmentTypePK)
	{
		return priceFragmentTypes.remove(priceFragmentTypePK);
	}

//	/**
//	 * This method must return a price of a product for a certain customer in a certain
//	 * currency.
//	 *
//	 * @param productInfo The ProductInfo representing a ProductType for which to get a price. Never null.
//	 * @param customer The customer to whom the ProductType should be sold.
//	 * @param currency The currency for which to give a price.
//	 * @return Returns an instance of ArticlePrice representing the price in a certain currency for the given
//	 *	situation. This method may return null if there is no price available for the given parameters.
//	 *	In this case, a product cannot be added to an Offer.
//	 */
//	public abstract ArticlePrice getPrice(ProductInfo productInfo, LegalEntity customer, Currency currency);

	/**
	 * This method sets the ArticlePrice of a product in a certain context in the <code>article</code>.
	 * The context is defined by <code>article</code>, which knows the product, the customer and all the
	 * other items in the offer and even the whole order.
	 * <p>
	 * The returned ArticlePrice should contain nested {@link ArticlePrice}s according to
	 * the packaging-structure of the top-level product within the <code>article</code>.
	 * </p>
	 * <p>
	 * In a simple PriceConfig, the result is usually dependent only on the customer
	 * and the product, but the PriceConfig may integrate other factors like total
	 * offer amount into price calculation, as well.
	 * </p>
	 * But be careful! This means, you can easily program instable offers which have
	 * an indefinite price! To avoid this problem, the offer is recalculated
	 * twice whenever it is validated. If the second result does not match the first,
	 * the offer is not valid and marked as not stable. Only if it is marked stable,
	 * it can be finalized (and confirmed).
	 * <p>
	 * It's urgently recommended to delegate to
	 * {@link PriceConfigUtil#createArticlePrice(IPackagePriceConfig, Article, org.nightlabs.jfire.accounting.Price)}
	 * </p>
	 *
	 * @param article
	 * @return The ArticlePrice for this <code>article</code> NOT YET with nested {@link ArticlePrice}s. They're
	 *		filled later.
	 */
	public abstract ArticlePrice createArticlePrice(Article article);

	/**
	 * Is automatically set to the value returned by <tt>isDependentOnOffer()</tt>
	 * in the jdoPreStore method.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@SuppressWarnings("unused")
	private boolean dependentOnOffer;

	@Override
	public abstract boolean isDependentOnOffer();

	/**
	 * Is automatically set to the value returned by <tt>requiresProductTypePackageInternal()</tt>
	 * in the jdoPreStore method.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	@SuppressWarnings("unused")
	private boolean requiresProductTypePackageInternal;

	/**
	 * There are implementations of <tt>PriceConfig</tt> that are useable only within
	 * product packages, because their values are indefinit (formulas depending on the
	 * siblings within the package). Therefore a <tt>ProductType</tt> is not saleable directly
	 * if such a <tt>PriceConfig</tt> is assigned.
	 *
	 * @return An implementation of <tt>PriceConfig</tt> must return <tt>true</tt>, if
	 * it's prices are dependent on the <tt>ProductType</tt>'s siblings within a package
	 * (or the package itself) and therefore, the <tt>ProductType</tt> cannot be sold outside
	 * of a package.
	 */
	public abstract boolean requiresProductTypePackageInternal();

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private long nextPriceID = 0;
//
//	/**
//	 * Creates a <tt>priceID</tt> by incrementing the member nextPriceID.
//	 * The new ID is unique within the context of this <tt>PriceConfig</tt>
//	 * (<tt>organisationID</tt> & <tt>priceConfigID</tt>).
//	 *
//	 * @return Returns a price id, which is unique within the context of this <tt>PriceConfig</tt>.
//	 */
//	public synchronized long createPriceID()
//	{
//		long res = nextPriceID;
//		nextPriceID = res + 1;
//		return res;
//	}

	public static String createPriceConfigID()
	{
		return ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(PriceConfig.class));
	}

//	public static long createPriceID(String priceConfigOrganisationID, String priceConfigID)
//	{
//		if (IDGenerator.getOrganisationID().equals(priceConfigOrganisationID))
//			return IDGenerator.nextID(Price.class, getPrimaryKey(priceConfigOrganisationID, priceConfigID));
//
//		// TODO On the long run, we must be able to obtain the ID from another organisation if we cooperate with it because
//		// we might do price calculations "abroad" - but currently, this doesn't happen.
//		throw new UnsupportedOperationException("This method does not yet support to generate a priceID when called outside its organisation!");
//	}

//	@Override
//	public long createPriceID()
//	{
//		return createPriceID(organisationID, priceConfigID);
//	}

	@Override
	public void jdoPreStore()
	{
		if (JDOHelper.isNew(this)) {
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);

			PriceFragmentType totalPFT = PriceFragmentType.getTotalPriceFragmentType(pm);
			addPriceFragmentType(totalPFT);

			this.dependentOnOffer = isDependentOnOffer();
			this.requiresProductTypePackageInternal = requiresProductTypePackageInternal();
		}
	}

	@Override
	public void jdoPreAttach() {
	}

	@Override
	public void jdoPostAttach(Object detached) {
	}

	@Override
	public PriceConfigName getName()
	{
		return name;
	}

	@Override
	public int hashCode()
	{
		return (31 * Util.hashCode(this.organisationID)) + Util.hashCode(this.priceConfigID);
	}

	@Override
	public String getManagedBy() {
		return managedBy;
	}

	@Override
	public void setManagedBy(String managedBy) {
		if (JDOHelper.isDetached(this))
			throw new IllegalStateException("setManagedBy can only be set for attached instances of " + this.getClass().getSimpleName());
		this.managedBy = managedBy;
	}

	/**
	 * Checks if the given {@link PriceConfig} is tagged with a non-<code>null</code> managed-by property.
	 * This method will throw an {@link ManagedPriceConfigModficationException}
	 * if the given {@link PriceConfig} is found to be tagged with a manged-by flag.
	 *
	 * @param pm The {@link PersistenceManager} to use.
	 * @param priceConfigID The id of the {@link PriceConfig} to check, this might also be <code>null</code> (the result of JDOHelper.getObjectId() of a new object).
	 */
	public static void assertPriceConfigNotManaged(PersistenceManager pm, PriceConfigID priceConfigID) {
		PriceConfig priceConfig = (PriceConfig) pm.getObjectById(priceConfigID);
		if (priceConfig.getManagedBy() != null)
			throw new ManagedPriceConfigModficationException(priceConfigID, priceConfig.getManagedBy());
	}

	/**
	 * Checks if the {@link PriceConfig} is tagged with a non-<code>null</code> managed-by property.
	 * <p>
	 * If the {@link PriceConfig} can't be found in the datastore <code>false</code> will be returned.
	 * This might occur if the given {@link PriceConfigID} is of a {@link PriceConfig} not yet in the given datastore,
	 * or <code>null</code>.
	 * </p>
	 *
	 * @param pm The {@link PersistenceManager} to use.
	 * @param priceConfigID The id of the {@link PriceConfig} to check, this might also be <code>null</code> (the result of JDOHelper.getObjectId() of a new object).
	 * @return <code>true</code> if the given {@link PriceConfig} is found to be tagged with the managed-by flag, <code>false</code> otherwise.
	 */
	public static boolean isPriceConfigManaged(PersistenceManager pm, PriceConfigID priceConfigID) {
		PriceConfig priceConfig = (PriceConfig) pm.getObjectById(priceConfigID);
		return priceConfig.getManagedBy() != null;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;

		IPriceConfig other = (IPriceConfig) obj;

		return Util.equals(this.organisationID, other.getOrganisationID()) && Util.equals(this.priceConfigID, other.getPriceConfigID());
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + priceConfigID + ']';
	}
}
