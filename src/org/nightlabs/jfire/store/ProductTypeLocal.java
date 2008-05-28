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

package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.InheritanceCallbacks;
import org.nightlabs.inheritance.StaticFieldMetaData;
import org.nightlabs.jdo.FetchPlanBackup;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jdo.inheritance.JDOSimpleFieldInheriter;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.priceconfig.AffectedProductType;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfigUtil;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorityType;
import org.nightlabs.jfire.security.SecuredObject;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorityTypeID;
import org.nightlabs.jfire.store.book.LocalStorekeeperDelegate;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductTypeLocalID"
 *		detachable="true"
 *		table="JFireTrade_ProductTypeLocal"
 *
 * @jdo.version strategy="version-number"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeID"
 *
 * @jdo.fetch-group name="ProductTypeLocal.productType" fields="productType"
 * @jdo.fetch-group name="ProductTypeLocal.home" fields="home"
 * @jdo.fetch-group name="ProductTypeLocal.localAccountantDelegate" fields="localAccountantDelegate"
 * @jdo.fetch-group name="ProductTypeLocal.localStorekeeperDelegate" fields="localStorekeeperDelegate"
 * @jdo.fetch-group name="ProductTypeLocal.nestedProductTypeLocals" fields="nestedProductTypeLocals"
 * @jdo.fetch-group name="ProductTypeLocal.fieldMetaDataMap" fields="fieldMetaDataMap"
 *
 * @jdo.fetch-group name="ProductType.productTypeLocal" fields="productType"
 * @jdo.fetch-group name="ProductType.this" fields="productType"
 */
public class ProductTypeLocal
implements Serializable, Inheritable, InheritanceCallbacks, SecuredObject
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ProductTypeLocal.class);

	public static final String FETCH_GROUP_PRODUCT_TYPE = "ProductTypeLocal.productType";
	public static final String FETCH_GROUP_HOME = "ProductTypeLocal.home";
	public static final String FETCH_GROUP_LOCAL_ACCOUNTANT_DELEGATE = "ProductTypeLocal.localAccountantDelegate";
	public static final String FETCH_GROUP_LOCAL_STOREKEEPER_DELEGATE = "ProductTypeLocal.localStorekeeperDelegate";
	public static final String FETCH_GROUP_FIELD_METADATA_MAP = "ProductTypeLocal.fieldMetaDataMap";
	public static final String FETCH_GROUP_NESTED_PRODUCT_TYPE_LOCALS = "ProductTypeLocal.nestedProductTypeLocals";

//	/**
//	 * loads both fields {@link #securingAuthority} and {@link #securingAuthorityType}, because the <code>AuthorityType</code> is the same instance anyway.
//	 */
//	public static final String FETCH_GROUP_ALL = "ProductTypeLocal.securingAuthority";

	/**
	 * This class defines constants for the field names of implementation of
	 * {@link Inheritable}, to avoid the use of "hardcoded" Strings for retrieving
	 * {@link FieldMetaData} or {@link FieldInheriter}.  
	 * In the future the JFire project will probably autogenerate this class,
	 * but until then you should implement it manually.
	 */	
	public static final class FieldName
	{
		public static final String fieldMetaDataMap = "fieldMetaDataMap";
		public static final String localAccountantDelegate = "localAccountantDelegate";
		public static final String localStorekeeperDelegate = "localStorekeeperDelegate";
		public static final String nestedProductTypeLocals = "nestedProductTypeLocals";
		public static final String organisationID = "organisationID";
		public static final String productType = "productType";
		public static final String productTypeID = "productTypeID";
		public static final String securingAuthority = "securingAuthority";
		public static final String selfForVirtualSelfPackaging = "selfForVirtualSelfPackaging";
		public static final String tmpInherit_innerPriceConfigID = "tmpInherit_innerPriceConfigID";
		public static final String tmpInherit_nestedProductTypes = "tmpInherit_nestedProductTypes";
	};
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String productTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductType productType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LocalAccountantDelegate localAccountantDelegate = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LocalStorekeeperDelegate localStorekeeperDelegate = null;

	/**
	 * key: String primaryKey(organisationID + '/' + productTypeID)<br/>
	 * value: {@link NestedProductTypeLocal} nestedProductTypeLocal
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="NestedProductTypeLocal"
	 *		mapped-by="packageProductTypeLocal"
	 *		dependent-value="true"
	 *
	 * @jdo.key mapped-by="innerProductTypePrimaryKey"
	 */
	private Map<String, NestedProductTypeLocal> nestedProductTypeLocals;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="ProductTypeLocalFieldMetaData"
	 *		dependent-value="true"
	 *		mapped-by="productTypeLocal"
	 *
	 * @jdo.key mapped-by="fieldName"
	 */
	protected Map<String, ProductTypeLocalFieldMetaData> fieldMetaDataMap;

	/**
	 * The securingAuthority type for this product type. In an inheritance tree of product types, this must be the same for all product types.
	 * That's why the {@link ProductTypeActionHandler#getAuthorityType(ProductType)} is called once for the root object only, the other
	 * instances inherit this value.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String securingAuthorityTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String securingAuthorityID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProductTypeLocal() { }

	// TODO we should pass the user here and store it somehow
	public ProductTypeLocal(User user, ProductType productType)
	{
		if (productType == null)
			throw new IllegalArgumentException("productType must not be null!");

		this.productType = productType;
		this.organisationID = productType.getOrganisationID();
		this.productTypeID = productType.getProductTypeID();
		nestedProductTypeLocals = new HashMap<String, NestedProductTypeLocal>();
		fieldMetaDataMap = new HashMap<String, ProductTypeLocalFieldMetaData>();
		productType.setProductTypeLocal(this); // TODO JPOX WORKAROUND: This causes problems, hence we set it in the Store.addProductType(...) method:
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getProductTypeID()
	{
		return productTypeID;
	}

	public ProductType getProductType()
	{
		return productType;
	}

	/**
	 * The LocalAccountantDelegate is in charge of booking money to different
	 * account for the productType it is assigned to and its packaged types
	 * when an invoice is booked.
	 */
	public LocalAccountantDelegate getLocalAccountantDelegate() {
		return localAccountantDelegate;
	}

	/**
	 * Set the LocalAccountantDelegate.
	 */
	public void setLocalAccountantDelegate(LocalAccountantDelegate localAccountantDelegate) {
		this.localAccountantDelegate = localAccountantDelegate;
	}

	/**
	 * @return Returns the localStorekeeperDelegate.
	 */
	public LocalStorekeeperDelegate getLocalStorekeeperDelegate()
	{
		return localStorekeeperDelegate;
	}

	/**
	 * @param localStorekeeperDelegate The localStorekeeperDelegate to set.
	 */
	public void setLocalStorekeeperDelegate(
			LocalStorekeeperDelegate localStorekeeperDelegate)
	{
		this.localStorekeeperDelegate = localStorekeeperDelegate;
	}

	public FieldInheriter getFieldInheriter(String fieldName)
	{
		if (FieldName.nestedProductTypeLocals.equals(fieldName))
			return new NestedProductTypeLocalMapInheriter();

		return new JDOSimpleFieldInheriter();
	}

	private static Set<String> nonInheritableFields = new HashSet<String>();

	public FieldMetaData getFieldMetaData(String fieldName)
	{
		return getFieldMetaData(fieldName, true);
	}
	
	public FieldMetaData getFieldMetaData(String fieldName, boolean createMissingMetaData)
	{
		if (fieldName.startsWith("jdo"))
			return null;

		if (fieldName.equals(FieldName.tmpInherit_innerPriceConfigID))
			return null;

		if (fieldName.equals(FieldName.tmpInherit_nestedProductTypes))
			return null;
		
		if (FieldName.securingAuthority.equals(fieldName))
			return new StaticFieldMetaData(fieldName);

// TODO the below checks for nestedProductTypes should be removed after a few months transition time.
		if ("nestedProductTypes".equals(fieldName))
			throw new IllegalArgumentException("The field 'nestedProductTypes' has been renamed to 'nestedProductTypeLocals' while it was moved to ProductTypeLocal!");
// END to do

		synchronized (nonInheritableFields) {
			if (nonInheritableFields.isEmpty()) {
				// PK fields
				nonInheritableFields.add(FieldName.organisationID);
				nonInheritableFields.add(FieldName.productTypeID);

				// other fields
				nonInheritableFields.add(FieldName.productType);
				nonInheritableFields.add(FieldName.fieldMetaDataMap);
				nonInheritableFields.add(FieldName.selfForVirtualSelfPackaging);
			}

			if (nonInheritableFields.contains(fieldName))
				return null;
		}

		ProductTypeLocalFieldMetaData fmd = fieldMetaDataMap.get(fieldName);
		if (fmd == null && createMissingMetaData) {
			if (FieldName.nestedProductTypeLocals.equals(fieldName))
				fmd = new ProductTypeLocalMapFieldMetaData(this, fieldName);
			else
				fmd = new ProductTypeLocalFieldMetaData(this, fieldName);

			fieldMetaDataMap.put(fieldName, fmd);
		} // if (fmd == null) {

		return fmd;
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient PriceConfigID tmpInherit_innerPriceConfigID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Map<String, NestedProductTypeLocal> tmpInherit_nestedProductTypes = null;

	public void preInherit(Inheritable mother, Inheritable child)
	{
		if (getSecuringAuthorityID() == null);
		if (getSecuringAuthorityTypeID() == null);
		
		if (child == this) {
			// check whether the nestedPoductTypes change - in this case we will recalculate prices after inheritance in postInherit(...)
			// we copy the current nestedProductTypeLocals to tmpInherit_nestedProductTypes - then we compare them afterwards
			PersistenceManager pm = getPersistenceManager();
			FetchPlanBackup fetchPlanBackup = NLJDOHelper.backupFetchPlan(pm.getFetchPlan());
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getFetchPlan().setMaxFetchDepth(1);
			try {
				tmpInherit_nestedProductTypes = new HashMap<String, NestedProductTypeLocal>(nestedProductTypeLocals.size());
				for (Map.Entry<String, NestedProductTypeLocal> me : nestedProductTypeLocals.entrySet()) {
					NestedProductTypeLocal npt = pm.detachCopy(me.getValue());
					tmpInherit_nestedProductTypes.put(me.getKey(), npt);
				}
			} finally {
				NLJDOHelper.restoreFetchPlan(pm.getFetchPlan(), fetchPlanBackup);
			}

			// additionally, we need to check, whether the innerPriceConfig is replaced, which would cause recalculation, too
			tmpInherit_innerPriceConfigID = (PriceConfigID) JDOHelper.getObjectId(getProductType().getInnerPriceConfig());
		}

		// ensure that these fields are loaded
		if (localAccountantDelegate == null);
		if (localStorekeeperDelegate == null);
		nestedProductTypeLocals.size();

		if (securingAuthorityTypeID == null);
		if (securingAuthorityID == null);
	}

	/**
	 * @return <code>true</code> if they are equal, <code>false</code> if they are different
	 */
	public static boolean compareNestedProductTypeLocals(
			Collection<NestedProductTypeLocal> nestedProductTypeLocals1,
			Map<String, NestedProductTypeLocal> nestedProductTypeLocals2)
	{
		if (nestedProductTypeLocals1.size() != nestedProductTypeLocals2.size())
			return false;

		for (NestedProductTypeLocal orgNPT : nestedProductTypeLocals1) {
			NestedProductTypeLocal newNPT = nestedProductTypeLocals2.get(orgNPT.getInnerProductTypePrimaryKey());
			if (newNPT == null)
				return false;

			if (newNPT.getQuantity() != orgNPT.getQuantity())
				return false;
		}

		return true;
	}

	public void postInherit(Inheritable mother, Inheritable child)
	{
		if (child == this) {
			if (!Util.equals(tmpInherit_innerPriceConfigID, JDOHelper.getObjectId(getProductType().getInnerPriceConfig())) ||
					!compareNestedProductTypeLocals(nestedProductTypeLocals.values(), tmpInherit_nestedProductTypes)) {
				// there are changes => recalculate prices!
				PersistenceManager pm = getPersistenceManager();

				HashSet<ProductTypeID> processedProductTypeIDs = new HashSet<ProductTypeID>();
				ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(this.getProductType());
				for (AffectedProductType apt : PriceConfigUtil.getAffectedProductTypes(pm, this.getProductType())) {
					if (!processedProductTypeIDs.add(apt.getProductTypeID()))
						continue;
					
					ProductType pt;
					if (apt.getProductTypeID().equals(productTypeID))
						pt = this.getProductType();
					else
						pt = (ProductType) pm.getObjectById(apt.getProductTypeID());

					if (pt.isResponsibleForPriceCalculation()) {
						logger.info("postInherit: price-calculation starting for: " + JDOHelper.getObjectId(pt));

						pt.calculatePrices();

						logger.info("postInherit: price-calculation complete for: " + JDOHelper.getObjectId(pt));
					}
				}
			}
		}
	}

	/**
	 * A <code>ProductType</code> can contain others (e.g. a car would contain 4 wheels,
	 * the engine and much more). This method allows to package another <code>ProductType</code>
	 * within this one.
	 *
	 * @param productType The <code>ProductType</code> which will be packaged within this one.
	 * @return Returns the {@link NestedProductTypeLocal} which functions as glue-with-metadata between
	 *		the package and its content. You can change the quantity (and later other properties)
	 *		manipulating the <code>NestedProductTypeLocal</code>.
	 */
	public NestedProductTypeLocal createNestedProductTypeLocal(ProductTypeLocal productTypeLocal)
	{
		// it MUST be possible to continue nesting indefinitely
//		if (this.isPackageInner())
//			throw new IllegalStateException("This ProductType ("+getPrimaryKey()+") is marked as package-inner and can therefore not contain nested product types!");

		NestedProductTypeLocal packagedProductType = nestedProductTypeLocals.get(productTypeLocal.getPrimaryKey());
		if (packagedProductType == null) {
			packagedProductType = new NestedProductTypeLocal(this, productTypeLocal);
			nestedProductTypeLocals.put(productTypeLocal.getPrimaryKey(), packagedProductType);
		}
		return packagedProductType;
	}

	/**
	 * @return Returns a <code>Collection</code> of {@link NestedProductTypeLocal}. These are all
	 *		other <code>ProductType</code>s that have been added to this package. It does <b>not</b>
	 *		contain itself, even if this <code>ProductType</code> does virtual-self-packaging.
	 *
	 * @see #getNestedProductTypeLocals(boolean)
	 */
	public Collection<NestedProductTypeLocal> getNestedProductTypeLocals()
	{
		return Collections.unmodifiableCollection(nestedProductTypeLocals.values());
	}

	/**
	 * This method can be used instead of {@link #getNestedProductTypeLocals()} and provides
	 * the possibility to take the virtual-self-packaging into account.
	 *
	 * @param includeSelfForVirtualSelfPackaging Whether or not to add <code>this</code>
	 *		to the result, if this ProductType virtually packages itself (i.e. has an {@link #getInnerPriceConfig() inner}
	 *		<b>and</b> a {@link #getPackagePriceConfig() package price config} assigned).
	 *
	 * @return Returns either the same as {@link #getNestedProductTypeLocals()} or adds <code>this</code> to the result.
	 */
	public Collection<NestedProductTypeLocal> getNestedProductTypeLocals(boolean includeSelfForVirtualSelfPackaging)
	{
		if (!includeSelfForVirtualSelfPackaging || getProductType().isPackageInner() || getProductType().getInnerPriceConfig() == null)
			return getNestedProductTypeLocals();
		else {
			HashSet<NestedProductTypeLocal> res = new HashSet<NestedProductTypeLocal>(nestedProductTypeLocals.values());
			res.add(getSelfForVirtualSelfPackaging());
			return Collections.unmodifiableCollection(res);
		}
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	protected transient NestedProductTypeLocal selfForVirtualSelfPackaging = null;

	/**
	 * This method is used internally (e.g. by {@link #getNestedProductTypeLocals(boolean)}).
	 *
	 * @return Returns a transient, temporary instance of {@link NestedProductTypeLocal} linking <code>this</code>
	 *		to itself.
	 */
	protected NestedProductTypeLocal getSelfForVirtualSelfPackaging()
	{
		if (selfForVirtualSelfPackaging == null)
			selfForVirtualSelfPackaging = new NestedProductTypeLocal(this, this);
		return selfForVirtualSelfPackaging;
	}

	public String getPrimaryKey()
	{
		return ProductType.getPrimaryKey(organisationID, productTypeID);
	}

	/**
	 * @param productTypePK The composite primary key returned by {@link #getPrimaryKey()}.
	 * @return Returns a packaged productType or <code>null</code> if none with the given ID exists.
	 */
	public NestedProductTypeLocal getNestedProductTypeLocal(String productTypePK, boolean throwExceptionIfNotExistent)
	{
		if (this.getPrimaryKey().equals(productTypePK))
			return getSelfForVirtualSelfPackaging();

		NestedProductTypeLocal res = nestedProductTypeLocals.get(productTypePK);
		if (res == null && throwExceptionIfNotExistent)
			throw new IllegalArgumentException("No NestedProductTypeLocal existing with productTypePK=\""+productTypePK+"\"!");
		return res;
	}
	/**
	 * @param organisationID The organisation part of the primary key as returned by {@link #getOrganisationID()}.
	 * @param productTypeID The local id part of the primary key as returned by {@link #getProductTypeID()}.
	 * @return Returns a packaged product or <code>null</code> if none with the given ID exists.
	 */
	public NestedProductTypeLocal getNestedProductTypeLocal(String organisationID, String productTypeID, boolean throwExceptionIfNotExistent)
	{
		if (this.organisationID.equals(organisationID) && this.productTypeID.equals(productTypeID))
			return getSelfForVirtualSelfPackaging();

		NestedProductTypeLocal res = nestedProductTypeLocals.get(ProductType.getPrimaryKey(organisationID, productTypeID));
		if (res == null && throwExceptionIfNotExistent)
			throw new IllegalArgumentException("No NestedProductTypeLocal existing with organisationID=\""+organisationID+"\", productTypeID=\""+productTypeID+"\"!");
		return res;
	}
	/**
	 * Removes a packaged product from this package.
	 *
	 * @param organisationID The organisation part of the primary key as returned by {@link #getOrganisationID()}.
	 * @param productTypeID The local id part of the primary key as returned by {@link #getProductTypeID()}.
	 */
	public void removeNestedProductTypeLocal(String organisationID, String productTypeID)
	{
		nestedProductTypeLocals.remove(ProductType.getPrimaryKey(organisationID, productTypeID));
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not yet persistent or currently not attached to a datastore! Cannot obtain PersistenceManager!");

		return pm;
	}

	@Override
	public AuthorityTypeID getSecuringAuthorityTypeID() {
		return (AuthorityTypeID) ObjectIDUtil.createObjectID(securingAuthorityTypeID);
	}

	public void setSecuringAuthorityTypeID(AuthorityTypeID authorityTypeID) {
		if (this.securingAuthorityTypeID != null && !this.getSecuringAuthorityTypeID().equals(authorityTypeID))
			throw new IllegalStateException("A different AuthorityType has already been assigned! Cannot change this value afterwards! Currently assigned: " + this.securingAuthorityTypeID + " New value: " + authorityTypeID);

		this.securingAuthorityTypeID = authorityTypeID == null ? null : authorityTypeID.toString();
	}

	/**
	 * Get the currently assigned <code>AuthorityID</code> or <code>null</code>.
	 * <p>
	 * If there is no securingAuthority assigned (i.e. this property is <code>null</code>), no additional access right
	 * checks (besides the EJB method privileges) will be done. If there is a securingAuthority, access to the <code>ProductType</code>
	 * and the <code>ProductTypeLocal</code> is only granted, if first the global (EJB method based) privileges allow the action
	 * <b>and</b> second the assigned securingAuthority allows the action, as well.
	 * </p>
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 *
	 * @return the identifier of the <code>Authority</code> responsible for this <code>ProductType</code> or <code>null</code> if there is none assigned.
	 */
	@Override
	public AuthorityID getSecuringAuthorityID()
	{
		return (AuthorityID) ObjectIDUtil.createObjectID(securingAuthorityID);
	}

	/**
	 * Set an <code>AuthorityID</code> or <code>null</code>.
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * 
	 * @param authorityID the new <code>AuthorityID</code> or <code>null</code>.
	 * @see #getSecuringAuthorityID()
	 */
	@Override
	public void setSecuringAuthorityID(AuthorityID authorityID)
	{
		// Already obtain the PM directly at the beginning of the method so that it
		// always fails outside of the server (independent from the parameter).
		PersistenceManager pm = getPersistenceManager();

		if (Util.equals(authorityID, this.getSecuringAuthorityID()))
			return; // no change => no need to do anything

		if (authorityID != null) {
			// check if the AuthorityType is correct. this is done already by JFireSecurityManager.assignAuthority(...), but just to be absolutely sure
			// since this method might be called by someone else.
			Authority authority = (Authority) pm.getObjectById(authorityID);
			AuthorityType securingAuthorityType = (AuthorityType) pm.getObjectById(getSecuringAuthorityTypeID());

			if (!authority.getAuthorityType().equals(securingAuthorityType))
				throw new IllegalArgumentException("securingAuthority.authorityType does not match this.securingAuthorityTypeID! securingAuthority: " + authorityID + " this: " + JDOHelper.getObjectId(this));
		}
		this.securingAuthorityID = authorityID == null ? null : authorityID.toString();

		getProductType().applyInheritance();
	}
}
