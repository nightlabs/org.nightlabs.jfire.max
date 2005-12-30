/*
 * Created on Oct 25, 2005
 */
package org.nightlabs.ipanema.store;

import java.io.Serializable;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.ipanema.store.id.ProductReferenceGroupID"
 *		detachable="true"
 *		table="JFireTrade_ProductReferenceGroup"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, anchorTypeID, productReferenceGroupID, productOrganisationID, productProductID"
 */
public class ProductReferenceGroup
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
	private String anchorTypeID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="201"
	 */
	private String productReferenceGroupID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String productOrganisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private long productProductID;

	/**
	 * This must be in the range -1 &lt; quantity &lt; 1 at the end of a transaction. During the
	 * transaction, it might be more or less.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int quantity = 0;

	private ProductReference significantProductReference = null;
	
	public ProductReferenceGroup(String organisationID, String anchorTypeID, String productReferenceGroupID, String productOrganisationID, long productProductID)
	{
		this.organisationID = organisationID;
		this.anchorTypeID = anchorTypeID;
		this.productReferenceGroupID = productReferenceGroupID;
		this.productOrganisationID = productOrganisationID;
		this.productProductID = productProductID;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected ProductReferenceGroup() { }

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getAnchorTypeID()
	{
		return anchorTypeID;
	}
	public String getProductReferenceGroupID()
	{
		return productReferenceGroupID;
	}
	public String getProductOrganisationID()
	{
		return productOrganisationID;
	}
	public long getProductProductID()
	{
		return productProductID;
	}

	public String getPrimaryKey()
	{
		return organisationID + '/' + anchorTypeID + '/' + productReferenceGroupID + '/' + productOrganisationID + '/' + Long.toHexString(productProductID);
	}

	public int getQuantity()
	{
		return quantity;
	}
	protected void setQuantity(int quantity)
	{
		this.quantity = quantity;

		if (quantity == 0 && significantProductReference != null)
			significantProductReference = null;
	}

	/**
	 * @return Returns the ProductReference that caused {@link #quantity} to be 1 or -1. Returns <code>null</code>,
	 *		if quantity == 0.
	 */
	public ProductReference getSignificantProductReference()
	{
		return significantProductReference;
	}
	protected void setSignificantProductReference(
			ProductReference significantProductReference)
	{
		this.significantProductReference = significantProductReference;
	}
}
