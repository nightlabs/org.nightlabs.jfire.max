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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.InheritanceCallbacks;
import org.nightlabs.jdo.inheritance.JDOSimpleFieldInheriter;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.book.LocalStorekeeperDelegate;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductTypeLocalID"
 *		detachable="true"
 *		table="JFireTrade_ProductTypeLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeID"
 *
 * @jdo.fetch-group name="ProductTypeLocal.productType" fields="productType"
 * @jdo.fetch-group name="ProductTypeLocal.home" fields="home"
 * @jdo.fetch-group name="ProductTypeLocal.localAccountantDelegate" fields="localAccountantDelegate"
 * @jdo.fetch-group name="ProductTypeLocal.localStorekeeperDelegate" fields="localStorekeeperDelegate"
 * @jdo.fetch-group name="ProductTypeLocal.fieldMetaDataMap" fields="fieldMetaDataMap"
 *
 * @jdo.fetch-group name="ProductType.productTypeLocal" fields="productType"
 * @jdo.fetch-group name="ProductType.this" fields="productType"
 */
public class ProductTypeLocal
implements Serializable, Inheritable, InheritanceCallbacks
{
	// TODO: add field authority for security checking 
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_PRODUCT_TYPE = "ProductTypeLocal.productType";
	public static final String FETCH_GROUP_HOME = "ProductTypeLocal.home";
	public static final String FETCH_GROUP_LOCAL_ACCOUNTANT_DELEGATE = "ProductTypeLocal.localAccountantDelegate";
	public static final String FETCH_GROUP_LOCAL_STOREKEEPER_DELEGATE = "ProductTypeLocal.localStorekeeperDelegate";
	public static final String FETCH_GROUP_FIELD_METADATA_MAP = "ProductTypeLocal.fieldMetaDataMap";

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
	private Repository home;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LocalAccountantDelegate localAccountantDelegate = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LocalStorekeeperDelegate localStorekeeperDelegate = null;


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
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProductTypeLocal() { }

	// TODO we should pass the user here and store it somehow
	public ProductTypeLocal(User user, ProductType productType, Repository home)
	{
		if (productType == null)
			throw new IllegalArgumentException("productType must not be null!");

		this.productType = productType;
		this.organisationID = productType.getOrganisationID();
		this.productTypeID = productType.getProductTypeID();
		fieldMetaDataMap = new HashMap<String, ProductTypeLocalFieldMetaData>();
		this.setHome(home);
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
	 * @see Repository#ANCHOR_TYPE_ID_HOME
	 */
	public Repository getHome()
	{
		return home;
	}
	public void setHome(Repository home)
	{
		if (home == null)
			throw new IllegalArgumentException("home must not be null!");

		this.home = home;
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

		if (fieldName.startsWith("tmpInherit"))
			return null;

		synchronized (nonInheritableFields) {
			if (nonInheritableFields.isEmpty()) {
				// PK fields
				nonInheritableFields.add("organisationID");
				nonInheritableFields.add("productTypeID");

				// other fields
				nonInheritableFields.add("productType");
				nonInheritableFields.add("fieldMetaDataMap");
			}

			if (nonInheritableFields.contains(fieldName))
				return null;
		}

		ProductTypeLocalFieldMetaData fmd = fieldMetaDataMap.get(fieldName);
		if (fmd == null && createMissingMetaData) {
			fmd = new ProductTypeLocalFieldMetaData(this, fieldName);

			fieldMetaDataMap.put(fieldName, fmd);
		} // if (fmd == null) {

		return fmd;
	}

	public void preInherit(Inheritable mother, Inheritable child)
	{
		// ensure that these fields are loaded
		if (home == null);
		if (localAccountantDelegate == null);
		if (localStorekeeperDelegate == null);
	}

	public void postInherit(Inheritable mother, Inheritable child)
	{
		// nothing to do
	}	
}
