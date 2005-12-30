/*
 * Created on Nov 4, 2005
 */
package org.nightlabs.jfire.store;

import java.io.Serializable;

import org.nightlabs.jfire.transfer.Anchor;

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
	private Anchor home;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ProductTypeLocal() { }

	public ProductTypeLocal(ProductType productType, Anchor home)
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
	 * Home is an <code>Anchor</code> (normally a {@link Repository}), into which every newly created 
	 *
	 * @return
	 */
	public Anchor getHome()
	{
		return home;
	}
	public void setHome(Anchor home)
	{
		if (home == null)
			throw new IllegalArgumentException("home must not be null!");

		this.home = home;
	}
}
