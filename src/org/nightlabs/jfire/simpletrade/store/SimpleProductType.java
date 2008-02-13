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

package org.nightlabs.jfire.simpletrade.store;

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.annotation.Implement;
import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.jdo.inheritance.JDOInheritableFieldInheriter;
import org.nightlabs.jfire.accounting.TariffMapper;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculationException;
import org.nightlabs.jfire.accounting.gridpriceconfig.PriceCalculator;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeLocal;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.CustomerGroupMapper;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.ProductType"
 *		detachable="true"
 *		table="JFireSimpleTrade_SimpleProductType"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.query
 * 		global="false"
 *		name="getChildProductTypes_topLevel"
 *		language="javax.jdo.query.JDOQL"
 *		query="SELECT
 *		  WHERE this.extendedProductType == null"
 *
 * @jdo.query
 *		name="getChildProductTypes_hasParent"
 *		language="javax.jdo.query.JDOQL"
 *		query="SELECT
 *		  WHERE
 *		    this.extendedProductType.organisationID == parentProductTypeOrganisationID &&
 *		    this.extendedProductType.productTypeID == parentProductTypeProductTypeID
 *		  PARAMETERS String parentProductTypeOrganisationID, String parentProductTypeProductTypeID
 *		  import java.lang.String"
 * 
 * @jdo.fetch-group name="SimpleProductType.this" fetch-groups="default, ProductType.this"
 * @jdo.fetch-group name="SimpleProductType.propertySet" fetch-groups="default" fields="propertySet"
 *
 * !@jdo.fetch-group name="ProductType.name" fields="name"
 *
 * !@jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default" fields="name"
 * !@jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default" fields="name"
 * !@jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default" fields="name"
 * !@jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default" fields="name"
 */
public class SimpleProductType extends ProductType
{
	private static final long serialVersionUID = 1L;
	
	public static final String FETCH_GROUP_THIS_SIMPLE_PRODUCT_TYPE = "SimpleProductType.this";
	public static final String FETCH_GROUP_PROPERTY_SET = "SimpleProductType.propertySet";

	/**
	 * Note, that this method does only return instances of {@link SimpleProductType} while
	 * the same-named method {@link ProductType#getChildProductTypes(PersistenceManager, ProductTypeID)}
	 * returns all types inherited from {@link ProductType}.
	 *
	 * @param pm The <tt>PersistenceManager</tt> that should be used to access the datastore.
	 * @param parentProductTypeID The <tt>ProductType</tt> of which to find all children or <tt>null</tt> to find all top-level-<tt>SimpleProductType</tt>s.
	 * @return Returns instances of <tt>SimpleProductType</tt>.
	 */
	public static Collection getChildProductTypes(PersistenceManager pm, ProductTypeID parentProductTypeID)
	{
		if (parentProductTypeID == null) {
			Query q = pm.newNamedQuery(SimpleProductType.class, "getChildProductTypes_topLevel");
			return (Collection)q.execute();
		}

		Query q = pm.newNamedQuery(SimpleProductType.class, "getChildProductTypes_hasParent");
		return (Collection) q.execute(
			parentProductTypeID.organisationID, parentProductTypeID.productTypeID);
	}

	/**
	 * @deprecated Constructor only for JDO!
	 */
	@Deprecated
	protected SimpleProductType() { }

	/**
	 * @see ProductType#ProductType(String, String, ProductType, LegalEntity, String)
	 */
	public SimpleProductType(String organisationID, String productTypeID,
			ProductType extendedProductType,
			byte inheritanceNature, byte packageNature)
	{
		super(organisationID, productTypeID, extendedProductType, inheritanceNature, packageNature);
		this.structLocalScope = StructLocal.DEFAULT_SCOPE;
		this.propertySet = new PropertySet(organisationID, IDGenerator.nextID(PropertySet.class), SimpleProductType.class.getName(), structLocalScope);
//		this.name = new SimpleProductTypeName(this);
//		getFieldMetaData("name").setValueInherited(false);
	}

	@Override
	protected ProductTypeLocal createProductTypeLocal(User user)
	{
		return new SimpleProductTypeLocal(user, this);
	}

	@Override
	@Implement
	protected void calculatePrices()
	{
		PersistenceManager pm = getPersistenceManager();
		PriceCalculator priceCalculator = new PriceCalculator(this, new CustomerGroupMapper(pm), new TariffMapper(pm)); // this method is never called when this instance is detached
		priceCalculator.preparePriceCalculation();
		try {
			priceCalculator.calculatePrices();
		} catch (PriceCalculationException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * The scope of the StructLocal by which the propertySet is build from.
	 * 
	 * @jdo.field persistence-modifier="persistent" null-value="exception" indexed="true"
	 */
	private String structLocalScope;
	
	/**
	 * Returns the scope of the StructLocal by which the propertySet is build from.
	 * @return The scope of the StructLocal by which the propertySet is build from.
	 */
	public String getStructLocalScope() {
		return structLocalScope;
	}
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PropertySet propertySet;

	/**
	 * Returns the property set of this {@link SimpleProductType}.
	 * 
	 * @return The property set of this {@link SimpleProductType}.
	 */
	public PropertySet getPropertySet() {
		return propertySet;
	}

	@Override
	public FieldInheriter getFieldInheriter(String fieldName) {
		if ("propertySet".equals(fieldName))
			return new JDOInheritableFieldInheriter();

		return super.getFieldInheriter(fieldName);
	}

	@Override
	public FieldMetaData getFieldMetaData(String fieldName, boolean createMissingMetaData)
	{
		if ("packagePriceConfig".equals(fieldName))
			return null;

		return super.getFieldMetaData(fieldName, createMissingMetaData);
	}

	@Override
	public void preInherit(Inheritable mother, Inheritable child)
	{
		super.preInherit(mother, child);
		if (propertySet != null)
			; // do nothing, just ensure the field is loaded by jdo
		if (structLocalScope != null)
			; // do nothing, just ensure the field is loaded by jdo
	}

//	/**
//	 * If <tt>maxProductCount</tt> has a value <tt>&gt;=0</tt>, this is the maximum number of
//	 * <tt>Product</tt>s that can be created and sold, To have an unlimited amount of
//	 * <tt>Product</tt>s available, set this to <tt>-1</tt>.
//	 *
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private long maxProductCount = -1;
//
//	/**
//	 * Keeps track, how many <tt>Product</tt>s have already been created. If this number
//	 * reaches <tt>maxProductCount</tt> and <tt>maxProductCount</tt> is a positive number,
//	 * the {@link SimpleProductTypeActionHandler} will stop to create new <tt>SimpleProduct</tt>s.
//	 *
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private long createdProductCount = 0;

//	/**
//	 * @jdo.field persistence-modifier="persistent" mapped-by="simpleProductType"
//	 */
//	private SimpleProductTypeName name;
//
//	/**
//	 * @return Returns the name.
//	 */
//	public I18nText getName()
//	{
//		return name;
//	}

//	/**
//	 * @see org.nightlabs.jfire.store.ProductType#isProductProvider()
//	 */
//	public boolean isProductProvider()
//	{
//		return true;
//	}

//	/**
//	 * @see org.nightlabs.jfire.store.ProductType#provideAvailableProduct()
//	 */
//	public Product provideAvailableProduct()
//	{
//		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//		if (pm == null)
//			throw new IllegalStateException("This instance of SimpleProductType is currently not persistent!");
//
//		if (maxProductCount >= 0) {
//			if (createdProductCount >= maxProductCount)
//				return null;
//		}
//
//		Store store = Store.getStore(pm);
//		if (!store.getOrganisationID().equals(this.getOrganisationID()))
//			throw new IllegalStateException("Cannot create a Product in a foreign datastore! this.organisationID=\""+getOrganisationID()+"\" != store.organisationID=\""+store.getOrganisationID()+"\"");
//
//		createdProductCount = createdProductCount + 1;
//
//		SimpleProduct newProduct = new SimpleProduct(this, store.createProductID());
//		return newProduct;
//	}

//	/**
//	 * @see org.nightlabs.jfire.store.ProductType#_checkProductAvailability()
//	 */
//	protected boolean _checkProductAvailability()
//	{
//		if (maxProductCount >= 0) {
//			if (createdProductCount >= maxProductCount)
//				return false;
//		}
//
//		return true;
//	}

//	/**
//	 * WORKAROUND Because of a JPOX bug, we have to re-set the member extendedProductType in the EJBean.
//	 *
//	 * @see org.nightlabs.jfire.store.ProductType#setExtendedProductType(org.nightlabs.jfire.store.ProductType)
//	 */
//	public void setExtendedProductType(ProductType extendedProductType)
//	{
//		super.setExtendedProductType(extendedProductType);
//	}


//	/**
//	 * @return Returns the createdProductCount.
//	 */
//	public long getCreatedProductCount()
//	{
//		return createdProductCount;
//	}
//	/**
//	 * @param createdProductCount The createdProductCount to set.
//	 */
//	public void setCreatedProductCount(long createdProductCount)
//	{
//		this.createdProductCount = createdProductCount;
//	}
//	/**
//	 * @return Returns the maxProductCount.
//	 */
//	public long getMaxProductCount()
//	{
//		return maxProductCount;
//	}
//	/**
//	 * @param maxProductCount The maxProductCount to set.
//	 */
//	public void setMaxProductCount(long maxProductCount)
//	{
//		this.maxProductCount = maxProductCount;
//	}


	// ******************************
	// /// *** begin inheritance *** ///
//	@Override
//	public FieldMetaData getFieldMetaData(String fieldName)
//	{
//		if ("createdProductCount".equals(fieldName))
//			return null;
//
//		return super.getFieldMetaData(fieldName);
//	}
//
//	@Override
//	public FieldInheriter getFieldInheriter(String fieldName)
//	{
//		if ("name".equals(fieldName))
//			return new InheritableFieldInheriter();
//
//		return super.getFieldInheriter(fieldName);
//	}
//
//	@Override
//	public void preInherit(Inheritable mother, Inheritable child)
//	{
//		super.preInherit(mother, child);
//		name.getI18nMap();
//	}
	// /// *** end inheritance *** ///
	// ******************************

}
