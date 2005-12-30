/*
 * Created on Mar 2, 2005
 */
package org.nightlabs.ipanema.store;

import java.io.Serializable;

import org.nightlabs.ipanema.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.ipanema.store.id.ProductTypeStatusTrackerID"
 *		detachable="true"
 *		table="JFireTrade_ProductTypeStatusTracker"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeID"
 */
public class ProductTypeStatusTracker
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
	private ProductTypeStatus currentStatus;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int nextStatusID = 0;

	protected ProductTypeStatusTracker()
	{
	}

	public ProductTypeStatusTracker(ProductType productType, User user)
	{
		this.productType = productType;
		this.organisationID = productType.getOrganisationID();
		this.productTypeID = productType.getProductTypeID();

		newCurrentStatus(user);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the productTypeID.
	 */
	public String getProductTypeID()
	{
		return productTypeID;
	}
	/**
	 * @return Returns the productType.
	 */
	public ProductType getProductType()
	{
		return productType;
	}

	public void newCurrentStatus(User user)
	{
		currentStatus = new ProductTypeStatus(this, createStatusID(), user);
	}

	public ProductTypeStatus getCurrentStatus()
	{
		return currentStatus;
	}

	protected synchronized int createStatusID()
	{
		int res = nextStatusID;
		nextStatusID = res + 1;
		return res;
	}
}
