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
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.util.Utils;

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
 *
 * @jdo.create-objectid-class field-order="organisationID, priceConfigID"
 *
 * @jdo.fetch-group name="PriceConfig.currencies" fields="currencies"
 * @jdo.fetch-group name="PriceConfig.name" fields="name"
 * @jdo.fetch-group name="PriceConfig.priceFragmentTypes" fields="priceFragmentTypes"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fields="currencies, name, priceFragmentTypes"
 */
public abstract class PriceConfig implements Serializable, StoreCallback, IPriceConfig
{
	public static final String FETCH_GROUP_CURRENCIES = "PriceConfig.currencies";
	public static final String FETCH_GROUP_NAME = "PriceConfig.name";
	public static final String FETCH_GROUP_PRICE_FRAGMENT_TYPES = "PriceConfig.priceFragmentTypes";

	public static long createPriceConfigID() {
		return IDGenerator.nextID(PriceConfig.class);
	}
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID = null;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long priceConfigID = -1;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="priceConfig"
	 */
	private PriceConfigName name;

//	protected PriceConfig extendedPriceConfig = null;

	protected PriceConfig() { }
	public PriceConfig(String organisationID, long priceConfigID)
	{
		if (organisationID == null)
			throw new NullPointerException("organisationID must not be null!");
		if (priceConfigID < 0)
			throw new IllegalArgumentException("priceConfigID < 0!");

		this.organisationID = organisationID;
		this.priceConfigID = priceConfigID;
		this.primaryKey = getPrimaryKey(organisationID, priceConfigID);
		this.name = new PriceConfigName(this);
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
	public long getPriceConfigID()
	{
		return priceConfigID;
	}
	public static String getPrimaryKey(String organisationID, long priceConfigID)
	{
		if (organisationID == null)
			throw new NullPointerException("organisationID must not be null!");
		if (priceConfigID < 0)
			throw new IllegalArgumentException("priceConfigID < 0!");

		return organisationID + '/' + Long.toHexString(priceConfigID);
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
	 *
	 * @jdo.join
	 */
	private Map currencies = new HashMap();

	public Collection<Currency> getCurrencies()
	{
		return currencies.values();
	}

	/**
	 * @param currency The Currency to add.
	 *
	 * @see #beginAdjustParameters()
	 * @see #endAdjustParameters()
	 */
	public boolean addCurrency(Currency currency)
	{
		return null == currencies.put(currency.getCurrencyID(), currency);
	}

	/**
	 * @return Returns the desired Currency if registered or <tt>null</tt> if the
	 * given currencyID is not known.
	 */
	public Currency getCurrency(String currencyID, boolean throwExceptionIfNotRegistered)
	{
		Currency res = (Currency) currencies.get(currencyID);
		if (res == null && throwExceptionIfNotRegistered)
			throw new IndexOutOfBoundsException("There is no Currency registered in this PriceConfig with the currencyID "+currencyID);
		return res;
	}
	public boolean containsCurrency(String currencyID)
	{
		return currencies.containsKey(currencyID);
	}
	public boolean containsCurrency(Currency currency)
	{
		return currencies.containsKey(currency.getCurrencyID());
	}
	public Currency removeCurrency(String currencyID)
	{
		return (Currency) currencies.remove(currencyID);
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
	 *
	 * @jdo.join
	 */
	private Map priceFragmentTypes = new HashMap();
	
	public Collection<PriceFragmentType> getPriceFragmentTypes()
	{
		return priceFragmentTypes.values();
	}
	public boolean addPriceFragmentType(PriceFragmentType priceFragmentType)
	{
		return null == priceFragmentTypes.put(priceFragmentType.getPrimaryKey(), priceFragmentType);
	}
	public PriceFragmentType getPriceFragmentType(String organisationID, String priceFragmentTypeID, boolean throwExceptionIfNotExistent)
	{
		return getPriceFragmentType(
				PriceFragmentType.getPrimaryKey(organisationID, priceFragmentTypeID),
				throwExceptionIfNotExistent);
	}
	public PriceFragmentType getPriceFragmentType(String priceFragmentTypePK, boolean throwExceptionIfNotExistent)
	{
		PriceFragmentType res = (PriceFragmentType) priceFragmentTypes.get(priceFragmentTypePK);
		if (throwExceptionIfNotExistent && res == null)
			throw new IllegalArgumentException("No PriceFragmentType registered with \""+priceFragmentTypePK+"\"!");
		return res;
	}
	public boolean containsPriceFragmentType(PriceFragmentType priceFragmentType)
	{
		return priceFragmentTypes.containsKey(priceFragmentType.getPrimaryKey());
	}
	public boolean containsPriceFragmentType(String priceFragmentTypePK)
	{
		return priceFragmentTypes.containsKey(priceFragmentTypePK);
	}
	public boolean containsPriceFragmentType(String organisationID, String priceFragmentTypeID)
	{
		return priceFragmentTypes.containsKey(
				PriceFragmentType.getPrimaryKey(organisationID, priceFragmentTypeID));
	}
	/**
	 * This method calls removePriceFragmentType(String priceFragmentTypePK), hence
	 * you don't need to overwrite this method to react on a remove.
	 *
	 * @see #removePriceFragmentType(String)
	 * @see PriceFragmentType#getPrimaryKey(String, String)
	 */
	public PriceFragmentType removePriceFragmentType(String organisationID, String priceFragmentTypeID)
	{
		return removePriceFragmentType(
				PriceFragmentType.getPrimaryKey(organisationID, priceFragmentTypeID));
	}
	/**
	 * @param priceFragmentTypePK The composite primary key of the PriceFragmentType to remove. 
	 * @return Returns the PriceFragmentType that has been removed or <tt>null</tt> if none was registered with the given key.
	 * 
	 * @see #removePriceFragmentType(String, String)
	 */
	public PriceFragmentType removePriceFragmentType(String priceFragmentTypePK)
	{
		return (PriceFragmentType) priceFragmentTypes.remove(priceFragmentTypePK);
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
	 * This method sets the ArticlePrice of a product in a certain context in the offerItem. 
	 * The context is defined by offerItem, which knows the product, the customer and all the
	 * other items in the offer and even the whole order.
	 * <p>
	 * The returned ArticlePrice should contain nested OfferItemPrices according to
	 * the packaging-structure of the top-level product within the offerItem
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
	 * @param article
	 * @return TODO
	 * @return The ArticlePrice for this offerItem already with nested offerItemPrices nested 
	 * accordingly to packaging-structure of the top-level offerItem product.
	 * 					
	 */
	public abstract ArticlePrice createArticlePrice(Article article);
	
	/**
	 * Is automatically set to the value returned by <tt>isDependentOnOffer()</tt>
	 * in the jdoPreStore method.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean dependentOnOffer;

	public abstract boolean isDependentOnOffer();
	
	/**
	 * Is automatically set to the value returned by <tt>requiresProductTypePackageInternal()</tt>
	 * in the jdoPreStore method.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
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

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long nextPriceID = 0;

	/**
	 * Creates a <tt>priceID</tt> by incrementing the member nextPriceID.
	 * The new ID is unique within the context of this <tt>PriceConfig</tt>
	 * (<tt>organisationID</tt> & <tt>priceConfigID</tt>).
	 *
	 * @return Returns a price id, which is unique within the context of this <tt>PriceConfig</tt>.
	 */
	public synchronized long createPriceID()
	{
		long res = nextPriceID;
		nextPriceID = res + 1;
		return res;
	}

	/**
	 * @see javax.jdo.listener.StoreCallback#jdoPreStore()
	 */
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
	
	/**
	 * @return Returns the name.
	 */
	public PriceConfigName getName()
	{
		return name;
	}

	@Override
	public int hashCode()
	{
		return Utils.hashCode(this.organisationID) + Utils.hashCode(this.priceConfigID);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;
		if (!(obj instanceof IPriceConfig)) return false;

		IPriceConfig other = (IPriceConfig) obj;

		return Utils.equals(this.organisationID, other.getOrganisationID()) && Utils.equals(this.priceConfigID, other.getPriceConfigID());
	}

//	/**
//	 * @jdo.field persistence-modifier="none"
//	 */
//	private transient int adjustParametersCounter = 0;
//	
//	/**
//	 * 
//	 */
//	public void beginAdjustParameters()
//	{
//		
//	}
//	public void endAdjustParameters()
//	{
//		
//	}
}
