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

package org.nightlabs.jfire.accounting;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Value;

import org.apache.log4j.Logger;
import org.nightlabs.clone.CloneContext;
import org.nightlabs.clone.CloneableWithContext;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.id.PriceID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.util.Util;
import org.nightlabs.util.reflect.ReflectUtil;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.id.PriceID"
 *		detachable="true"
 *		table="JFireTrade_Price"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, priceID"
 *
 * @jdo.fetch-group name="Price.currency" fields="currency"
 * @jdo.fetch-group name="Price.fragments" fields="fragments"
 * @jdo.fetch-group name="Price.this" fetch-groups="default" fields="currency, fragments"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="currency, fragments"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default" fields="currency"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default" fields="currency"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default" fields="currency"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default" fields="currency"
 *
 * @jdo.fetch-group
 * 		name="FetchGroupsTrade.articleCrossTradeReplication"
 * 		fields="currency, fragments"
 */
@PersistenceCapable(
	objectIdClass=PriceID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_Price")
@FetchGroups({
	@FetchGroup(
		name=Price.FETCH_GROUP_CURRENCY,
		members=@Persistent(name="currency")),
	@FetchGroup(
		name=Price.FETCH_GROUP_FRAGMENTS,
		members=@Persistent(name="fragments")),
	@FetchGroup(
		fetchGroups={"default"},
		name=Price.FETCH_GROUP_THIS_PRICE,
		members={@Persistent(name="currency"), @Persistent(name="fragments")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsPriceConfig.edit",
		members={@Persistent(name="currency"), @Persistent(name="fragments")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInOrderEditor",
		members=@Persistent(name="currency")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInOfferEditor",
		members=@Persistent(name="currency")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInInvoiceEditor",
		members=@Persistent(name="currency")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInDeliveryNoteEditor",
		members=@Persistent(name="currency")),
	@FetchGroup(
		name="FetchGroupsTrade.articleCrossTradeReplication",
		members={@Persistent(name="currency"), @Persistent(name="fragments")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Price
	implements Serializable, CloneableWithContext
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(Price.class);

	public static final String FETCH_GROUP_CURRENCY = "Price.currency"; //$NON-NLS-1$
	public static final String FETCH_GROUP_FRAGMENTS = "Price.fragments"; //$NON-NLS-1$
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_PRICE = "Price.this"; //$NON-NLS-1$
	
	private static PriceFragmentType defaultTotalPriceFragmentType = null;
	
	/**
	 * Set the default instance of the PriceFragmentType that is used for the total price.
	 * 
	 * <p>
	 * 	<b>Important</b>: This should only be used for testing purpose! <br/>
	 * 	When writing offline testcases, the default total price fragment type cannot be retrieved from the datastore,
	 * 	hence you can use this method to always return the same offline instance for objects instantiated when running
	 * 	these tests. 
	 * </p>
	 * 
	 * @param defaultTotalType The instance that shall be used as the default total price fragment type.
	 */
	public static void setDefaultTotalPriceFragmentType(PriceFragmentType defaultTotalType)
	{
		if (defaultTotalType == null)
			throw new IllegalArgumentException("The defaultTotalPriceFragmentType must NOT be null!");
		
		synchronized (Price.class)
		{
			defaultTotalPriceFragmentType = defaultTotalType;
		}
	}
	
	/**
	 * Returns the cached default "TOTAL" PriceFragmentType.
	 * <p>
	 * 	Note: As this is only the cached type, it may be null the moment this method is called and will be set lazily when
	 * 	{@link #setAmount(long)} is called.
	 * </p>
	 * 
	 * @return the cached default "TOTAL" PriceFragmentType.
	 */
	public static PriceFragmentType getDefaultTotalPriceFragmentType()
	{
		return defaultTotalPriceFragmentType;
	}
	
	/**
	 * Returns the default total PriceFragmentType instance.
	 * <p>
	 * 	If the default PriceFragmentType has been set manually 
	 * 	(see {@link #setDefaultTotalPriceFragmentType(PriceFragmentType)}), then this instance is returned.
	 * 	If none was set explicitly, it is tried to get the default one by looking it up from the datastore with the 
	 * 	{@link PersistenceManager} of this object. <br/>
	 * 	If no PersistenceManager can be found or no default total PriceFragmentType can be located in the datastore, an
	 * 	IllegalStateException is thrown.
	 * </p>
	 * 
	 * @return the default total PriceFragmentType instance.
	 */
	private PriceFragmentType lookupDefaultTotalPriceFragmentType()
	{
		if (defaultTotalPriceFragmentType != null)
		{
			if (defaultTotalPriceFragmentType.primaryKey == null)
			{
				throw new IllegalStateException(
						"Who changed the source code and messed up the defaultTotalPriceFragmentType such that its primaryKey == null!?!"
				);
			}
			
			return defaultTotalPriceFragmentType;
		}
		
		synchronized (Price.class)
		{
			if (defaultTotalPriceFragmentType != null)
				return defaultTotalPriceFragmentType;
			
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			if (pm == null)
			{
				throw new IllegalStateException("This instance of Price ("+getPrimaryKey()+") does neither have a " +
						"PriceFragment for the PriceFragmentType \"_Total_\" nor is it attached to a datastore (=> cannot lookup " +
						"the PriceFragmentType). You must either use setAmount(PriceFragmentType, long) or call this method when " +
						"the PriceFragment already exists or when this Price is currently persistent (connected to the datastore)."
				);
			}

			pm.getFetchPlan().addGroup(FetchPlan.DEFAULT);
			defaultTotalPriceFragmentType = pm.detachCopy((PriceFragmentType) 
					pm.getObjectById(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL));

			if (defaultTotalPriceFragmentType.getPrimaryKey() == null)
				throw new IllegalStateException("How can the fucking primaryKey field of the default total price " +
				"fragment be null!?!");
			
			if (defaultTotalPriceFragmentType == null)
			{
				throw new IllegalStateException(
						"There is no default PriceFragmentType existing in the Datastore!" +
						"\n\t fragmentTypeID = " + PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL
				);
			}
		}
		
		return defaultTotalPriceFragmentType;
		
//		if (defaultTotalPriceFragmentType != null)
//			return defaultTotalPriceFragmentType;
//		
//		synchronized (Price.class)
//		{
//			if (defaultTotalPriceFragmentType != null)
//				return defaultTotalPriceFragmentType;
//			
//			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//			if (pm == null)
//				throw
//				
//			PriceFragmentType totalPriceFragmentType = (PriceFragmentType)
//			pm.getObjectById(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL);
//			
//			if (! pm.getFetchPlan().getGroups().contains(FetchPlan.DEFAULT))
//				throw new IllegalStateException("No Default FetchPlan found, although added before!");
//			
//			if (totalPriceFragmentType.getPrimaryKey() == null)
//				throw new IllegalStateException("How can the fucking primaryKey field of the default total price " +
//				"fragment be null!?!");
//			
//			defaultTotalPriceFragmentType = Util.cloneSerializable(pm.detachCopy(totalPriceFragmentType));
//			
//			if (defaultTotalPriceFragmentType.getPrimaryKey() == null)
//				throw new IllegalStateException("How can the fucking primaryKey field of the default total price " +
//				"fragment be null AFTER detachment!?!");
//			
//			if (defaultTotalPriceFragmentType == null)
//			{
//				throw new IllegalStateException(
//						"There is no default PriceFragmentType existing in the Datastore!" +
//						"\n\t fragmentTypeID = " + PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL
//				);
//			}
//		}
	}		
//		if (pm != null)
//		{
//			PriceFragmentType totalPriceFragmentType = (PriceFragmentType)
//				pm.getObjectById(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL);
//			
//			if (defaultTotalPriceFragmentType == null)
//			{
//				pm.getFetchPlan().addGroup(FetchPlan.DEFAULT);
//				defaultTotalPriceFragmentType = Util.cloneSerializable(pm.detachCopy(totalPriceFragmentType));				
//			}
//			return (PriceFragmentType) pm.getObjectById(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL);
//		}
//		
//		
//		if (defaultTotalPriceFragmentType == null)
//		{
//			throw new IllegalStateException("This instance of Price ("+getPrimaryKey()+") does neither have a " +
//					"PriceFragment for the PriceFragmentType \"_Total_\" nor is it attached to a datastore (=> cannot lookup " +
//					"the PriceFragmentType). You must either use setAmount(PriceFragmentType, long) or call this method when " +
//					"the PriceFragment already exists or when this Price is currently persistent (connected to the datastore)."
//			);
//		}
//		
//		if (defaultTotalPriceFragmentType.getPrimaryKey() == null)
//			throw new IllegalStateException(
//					"How can the fucking primaryKey field of the already detached default total" +
//					"price fragment be null!?!");
//		
//		return Util.cloneSerializable(defaultTotalPriceFragmentType);
//		
//		
//		
//	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

//	/**
//	 * @jdo.field primary-key="true"
//	 * @jdo.column length="100"
//	 */
//	private String priceConfigID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long priceID = -1;
	/////// end PK /////


	/////// begin normal fields ///////
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Currency currency;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long amount = 0;

	/**
	 * key: String priceFragmentTypePK<br/>
	 * value: PriceFragment priceFragment
	 * <p>
	 * A total price can contain various fragments that need to be known for
	 * legal reasons. E.g. the VAT is managed as a fragment with a certain key
	 * like "vat-de-19" or "vat-de-7". Other data that one trade partner
	 * wants/allows the other to be known might be transferred in here as well
	 * E.g. the system fees might be declared like this. The price fragments
	 * declared here are used by an Accountant to book the right amounts on the
	 * right Accounts.
	 * <p>
	 * Note, that PriceFragments can overlap or be incomplete and therefore their sum is NOT
	 * the <tt>Price.amount</tt> (or only by accident).
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="PriceFragment"
	 *		dependent-value="true"
	 *		mapped-by="price"
	 *
	 * @jdo.key mapped-by="priceFragmentTypePK"
	 */
	@Persistent(mappedBy="price")
	@Key(mappedBy="priceFragmentTypePK")
	@Value(dependent="true")
	private Map<String, PriceFragment> fragments;

	/**
	 * A Price can contain virtual fragments. Virtual fragments
	 * are not stored persitently, they are for example calculated/used
	 * by the book process.
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	@NotPersistent
	private final Map<String, PriceFragment> virtualFragments = new HashMap<String, PriceFragment>();

	/////// end normal fields ////////

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected Price() { }

	/**
	 * This constructor is used to create a price within a TariffPrice object
	 * in the general price grid.
	 *
	 * @param tariffPrice
	 * @param currency
	 */
	public Price(
			String organisationID,
//			String priceConfigID,
			long priceID,
			Currency currency
	)
	{
		if (priceID < 0) {
			if (!"".equals(organisationID)) //$NON-NLS-1$
				throw new IllegalArgumentException("organisationID must be empty if priceID < 0! organisationID: " + organisationID); //$NON-NLS-1$

//			if (!"".equals(priceConfigID)) //$NON-NLS-1$
//				throw new IllegalArgumentException("priceConfigID must be empty if priceID < 0! priceConfigID: " + priceConfigID); //$NON-NLS-1$
		}
		else {
			if (!ObjectIDUtil.isValidIDString(organisationID))
				throw new IllegalArgumentException("organisationID must a valid ID, if priceID >= 0! organisationID: " + organisationID); //$NON-NLS-1$

//			if (!ObjectIDUtil.isValidIDString(priceConfigID))
//				throw new IllegalArgumentException("priceConfigID must a valid ID, if priceID >= 0! priceConfigID: " + priceConfigID); //$NON-NLS-1$
		}

		this.organisationID = organisationID;
//		this.priceConfigID = priceConfigID;
		this.priceID = priceID;
		if (currency == null)
			throw new NullPointerException("currency"); //$NON-NLS-1$
		this.currency = currency;
		this.fragments = new HashMap<String, PriceFragment>();
	}

	public static String getPrimaryKey(String organisationID, long priceID)
	{
		return organisationID + '/' + ObjectIDUtil.longObjectIDFieldToString(priceID);
	}

	public String getPrimaryKey()
	{
		return getPrimaryKey(organisationID, priceID);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
//	/**
//	 * @return Returns the priceConfigID.
//	 */
//	public String getPriceConfigID()
//	{
//		return priceConfigID;
//	}
	/**
	 * @return Returns the priceID.
	 */
	public long getPriceID()
	{
		return priceID;
	}
	/**
	 * @return Returns the currency.
	 */
	public Currency getCurrency()
	{
		return currency;
	}

	/**
	 * Returns ALL PriceFragments including the virtual ones.
	 * @see #getFragments(boolean)
	 * @return Returns a Collection of PriceFragment.
	 */
	public Collection<PriceFragment> getFragments()
	{
		return getFragments(true);
	}

	/**
	 * Returns the list of PriceFragments where the caller can
	 * decide whether to include the virtual fragments or not.
	 *
	 * @param includeVirtual Whether to include the virtual fragments.
	 * @return A Colleciton of PriceFragments.
	 */
	public Collection<PriceFragment> getFragments(boolean includeVirtual) {
		HashSet<PriceFragment> result = new HashSet<PriceFragment>(fragments.values());
		if (includeVirtual)
			result.addAll(virtualFragments.values());
		return result;
	}

	/**
	 * @return The list of virtual (non-persistent) PriceFragments.
	 */
	public Collection<PriceFragment> getVirtualFragments() {
		return new HashSet<PriceFragment>(virtualFragments.values());
	}

	/**
	 * This is a convenience method for JSTL
	 * @return Returns the amount as a double.
	 */
	public double getAmountAsDouble()
	{
		return getCurrency().toDouble(amount);
	}

	/**
	 * @return Returns the amount.
	 */
	public long getAmount()
	{
		return amount;
	}
	/**
	 * Sets the amount of the 'Total' PriceFragment and with it the amount of this price.
	 * @param amount The amount to set.
	 */
	public void setAmount(long amount)
	{
		PriceFragment priceFragmentTotal = getPriceFragment(
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID, 
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID, 
				false
		);

		if (priceFragmentTotal == null)
		{
			PriceFragmentType pftTotal = lookupDefaultTotalPriceFragmentType();
			priceFragmentTotal = createPriceFragment(pftTotal);
		}

		priceFragmentTotal.setAmount(amount);
	}

	/**
	 * This method is called by {@link PriceFragment#setAmount(long)}, if it represents the total price.
	 */
	protected void _setAmount(long amount)
	{
		this.amount = amount;
	}

	/**
	 * Returns the mathematical absolute value of this price's amount.
	 */
	public long getAmountAbsoluteValue() {
		return Math.abs(amount);
	}

	/**
	 * Returns the PriceFragment for the given PriceFragmentType primary key.
	 * The fragment is searched in the persistent and in the virtual fragments.
	 *
	 * @param priceFragmentTypePK The primary key of the PriceFragmentType the PriceFragment should be searched for.
	 * @param throwExceptionIfNotExistent Whether to throw an {@link IllegalArgumentException} when the fragment for the desired type was not found.
	 * @return The PriceFragment for the given PriceFragmentType primary key
	 */
	public PriceFragment getPriceFragment(
			String priceFragmentTypePK, boolean throwExceptionIfNotExistent)
	{
		return getPriceFragment(priceFragmentTypePK, throwExceptionIfNotExistent, true);
	}

	/**
	 * Returns the PriceFragment for the given PriceFragmentType primary key.
	 * The caller can decide whether the fragment is searched in the persistent fragments only,
	 * or whether the search includes the virtual fragments as well.
	 *
	 * @param priceFragmentTypePK The primary key of the PriceFragmentType the PriceFragment should be searched for.
	 * @param throwExceptionIfNotExistent Whether to throw an {@link IllegalArgumentException} when the fragment for the desired type was not found.
	 * @param includeVirtual Whether to extend the search to the virtual PriceFragments
	 * @return The PriceFragment for the given PriceFragmentType primary key
	 */
	public PriceFragment getPriceFragment(
			String priceFragmentTypePK, boolean throwExceptionIfNotExistent, boolean includeVirtual)
	{
		PriceFragment fragment = fragments.get(priceFragmentTypePK);
		if (fragment == null && includeVirtual) {
			fragment = virtualFragments.get(priceFragmentTypePK);
		}
		if (fragment == null && throwExceptionIfNotExistent)
			throw new IllegalArgumentException("No PriceFragment registered with priceFragmentTypePK=\""+priceFragmentTypePK+"\" for "+this.getClass().getSimpleName()+" "+this.getPrimaryKey()+", the following fragments are contained "+fragments); //$NON-NLS-1$ //$NON-NLS-2$
		return fragment;
	}

	/**
	 * Returns the PriceFragment for the given PriceFragmentType primary key.
	 * The fragment is searched in the persistent and in the virtual fragments.
	 *
	 * @param priceFragmentTypeOrganisationID The organisationID of the primary key of the PriceFragmentType the PriceFragment should be searched for.
	 * @param priceFragmentTypeID The priceFragmentTypeID of the primary key of the PriceFragmentType the PriceFragment should be searched for.
	 * @param throwExceptionIfNotExistent Whether to throw an {@link IllegalArgumentException} when the fragment for the desired type was not found.
	 * @return The PriceFragment for the given PriceFragmentType primary key
	 */
	public PriceFragment getPriceFragment(
			String priceFragmentTypeOrganisationID,
			String priceFragmentTypeID, boolean throwExceptionIfNotExistent)
	{
		return getPriceFragment(priceFragmentTypeOrganisationID, priceFragmentTypeID, throwExceptionIfNotExistent, true);
	}

	/**
	 * Returns the PriceFragment for the given PriceFragmentType primary key.
	 * The caller can decide whether the fragment is searched in the persistent fragments only,
	 * or whether the search includes the virtual fragments as well.
	 *
	 * @param priceFragmentTypeOrganisationID The organisationID The primary key of the PriceFragmentType the PriceFragment should be searched for.
	 * @param priceFragmentTypeID The priceFragmentTypeID The primary key of the PriceFragmentType the PriceFragment should be searched for.
	 * @param throwExceptionIfNotExistent Whether to throw an {@link IllegalArgumentException} when the fragment for the desired type was not found.
	 * @param includeVirtual Whether to extend the search to the virtual PriceFragments
	 * @return The PriceFragment for the given PriceFragmentType primary key
	 */
	public PriceFragment getPriceFragment(
			String priceFragmentTypeOrganisationID,
			String priceFragmentTypeID, boolean throwExceptionIfNotExistent, boolean includeVirtual)
	{
		return getPriceFragment(PriceFragmentType.getPrimaryKey(priceFragmentTypeOrganisationID, priceFragmentTypeID), throwExceptionIfNotExistent, includeVirtual);
	}

	/**
	 * Creates a new PriceFragment for the given PriceFragmentType if it is not already
	 * part of the list of fragments of this Price. If could be found in the list, this
	 * one is returned.
	 *
	 * @param priceFragmentType The PriceFragmentType a fragment should be created for.
	 * @return The PriceFragment for the given PriceFragmentType (a new one will be created if it is not already there).
	 */
	public PriceFragment createPriceFragment(PriceFragmentType priceFragmentType)
	{
		if (priceFragmentType == null)
			throw new NullPointerException("priceFragmentType"); //$NON-NLS-1$

		PriceFragment fragment = fragments.get(priceFragmentType.getPrimaryKey());
		if (fragment == null) {
			fragment = new PriceFragment(this, priceFragmentType);
			fragments.put(priceFragmentType.getPrimaryKey(), fragment);
			if (logger.isDebugEnabled()) {
				logger.debug("Created PriceFragment "+fragment.getPriceFragmentTypePK()+" and added to price "+getPrimaryKey());
			}
		}
		return fragment;
	}

	/**
	 * Creates a new virtual (non-persistent) PriceFragment for the given PriceFragmentType if it is not already
	 * part of the list of virtual fragments of this Price. If it could be found in the list, this
	 * one is returned.
	 *
	 * @param priceFragmentType The PriceFragmentType a fragment should be created for.
	 * @return The virtual PriceFragment for the given PriceFragmentType (a new one will be created if it is not already there).
	 */
	public PriceFragment createVirtualPriceFragment(PriceFragmentType priceFragmentType)
	{
		if (priceFragmentType == null)
			throw new NullPointerException("priceFragmentType"); //$NON-NLS-1$

		PriceFragment fragment = virtualFragments.get(priceFragmentType.getPrimaryKey());
		if (fragment == null) {
			fragment = new PriceFragment(this, priceFragmentType);
			fragment.setVirtual(true);
			virtualFragments.put(priceFragmentType.getPrimaryKey(), fragment);
		}
		return fragment;
	}

	/**
	 * Returns the amount of the PriceFragment for the given PriceFragmentType in this Price.
	 * If the corresponding fragment cannot be found, <code>0</code> is returned.
	 * <p>
	 * The fragments will be searched in the list of persistent fragments as well in the list of virtual fragments.
	 * </p>
	 * @param priceFragmentType The PriceFragmentType the amount should be returned for.
	 * @return The amount for the corresponding price fragment, or <code>0</code> if this cannot be found.
	 */
	public long getAmount(PriceFragmentType priceFragmentType)
	{
		return getAmount(priceFragmentType.getOrganisationID(), priceFragmentType.getPriceFragmentTypeID());
	}

	/**
	 * Returns the amount of the PriceFragment for the given PriceFragmentType in this Price.
	 * If the corresponding fragment cannot be found, <code>0</code> is returned.
	 * <p>
	 * The fragments will be searched in the list of persistent fragments as well in the list of virtual fragments.
	 * </p>
	 * @param priceFragmentTypePK The primary key of the PriceFragmentType the amount should be returned for.
	 * @return The amount for the corresponding price fragment, or <code>0</code> if this cannot be found.
	 */
	public long getAmount(String priceFragmentTypePK)
	{
		if (priceFragmentTypePK == null ||
				PriceFragmentType.getPrimaryKey(PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID, PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID).equals(priceFragmentTypePK))
			return amount;

		PriceFragment priceFragment = getPriceFragment(priceFragmentTypePK, false);
		if (priceFragment != null)
			return priceFragment.getAmount();

		return 0;
	}

	/**
	 * Adds the given PriceFragment to the list of fragments of this Price.
	 *
	 * @param priceFragment The PriceFragment to add.
	 */
	protected void addPriceFragment(PriceFragment priceFragment)
	{
		fragments.put(priceFragment.getPriceFragmentTypePK(), priceFragment);
		if (logger.isDebugEnabled()) {
			logger.debug("Added PriceFragment "+priceFragment.getPriceFragmentTypePK()+" to price "+getPrimaryKey());
		}
	}

	/**
	 * Returns the amount of the PriceFragment for the given PriceFragmentType in this Price.
	 * If the corresponding fragment cannot be found, <code>0</code> is returned.
	 * <p>
	 * The fragments will be searched in the list of persistent fragments as well in the list of virtual fragments.
	 * </p>
	 * @param priceFragmentTypeOrganisationID The organisationID of the primary key of the PriceFragmentType the amount should be returned for.
	 * @param priceFragmentTypeID The priceFragmentTypeID of the primary key of the PriceFragmentType the amount should be returned for.
	 * @return The amount for the corresponding price fragment, or <code>0</code> if this cannot be found.
	 */
	public long getAmount(String priceFragmentTypeOrganisationID, String priceFragmentTypeID)
	{
		return getAmount(priceFragmentTypeOrganisationID, priceFragmentTypeID, true);
	}

	/**
	 * Returns the amount of the PriceFragment for the given PriceFragmentType in this Price.
	 * If the corresponding fragment cannot be found, <code>0</code> is returned.
	 * <p>
	 * The caller can decide if the search for fragments is limited to the list of persistent fragments only,
	 * or whether the fragment is searched in the list of virtual fragments as well.
	 * </p>
	 * @param priceFragmentTypeOrganisationID The organisationID of the primary key of the PriceFragmentType the amount should be returned for.
	 * @param priceFragmentTypeID The priceFragmentTypeID of the primary key of the PriceFragmentType the amount should be returned for.
	 * @param includeVirtual Whether to extend the search for the fragment to the list of virtual fragments.
	 * @return The amount for the corresponding price fragment, or <code>0</code> if this cannot be found.
	 */
	public long getAmount(String priceFragmentTypeOrganisationID, String priceFragmentTypeID, boolean includeVirtual)
	{
		if (priceFragmentTypeOrganisationID != null && priceFragmentTypeID == null)
			throw new IllegalArgumentException("priceFragmentTypeOrganisationID is not null, but priceFragmentTypeID is null! Either none or both must be null!"); //$NON-NLS-1$

		if (priceFragmentTypeOrganisationID == null && priceFragmentTypeID != null)
			throw new IllegalArgumentException("priceFragmentTypeOrganisationID is null, but priceFragmentTypeID is not null! Either none or both must be null!"); //$NON-NLS-1$

		if (priceFragmentTypeID == null || (
						PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID.equals(priceFragmentTypeOrganisationID) &&
						PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID.equals(priceFragmentTypeID)))
			return amount;

		PriceFragment priceFragment = getPriceFragment(priceFragmentTypeOrganisationID, priceFragmentTypeID, false, includeVirtual);
		if (priceFragment != null)
			return priceFragment.getAmount();

		return 0;
	}

	/**
	 * Set the amount of the persistent PriceFragment corresponding to the given PriceFragmentType.
	 * <p>
	 * Note that the PriceFragment will be created if not already existing in this Price.
	 * </p>
	 * @param priceFragmentType The PriceFragmentType the amount should be set for.
	 * @param amount The amount to set.
	 */
	public void setAmount(PriceFragmentType priceFragmentType, long amount) {
		setAmount(priceFragmentType, amount, false);
	}

	/**
	 * Set the amount of the persistent PriceFragment corresponding to the given PriceFragmentType.
	 * <p>
	 * The caller can decide whether the PriceFragment searched or created is (or should be created as)
	 * a virtual PriceFragment.
	 * </p>
	 * @param priceFragmentType The PriceFragmentType the amount should be set for.
	 * @param amount The amount to set.
	 * @param virtual Whether the PriceFragment is virtual.
	 */
	public void setAmount(PriceFragmentType priceFragmentType, long amount, boolean virtual)
	{
		// the PriceFragment for the _Total_ PriceFragmentType will call _setAmount(...) here
		PriceFragment priceFragment = null;
		if (virtual)
			priceFragment = createVirtualPriceFragment(priceFragmentType);
		else
			priceFragment = createPriceFragment(priceFragmentType);
		priceFragment.setAmount(amount);
	}

	/**
	 * Remove ALL fragments from this Price, persistent and virtual.
	 */
	public void clearFragments()
	{
		clearFragments(true);
	}

	/**
	 * Remove fragments from this Price and decide whether the virtual once should be cleared as well.
	 * @param includeVirtual Wheter the virtual fragments should be cleared as well.
	 */
	public void clearFragments(boolean includeVirtual)
	{
		fragments.clear();
		if (includeVirtual)
			virtualFragments.clear();
	}

	/**
	 * Remove the virtual fragments from this Price.
	 */
	public void clearVirtualFragments() {
		virtualFragments.clear();
	}
	
	/**
	 * This method finds the local PriceFragment with the same priceFragmentID and
	 * adds the amount of the given PriceFragment to it. If there is no local PriceFragment
	 * existing for the given PriceFragmentType, it will be created.
	 *
	 * @param priceFragment
	 */
	public void sumPriceFragment(PriceFragment priceFragment)
	{
		PriceFragmentType priceFragmentType = priceFragment.getPriceFragmentType();
		String priceFragmentTypePK = priceFragmentType.getPrimaryKey();
		PriceFragment localPriceFragment = getPriceFragment(priceFragmentTypePK, false);
		if (localPriceFragment == null) {
			if (priceFragment.isVirtual())
				localPriceFragment = createVirtualPriceFragment(priceFragmentType);
			else
				localPriceFragment = createPriceFragment(priceFragmentType);
		}
		localPriceFragment.setAmount(localPriceFragment.getAmount() + priceFragment.getAmount());
	}
	
	public void sumPrice(Price price)
	{
		this.amount += price.getAmount();
		for (PriceFragment fragment : price.getFragments()) {
			sumPriceFragment(fragment);
		}
	}
	
	@Override
	public CloneableWithContext clone(CloneContext context, boolean cloneReferences)
	{
//	WORKAROUND - JDO does not support cloning a detached object and consider it transient! ( http://www.jpox.org/servlet/forum/viewthread_thread,1865 )
//	Price clone = (Price) super.clone();
		Price clone = ReflectUtil.newInstanceRuntimeException(getClass());
		clone.organisationID = organisationID;
		clone.amount = amount;
		clone.currency = currency;
//	END OF WORKAROUND
		
		clone.priceID = IDGenerator.nextID(getClass());
		if (fragments != null)
		{
			for (Map.Entry<String, PriceFragment> fragmentEntry : fragments.entrySet())
			{
				fragmentEntry.setValue(new PriceFragment(this, fragmentEntry.getValue()));
			}
		}
		
		if (virtualFragments != null)
		{
			for (Map.Entry<String, PriceFragment> fragmentEntry : virtualFragments.entrySet())
			{
				fragmentEntry.setValue(new PriceFragment(this, fragmentEntry.getValue()));
			}			
		}
		return clone;
	}

	/**
	 * Nothing to do.
	 */
	@Override
	public void updateReferencesOfClone(CloneableWithContext clone, CloneContext context)
	{
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (priceID ^ (priceID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Price other = (Price) obj;
		return (
				Util.equals(this.priceID, other.priceID) &&
				Util.equals(this.organisationID, other.organisationID)
		);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + priceID + ']';
	}

}
