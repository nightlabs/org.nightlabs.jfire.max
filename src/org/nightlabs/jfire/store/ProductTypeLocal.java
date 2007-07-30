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

import org.nightlabs.jfire.security.User;

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
 */
public class ProductTypeLocal
implements Serializable
{
	// TODO: add field authority for security checking 
	private static final long serialVersionUID = 1L;

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
	 * @deprecated Only for JDO!
	 */
	protected ProductTypeLocal() { }

	// TODO we should pass the user here and store it somehow
	public ProductTypeLocal(User user, ProductType productType, Repository home)
	{
		if (productType == null)
			throw new IllegalArgumentException("productType must not be null!");

		this.productType = productType;
		this.organisationID = productType.getOrganisationID();
		this.productTypeID = productType.getProductTypeID();
		this.setHome(home);
		productType.setProductTypeLocal(this);
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
}
