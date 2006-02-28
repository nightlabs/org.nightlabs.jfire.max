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

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jfire.store.id.ProductTypeGroupID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductTypeGroupID"
 *		detachable="true"
 *		table="JFireTrade_ProductTypeGroup"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeGroupID"
 *
 * @jdo.fetch-group name="ProductTypeGroup.name" fields="name"
 * @jdo.fetch-group name="ProductTypeGroup.managedByProductType" fields="managedByProductType"
 */
public class ProductTypeGroup
implements Serializable, DetachCallback
{
	public static final String FETCH_GROUP_NAME = "ProductTypeGroup.name";
	public static final String FETCH_GROUP_MANAGED_BY_PRODUCT_TYPE = "ProductTypeGroup.managedByProductType";

	/**
	 * This is not a real fetch-group, but it is handled manually in {@link #jdoPostDetach(Object)}.
	 */
	public static final String FETCH_GROUP_PRODUCT_TYPES = "ProductTypeGroup.productTypes";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String productTypeGroupID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="productTypeGroup"
	 */
	private ProductTypeGroupName name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductType managedByProductType = null;

	/**
	 * This should only be non-null AFTER detaching, if the method
	 * {@link #setIncludeProductTypes()} has been called before detaching.
	 *
	 * @see #jdoPostDetach(Object)
	 *
	 * @jdo.field persistence-modifier="none"
	 */
	private Collection productTypes = null;

	/**
	 * @deprecated
	 */
	protected ProductTypeGroup()
	{
	}

	public ProductTypeGroup(String organisationID, String productTypeGroupID)
	{
		this.organisationID = organisationID;
		this.productTypeGroupID = productTypeGroupID;
		this.primaryKey = getPrimaryKey(organisationID, productTypeGroupID);
		this.name = new ProductTypeGroupName(this);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getProductTypeGroupID()
	{
		return productTypeGroupID;
	}
	public static String getPrimaryKey(String organisationID, String productTypeGroupID)
	{
		return organisationID + '/' + productTypeGroupID;
	}
	public String getPrimaryKey()
	{
		return primaryKey;
	}

	public static Collection getProductTypes(PersistenceManager pm, ProductTypeGroupID productTypeGroupID)
	{
		Query q = pm.newNamedQuery(ProductType.class, "getProductTypesOfProductTypeGroup");
		return (Collection) q.execute(productTypeGroupID.organisationID, productTypeGroupID.productTypeGroupID);
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of ProductTypeGroup is currently not attached to a datastore! Cannot obtain PersistenceManager!");

		return pm;
	}

	/**
	 * This method is only accessible, if
	 * <ul>
	 *	<li>
	 *		the object is currently attached to the datastore,
	 *	</li>
	 *	<li>
	 *		{@link #setIncludeProductTypes(boolean)} has been called with value <code>true</code>, before
	 *		this object has been detached.
	 *	</li>
	 * </ul>
	 *
	 * @return Returns instances of {@link ProductType}.
	 */
	public Collection getProductTypes()
	{
		if (productTypes != null)
			return productTypes;

		try{
			return getProductTypes(getPersistenceManager(), (ProductTypeGroupID) JDOHelper.getObjectId(this));
		} catch (IllegalStateException x) {
			throw new IllegalStateException("It seems, you are calling getProductTypes() on a detached instance without having called loadProductTypes() before detaching it.", x);
		}
	}

	public ProductTypeGroupName getName()
	{
		return name;
	}

	/**
	 * @return Returns either <tt>null</tt> or the ProductType, by which this
	 *		ProductTypeGroup is managed.
	 *
	 * @see #setManagedByProductType(ProductType)
	 */
	public ProductType getManagedByProductType()
	{
		return managedByProductType;
	}
	/**
	 * This method calls {@link ProductType#setManagedProductTypeGroup(ProductTypeGroup)}.
	 *
	 * @param managedByProductType Either <tt>null</tt> or the ProductType to which this ProductTypeGroup will be linked.
	 */
	public void setManagedByProductType(ProductType managedByProductType)
	{
		managedByProductType.setManagedProductTypeGroup(this);
	}

	/**
	 * This method is called by {@link ProductType#setManagedProductTypeGroup(ProductTypeGroup)}. 
	 */
	protected void _setManagedByProductType(ProductType managedByProductType)
	{
		this.managedByProductType = managedByProductType;
	} 

	/**
	 * @see javax.jdo.listener.DetachCallback#jdoPreDetach()
	 */
	public void jdoPreDetach()
	{
	}

	/**
	 * @see javax.jdo.listener.DetachCallback#jdoPostDetach(java.lang.Object)
	 */
	public void jdoPostDetach(Object _attached)
	{
		ProductTypeGroup attached = (ProductTypeGroup)_attached;
		ProductTypeGroup detached = this;
		PersistenceManager pm = getPersistenceManager();
		Collection fetchGroups = pm.getFetchPlan().getGroups();
		if (fetchGroups.contains(FETCH_GROUP_PRODUCT_TYPES) || fetchGroups.contains(FetchPlan.ALL))
			detached.productTypes = pm.detachCopyAll(
					getProductTypes(pm, (ProductTypeGroupID) JDOHelper.getObjectId(attached)));
	}
}
