/*
 * Created on Mar 2, 2005
 */
package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.Date;

import org.nightlabs.jfire.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductTypeStatusID"
 *		detachable="true"
 *		table="JFireTrade_ProductTypeStatus"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeID, statusID"
 */
public class ProductTypeStatus
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
	 * @jdo.field primary-key="true"
	 */
	private int statusID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductTypeStatusTracker productTypeStatusTracker;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductType productType;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User user;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date timestamp;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean published;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean confirmed;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean saleable;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean closed;

	protected ProductTypeStatus()
	{
	}

	public ProductTypeStatus(ProductTypeStatusTracker productTypeStatusTracker, int statusID, User user)
	{
		this.productTypeStatusTracker = productTypeStatusTracker;
		this.organisationID = productTypeStatusTracker.getOrganisationID();
		this.productTypeID = productTypeStatusTracker.getProductTypeID();
		this.statusID = statusID;
		this.productType = productTypeStatusTracker.getProductType();
		this.user = user;
		this.timestamp = new Date();

		this.published = productType.isPublished();
		this.confirmed = productType.isConfirmed();
		this.saleable = productType.isSaleable();
		this.closed = productType.isClosed();
	}
	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getProductTypeID()
	{
		return productTypeID;
	}
	public int getStatusID()
	{
		return statusID;
	}
	public ProductTypeStatusTracker getProductTypeStatusTracker()
	{
		return productTypeStatusTracker;
	}
	public ProductType getProductType()
	{
		return productType;
	}

	public User getUser()
	{
		return user;
	}
	public Date getTimestamp()
	{
		return timestamp;
	}

	public boolean isPublished()
	{
		return published;
	}
	public boolean isConfirmed()
	{
		return confirmed;
	}
	public boolean isSaleable()
	{
		return saleable;
	}
	public boolean isClosed()
	{
		return closed;
	}
}
